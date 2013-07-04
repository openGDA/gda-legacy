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

import gda.factory.Configurable;
import gda.lockable.Lockable;
import gda.observable.IObservable;

import java.util.ArrayList;

import org.jscience.physics.quantities.Quantity;
import org.jscience.physics.units.Unit;

/**
 * An interface to be implemented by things which move (eg DOFs and Positioners)
 * 
 * @see gda.oe.AbstractMoveable
 */
public interface Moveable extends IObservable, Lockable, Configurable {

	/**
	 * Check that an incremental move is allowed, calculate and set target positions and lock the Moveable for the
	 * command object. A MoveableStatus value is returned to indicate success or failure. The Moveable is locked in the
	 * case of success, not locked otherwise.
	 * 
	 * @param increment
	 *            The increment required.
	 * @param mover
	 *            The locking object.
	 * @return A MoveableStatus value.
	 */
	public int checkMoveBy(Quantity increment, Object mover);

	/**
	 * Check that an absolute move is allowed, calculate and set target positions and lock the Moveable for the command
	 * object. A MoveableStatus value is returned to indicate success or failure. The Moveable is locked in the case of
	 * success, not locked otherwise.
	 * 
	 * @param position
	 *            The abolute position required.
	 * @param mover
	 *            The locking object.
	 * @return A MoveableStatus value.
	 */
	public int checkMoveTo(Quantity position, Object mover);

	/**
	 * Check that a set position is allowed, calculate and set target positions and lock the Moveable for the command
	 * object. A MoveableStatus value is returned to indicate success or failure. The Moveable is locked in the case of
	 * success, not locked otherwise. NB In the terminology of some stations 'set position' means 'move to position'
	 * this is NOT the case for Moveables. A setPosition should only change the Moveable's idea of what its position is
	 * without moving anything.
	 * 
	 * @param position
	 *            The absolute position required.
	 * @param setter
	 *            The locking object.
	 * @return A MoveableStatus value.
	 */
	public int checkSetPosition(Quantity position, Object setter);

	/**
	 * Check that a Moveable is capable of homing, homing to the requested position would be allowed, and lock the
	 * Moveable for the command object. A MoveableStatus value is returned to indicate success or failure. The Moveable
	 * is locked in the case of success, not locked otherwise.
	 * 
	 * @param position
	 *            The required position at the homing point.
	 * @param mover
	 *            The locking object.
	 * @return A MoveableStatus value.
	 */
	public int checkHome(Quantity position, Object mover);

	/**
	 * Actually starts a move which has been previously checked and locked (by checkMoveTo or checkMoveBy). Should throw
	 * exception if mover is not the object which did the checking and locking. Failure of the move should also cause an
	 * exception.
	 * 
	 * @param mover
	 *            The locking object.
	 * @param id
	 *            The unique integer identifying the command requesting this move.
	 * @throws MoveableException
	 */
	public void doMove(Object mover, int id) throws MoveableException;

	/**
	 * Actually starts a position setting which has been previously checked and locked (by checkSet). Should throw
	 * exception if setter is not the object which did the checking and locking. Failure of the setting should also
	 * cause an exception. NB In the terminology of some stations 'set position' means 'move to position' this is NOT
	 * the case for Moveables. A setPosition should only change the Moveable's idea of what its position is without
	 * moving anything.
	 * 
	 * @param setter
	 *            The locking object.
	 * @throws MoveableException
	 */
	public void doSet(Object setter) throws MoveableException;

	/**
	 * Actually starts a homing move which has been previously checked and locked (by checkHome). Should throw exception
	 * if mover is not the object which did the checking and locking. Failure of the move should also cause an
	 * exception.
	 * 
	 * @param mover
	 *            The locking object.
	 * @throws MoveableException
	 */
	public void doHome(Object mover) throws MoveableException;

	/**
	 * Returns the current cached position of the motor
	 * 
	 * @return Current position in current reporting units as opposed to displayed units.
	 */
	public Quantity getPosition();

	/**
	 * Returns the current position cached postion of the motor
	 * 
	 * @param units
	 *            Requested units for position.
	 * @return Current position in requested units as Quantity.
	 */
	public Quantity getPosition(Unit<? extends Quantity> units);

	/**
	 * Stops current move.
	 * 
	 * @throws MoveableException
	 */
	public void stop() throws MoveableException;

	/**
	 * Returns whether moving or not.
	 * 
	 * @return boolean true if moving, false if not
	 * @throws MoveableException
	 */
	public boolean isMoving() throws MoveableException;

	/**
	 * Gets the status.
	 * 
	 * @return The status as MoveableStatus
	 */
	public MoveableStatus getStatus();

	/**
	 * Start a continuous move in specified direction. Implementations may handle the parameter as they please. Values
	 * are DOF.POSITIVE and DOF.NEGATIVE
	 * 
	 * @param direction
	 *            The direction of move.
	 * @throws MoveableException
	 */
	public void moveContinuously(int direction) throws MoveableException;

	/**
	 * Set the speed level. Implementations may handle the parameter as they please. Values are currently SLOW, MEDIUM
	 * or FAST
	 * 
	 * @see gda.device.Motor
	 * @param speed
	 *            The required speed level.
	 * @throws MoveableException
	 */
	public void setSpeedLevel(int speed) throws MoveableException;

	/**
	 * Return whether or not setting of speed is allowed.
	 * 
	 * @return boolean true if speed setting is allowed, false if not.
	 */
	public boolean isSpeedLevelSettable();

	/**
	 * Sets the name of this Moveable.
	 * 
	 * @param name
	 *            The String name of the Moveable
	 */
	public void setName(String name);

	/**
	 * Gets the name of this MOveable.
	 * 
	 * @return String The name.
	 */
	public String getName();

	/**
	 * Returns whether or not homing is allowed.
	 * 
	 * @return boolean true if can be homed, false if not.
	 */
	public boolean isHomeable();

	/**
	 * Sets the positionOffset value.
	 * 
	 * @param offset
	 * @throws MoveableException
	 */
	public void setPositionOffset(Quantity offset) throws MoveableException;

	/**
	 * Returns the positionOffset value.
	 * 
	 * @return The offset.
	 * @throws MoveableException
	 */
	public Quantity getPositionOffset() throws MoveableException;

	/**
	 * Sets the homeOffset value. This is the value of the position of the moveable after is has been homed (it is not
	 * necessarily 0).
	 * 
	 * @param offset
	 * @throws MoveableException
	 */
	public void setHomeOffset(Quantity offset) throws MoveableException;

	/**
	 * Returns the offset value.
	 * 
	 * @return The offset.
	 * @throws MoveableException
	 */
	public Quantity getHomeOffset() throws MoveableException;

	/**
	 * Forces a recalculation of the current position.
	 */
	public void refresh();

	/**
	 * Indicates whether the this Moveable should be available for use by users (returns true) or exists only to be used
	 * by other Moveables (returns false).
	 * 
	 * @return true if avaible for use, false otherwise
	 */
	public boolean isDirectlyUseable();

	/**
	 * Returns whether or not the Moveable can be scanned.
	 * 
	 * @return true if it can, false if not
	 */
	public boolean isScannable();

	/**
	 * Returns the current password protection level.
	 * 
	 * @return the password protection level
	 */
	public int getProtectionLevel();

	/**
	 * Sets the Reporting Units. The getPosition() method should return the position in these units
	 * 
	 * @param units
	 */
	public void setReportingUnits(Unit<? extends Quantity> units);

	/**
	 * Returns the Reporting Units.
	 * 
	 * @return the Reporting Units
	 */

	public Unit<? extends Quantity> getReportingUnits();

	/**
	 * Return the acceptable units
	 * 
	 * @return the ArrayList of acceptable units
	 */
	public ArrayList<Unit<? extends Quantity>> getAcceptableUnits();

	/**
	 * Returns whether or not the current position is valid. Implementations are free to decide how to determine this.
	 * 
	 * @return true if the position is valid, false otherwise.
	 */
	public boolean isPositionValid();

	/**
	 * Creates a MoveableCommandExecutor which will carry out an absolute move to the specified position.
	 * 
	 * @param position
	 *            the requested position
	 * @return the created MoveableCommandExecutor
	 * @throws MoveableException
	 *             if the MoveableCommandExecutor cannot be created
	 */
	public MoveableCommandExecutor createAbsoluteMover(Quantity position) throws MoveableException;

	/**
	 * Creates a MoveableCommandExecutor which will carry out a relative move by the specified increment.
	 * 
	 * @param increment
	 *            the requested increment
	 * @return the created MoveableCommandExecutor
	 * @throws MoveableException
	 *             if the MoveableCommandExecutor cannot be created
	 */
	public MoveableCommandExecutor createRelativeMover(Quantity increment) throws MoveableException;

	/**
	 * Creates a MoveableCommandExecutor which will set position to that specified
	 * 
	 * @param position
	 *            the specified position
	 * @return the created MoveableCommandExecutor
	 * @throws MoveableException
	 *             if the MoveableCommandExecutor cannot be created
	 */
	public MoveableCommandExecutor createPositionSetter(Quantity position) throws MoveableException;

	/**
	 * Creates a MoveableCommandExecutor which will carry out a homing move and set the position to that specified
	 * 
	 * @param position
	 *            the specified position
	 * @return the created MoveableCommandExecutor
	 * @throws MoveableException
	 *             if the MoveableCommandExecutor cannot be created
	 */
	public MoveableCommandExecutor createHomer(Quantity position) throws MoveableException;

	/**
	 * Formats the given position (a double) assuming it is the numerical value of a Quantity in current reporting
	 * units.
	 * 
	 * @param position
	 *            the position to format
	 * @return a suitably formatted string
	 */
	public String formatPosition(double position);

	/**
	 * Sets an ArrayList containing all the Moveables in the same collection as this one. NB This is NOT the list of
	 * Moveables which this Moveable uses. But it should be able to find the ones it uses in this list. This method is
	 * only necessary for backwards compatability because DOFs are not known to the Finder so though a DOF knows the
	 * names of the Moveables it uses from the XML it can only find the actual objects by being given a list containing
	 * them. Most implementations of Moveable can have a do-nothing method. NB This method has nothing to do with
	 * moveability and so should not be part of this interface but we are all victims of history.
	 * 
	 * @param moveableList
	 */
	public void setMoveableList(ArrayList<Moveable> moveableList);

	/**
	 * Returns the lowest value which this moveable can go to. This value could potentially change if attributes within
	 * this moveable are changed.
	 * 
	 * @return software lower limit as Quantity
	 */
	public Quantity getSoftLimitLower();

	/**
	 * Set the lowest value which this moveable can go to. This value could potentially change if attributes within this
	 * moveable are changed.
	 * 
	 * @return software upper limit as Quantity
	 */
	public Quantity getSoftLimitUpper();

	/**
	 * Sets the speed of the Moveable
	 * 
	 * @param speed
	 *            the speed Returns the highest value which this moveable can go to.
	 * @throws MoveableException
	 */
	public void setSpeed(Quantity speed) throws MoveableException;

	/**
	 * Gets the speed of the Moveable
	 * 
	 * @return speed as Quantity
	 */
	public Quantity getSpeed();

	/**
	 * Allows the Moveable to calculate and set its own speed from the given range parameters.
	 * 
	 * @param start
	 *            the start of the movement
	 * @param end
	 *            the end of the movement
	 * @param time
	 *            the time the movement should take
	 * @throws MoveableException
	 */
	public void setSpeed(Quantity start, Quantity end, Quantity time) throws MoveableException;

	/**
	 * Saves the current speed of the Moveable for later restoration.
	 */
	public void pushSpeed();

	/**
	 * Restores the speed stored by pushSpeed.
	 */
	public void popSpeed();

	/**
	 * Get a hardware specific attribute
	 * 
	 * @param name
	 *            the String name of the attribute
	 * @return the value of the attribute as an object
	 * @throws MoveableException
	 */
	public Object getAttribute(String name) throws MoveableException;

	/**
	 * Set a hardware specific attribute
	 * 
	 * @param name
	 *            the String name of the attribute
	 * @param o
	 *            the value of the attribute as an object
	 * @throws MoveableException
	 */
	public void setAttribute(String name, Object o) throws MoveableException;
	
	// methods for allowing the beamline configuration manager parameters to be configured in CASTOR

	/**
	 * gets the lower limits for the device, used in the Beamline Configuration Manager
	 * @return The lower limits
	 */
	public double[] getLowerGdaLimits();

	/**
	 * gets the Tolerance settings for the device, used in the Beamline Configuration Manager
	 * @return The tolerance
	 */
	public double[] getTolerance();

	/**
	 * gets the upper limits for the device, used in the Beamline Configuration Manager
	 * @return the upper limits
	 */
	public double[] getUpperGdaLimits();

	/**
	 * sets the lower limits for the device, used in the Beamline Configuration Manager
	 * @param lowerLim 
	 * @throws MoveableException 
	 */
	public void setLowerGdaLimits(double[] lowerLim) throws MoveableException;

	/**
	 * sets the lower limits for the device, used in the Beamline Configuration Manager
	 * @param lowerLim 
	 * @throws MoveableException 
	 */
	public void setLowerGdaLimits(double lowerLim) throws MoveableException;

	/**
	 * sets the Tolerance settings for the device, used in the Beamline Configuration Manager
	 * @param tolerance 
	 * @throws MoveableException 
	 */
	public void setTolerance(double[] tolerance) throws MoveableException;


	/**
	 * sets the Tolerance settings for the device, used in the Beamline Configuration Manager
	 * @param tolerance 
	 * @throws MoveableException 
	 */
	public void setTolerance(double tolerance) throws MoveableException;

	/**
	 * sets the upper limits for the device, used in the Beamline Configuration Manager
	 * @param upperLim 
	 * @throws MoveableException 
	 */
	public void setUpperGdaLimits(double[] upperLim) throws MoveableException;

	/**
	 * sets the upper limits for the device, used in the Beamline Configuration Manager
	 * @param upperLim 
	 * @throws MoveableException 
	 */
	public void setUpperGdaLimits(double upperLim) throws MoveableException;
	
	

	/**
	 * @return Returns the docString.
	 * @throws MoveableException 
	 */
	public String getDocString() throws MoveableException;

	/**
	 * @param docString The docString to set.
	 * @throws MoveableException 
	 */
	public void setDocString(String docString) throws MoveableException;
	

}