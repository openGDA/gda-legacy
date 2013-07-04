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
import gda.oe.positioners.Positioner;

import java.util.ArrayList;

import org.jscience.physics.quantities.Angle;
import org.jscience.physics.quantities.Dimensionless;
import org.jscience.physics.quantities.Length;
import org.jscience.physics.quantities.Quantity;
import org.jscience.physics.units.ConversionException;
import org.jscience.physics.units.SI;
import org.jscience.physics.units.Unit;

/**
 * A DOF responsible for angular motion using two linear moveables.
 */
public class DoubleAxisAngularDOF extends DOF {
	// Separation between the two linear axes in mm.
	private double separation = 0.0;

	// Offset of the virtual rotation axis from the 1st motor axis.
	// This offset MUST be in MM.
	// Positive values indicate that the virtual axis is on the side
	// of the 1st motor axis away from the second motor axis.
	private double axisOffset = 0.0;

	private boolean centralOffset = true;

	/**
	 * Constructor
	 */
	public DoubleAxisAngularDOF() {
	}

	@Override
	public void configure() throws FactoryException {
		super.configure();

		updatePosition();
		setPositionValid(true);
		updateStatus();
	}

	/**
	 * @param separation
	 */
	public void setSeparation(double separation) {
		this.separation = separation;
	}

	/**
	 * @return separation
	 */
	public double getSeparation() {
		return separation;
	}

	/**
	 * @param axisOffset
	 */
	public void setAxisOffset(double axisOffset) {
		this.axisOffset = axisOffset;
	}

	/**
	 * @return axisOffset
	 */
	public double getAxisOffset() {
		return axisOffset;
	}

	/**
	 * @param centralOffset
	 */
	public void setCentralOffset(boolean centralOffset) {
		this.centralOffset = centralOffset;
	}

	/**
	 * @return centralOffset
	 */
	public boolean isCentralOffset() {
		return centralOffset;
	}

	@Override
	public void moveContinuously(int direction) throws MoveableException {
		moveables[0].moveContinuously(direction);
		moveables[1].moveContinuously(direction);
	}

	@Override
	protected Quantity[] calculateMoveables(Quantity fromQuantity) {
		double d1 = moveables[0].getPosition().to(SI.MILLI(SI.METER)).getAmount();
		double d2 = moveables[1].getPosition().to(SI.MILLI(SI.METER)).getAmount();

		double height = 0.0;
		if (centralOffset) {
			height = (d1 + d2) / 2;
		}

		if (relativeMove) {
			Quantity relativeAngle = fromQuantity.minus(getCurrentQuantity());
			double angleValue = relativeAngle.to(SI.RADIAN).getAmount();
			d1 += Math.tan(angleValue) * axisOffset;
			d2 += Math.tan(angleValue) * (axisOffset + separation);
		} else {
			double absoluteAngle = ((Angle) fromQuantity).to(SI.RADIAN).getAmount();
			d1 = Math.tan(absoluteAngle) * axisOffset;
			d2 = Math.tan(absoluteAngle) * (axisOffset + separation);
			d1 += height;
			d2 += height;
		}

		Quantity rtrn[] = { Quantity.valueOf(d1, SI.MILLI(SI.METER)), Quantity.valueOf(d2, SI.MILLI(SI.METER)) };
		return (rtrn);
	}

	@Override
	protected void updatePosition() {
		double currentOffset = 0.0;
		double offsetError = 0.0;

		if (moveables[0] != null && moveables[1] != null) {
			// p1 and p2 need to be in mm because separation and offset
			// are
			Quantity x = moveables[0].getPosition().to(SI.MILLI(SI.METER));
			double p1 = x.getAmount();
			// double p1 =
			// moveables[0].getPosition().to(SI.MILLI(SI.METER)).getAmount();
			double p2 = moveables[1].getPosition().to(SI.MILLI(SI.METER)).getAmount();

			setCurrentQuantity(Quantity.valueOf(Math.atan((p2 - p1) / separation), SI.RADIAN));

			// Do some checks to see if any changes in the two independent
			// moveables leave this DOF in a valid position state.
			if (centralOffset) {
				setPositionValid(true);
			} else {
				currentOffset = p1 / Math.tan(getCurrentQuantity().doubleValue());

				offsetError = Math.abs((currentOffset - axisOffset) / axisOffset);
				if (Double.isNaN(offsetError)) {
					// A special case when the angle is 0.
					setPositionValid(true);
				} else {
					// If the DOF does not go through the point of rotation,
					// within expected errors, then its position is invalid.
					double e = estimateError(p1, p2);
					setPositionValid(offsetError < e ? true : false);
				}
			}

			// FIXME: The determination of validity needs re-visiting.
			// I think some similar account of relative/absolute would have
			// to be
			// made,
			// as in calculateMoveables().
			// Validity is only being used as feedback in OEMove.
			// The below effectively ignores validity for now.
			setPositionValid(true);
		}
	}

	/**
	 * Estimates the expected fractional error in the current offset due to the integer step nature of motors.
	 * 
	 * @param p1
	 *            Moveable 1 value.
	 * @param p2
	 *            Moveable 2 value.
	 * @return estimated error
	 */
	private double estimateError(double p1, double p2) {
		// offset = p1 / tan(position).
		// position = atan((p2 - p1) / separation).
		// Therefore offset = (p1 * separation) / (p2 - p1).
		//
		// foff (fractional error in offset) = sqrt((fp1)**2 + (fdiff)**2).
		// fp1 - fractional error in p1.
		// fdiff - fractional error in (p2 - p1).
		//
		// fdiff = (actual error in (p2 -p1)) / (p2 - p1).
		// p1 = (steps1 +/- 0.5) / gear1.
		// p2 = (steps2 +/- 0.5) / gear2.
		// Therfore fdiff = ((0.5 / gear1) + (0.5 / gear2)) / (p2 - p1).

		double fp1 = 0.0, fdiff = 0.0;

		// The gearing ratios for the two moveables.
		double g1 = ((Positioner) moveables[0]).getStepsPerUnit();
		double g2 = ((Positioner) moveables[1]).getStepsPerUnit();

		// The fractional error in p1.
		fp1 = Math.pow((0.5 / (g1 * p1)), 2.0);

		// The fractional error in (p2 - p1).
		fdiff = (0.5 / g1) + (0.5 / g2);
		fdiff = Math.pow(fdiff / (p2 - p1), 2.0);

		return (Math.sqrt(fp1 + fdiff));
	}

	/**
	 * given a Quantity checks whether its units are acceptable and if so constructs a new Quantity of the correct
	 * subclass for this DOF and returns it
	 * 
	 * @param newQuantity
	 *            the Quantity to be checked
	 * @return an Energy of the same numerical value and Units
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
	public int calculateDecimalPlaces() {
		int decimalPlaces = 0;
		double precision;

		// README: this is one of several instances of DOFs needing to know
		// that their Moveables are actually positioners. These should
		// be expunged. NB It is important to retain a sensible default
		// value for decimalPlace - see bug #442
		if (moveables != null) {
			int dp = getDecimalPlaces();
			if (dp != -1) {
				decimalPlaces = dp;
			} else {
				Positioner p1 = (Positioner) moveables[0];
				Positioner p2 = (Positioner) moveables[1];

				double precision1 = Math.abs(1.0 / p1.getStepsPerUnit());
				double precision2 = Math.abs(1.0 / p2.getStepsPerUnit());

				Length x = Quantity.valueOf(separation, SI.MILLI(SI.METER));
				Length y1 = Quantity.valueOf(1, precision1, SI.MILLI(SI.METER));
				Length y2 = Quantity.valueOf(1, precision2, SI.MILLI(SI.METER));
				Length y = (Length) y1.plus(y2);
				Angle a = Angle.atan2(y, x);
				// precision = a.in(Angle.MDEG).absoluteError();
				precision = a.getAbsoluteError();

				while (precision < 1) {
					decimalPlaces++;
					precision *= 10.0;
				}
				if (Math.abs(precision - Math.round(precision)) >= 0.1) {
					decimalPlaces++;
				}
				if (getReportingUnits().equals(NonSIext.DEG_ANGLE)) {
					decimalPlaces += 3;
				}
			}
		}
		return decimalPlaces;
	}

	@Override
	protected void setDefaultAcceptableUnits() {
		defaultAcceptableUnits = new ArrayList<Unit<? extends Quantity>>();

		defaultAcceptableUnits.add(NonSIext.mDEG_ANGLE);
		defaultAcceptableUnits.add(NonSIext.DEG_ANGLE);
	}

	@SuppressWarnings( { "cast", "unchecked", "rawtypes" })
	@Override
	protected void setValidAcceptableUnits() {
		validAcceptableUnits = new ArrayList<Unit<? extends Quantity>>();

		// Leave these 'unnecessary' casts alone!! - see bug #634
		validAcceptableUnits.add((Unit) NonSIext.mDEG_ANGLE.getBaseUnits());
		validAcceptableUnits.add((Unit) NonSIext.DEG_ANGLE.getBaseUnits());
	}

	@Override
	public Quantity getSoftLimitLower() {
		Quantity limit0 = moveables[0].getSoftLimitLower().to(getReportingUnits());
		Quantity limit1 = moveables[1].getSoftLimitLower().to(getReportingUnits());

		return Dimensionless.valueOf(Math.atan((limit1.doubleValue() - limit0.doubleValue()) / separation));
	}

	@Override
	public Quantity getSoftLimitUpper() {
		Quantity limit0 = moveables[0].getSoftLimitUpper().to(getReportingUnits());
		Quantity limit1 = moveables[1].getSoftLimitUpper().to(getReportingUnits());

		return Dimensionless.valueOf(Math.atan((limit1.doubleValue() - limit0.doubleValue()) / separation));
	}

}
