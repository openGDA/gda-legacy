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
import gda.observable.IObserver;
import gda.oe.Moveable;
import gda.oe.MoveableStatus;
import gda.oe.MoveableException;
import gda.util.exceptionUtils;
import gda.util.converters.CoupledConverterHolder;
import gda.util.converters.IQuantitiesConverter;
import gda.util.converters.NullConverter;

import org.jscience.physics.quantities.Quantity;
import org.jscience.physics.units.Unit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * class to allow one DOF to control a number of other DOFs or moveables. If specified a conversion between the position
 * of this DOF to that of the child moveables/DOFs can be made. IF no conversion is specified then this DOF acts as a
 * fan-out. As CombinedDOF is itself a DOF one can build up a hierarchy of such objects to perform complex control over
 * any number of real moveables. For example 1 CombinedDOF could be used to fan-out positions to a number of other
 * CombinedDOFs with no conversion; and these other CombinedDOFs could pass the value onto a moveable having performed a
 * conversion specified in a lookup table or Java Expression Parser Expression. An example of such a hierarchy in a
 * Castor XML file is:
 * 
 * <pre>
 *  &lt;CombinedDOF&gt;
 *  &lt;name&gt;BeamLineEnergy_DCM_Roll&lt;/name&gt;
 *  &lt;protectionLevel&gt;0&lt;/protectionLevel&gt;
 *  &lt;moveableName&gt;DCM.DCM_Roll&lt;/moveableName&gt;
 *  &lt;converterName&gt;BeamLineEnergy_DCM_Roll_converter&lt;/converterName&gt;
 *  &lt;softLimitLow&gt;4.0&lt;/softLimitLow&gt;
 *  &lt;softLimitHigh&gt;25.0&lt;/softLimitHigh&gt;
 *  &lt;evalLowerLimit&gt;false&lt;/evalLowerLimit&gt;
 *  &lt;evalUpperLimit&gt;false&lt;/evalUpperLimit&gt;         
 *  &lt;/CombinedDOF&gt;
 *  &lt;CombinedDOF&gt;
 *  &lt;name&gt;BeamLineEnergy_Bragg_Degree&lt;/name&gt;
 *  &lt;protectionLevel&gt;0&lt;/protectionLevel&gt;
 *  &lt;moveableName&gt;DCM.DCM_Bragg&lt;/moveableName&gt;
 *  &lt;moveableName&gt;BeamLineEnergy_DCM_Perp&lt;/moveableName&gt;
 *  &lt;moveableToReport&gt;DCM_Bragg&lt;/moveableToReport&gt;
 *  &lt;softLimitLow&gt;4.0&lt;/softLimitLow&gt;
 *  &lt;softLimitHigh&gt;25.0&lt;/softLimitHigh&gt;
 *  &lt;evalLowerLimit&gt;true&lt;/evalLowerLimit&gt;
 *  &lt;evalUpperLimit&gt;true&lt;/evalUpperLimit&gt;
 *  &lt;/CombinedDOF&gt;
 *  &lt;CombinedDOF&gt;
 *  &lt;name&gt;BeamLineEnergy_DCM_Bragg_Angstrom&lt;/name&gt;
 *  &lt;protectionLevel&gt;0&lt;/protectionLevel&gt;
 *  &lt;moveableName&gt;BeamLineEnergy_Bragg_Degree&lt;/moveableName&gt;
 *  &lt;converterName&gt;BeamLineEnergy_DCM_Angstrom_To_Deg_converter&lt;/converterName&gt;
 *  &lt;softLimitLow&gt;.5&lt;/softLimitLow&gt;
 *  &lt;softLimitHigh&gt;2.5&lt;/softLimitHigh&gt;
 *  &lt;/CombinedDOF&gt;
 * </pre>
 * 
 * <p>
 * In this case BeamLineEnergy_DCM_Bragg_Angstrom drives BeamLineEnergy_Bragg_Degree using the conversion identified by
 * the name BeamLineEnergy_DCM_Angstrom_To_Deg_converter
 * <p>
 * BeamLineEnergy_DCM_Bragg_Angstrom has limits .5 to 2.5.
 * <p>
 * In turn BeamLineEnergy_Bragg_Degree drives the DOFs DCM.DCM_Bragg and BeamLineEnergy_DCM_Perp but with no conversion
 * so it is a simple fan-out.
 * <p>
 * In turn BeamLineEnergy_DCM_Roll drives DCM.DCM_Roll using the converter named BeamLineEnergy_DCM_Roll_converter. Note
 * that the limits for BeamLineEnergy_DCM_Roll are not to be calculated by converting the limits from DCM_Roll; this is
 * because the conversion table for DCM_Roll would give non-sensical values for BeamLineEnergy_DCM_Roll at the limits of
 * its movement.
 * <p>
 */
public final class CombinedDOF extends DOF {
	private static final Logger logger = LoggerFactory.getLogger(CombinedDOF.class);

	private double softLimitLow = -1.0 * Double.MAX_VALUE;

	private double softLimitHigh = Double.MAX_VALUE;

	private Quantity softLimitHighQuantity = null;

	private Quantity softLimitLowQuantity = null;

	private String moveablesOrderString = null;

	private Moveable[] conversionOrderedMoveables = null;

	private boolean evalLowerLimit = true;

	private boolean evalUpperLimit = true;

	private int moveableToReport = 0;

	private String moveableToReportString = null;

	private boolean configuredCombinedDOF = false;

	private IQuantitiesConverter quantitiesConverter = null;

	private String converterName = null;
	private boolean updatePositionFromMoveables = true; // useful if the back
	// conversion is not
	// possible.
	private Quantity lastPositionUsedToCalcMoveables = null, lastMovedToPosition = null;

	/**
	 * Sets the quantities converter used by this DOF.
	 * 
	 * @param quantitiesConverter the quantities converter
	 */
	public void setQuantitiesConverter(IQuantitiesConverter quantitiesConverter) {
		this.quantitiesConverter = quantitiesConverter;
	}
	
	/**
	 * @param converterName
	 */
	public void setConverterName(String converterName) {
		if (converterName == null) {
			throw new IllegalArgumentException("CombinedDOF.setConverter() : Error converterName = null");
		}
		this.converterName = converterName;
	}

	/**
	 * @return converterName
	 */
	public String getConverterName() {
		return converterName;
	}

	/**
	 * @param moveablesOrderString
	 */
	public void setMoveablesOrderString(String moveablesOrderString) {
		if (moveablesOrderString == null) {
			throw new IllegalArgumentException(
					"CombinedDOF.setMoveablesOrderString() : Error moveablesOrderString = null");
		}
		this.moveablesOrderString = moveablesOrderString;
	}

	/**
	 * @return moveablesOrderString
	 */
	public String getMoveablesOrderString() {
		return moveablesOrderString;
	}

	/**
	 * @param moveableToReportString
	 */
	public void setMoveableToReport(String moveableToReportString) {
		if (moveableToReportString == null) {
			throw new IllegalArgumentException("CombinedDOF.setMoveableToReport() : Error moveableToReport = null");
		}
		this.moveableToReportString = moveableToReportString.trim();
	}

	/**
	 * @return moveableToReportString
	 */
	public String getMoveableToReport() {
		return moveableToReportString;
	}

	/**
	 * @return evalUpperLimit
	 */
	public boolean getEvalUpperLimit() {
		return evalUpperLimit;
	}

	/**
	 * @return evalLowerLimit
	 */
	public boolean getEvalLowerLimit() {
		return evalLowerLimit;
	}

	/**
	 * @param evalUpperLimit
	 */
	public void setEvalUpperLimit(boolean evalUpperLimit) {
		this.evalUpperLimit = evalUpperLimit;
	}

	/**
	 * @param evalLowerLimit
	 */
	public void setEvalLowerLimit(boolean evalLowerLimit) {
		this.evalLowerLimit = evalLowerLimit;
	}

	/**
	 * @param updatePositionFromMoveables
	 */
	public void setUpdatePositionFromMoveables(boolean updatePositionFromMoveables) {
		this.updatePositionFromMoveables = updatePositionFromMoveables;
	}

	/**
	 * @return boolean
	 */
	public boolean getUpdatePositionFromMoveables() {
		return updatePositionFromMoveables;
	}

	@Override
	protected Quantity[] calculateMoveables(Quantity q) {
		if (!configuredCombinedDOF) {
			throw new RuntimeException("CombinedDOF.calculateMoveables called before configuration complete");
		}

		if (moveablesOrderString != null) {
			throw new IllegalArgumentException(
					"CombinedDOF.configureConversionOrderedMoveables() - not fully implemented - I need to returned a re-ordered array");
		}

		Quantity sources[] = new Quantity[conversionOrderedMoveables.length];
		java.util.Arrays.fill(sources, q);
		try {
			Quantity[] calculatedMovables = getConverter().calculateMoveables(sources, conversionOrderedMoveables);
			// record position only if everything else is successful. This is
			// used to
			// update the position.
			lastPositionUsedToCalcMoveables = q;
			return calculatedMovables;
		} catch (Exception e) {
			exceptionUtils.logException(logger, "calculateMoveables", e);
		}
		return null;
	}

	@Override
	/**
	 * given a Quantity returns a new Quantity of the correct subclamoveablesss or null if the Units are incorrect. We
	 * do not check against limits here as that is done in ScannableBase at a higher level
	 * 
	 * @param newQuantity
	 *            the position with which to move to
	 * @return a quantity of the correct type
	 */
	protected Quantity checkTarget(Quantity newQuantity) {
		if (!configuredCombinedDOF) {
			throw new RuntimeException("CombinedDOF.UpdatePosition called before configuration complete");
		}

		// if unit of newQuantity is one of the acceptable units then OK
		boolean found = false;
		Unit<?> newUnit = newQuantity.getUnit();
		for (Unit<?> unit : validAcceptableUnits) {
			if (newUnit.equals(unit)) {
				found = true;
				break;
			}
		}
		if(!found && newUnit.isCompatible(validAcceptableUnits.get(0))) {
			newQuantity = newQuantity.to(validAcceptableUnits.get(0));
			found = true;
		
		}
		return found ? newQuantity : null;
	}

	@Override
	protected void setDefaultAcceptableUnits() {
		defaultAcceptableUnits = getConverter().getAcceptableUnits().get(0);
	}

	@Override
	protected void setValidAcceptableUnits() {
		validAcceptableUnits = getConverter().getAcceptableUnits().get(0);
	}

	@Override
	protected void updatePosition() {
		if (!configuredCombinedDOF) {
			logger.debug("CombinedDOF.UpdatePosition called before configuration complete");
			setPositionValid(false);
			return;
		}

		if (!updatePositionFromMoveables) {
			if (lastMovedToPosition != null) {
				setCurrentQuantity(lastMovedToPosition);
				setPositionValid(true);
			}
			return;
		}

		// TODO need to respond to when the values returned by
		// getConverter().ToSource are different
		int numMoveables = conversionOrderedMoveables.length;
		Quantity targets[] = new Quantity[numMoveables];
		for (int i = 0; i < numMoveables; i++) {
			targets[i] = conversionOrderedMoveables[i].getPosition();
		}
		Quantity newPosition = null;
		try {
			newPosition = getConverter().toSource(targets, conversionOrderedMoveables)[moveableToReport];
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		if (newPosition == null) {
			setCurrentQuantity(Quantity.valueOf(0.0, getConverter().getAcceptableUnits().get(0).get(0)));
			setPositionValid(false);
		} else if (Double.isInfinite(newPosition.doubleValue())) {
			logger.debug("CombinedDOF.UpdatePosition. NewValue for " + getName() + " is infinite. ");
			setCurrentQuantity(Quantity.valueOf(0.0, getConverter().getAcceptableUnits().get(0).get(0)));
			setPositionValid(false);
		} else {
			setCurrentQuantity(newPosition);
			setPositionValid(true);
		}
	}

	private String getMoveableNames(Moveable[] moveablesToList) {
		StringBuffer buffer = new StringBuffer();
		for (Moveable moveable : moveablesToList) {
			buffer.append(moveable.getName());
			buffer.append(",");
		}
		return buffer.toString();
	}

	private void configureConversionOrderedMoveables() {
		if (moveablesOrderString == null) {
			conversionOrderedMoveables = moveables;
		} else {
			Moveable[] moveablesToBeOrdered = moveables.clone();
			conversionOrderedMoveables = new Moveable[moveables.length];

			/*
			 * split string into moveable names and place into conversionOrderedMoveables in order
			 */
			String moveableNames[] = moveablesOrderString.split(",");
			if (moveableNames.length != moveables.length) {
				throw new IllegalArgumentException(
						"CombinedDOF.configureConversionOrderedMoveables() : number of moveables in moveablesOrderString ("
								+ moveablesOrderString + ") does not match the number of moveables ("
								+ getMoveableNames(moveables) + ")");
			}
			for (int i = 0; i < moveableNames.length; i++) {
				String moveableName = moveableNames[i].trim();
				boolean found = false;
				for (int j = 0; j < moveablesToBeOrdered.length; j++) {
					if (moveablesToBeOrdered[j] != null) {
						if (moveableName.equals(moveablesToBeOrdered[j].getName())) {
							conversionOrderedMoveables[i] = moveablesToBeOrdered[j];
							moveablesToBeOrdered[j] = null; // remove so not
							// used again
							found = true;
						}
					}
				}
				if (!found) {
					throw new IllegalArgumentException(
							"CombinedDOF.configureConversionOrderedMoveables() : Unable to find moveable - "
									+ moveableName + " in list of moveables (" + getMoveableNames(moveables) + ")");
				}
			}
			for (int j = 0; j < moveablesToBeOrdered.length; j++) {
				if (moveablesToBeOrdered[j] != null) {
					throw new IllegalArgumentException(
							"CombinedDOF.configureConversionOrderedMoveables() : moveablesOrderString ("
									+ moveablesOrderString + ") does not contain an entry for ("
									+ moveablesToBeOrdered[j].getName() + ")");
				}
			}

		}
	}

	@Override
	public void configure() throws FactoryException {
		if (!configuredCombinedDOF) {
			if (quantitiesConverter == null && converterName != null) {
				quantitiesConverter = CoupledConverterHolder.FindQuantitiesConverter(converterName);
			}
			super.configure();

			configureConversionOrderedMoveables();
			// configure moveableToReport from moveableToReportString
			if (moveableToReportString == null) {
				moveableToReport = 0;
			} else {
				boolean found = false;
				for (int i = 0; i < conversionOrderedMoveables.length; i++) {
					if (moveableToReportString.equals(conversionOrderedMoveables[i].getName())) {
						found = true;
						moveableToReport = i;
					}
				}
				if (!found) {
					throw new IllegalArgumentException("CombinedDOF.configure() : Unable to find moveableToReport - "
							+ moveableToReportString + " in list of moveables (" + getMoveableNames(moveables) + ")");
				}
			}
			if (!updatePositionFromMoveables) {
				if (evalLowerLimit || evalUpperLimit) {
					throw new IllegalArgumentException(
							"CombinedDOF.configure() : updatePositionFromMoveables is false but evalLowerLimit or evalUpperLimit is true ");
				}
			}
			configuredCombinedDOF = true;
		}
	}

	@Override
	public Quantity getSoftLimitLower() {
		Quantity highest = getSoftLimitLowQuantity();
		if (evalLowerLimit) {
			int numMoveables = conversionOrderedMoveables.length;
			Quantity targets[] = new Quantity[numMoveables];
			for (int i = 0; i < numMoveables; i++) {
				targets[i] = getConverter().sourceMinIsTargetMax() ? conversionOrderedMoveables[i].getSoftLimitUpper()
						: conversionOrderedMoveables[i].getSoftLimitLower();
			}
			// return the highest of the low limits of the moveables and
			// this objects low limit
			try {
				Quantity[] SoftLimitLowers = getConverter().toSource(targets, conversionOrderedMoveables);
				for (Quantity q : SoftLimitLowers) {
					if (q.compareTo(highest) > 0) {
						highest = q;
					}
				}
			} catch (Exception e) {
				exceptionUtils.logException(logger, "getSoftLimitLower", e);
			}
		}
		return highest;
	}

	@Override
	public Quantity getSoftLimitUpper() {
		Quantity lowest = getSoftLimitHighQuantity();
		if (evalUpperLimit) {
			int numMoveables = conversionOrderedMoveables.length;
			Quantity targets[] = new Quantity[numMoveables];
			for (int i = 0; i < numMoveables; i++) {
				targets[i] = getConverter().sourceMinIsTargetMax() ? conversionOrderedMoveables[i].getSoftLimitLower()
						: conversionOrderedMoveables[i].getSoftLimitUpper();
			}

			// return the lowest of the high limits of the moveables and
			// this objects high limit
			try {
				Quantity[] SoftLimitUppers = getConverter().toSource(targets, conversionOrderedMoveables);
				for (Quantity q : SoftLimitUppers) {
					if (q.compareTo(lowest) < 0) {
						lowest = q;
					}
				}
			} catch (Exception e) {
				exceptionUtils.logException(logger, "getSoftLimitLower", e);
			}
		}
		return lowest;
	}

	/**
	 * Sets the lower soft limit (used by Castor to interpret XML file)
	 * 
	 * @param soft_LimitLow
	 *            the lower soft limit
	 */
	public void setSoftLimitLow(double soft_LimitLow) {
		this.softLimitLow = soft_LimitLow;
		softLimitLowQuantity = null; // clear so that we re-create it when we
		// need it using the correct converter
		// which may not exists at the moment
	}

	/**
	 * Gets the lower soft limit (used by Castor to interpret XML file)
	 * 
	 * @return the lower soft limit
	 */
	public double getSoftLimitLow() {
		return softLimitLow;
	}

	/**
	 * Sets the higher soft limit (used by Castor to interpret XML file)
	 * 
	 * @param soft_LimitHigh
	 *            the higher soft limit
	 */
	public void setSoftLimitHigh(double soft_LimitHigh) {
		this.softLimitHigh = soft_LimitHigh;
		softLimitHighQuantity = null; // clear so that we re-create it when we
		// need it using the correct converter
		// which may not exists at the moment
	}

	/**
	 * Gets the higher soft limit (used by Castor to interpret XML file)
	 * 
	 * @return the higher soft limit
	 */
	public double getSoftLimitHigh() {
		return softLimitHigh;
	}

	private IQuantitiesConverter getConverter() {
		if (quantitiesConverter == null) {
			quantitiesConverter = new NullConverter(moveables[0]);
			if (quantitiesConverter == null) {
				throw new RuntimeException("CombinedDOF.configure unable to create converter " + this.getName());
			}
		}
		return quantitiesConverter;
	}

	private Quantity getSoftLimitHighQuantity() {
		if (softLimitHighQuantity == null) {
			softLimitHighQuantity = Quantity.valueOf(softLimitHigh, getConverter().getAcceptableUnits().get(0).get(0));
		}
		return softLimitHighQuantity;
	}

	private Quantity getSoftLimitLowQuantity() {
		if (softLimitLowQuantity == null) {
			softLimitLowQuantity = Quantity.valueOf(softLimitLow, getConverter().getAcceptableUnits().get(0).get(0));
		}
		return softLimitLowQuantity;
	}

	/*
	 * moveEnabled allows a combinedDof to be prevented from driving the moveables
	 */
	private boolean moveEnabled = true;

	/**
	 * @return moveEnabled
	 */
	public boolean getMoveEnabled() {
		return moveEnabled;
	}

	/**
	 * @param moveEnabled
	 */
	public void setMoveEnabled(boolean moveEnabled) {
		this.moveEnabled = moveEnabled;
	}

	@Override
	public void doMove(Object mover, int id) throws MoveableException {
		if (moveEnabled) {
			super.doMove(mover, id);
		} else {
			// must do what super,doMove does wrt adding mover as an observer to
			// ensure the unLock takes place
			// and the DOFCommand completes properly
			if (statusCode != MoveableStatus.READY) {
				throw new MoveableException(new MoveableStatus(statusCode, getName(), getCurrentQuantity(), id),
						"doMove - invalid status = " + statusCode);
			}
			logger.warn("doMove for " + getName() + " ignored as moveEnabled = False");
			this.addIObserver((IObserver) mover);
			notifyIObservers(this, new MoveableStatus(MoveableStatus.READY, getName(), getCurrentQuantity(), id));
		}
		lastMovedToPosition = lastPositionUsedToCalcMoveables;
	}
}
