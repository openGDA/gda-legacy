/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

package gda.gui.text.parameter;

import gda.oe.MoveableStatus;
import gda.oe.dofs.DofMSListener;
import gda.oe.dofs.DofStatusNotifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * class to update the parameter value field of a ParametersPanelBuilder with dof value
 */
public class ObservableParameterMonitor implements DofMSListener {
	
	private static final Logger logger = LoggerFactory.getLogger(ObservableParameterMonitor.class);
	
	final private ParametersPanelBuilder builder;

	final private long limitedId;

	final boolean refreshDOFIfBusy;

	private volatile boolean monitorThreadRunning = false;

	private final DofStatusNotifier dofStatusNotifier;

	/**
	 * @param findableName
	 * @param dofName
	 * @param builder
	 * @param limitedId
	 */
	public ObservableParameterMonitor(String findableName, String dofName, ParametersPanelBuilder builder,
			long limitedId) {
		this(findableName, dofName, builder, limitedId, true);
	}

	/**
	 * @param findableName
	 * @param dofName
	 * @param builder
	 * @param limitedId
	 * @param refreshDOFIfBusy
	 */
	public ObservableParameterMonitor(String findableName, String dofName, ParametersPanelBuilder builder,
			long limitedId, boolean refreshDOFIfBusy) {
		this.builder = builder;
		this.limitedId = limitedId;
		this.refreshDOFIfBusy = refreshDOFIfBusy;
		this.dofStatusNotifier = new DofStatusNotifier(findableName, dofName, this, true);
		dofStatusNotifier.startUpdates();
	}

	private void setForeground(MoveableStatus status) {
		switch (status.value()) {
		case MoveableStatus.READY:
		case -1: // default
			builder.setForeground(limitedId, java.awt.Color.BLACK);
			builder.setEnabled(limitedId, true);
			break;
		case MoveableStatus.BUSY:
			builder.setForeground(limitedId, java.awt.Color.MAGENTA);
			builder.setEnabled(limitedId, false);
			break;
		default:
			builder.setForeground(limitedId, java.awt.Color.RED);
		}
	}

	private void setValue(double val) {
		builder.setParameterConnectedState(limitedId, true);
		builder.setParameterFromMonitor(limitedId, val);
	}

	@Override
	public void update(final DofStatusNotifier dofStatusNotifierIn, MoveableStatus moveableStatus) {
		if (this.dofStatusNotifier != dofStatusNotifierIn)
			logger.error("this.dofStatusNotifier != dofStatusNotifierIn "+ this.dofStatusNotifier.toString() + " - " + dofStatusNotifierIn.toString());

		setForeground(moveableStatus);
		setValue(moveableStatus.getPosition().getAmount());
		if (moveableStatus.value() == MoveableStatus.BUSY && !monitorThreadRunning && refreshDOFIfBusy) {
			monitorThreadRunning = true;
			uk.ac.gda.util.ThreadManager.getThread(new Runnable() {
				@Override
				public void run() {
					try {
						Thread.sleep(1000);
						dofStatusNotifier.refresh();
					} catch (Exception e) {
						logger.error(e.getMessage(),e);
					}
					monitorThreadRunning = false;
				}
			}).start();
		}
	}
}
