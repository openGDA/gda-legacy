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

package gda.oe.dofs;

import gda.factory.Configurable;
import gda.factory.FactoryException;
import gda.factory.Findable;
import gda.factory.Finder;
import gda.gui.oemove.DOFType;
import gda.observable.IObserver;
import gda.oe.AbstractMoveable;
import gda.oe.Moveable;
import gda.oe.MoveableException;
import gda.oe.MoveableStatus;
import gda.oe.OEBase;
import gda.oe.commands.AbsoluteMove;
import gda.oe.commands.DOFCommand;
import gda.oe.commands.HomeCommand;
import gda.oe.commands.RelativeMove;
import gda.oe.commands.SetPosition;
import gda.util.LoggingConstants;
import gda.util.QuantityFactory;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import org.jscience.physics.quantities.Quantity;
import org.jscience.physics.units.Unit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An abstract base class for all Optical Element Degrees of Freedom.
 */

public abstract class DOF extends AbstractMoveable implements IObserver, Findable, Configurable {

	private static final Logger logger = LoggerFactory.getLogger(DOF.class);

	protected String dofType = DOFType.DefaultDOF;

	/**
	 * these are the directions for continuous moves
	 */
	public static final int POSITIVE = +1;

	/**
	 * these are the directions for continuous moves
	 */
	public static final int NEGATIVE = -1;

	private ArrayList<Moveable> moveableList = new ArrayList<Moveable>();

	private ArrayList<String> moveableNameList = new ArrayList<String>();

	private int protectionLevel = 1;

	private volatile boolean positionValid = false;

	private volatile Quantity currentQuantity;

	private volatile Quantity currentSpeed;

	private ArrayList<Unit<? extends Quantity>> acceptableUnits = new ArrayList<Unit<? extends Quantity>>();

	// the DOFs currentQuantity will have varying units but getPosition
	// should always return a Quantity in reportingUnits
	// THIS IS NEEDED FOR JYTHON SCRIPTING
	private Unit<? extends Quantity> reportingUnits;

	private NumberFormat nf;

	private int defaultDecimalPlaces = 3;

	private int decimalPlaces = -1;

	protected Moveable[] moveables;

	protected volatile MoveableStatus[] lastDOFStatus;

	protected volatile int statusCode = MoveableStatus.SUCCESS;

	protected int id = -1;

	private boolean configured = false;

	protected boolean relativeMove = false;

	protected String errorMessage = null;

	protected ArrayList<Unit<? extends Quantity>> validAcceptableUnits;

	protected ArrayList<Unit<? extends Quantity>> defaultAcceptableUnits;

	/**
	 * Constructor
	 */
	public DOF() {
	}
	
	/**
	 * Sets the moveables in this DOF.
	 * 
	 * @param moveables the moveables
	 */
	public void setMoveables(Moveable[] moveables) {
		this.moveables = moveables;
	}

	@Override
	public void configure() throws FactoryException {
		if (!configured) {
			
			if (moveables == null) {
			
				// Unlike moveableList moveableNameList actually belongs to
				// this DOF and will have been constructed by Castor to
				// contain the names of the Moveables required. The array
				// moveables is constructed by extracting the required
				// ones from the overall moveableList.
				int nosOfMoveables = moveableNameList.size();
				moveables = new Moveable[nosOfMoveables];
				lastDOFStatus = new MoveableStatus[nosOfMoveables];
	
				for (int i = 0; i < nosOfMoveables; i++) {
					for (Moveable moveable : moveableList) {
						String positionerName = moveable.getName();
						if (positionerName.equals(moveableNameList.get(i))) {
							moveables[i] = moveable;
							break;
						}
					}
	
					// If the Moveable has not been found in the list provided
					// by the OE then assume it belongs to another OE and is
					// specified
					// in the form OEName.DofName. We can find this from the Finder.
					if (moveables[i] == null
							&& (this instanceof CoupledDOF || this instanceof LookupDOF || this instanceof CombinedDOF)) {
						String thisName = moveableNameList.get(i);
						int index = thisName.indexOf(".");
						if (index > -1) {
							String oeName = thisName.substring(0, index);
							Findable findable = Finder.getInstance().find(oeName);
							if (findable != null && findable instanceof OEBase) {
								String dofName = thisName.substring(index + 1);
								moveables[i] = ((OEBase) findable).getMoveable(dofName);
							}
						}
					}
	
					if (moveables[i] == null) {
						throw new IllegalArgumentException("DOF.configure: Error configuring " + getName()
								+ ". Unable to find the specified moveable. moveableNameList = "
								+ moveableNameList.toString());
					}
					// Configure the new moveable (it may be already configured)
					// but should deal with this itself).
					((Configurable) moveables[i]).configure();
					lastDOFStatus[i] = moveables[i].getStatus();
					moveables[i].addIObserver(this);
				}
			}
			
			else {
				lastDOFStatus = new MoveableStatus[moveables.length];
				for (int i=0; i<moveables.length; i++) {
					moveables[i].configure();
					lastDOFStatus[i] = moveables[i].getStatus();
					moveables[i].addIObserver(this);
				}
			}
			
			setDefaultAcceptableUnits();
			setValidAcceptableUnits();

			if (getAcceptableUnits().isEmpty()) {
				setAcceptableUnits(defaultAcceptableUnits);
			} else {
				for (Unit<? extends Quantity> unit : getAcceptableUnits())
					if (!validAcceptableUnits.contains(unit.getBaseUnits())) {
						setAcceptableUnits(defaultAcceptableUnits);
						break;
					}
			}

			// must set reportingUnits before initialising formatting
			if (getReportingUnits() == null) {
				reportingUnits = acceptableUnits.get(0);
			}
			initialiseFormatting();
			setCurrentQuantity(Quantity.valueOf(0.0, getReportingUnits()));
			configured = true;
		}
	}

	/**
	 * set the default units to set acceptable units to if XML acceptable units are not valid, should be overriden in
	 * sub-classes requiring different defaults
	 */
	protected abstract void setDefaultAcceptableUnits();

	/**
	 * set acceptable units that are valid and store as BaseUnit for XML checking, needs to be over-ridden in
	 * sub-classes requiring different valid units
	 */
	protected abstract void setValidAcceptableUnits();

	/**
	 * Add a unit to the list of acceptable units for use in GUI, needed only by subclasses
	 * 
	 * @param unitString
	 *            String containing the units eg "mm"
	 */
	public void addAcceptableUnit(String unitString) {
		acceptableUnits.add(QuantityFactory.createUnitFromString(unitString));
	}
	
	/**
	 * Sets the acceptable units for this DOF.
	 * 
	 * @param acceptableUnits the acceptable units
	 */
	public void setAcceptableUnitNames(List<String> acceptableUnits) {
		this.acceptableUnits = new ArrayList<Unit<? extends Quantity>>();
		for (String acceptableUnit : acceptableUnits) {
			addAcceptableUnit(acceptableUnit);
		}
	}

	/**
	 * set the acceptableUnits array (needed only by subclasses)
	 * 
	 * @param units
	 *            the array of acceptable units
	 */
	protected void setAcceptableUnits(ArrayList<Unit<? extends Quantity>> units) {
		acceptableUnits = new ArrayList<Unit<? extends Quantity>>();
		for (Unit<? extends Quantity> unit : units)
			acceptableUnits.add(unit);
	}

	/**
	 * add a Moveable to this DOF's list of moveables
	 * 
	 * @param moveable
	 */
	public void addMoveable(Moveable moveable) {
		moveableList.add(moveable);
	}

	/**
	 * Adds a name to the list of Moveable names for this DOF. This method is specified in mapping.xml as the method
	 * Castor should use for an
	 * 
	 * @param moveableName
	 *            the moveable name to be added
	 */
	public void addMoveableName(String moveableName) {
		logger.debug("Adding moveableName " + moveableName + " to list");
		moveableNameList.add(moveableName);
	}
	
	/**
	 * Sets the list of moveable names in this DOF.
	 * 
	 * @param moveableNames the moveable names
	 */
	public void setMoveableNames(ArrayList<String> moveableNames) {
		this.moveableNameList = new ArrayList<String>();
		for (String moveableName : moveableNames) {
			addMoveableName(moveableName);
		}
	}
	/**
	 * Sets the list of moveable names in this DOF.
	 * 
	 * @param moveableNames the moveable names
	 */
	public void setMoveableNameList(ArrayList<String> moveableNames) {
		this.moveableNameList = new ArrayList<String>();
		for (String moveableName : moveableNames) {
			addMoveableName(moveableName);
		}
	}
	/**
	 * get list pf moveable name for this DOF
	 * 
	 * @return list of moveable names
	 */
	public ArrayList<String> getMoveableNameList() {
		return moveableNameList;
	}
	
	/**
	 * get list of moveable names for this DOF
	 * 
	 * @return list of moveable names
	 */
	public ArrayList<String> getMoveableNames() {
		return moveableNameList;
	}

	/**
	 * Get the Quantity value to be used in GUI interface
	 * 
	 * @return quantity used
	 */
	public Quantity getCurrentQuantity() {
		return currentQuantity;
	}

	/**
	 * Set the Quantity value to be used in GUI interface
	 * 
	 * @param q
	 *            Set the current quantity to be q.
	 */
	public void setCurrentQuantity(Quantity q) {
		currentQuantity = q.to(getReportingUnits());
	}

	/**
	 * Sets the list of available Moveables. NB this is NOT this list of Moveables which this DOF actually uses - it is
	 * the list of all Moveables known to the containing OE. This method is called during the OEBase configure() to make
	 * the known Moveables available to the DOF. When the DOF configure() is called the DOF will try to find its own
	 * Moveables from this list.
	 * 
	 * @param moveableList
	 */
	@Override
	public void setMoveableList(ArrayList<Moveable> moveableList) {
		this.moveableList = moveableList;
	}

	/**
	 * @param valid
	 */
	public void setPositionValid(boolean valid) {
		positionValid = valid;
	}

	/**
	 * @return boolean
	 */
	public boolean getPositionValid() {
		return positionValid;
	}

	/**
	 * Set password protection level of this DOF
	 * 
	 * @param protectionLevel
	 *            the protectionLevel
	 */
	public void setProtectionLevel(int protectionLevel) {
		this.protectionLevel = protectionLevel;
	}

	/**
	 * Return password protection level of this DOF
	 * 
	 * @return the password protection level
	 */
	@Override
	public int getProtectionLevel() {
		return protectionLevel;
	}

	/**
	 * Set the units in which the DOF will provide its position
	 * 
	 * @param reportingUnits
	 *            the reporting units in which positions will be displayed
	 */
	@Override
	public void setReportingUnits(Unit<? extends Quantity> reportingUnits) {
		this.reportingUnits = reportingUnits;

		setFormatting(calculateDecimalPlaces());
		update(null, null);
	}

	/**
	 * return the units in which the DOF will provide its position
	 * 
	 * @return the reporting unit
	 */
	@Override
	public Unit<? extends Quantity> getReportingUnits() {
		return reportingUnits;
	}

	/**
	 * @return Returns the decimalPlaces.
	 */
	public int getDecimalPlaces() {
		return decimalPlaces;
	}

	/**
	 * @param decimalPlaces
	 *            The decimalPlaces to set.
	 */
	public void setDecimalPlaces(int decimalPlaces) {
		this.decimalPlaces = decimalPlaces;
	}

	protected void initialiseFormatting() {
		nf = NumberFormat.getInstance();
		nf.setGroupingUsed(false);
		setFormatting(calculateDecimalPlaces());
	}

	/**
	 * sub classes must define this method to calculate positions for their moveableList which correspond to the given
	 * Quantity
	 * 
	 * @param q
	 *            the desired position
	 * @return an array of moveable positions
	 */
	protected abstract Quantity[] calculateMoveables(Quantity q);

	/**
	 * sub classes must define this method which given a Quantity returns a new Quantity of the correct subclass or null
	 * if the Units are incorrect.
	 * 
	 * @param newQuantity
	 *            the position with which to move to
	 * @return a quantity of the correct type
	 */
	protected abstract Quantity checkTarget(Quantity newQuantity);

	/**
	 * Returning currentQuantity is not correct, this cannot just return currentQuantity (defined here in DOF) as the
	 * position for all DOFs.
	 * 
	 * @return the current position
	 */
	@Override
	public Quantity getPosition() {
		return currentQuantity;
	}

	/**
	 * DOFS that can accept more than one different Quantity type e.g. MonoDOF must override this method.
	 * 
	 * @param reportingUnits
	 *            the reporting units
	 * @return the current position in the given reporting units
	 */
	@Override
	public Quantity getPosition(Unit<? extends Quantity> reportingUnits) {
		Quantity q = Quantity.valueOf(1.0, reportingUnits);
		Quantity position = null;
		if (q.getUnit().getBaseUnits().equals(currentQuantity.getUnit().getBaseUnits()))
			position = currentQuantity;
		return position;
	}

	@Override
	public void moveContinuously(int direction) throws MoveableException {
		// This default implementation deliberately does nothing.
	}

	/**
	 * Sets the number of decimalPlaces used in formatting
	 * 
	 * @param decimalPlaces
	 *            the number of decimal places
	 */

	public void setFormatting(int decimalPlaces)

	{
		nf.setMaximumFractionDigits(decimalPlaces);
		nf.setMinimumFractionDigits(decimalPlaces);
	}

	/**
	 * returns the position as a formatted String
	 * 
	 * @param position
	 *            the value to be formatted
	 * @return the DOFs position as a formatted String
	 */

	@Override
	public String formatPosition(double position) {
		return nf.format(position);
	}

	/**
	 * @return int
	 */
	public int calculateDecimalPlaces() {
		return (decimalPlaces == -1) ? defaultDecimalPlaces : decimalPlaces;
	}

	/**
	 * sub classes define this method to work out their currentQuantity from the positions of their moveables.
	 */

	protected abstract void updatePosition();

	/**
	 * This adds an increment to the currentQuantity to produce a target position which it returns. DOFs which allow
	 * Quantities which have non-linear relations eg MonoDOF should override this method to do the correct calculations
	 * but this simple one will work for most DOFs
	 * 
	 * @param increment
	 *            the increment to be added
	 * @return the targetPosition calculated or null if increment is not of the correct subclass
	 */

	protected Quantity checkAndAddIncrement(Quantity increment) {
		Quantity rtrn = null;

		// get the actual DOF to convert increment to the correct subclass
		Quantity newIncrement = checkTarget(increment);

		if (newIncrement != null) {
			// since the subclass of newQuantity has been checked the add
			// will not fail
			rtrn = currentQuantity.plus(newIncrement);
		}
		return rtrn;
	}

	@Override
	public synchronized int checkMoveBy(Quantity increment, Object mover) {
		int check = MoveableStatus.SUCCESS;

		Quantity targetPosition = checkAndAddIncrement(increment);
		logger.debug("DOF checkMoveBy targetPosition " + targetPosition);

		relativeMove = true;
		if (targetPosition != null)
			check = checkMoveTo(targetPosition, mover);
		else
			check = MoveableStatus.INCORRECT_QUANTITY;
		relativeMove = false;

		return check;
	}

	/**
	 * calculates the positions moveables will need to go to in order to achieve a position and then checks them
	 * individually if any check fails unlocks those already locked down at the bottom of the recursion the Positioners
	 * will actually keep a record of where they are expected to move to
	 * 
	 * @param position
	 *            the requested position
	 * @param mover
	 *            the object requesting the check
	 * @return success if move can be achieved else ERROR or ALREADY_LOCKED
	 */
	protected int checkMoveMoveables(Quantity position, Object mover) {
		int check = MoveableStatus.SUCCESS;

		Quantity[] moveablePositions = calculateMoveables(position);
		if (moveablePositions == null) {
			check = MoveableStatus.ERROR;
			unLockMoveables();
		} else if (moveablePositions.length != moveables.length) {
			check = MoveableStatus.ERROR;
			unLockMoveables();
			throw new RuntimeException(
					"DOF.checkMoveMoveables error. calculateMoveables.length != moveablePositions.length");
		} else {

			for (int j = 0; j < moveablePositions.length; j++)
				logger.debug("checkMoveMoveables report calculated position for {} is {}", moveables[j].getName(),
						moveablePositions[j]);
			// implement recursion on moveables
			for (int i = 0; i < moveables.length; i++) {
				check = moveables[i].checkMoveTo(moveablePositions[i], this);
				if ( check != MoveableStatus.SUCCESS) {
					for( int j=0;j < i-1;j++)
						moveables[j].unLock(this);
//					unLockMoveables();
					break;
				}
			}
		}
		if (check == MoveableStatus.SUCCESS && !lock(mover)) {
			check = MoveableStatus.ALREADY_LOCKED;
		}
		return check;
	}

	@Override
	public synchronized int checkMoveTo(Quantity position, Object mover) {
		int check = MoveableStatus.SUCCESS;

		// get the DOF to convert targetQuantity to the correct subclass
		Quantity newTargetQuantity = checkTarget(position);

		if (newTargetQuantity != null)
			check = checkMoveMoveables(newTargetQuantity, mover);
		else {
			logger.error("DOF checkMoveTo newTargetPosition is null. position = " + position.toString());
			check = MoveableStatus.INCORRECT_QUANTITY;
		}
		if(check != MoveableStatus.SUCCESS){
			logger.warn("move of " + getName() + " to " + position.toString() + " is not possible");
		}
		return check;
	}

	@Override
	public synchronized int checkSetPosition(Quantity position, Object setter) {
		int check = MoveableStatus.SUCCESS;

		Quantity newQuantity = checkTarget(position);

		if (newQuantity != null)
			check = checkSetMoveables(newQuantity, setter);
		else
			check = MoveableStatus.INCORRECT_QUANTITY;

		return check;
	}

	@Override
	public synchronized int checkHome(Quantity position, Object mover) {
		int check = MoveableStatus.SUCCESS;

		if (!isHomeable()) {
			check = MoveableStatus.NOT_HOMEABLE;
			return check;
		}
		// get the DOF to convert targetQuantity to the correct subclass
		Quantity newTargetQuantity = checkTarget(position);
		logger.debug("DOF checkMoveTo targetPosition " + newTargetQuantity);

		if (newTargetQuantity != null)
			check = checkHomeMoveables(newTargetQuantity, mover);
		else
			check = MoveableStatus.INCORRECT_QUANTITY;

		return check;
	}

	@Override
	public boolean isHomeable() {
		if (moveables.length > 1) {
			return false;
		}
		return moveables[0].isHomeable();
	}

	/**
	 * calculates the positions Moveables will need to set after reaching the home position and then checks them
	 * individually if any check fails unlocks those already locked
	 * 
	 * @param position
	 *            the requested position
	 * @param mover
	 *            the object requesting the check
	 * @return success if move can be achieved else ERROR or ALREADY_LOCKED
	 */
	protected int checkHomeMoveables(Quantity position, Object mover) {
		int check = MoveableStatus.SUCCESS;

		Quantity[] moveablePositions = calculateMoveables(position);
		if (moveablePositions == null) {
			check = MoveableStatus.ERROR;
			unLockMoveables();
		} else {
			for (int i = 0; i < moveables.length; i++) {
				check = moveables[i].checkHome(moveablePositions[i], this);
				if (check != MoveableStatus.SUCCESS) {
					if (check != MoveableStatus.ALREADY_LOCKED) {
						unLockMoveables();
					}
					break;
				}
			}
		}
		if (check == MoveableStatus.SUCCESS) {
			if (!lock(mover)) {
				check = MoveableStatus.ALREADY_LOCKED;
			}
		}

		return check;
	}

	/**
	 * calculates the positions Moveables will need to set in order order to set a position and then checks them
	 * individually if any check fails unlocks those already locked down at the bottom of the recursion the Positioners
	 * will actually keep a record of where they are expected to set position to
	 * 
	 * @param position
	 *            the requested position
	 * @param setter
	 *            the object requesting the check
	 * @return success if set can be achieved else ERROR or ALREADY_LOCKED
	 */
	protected int checkSetMoveables(Quantity position, Object setter) {
		int check = MoveableStatus.SUCCESS;

		Quantity[] moveablePositions = calculateMoveables(position);
		if (moveablePositions == null) {
			check = MoveableStatus.ERROR;
			unLockMoveables();
		} else {
			for (int i = 0; i < moveables.length; i++) {
				check = moveables[i].checkSetPosition(moveablePositions[i], this);
				if (check != MoveableStatus.SUCCESS) {
					unLockMoveables();
					break;
				}
			}
		}
		if (check == MoveableStatus.SUCCESS) {
			if (!lock(setter))
				check = MoveableStatus.ALREADY_LOCKED;
		}

		return check;
	}

	@Override
	public synchronized void doMove(Object mover, int id) throws MoveableException {
		logger.debug("DOF doMove called mover is " + mover + " id is " + id);
		if (lockedFor(mover)) {
			this.id = id;
			this.addIObserver((IObserver) mover);
			for (int i = 0; i < moveables.length; i++)
				moveables[i].doMove(this, id);
		} else
			throw new MoveableException(new MoveableStatus(MoveableStatus.NOTLOCKED, getName()),
					"DOF.doMove: lockedFor(mover) returned false");
	}

	@Override
	public synchronized void doSet(Object setter) throws MoveableException {
		if (lockedFor(setter)) {
			this.addIObserver((IObserver) setter);
			for (int i = 0; i < moveables.length; i++)
				moveables[i].doSet(this);
		} else
			throw new MoveableException(new MoveableStatus(MoveableStatus.NOTLOCKED, getName()),
					"DOF.doSet: lockedFor(setter) returned false");
	}

	@Override
	public synchronized void doHome(Object mover) throws MoveableException {
		if (lockedFor(mover)) {
			this.addIObserver((IObserver) mover);
			for (int i = 0; i < moveables.length; i++)
				moveables[i].doHome(this);
		} else {
			throw new MoveableException(new MoveableStatus(MoveableStatus.NOTLOCKED, getName()),
					"DOF.doHome: lockedFor(mover) returned false");
		}
	}

	@Override
	public ArrayList<Unit<? extends Quantity>> getAcceptableUnits() {
		return (acceptableUnits);
	}

	@Override
	public MoveableStatus getStatus() {
		MoveableStatus rtrn = new MoveableStatus(statusCode, getName(), getPosition(), id, errorMessage);
		return (rtrn);
	}

	@Override
	public boolean isMoving() throws MoveableException {
		boolean rtrn = false;

		for (int i = 0; i < moveables.length; i++)
			rtrn = rtrn || moveables[i].isMoving();

		if (lockedFor((Object) null) == false)
			rtrn = true;

		return (rtrn);
	}

	/**
	 * Check if the DOF is currently in a valid position. The rules for validity will depend upon the particular DOF.
	 * 
	 * @return true = valid, false = invalid.
	 */
	@Override
	public boolean isPositionValid() {
		return positionValid;
	}

	@Override
	public void stop() throws MoveableException {
		for (int i = 0; i < moveables.length; i++)
			moveables[i].stop();
	}

	/**
	 * this overrides Moveable's own definition in order to unlock child moveables, the simple version in Moveable will
	 * work for Positioners qv
	 * 
	 * @param unLocker
	 *            the object which is trying to unlock
	 * @return true if the unlock succeeds, false otherwise
	 */
	@Override
	public boolean unLock(Object unLocker) {
		logger.debug("DOF unlock called in {}", getName());
		if (lockedFor(unLocker)) {
			logger.debug("DOF {} unlocking moveables", getName());
			unLockMoveables();
			// DOFs which are not acively part of a move must always have
			// id -1, this ensures that in updateStatus they take into
			// account the status of all of their positioners, some of
			// which may be moving independently.
			id = -1;
			return (super.unLock(unLocker));
		}
		return (false);
	}

	/**
	 * unlocks child moveables
	 */
	protected void unLockMoveables() {
		for (int i = 0; i < moveables.length; i++)
			moveables[i].unLock(this);
	}

	/**
	 * update the DOFs current position from moveables and notify any observers of this DOF that a change may have
	 * occured. This methid is synchronized to ensure atomic updating. It is certainly possible that multiple Moveables
	 * could attempt to update a DOF at the same time. Having this synchronized should mean that the sub-classes don't
	 * all need to synchronize stuff like updatePosition individually.
	 * 
	 * @param o
	 *            Observable making the call
	 * @param arg
	 *            argument that observable passed to notifyObservers
	 */
	@Override
	public void update(Object o, Object arg) {

		if (!configured) {
			logger.error("DOF.Update called before configuration complete");
			return;
		}

		MoveableStatus ms = updateNoNotify(o, arg);
		if (ms != null)
			notifyIObservers(this, ms);
	}

	/**
	 * @param o
	 * @param arg
	 * @return MoveableStatus
	 */
	public synchronized MoveableStatus updateNoNotify(Object o, Object arg) {
		MoveableStatus ds = null;

		if (o instanceof Moveable) {
			Moveable m = (Moveable) o;
			logger.debug( "DOF " + getName() + " update called by moveable " + m.getName());

			// Which Moveable are we dealing with
			for (int i = 0; i < moveables.length; i++) {
				if (m == moveables[i]) {
					// recalculate the position from position of the Moveables
					updatePosition();

					// update the array of Moveable statuses
					if (arg instanceof MoveableStatus) {
						lastDOFStatus[i] = ((MoveableStatus) arg);
						logger.debug( "     arg is " + arg);
						logger.debug( "     ((MoveableStatus)arg).value " + ((MoveableStatus) arg).value());

					}

					// recalculate the overall status of the DOF from the
					// statuses of the Moveables
					updateStatus();

					break;
				}
			}
		} else {
			if (o == null) {
				logger.debug( "DOF update called by empty object ");

			} else {

				logger.debug( "DOF update called by object " + o);

			}
			updatePosition();
			updateStatus();
		}

		// notify observers with a MoveableStatus (containing a possible error
		// message from one of the moveables).
		if (currentQuantity != null) {
			ds = new MoveableStatus(statusCode, getName(), currentQuantity, id, errorMessage);
			errorMessage = null;
		}
		return ds;

	}

	/**
	 * Updates the DOF's overall status from the statuses of its Moveables. If there are more than one error the last is
	 * reported. The status can only be ready when all Moveables are ready. Otherwise the DOF is busy. This must be
	 * synchronized so that positioner polling threads do not result in indivividual Moveable's status codes being
	 * updated during the overall update.
	 */
	public synchronized void updateStatus() {
		int thisStatusCode;
		int readyCount = 0;
		int errorCount = 0;
		String message;

		message = "MoveableStatus update for " + getName() + "(ID " + id + "), stati ";

		for (int i = 0; i < moveables.length; i++) {
			message += "[" + lastDOFStatus[i].id() + "," + lastDOFStatus[i].value() + "] ";

			// If id is -1 then this DOF is not actively taking part in a
			// move and can take notice of any Moveable status it gets (one
			// or more Moveable may be moving because some other DOF is
			// moving). If id is not -1 then this DOF is actively involved
			// in a move and should take notice only of Moveables which send
			// the same id
			if ((id < 0) || (lastDOFStatus[i].id() == id)) {
				thisStatusCode = lastDOFStatus[i].value();
				switch (thisStatusCode) {
				case MoveableStatus.READY:
				case MoveableStatus.SUCCESS:
					readyCount++;
					break;
				case MoveableStatus.BUSY:
					break;
				default:
					errorCount++;
					statusCode = thisStatusCode;
					errorMessage = lastDOFStatus[i].getMessage();
					break;
				}
			}
		}

		if (errorCount == 0) {
			if (readyCount == moveables.length)
				statusCode = MoveableStatus.READY;
			else
				statusCode = MoveableStatus.BUSY;
		}
		message += "=> " + statusCode;

		logger.debug(LoggingConstants.FINEST, message);

	}

	/**
	 * Sets the speed to one of a set of predetermined choices. Note that these predetermined choices are properties of
	 * the Motors so this DOF and its Moveables (and their Moveables) will have no idea of its own speed. Compare the
	 * setSpeed() methods.
	 * 
	 * @param speedLevel
	 *            an integer indicating which of the predetermined speeds to set
	 * @throws MoveableException
	 */
	@Override
	public void setSpeedLevel(int speedLevel) throws MoveableException {
		for (int i = 0; i < moveables.length; i++) {
			moveables[i].setSpeedLevel(speedLevel);
		}
	}

	/**
	 * The default implementation of getHomeOffset returns null
	 * 
	 * @return null
	 * @throws MoveableException
	 */
	@Override
	public Quantity getPositionOffset() throws MoveableException {
		return null;
	}

	/**
	 * The default implementation of getHomeOffset returns null
	 * 
	 * @return null
	 * @throws MoveableException
	 */
	@Override
	public Quantity getHomeOffset() throws MoveableException {
		return null;
	}

	@Override
	public void setPositionOffset(Quantity offset) throws MoveableException {
		int check = MoveableStatus.SUCCESS;
		Quantity newQuantity = checkTarget(offset);
		if (newQuantity != null) {
			Quantity[] offsetPosition = calculateMoveables(newQuantity);
			if (offsetPosition == null) {
				check = MoveableStatus.ERROR;
				throw new MoveableException(new MoveableStatus(check, getName(), offset),
						"DOF.setPositionOffset: offsetPosition == null");
			}
			moveables[0].setPositionOffset(offsetPosition[0]);
		} else {
			check = MoveableStatus.INCORRECT_QUANTITY;
			throw new MoveableException(new MoveableStatus(check, getName(), offset),
					"DOF.setPositionOffset: newQuantity == null");
		}
	}

	@Override
	public void setHomeOffset(Quantity offset) throws MoveableException {
		int check = MoveableStatus.SUCCESS;
		Quantity newQuantity = checkTarget(offset);
		if (newQuantity != null) {
			Quantity[] offsetPosition = calculateMoveables(newQuantity);
			if (offsetPosition == null) {
				check = MoveableStatus.ERROR;
				throw new MoveableException(new MoveableStatus(check, getName(), offset),
						"DOF.setHomeOffset: offsetPosition == null");
			}
			moveables[0].setHomeOffset(offsetPosition[0]);
		} else {
			check = MoveableStatus.INCORRECT_QUANTITY;
			throw new MoveableException(new MoveableStatus(check, getName(), offset),
					"DOF.setHomeOffset: newQuantity == null");
		}
	}

	@Override
	public boolean isSpeedLevelSettable() {
		return true;
	}

	@Override
	public boolean isScannable() {
		return true;
	}

	@Override
	public void refresh() {
		for (int i = 0; i < moveables.length; i++) {
			moveables[i].refresh();
		}
	}

	/**
	 * @return String - dofType
	 */
	public String getDOFType() {
		return dofType;
	}

	/**
	 * Returns a DOFCommand which can be used to carry out a move to the specified position. Subclasses override this if
	 * a simple AbsoluteMove is not enough.
	 * 
	 * @param position
	 *            the position to move to
	 * @return An AbsoluteMove to get the DOF to that position
	 * @throws MoveableException
	 *             if a move cannot be created (never true in base class).
	 */
	@Override
	public DOFCommand createAbsoluteMover(Quantity position) throws MoveableException {
		return new AbsoluteMove(this, position);
	}

	/**
	 * Returns a DOFCommand which can be used to carry out a move by the specified increment. Subclasses override this
	 * if a simple RelativMove is not enough.
	 * 
	 * @param increment
	 *            the increment to move by
	 * @return A RelativeMove to move the DOF by that increment
	 * @throws MoveableException
	 *             if a move cannot be created (never true in base class).
	 */
	@Override
	public DOFCommand createRelativeMover(Quantity increment) throws MoveableException {
		return new RelativeMove(this, increment);
	}

	/**
	 * Returns a DOFCommand which can be used to carry out a set position for the specified position. Subclasses
	 * override this if a simple SetPosition is not enough.
	 * 
	 * @param position
	 *            the position to set
	 * @return A SetPosition to set the DOF's position
	 * @throws MoveableException
	 *             if a DOFCommand cannot be created (never true in base class).
	 */
	@Override
	public DOFCommand createPositionSetter(Quantity position) throws MoveableException {
		return new SetPosition(this, position);
	}

	/**
	 * Returns a DOFCommand which can be used to carry out a homing command. Subclasses override this if a simple
	 * HomeCommand is not enough.
	 * 
	 * @param position
	 *            the position to set after the homing
	 * @return A HomeCommand to carry out the homing.
	 * @throws MoveableException
	 *             if a DOFCommand cannot be created (never true in base class).
	 */
	@Override
	public DOFCommand createHomer(Quantity position) throws MoveableException {
		// return Doh!
		return new HomeCommand(this, position);
	}

	@Override
	public boolean isDirectlyUseable() {
		return true;
	}

	@Override
	public void popSpeed() {
		super.popSpeed();
		for (int i = 0; i < moveables.length; i++) {
			moveables[i].popSpeed();
		}
	}

	@Override
	public void pushSpeed() {
		super.pushSpeed();
		for (int i = 0; i < moveables.length; i++) {
			moveables[i].pushSpeed();
		}
	}

	/**
	 * Returns the current speed. Note that this speed is not necessarily meaningful.
	 * 
	 * @return the current speed
	 */
	@Override
	public Quantity getSpeed() {
		return currentSpeed;
	}

	/**
	 * Sets the current speed. Note that this default implementation may or may not work for individual subclasses
	 * depending on the relationships between their moveables. Compare the other setSpeed().
	 * 
	 * @param speed
	 *            the new speed to set
	 * @throws MoveableException
	 */
	@Override
	public void setSpeed(Quantity speed) throws MoveableException {
		logger.debug("DOF setSpeed called with speed: " + speed);
		currentSpeed = speed;

		// This will work for some DOFs, for example the DoubleAxisLinearDOF
		// but it will not work in general. Subclasses should override this
		// method.
		for (int i = 0; i < moveables.length; i++) {
			moveables[i].setSpeed(speed);
		}
	}

	/**
	 * Sets the speed of the DOF given a range and the time which should be taken to travel over it. Note that this
	 * default implementation ought to work for all DOFs (so long as the individual Moveables have implemented it
	 * correctly as well). Compare the other setSpeed().
	 * 
	 * @param start
	 *            the start of the range
	 * @param end
	 *            the end of the range
	 * @param time
	 *            the time which should be taken to cover the range
	 * @throws MoveableException
	 */
	@Override
	public void setSpeed(Quantity start, Quantity end, Quantity time) throws MoveableException {

		logger.debug("DOF setSpeed called with start, end, time: " + start + " " + end + " " + time);

		// The effective speed of this DOF will be:
		currentSpeed = end.minus(start).divide(time);

		// Now calculate positions for the Moveables at the start and end of
		// the range and get them to calculate effective speeds for themselves.
		// Eventually this will work its way down to Positioners which will set
		// actual motor speeds to achieve the overall effective speed.
		Quantity[] starts = calculateMoveables(start);
		Quantity[] ends = calculateMoveables(end);
		for (int i = 0; i < moveables.length; i++) {
			moveables[i].setSpeed(starts[i], ends[i], time);
		}

	}

	@Override
	public Object getAttribute(String name) throws MoveableException {
		return moveables[0].getAttribute(name);
	}

	@Override
	public void setAttribute(String name, Object o) throws MoveableException {
		moveables[0].setAttribute(name, o);
	}
}