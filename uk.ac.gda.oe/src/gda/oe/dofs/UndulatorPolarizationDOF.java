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

import gda.factory.FactoryException;
import gda.factory.Finder;
import gda.gui.oemove.DOFType;
import gda.jscience.physics.units.NonSIext;
import gda.oe.Moveable;
import gda.oe.MoveableException;
import gda.oe.MoveableStatus;
import gda.oe.commands.DOFCommand;
import gda.oe.commands.MultipleMove;
import gda.oe.commands.MultipleMoveWatcher;
import gda.oe.util.UndulatorMoveCalculator;
import gda.oe.util.UndulatorMoveCalculatorWatcher;
import gda.oe.util.UndulatorMoveCalculator.Move;

import java.util.ArrayList;

import org.jscience.physics.quantities.Angle;
import org.jscience.physics.quantities.Quantity;
import org.jscience.physics.units.ConversionException;
import org.jscience.physics.units.Unit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A DOF responsible for setting the polarization of an undulator. Needs to know about the Gap, LowerPhase and
 * UpperPhase Positioners. This DOF cannot determine its position from the positions of its Moveables (the Gap,
 * LowerPhase and UpperPhase DOFs of the Undulator). It must assume that its position changes as the result of a
 * successful move. In order to determine that this has happenned it must override some methods of the DOF class in
 * order to provide itself with further information.
 */

public class UndulatorPolarizationDOF extends DOF implements MultipleMoveWatcher, UndulatorMoveCalculatorWatcher {
	private static final Logger logger = LoggerFactory.getLogger(UndulatorPolarizationDOF.class);

	private boolean moveFailed = false;

	private String moveFailureMessage;

	private boolean moveStarted = false;

	private UndulatorMoveCalculator umm = null;

	/**
	 * Constructor.
	 */
	public UndulatorPolarizationDOF() {
		dofType = DOFType.PolarizationDOF;
	}

	/**
	 * This exists so that UndulatorHarmonicDOF can be a Moveable but it never actually gets called because the
	 * checkMoveMoveables is delegated to the UndulatorMoveCalculator and its calculateMoveables gets called.
	 * 
	 * @param notUsed
	 * @return null
	 */
	@Override
	protected Quantity[] calculateMoveables(Quantity notUsed) {
		return null;
	}

	/**
	 * Overrides the DOF method so that Home is impossible
	 * 
	 * @param position
	 * @param setter
	 * @return MoveableStatus.ERROR
	 */
	@Override
	public synchronized int checkHome(Quantity position, Object setter) {
		return MoveableStatus.ERROR;
	}

	/**
	 * Overrides the DOF method so that moveBy is impossible
	 * 
	 * @param increment
	 * @param mover
	 * @return MoveableStatus.ERROR
	 */
	@Override
	public synchronized int checkMoveBy(Quantity increment, Object mover) {
		return MoveableStatus.ERROR;
	}

	/**
	 * Overrides the DOF method so that setPosition is impossible
	 * 
	 * @param position
	 * @param setter
	 * @return MoveableStatus.ERROR
	 */
	@Override
	public synchronized int checkSetPosition(Quantity position, Object setter) {
		return MoveableStatus.ERROR;
	}

	/**
	 * given a Quantity checks whether its units are acceptable and if so constructs a new Quantity of the correct
	 * subclass for this DOF and returns it
	 * 
	 * @param newQuantity
	 *            the Quantity to be checked
	 * @return an Angle of the same numerical value and Units
	 */
	@Override
	protected Quantity checkTarget(Quantity newQuantity) {
		Angle rtrn = null;
		Angle angle;
		try {
			angle = (Angle) newQuantity;
			// The value of angle should also be checked - it should be
			// between
			// plus and minus PI (for variable polarization) or it should be
			// one
			// of the fixed values
			if (isAllowedValue(angle)) {
				rtrn = angle;
			}
		} catch (ConversionException cce) {
			// Deliberately do nothing because if the cast fails then the
			// targetQuantity is not an angle so we should return null
		}
		return rtrn;
	}

	@Override
	public void configure() throws FactoryException {
		super.configure();

		// The UndulatorMoveCalculator is found from the Finder
		// because there are ordering difficulties in the XML file.
		// The UndulatorMoveCalculator cannot be created until after
		// the Gap and Phase DOFs have been. But they are part of
		// the same GenericOE as this so this is also created before
		// the UndulatorMoveCalculator and so must find it out at
		// configure time.
		umm = (UndulatorMoveCalculator) Finder.getInstance().find("UndulatorMediator");
		umm.addWatcher(this);

		// The move calculator guesses a starting Polarization so get it
		// to use here as well.
		setCurrentQuantity(umm.getPolarization());
		// We do not want this DOF to observe its moveable (which
		// exists only so that this can use the super configure method).
		moveables[0].deleteIObserver(this);

		updateStatus();
	}

	@Override
	public DOFCommand createAbsoluteMover(Quantity position) throws MoveableException {
		MultipleMove mm = null;
		Quantity newPosition = checkTarget(position);
		if (newPosition != null) {
			umm.setRequestedPolarization((Angle) position);
			setPositionValid(false);
			MoveableStatus ds = new MoveableStatus(statusCode, getName(), getPosition(), id);
			notifyIObservers(this, ds);

			int status = umm.checkMoveMoveables();
			if (status == MoveableStatus.SUCCESS) {
				Moveable[] toBeMoved = umm.getToBeMoved();
				ArrayList<Move> moves = umm.getMoves();
				mm = new MultipleMove(this, toBeMoved, moves);
				mm.addWatcher(this);
			} else if (status == MoveableStatus.ERROR)
				throw new MoveableException(new MoveableStatus(MoveableStatus.ERROR),
						"Undulator gap or phase has returned an error status.\nCheck communication with SR Control System ");
			else
				throw new MoveableException(new MoveableStatus(MoveableStatus.MOVE_NOT_ALLOWED),
						"UndulatorMoveCalculator did not return suitable moves\nfor move of " + getName() + " to "
								+ position);
		}
		return mm;
	}

	/**
	 * This is called by the DOFCommand object to actually start the move. Synchronized because it overrides the
	 * synchronized super class method.
	 * 
	 * @param mover
	 *            the DOFCommand object in control of the move
	 * @param id
	 *            the identifier of the move
	 * @throws MoveableException
	 */
	@Override
	public synchronized void doMove(Object mover, int id) throws MoveableException {
		// Deliberately do nothing
	}

	/**
	 * Implements the MultipleMoveWatcher interface - allows a MultipleMove to inform this about a move which has gone
	 * wrong.
	 * 
	 * @param mm
	 *            the MultipleMove
	 * @param me
	 *            a MoveableException caught during the MultipleMove
	 */
	@Override
	public void inform(MultipleMove mm, MoveableException me) {
		logger.error("!!!!!!! Movefailed");
		moveFailed = true;
		moveFailureMessage = me.getMessage();
		logger.error("!!!!!!! message was: " + moveFailureMessage);
	}

	/**
	 * Implements the UndulatorMoveCalculatorWatcher interface - allows the UndulatorMoveCalculator to provided the
	 * latest position.
	 * 
	 * @param newEnergy
	 *            the new Energy
	 * @param newHarmonic
	 *            the new Harmonic
	 * @param newPolarization
	 *            the new Polarization
	 * @param valid
	 *            whether or not the position is valid
	 */
	@Override
	public void inform(Quantity newEnergy, Quantity newHarmonic, Quantity newPolarization, boolean valid) {
		setCurrentQuantity(newPolarization);
		setPositionValid(valid);
		MoveableStatus ds = new MoveableStatus(statusCode, getName(), getPosition(), id);
		notifyIObservers(this, ds);
	}

	/**
	 * Checks whether an angle is suitable as an undulator polarization. This means that it must either have a value
	 * between -180 and +180 or be one of the higher values representing the fixed polarizations
	 * 
	 * @param toCheck
	 * @return true if it is a valid value, false otherwise
	 */
	private boolean isAllowedValue(Angle toCheck) {
		double value = toCheck.to(NonSIext.DEG_ANGLE).getAmount();
		long i = Math.round(value);
		return (value >= -180.0 && value <= 180.0 || i % 180 == 0);
	}

	@Override
	public boolean isSpeedLevelSettable() {
		return false;
	}

	@Override
	public boolean lock(Object locker) {
		// This DOF should only be moved my MultipleMoves, they will call this
		// method as they are about to start moving.
		moveStarted = true;
		moveFailed = false;
		notifyIObservers(this, new MoveableStatus(MoveableStatus.BUSY, getName()));
		return super.lock(locker);
	}

	@Override
	public void moveContinuously(int direction) throws MoveableException {
		// Deliberately does nothing
	}

	@Override
	public void refresh() {
		umm.refresh();
	}

	/**
	 * set the default units to set acceptable units to if XML acceptable units are not valid
	 */
	@Override
	protected void setDefaultAcceptableUnits() {
		defaultAcceptableUnits = new ArrayList<Unit<? extends Quantity>>();
		defaultAcceptableUnits.add(NonSIext.DEG_ANGLE);
	}

	/**
	 * set acceptable units that are valid and store as BaseUnit for XML checking
	 */
	@Override
	protected void setValidAcceptableUnits() {
		validAcceptableUnits = new ArrayList<Unit<? extends Quantity>>();
		validAcceptableUnits.add(NonSIext.DEG_ANGLE);
	}

	/**
	 * This is called by the DOFCommand object when the move is completed or abandoned.
	 * 
	 * @param unLocker
	 *            the DOFCommand object in control of the move
	 * @return true if unlocked????
	 */
	@Override
	public boolean unLock(Object unLocker) {
		logger.debug("UndulatorPolarizationDOF being unlocked by " + unLocker);

		boolean rtrn = super.unLock(unLocker);

		// The move may be abandoned after checkMoveMoveables is called but
		// before doMove is called (for example if some other part of the
		// locking failed) - in this case moveStarted would be false
		// and we do not want to change the position.
		if (moveStarted) {
			moveStarted = false;

			// The move may have failed for some other reason - motor hit
			// limit
			// or similar in which case (we hope) the status of the
			// UndulatorMoveCalculator will not be READY and again we do not
			// want to change the position. NB this cannot rely on its own
			// status being correct at the time of unlocking.
			if (statusCode == MoveableStatus.READY) {
				if (moveFailed) {
					logger.debug("UndulatorHarmonicDOF unsuccessful move. Message was:" + moveFailureMessage);
					setPositionValid(false);
					notifyIObservers(this, new MoveableException(new MoveableStatus(statusCode, getName()),
							"UndulatorHarmonicDOF unsuccessful move. Message was:\n" + moveFailureMessage));
				} else {
					umm.moveDone();
				}
				MoveableStatus ds = new MoveableStatus(statusCode, getName(), getPosition(), id);
				notifyIObservers(this, ds);
			}
		}
		return rtrn;

	}

	/**
	 * Overrides the method in DOF, is called in DOF.unLock
	 */
	@Override
	protected void unLockMoveables() {
		// Deliberately do nothing
	}

	/**
	 * Should calculate the position of the DOF from the positions of its Moveables.This is impossible for this DOF, its
	 * current position only changes as a result of a successful move.
	 */
	@Override
	protected void updatePosition() {
		// Deliberately do nothing.
	}

	@Override
	public Quantity getSoftLimitLower() {
		// do nothing as updatePosition() does nothing
		return null;
	}

	@Override
	public Quantity getSoftLimitUpper() {
		// do nothing as updatePosition() does nothing
		return null;
	}

}