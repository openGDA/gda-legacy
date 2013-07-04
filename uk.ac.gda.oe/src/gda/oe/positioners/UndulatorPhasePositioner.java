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

package gda.oe.positioners;

import gda.device.DeviceException;
import gda.device.MotorException;
import gda.factory.FactoryException;

import org.jscience.physics.units.SI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extends LinearPositioner to provide special methods for setting and getting mode of Undulator via SR Control system.
 */
public class UndulatorPhasePositioner extends LinearPositioner {
	private static final Logger logger = LoggerFactory.getLogger(UndulatorPhasePositioner.class);

	// This is the amount (in mm) the UndulatorPhase can vary.
	/**
	 * 
	 */
	public static final double ZERO_PHASE_TOLERANCE = 0.001;

	private String currentMode = null;

	private boolean configured = false;

	private boolean reallyGetMode = true;

	@Override
	public void configure() {
		if (!configured) {
			try {
				super.configure();
				getMode();
				configured = true;
			} catch (FactoryException e) {
			}
		}
	}

	/**
	 * Returns the current phase mode. This may be obtained directly from the SR Control system or it may be a cached
	 * value.
	 * 
	 * @return Returns the phase mode as a string ("MUTUAL" or "OPPOSING").
	 */
	public String getMode() {
		if (reallyGetMode) {
			logger.debug("UndulatorPhasePositioner really getting mode from SRControlMotor");

			try {
				// Have to access the motor via its getAttribute method. Which
				// attribute to get depends on whether or not it is at zero.
				// Look
				// inside it for an explanation.

				String attribute = Math.abs(motor.getPosition()) < ZERO_PHASE_TOLERANCE ? "ZEROPHASEMODE" : "PHASEMODE";
				currentMode = (String) motor.getAttribute(attribute);
				reallyGetMode = false;
			} catch (MotorException me) {
				logger
						.error("UndulatorPhasePositioner.getMode() caught MotorException with message:"
								+ me.getMessage());
			} catch (DeviceException de) {
				logger.error("UndulatorPhasePositioner.getMode() caught DeviceException with message: "
						+ de.getMessage());
			}
		} else {
			logger.debug("UndulatorPhasePositioner returning alreay known mode");
		}
		return currentMode;
	}

	/**
	 * Physically sets the required motor phase mode and stores mode string
	 * 
	 * @param mode
	 *            The mode to set.
	 */
	public void setMode(String mode) {
		try {
			motor.setAttribute("PHASEMODE", mode);
			currentMode = mode;
		} catch (DeviceException e) {
			logger.error("UndulatorPhasePositioner.setMode() caught DeviceException with message: " + e.getMessage());
		}
	}

	/**
	 * @return boolean
	 */
	public boolean positionIsZero() {
		return getPosition().to(SI.MILLI(SI.METER)).getAmount() < ZERO_PHASE_TOLERANCE;
	}

	@Override
	public void refresh() {
		super.refresh();
		getMode();
	}
}