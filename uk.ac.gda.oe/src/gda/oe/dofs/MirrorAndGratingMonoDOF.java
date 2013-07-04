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
 * High level DOF for setting wavelength or energy of the pre-mirrored grating monochromator on beamline 5u. Can handle
 * the three modes of operation FixedFocus, FixedGrating and FixedMirror
 */
public class MirrorAndGratingMonoDOF extends DOF {
	private static final Logger logger = LoggerFactory.getLogger(MirrorAndGratingMonoDOF.class);

	private int monoMode = 0;

	// Mono mode constants
	/**
	 * Mono mode constant
	 */
	public final static int FIXEDFOCUS = 0;

	/**
	 * Mono mode constant
	 */
	public final static int FIXEDGRATING = 1;

	/**
	 * Mono mode constant
	 */
	public final static int FIXEDMIRROR = 2;

	// Angular DOF identifiers
	private final int GRATING_ANGLE_DOF = 0;

	private final int MIRROR_ANGLE_DOF = 1;

	private Quantity gratingLineDensity;

	private Quantity kFF;

	private double calculatedKFF;

	// minEnergy & maxEnergy are the min and max beam energy in eV, read
	// from XML
	// and converted to wavelengths in updatePosition. Here are defaults :
	private double maxEnergy = 1000.0;

	private double minEnergy = 60.0;

	/**
	 * Constructor
	 */
	public MirrorAndGratingMonoDOF() {
	}

	@Override
	public void configure() throws FactoryException {
		super.configure();

		// these numbers are now obtained efrom XML fields minEnergy and
		// maxEnergy
		// minWavelength = Wavelength.wavelengthOf(Energy.valueOf(1000.0,
		// NonSI.ELECTRON_VOLT));
		// maxWavelength = Wavelength.wavelengthOf(Energy.valueOf(60.0,
		// NonSI.ELECTRON_VOLT));

		updatePosition();
		updateStatus();
	}

	/**
	 * @return Returns the gratingLines in metres.
	 */
	public double getGratingLinesPerM() {
		return (gratingLineDensity != null) ? gratingLineDensity.doubleValue() : 0.0;
	}

	/**
	 * @param gratingLines
	 *            The gratingLineDensity to set in metres
	 */
	public void setGratingLinesPerM(double gratingLines) {
		this.gratingLineDensity = Quantity.valueOf(1.0 / gratingLines, SI.METER).inverse();
	}

	/**
	 * @return Returns the kFF.
	 */
	public double getKFF() {
		return (kFF != null) ? kFF.doubleValue() : 0.0;
	}

	/**
	 * @param kff
	 *            The kFF to set.
	 */
	public void setKFF(double kff) {
		this.kFF = Quantity.valueOf(kff, Unit.ONE);
	}

	/**
	 * @return the maxeV in eV (and as specified in XML).
	 */
	public double getMaxEnergy() {
		return maxEnergy;
	}

	/**
	 * set the maxEnergy in eV as passed in (and as specified in XML).
	 * 
	 * @param maxEnergy
	 *            The maximum energy.
	 */
	public void setMaxEnergy(double maxEnergy) {
		this.maxEnergy = maxEnergy;
	}

	/**
	 * @return the minEnergy in eV (as specified in XML).
	 */
	public double getMinEnergy() {
		return minEnergy;
	}

	/**
	 * set the minEnergy in eV as passed in (and as specified in XML).
	 * 
	 * @param minEnergy
	 *            The minimum energy.
	 */
	public void setMinEnergy(double minEnergy) {
		this.minEnergy = minEnergy;
	}

	/**
	 * @return Returns the monoMode.
	 */
	public int getMonoMode() {
		return monoMode;
	}

	/**
	 * @param monoMode
	 *            The monoMode to set.
	 */
	public void setMonoMode(int monoMode) {
		this.monoMode = monoMode;
	}

	@Override
	public void moveContinuously(int direction) throws MoveableException {
	}

	/**
	 * Called (via the general update method in DOF) when Moveables notify.
	 */
	@Override
	protected void updatePosition() {
		Angle gamma = (Angle) moveables[MIRROR_ANGLE_DOF].getPosition();
		Angle beta = (Angle) moveables[GRATING_ANGLE_DOF].getPosition();
		Angle alpha = (Angle) gamma.times(2.0).minus(beta);

		calculatedKFF = beta.sine().divide(alpha.sine()).doubleValue();

		setCurrentQuantity((alpha.cos().minus(beta.cos())).abs().divide(gratingLineDensity));

		// If the wavelength comes out less than the minimum wavelength for
		// the grating then the position should be marked as invalid. The
		// system is allowed to move to such positions for setting up
		// purposes but it is not monochromatic light which comes out.
		Length minWavelength = Wavelength.wavelengthOf(Quantity.valueOf(maxEnergy, NonSI.ELECTRON_VOLT));
		Length maxWavelength = Wavelength.wavelengthOf(Quantity.valueOf(minEnergy, NonSI.ELECTRON_VOLT));
		if (getCurrentQuantity().compareTo(minWavelength) == -1 || getCurrentQuantity().compareTo(maxWavelength) == 1) {
			setPositionValid(false);
		} else {
			setPositionValid(true);
		}
		// FIXME Is this tolerance suitable? Ask Mark Roper
		if (monoMode == FIXEDFOCUS && Math.abs(calculatedKFF - kFF.doubleValue()) > 0.001) {
			setPositionValid(false);
			logger.debug("The calculated kFF value is " + calculatedKFF);
		}
	}

	/**
	 * Calculates the positions the moveables (Mirror and grating angles) need to move to in order set the required mono
	 * position for the given mono operation mode
	 * 
	 * @param fromQuantity
	 *            the Quantity for which moveable positions are to be calculated
	 * @return an array of Angle Quantities comprising the grating angle (beta) then the pre-mirror abgle (gamma)
	 */
	@Override
	protected Quantity[] calculateMoveables(Quantity fromQuantity) {
		Quantity angles[] = null;
		Length wavelength;
		if (fromQuantity instanceof Energy) {
			wavelength = Wavelength.wavelengthOf((Energy) fromQuantity);
		} else {
			wavelength = (Length) fromQuantity;
		}

		Angle beta = (Angle) moveables[GRATING_ANGLE_DOF].getPosition();
		Angle gamma = (Angle) moveables[MIRROR_ANGLE_DOF].getPosition();

		if (monoMode == FIXEDGRATING) {
			// This mode calculates a new value for gamma (PreMirrorAngle)
			// In Fixedgrating mode calculate cosAlpha from cosBeta and
			// d lambda, then calculate gamma from alpha and beta.
			double cosAlpha = beta.cos().plus((gratingLineDensity).times(wavelength)).doubleValue();
			if (cosAlpha >= -1.0 && cosAlpha <= 1.0) {
				Angle alpha = Quantity.valueOf(Math.acos(cosAlpha), SI.RADIAN);
				gamma = (Angle) alpha.plus(beta).divide(2.0);
			} else {
				logger.error("Error: FixedGrating is unable to calculate arccos");
				gamma = null;
			}
		} else if (monoMode == FIXEDMIRROR) {
			// This mode calculates a new value for beta (GratingAngle)
			// It does this by solving a quadratic for cos(beta) by the
			// traditional BFI method (-b + sqrt(b*b - 4ac)) / 2a. The
			// quadratic was obtained by starting from:
			// alpha = 2 * gamma - beta [1]
			// N * lambda = cos(alpha) - cos(beta) [2]
			// take cos of [1], expand using cos(a-b) relationship
			// and substitute in [2]. Rearrange to have:
			// sin(2 * gamma) * sin(beta) on one side and then square
			// substitute sin*sin = 1 - cos*cos and rearrange again to
			// get quadratic in cos(beta)

			Quantity dLambda = gratingLineDensity.times(wavelength);
			Angle twoGamma = (Angle) gamma.times(2.0);
			Quantity sinTwoGamma = twoGamma.sine();
			Quantity sinTwoGammaSquared = sinTwoGamma.times(sinTwoGamma);
			Quantity cosTwoGammaMinusOne = twoGamma.cos().minus(Quantity.valueOf(1.0, Unit.ONE));

			Quantity a = sinTwoGammaSquared.plus(cosTwoGammaMinusOne.times(cosTwoGammaMinusOne));
			Quantity b = dLambda.times(2.0).times(1.0 - twoGamma.cos().doubleValue());
			Quantity c = dLambda.times(dLambda).minus(sinTwoGammaSquared);
			Quantity cosBeta = (b.times(b).minus(a.times(c).times(4))).root(2).minus(b).divide(a.times(2));
			if (cosBeta.doubleValue() >= -1.0 && cosBeta.doubleValue() <= 1.0) {
				beta = Quantity.valueOf(Math.acos(cosBeta.doubleValue()), SI.RADIAN);
			} else {
				logger.error("Error: FixedMirror is unable to calculate arccos");
				beta = null;
			}
		} else if (monoMode == FIXEDFOCUS) {
			// This mode calculates new values for gamma (PreMirrorAngle)
			// and beta
			// (GratingAngle) based on the supplied value of kFF
			Quantity oneMinusKFFSquared = Quantity.valueOf(1.0, Unit.ONE).minus(kFF.times(kFF));
			Quantity dLamda = gratingLineDensity.times(wavelength);
			Quantity dLamdaSquared = dLamda.times(dLamda);
			Quantity partOne = dLamdaSquared.minus(oneMinusKFFSquared).times(oneMinusKFFSquared).times(4.0);
			Quantity partTwo = dLamdaSquared.times(4.0).minus(partOne);
			Quantity cosAlpha = dLamda.times(2.0).minus(partTwo.root(2)).divide(oneMinusKFFSquared.times(2.0));
			Angle alpha;
			double d = 0.0;
			if (cosAlpha.doubleValue() >= -1.0 && cosAlpha.doubleValue() <= 1.0) {
				alpha = Quantity.valueOf(Math.acos(cosAlpha.doubleValue()), SI.RADIAN);
				d = alpha.sine().times(kFF).doubleValue();
			} else {
				logger.error("Error: FixedFocus is unable to calculate arccos");
				alpha = null;
			}

			if (alpha != null && d >= -1.0 && d <= 1.0) {
				beta = Quantity.valueOf(Math.asin(d), SI.RADIAN);
				gamma = (Angle) alpha.plus(beta).divide(2.0);
			} else {
				logger.error("Error: FixedFocus is unable to calculate arcsin");
				beta = null;
				gamma = null;
			}

		}

		if (beta != null && gamma != null) {
			angles = new Quantity[2];
			angles[0] = beta;
			angles[1] = gamma;

			logger.debug("Calculated angle for " + ((DOF) moveables[0]).getName() + " " + angles[0]);
			logger.debug("Calculated angle for " + ((DOF) moveables[1]).getName() + " " + angles[1]);
		}
		return (angles);
	}

	/**
	 * given a Quantity checks whether its units are acceptable and if so constructs a new Quantity of the correct
	 * subclass for this DOF and returns it
	 * 
	 * @param newQuantity
	 *            the Quantity to be checked
	 * @return an Energy or length of the same numerical value and Units
	 */
	@Override
	protected Quantity checkTarget(Quantity newQuantity) {
		Length rtrn = null;
		if (newQuantity instanceof Length) {
			rtrn = (Length) newQuantity;
		}
		if (newQuantity instanceof Energy) {
			rtrn = Wavelength.wavelengthOf((Energy) newQuantity);
		}
		return rtrn;
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
	 * checks the sub-class of increment and adds it to the curent Quantity to produce a target Quantity, since this DOF
	 * allows movements to be specified in units which have a non-linear relationship to its Moveable's position this
	 * method has to be complicated
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

		// null will be returned if increment is not an Length or Energy
		logger.debug("checkAndAddIncrement returns " + rtrn);
		return rtrn;
	}

	@Override
	protected void setDefaultAcceptableUnits() {
		defaultAcceptableUnits = new ArrayList<Unit<? extends Quantity>>();

		defaultAcceptableUnits.add(NonSI.ELECTRON_VOLT);
		defaultAcceptableUnits.add(SI.NANO(SI.METER));
	}

	@SuppressWarnings( { "cast", "unchecked", "rawtypes" })
	@Override
	protected void setValidAcceptableUnits() {
		validAcceptableUnits = new ArrayList<Unit<? extends Quantity>>();

		// Leave these 'unnecessary' casts alone!! - see bug #634
		validAcceptableUnits.add((Unit) NonSI.ELECTRON_VOLT.getBaseUnits());
		validAcceptableUnits.add((Unit) SI.NANO(SI.METER).getBaseUnits());
	}

	@Override
	public Quantity getSoftLimitLower() {
		Angle gamma = (Angle) moveables[MIRROR_ANGLE_DOF].getSoftLimitLower();
		Angle beta = (Angle) moveables[GRATING_ANGLE_DOF].getSoftLimitLower();
		Angle alpha = (Angle) gamma.times(2.0).minus(beta);

		return (alpha.cos().minus(beta.cos())).abs().divide(gratingLineDensity);
	}

	@Override
	public Quantity getSoftLimitUpper() {
		Angle gamma = (Angle) moveables[MIRROR_ANGLE_DOF].getSoftLimitUpper();
		Angle beta = (Angle) moveables[GRATING_ANGLE_DOF].getSoftLimitUpper();
		Angle alpha = (Angle) gamma.times(2.0).minus(beta);

		return (alpha.cos().minus(beta.cos())).abs().divide(gratingLineDensity);
	}
}