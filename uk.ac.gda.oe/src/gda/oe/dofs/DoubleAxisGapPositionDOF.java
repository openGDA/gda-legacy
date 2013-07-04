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

import gda.configuration.properties.LocalProperties;
import gda.factory.FactoryException;
import gda.oe.MoveableException;

import java.util.ArrayList;

import org.jscience.physics.quantities.Length;
import org.jscience.physics.quantities.Quantity;
import org.jscience.physics.units.ConversionException;
import org.jscience.physics.units.SI;
import org.jscience.physics.units.Unit;

/**
 * A DOF responsible for a gap between two linear moveables, the position of this DOF is the position of the gap. Note
 * that with the positive is outwards convention for jaw movement the coordinates of this DOF have to be arbitrarily
 * chosen to be the same as one of the jaws, here the FIRST jaw is always used.
 */
public class DoubleAxisGapPositionDOF extends DOF {
	private String coordinate = LocalProperties.get("gda.coordinate.name");

	/**
	 * Constructor.
	 */
	public DoubleAxisGapPositionDOF() {
	}

	@Override
	public void configure() throws FactoryException {
		super.configure();
		updatePosition();
		setPositionValid(true);
		updateStatus();
	}

	@Override
	public void moveContinuously(int direction) throws MoveableException {
		// Moving continuously for this DOF means moving the centre
		// of the gap continuously. The positive direction is taken
		// to be the same as that for moveable[0], so it must move
		// in the same direction as the gap.

		if (moveables[0] != null && moveables[1] != null) {
			moveables[0].moveContinuously(direction);
			moveables[1].moveContinuously(-1 * direction);
		}
	}

	/**
	 * called by setMoveables and moveMoveables calculates values to use as positions of moveables
	 * 
	 * @param fromQuantity
	 *            required position
	 * @return an array of positions to move to
	 */
	@Override
	protected Quantity[] calculateMoveables(Quantity fromQuantity) {
		// NB moveables will return Lengths so all calculations are in meters
		double d1 = 0, d2 = 0;
		double requiredPos = ((Length) fromQuantity).to(getReportingUnits()).doubleValue();
		double gap;

		// Remember the position of this DOF is the position of the
		// centre of the gap in the coordinates of moveable[0].
		// We need to calculate positions for the moveables which will
		// change this without changing the width of the gap
		// With the positive is away from the beam convention the
		// width of the gap is the SUM of the two positions and this
		// must remain unchanged.
		// Compare the same method in DoubleAxisGapDOF.java.

		if (coordinate != null && coordinate.compareTo("DLS-coordinate") == 0) {
			// new DLS coordinate system - absolute
			gap = moveables[1].getPosition().to(getReportingUnits()).doubleValue()
					- moveables[0].getPosition().to(getReportingUnits()).doubleValue();

			// After the move (d2 - d1) must still equal the gap and
			// (d1 + d2)/ 2.0 must equal the requiredPos so:
			d1 = requiredPos - gap / 2.0;
			d2 = requiredPos + gap / 2.0;
		} else {
			// Existing SRS coordinate system - relative to beam as default
			gap = moveables[0].getPosition().to(getReportingUnits()).doubleValue()
					+ moveables[1].getPosition().to(getReportingUnits()).doubleValue();

			// After the move (d1 + d2) must still equal the gap and
			// (d1 - d2)/ 2.0 must equal the requiredPos so:
			d1 = gap / 2.0 + requiredPos;
			d2 = gap / 2.0 - requiredPos;
		}

		Quantity rtrn[] = { Quantity.valueOf(d1, getReportingUnits()), Quantity.valueOf(d2, getReportingUnits()) };
		return rtrn;
	}

	/*
	 * UpdatePosition method used to calculate currentQuantity from position of moveables, called from update method of
	 * DOF
	 */
	@Override
	protected void updatePosition() {
		// The currentPosition of this DOF is actually
		// the current gap position. With the positive is outwards
		// convention we arbitrarily choose to calculate this it
		// moveable[0] coordinates
		// NB moveables will return Lengths so all calculations are in m
		double p0 = moveables[0].getPosition().to(getReportingUnits()).doubleValue();
		double p1 = moveables[1].getPosition().to(getReportingUnits()).doubleValue();

		if (coordinate != null && coordinate.compareTo("DLS-coordinate") == 0) {
			// new dls coordinate system
			setCurrentQuantity(Quantity.valueOf((p1 + p0) / 2.0, getReportingUnits()));
		} else {
			// existing SRS coordinate system - as default
			setCurrentQuantity(Quantity.valueOf((p0 - p1) / 2.0, getReportingUnits()));
		}
		setPositionValid(true);
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
		Quantity limit0 = moveables[0].getSoftLimitLower().to(getReportingUnits());
		Quantity limit1 = moveables[1].getSoftLimitLower().to(getReportingUnits());

		if (coordinate != null && coordinate.compareTo("DLS-coordinate") == 0) {
			// new dls coordinate system
			return Quantity.valueOf((limit1.doubleValue() + limit0.doubleValue()) / 2.0, getReportingUnits());
		}
		// existing SRS coordinate system - as default
		return Quantity.valueOf((limit0.doubleValue() - limit1.doubleValue()) / 2.0, getReportingUnits());
	}

	@Override
	public Quantity getSoftLimitUpper() {
		Quantity limit0 = moveables[0].getSoftLimitUpper().to(getReportingUnits());
		Quantity limit1 = moveables[1].getSoftLimitUpper().to(getReportingUnits());

		if (coordinate != null && coordinate.compareTo("DLS-coordinate") == 0) {
			// new dls coordinate system
			return Quantity.valueOf((limit1.doubleValue() + limit0.doubleValue()) / 2.0, getReportingUnits());
		}
		// existing SRS coordinate system - as default
		return Quantity.valueOf((limit0.doubleValue() - limit1.doubleValue()) / 2.0, getReportingUnits());
	}
}
