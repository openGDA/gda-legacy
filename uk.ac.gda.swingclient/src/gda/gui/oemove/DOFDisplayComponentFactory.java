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

package gda.gui.oemove;

import gda.gui.oemove.control.DOFMode;
import gda.gui.oemove.control.DOFUnits;
import gda.gui.oemove.control.DOFUnitsDisplayRepeater;
import gda.gui.oemove.control.DefaultDOFInputDisplay;
import gda.gui.oemove.control.DefaultDOFPositionDisplay;
import gda.gui.oemove.control.DefaultDOFStatusIndicator;
import gda.gui.oemove.control.DefaultSpeedLevel;
import gda.gui.oemove.control.DoNothingDOFMode;
import gda.gui.oemove.control.DoNothingDOFUnits;
import gda.gui.oemove.control.DoNothingSpeedLevel;
import gda.gui.oemove.control.HarmonicDOFInputDisplay;
import gda.gui.oemove.control.HarmonicDOFPositionDisplay;
import gda.gui.oemove.control.PolarizationDOFInputDisplay;
import gda.gui.oemove.control.PolarizationDOFPositionDisplay;
import gda.gui.oemove.control.UndulatorEnergyDOFPositionDisplay;
import gda.jscience.physics.units.NonSIext;
import gda.oe.MoveableException;
import gda.oe.OE;
import gda.oe.dofs.PolarizationValue;

import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.JLabel;

import org.jscience.physics.quantities.Quantity;
import org.jscience.physics.units.NonSI;
import org.jscience.physics.units.SI;
import org.jscience.physics.units.Unit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DOFDisplayComponentFactory Class
 */
public class DOFDisplayComponentFactory {
	private static final Logger logger = LoggerFactory.getLogger(DOFDisplayComponentFactory.class);

	/**
	 * @param oe
	 * @param dofName
	 * @return DOFInputDisplay
	 */
	public static DOFInputDisplay createInputDisplay(OE oe, String dofName) {
		DOFInputDisplay input = null;
		try {
			if (oe != null && dofName != null && oe.getDOFType(dofName).equals(DOFType.PolarizationDOF)) {
				input = new PolarizationDOFInputDisplay();
				// This sets the starting value of the menu to the current
				// position
				input.setValue(PolarizationValue.doubleToString(oe.getPosition(dofName).to(NonSI.DEGREE_ANGLE)
						.getAmount()));
			} else if (oe != null && dofName != null && oe.getDOFType(dofName).equals(DOFType.HarmonicDOF)) {
				input = new HarmonicDOFInputDisplay();
				// This sets the starting value of the menu to the current
				// position
				input.setValue(String.valueOf(oe.getPosition(dofName).intValue()));
			} else {
				input = new DefaultDOFInputDisplay(oe, dofName);
			}
		} catch (MoveableException e) {
			logger.error("Exception " + e);
		}
		return input;
	}

	/**
	 * @param oe
	 * @param dofName
	 * @param columns
	 * @param border
	 * @return DOFPositionDisplay
	 */
	public static DOFPositionDisplay createPositionDisplay(OE oe, String dofName, int columns, boolean border) {
		DOFPositionDisplay position = null;
		try {
			if (oe != null && dofName != null && oe.getDOFType(dofName).equals(DOFType.PolarizationDOF)) {
				position = new PolarizationDOFPositionDisplay(oe, dofName, columns, border);
			} else if (oe != null && dofName != null && oe.getDOFType(dofName).equals(DOFType.HarmonicDOF)) {
				position = new HarmonicDOFPositionDisplay(oe, dofName, columns, border);
			} else if (oe != null && dofName != null && oe.getDOFType(dofName).equals(DOFType.UndulatorEnergyDOF)) {
				position = new UndulatorEnergyDOFPositionDisplay(oe, dofName, columns, border);
			} else {
				position = new DefaultDOFPositionDisplay(oe, dofName, columns, border);
			}
		} catch (MoveableException e) {
			logger.error("Exception " + e);
		}
		return position;

	}

	/**
	 * @param oe
	 * @param dofName
	 * @return DOFPositionDisplay
	 */
	public static DOFPositionDisplay createPositionDisplay(OE oe, String dofName) {
		DOFPositionDisplay position = null;
		try {
			if (oe != null && dofName != null && oe.getDOFType(dofName).equals(DOFType.PolarizationDOF)) {
				position = new PolarizationDOFPositionDisplay(oe, dofName);
			} else if (oe != null && dofName != null && oe.getDOFType(dofName).equals(DOFType.HarmonicDOF)) {
				position = new HarmonicDOFPositionDisplay(oe, dofName);
			} else if (oe != null && dofName != null && oe.getDOFType(dofName).equals(DOFType.UndulatorEnergyDOF)) {
				position = new UndulatorEnergyDOFPositionDisplay(oe, dofName);
			} else {
				position = new DefaultDOFPositionDisplay(oe, dofName);
			}
		} catch (MoveableException e) {
			logger.error("Exception " + e);
		}
		return position;
	}

	/**
	 * @param oe
	 * @param dofName
	 * @return DOFSpeedLevel
	 */
	public static DOFSpeedLevel createSpeedLevelDisplay(OE oe, String dofName) {
		// The speedLevelComponent is either a DOFSpeedLevel or an empty label
		// The choice is based on the value of isSpeedLevelSettable
		DOFSpeedLevel dofSpeedLevel = null;
		try {
			if (oe != null && oe.isSpeedLevelSettable(dofName)) {
				dofSpeedLevel = new DefaultSpeedLevel();
			} else {
				dofSpeedLevel = new DoNothingSpeedLevel();
			}
		} catch (MoveableException e) {
			logger.error("Error getting isSpeedLevelSettable");
		}
		return dofSpeedLevel;
	}

	/**
	 * @param oe
	 * @param dofName
	 * @return DOFUnitsDisplay
	 */
	public static DOFUnitsDisplay createUnitsDisplay(OE oe, String dofName) {
		DOFUnitsDisplay unitsDisplay = null;
		ArrayList<Unit<? extends Quantity>> units = new ArrayList<Unit<? extends Quantity>>();
		try {
			if (oe != null) {
				if (oe.getDOFType(dofName).equals(DOFType.HarmonicDOF)) {
					unitsDisplay = new DoNothingDOFUnits(Unit.ONE);
				} else if (oe.getDOFType(dofName).equals(DOFType.PolarizationDOF)) {
					unitsDisplay = new DoNothingDOFUnits(NonSIext.DEG_ANGLE);
				} else {
					units = oe.getAcceptableUnits(dofName);
					unitsDisplay = new DOFUnits(units, oe.getReportingUnits(dofName));
				}
			} else {
				Unit<? extends Quantity> u = SI.MILLI(SI.METER);
				units.add(u);
				unitsDisplay = new DOFUnits(units, null);
			}
		} catch (MoveableException e) {
			logger.error("Error getting units display");
		}
		return unitsDisplay;
	}

	/**
	 * @param oe
	 * @param dofName
	 * @return JComponent
	 */
	public static JComponent createUnitsDisplayRepeater(OE oe, String dofName) {
		JComponent unitsDisplay = new JLabel("");
		try {
			if (oe.getDOFType(dofName).equals(DOFType.HarmonicDOF)) {
				unitsDisplay = new JLabel("");
			} else if (oe.getDOFType(dofName).equals(DOFType.PolarizationDOF)) {
				unitsDisplay = new JLabel("");
			} else {
				unitsDisplay = new DOFUnitsDisplayRepeater(oe, dofName);
			}
		} catch (MoveableException me) {
			logger.error("Exception " + me);
		}
		return unitsDisplay;

	}

	/**
	 * @param oe
	 * @param dofName
	 * @return DOFModeDisplay
	 */
	public static DOFModeDisplay createModeDisplay(OE oe, String dofName) {
		DOFModeDisplay modeDisplay = null;
		try {
			if (oe != null
					&& (oe.getDOFType(dofName).equals(DOFType.HarmonicDOF) || oe.getDOFType(dofName).equals(
							DOFType.PolarizationDOF))) {
				modeDisplay = new DoNothingDOFMode();
				modeDisplay.setMode(DOFMode.ABSOLUTE);
			} else {
				modeDisplay = new DOFMode();
			}
		} catch (MoveableException e) {
			logger.error("Exception " + e);
		}
		return modeDisplay;
	}

	/**
	 * @param oe
	 * @param dofName
	 * @return DOFStatusIndicator
	 */
	public static DOFStatusIndicator createStatusIndicator(OE oe, String dofName) {
		return new DefaultDOFStatusIndicator(oe, dofName);
	}
}
