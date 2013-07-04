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
 * This DOF is used for the situation where a LinearPositioner can cause angular movement by pushing at one end. The
 * LinearPositioner is used in a SingleAxisLinearDOF. That SingleAxisLinearDOF should be specified as the Moveable of
 * this DOF. This DOF was first produced for the mirror and grating movements in a monochromator of the type used at 5U.
 */
public class SineDriveAngularDOF extends DOF {
	private static final Logger logger = LoggerFactory.getLogger(SineDriveAngularDOF.class);

	private Angle angleLowerLimit = Quantity.valueOf(0.0, SI.RADIAN);

	private Angle angleUpperLimit = Quantity.valueOf(0.0, SI.RADIAN);

	private Angle angleOffset = Quantity.valueOf(0.0, SI.RADIAN);

	private Length armLength = Quantity.valueOf(0.0, SI.METER);

	/**
	 * 
	 */
	public SineDriveAngularDOF() {
	}

	@Override
	public void configure() throws FactoryException {
		super.configure();
		updatePosition();
		setPositionValid(true);
		updateStatus();
	}

	/**
	 * @return Returns the angleLowerLimit expressed in degrees
	 */
	public double getAngleLowerLimit() {
		return angleLowerLimit.to(NonSIext.DEG_ANGLE).getAmount();
	}

	/**
	 * @param angleLowerLimit
	 *            The angleLowerLimit to set expressed in degrees
	 */
	public void setAngleLowerLimit(double angleLowerLimit) {
		this.angleLowerLimit = Quantity.valueOf(angleLowerLimit, NonSIext.DEG_ANGLE);
	}

	/**
	 * @return Returns the angleOffset in degrees
	 */
	public double getAngleOffset() {
		return angleOffset.to(NonSIext.DEG_ANGLE).getAmount();
	}

	/**
	 * @param angleOffset
	 *            The angleOffset to set expressed in degrees
	 */
	public void setAngleOffset(double angleOffset) {
		this.angleOffset = Quantity.valueOf(angleOffset, NonSIext.DEG_ANGLE);
	}

	/**
	 * @return Returns the angleUpperLimit expressed in degrees
	 */
	public double getAngleUpperLimit() {
		return angleUpperLimit.to(NonSIext.DEG_ANGLE).getAmount();
	}

	/**
	 * @param angleUpperLimit
	 *            the angleUpperLimit to set, expressed in degrees.
	 */
	public void setAngleUpperLimit(double angleUpperLimit) {
		this.angleUpperLimit = Quantity.valueOf(angleUpperLimit, NonSIext.DEG_ANGLE);
	}

	/**
	 * @return Returns the armLength expressed in metres
	 */
	public double getArmLength() {
		return armLength.doubleValue();
	}

	/**
	 * @param armLength
	 *            the armLength to set expressed in metres.
	 */
	public void setArmLength(double armLength) {
		this.armLength = Quantity.valueOf(armLength, SI.METER);
	}

	@Override
	public void moveContinuously(int direction) throws MoveableException {
		// Deliberately does nothing
	}

	/**
	 * UpdatePosition method used to calculate currentQuantity from position of moveables, called from update method of
	 * DOF
	 */
	@Override
	protected void updatePosition() {
		try {
			Quantity q = moveables[0].getPosition().minus(moveables[0].getPositionOffset()).divide(armLength);

			setCurrentQuantity(Quantity.valueOf(Math.asin(q.doubleValue()), SI.RADIAN).plus(angleOffset));
		} catch (MoveableException e) {
			logger.error(e.getMessage());
		}
	}

	/**
	 * Calculates the position the linear moveable needed in order to set the required angle
	 * 
	 * @param fromQuantity
	 *            the Quantity (angle) for which the linear moveable positions is to be calculated
	 * @return an array of Length Quantities holding the required linear moveable length
	 */
	@Override
	protected Quantity[] calculateMoveables(Quantity fromQuantity) {
		Quantity positions[] = new Quantity[1];
		try {
			Quantity sinDiff = ((Angle) fromQuantity.minus(angleOffset)).sine();
			Quantity length = sinDiff.times(armLength).plus(moveables[0].getPositionOffset());
			positions[0] = length;
		} catch (Exception e) {
			logger.error("Exception in calculateMoveables " + e.getMessage());
		}
		return positions;
	}

	/**
	 * Given a Quantity checks whether its units are acceptable and if so constructs a new Quantity of the correct
	 * subclass for this DOF and returns it
	 * 
	 * @param newQuantity
	 *            the Quantity to be checked
	 * @return an Energy or length of the same numerical value and Units
	 */
	@Override
	protected Quantity checkTarget(Quantity newQuantity) {
		Angle rtrn = null;
		try {
			rtrn = (Angle) newQuantity;
		} catch (ConversionException cce) {
			// Deliberately do nothing because if the cast fails then the
			// targetQuantity is not a length so we should return null
		}
		return rtrn;
	}

	@Override
	protected void setDefaultAcceptableUnits() {
		defaultAcceptableUnits = new ArrayList<Unit<? extends Quantity>>();

		defaultAcceptableUnits.add(NonSIext.mDEG_ANGLE);
		defaultAcceptableUnits.add(NonSIext.DEG_ANGLE);
		defaultAcceptableUnits.add(SI.RADIAN);
	}

	@SuppressWarnings( { "cast", "unchecked", "rawtypes" })
	@Override
	protected void setValidAcceptableUnits() {
		validAcceptableUnits = new ArrayList<Unit<? extends Quantity>>();

		// Leave these 'unnecessary' casts alone!! - see bug #634
		validAcceptableUnits.add((Unit) NonSIext.mDEG_ANGLE.getBaseUnits());
		validAcceptableUnits.add((Unit) NonSIext.DEG_ANGLE.getBaseUnits());
		validAcceptableUnits.add((Unit) SI.RADIAN.getBaseUnits());
	}

	@Override
	public Quantity getSoftLimitLower() {
		try {
			Quantity q = moveables[0].getSoftLimitLower().minus(moveables[0].getPositionOffset()).divide(armLength);

			return Quantity.valueOf(Math.asin(q.doubleValue()), SI.RADIAN).plus(angleOffset);
		} catch (Exception e) {
			logger.error(e.getMessage());
			return null;
		}
	}

	@Override
	public Quantity getSoftLimitUpper() {
		try {
			Quantity q = moveables[0].getSoftLimitUpper().minus(moveables[0].getPositionOffset()).divide(armLength);

			return Quantity.valueOf(Math.asin(q.doubleValue()), SI.RADIAN).plus(angleOffset);
		} catch (Exception e) {
			logger.error(e.getMessage());
			return null;
		}
	}
}