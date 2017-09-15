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

import gda.device.MotorStatus;
import gda.oe.MoveableStatus;

/**
 * A factory for creating MoveableStatuses from MotorStatuses. This is here so that knowledge of how Positioners work
 * remains confined to this package. MoveableStatus itself remains ignorant of the DOF/Positioner/Motor heirarchy and
 * indeed DOFs are ignorant of the existence of Motors. DO NOT return these methods to MoveableStatus.
 */

public class MoveableStatusFactory {
	/**
	 * @param status
	 * @return MoveableStatus
	 */
	public static MoveableStatus createMoveableStatus(MotorStatus status) {
		MoveableStatus ms = new MoveableStatus(convertValue(status));
		return ms;
	}

	/**
	 * @param status
	 * @param id
	 * @return MoveableStatus
	 */
	public static MoveableStatus createMoveableStatus(MotorStatus status, int id) {
		MoveableStatus ms = new MoveableStatus(convertValue(status), id);
		return ms;

	}

	/**
	 * @param status
	 * @param id
	 * @param message
	 * @return MoveableStatus
	 */
	public static MoveableStatus createMoveableStatus(MotorStatus status, int id, String message) {
		MoveableStatus ms = new MoveableStatus(convertValue(status), id, message);
		return ms;

	}

	/**
	 * @param status
	 * @param dofName
	 * @return MoveableStatus
	 */
	public static MoveableStatus createMoveableStatus(MotorStatus status, String dofName) {
		MoveableStatus ms = new MoveableStatus(convertValue(status), dofName);
		return ms;
	}

	/**
	 * Converts MotorStatus values into suitable MoveableStatus values.
	 * 
	 * @param motorStatus
	 *            the MotorStatus
	 * @return a suitable choice from the MoveableStatus static fields
	 */
	private static int convertValue(MotorStatus motorStatus) {
		int msv = MoveableStatus.ERROR;

		if (motorStatus == MotorStatus.FAULT || motorStatus == MotorStatus.UNKNOWN)
			msv = MoveableStatus.ERROR;
		else if (motorStatus == MotorStatus.BUSY)
			msv = MoveableStatus.BUSY;
		else if (motorStatus == MotorStatus.UPPER_LIMIT)
			msv = MoveableStatus.UPPERLIMIT;
		else if (motorStatus == MotorStatus.LOWER_LIMIT)
			msv = MoveableStatus.LOWERLIMIT;
		else
			msv = MoveableStatus.READY;

		return msv;
	}
}
