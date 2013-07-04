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

import java.awt.event.ActionEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.epicsdevice.EpicsCtrlEnum;
import gda.device.epicsdevice.IEpicsChannel;
import gda.device.epicsdevice.IEpicsDevice;
import gda.device.epicsdevice.ReturnType;
import gda.factory.Finder;
import gda.gui.epics.ActionEventRunner.EventDispatcher;

/**
 * ShutterActionHandler
 */
public class ShutterActionHandler implements ActionEventRunnerObserver {
	
	private static final Logger logger = LoggerFactory.getLogger(ShutterActionHandler.class);
	
	final private String shutterDeviceName;
	IEpicsChannel get_epicsChannel, set_epicsChannel;

	/**
	 * @param shutterDeviceName
	 */
	public ShutterActionHandler(String shutterDeviceName) {
		this.shutterDeviceName = shutterDeviceName;
	}

	private void configure() {
		if (set_epicsChannel == null || get_epicsChannel == null) {
			IEpicsDevice experimentEpicsDevice = (IEpicsDevice) Finder.getInstance().find(shutterDeviceName);
			if (experimentEpicsDevice == null)
				throw new IllegalArgumentException(" EnumHandle. unable to find device" + shutterDeviceName);
			if (set_epicsChannel == null)
				set_epicsChannel = experimentEpicsDevice.createEpicsChannel(ReturnType.DBR_CTRL, "CONTROL", "");
			if (get_epicsChannel == null)
				get_epicsChannel = experimentEpicsDevice.createEpicsChannel(ReturnType.DBR_CTRL, "STA", "");
		}
	}

	/**
	 * @return boolean
	 * @throws DeviceException
	 */
	public boolean isOpen() throws DeviceException {
		configure();
		String s = ((EpicsCtrlEnum) get_epicsChannel.getValue()).getValueAsString();
		return s.equals("Open");
	}

	/**
	 * @return boolean
	 * @throws DeviceException
	 */
	public boolean isClosed() throws DeviceException {
		configure();
		String s = ((EpicsCtrlEnum) get_epicsChannel.getValue()).getValueAsString();
		return s.equals("Closed");
	}

	private void waitForActionToComplete() {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException expt) {
		}
	}

	private void waitForState(boolean tobeOpen) throws DeviceException {
		int max_count = 10;
		int count = 0;
		while (count < max_count) {
			if (tobeOpen ? isOpen() : isClosed())
				return;
			count++;
			try {
				Thread.sleep(500);
			} catch (InterruptedException expt) {
			}
		}
		logger.error("Timeout waiting for shutter to " + (tobeOpen ? "open" : "close"));
	}

	/**
	 * @throws DeviceException
	 */
	public void open() throws DeviceException {
		configure();
		if (isOpen())
			return;
		set_epicsChannel.setValue("Reset");
		waitForActionToComplete();
		set_epicsChannel.setValue("Open");
		waitForState(true);
	}

	/**
	 * @throws DeviceException
	 */
	public void close() throws DeviceException {
		configure();
		set_epicsChannel.setValue("Close");
		waitForState(false);
	}

	@Override
	public void run(EventDispatcher dispatcher, ActionEvent e) throws DeviceException {
		configure();
		if (isOpen()) {
			close();
		} else {
			open();
		}
	}

	/**
	 * 
	 */
	public void dispose() {
		if (set_epicsChannel != null) {
			set_epicsChannel.dispose();
			set_epicsChannel = null;
		}
		if (get_epicsChannel != null) {
			get_epicsChannel.dispose();
			get_epicsChannel = null;
		}
	}

	@Override
	public void setChildRunner(ActionEventRunnerObserver otherRunner) {
		// no nothing
	}
}