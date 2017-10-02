/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

import java.text.DecimalFormat;

import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.scannable.PVScannable;
import gda.device.scannable.ScannableStatus;
import gda.device.scannable.ScannableUtils;
import gda.factory.Finder;

/**
 * Displays and allows user to change the value of a single pv via control of a PVScannable object (or a Dummy Scannable
 * when offline).
 */
public class PVScannableBean extends BeanBase {

	private static final Logger logger = LoggerFactory.getLogger(PVScannableBean.class);

	private Scannable theScannable;

	private String scannableName;

	/**
	 * Constructor.
	 */
	public PVScannableBean() {
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

	@Override
	public void update(Object theObserved, Object changeCode) {
		if (theObserved instanceof Scannable && changeCode instanceof ScannableStatus) {
			super.update(theObserved, changeCode);
		}
	}

	@Override
	public void startDisplay() {
		if (scannableName != null) {
			this.theScannable = (Scannable) Finder.getInstance().find(scannableName);
			if (theScannable != null) {
				theScannable.addIObserver(this);
				manualUpdate = true;
			}
		} else {
			logger.error("DOFDisplayPanel cannot connect to DOF as information missing!");
		}
	}

	@Override
	protected boolean theObservableIsChanging() {
		try {
			return theScannable.isBusy();
		} catch (DeviceException e) {
			return false;
		}
	}

	@Override
	protected void txtValueActionPerformed() {
		try {
			theScannable.moveTo(NumberUtils.createDouble(getTxtNoCommas()));
		} catch (DeviceException e) {
			//
		}
	}

	@Override
	protected void refreshValues() {
		synchronized (this) {
			try {
				// get the units
				Object unit = theScannable.getAttribute(PVScannable.UNITSATTRIBUTE);
				if (unit != null) {
					unitsString = unit.toString();
				} else {
					unitsString = "";
				}

				// generate the tooltip
				tooltipString = theScannable.getName() + " ";

				// display the current value
				Double currentPosition = ScannableUtils.getCurrentPositionArray(theScannable)[0];
				if (isZeroSmallNumbers() && currentPosition < 0.0001) {
					DecimalFormat myFormat = new DecimalFormat();
					myFormat.applyPattern("#####.###");
					valueString = myFormat.format(currentPosition).trim();
				} else {
					valueString = String.format("%5.3g", currentPosition).trim();
				}
			} catch (DeviceException e) {
				logger.error("Exception while trying to move " + scannableName + ": " + e.getMessage());
			}
		}
	}

}
