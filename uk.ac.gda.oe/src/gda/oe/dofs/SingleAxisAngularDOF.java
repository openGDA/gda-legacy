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
import org.jscience.physics.quantities.Quantity;
import org.jscience.physics.units.ConversionException;
import org.jscience.physics.units.SI;
import org.jscience.physics.units.Unit;

/**
 * A DOF responsible for linear motion using one linear moveable.
 */
public class SingleAxisAngularDOF extends DOF {
	/**
	 *
	 */
	public SingleAxisAngularDOF() {
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
		moveables[0].moveContinuously(direction);
	}

	/**
	 * UpdatePosition method used to calculate currentQuantity from position of moveables, called from update method of
	 * DOF
	 */
	@Override
	protected void updatePosition() {
		setCurrentQuantity(moveables[0].getPosition());
		setPositionValid(true);
	}

	@Override
	protected Quantity[] calculateMoveables(Quantity fromQuantity) {
		Quantity rtrn[] = { fromQuantity };
		return rtrn;
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

	/**
	 * returns the home position distance/offset converted to reporting units
	 * 
	 * @return the offset
	 */
	@Override
	public Quantity getPositionOffset() {
		Angle newValue = null;
		try {
			newValue = (Angle) moveables[0].getPositionOffset();
		} catch (MoveableException doe) {
		}
		return newValue;
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
		// be expunged. NB It is important to retain a sensible default
		// value for decimalPlace - see bug #442
		int decimalPlaces = 0;

		if (moveables[0] instanceof Positioner) {
			int dp = getDecimalPlaces();
			if (dp != -1) {
				decimalPlaces = dp;
			} else {
				Positioner p = (Positioner) moveables[0];

				double precision = Math.abs(1.0 / p.getStepsPerUnit());
				if (precision == 1.0) {
					decimalPlaces = 3;
				} else {
					if (getReportingUnits().equals(NonSIext.DEG_ANGLE)) {
						precision /= 1000;
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
		}
		return decimalPlaces;
	}

	@Override
	protected void setDefaultAcceptableUnits() {
		defaultAcceptableUnits = new ArrayList<Unit<? extends Quantity>>();

		defaultAcceptableUnits.add(NonSIext.mDEG_ANGLE);
		defaultAcceptableUnits.add(NonSIext.DEG_ANGLE);
		defaultAcceptableUnits.add(NonSIext.μRADIAN_ANGLE);
	}

	@SuppressWarnings( { "cast", "unchecked", "rawtypes" })
	@Override
	protected void setValidAcceptableUnits() {
		validAcceptableUnits = new ArrayList<Unit<? extends Quantity>>();

		// Leave these 'unnecessary' casts alone!! - see bug #634
		validAcceptableUnits.add((Unit) NonSIext.mDEG_ANGLE.getBaseUnits());
		validAcceptableUnits.add((Unit) NonSIext.DEG_ANGLE.getBaseUnits());

		validAcceptableUnits.add((Unit) SI.RADIAN.getBaseUnits());
		// validAcceptableUnits.add((Unit)
		// SI.MILLI(SI.RADIAN).alternate("mRad"));
		// validAcceptableUnits.add((Unit)
		// SI.MICRO(SI.RADIAN).alternate("uRad"));
		// validAcceptableUnits.add((Unit)
		// SI.MICRO(SI.RADIAN).alternate("µRad"));

		validAcceptableUnits.add((Unit) NonSIext.mRADIAN_ANGLE.getBaseUnits());
		validAcceptableUnits.add((Unit) NonSIext.μRADIAN_ANGLE.getBaseUnits());
	}

	@Override
	public void setSpeed(Quantity speed) throws MoveableException {
		moveables[0].setSpeed(speed);
	}

	@Override
	public Quantity getSpeed() {
		return moveables[0].getSpeed();
	}

	@Override
	public Quantity getSoftLimitLower() {
		return moveables[0].getSoftLimitLower().to(getReportingUnits());
	}

	@Override
	public Quantity getSoftLimitUpper() {
		return (moveables[0].getSoftLimitUpper().to(getReportingUnits()));
	}
}
