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

package gda.oe;

/**
 * BeamlineStatus Class
 */
public class BeamlineStatus {
	/**
	 * 
	 */
	public static final int SAFE = 0;

	/**
	 * 
	 */
	public static final int RESTORED = 1;

	private static final String[] messages = { "Safe", "Restored" };

	private int status;

	private String message;

	/**
	 * 
	 */
	public static BeamlineStatus safe = new BeamlineStatus(BeamlineStatus.SAFE);

	/**
	 * 
	 */
	public static BeamlineStatus restored = new BeamlineStatus(BeamlineStatus.RESTORED);

	/**
	 * @param status
	 */
	public BeamlineStatus(int status) {
		this.status = status;
		this.message = messages[status];
	}

	/**
	 * @return status
	 */
	public int getStatus() {
		return status;
	}

	/**
	 * @return message
	 */
	public String getMessage() {
		return message;
	}
}
