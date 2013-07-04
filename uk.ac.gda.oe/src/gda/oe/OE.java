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

import gda.factory.Findable;
import gda.jython.accesscontrol.MethodAccessProtected;
import gda.lockable.Locker;
import gda.observable.IObservable;

import java.util.ArrayList;

import org.jscience.physics.quantities.Quantity;
import org.jscience.physics.units.Unit;

/**
 * An interface for all Optical Elements.
 */
public interface OE extends Findable, IObservable {
	/**
	 * Get the type of DOF.
	 * 
	 * @param dofName
	 *            the name of the Moveable
	 * @return the DOF type.
	 * @throws MoveableException
	 */
	public String getDOFType(String dofName) throws MoveableException;

	/**
	 * Get the names of all DOFs available for this OE.
	 * 
	 * @return An array of DOF names.
	 */
	public String[] getDOFNames();

	/**
	 * Move a named DOF by an incremental amount. Invalid units for the increment will result in an exception.
	 * 
	 * @param dofname
	 *            The DOF to be moved.
	 * @param increment
	 *            The incremental move required, units specified in the Quantity itself.
	 * @throws MoveableException
	 * @see org.jscience.physics.quantities.Quantity
	 */
	@MethodAccessProtected(isProtected=true)
	public void moveBy(String dofname, Quantity increment) throws MoveableException;

	/**
	 * Move a named DOF to a position. Invalid units for the position will result in an exception.
	 * 
	 * @param dofname
	 *            The DOF to be moved.
	 * @param position
	 *            The position required, units specified in the Quantity itself.
	 * @throws MoveableException
	 * @see org.jscience.physics.quantities.Quantity
	 */
	@MethodAccessProtected(isProtected=true)
	public void moveTo(String dofname, Quantity position) throws MoveableException;

	/**
	 * Move a locked DOF of to a required position. The required DOF is assumed to already be externally locked. The
	 * passed lockId gives a handle to unlock this DOF. Upon completion of the move the DOF will again be locked, but a
	 * new lockId is required to unlock it in future. This new lockId is the returned argument. Invalid units for the
	 * position will result in an exception.
	 * 
	 * @param dofname
	 *            The DOF to be moved.
	 * @param position
	 *            The position required, units specified in the Quantity itself.
	 * @param lockId
	 *            Handle on the existing lock on this DOF.
	 * @throws MoveableException
	 * @return A new lockId for the DOF after completion of the move.
	 * @see org.jscience.physics.quantities.Quantity
	 */
	@MethodAccessProtected(isProtected=true)
	public int moveLockedTo(String dofname, Quantity position, int lockId) throws MoveableException;

	/**
	 * Move a named DOF continuously. The sign of the direction parameter specifies which direction the continuous move
	 * will be in. Positive values equate to positive user units. Negative values equate to negative user units. The
	 * actual value should not matter. A deliberate stop is required to end the move.
	 * 
	 * @param dofname
	 *            The DOF to be moved.
	 * @param direction
	 *            Specifies the direction required as a positive or negative value.
	 * @throws MoveableException
	 * @see #stop()
	 * @see #stop(String)
	 */
	@MethodAccessProtected(isProtected=true)
	public void moveContinuously(String dofname, int direction) throws MoveableException;

	/**
	 * Stop an existing move of a named DOF as soon as possible.
	 * 
	 * @param dofname
	 *            The DOF to be stopped
	 * @throws MoveableException
	 */
	public void stop(String dofname) throws MoveableException;

	/**
	 * Stop all existing DOF moves as soon as possible.
	 * 
	 * @throws MoveableException
	 */
	public void stop() throws MoveableException;

	/**
	 * Checks whether any DOFs of this OE are moving.
	 * 
	 * @return true if any DOFs are moving, false if not.
	 * @throws MoveableException
	 */
	public boolean isMoving() throws MoveableException;

	/**
	 * Checks whether specified DOF of this OE is moving.
	 * 
	 * @param dofname
	 *            The DOF to check
	 * @return true if any DOFs are moving, false if not.
	 * @throws MoveableException
	 */
	public boolean isMoving(String dofname) throws MoveableException;

	/**
	 * Sets the position of a named DOF. Invalid units for the position will result in an exception.
	 * 
	 * @param dofname
	 *            The DOF to be set.
	 * @param position
	 *            The position required, units specified in the Quantity itself.
	 * @throws MoveableException
	 * @see org.jscience.physics.quantities.Quantity
	 */
	@MethodAccessProtected(isProtected=true)
	public void setPosition(String dofname, Quantity position) throws MoveableException;

	/**
	 * Gets the position of a named DOF in it's default reporting units.
	 * 
	 * @param dofname
	 *            The DOF the position is required for.
	 * @return The DOFs position as a Quantity.
	 * @throws MoveableException
	 * @see org.jscience.physics.quantities.Quantity
	 */
	public Quantity getPosition(String dofname) throws MoveableException;

	/**
	 * Gets the position of a DOF in named reporting units.
	 * 
	 * @param dofname
	 *            The DOF the position is required for.
	 * @param units
	 *            The units the DOF's position is required in.
	 * @return The DOF's position as a Quantity.
	 * @throws MoveableException
	 * @see org.jscience.physics.quantities.Quantity
	 * @see org.jscience.physics.units.Unit
	 */
	public Quantity getPosition(String dofname, Unit<? extends Quantity> units) throws MoveableException;

	/**
	 * Converts the position of a named DOF to a formatted string..
	 * 
	 * @param dofname
	 *            The DOF the formatting is required for.
	 * @param position
	 *            The DOF's unformatted position.
	 * @return The DOF's position as a formatted string.
	 * @throws MoveableException
	 */
	public String formatPosition(String dofname, double position) throws MoveableException;

	/**
	 * Determines if the position of an OE's named DOF is valid. The criterion for validity will be defined within
	 * individual DOFs.
	 * 
	 * @param dofname
	 *            The DOF the validity is required for.
	 * @return true = position valid, false = position invalid.
	 * @throws MoveableException
	 */
	public boolean isPositionValid(String dofname) throws MoveableException;

	/**
	 * Determines if the speed level of an OE's named DOF can be set.
	 * 
	 * @param dofname
	 *            The DOF the ability to set speed level is required for.
	 * @return true = speed level can be set, false = speed level cannot be set.
	 * @throws MoveableException
	 */
	public boolean isSpeedLevelSettable(String dofname) throws MoveableException;

	/**
	 * Get the list of units available for operations on a named DOF.
	 * 
	 * @param dofname
	 *            The DOF units are required for.
	 * @return An array of acceptable units.
	 * @throws MoveableException
	 * @see org.jscience.physics.units.Unit
	 */
	public ArrayList<Unit<? extends Quantity>> getAcceptableUnits(String dofname) throws MoveableException;

	/**
	 * Set the default reporting units for a named DOF.
	 * 
	 * @param dofname
	 *            The DOF the reporting units are to be set for.
	 * @param units
	 *            The required reporting units.
	 * @throws MoveableException
	 * @see org.jscience.physics.units.Unit
	 */
	public void setReportingUnits(String dofname, Unit<? extends Quantity> units) throws MoveableException;

	/**
	 * Get the current default reporting units for a named DOF.
	 * 
	 * @param dofname
	 *            The DOF the reporting units are to be got for.
	 * @return The default reporting units for the named DOF.
	 * @throws MoveableException
	 * @see org.jscience.physics.units.Unit
	 */
	public Unit<? extends Quantity> getReportingUnits(String dofname) throws MoveableException;

	/**
	 * Get the current protection level for a named DOF. The protection level specifies what password is required (if
	 * any) to allow the DOF's use.
	 * 
	 * @param dofname
	 *            The DOF the protection level is required for.
	 * @return The protection level for the named DOF.
	 * @throws MoveableException
	 */
	public int getProtectionLevel(String dofname) throws MoveableException;

	/**
	 * Get the current status for a named DOF.
	 * 
	 * @param dofname
	 *            The DOF the status is required for.
	 * @return The status for the named DOF.
	 * @throws MoveableException
	 * @see gda.oe.MoveableStatus
	 */
	public MoveableStatus getStatus(String dofname) throws MoveableException;

	/**
	 * Set the speed of the named DOF
	 * 
	 * @param dofname
	 *            The DOF the speed is to be set for.
	 * @param speed
	 *            The speed in
	 * @throws MoveableException
	 */
	public void setSpeed(String dofname, Quantity speed) throws MoveableException;

	/**
	 * Allows the named DOF to set its own speed from the given range information.
	 * 
	 * @param dofname
	 *            The DOF the speed is to be set for.
	 * @param start
	 *            the start of the movement range
	 * @param end
	 *            the end of the movement range
	 * @param time
	 *            the time the movement should take
	 * @throws MoveableException
	 */
	public void setSpeed(String dofname, Quantity start, Quantity end, Quantity time) throws MoveableException;

	/**
	 * Get the speed of the named DOF
	 * 
	 * @param dofname
	 *            name of the DOF
	 * @return speed
	 * @throws MoveableException
	 */
	public Quantity getSpeed(String dofname) throws MoveableException;

	/**
	 * Set the speed of a named DOF to one of a range of levels.
	 * 
	 * @param dofname
	 *            The DOF the speed level is to be set for.
	 * @param speed
	 *            the required speed level e.g. slow, medium, fast for constant see:
	 * @see gda.device.Motor
	 * @throws MoveableException
	 */
	public void setSpeedLevel(String dofname, int speed) throws MoveableException;

	/**
	 * Moves the named DOF to a known home position and sets the position. The actual mechanism for positioning the DOF
	 * at its home is likely to use some sort of hardware reference. However at this level, the mechanism is not
	 * important.
	 * 
	 * @param dofName
	 *            The DOF which is to be homed.
	 * @throws MoveableException
	 */
	public void home(String dofName) throws MoveableException;

	/**
	 * Get the offset to be applied to the home position for a named DOF. When homing, the DOF's position will be offset
	 * by this value.
	 * 
	 * @param dofName
	 *            The DOF to get the offset for.
	 * @return The homing offset.
	 * @throws MoveableException
	 * @see org.jscience.physics.quantities.Quantity
	 */
	public Quantity getHomeOffset(String dofName) throws MoveableException;

	/**
	 * Set the offset to be applied to the home position for a named DOF. When homing, the DOF's position will be offset
	 * by this value.
	 * 
	 * @param dofName
	 *            The DOF to set the offset for.
	 * @param offset
	 *            The homing offset.
	 * @throws MoveableException
	 * @see org.jscience.physics.quantities.Quantity
	 */
	public void setHomeOffset(String dofName, Quantity offset) throws MoveableException;

	/**
	 * Get the offset to be applied to the position for a named DOF.
	 * 
	 * @param dofName
	 *            The DOF to get the offset for.
	 * @return The offset.
	 * @throws MoveableException
	 * @see org.jscience.physics.quantities.Quantity
	 */
	public Quantity getPositionOffset(String dofName) throws MoveableException;

	/**
	 * Set the offset to be applied to the position for a named DOF.
	 * 
	 * @param dofName
	 *            The DOF to set the offset for.
	 * @param offset
	 *            The offset.
	 * @throws MoveableException
	 * @see org.jscience.physics.quantities.Quantity
	 */
	public void setPositionOffset(String dofName, Quantity offset) throws MoveableException;

	/**
	 * Impose a software lock on operations on a named DOF.
	 * 
	 * @param dofname
	 *            The DOF to impose lock on.
	 * @return The lock id.
	 * @throws MoveableException
	 */
	public int lock(String dofname) throws MoveableException;

	/**
	 * Impose a software lock on operations on a named DOF, using a specified locker to generate the lock id.
	 * 
	 * @param dofname
	 *            The DOF to impose a lock on.
	 * @param locker
	 *            The locker to be used to generate the lock id.
	 * @return The lock id.
	 * @throws MoveableException
	 */
	public int lock(String dofname, Locker locker) throws MoveableException;

	/**
	 * Release a software lock on a named DOF.
	 * 
	 * @param dofname
	 *            The DOF to release the lock on.
	 * @param lockId
	 *            The lock id.
	 * @throws MoveableException
	 */
	public void unlock(String dofname, int lockId) throws MoveableException;

	/**
	 * @param dofname
	 * @param position
	 * @return MoveableStatus code
	 * @throws MoveableException
	 */
	public int moveCheck(String dofname, Quantity position) throws MoveableException;

	/**
	 * Determines if the named DOF is scannable. The criterion for validity will be defined within individual DOFs.
	 * 
	 * @param dofname
	 *            The DOF the validity is required for.
	 * @return true = scannable valid, false = not scannable.
	 * @throws MoveableException
	 */
	public boolean isScannable(String dofname) throws MoveableException;

	/**
	 * Refresh the specified dof's current position by getting its moveable to recalculate their positions.
	 * 
	 * @param dofname
	 *            the dofname to refresh
	 * @throws MoveableException
	 */
	public void refresh(String dofname) throws MoveableException;

	/**
	 * Returns the lower limit of the given Moveable in its current reporting units.
	 * 
	 * @param dofname
	 * @return Lower limit of the given Moveable in its current reporting units
	 * @throws MoveableException
	 */
	public Quantity getSoftLimitLower(String dofname) throws MoveableException;

	/**
	 * Returns the upper limit of the given Moveable in its current reporting units.
	 * 
	 * @param dofname
	 * @return Upper limit of the given Moveable in its current reporting units
	 * @throws MoveableException
	 */
	public Quantity getSoftLimitUpper(String dofname) throws MoveableException;

	/**
	 * Tells the named DOF to save its current speed for later restoration.
	 * 
	 * @param dofname
	 * @throws MoveableException
	 */
	public void pushSpeed(String dofname) throws MoveableException;

	/**
	 * Tells the named DOF to restore a speed saved earlier.
	 * 
	 * @param dofname
	 * @throws MoveableException
	 */
	public void popSpeed(String dofname) throws MoveableException;

	/**
	 * @param name
	 * @return String[] moveable names
	 * @throws MoveableException
	 */
	public String[] getMoveableNames(String name) throws MoveableException;

	/**
	 * Get a device specific attribute
	 * 
	 * @param name
	 *            the name of the attribute
	 * @param dofname
	 *            the name of the dof
	 * @return the value of the attribute as an Object
	 * @throws MoveableException
	 */
	public Object getDeviceAttribute(String dofname, String name) throws MoveableException;

	/**
	 * Get a device specific attribute
	 * 
	 * @param name
	 *            the name of the attribute
	 * @param dofname
	 *            the name of the dof
	 * @param value
	 *            the value of the attribute
	 * @throws MoveableException
	 */
	public void setDeviceAttribute(String dofname, String name, Object value) throws MoveableException;
	/*
	 * public boolean getMoveEnabled(String dofname) throws MoveableException; public void setMoveEnabled(String
	 * dofname, boolean moveEnabled) throws MoveableException;
	 */
	
	// methods for allowing the beamline configuration manager parameters to be configured in CASTOR

	/**
	 * gets the lower limits for the device, used in the Beamline Configuration Manager
	 * @param dofname 
	 * @return The lower limits
	 * @throws MoveableException 
	 */
	public double[] getLowerGdaLimits(String dofname) throws MoveableException;

	/**
	 * gets the Tolerance settings for the device, used in the Beamline Configuration Manager
	 * @param dofname 
	 * @return The tolerance
	 * @throws MoveableException 
	 */
	public double[] getTolerance(String dofname) throws MoveableException;

	/**
	 * gets the upper limits for the device, used in the Beamline Configuration Manager
	 * @param dofname 
	 * @return the upper limits
	 * @throws MoveableException 
	 */
	public double[] getUpperGdaLimits(String dofname) throws MoveableException;

	/**
	 * sets the lower limits for the device, used in the Beamline Configuration Manager
	 * @param dofname 
	 * @param lowerLim 
	 * @throws MoveableException 
	 */
	public void setLowerGdaLimits(String dofname, double[] lowerLim) throws MoveableException;

	/**
	 * sets the lower limits for the device, used in the Beamline Configuration Manager
	 * @param dofname 
	 * @param lowerLim 
	 * @throws MoveableException 
	 */
	public void setLowerGdaLimits(String dofname, double lowerLim) throws MoveableException;

	/**
	 * sets the Tolerance settings for the device, used in the Beamline Configuration Manager
	 * @param dofname 
	 * @param tolerance 
	 * @throws MoveableException 
	 */
	public void setTolerance(String dofname, double[] tolerance) throws MoveableException;


	/**
	 * sets the Tolerance settings for the device, used in the Beamline Configuration Manager
	 * @param dofname 
	 * @param tolerance 
	 * @throws MoveableException 
	 */
	public void setTolerance(String dofname, double tolerance) throws MoveableException;

	/**
	 * sets the upper limits for the device, used in the Beamline Configuration Manager
	 * @param dofname 
	 * @param upperLim 
	 * @throws MoveableException 
	 */
	public void setUpperGdaLimits(String dofname, double[] upperLim) throws MoveableException;

	/**
	 * sets the upper limits for the device, used in the Beamline Configuration Manager
	 * @param dofname 
	 * @param upperLim 
	 * @throws MoveableException 
	 */
	public void setUpperGdaLimits(String dofname, double upperLim) throws MoveableException;
	
	
	/**
	 * docstring getter for a dof
	 * @param dofname
	 * @return the docString
	 * @throws MoveableException
	 */
	public String getDocString(String dofname) throws MoveableException ;
	/**
	 * docString setter for a dof
	 * @param dofname
	 * @param docString
	 * @throws MoveableException
	 */
	public void setDocString(String dofname, String docString) throws MoveableException ;
	
	
	
	
}