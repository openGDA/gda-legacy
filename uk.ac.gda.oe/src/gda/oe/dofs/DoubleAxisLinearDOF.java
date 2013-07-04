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
import gda.oe.Moveable;
import gda.oe.MoveableException;
import gda.oe.positioners.Positioner;

import java.util.ArrayList;

import org.jscience.physics.quantities.Length;
import org.jscience.physics.quantities.Quantity;
import org.jscience.physics.units.ConversionException;
import org.jscience.physics.units.SI;
import org.jscience.physics.units.Unit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A DOF responsible for linear motion using two linear moveables.
 */

public class DoubleAxisLinearDOF extends DOF {
	private static final Logger logger = LoggerFactory.getLogger(DoubleAxisLinearDOF.class);

	// Separation between the two linear axes.
	private double separation = 0.0;

	// Offset of the virtual rotation axis from the 1st motor axis in mm.
	// This offset MUST be in MM.
	// Positive values indicate that the virtual axis is on the side
	// of the 1st motor axis away from the second motor axis.
	private double axisOffset = 0.0;

	private boolean centralOffset = true;

	/**
	 * Constructor.
	 */
	public DoubleAxisLinearDOF() {
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
		if (moveables[0] != null && moveables[1] != null) {
			moveables[0].moveContinuously(direction);
			moveables[1].moveContinuously(direction);
		}
	}

	/*
	 * called by setMoveables and moveMoveables calculates values to use as positions of moveables
	 */
	@Override
	protected Quantity[] calculateMoveables(Quantity fromQuantity) {
		// NB offset and separation are in mm so all calculations are
		// done in mm.
		// Calculated positions for the 2 linear moveables.
		double d1 = 0, d2 = 0;

		double pos = fromQuantity.to(SI.MILLI(SI.METER)).getAmount();

		// For centralOffset calculate so that the difference between the
		// two moveables is maintained.
		// For non centralOffset calculate so that the move is achieved
		// about the central point between the two linear axes, but by
		// rotating about the virtual rotation axis.
		if (centralOffset) {
			double p0 = moveables[0].getPosition().to(SI.MILLI(SI.METER)).getAmount();
			double p1 = moveables[1].getPosition().to(SI.MILLI(SI.METER)).getAmount();
			double diff = p0 - p1;

			d1 = pos + (diff / 2.0);
			d2 = pos - (diff / 2.0);
		} else {
			double tanOfAngle = pos / (axisOffset + (separation / 2.0));
			d1 = axisOffset * tanOfAngle;
			d2 = (separation + axisOffset) * tanOfAngle;
		}
		Quantity rtrn[] = { Quantity.valueOf(d1, SI.MILLI(SI.METER)), Quantity.valueOf(d2, SI.MILLI(SI.METER)) };

		return (rtrn);
	}

	/*
	 * UpdatePosition method used to calculate currentQuantity from position of moveables, called from update method of
	 * DOF
	 */

	@Override
	protected void updatePosition() {
		// NB offset and separation are in mm so all calculations are
		// done in mm.
		double p0 = moveables[0].getPosition().to(SI.MILLI(SI.METER)).getAmount();
		double p1 = moveables[1].getPosition().to(SI.MILLI(SI.METER)).getAmount();

		double cpmm = (p0 + p1) / 2.0;

		setCurrentQuantity(Quantity.valueOf(cpmm, SI.MILLI(SI.METER)));

		if (centralOffset) {
			setPositionValid(true);
		} else {
			// Calculate the angle the between a line joining the DOF's
			// central
			// point to the pivot point and horizontal
			double theta1 = Math.atan(cpmm / (axisOffset + (separation / 2.0)));

			// Calculate the angle the DOF makes to the horizontal.
			double theta2 = Math.atan((p1 - p0) / separation);

			double angleError = Math.abs((theta2 - theta1) / theta1);

			if (Double.isNaN(angleError)) {
				// A special case when the angle is 0.
				setPositionValid(true);
			} else {
				// If the angles are equal we are in a valid position.
				// A crude approximation.
				setPositionValid(angleError < 0.001 ? true : false);
			}
		}
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
		Length rtrn = null;
		try {
			rtrn = (Length) newQuantity;
		} catch (ConversionException cce) {
			// Deliberately do nothing because if the cast fails then the
			// targetQuantity is not a length so we should return null
		}
		return rtrn;
	}

	/**
	 * Calculates the number of decimal places for formatting positions, based upon things like the gearing and
	 * combination of underlying positioners.
	 * 
	 * @return the number of decimal places
	 */

	@Override
	public int calculateDecimalPlaces() {
		// README: this is one of several instances of DOFs needing to know
		// that their Moveables are actually positioners. These should
		// be expunged - see bug #442.
		int decimalPlaces = 0;
		double precision;

		if (moveables != null && moveables[0] instanceof Positioner && moveables[1] instanceof Positioner) {
			int dp = getDecimalPlaces();
			if (dp != -1) {
				decimalPlaces = dp;
			} else {
				Positioner p1 = (Positioner) moveables[0];
				Positioner p2 = (Positioner) moveables[1];

				double precision1 = Math.abs(1.0 / p1.getStepsPerUnit());
				double precision2 = Math.abs(1.0 / p2.getStepsPerUnit());
				precision = (precision1 + precision2) / 2.0;
				if (getReportingUnits().equals(SI.CENTI(SI.METER))) {
					precision /= 10;
				} else if (getReportingUnits().equals(SI.MICRO(SI.METER))) {
					precision *= 1000;
				}

				while (precision < 1) {
					decimalPlaces++;
					precision *= 10.0;
				}
				if (Math.abs(precision - Math.round(precision)) >= 0.1) {
					decimalPlaces++;
				}
			}
		}
		return decimalPlaces;
	}

	@Override
	protected void setDefaultAcceptableUnits() {
		defaultAcceptableUnits = new ArrayList<Unit<? extends Quantity>>();

		defaultAcceptableUnits.add(SI.MILLI(SI.METER));
		defaultAcceptableUnits.add(SI.MICRO(SI.METER));
	}

	@SuppressWarnings( { "cast", "unchecked", "rawtypes" })
	@Override
	protected void setValidAcceptableUnits() {
		validAcceptableUnits = new ArrayList<Unit<? extends Quantity>>();

		// Leave these 'unnecessary' casts alone!! - see bug #634
		validAcceptableUnits.add((Unit) SI.MILLI(SI.METER).getBaseUnits());
		validAcceptableUnits.add((Unit) SI.MICRO(SI.METER).getBaseUnits());
	}

	@Override
	public Quantity getSoftLimitLower() {
		double p0 = moveables[0].getSoftLimitLower().to(getReportingUnits()).getAmount();
		double p1 = moveables[1].getSoftLimitLower().to(getReportingUnits()).getAmount();

		double cpmm = (p0 + p1) / 2.0;

		return Quantity.valueOf(cpmm, SI.MILLI(SI.METER));
	}

	@Override
	public Quantity getSoftLimitUpper() {
		double p0 = moveables[0].getSoftLimitUpper().to(getReportingUnits()).getAmount();
		double p1 = moveables[1].getSoftLimitUpper().to(getReportingUnits()).getAmount();

		double cpmm = (p0 + p1) / 2.0;

		return Quantity.valueOf(cpmm, SI.MILLI(SI.METER));
	}

	@Override
	public void setSpeed(Quantity start, Quantity end, Quantity time) throws MoveableException {
		logger.debug("DoubleAxisLinearDOF setSpeed called with start, end, time: " + start + " " + end + " " + time);
		for (Moveable m : moveables) {
			m.setSpeed(start, end, time);
		}
	}
}
