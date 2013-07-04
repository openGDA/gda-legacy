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

import gda.device.Motor;
import gda.factory.FactoryException;
import gda.oe.MoveableException;

import java.util.ArrayList;

import org.jscience.physics.quantities.Length;
import org.jscience.physics.quantities.Quantity;
import org.jscience.physics.units.ConversionException;
import org.jscience.physics.units.NonSI;
import org.jscience.physics.units.SI;
import org.jscience.physics.units.Unit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A DOF responsible for linear motion using one linear moveable. Specific to triax mono as it doesn't convert
 * quantities to mm.
 */
public class SingleAxisWavelengthDOF extends DOF {
	private static final Logger logger = LoggerFactory.getLogger(SingleAxisWavelengthDOF.class);

	/**
	 * Constructor
	 */
	public SingleAxisWavelengthDOF() {
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
	 * @return a Wavelength of the same numerical value and Units
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
	 * returns the home position distance/offset converted to reporting units
	 * 
	 * @return the offset
	 */
	@Override
	public Quantity getPositionOffset() {
		Length newValue = null;
		try {
			newValue = (Length) moveables[0].getPositionOffset();
		} catch (MoveableException doe) {
		}
		return newValue;
	}

	/**
	 * This update method was moved from Triax monochromator class when the castor xml branch work started. It is
	 * similar to the update mechanism in the scattered wavelength dof. I do not remember why this has to have a
	 * seperate update.
	 * 
	 * @param o
	 *            the calling IObservable
	 * @param arg
	 *            the argument it passes
	 */
	@Override
	public void update(Object o, Object arg) {
		super.update(o, arg);
		if (o instanceof Motor) {
			try {
				// FIXME: what on earth is this for?
				moveContinuously(1);
			} catch (Exception e) {
				logger.debug(e.getStackTrace().toString());
			}
			notifyIObservers(this, getStatus());
		}
	}

	@Override
	protected void setDefaultAcceptableUnits() {
		defaultAcceptableUnits = new ArrayList<Unit<? extends Quantity>>();

		defaultAcceptableUnits.add(NonSI.ANGSTROM);
		defaultAcceptableUnits.add(SI.NANO(SI.METER));
	}

	@SuppressWarnings( { "cast", "unchecked", "rawtypes" })
	@Override
	protected void setValidAcceptableUnits() {
		validAcceptableUnits = new ArrayList<Unit<? extends Quantity>>();

		// Leave these 'unnecessary' casts alone!! - see bug #634
		validAcceptableUnits.add((Unit) NonSI.ANGSTROM.getBaseUnits());
		validAcceptableUnits.add((Unit) SI.NANO(SI.METER).getBaseUnits());
	}

	@Override
	public Quantity getSoftLimitLower() {
		return moveables[0].getSoftLimitLower();
	}

	@Override
	public Quantity getSoftLimitUpper() {
		return moveables[0].getSoftLimitUpper();
	}
}
