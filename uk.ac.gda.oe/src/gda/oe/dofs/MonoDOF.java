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
import gda.jscience.physics.quantities.BraggAngle;
import gda.jscience.physics.quantities.PhotonEnergy;
import gda.jscience.physics.quantities.Wavelength;
import gda.jscience.physics.units.NonSIext;
import gda.oe.MoveableException;

import java.util.ArrayList;

import org.jscience.physics.quantities.Angle;
import org.jscience.physics.quantities.Energy;
import org.jscience.physics.quantities.Length;
import org.jscience.physics.quantities.Quantity;
import org.jscience.physics.units.NonSI;
import org.jscience.physics.units.SI;
import org.jscience.physics.units.Unit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A DOF responsible for changing the angle, wavelength or energy of a Monochromator.
 */

public class MonoDOF extends DOF {
	private Length twoDee;
	private static final Logger logger = LoggerFactory.getLogger(MonoDOF.class);
	private String crystalType = "unknown";

	private String[] knownCrystalTypes = { "Si311", "Si220", "Si111", "Si333" };

	private double[] twoDValues = { 3.275, 3.840, 6.2695, 2.0903 };

	/**
	 * Constructor.
	 */
	public MonoDOF() {
	}

	@Override
	public void configure() throws FactoryException {
		super.configure();
		updatePosition();
		setPositionValid(true);
		updateStatus();
	}

	/**
	 * @param twoD
	 *            2d expressed in Angstroms
	 */
	public void setTwoD(double twoD) {
		twoDee = Quantity.valueOf(twoD, NonSI.ANGSTROM);
	}

	private double twoDFromCrystalType(String crystalType) {
		double twoD = 0.0;

		for (int i = 0; i < knownCrystalTypes.length; i++) {
			String s = knownCrystalTypes[i];
			if (s.equals(crystalType)) {
				twoD = twoDValues[i];
				break;
			}
		}
		return twoD;
	}

	/**
	 * @param crystalType
	 */
	public void setCrystalType(String crystalType) {
		this.crystalType = crystalType;
		twoDee = Quantity.valueOf(twoDFromCrystalType(crystalType), NonSI.ANGSTROM);
	}

	/**
	 * @return twoD
	 */
	public double getTwoD() {
		return (twoDee != null) ? twoDee.to(NonSI.ANGSTROM).getAmount() : 0.0;
	}

	/**
	 * @return crystalType
	 */
	public String getCrystalType() {
		return crystalType;
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
	 * @return an Energy of the same numerical value and Units
	 */
	@Override
	protected Quantity checkTarget(Quantity newQuantity) {
		Angle angle = null;

		// for each of the allowed type of unit calculate the
		// equivalent Angle, if the unit type is not allowed then
		// null will be returned

		if (newQuantity instanceof Angle) {
			angle = (Angle) newQuantity;
		} else if (newQuantity instanceof Length) {
			angle = BraggAngle.braggAngleOf((Length) newQuantity, twoDee);
		} else if (newQuantity instanceof Energy) {
			angle = BraggAngle.braggAngleOf((Energy) newQuantity, twoDee);
		}
		return angle;
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
		if (q instanceof Angle) {
			position = getCurrentQuantity();
		} else if (q instanceof Length) {
			position = Wavelength.wavelengthOf((Angle) getCurrentQuantity(), twoDee);

		} else if (q instanceof Energy) {
			position = PhotonEnergy.photonEnergyOf((Angle) getCurrentQuantity(), twoDee);
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

		if (increment instanceof Angle) {
			// add increment to currentQuantity
			rtrn = getCurrentQuantity().plus(increment);
		} else if (increment instanceof Length) {
			// calculate the current wavelength and add the increment
			rtrn = Wavelength.wavelengthOf((Angle) getCurrentQuantity(), twoDee).plus(increment);
		} else if (increment instanceof Energy) {
			// calculate the current energy and add the increment
			rtrn = PhotonEnergy.photonEnergyOf((Angle) getCurrentQuantity(), twoDee).plus(increment);
		}

		// null will be returned if increment is not an Angle, Length or Energy
		logger.debug("MonoDOF addTo... increment " + increment);
		logger.debug("MonoDOF addTo... rtrn " + rtrn);
		return rtrn;
	}

	@Override
	protected void setDefaultAcceptableUnits() {
		defaultAcceptableUnits = new ArrayList<Unit<? extends Quantity>>();

		defaultAcceptableUnits.add(NonSIext.mDEG_ANGLE);
		defaultAcceptableUnits.add(NonSIext.DEG_ANGLE);
		defaultAcceptableUnits.add(NonSI.ANGSTROM);
		defaultAcceptableUnits.add(SI.NANO(SI.METER));
		defaultAcceptableUnits.add(NonSI.ELECTRON_VOLT);
		defaultAcceptableUnits.add(SI.KILO(NonSI.ELECTRON_VOLT));
	}

	@SuppressWarnings( { "cast", "unchecked", "rawtypes" })
	@Override
	protected void setValidAcceptableUnits() {
		validAcceptableUnits = new ArrayList<Unit<? extends Quantity>>();

		// Leave these 'unnecessary' casts alone!! - see bug #634
		validAcceptableUnits.add((Unit) NonSIext.mDEG_ANGLE.getBaseUnits());
		validAcceptableUnits.add((Unit) NonSIext.DEG_ANGLE.getBaseUnits());
		validAcceptableUnits.add((Unit) NonSI.ANGSTROM.getBaseUnits());
		validAcceptableUnits.add((Unit) SI.NANO(SI.METER).getBaseUnits());
		validAcceptableUnits.add((Unit) NonSI.ELECTRON_VOLT.getBaseUnits());
		validAcceptableUnits.add((Unit) SI.KILO(NonSI.ELECTRON_VOLT).getBaseUnits());
		validAcceptableUnits.add((Unit) NonSIext.DEG_ANGLE.getBaseUnits());
	}

	@Override
	public void setSpeed(Quantity start, Quantity end, Quantity time) throws MoveableException {
		logger.debug("MonoDOF setSpeed called with start, end, time: " + start + " " + end + " " + time);

		moveables[0].setSpeed(start, end, time);

	}

	@Override
	public Quantity getSpeed() {
		return moveables[0].getSpeed();
	}

	@Override
	public Quantity getSoftLimitLower() {
		return moveables[0].getSoftLimitLower();
	}

	@Override
	public Quantity getSoftLimitUpper() {
		return moveables[0].getSoftLimitUpper();
	}

	@Override
	public Object getAttribute(String name) throws MoveableException {
		Object attribute = null;
		if (name.equals("twoD")) {
			// Quantities seem not to be serializable so pass the double
			// value
			attribute = twoDee.to(NonSI.ANGSTROM).getAmount();
		} else {
			attribute = super.getAttribute(name);
		}
		return attribute;
	}
}