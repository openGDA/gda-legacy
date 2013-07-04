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

import gda.epics.CAClient;
import gda.epics.connection.EpicsController;
import gda.epics.connection.InitializationListener;
import gov.aps.jca.Channel;
import gov.aps.jca.event.MonitorEvent;
import gov.aps.jca.event.MonitorListener;

import java.text.DecimalFormat;

import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This code was edited or generated using CloudGarden's Jigloo SWT/Swing GUI Builder, which is free for non-commercial
 * use. If Jigloo is being used commercially (ie, by a corporation, company or business for any purpose whatever) then
 * you should purchase a license for each developer using Jigloo. Please visit www.cloudgarden.com for details. Use of
 * Jigloo implies acceptance of these licensing terms. A COMMERCIAL LICENSE HAS NOT BEEN PURCHASED FOR THIS MACHINE, SO
 * JIGLOO OR THIS CODE CANNOT BE USED LEGALLY FOR ANY CORPORATE OR COMMERCIAL PURPOSE.
 */
/**
 * Directly connects to a PV and displays it without any control. If it fails to see the PV then absorbs any exceptions
 * so that this may be used offline (but may result in a significant timeout delay).
 * <p>
 * You should call configure to connect to the pv and start the GUI. The GDA_StartClient script should also include the
 * -Dgov.aps.jca.JCALibrary.properties property so that communication with PVs is possible.
 */
public class PVBean extends BeanBase implements MonitorListener, InitializationListener, Runnable {
	
	private static final Logger logger = LoggerFactory.getLogger(PVBean.class);

	private volatile String newValue = null;

	private double zeroThreshold = 0.0001;
	private String pv = null;
	private String units = "";
	private String unitsPV = null;
	private String displayFormat = "%5.2g";

	private Channel theChannel;
	private EpicsController controller;

	private volatile boolean updateLock = false;

	/**
	 * Constructor.
	 */
	public PVBean() {
		super();
		initGUI();
		getTxtValue().setText("Not connected");
	}

	@Override
	public void startDisplay() {
		if (pv != null) {

			if (!getUnits().equals("")) {
				getLblUnits().setText(getUnits());
			}
			// connect to PV
			else if (unitsPV != null) {
				try {
					CAClient ca = new CAClient();
					String epicsUnits = ca.caget(unitsPV);
					getLblUnits().setText(epicsUnits);

				} catch (Exception e) {
					getLblUnits().setText("Exception!");
					logger.error(getName() + "  exception in startDisplay",e);
				}
			}

			try {
				controller = EpicsController.getInstance();
				theChannel = controller.createChannel(pv);
				controller.setMonitor(theChannel, this, EpicsController.MonitorType.STS);
				initGUI();
				updateDisplay();
				// super.configure();
			} catch (Exception e) {
				getTxtValue().setText("Exception!");
				logger.error(getName() + "  exception in startDisplay",e);
			}
		} else {
			getTxtValue().setText("not connected");
		}
	}

	@Override
	public void run() {
		updateDisplay();
	}

	@Override
	protected boolean theObservableIsChanging() {
		return false;
	}

	@Override
	protected void txtValueActionPerformed() {
		// don't want this for this type of bean
	}

	@Override
	protected void refreshValues() {
		// set the label
		this.getLblLabel().setText(getLabel());

		getValue();
		if (newValue != null) {
			try {
				// display the current value, assume its a number
				if (isZeroSmallNumbers() && Double.parseDouble(newValue) < zeroThreshold) {
					DecimalFormat myFormat = new DecimalFormat();
					myFormat.applyPattern("#####.####");
					String newText = myFormat.format(Double.parseDouble(newValue));
					valueString = newText.trim();
				} else {
					String displayValue = String.format(displayFormat, Double.parseDouble(newValue));
					valueString = displayValue.trim();
				}
			} catch (java.util.IllegalFormatConversionException e) {
				// some parsing exception due to wrong display format, or
				// possibly pv returning something which cannot
				// be converted into a number
				try {
					String displayValue = String.format(displayFormat, newValue);
					valueString = displayValue.trim();
				} catch (java.util.IllegalFormatConversionException e1) {
					// its really a problem, so simply give the pv string to the
					// display
					valueString = newValue;
				}
			}
			newValue = null;
		}
	}

	/**
	 * @return the pv
	 */
	public String getPv() {
		return pv;
	}

	/**
	 * @param pv
	 *            the pv to set
	 */
	public void setPv(String pv) {
		this.pv = pv;
	}

	/**
	 * @return the unitsPV
	 */
	public String getUnitsPV() {
		return unitsPV;
	}

	/**
	 * @param unitsPV
	 *            the unitsPV to set
	 */
	public void setUnitsPV(String unitsPV) {
		this.unitsPV = unitsPV;
	}

	/**
	 * @param units
	 *            the units to set
	 */
	public void setUnits(String units) {
		this.units = units;
	}

	/**
	 * @return the units
	 */
	public String getUnits() {
		return units;
	}

	/**
	 * @return the zeroThreshold
	 */
	public double getZeroThreshold() {
		return zeroThreshold;
	}

	/**
	 * @param zeroThreshold
	 *            the zeroThreshold to set
	 */
	public void setZeroThreshold(double zeroThreshold) {
		this.zeroThreshold = zeroThreshold;
	}

	
	/**
	 * @see gov.aps.jca.event.MonitorListener#monitorChanged(gov.aps.jca.event.MonitorEvent)
	 */
	@Override
	public void monitorChanged(MonitorEvent arg0) {
		getValue();
	}

	/**
	 * @see gda.epics.connection.InitializationListener#initializationCompleted()
	 */
	@Override
	public void initializationCompleted() {
		getValue();
	}

	private void getValue() {
		// only do this if the lock has been released since last time. Do not want other threads to wait here, but to
		// move on so no synchronisation.
		if (!updateLock ) {
			// grab the lock
			updateLock = true;
			// get the value
			try {
				newValue = controller.cagetString(theChannel);
			} catch (Exception e) {
				logger.warn("Exception while getting value of pv " + pv);
				updateLock = false;
			}
			// call this class's run method in the swing thread
			SwingUtilities.invokeLater(this);
		}
	}

}
