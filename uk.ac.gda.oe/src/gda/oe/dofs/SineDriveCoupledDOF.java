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
import gda.jscience.physics.units.NonSIext;
import gda.oe.MoveableException;
import gda.oe.MoveableStatus;
import gda.util.QuantityFactory;

import java.util.ArrayList;

import org.jscience.physics.quantities.Angle;
import org.jscience.physics.quantities.Length;
import org.jscience.physics.quantities.Quantity;
import org.jscience.physics.units.ConversionException;
import org.jscience.physics.units.SI;
import org.jscience.physics.units.Unit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A specialised DOF which has one actual moveable but gets its position from a single readout from a non-moveable third
 * component. Moves are sent to the individual moveables one at a time, the position always comes from the second
 * non-moveable component. FIXME - check this next bit - may be part of the posiiton problem** There is further
 * complication in that the readout is angular, but the moves are linear (one of which is a sine drive) and accuracy is
 * essential.
 */
public class SineDriveCoupledDOF extends DOF {
	private static final Logger logger = LoggerFactory.getLogger(SineDriveCoupledDOF.class);

	private Angle angleOffset = Quantity.valueOf(0.0, SI.RADIAN);

	private Length armLength = Quantity.valueOf(0.0, SI.METER);

	private Length cageRadius = Quantity.valueOf(0.0, SI.METER);

	// NB coarse move accuracy is 0.5mm, accuracy in metres
	private double accuracy = 0.0005;

	// private boolean firstMove = false;

	/**
	 * Constructor
	 */
	public SineDriveCoupledDOF() {
	}

	@Override
	public void configure() throws FactoryException {
		// super.configure();
		// updatePosition();
		// setPositionValid(true);
		// updateStatus();
		logger.debug(" ***** SineDriveCoupledDOF: Calling super configure");
		super.configure();
		logger.debug(" ***** SineDriveCoupledDOF: Calling update position");
		updatePosition();
		logger.debug(" ***** SineDriveCoupledDOF: Setting position valid to true");
		setPositionValid(true);
		logger.debug(" ***** SineDriveCoupledDOF: Calling update status");
		updateStatus();
	}

	/**
	 * @return Returns the angleOffset in degrees
	 */
	public double getAngleOffset() {
		logger.debug(" ***** SineDriveCoupledDOF: Returning angleoffset " + angleOffset);
		return angleOffset.doubleValue();
	}

	/**
	 * @param angleOffset
	 *            The angleOffset to set expressed in degrees
	 */
	public void setAngleOffset(double angleOffset) {
		logger.debug(" ***** SineDriveCoupledDOF: Setting angleoffset to " + angleOffset);
		this.angleOffset = Quantity.valueOf(angleOffset, NonSIext.DEG_ANGLE);
	}

	/**
	 * @return Returns the armLength expressed in metres
	 */
	public double getArmLength() {
		logger.debug(" ***** SineDriveCoupledDOF: returning armlength " + armLength);
		return armLength.doubleValue();
	}

	/**
	 * @param armLength
	 *            the armLength to set expressed in metres.
	 */
	public void setArmLength(double armLength) {
		logger.debug(" ***** SineDriveCoupledDOF: setting armlength to " + armLength);
		this.armLength = Quantity.valueOf(armLength, SI.METER);
	}

	/**
	 * @return Returns the accuracy expressed in metres
	 */
	public double getAccuracy() {
		logger.debug(" ***** SineDriveCoupledDOF: returning accuracy " + accuracy);
		return accuracy;
	}

	/**
	 * Sets the accuracy
	 * 
	 * @param accuracy
	 *            the accuracy.
	 */
	public void setAccuracy(double accuracy) {
		logger.debug(" ***** SineDriveCoupledDOF: Setting accuracy to " + accuracy);
		this.accuracy = accuracy;
	}

	/**
	 * @return Returns the cage radius expressed in millimetres???
	 */
	public double getCageRadius() {
		logger.debug(" ***** SineDriveCoupledDOF: Returning cage radius " + cageRadius);
		return cageRadius.doubleValue();
	}

	/**
	 * @param cageRadius
	 *            to set expressed in millimetres???.
	 */
	public void setCageRadius(double cageRadius) {
		logger.debug(" ***** SineDriveCoupledDOF: Setting cage radiud to " + cageRadius);
		this.cageRadius = Quantity.valueOf(cageRadius, SI.METER);
	}

	/**
	 * Calculates the positions the moveables need to move to in order to go to position fromQuantity, which will come
	 * in as an Angle (or null) set in DEGREES and will be converted to linear movements, set in METRES.
	 * 
	 * @param moveQuantity
	 *            the Quantity for which Moveable positions are to be calculated
	 * @return an array of Quantities, the positions to which the individual Moveables must move in order to achieve
	 *         position of fromQuantity for the whole DOF.
	 */
	@Override
	protected Quantity[] calculateMoveables(Quantity moveQuantity) {
		Quantity halfFineRange = QuantityFactory.createFromString("0.012 rad");
		Quantity coarseAccuracy = QuantityFactory.createFromString("0.004 rad");
		Quantity positions[] = new Quantity[moveables.length];

		logger.debug("SineDriveCoupledDOF.calculateMoveables moveQuantity is: " + moveQuantity);

		try {
			Quantity actualMoveQuantity = moveQuantity.minus(angleOffset);
			logger.debug("SineDriveCoupledDOF.calculateMoveables actualMoveQuantity is" + actualMoveQuantity);
			logger.debug(" ONE " + halfFineRange);
			Quantity idealCoarsePosition = actualMoveQuantity.minus(halfFineRange);
			logger.debug(" TWO " + idealCoarsePosition.getAmount() + " " + coarseAccuracy.getAmount());
			Quantity nearestCoarsePosition = coarseAccuracy
					.times((int) (idealCoarsePosition.getAmount() / coarseAccuracy.getAmount()));
			logger.debug(" THREE " + nearestCoarsePosition);
			positions[0] = nearestCoarsePosition;
			positions[1] = actualMoveQuantity.minus(nearestCoarsePosition);
			logger.debug(" FOUR " + positions[1]);
			positions[2] = Quantity.valueOf(1.0, SI.METER);
		} catch (Exception e) {
			logger.debug(" ***** SineDriveCoupledDOF: Exception in calculating moveables ");
			logger.error("Exception in calculateMoveables " + e.getMessage());
		}
		return positions;
	}

	/**
	 * Method overridden. This DOF requires the first move to be complete before the second move is performed. Update
	 * status is responsible for setting off the second move.
	 * 
	 * @param mover
	 *            the Absolute or Relative command in charge of this move
	 * @param id
	 *            the move number
	 * @throws MoveableException
	 */
	/*
	 * public synchronized void doMove(Object mover, int id) throws MoveableException { Message.debug("DOF doMove called
	 * mover is " + mover + " id is " + id, Message.Level.TWO); if (lockedFor(mover)) { this.id = id;
	 * this.addIObserver((IObserver) mover); moveables[0].doMove(this, id); } else { throw new MoveableException(new
	 * MoveableStatus( MoveableStatus.NOTLOCKED, getName())); } }
	 */

	/**
	 * Given a Quantity, checks whether its units are acceptable and, if so, constructs a new Quantity of Angle and
	 * returns it.
	 * 
	 * @param newQuantity
	 *            the Quantity to be checked
	 * @return an Angle of the same numerical value
	 */
	@Override
	protected Quantity checkTarget(Quantity newQuantity) {
		Angle rtrn = null;
		try {
			logger.debug(" ***** SineDriveCoupledDOF: checking target ");
			rtrn = (Angle) newQuantity;
		} catch (ConversionException cce) {
			logger.debug(" ***** SineDriveCoupledDOF: checking target EXCEPTION");
			// Deliberately do nothing because if the cast fails then the
			// targetQuantity is not an Angle so we should return null
		}
		return rtrn;
	}

	/**
	 * Overrides superclass to ensure third non-moveable component is not called. Calculates the positions moveables
	 * will need to go to in order to achieve a position and then checks them individually if any check fails unlocks
	 * those already locked down at the bottom of the recursion the Positioners will actually keep a record of where
	 * they are expected to move to
	 * 
	 * @param position
	 *            the requested position
	 * @param mover
	 *            the object requesting the check
	 * @return SUCCESS if move is OK else ERROR or ALREADY_LOCKED
	 */
	@Override
	protected int checkMoveMoveables(Quantity position, Object mover) {
		int check = MoveableStatus.SUCCESS;

		logger.debug(" ***** SineDriveCoupledDOF: checking move movables for position " + position);
		Quantity[] moveablePositions = calculateMoveables(position);
		if (moveablePositions == null) {
			logger.debug(" ***** SineDriveCoupledDOF: moveablePositions = null ");
			check = MoveableStatus.ERROR;
			unLockMoveables();
		} else {
			logger.debug("DOF checkMoveMoveables calculateMoveables has returned:");

			// README - only do this for the first moveable.
			for (int j = 0; j < moveablePositions.length - 1; j++)
				logger.debug("     " + moveablePositions[j]);

			for (int i = 0; i < moveables.length - 1; i++) {
				check = moveables[i].checkMoveTo(moveablePositions[i], this);
				if (check != MoveableStatus.SUCCESS) {
					logger.debug(" ***** SineDriveCoupledDOF: moveable status != success ");
					unLockMoveables();
					break;
				}
			}
		}
		if (check == MoveableStatus.SUCCESS && !lock(mover)) {
			logger.debug(" ***** SineDriveCoupledDOF: moveable status already locked ");
			check = MoveableStatus.ALREADY_LOCKED;
		}

		return check;
	}

	/**
	 * This method is called when observables (positioners) notify. It queries the position via the display module and
	 * converts it to set the DOF currentQuantity. {@inheritDoc}
	 * 
	 * @see gda.oe.dofs.DOF#updatePosition()
	 */
	@Override
	protected void updatePosition() {
		setPositionValid(false);
		try {
			//
			Quantity displayPosition = moveables[2].getPosition();
			logger.debug("SineDriveCoupleDOF.updatePosition(): displayPosition is " + displayPosition);

			Quantity sineCoarseAngle = moveables[0].getPosition().minus(moveables[0].getPositionOffset()).divide(
					cageRadius);
			logger.debug("SineDriveCoupledDOF.updatePosition(): sineCoarseAngle is " + sineCoarseAngle);

			Quantity coarseAngle = Quantity.valueOf(Math.asin(sineCoarseAngle.doubleValue()), SI.RADIAN);
			logger.debug("SineDriveCoupledDOF.updatePosition(): coarseAngle is: " + coarseAngle);

			Quantity sineFineAngle = moveables[1].getPosition().minus(moveables[1].getPositionOffset()).divide(
					armLength);
			logger.debug("SineDriveCoupledDOF.updatePosition(): sineFineAngle is " + sineFineAngle);

			Quantity fineAngle = Quantity.valueOf(Math.asin(sineFineAngle.doubleValue()), SI.RADIAN);
			logger.debug("SineDriveCoupledDOF.updatePosition(): fineAngle is: " + fineAngle);

			Quantity newValue = coarseAngle.plus(fineAngle).plus(angleOffset);

			setCurrentQuantity(newValue);

			logger.debug("SineDriveCoupledDOF.updatePosition(): new position is " + getCurrentQuantity());
			setPositionValid(true);
		} catch (Exception e) {
			logger.error("SineDriveCoupledDOF: Error getting Diplay Readout");
		}
	}

	/**
	 * Updates the DOF's overall status from the stati of its Moveables. If there are more than one error the last is
	 * reported. The status can only be ready when all Moveables are ready. Otherwise the DOF is busy. This must be
	 * synchronized so that positioner polling threads do not result in individual Moveable's status codes being updated
	 * during the overall update.
	 */
	/*
	 * public synchronized void updateStatus() { int thisStatusCode; int readyCount = 0; int errorCount = 0; String
	 * message = "SineDriveCoupledDOF: .Update status initial message"; message = "MoveableStatus update for " +
	 * getName() + "(ID " + id + "), stati "; // README - For the minute we will leave this only checking two moveables. //
	 * It is possible that we will need to check the status again if the // encoder move is still happening and thus
	 * updates to the DOF position // proves to be inaccurate. for (int i = 0; i < moveables.length - 1; i++) { message =
	 * "MoveableStatus update for " + getName() + "(ID " + id + "), stati "; // Message.debug("SineDriveCoupledDOF:
	 * update status loop " + i); int statusID = lastDOFStatus[i].id(); int statusValue = lastDOFStatus[i].value();
	 * message += "[" + statusID + "," + statusValue + "] "; // If id is -1 then this DOF is not actively taking part in
	 * a // move and can take notice of any Moveable status it gets (one // or more Moveable may be moving because some
	 * other DOF is // moving). If id is not -1 then this DOF is actively involved // in a move and should take notice
	 * only of Moveables which send // the same id if (id < 0 || statusID == id) { Message.debug("SineDriveCoupledDOF:
	 * update status id is " + id + " " + statusID); thisStatusCode = statusValue; switch (thisStatusCode) { case
	 * MoveableStatus.READY: case MoveableStatus.SUCCESS: { // readyCount++; // This is the difference required for this
	 * DOF if (firstMove && statusID == id) { try { Message.debug("SineDriveCoupledDOF: update status move 2 " + id + " " +
	 * statusID); firstMove = false; statusCode = MoveableStatus.BUSY; moveables[1].doMove(this, id); } catch
	 * (MoveableException e) { Message.alarm("SineDriveCoupledDOF: Exception caught trying to do second move."); }
	 * break; } else { Message.debug("SineDriveCoupledDOF: incrementing moveables that are ready."); readyCount++;
	 * break; } // end of difference } case MoveableStatus.BUSY: Message.debug("SineDriveCoupledDOF: update status busy ",
	 * Message.Level.TWO); break; default: errorCount++; statusCode = thisStatusCode; errorMessage =
	 * lastDOFStatus[i].getMessage(); break; } } } if (errorCount == 0) { // See README comment at start of for loop if
	 * (readyCount == moveables.length - 1) statusCode = MoveableStatus.READY; else statusCode = MoveableStatus.BUSY; }
	 * message += "=> " + statusCode; Message.debug(message, Message.Level.THREE); }
	 */
	/**
	 * Starts the DOF moving continuously - in this case not possible.
	 * 
	 * @param direction
	 *            the direction to move
	 * @throws MoveableException
	 */
	@Override
	public void moveContinuously(int direction) throws MoveableException {
		// Deliberately does nothing (continuous movement would be far
		// too difficult for this DOF).
	}

	/**
	 * This DOF cannot have its speed set as one of the moveables cannot have its speed set
	 * 
	 * @return false
	 */
	@Override
	public boolean isSpeedLevelSettable() {
		return false;
	}

	@Override
	protected void setDefaultAcceptableUnits() {
		logger.debug(" ***** SineDriveCoupledDOF: setting default acceptable units ");
		defaultAcceptableUnits = new ArrayList<Unit<? extends Quantity>>();

		defaultAcceptableUnits.add(NonSIext.mDEG_ANGLE);
		defaultAcceptableUnits.add(NonSIext.DEG_ANGLE);
	}

	@SuppressWarnings( { "unchecked", "cast", "rawtypes" })
	@Override
	protected void setValidAcceptableUnits() {
		validAcceptableUnits = new ArrayList<Unit<? extends Quantity>>();

		// Leave these 'unnecessary' casts alone!! - see bug #634
		validAcceptableUnits.add((Unit) NonSIext.mDEG_ANGLE.getBaseUnits());
		validAcceptableUnits.add((Unit) NonSIext.DEG_ANGLE.getBaseUnits());
	}

	@Override
	public Quantity getSoftLimitLower() {
		// TODO after the fixme in getPosition has been done.
		return null;
	}

	@Override
	public Quantity getSoftLimitUpper() {
		// TODO after the fixme in getPosition has been done.
		return null;
	}
}
