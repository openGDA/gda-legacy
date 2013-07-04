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

import gda.device.Monitor;
import gda.device.Scannable;
import gda.device.ScannableMotionUnits;
import gda.device.scannable.ScannableUtils;
import gda.factory.Finder;

import java.text.DecimalFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This bean displays the current value of a Monitor object.
 */
public class MonitorBean extends BeanBase {
	
	private static final Logger logger = LoggerFactory.getLogger(MonitorBean.class);

	private String monitorName = "";
	private Scannable monitor = null;

	/**
	 * Constructor.
	 */
	public MonitorBean() {
		getTxtValue().setEditable(false);
	}

	@Override
	public void startDisplay() {
		if (monitorName != null) {
			this.monitor = (Scannable) Finder.getInstance().find(monitorName);
			if (monitor != null) {
				monitor.addIObserver(this);
				if (!this.getLabel().equals("Name")) {
					getLblLabel().setText(monitor.getName());
				} else {
					getLblLabel().setText(getLabel());
				}
				initGUI();
				updateDisplay();
			}
			super.configure();
		} else {
			logger.error("DOFDisplayPanel cannot connect to DOF as information missing!");
		}
	}

	@Override
	protected boolean theObservableIsChanging() {
		return false;
	}

	@Override
	protected void txtValueActionPerformed() {
		// not relevent for this type of object
	}

	@Override
	protected void refreshValues() {
		synchronized (this) {
			try {
				if( monitor instanceof Monitor){
					unitsString = ((Monitor)monitor).getUnit();
				} else if ( monitor instanceof ScannableMotionUnits){
					unitsString = ((ScannableMotionUnits)monitor).getUserUnits();
				}
				
				tooltipString = monitor.getName();
				
				ScannableUtils.getCurrentPositionArray(monitor);
				Double currentPosition = ScannableUtils.getCurrentPositionArray(monitor)[0];
				try {
					if (isZeroSmallNumbers() && currentPosition < 0.0001) {
						DecimalFormat myFormat = new DecimalFormat();
						myFormat.applyPattern("#####.###");
						valueString = myFormat.format(currentPosition).trim();
					} else {
						valueString = String.format(getDisplayFormat(), currentPosition).trim();
					}
				} catch (java.util.IllegalFormatConversionException e) {
					// some parsing exception due to wrong display format, or possibly pv returning something which
					// cannot
					valueString = currentPosition.toString().trim();
				}
			} catch (Exception e) {
				logger.error("Exception in updateDisplay for " + monitorName + (e.getMessage() == null ? "" : " (" + e.getMessage() + ")") ,e);
			}
		}
	}

	@Override
	public void update(Object theObserved, Object changeCode) {
		// whatever the message, get the latest value from the monitor
		super.update(theObserved, changeCode);
	}

	/**
	 * @return the monitorName
	 */
	public String getMonitorName() {
		return monitorName;
	}

	/**
	 * @param monitorName
	 *            the monitorName to set
	 */
	public void setMonitorName(String monitorName) {
		this.monitorName = monitorName;
	}

}
