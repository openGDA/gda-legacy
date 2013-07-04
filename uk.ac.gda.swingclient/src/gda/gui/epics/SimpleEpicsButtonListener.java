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

package gda.gui.epics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.epicsdevice.IEpicsChannel;
import gda.device.epicsdevice.IEpicsDevice;
import gda.device.epicsdevice.ReturnType;
import gda.factory.Finder;

/**
 * SimpleEpicsButtonListener Class
 */
public class SimpleEpicsButtonListener implements java.awt.event.ActionListener {
	
	private static final Logger logger = LoggerFactory.getLogger(SimpleEpicsButtonListener.class);
	
	private final boolean testLayout;
	Object valueToSend;
	IEpicsChannel epicsChannel = null;
	final double timeoutInS = 5.0;

	/**
	 * @param testLayout
	 * @param returnType
	 * @param deviceName
	 * @param recordName
	 * @param fieldName
	 * @param valueToSend
	 */
	public SimpleEpicsButtonListener(boolean testLayout, ReturnType returnType, String deviceName, String recordName,
			String fieldName, Object valueToSend) {
		this.testLayout = testLayout;
		this.valueToSend = valueToSend;
		IEpicsDevice experimentEpicsDevice = (IEpicsDevice) Finder.getInstance().find(deviceName);
		if (experimentEpicsDevice == null)
			throw new IllegalArgumentException(" SimpleEpicsButton. unable to find device" + deviceName);
		epicsChannel = experimentEpicsDevice.createEpicsChannel(returnType, recordName, fieldName, 5.0);
	}

	/**
	 * @param valueToSend
	 */
	public void setValueToSend(Object valueToSend) {
		this.valueToSend = valueToSend;
	}

	@SuppressWarnings("unused")
	@Override
	public void actionPerformed(final java.awt.event.ActionEvent e) {
		if (testLayout)
			return;
		if (timeoutInS < 0.) {
			actionPerformedInNewThread();
			return;
		}
		java.awt.Component c = null;
		Object obj = e.getSource();
		if (obj instanceof java.awt.Component) {
			c = (java.awt.Component) obj;
		}
		if (c != null)
			c.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));
		Thread t = uk.ac.gda.util.ThreadManager.getThread(new Runnable() {
			@Override
			public void run() {
				actionPerformedInNewThread();
			}
		});
		t.setPriority(java.lang.Thread.MIN_PRIORITY);
		t.start();
		if (c != null)
			c.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));
	}

	/**
	 */
	public void actionPerformedInNewThread() {
		if (testLayout)
			return;
		try {
			if (epicsChannel != null) {
				epicsChannel.setValue(valueToSend);
			}
		} catch (DeviceException expt) {
			logger.error(expt.getMessage());
		} catch (RuntimeException expt) {
			logger.error(expt.getMessage());
		}
	}
}
