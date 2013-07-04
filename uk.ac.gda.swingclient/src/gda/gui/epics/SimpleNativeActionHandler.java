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

import gda.device.DeviceException;
import gda.device.epicsdevice.IEpicsChannel;
import gda.device.epicsdevice.IEpicsDevice;
import gda.device.epicsdevice.ReturnType;
import gda.factory.Finder;
import gda.gui.epics.ActionEventRunner.EventDispatcher;

import java.awt.event.ActionEvent;
import java.util.HashMap;

/**
 * SimpleNativeActionHandler Class
 */
public class SimpleNativeActionHandler implements ActionEventRunnerObserver {
	final private HashMap<String, String> commandMap;
	final private String defCommand;
	final private String recordName, fieldName, deviceName;
	private IEpicsChannel epicsChannel;

	/**
	 * @param deviceName
	 * @param recordName
	 * @param fieldName
	 * @param commandMap
	 * @param defCommand
	 */
	public SimpleNativeActionHandler(String deviceName, String recordName, String fieldName,
			HashMap<String, String> commandMap, String defCommand) {
		this.commandMap = commandMap;
		this.defCommand = defCommand;
		this.deviceName = deviceName;
		this.recordName = recordName;
		this.fieldName = fieldName;
	}

	@Override
	public void run(EventDispatcher dispatcher, ActionEvent e) throws DeviceException {
		if (epicsChannel == null) {
			IEpicsDevice experimentEpicsDevice = (IEpicsDevice) Finder.getInstance().find(deviceName);
			if (experimentEpicsDevice == null)
				throw new IllegalArgumentException(" EnumHandle. unable to find device" + deviceName);
			epicsChannel = experimentEpicsDevice.createEpicsChannel(ReturnType.DBR_NATIVE, recordName, fieldName);
		}
		if (epicsChannel != null) {
			Object o = epicsChannel.getValue();
			String s = o.toString();
			String command = commandMap.get(s);
			if (command == null)
				command = defCommand;
			epicsChannel.setValue(command);
		}
	}

	/**
	 * 
	 */
	public void dispose() {
		if (epicsChannel != null) {
			epicsChannel.dispose();
			epicsChannel = null;
		}
	}

	@Override
	public void setChildRunner(ActionEventRunnerObserver otherRunner) {
		// TODO Auto-generated method stubdo nothing
	}

}