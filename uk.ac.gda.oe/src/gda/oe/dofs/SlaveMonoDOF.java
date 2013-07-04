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
import gda.jscience.physics.quantities.PhotonEnergy;
import gda.jscience.physics.quantities.Wavelength;
import gda.oe.MoveableException;

import java.util.ArrayList;

import org.jscience.physics.quantities.Energy;
import org.jscience.physics.quantities.Length;
import org.jscience.physics.quantities.Quantity;
import org.jscience.physics.units.NonSI;
import org.jscience.physics.units.Unit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A DOF responsible for changing the wavelength or energy of a Slave Monochromator.
 * 
 */
// FIXME assumes lengths are Angstrom and Energies are eV, should do conversion really
public class SlaveMonoDOF extends DOF {
	private static final Logger logger = LoggerFactory.getLogger(SlaveMonoDOF.class);

	/**
	 * Constructor
	 */
	public SlaveMonoDOF() {
	}

	@Override
	public void configure() throws FactoryException {
		super.configure();
		updatePosition();
		setPositionValid(true);
		updateStatus();

		// dummy conversions to pre-load classes
		Wavelength.wavelengthOf(PhotonEnergy.photonEnergyOf((Length) getCurrentQuantity()));
	}

	/**
	 * updatePosition method called when observables (positioners) notify used to calculate currentQuantity from
	 * position of positioners
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
	 * @return a Length of the same numerical value and Units
	 */
	@Override
	protected Quantity checkTarget(Quantity newQuantity) {
		Quantity target = null;

		// for each of the allowed type of unit calculate the
		// equivalent Angle, if the unit type is not allowed then
		// null will be returned

		if (newQuantity instanceof Length) {
			target = newQuantity;
		} else if (newQuantity instanceof Energy) {
			target = Wavelength.wavelengthOf((Energy) newQuantity);
		}
		return target;
	}

	/**
	 * returns the currentQuantity converted into reportingUnits
	 * 
	 * @param reportingUnits
	 *            the reporting units
	 * @return a Quantity representing the current position in reportingUnits
	 */
	@Override
	public Quantity getPosition(Unit<? extends Quantity> reportingUnits) {
		Quantity q = Quantity.valueOf(1.0, reportingUnits);
		Quantity position = null;
		if (q instanceof Length) {
			position = getCurrentQuantity();

		} else if (q instanceof Energy) {
			position = PhotonEnergy.photonEnergyOf((Length) getCurrentQuantity());
		}
		return position;
	}

	/**
	 * Checks the sub-class of increment and adds it to the current Quantity to produce a target Quantity, since this
	 * DOF allows movements to be specified in units which have a non-linear relationship to its Moveable's position
	 * this method has to be complicated
	 * 
	 * @param increment
	 *            the Quantity to add
	 * @return the targetQuantity or null if increment is invalid
	 */
	@Override
	protected Quantity checkAndAddIncrement(Quantity increment) {
		Quantity rtrn = null;

		if (increment instanceof Length) {
			rtrn = getCurrentQuantity().plus(increment);
		} else if (increment instanceof Energy) {
			// calculate the current energy and add the increment
			rtrn = PhotonEnergy.photonEnergyOf((Length) getCurrentQuantity()).plus(increment);
		}

		// null will be returned if increment is not an Angle, Length or Energy
		logger.debug("SlaveMonoDOF addTo... increment " + increment);
		logger.debug("SlaveMonoDOF addTo... rtrn " + rtrn);
		return rtrn;
	}

	/**
	 * Determine if speedLevel can be set
	 * 
	 * @return true if the speed level is settable else false
	 */
	@Override
	public boolean isSpeedLevelSettable() {
		return false;
	}

	@Override
	protected void setDefaultAcceptableUnits() {
		defaultAcceptableUnits = new ArrayList<Unit<? extends Quantity>>();

		defaultAcceptableUnits.add(NonSI.ELECTRON_VOLT);
		defaultAcceptableUnits.add(NonSI.ANGSTROM);
	}

	@SuppressWarnings( { "cast", "unchecked", "rawtypes" })
	@Override
	protected void setValidAcceptableUnits() {
		validAcceptableUnits = new ArrayList<Unit<? extends Quantity>>();

		// Leave these 'unnecessary' casts alone!! - see bug #634
		validAcceptableUnits.add((Unit) NonSI.ELECTRON_VOLT.getBaseUnits());
		validAcceptableUnits.add((Unit) NonSI.ANGSTROM.getBaseUnits());
	}

	@Override
	public void setSpeed(Quantity start, Quantity end, Quantity time) throws MoveableException {
		logger.debug("SlaveMonoDOF setSpeed not available ");
	}

	@Override
	public Quantity getSpeed() {
		return null;
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
