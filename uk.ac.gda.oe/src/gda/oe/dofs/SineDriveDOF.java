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
import org.jscience.physics.units.SI;
import org.jscience.physics.units.Unit;

/**
 * A DOF responsible for SineDriveMonochromator movements
 */
public class SineDriveDOF extends DOF {
	private double sineArmLength;

	private double gratingDensity;

	private double correctionFactor = 1.0;

	private double factor;

	/**
	 * Constructor
	 */
	public SineDriveDOF() {
	}

	@Override
	public void configure() throws FactoryException {
		super.configure();

		updatePosition();
		setPositionValid(true);
		updateStatus();

		factor = (sineArmLength * gratingDensity) / (2 * Math.cos(Math.toRadians(45))) * correctionFactor;
	}

	/**
	 * @return Returns the correctionFactor.
	 */
	public double getCorrectionFactor() {
		return correctionFactor;
	}

	/**
	 * @param correctionFactor
	 *            The correctionFactor to set.
	 */
	public void setCorrectionFactor(double correctionFactor) {
		this.correctionFactor = correctionFactor;
	}

	/**
	 * @return Returns the gratingDensity.
	 */
	public double getGratingDensity() {
		return gratingDensity;
	}

	/**
	 * @param gratingDensity
	 *            The gratingDensity to set.
	 */
	public void setGratingDensity(double gratingDensity) {
		this.gratingDensity = gratingDensity;
	}

	/**
	 * @return Returns the sineArm.
	 */
	public double getSineArmLength() {
		return sineArmLength;
	}

	/**
	 * @param sineArmLength
	 *            The sine Arm length to set.
	 */
	public void setSineArmLength(double sineArmLength) {
		this.sineArmLength = sineArmLength;
	}

	@Override
	public void moveContinuously(int direction) throws MoveableException {
		if (moveables[0] != null)
			moveables[0].moveContinuously(direction);
	}

	/**
	 * updatePosition method called when observables (positioners) notify used to calculate currentQuantity from
	 * position of positioners
	 */
	@Override
	protected void updatePosition() {
		setCurrentQuantity(moveables[0].getPosition().divide(factor));
		setPositionValid(true);
	}

	@Override
	protected Quantity[] calculateMoveables(Quantity targetQuantity) {
		Quantity movement = targetQuantity.times(factor);
		Quantity rtn[] = { movement };
		return rtn;
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
		Length length = null;

		if (newQuantity instanceof Length) {
			length = (Length) newQuantity;
		} else if (newQuantity instanceof Energy) {
			length = Wavelength.wavelengthOf((Energy) newQuantity);
		}
		return length;
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
			// add increment to currentQuantity
			rtrn = getCurrentQuantity().plus(increment);
		} else if (increment instanceof Energy) {
			// calculate the current energy and add the increment
			rtrn = PhotonEnergy.photonEnergyOf((Length) getCurrentQuantity()).plus(increment);
		}
		return rtrn;
	}

	@Override
	protected void setDefaultAcceptableUnits() {
		defaultAcceptableUnits = new ArrayList<Unit<? extends Quantity>>();

		defaultAcceptableUnits.add(NonSI.ANGSTROM);
	}

	@SuppressWarnings( { "cast", "unchecked", "rawtypes" })
	@Override
	protected void setValidAcceptableUnits() {
		validAcceptableUnits = new ArrayList<Unit<? extends Quantity>>();

		// Leave these 'unnecessary' casts alone!! - see bug #634
		validAcceptableUnits.add((Unit) NonSI.ANGSTROM.getBaseUnits());
		validAcceptableUnits.add((Unit) SI.NANO(SI.METER).getBaseUnits());
		validAcceptableUnits.add((Unit) NonSI.ELECTRON_VOLT.getBaseUnits());
		validAcceptableUnits.add((Unit) SI.KILO(NonSI.ELECTRON_VOLT).getBaseUnits());
	}

	@Override
	public Quantity getSoftLimitLower() {
		return moveables[0].getSoftLimitLower().divide(factor);
	}

	@Override
	public Quantity getSoftLimitUpper() {
		return moveables[0].getSoftLimitUpper().divide(factor);
	}
}