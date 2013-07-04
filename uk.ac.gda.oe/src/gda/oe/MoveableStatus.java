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

import java.io.Serializable;

import org.jscience.physics.quantities.Quantity;

/**
 * A class containing information on the status of a Moveable.
 */
final public class MoveableStatus implements Serializable {
	/**
	 * 
	 */
	public final static int SUCCESS = 0;

	/**
	 * 
	 */
	public final static int ERROR = 1;

	/**
	 * 
	 */
	public final static int BUSY = 2;

	/**
	 * 
	 */
	public final static int READY = 3;

	/**
	 * 
	 */
	public final static int POSITION_INVALID = 4;

	/**
	 * 
	 */
	public final static int CONFIGURATION_FAILURE = 5;

	/**
	 * 
	 */
	public final static int UPPERLIMIT = 6;

	/**
	 * 
	 */
	public final static int LOWERLIMIT = 7;

	/**
	 * 
	 */
	public final static int UNCHANGED = 8;

	/**
	 * 
	 */
	public final static int INCORRECT_QUANTITY = 9;

	/**
	 * 
	 */
	public final static int NOTLOCKED = 10;

	/**
	 * 
	 */
	public final static int STATUS_ERROR = 11;

	/**
	 * 
	 */
	public final static int INTO_LIMIT = 12;

	/**
	 * 
	 */
	public final static int SOFT_LIMIT = 13;

	/**
	 * 
	 */
	public final static int ALREADY_LOCKED = 14;

	/**
	 * 
	 */
	public final static int AWAY_FROM_LIMIT = 15;

	/**
	 * 
	 */
	public final static int MOVE_TYPE_NOT_SUPPORTED = 16;

	/**
	 * 
	 */
	public final static int SPEEDLEVEL_ERROR = 17;

	/**
	 * 
	 */
	public final static int COMMUNICATION_FAILURE = 18;

	/**
	 * 
	 */
	public final static int NOT_HOMEABLE = 19;

	/**
	 * 
	 */
	public final static int MOVEABLENAME_INVALID = 20;

	/**
	 * 
	 */
	public final static int MOVE_NOT_ALLOWED = 21;
	
	/**
	 * Creates a human readable message based on the error value
	 * 
	 * @param value
	 * @return String
	 */
	public static String mapIntToMessage(int value) {
		String returnMessage = "";
		
		switch (value) {
		case BUSY:
			returnMessage = "Busy";
			break;

		case READY:
			returnMessage = new String("Ready");
			break;

		case ERROR:
			returnMessage = new String("Some fundamental error has occured with " + "the drive hardware behind this DOF.\n"
					+ "Consult station personnel.\n");
			break;
		case POSITION_INVALID:
			returnMessage = new String("The DOF is in an invalid position.\n" + "To move it set the position and try again.");
			break;
		case CONFIGURATION_FAILURE:
			returnMessage = new String("The OEs initial configuration failed.\n" + "There may be something wrong with the "
					+ "resource data.\n" + "Check this and try again.");
			break;
		case INCORRECT_QUANTITY:
			returnMessage = new String("DOF has been sent a Quantity of the wrong type");
			break;
		case UPPERLIMIT:
			returnMessage = new String("At least one motor has hit its upper " + "limit.\n");
			break;
		case LOWERLIMIT:
			returnMessage = new String("At least one motor has hit its lower " + "limit.\n");
			break;
		case UNCHANGED:
			returnMessage = new String("DOF is already in position required .\n");
			break;
		case NOTLOCKED:
			returnMessage = new String("An attempt to move DOF  has been made without previously checking and locking");
			break;
		case STATUS_ERROR:
		case INTO_LIMIT:
		case SOFT_LIMIT:
		case ALREADY_LOCKED:
			returnMessage = new String("DOF will not move or setPosition.\n");
			switch (value) {
			case STATUS_ERROR:
				returnMessage += "The current status of a Moveable will not allow it.";
				break;
			case INTO_LIMIT:
				returnMessage += "A Moveable is driving into a limit.";
				break;
			case SOFT_LIMIT:
				returnMessage += "A requested position is outside soft limits.";
				break;
			case ALREADY_LOCKED:
				returnMessage += "A Moveable is already locked.";
				break;
			}
			break;
		case AWAY_FROM_LIMIT:
			returnMessage = "A moveable is moving away from a limit.\n";
			break;
		case MOVE_TYPE_NOT_SUPPORTED:
			returnMessage = new String("DOF does not support the requested move type.\n");
			break;
		case SPEEDLEVEL_ERROR:
			returnMessage = new String("DOF does not support the requested speed level.\n");
			break;

		case NOT_HOMEABLE:
			returnMessage = new String("DOF does not support the home move.\n");
			break;
		case MOVEABLENAME_INVALID:
			returnMessage = "The DOF name is not recognised";
			break;
		case MOVE_NOT_ALLOWED:
			returnMessage = "The requested move is not allowed.\n Consult station personnel";
			break;
		}
		return returnMessage;
	}

	
	

	private int value = ERROR;

	private int id = -1;

	private String message = null;

	private String moveableName = null;

	private String position = "0.0";

	// Constructors.

	/**
	 * @param other
	 * @return boolean
	 */
	public boolean equals(MoveableStatus other) {
		if (other == null) {
			throw new IllegalArgumentException("MoveableStatus.equals: other is null");
		}
		return value == other.value
				&& id == other.id
				&& ((message == other.message) || ((message != null && other.message != null) ? message
						.equals(other.message) : false))
				&& ((moveableName == other.moveableName) || ((moveableName != null && other.moveableName != null) ? moveableName
						.equals(other.moveableName)
						: false))
				&& ((position == other.position) || ((position != null && other.position != null) ? position
						.equals(other.position) : false));
	}

	@SuppressWarnings("unused")
	private MoveableStatus() {
	}

	/**
	 * @param value
	 */
	public MoveableStatus(int value) {
		setMoveableName(null);
		setMessage(value);
	}

	/**
	 * @param value
	 * @param moveableName
	 */
	public MoveableStatus(int value, String moveableName) {
		setMoveableName(moveableName);
		setMessage(value);
	}

	/**
	 * @param value
	 * @param moveableName
	 * @param id
	 */
	public MoveableStatus(int value, String moveableName, int id) {
		setMoveableName(moveableName);
		setMessage(value);
		setID(id);
	}

	/**
	 * @param value
	 * @param id
	 */
	public MoveableStatus(int value, int id) {
		setMessage(value);
		setID(id);
	}

	/**
	 * @param value
	 * @param id
	 * @param message
	 */
	public MoveableStatus(int value, int id, String message) {
		this.value = value;
		this.message = message;
		setID(id);
	}

	/**
	 * @param value
	 * @param moveableName
	 * @param position
	 */
	public MoveableStatus(int value, String moveableName, Quantity position) {
		setMoveableName(moveableName);
		setMessage(value);
		setPosition(position);
	}

	/**
	 * @param value
	 * @param moveableName
	 * @param position
	 * @param id
	 */
	public MoveableStatus(int value, String moveableName, Quantity position, int id) {
		setMoveableName(moveableName);
		setMessage(value);
		setPosition(position);
		setID(id);
	}

	/**
	 * @param value
	 * @param moveableName
	 * @param position
	 * @param id
	 * @param message
	 */
	public MoveableStatus(int value, String moveableName, Quantity position, int id, String message) {
		setMoveableName(moveableName);
		if (message == null) {
			setMessage(value);
		} else {
			this.value = value;
			this.message = message;
		}
		setPosition(position);
		setID(id);
	}

	// Private utility methods.

	private void setMoveableName(String moveableName) {
		if (moveableName != null)
			this.moveableName = moveableName;
		else
			this.moveableName = new String("notset");
	}

	private void setMessage(int value) {
		this.value = value;

		this.message = mapIntToMessage(value);
	}

	private void setPosition(Quantity position) {
		this.position = position.toString();
	}

	private void setID(int id) {
		this.id = id;
	}

	// Public setters & getters.

	/**
	 * @return value
	 */
	public int value() {
		return value;
	}

	/**
	 * @return id
	 */
	public int id() {
		return id;
	}

	/**
	 * @return message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @return position as Quantity
	 */
	public Quantity getPosition() {
		return Quantity.valueOf(position);
	}

	/**
	 * @return moveable name
	 */
	public String getMoveableName() {
		return moveableName;
	}

	@Override
	public String toString() {
		// make sure null name does not get written to output
		if (moveableName == null) {
			return "MoveableStatus from empty moveable" + " message is: " + message + " id is " + id;
		}

		return "MoveableStatus from " + moveableName + " message is: " + message + " id is " + id;

	}
}
