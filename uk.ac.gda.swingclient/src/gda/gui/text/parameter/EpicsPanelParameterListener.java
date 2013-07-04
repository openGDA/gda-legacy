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

package gda.gui.text.parameter;

import gda.device.DeviceException;
import gda.device.epicsdevice.IEpicsChannel;
import gda.device.epicsdevice.IEpicsDevice;
import gda.device.epicsdevice.ReturnType;
import gda.factory.Finder;
import gda.util.exceptionUtils;

import java.beans.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * EpicsPanelParameterListener Classs
 */
public class EpicsPanelParameterListener implements VetoableChangeListener {
	
	private static final Logger logger = LoggerFactory.getLogger(EpicsPanelParameterListener.class);
	
	private final IEpicsChannel epicsChannel;

	private Double lastSuccessfullySentValue = null;

	private final Double putTimeOut;
	
	private DoubleConverter converter = null;

	/**
	 * @param deviceName
	 * @param recordName
	 * @param fieldName
	 * @param putTimeOut
	 * @param initialValue
	 */
	public EpicsPanelParameterListener(String deviceName, String recordName, String fieldName, Double putTimeOut,
			double initialValue) {
		this.putTimeOut = putTimeOut;
		lastSuccessfullySentValue = initialValue;
		IEpicsDevice experimentEpicsDevice = (IEpicsDevice) Finder.getInstance().find(deviceName);
		if (experimentEpicsDevice == null)
			throw new IllegalArgumentException(" ParametersPanelListener. unable to find device " + StringUtils.quote(deviceName));
		// if putTimeout is not defined then use -1.0 to make epics device use
		// default
		// if put Timeout is null then run in a separate thread as the caller
		// does not care about the result
		epicsChannel = experimentEpicsDevice.createEpicsChannel(ReturnType.DBR_NATIVE, recordName, fieldName,
				putTimeOut != null ? putTimeOut : -1.0);
	}

	@Override
	public void vetoableChange(final PropertyChangeEvent e) throws PropertyVetoException {
		if (putTimeOut != null) {
			java.awt.Component c = null;
			Object obj = e.getSource();
			if (obj instanceof ParametersPanelBuilder.ParameterChangeEventSource) {
				Object field = ((ParametersPanelBuilder.ParameterChangeEventSource) obj).parameterField.field;
				if (field instanceof java.awt.Component) {
					c = (java.awt.Component) field;
				}
			}
			if (c != null)
				c.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));
			vetoableChangeinNewThread(e);
			if (c != null)
				c.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));
		} else {
			Thread t = uk.ac.gda.util.ThreadManager.getThread(new Runnable() {
				@Override
				public void run() {
					try {
						vetoableChangeinNewThread(e);
					} catch (Exception ex) {
						exceptionUtils.logException(logger, "vetoableChange", ex);
					}
				}
			});
			t.setPriority(java.lang.Thread.MIN_PRIORITY);
			t.start();
		}
	}

	/**
	 * @param e
	 * @throws PropertyVetoException
	 */
	public void vetoableChangeinNewThread(PropertyChangeEvent e) throws PropertyVetoException {
		Object source = e.getSource();
		if (source == null)
			throw new IllegalArgumentException("ParametersPanelListener.propertyChange - source == null ");

		if (source instanceof ParametersPanelBuilder.ParameterChangeEventSource) {

			Object newObject = e.getNewValue();
			if ((newObject == null) || !(newObject instanceof Double)) {
				throw new IllegalArgumentException(
						"ParametersPanelListener.propertyChange -  (newObject == null ) || !(newObject instanceof Limited) ");
			}

			if (epicsChannel != null) {
				try {
					Double newVal = (Double) newObject;
					
					if (converter != null)
						newVal = converter.convertValue(newVal);
					
					if (lastSuccessfullySentValue == null || (lastSuccessfullySentValue.compareTo(newVal) != 0)) {
						epicsChannel.setValue(newVal);
						lastSuccessfullySentValue = newVal;
					}
				} catch (DeviceException expt) {
					logger.error(expt.getMessage());
					throw new PropertyVetoException(expt.getMessage(), e);
				}
			} else {
				throw new IllegalArgumentException("EpicsPanelParameterListener: epicsChannel == null");
			}
		}
	}
	
	public void setConverter(DoubleConverter dc) {
		converter = dc;
	}
}
