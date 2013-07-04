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

import gda.configuration.properties.LocalProperties;
import gda.device.MotorException;
import gda.jscience.physics.units.NonSIext;
import gda.oe.MoveableException;
import gda.oe.MoveableStatus;

import java.util.ArrayList;

import org.jscience.physics.quantities.Angle;
import org.jscience.physics.quantities.Quantity;
import org.jscience.physics.units.ConversionException;
import org.jscience.physics.units.NonSI;
import org.jscience.physics.units.SI;
import org.jscience.physics.units.Unit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An angular positioner which calculates the motor position in the opposite way to AngularPositioner. i.e.
 * motorPosition = positionOffset - user_position rather than motorPosition = positionOffset + user_position
 */
public class AngularPositioner_ReverseOffset extends Positioner {

	private static final Logger logger = LoggerFactory.getLogger(AngularPositioner_ReverseOffset.class);

	protected String driveUnitName = LocalProperties.get("gda.unit.name");

	protected Unit<Angle> driveUnit = NonSIext.mDEG_ANGLE;

	@Override
	protected void _initialisation() {
		if (driveUnitName != null && driveUnitName.compareTo("DLS-unit") == 0) {
			driveUnit = NonSIext.DEG_ANGLE;
		}
	}

	/**
	 * Moves the positioner to the specified position in degrees
	 * 
	 * @param position
	 *            the position to move to
	 * @throws MotorException
	 */
	@Override
	protected void _moveTo(double position) throws MotorException {
		double steps = (this.positionOffset - position) * getStepsPerUnit();
		motor.moveTo(steps);
	}

	@Override
	protected void _updatePosition(double motorPosition) {
		currentPosition = this.positionOffset - (motorPosition / getStepsPerUnit());
	}

	/**
	 * update the current motor Lower limit in GDA. This method converts EPICS motor limits to GDA motor limit in GDA
	 * drive unit.
	 * 
	 * @param lowLimit
	 */
	@Override
	protected void _updateLimitLow(double lowLimit) {
		softLimitLow = this.positionOffset - (lowLimit / getStepsPerUnit());
	}

	/**
	 * update the current motor high limit in GDA. This method converts EPICS motor limits to GDA motor limit in GDA
	 * drive unit.
	 * 
	 * @param highLimit
	 */
	@Override
	protected void _updateLimitHigh(double highLimit) {
		setSoftLimitHighValue(this.positionOffset - (highLimit / getStepsPerUnit()));
	}

	/**
	 * sets the softlimits for the motors
	 * 
	 * @param lowLimit
	 * @param highLimit
	 * @throws MotorException
	 */
	@Override
	protected void _setSoftLimits(double lowLimit, double highLimit) throws MotorException {
		double lowLimitSteps = (this.positionOffset - lowLimit) * getStepsPerUnit();
		double highLimitSteps = (this.positionOffset - highLimit) * getStepsPerUnit();
		logger.debug("calling motor setLimits");
		if (motor != null) {
			motor.setSoftLimits(lowLimitSteps, highLimitSteps);
		}
	}

	/**
	 * Sets the current position to be denoted by the passed value
	 * 
	 * @param position
	 *            The new value to represent the current position
	 * @throws MotorException
	 */
	@Override
	protected void _setPosition(double position) throws MotorException {
		double steps = (this.positionOffset - position) * getStepsPerUnit();
		motor.setPosition(steps);
	}

	@Override
	public Quantity getPosition() {
		return Quantity.valueOf(currentPosition, driveUnit);
	}

	/**
	 * gets the lower soft limit quantity in motor drive unit.
	 * 
	 * @return Lower limit quantity
	 */
	@Override
	public Quantity getLowerSoftLimit() {
		return Quantity.valueOf(softLimitLow, driveUnit);
	}

	/**
	 * gets the upper soft limit quantity in motor drive unit.
	 * 
	 * @return Upper Limit Quantity
	 */
	@Override
	public Quantity getUpperSoftLimit() {
		return Quantity.valueOf(getSoftLimitHighValue(), driveUnit);
	}

	/**
	 * checks that targetQuantity has the correct driveUnits and if it has calculates targetPosition from it
	 * 
	 * @param targetQuantity
	 *            the target Quantity
	 * @return true if the Quantity is alright, false otherwise
	 */
	@Override
	protected boolean checkTarget(Quantity targetQuantity) {
		boolean rtrn = false;
		// any Quantity with Angle driveUnits is acceptable but the
		// double value we actually want must be in mDeg because
		// stepsPerMdeg is what this positioner knows
		try {
			targetPosition = ((Angle) targetQuantity).to(driveUnit).getAmount();
			rtrn = true;
		} catch (ConversionException cce) {
			// Deliberately do nothing because if the cast fails then the
			// targetQuantity is not a length so we should return false
		}
		return rtrn;
	}

	/**
	 * checks that increment has the correct driveUnits and if it has calculates targetPosition from it
	 * 
	 * @param increment
	 *            the increment
	 * @return true if the Quantity is alright, false otherwise
	 */
	@Override
	protected boolean checkIncrement(Quantity increment) {
		boolean rtrn = false;
		// any Quantity with Angle driveUnits is acceptable but the
		// double value we actually want must be in mDeg because
		// stepsPerMdeg is what this positioner knows
		try {
			targetPosition = ((Angle) increment).to(driveUnit).getAmount();
			rtrn = true;
		} catch (ConversionException cce) {
			// Deliberately do nothing because if the cast fails then the
			// targetQuantity is not a length so we should return false
		}
		return rtrn;
	}

	@Override
	protected boolean checkSoftLimitHigh(Quantity highLimit) {
		boolean rtrn = false;
		// any Quantity with Length driveUnits is acceptable but the
		// double value we actually want must be in Deg because
		// stepsPerDeg is what this positioner knows
		try {
			setSoftLimitHighValue(((Angle) highLimit).to(driveUnit).getAmount());
			rtrn = true;
		} catch (ConversionException cce) {
			// Deliberately do nothing because if the cast fails then the
			// targetQuantity is not a length so we should return false
		}
		return rtrn;
	}

	@Override
	protected boolean checkSoftLimitLow(Quantity lowLimit) {
		boolean rtrn = false;
		// any Quantity with Length driveUnits is acceptable but the
		// double value we actually want must be in MM because
		// stepsPerMM is what this positioner knows
		try {
			setSoftLimitLowValue(((Angle) lowLimit).to(driveUnit).getAmount());
			rtrn = true;
		} catch (ConversionException cce) {
			// Deliberately do nothing because if the cast fails then the
			// targetQuantity is not a length so we should return false
		}
		return rtrn;
	}

	@Override
	protected boolean _setHomeOffset(Quantity offset) {
		boolean rtrn = false;
		// any Quantity with Length driveUnits is acceptable but the
		// double value we actually want must be in MM because
		// stepsPerMM is what this positioner knows
		try {
			setHomeOffsetValue(((Angle) offset).to(driveUnit).getAmount());
			rtrn = true;
		} catch (ConversionException cce) {
			// Deliberately do nothing because if the cast fails then the
			// targetQuantity is not a length so we should return false
		}
		return rtrn;
	}

	@Override
	protected boolean _setPositionOffset(Quantity offset) {
		boolean rtrn = false;
		// any Quantity with Length driveUnits is acceptable but the
		// double value we actually want must be in MM because
		// stepsPerMM is what this positioner knows
		try {
			setPositionOffsetValue(((Angle) offset).to(driveUnit).getAmount());
			rtrn = true;
		} catch (ConversionException cce) {
			// Deliberately do nothing because if the cast fails then the
			// targetQuantity is not a length so we should return false
		}
		return rtrn;
	}

	@Override
	public Quantity getPositionOffset() {
		return Quantity.valueOf(getPositionOffsetValue(), driveUnit);
	}

	@Override
	public Quantity getHomeOffset() {
		return Quantity.valueOf(getHomeOffsetValue(), driveUnit);
	}

	@Override
	public Unit<? extends Quantity> getReportingUnits() {
		return driveUnit;
	}

	@Override
	public ArrayList<Unit<? extends Quantity>> getAcceptableUnits() {
		ArrayList<Unit<? extends Quantity>> al = new ArrayList<Unit<? extends Quantity>>();
		al.add(driveUnit);
		return al;
	}

	@Override
	public void setSpeed(Quantity speed) throws MoveableException {
		// FIXME: check class of speed before proceeding

		// calculate motor speed

		logger.debug("AngularPositioner " + getName() + "  requested speed " + speed);

		Quantity spu = Quantity.valueOf(getStepsPerUnit(), Unit.ONE);
		logger.debug("AngularPositioner " + getName() + "  spu " + spu);

		Quantity du = Quantity.valueOf(1.0, driveUnit);
		logger.debug("AngularPositioner " + getName() + "  du " + du);

		Quantity gr = spu.divide(du);
		logger.debug("AngularPositioner " + getName() + "  gr " + gr);

		Quantity requiredMotorSpeed = speed.times(gr);
		logger.debug("AngularPositioner " + getName() + "  requiredMotorSpeed " + requiredMotorSpeed);

		double speedValue = requiredMotorSpeed.doubleValue();
		if (Math.abs(speedValue) > 1) { // set motor speed
			try {
				motor.setSpeed(requiredMotorSpeed.doubleValue());
			} catch (MotorException me) {
				throw new MoveableException(MoveableStatusFactory.createMoveableStatus(me.status, getName()), me
						.getMessage());
			}
		} else {
			throw new MoveableException(new MoveableStatus(MoveableStatus.SPEEDLEVEL_ERROR, getName()),
					"Cannot set speed " + speedValue);
		}
	}

	@Override
	public void setSpeed(Quantity start, Quantity end, Quantity time) throws MoveableException {
		setSpeed(end.minus(start).to(SI.MILLI(NonSI.DEGREE_ANGLE)).divide(time.to(SI.SECOND)));
	}

	@Override
	public Quantity getSpeed() {
		Quantity qs = null;

		// get motor speed
		double speed = 0;
		try {
			speed = motor.getSpeed();
		} catch (MotorException e) {
			logger.error("Exception: " + e.getMessage());
		}

		// calculate Positioner speed
		Quantity currentMotorSpeed = Quantity.valueOf(speed, SI.HERTZ);
		logger.debug("AngularPositioner " + getName() + "  currentMotorSpeed " + currentMotorSpeed);

		Quantity spu = Quantity.valueOf(getStepsPerUnit(), Unit.ONE);
		logger.debug("AngularPositioner " + getName() + "  spu " + spu);

		Quantity du = Quantity.valueOf(1.0, driveUnit);
		logger.debug("AngularPositioner " + getName() + "  du " + du);

		Quantity gr = spu.divide(du);
		logger.debug("AngularPositioner " + getName() + "  gr " + gr);

		qs = currentMotorSpeed.divide(gr);
		logger.debug("AngularPositioner " + getName() + "  qs " + qs);

		return qs;
	}
}
