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
import gda.oe.MoveableException;

import java.util.ArrayList;

import org.jscience.physics.quantities.Energy;
import org.jscience.physics.quantities.Quantity;
import org.jscience.physics.units.ConversionException;
import org.jscience.physics.units.NonSI;
import org.jscience.physics.units.SI;
import org.jscience.physics.units.Unit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provide support for beam energy.
 */
public class EnergyPositioner extends Positioner {
	private static final Logger logger = LoggerFactory.getLogger(EnergyPositioner.class);
	protected String driveUnitName = LocalProperties.get("gda.unit.name");
	protected Unit<Energy> driveUnit = SI.KILO(NonSI.ELECTRON_VOLT);

	@Override
	protected void _initialisation() {
		// no special unit setting required

	}

	/**
	 * Moves the positioner to the specified position in keV
	 * 
	 * @param position
	 *            the position to move to
	 * @throws MotorException
	 */
	@Override
	protected void _moveTo(double position) throws MotorException {
		double steps = (position - this.positionOffset) * getStepsPerUnit();
		motor.moveTo(steps);
	}

	@Override
	protected void _updatePosition(double motorPosition) {
		currentPosition = this.positionOffset + (motorPosition / getStepsPerUnit());
	}

	/**
	 * update the current motor Lower limit in GDA. This method converts EPICS motor limits to GDA motor limit in GDA
	 * drive unit.
	 * 
	 * @param lowLimit
	 */
	@Override
	protected void _updateLimitLow(double lowLimit) {
		softLimitLow = this.positionOffset + (lowLimit / getStepsPerUnit());
	}

	/**
	 * update the current motor high limit in GDA. This method converts EPICS motor limits to GDA motor limit in GDA
	 * drive unit.
	 * 
	 * @param highLimit
	 */
	@Override
	protected void _updateLimitHigh(double highLimit) {
		setSoftLimitHighValue(this.positionOffset + (highLimit / getStepsPerUnit()));
	}

	/**
	 * If the motor limits are settable this method converts positioner units into motor units and calls the motor to
	 * set the physical motor limits
	 * 
	 * @param lowLimit
	 * @param highLimit
	 * @throws MotorException
	 */
	@Override
	protected void _setSoftLimits(double lowLimit, double highLimit) throws MotorException {
		if (motor != null && motor.isLimitsSettable()) {
			logger.debug("calling motor setLimits");
			double lowLimitSteps = (lowLimit - this.positionOffset) * getStepsPerUnit();
			double highLimitSteps = (highLimit - this.positionOffset) * getStepsPerUnit();
			motor.setSoftLimits(lowLimitSteps, highLimitSteps);
		}
	}

	/**
	 * Sets the current position to be denoted by the passed value
	 * 
	 * @param position
	 *            the new value to represent the current position
	 * @throws MotorException
	 */
	@Override
	protected void _setPosition(double position) throws MotorException {
		double steps = (position - this.positionOffset) * getStepsPerUnit();
		if (motor != null) {
			motor.setPosition(steps);
		}
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
	 * checks that targetQuantity has the correct units and if it has calculates targetPosition from it
	 * 
	 * @param targetQuantity
	 *            the target Quantity
	 * @return true if the Quantity is alright, false otherwise
	 */
	@Override
	protected boolean checkTarget(Quantity targetQuantity) {
		boolean rtrn = false;
		// any Quantity with Energy units is acceptable but the
		// double value we actually want must be in keV because
		// stepsPerkeV is what this positioner knows
		try {
			targetPosition = ((Energy) targetQuantity).to(driveUnit).getAmount();
			rtrn = true;
		} catch (ConversionException cce) {
			// Deliberately do nothing because if the cast fails then the
			// targetQuantity is not a length so we should return false
		}
		return rtrn;
	}

	/**
	 * checks that increment has the correct units and if it has calculates targetPosition from it
	 * 
	 * @param increment
	 *            the increment
	 * @return true if the Quantity is alright, false otherwise
	 */
	@Override
	protected boolean checkIncrement(Quantity increment) {
		boolean rtrn = false;
		// any Quantity with Energy units is acceptable but the
		// double value we actually want must be in keV because
		// stepsPerKev is what this positioner knows
		try {
			targetPosition = currentPosition + ((Energy) increment).to(driveUnit).getAmount();
			rtrn = true;
		} catch (ConversionException cce) {
			// Deliberately do nothing because if the cast fails then the
			// increment is not a length so we should return false
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
			setSoftLimitHighValue(((Energy) highLimit).to(driveUnit).getAmount());
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
			setSoftLimitLowValue(((Energy) lowLimit).to(driveUnit).getAmount());
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
		// any Quantity with Energy units is acceptable but the
		// double value we actually want must be in KeV because
		// stepsPerKeV is what this positioner knows
		try {
			setHomeOffsetValue(((Energy) offset).to(driveUnit).getAmount());
			rtrn = true;
		} catch (ConversionException cce) {
			// Deliberately do nothing because if the cast fails then the
			// offset is not a length so we should return false
		}
		return rtrn;
	}

	@Override
	protected boolean _setPositionOffset(Quantity offset) {
		boolean rtrn = false;
		// any Quantity with Energy units is acceptable but the
		// double value we actually want must be in KeV because
		// stepsPerKeV is what this positioner knows
		try {
			setPositionOffsetValue(((Energy) offset).to(driveUnit).getAmount());
			rtrn = true;
		} catch (ConversionException cce) {
			// Deliberately do nothing because if the cast fails then the
			// offset is not a length so we should return false
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
		// Unit<? extends Quantity> u = speed.getUnit();
		// if (!u.equals(QuantityFactory.createUnitFromString("keV/s"))
		// && !u.equals(QuantityFactory.createUnitFromString("Hz keV")))
		// {
		// throw new MoveableException(new MoveableStatus(
		// MoveableStatus.SPEEDLEVEL_ERROR, getName()),
		// "Incorrect type of speed for LinearPositioner: " + speed);
		// }

		// calculate motor speed

		logger.debug("EnergyPositioner " + getName() + "  requested speed " + speed);

		Quantity spu = Quantity.valueOf(getStepsPerUnit(), Unit.ONE);
		logger.debug("EnergyPositioner " + getName() + "  spu " + spu);

		Quantity du = Quantity.valueOf(1.0, driveUnit);
		logger.debug("EnergyPositioner " + getName() + "  du " + du);

		Quantity gr = spu.divide(du);
		logger.debug("EnergyPositioner " + getName() + "  gr " + gr);

		Quantity requiredMotorSpeed = speed.times(gr);
		logger.debug("EnergyPositioner " + getName() + "  requiredMotorSpeed " + requiredMotorSpeed);

		double speedValue = Math.abs(requiredMotorSpeed.doubleValue());
		if (speedValue > 0.0) { // set motor speed
			try {
				motor.setSpeed(speedValue);
			} catch (MotorException me) {
				throw new MoveableException(MoveableStatusFactory.createMoveableStatus(me.status, getName()), me
						.getMessage());
			}
		}
		/*
		 * else { throw new MoveableException(new MoveableStatus( MoveableStatus.SPEEDLEVEL_ERROR, getName()), "Cannot
		 * set speed " + speedValue); }
		 */
	}

	@Override
	public void setSpeed(Quantity start, Quantity end, Quantity time) throws MoveableException {
		setSpeed(end.minus(start).to(SI.KILO(NonSI.ELECTRON_VOLT)).divide(time.to(SI.SECOND)));
	}

	@Override
	public Quantity getSpeed() {
		Quantity qs = null;

		// get motor speed
		double speed = 0;
		try {
			speed = motor.getSpeed();
		} catch (MotorException e) {
			logger.error("Exception: " + e.getMessage(), e);
		}

		// calculate Positioner speed
		Quantity currentMotorSpeed = Quantity.valueOf(speed, SI.HERTZ);
		logger.debug("EnergyPositioner " + getName() + "  currentMotorSpeed " + currentMotorSpeed);

		Quantity spu = Quantity.valueOf(getStepsPerUnit(), Unit.ONE);
		logger.debug("EnergyPositioner " + getName() + "  spu " + spu);

		Quantity du = Quantity.valueOf(1.0, driveUnit);
		logger.debug("EnergyPositioner " + getName() + "  du " + du);

		Quantity gr = spu.divide(du);
		logger.debug("EnergyPositioner " + getName() + "  gr " + gr);

		qs = currentMotorSpeed.divide(gr);
		logger.debug("EnergyPositioner " + getName() + "  qs " + qs);

		return qs;
	}

}
