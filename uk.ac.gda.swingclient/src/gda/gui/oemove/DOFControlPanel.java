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
import gda.observable.IObservable;
import gda.observable.IObserver;
import gda.observable.ObservableComponent;
import gda.observable.UpdateDelayer;
import gda.oe.MoveableException;
import gda.oe.OE;
import gda.util.QuantityFactory;

import java.awt.Color;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import org.jscience.physics.quantities.Quantity;
import org.jscience.physics.units.Unit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class make available the controls for a selected DOF.
 */
public class DOFControlPanel extends JPanel implements IObserver, IObservable {
	
	private static final Logger logger = LoggerFactory.getLogger(DOFControlPanel.class);
	
	private ObservableComponent observableComponent = new ObservableComponent();

	// private OEControl oeControl;
	private DOFInputDisplay inputComponent;
	private DOFPositionDisplay positionComponent;
	private DOFModeDisplay modeComponent;
	private JComponent unitsComponent;
	private DOFSpeedLevel speedLevelComponent;
	private DOFStatusIndicator statusComponent;
	private OE oe;
	private String dofName;
	private int currentMode;

	/**
	 * This constructor creates a DOF Control for a particular oeControl
	 * 
	 * @param oe
	 *            the Optical Element or high level moveable object
	 * @param dofName
	 *            the name of Degree of Freedom or actual scannable component
	 */
	@SuppressWarnings("unused")
	public DOFControlPanel(OE oe, String dofName) {
		this.oe = oe;
		this.dofName = dofName;

		inputComponent = DOFDisplayComponentFactory.createInputDisplay(oe, dofName);
		positionComponent = DOFDisplayComponentFactory.createPositionDisplay(oe, dofName);
		modeComponent = DOFDisplayComponentFactory.createModeDisplay(oe, dofName);

		try {
			if (oe != null && oe.getAcceptableUnits(dofName).size() > 1) {
				unitsComponent = (JComponent) DOFDisplayComponentFactory.createUnitsDisplay(oe, dofName);
			} else {
				unitsComponent = new DOFUnitsDisplayRepeater(oe, dofName);
				unitsComponent.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Units",
						TitledBorder.CENTER, TitledBorder.TOP, null, Color.black));
			}
		} catch (MoveableException e) {
			logger.debug("DOFControlPanel: Error getting acceptableUnits for DOF " + dofName.toString());
		}

		speedLevelComponent = DOFDisplayComponentFactory.createSpeedLevelDisplay(oe, dofName);
		statusComponent = DOFDisplayComponentFactory.createStatusIndicator(oe, dofName);

		add((JComponent) positionComponent);
		add((JComponent) inputComponent);
		add((JComponent) modeComponent);
		add(unitsComponent);
		add((JComponent) speedLevelComponent);
		add((JComponent) statusComponent);

		// The update method causes changes to the GUI so use an
		// update delayer to observe the modeComponent & unitsComponent
		new UpdateDelayer(this, modeComponent);
		if (unitsComponent instanceof DOFUnitsDisplay)
			new UpdateDelayer(this, (DOFUnitsDisplay) unitsComponent);

		if (oe == null) {
			modeComponent.setEnabled(false);
			unitsComponent.setEnabled(false);
			speedLevelComponent.setEnabled(false);
			inputComponent.setEnabled(false);
		}
		currentMode = modeComponent.getMode();
	}

	/**
	 * Returns the speedLevel
	 * 
	 * @return the speedLevel
	 */
	public int getSpeedLevel() {
		return speedLevelComponent.getSpeedLevel();
	}

	/**
	 * @return the input component
	 */
	public DOFInputDisplay getDOFInputDisplay() {
		return inputComponent;
	}

	/**
	 * @return the input quantity
	 */
	public Quantity getInputQuantity() {
		Quantity q = null;
		Double d;
		Unit<? extends Quantity> units;

		if (unitsComponent instanceof JLabel)
			units = QuantityFactory.createUnitFromString(((JLabel) unitsComponent).getText());
		else
			units = ((DOFUnits) unitsComponent).getUnits();

		if ((d = inputComponent.getValue()) != null)
			q = Quantity.valueOf(d.doubleValue(), units);

		return q;
	}

	/**
	 * @return the current selected mode
	 */
	public int getMode() {
		return modeComponent.getMode();
	}

	@Override
	public void update(Object iObservable, Object changeCode) {
		if (iObservable instanceof DOFUnits) {
			DOFUnits du = (DOFUnits) iObservable;
			try {
				oe.setReportingUnits(dofName, du.getUnits());
				if (currentMode == DOFMode.HOME || currentMode == DOFMode.HOME_SET) {
					try {
						inputComponent.setValue(oe.formatPosition(dofName, oe.getHomeOffset(dofName).doubleValue()));
					} catch (MoveableException doe) {
						inputComponent.setValue("");
					}
				}
			} catch (MoveableException mex) {
				logger.error("Error setting reporting units");
			}
		} else if (iObservable instanceof DOFModeDisplay) {
			currentMode = ((DOFModeDisplay) iObservable).getMode();
			inputComponent.setMode(currentMode);
			observableComponent.notifyIObservers(this, iObservable);
		} else
			observableComponent.notifyIObservers(this, iObservable);
	}

	/**
	 * Refesh dof positions
	 */
	public void refresh() {
		observableComponent.notifyIObservers(this, modeComponent);
		positionComponent.refresh();
	}

	/**
	 * Set the ordering and numbers of dof modes
	 * 
	 * @param modeList
	 */
	public void setModeNames(ArrayList<String> modeList) {
		(modeComponent).setModeNames(modeList);
	}

	/**
	 * Set the ordering and number of dof speeds
	 * 
	 * @param speedList
	 */
	public void setSpeedNames(ArrayList<String> speedList) {
		(speedLevelComponent).setSpeedNames(speedList);
	}

	/**
	 * Set the default input value
	 * 
	 * @param inputValue
	 */
	public void setDefaultInputValue(double inputValue) {
		if (inputValue != 0.0)
			inputComponent.setValue(new Double(inputValue).toString());
	}

	@Override
	public void addIObserver(IObserver anIObserver) {
		observableComponent.addIObserver(anIObserver);
	}

	@Override
	public void deleteIObserver(IObserver anIObserver) {
		observableComponent.deleteIObserver(anIObserver);
	}

	@Override
	public void deleteIObservers() {
		observableComponent.deleteIObservers();
	}
}
