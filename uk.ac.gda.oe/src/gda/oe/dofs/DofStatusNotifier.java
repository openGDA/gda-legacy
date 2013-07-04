/*-
 * Copyright © 2009 Diamond Light Source Ltd.
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */

package gda.oe.dofs;

import gda.device.Scannable;
import gda.factory.Finder;
import gda.observable.IObservable;
import gda.observable.IObserver;
import gda.oe.MoveableStatus;
import gda.oe.OE;
import gda.util.exceptionUtils;

import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * class to notify DofMSListeners of moveable status changes to dofs
 */
public class DofStatusNotifier implements IObserver {
	
	private static final Logger logger = LoggerFactory.getLogger(DofStatusNotifier.class);
	
	final DofMSListener listener;
	private final boolean updateInSwingEventThread;
	/**
	 * 
	 */
	public Object o;
	/**
	 * 
	 */
	public OE oe;
	/**
	 * 
	 */
	final public String dofName;

	Scannable s;
	/**
	 * @param oeName
	 * @param dofName
	 * @param listener
	 * @param updateInSwingEventThread
	 */
	@SuppressWarnings("cast")
	public DofStatusNotifier(String oeName, String dofName, DofMSListener listener, boolean updateInSwingEventThread) {
		if (oeName == null || dofName == null || listener == null)
			throw new IllegalArgumentException("dofEventRegister - invalid parameters");
		this.listener = listener;
		this.updateInSwingEventThread = updateInSwingEventThread;
		this.dofName = dofName;
		o = Finder.getInstance().find(oeName);
		if (o == null) {
			throw new IllegalArgumentException("ObservableParameterMonitor. unable to find oe " + oeName);
		}
		
		if (o instanceof OE) {
			oe = (OE) o;
			String names[] = oe.getDOFNames();
			boolean found = false;
			for (String name : names) {
				if (name.equals(dofName)) {
					found = true;
					break;
				}
			}
			if (!found) {
				throw new IllegalArgumentException("ObservableParameterMonitor. OE:" + oeName + " does not contain dof "
						+ dofName);
			}
			if (!(oe instanceof IObservable)) {
				throw new IllegalArgumentException("ObservableParameterMonitor. " + oeName + " is not an IObservable");
			}
		} 
	}

	/**
	 * 
	 */
	public void startUpdates(){
		if( o != null){
			if( oe != null)
			try {
				sendMoveableStatus(oe.getStatus(dofName));
			} catch (Exception e) {
				exceptionUtils.logException(logger, "Error calling getStatus for " + dofName, e);
			}
			((IObservable) o).addIObserver(this);
		}
	}
	private void sendMoveableStatus(MoveableStatus ms) {
		if (updateInSwingEventThread) {
			SwingUtilities.invokeLater(new RunLater(this, ms));
		} else {
			listener.update(this, ms);
		}
	}
	/**
	 * @throws Exception
	 */
	public void refresh() throws Exception{
		if( oe != null)
			oe.refresh(dofName);
	}
	@Override
	public void update(Object theObserved, Object changeCode) {
		if (changeCode instanceof MoveableStatus) {
			MoveableStatus ms = (MoveableStatus) changeCode;
			if (ms.getMoveableName().equals(dofName)) {
				sendMoveableStatus(ms);
			}
		} 
	}
}

class RunLater implements Runnable {
	private final DofStatusNotifier dofStatusNotifier;
	private final MoveableStatus ms;

	RunLater(DofStatusNotifier dofStatusNotifier, MoveableStatus ms) {
		this.dofStatusNotifier = dofStatusNotifier;
		this.ms = ms;
	}

	@Override
	public void run() {
		dofStatusNotifier.listener.update(dofStatusNotifier, ms);
	}
}