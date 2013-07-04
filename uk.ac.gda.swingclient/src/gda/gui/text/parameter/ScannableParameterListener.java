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

import gda.device.Scannable;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DOFParameterListener Class
 */
public class ScannableParameterListener implements VetoableChangeListener {
	
	private static final Logger logger = LoggerFactory.getLogger(ScannableParameterListener.class);
	
	Scannable scannable;

	/**
	 * @return The scannable that will be moved to the new position sent in the call to vetoableChange
	 */
	public Scannable getScannable() {
		return scannable;
	}

	/**
	 * @param scannable The scannable that will be moved to the new position sent in the call to vetoableChange
	 */
	public void setScannable(Scannable scannable) {
		this.scannable = scannable;
	}

	@Override
	public void vetoableChange(final PropertyChangeEvent e) throws PropertyVetoException {
		Object source = e.getSource();
		if (source == null)
			throw new IllegalArgumentException("ScannableParameterListener.propertyChange - source == null ");
		if (source instanceof ParametersPanelBuilder.ParameterChangeEventSource) {

			try {
				uk.ac.gda.util.ThreadManager.getThread(new Runnable() {
					@Override
					public void run() {
						try{
							scannable.moveTo(e.getNewValue());
						} catch (Exception ex) {
							logger.error(ex.getMessage(), ex);
						}
					}
				}).start();
			} catch (Exception ex) {
				logger.error(ex.getMessage(), ex);
				throw new PropertyVetoException(ex.getMessage(), e);
			}

		}
	}

}
