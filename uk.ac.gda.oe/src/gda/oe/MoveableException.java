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
 * An exception class that can be thrown by Moveables.
 */
public class MoveableException extends java.lang.Exception {
	final private MoveableStatus ms;

	/**
	 * @param ms
	 * @param message
	 * @param cause
	 */
	public MoveableException(MoveableStatus ms, String message, Throwable cause) {
		super(message, cause);
		this.ms = ms;
	}

	/**
	 * @param ms
	 * @param message
	 */
	public MoveableException(MoveableStatus ms, String message) {
		this(ms, message, null);
	}

	/**
	 * Returns the MoveableStatus
	 * 
	 * @return the MoveableStatus
	 */
	public MoveableStatus getMoveableStatus() {
		return ms;
	}

	/**
	 * Returns a message which describes the exception. If the Exception's own message string is set it is returned,
	 * otherwise the 'better than nothing' message string of the MoveableStatus is returned.
	 * 
	 * @return the message string
	 */
	@Override
	public String getMessage() {
		String message = super.getMessage();
		if (message == null) {
			message = ms.getMessage();
		}
		return message;
	}
}
