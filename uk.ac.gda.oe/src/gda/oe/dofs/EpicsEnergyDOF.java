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
 * A DOF responsible for Energy motion using one Energy moveable.
 */
public class EpicsEnergyDOF extends DOF {
	private static final Logger logger = LoggerFactory.getLogger(EpicsEnergyDOF.class);

	/**
	 * Constructor
	 */
	public EpicsEnergyDOF() {
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
	 * @return a Energy of the same numerical value and Units
	 */
	@Override
	protected Quantity checkTarget(Quantity newQuantity) {
		Energy rtrn = null;
		try {
			rtrn = (Energy) newQuantity;
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
		Energy newValue = null;
		try {
			newValue = (Energy) moveables[0].getPositionOffset();
		} catch (MoveableException doe) {
			logger.debug(doe.getStackTrace().toString(), doe);
		}
		return newValue;
	}

	/**
	 * returns the home position distance/offset converted to reporting units
	 * 
	 * @return the offset
	 */
	@Override
	public Quantity getHomeOffset() {
		Energy newValue = null;
		try {
			newValue = (Energy) moveables[0].getHomeOffset().to(getReportingUnits());
		} catch (MoveableException e) {
			logger.debug(e.getStackTrace().toString());
		}
		return newValue;
	}

	/**
	 * set the default units to set acceptable units to if XML acceptable units are not valid
	 */
	@Override
	protected void setDefaultAcceptableUnits() {
		defaultAcceptableUnits = new ArrayList<Unit<? extends Quantity>>();
		defaultAcceptableUnits.add(NonSI.ELECTRON_VOLT);
		defaultAcceptableUnits.add(SI.KILO(NonSI.ELECTRON_VOLT));
	}

	/**
	 * set acceptable units that are valid and store as BaseUnit for XML checking
	 */
	@SuppressWarnings( { "cast", "unchecked", "rawtypes" })
	@Override
	protected void setValidAcceptableUnits() {
		validAcceptableUnits = new ArrayList<Unit<? extends Quantity>>();
		// Leave these 'unnecessary' casts alone!! - see bug #634
		validAcceptableUnits.add((Unit) NonSI.ELECTRON_VOLT.getBaseUnits());
		validAcceptableUnits.add((Unit) SI.KILO(NonSI.ELECTRON_VOLT.getBaseUnits()));
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
		return moveables[0].getSoftLimitUpper().to(getReportingUnits());
	}
}
