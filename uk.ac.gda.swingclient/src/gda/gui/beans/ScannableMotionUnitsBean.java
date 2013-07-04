/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council
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

package gda.gui.beans;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.ScannableMotion;
import gda.device.ScannableMotionUnits;
import gda.device.scannable.ScannablePositionChangeEvent;
import gda.device.scannable.ScannableStatus;
import gda.device.scannable.ScannableUtils;
import gda.factory.Finder;

import java.text.DecimalFormat;

import javax.swing.JOptionPane;

import org.apache.commons.lang.math.NumberUtils;
import org.python.modules.math;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This bean displays and allows users to operate a Scannable which uses units and represents a single number.
 * <p>
 * Once setup, configure should be called to find and connect to the Scannable
 */
public class ScannableMotionUnitsBean extends BeanBase {
	
	private static final Logger logger = LoggerFactory.getLogger(ScannableMotionUnitsBean.class);

	private Scannable theScannable;
	private String scannableName;



	/**
	 * Constructor.
	 */
	public ScannableMotionUnitsBean() {
		super();
		initGUI();
	}

	/**
	 * @return Returns the oeName.
	 */
	public String getScannableName() {
		return scannableName;
	}

	/**
	 * @param oeName
	 *            The oeName to set.
	 */
	public void setScannableName(String oeName) {
		this.scannableName = oeName;
	}
	
	public void setScannable(Scannable scannableItem) {
		this.theScannable = scannableItem;
	}

	@Override
	public void startDisplay() {
		if ((scannableName != null) & (theScannable == null)) {
			this.theScannable = (Scannable) Finder.getInstance().find(scannableName);
		}
		if (theScannable != null) {
			theScannable.addIObserver(this);
			manualUpdate = true;
		}
		//use scannableName in preference to theScannable.getName()
		if (getLabel().equals("Name")) {
			if (scannableName != null) {
				setLabel(scannableName);
				this.getLblLabel().setText(getLabel());
			}
			else if (theScannable != null) {
				setLabel(theScannable.getName());
				this.getLblLabel().setText(getLabel());
			}
		}
		super.configure();
		if (theScannable == null) {
			logger.error("DOFDisplayPanel cannot connect to DOF as information missing!");
		}
	}

	@Override
	public void update(Object theObserved, Object changeCode) {
		if (theObserved instanceof Scannable) {
			if (changeCode instanceof ScannableStatus || changeCode instanceof ScannablePositionChangeEvent) {
				super.update(theObserved, changeCode);
			}
		}
	}

	@Override
	protected synchronized void refreshValues() {
		try {
			// get the units
			unitsString = theScannable.getAttribute(ScannableMotionUnits.USERUNITS).toString();
			
			// generate the tooltip
			String toolTip = theScannable.getName();
			{
				Double[] limits = (Double[]) theScannable.getAttribute(ScannableMotion.FIRSTINPUTLIMITS);
				if (limits != null) {
					toolTip += " (" + limits[0] != null ? limits[0] : ""  + ", " + limits[1] != null ? limits[1] : "" + ")";
				}
			}
			tooltipString = toolTip;

			// display the current value
			Double currentPosition = ScannableUtils.getCurrentPositionArray(theScannable)[0];
			if (isZeroSmallNumbers() && ( math.fabs(currentPosition) < 0.0001) ) {
				DecimalFormat myFormat = new DecimalFormat();
				myFormat.applyPattern("#####.####");
				String newText = myFormat.format(currentPosition);
				valueString = newText.trim();
			} else {
				String newText = String.format(getDisplayFormat(), currentPosition);
				valueString = newText.trim();
			}
		} catch (DeviceException e) {
			logger.error("Exception while trying to update display " + scannableName + ": " + e.getMessage());
		}
	}

	@Override
	protected void txtValueActionPerformed() {
		try {
			theScannable.moveTo(NumberUtils.createDouble(getTxtNoCommas())); 
		} catch (Exception e) {
			logger.error("Exception while trying to move " + scannableName + ": " + e.getMessage());
			JOptionPane.showMessageDialog(getTopLevelAncestor(), scannableName + ": " + e.getMessage(),
					"Error Message", JOptionPane.ERROR_MESSAGE);
		}
	}

	@Override
	protected boolean theObservableIsChanging() {
		try {
			if(theScannable == null)
				return false;
			boolean busy = this.theScannable.isBusy();
			return busy;
		} catch (Throwable e) {
			final String name = (scannableName != null ? scannableName : "unknown");
			logger.error("ScannableMotionUnitBeam.isBusy exception for " + name, e);
			return false;
		}
	}
}
