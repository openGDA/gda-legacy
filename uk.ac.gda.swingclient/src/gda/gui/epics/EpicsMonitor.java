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

import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import gda.device.DeviceException;
import gda.device.epicsdevice.*;
import gda.factory.Finder;
import gda.observable.IObserver;

/**
 * EpicsMonitor Class
 */
public class EpicsMonitor implements IObserver, Runnable {
	
	private static final Logger logger = LoggerFactory.getLogger(EpicsMonitor.class);
	
	private IEpicsChannel epicsChannel;
	private final EpicsMonitorListener observer;
	private final IEpicsDevice experimentEpicsDevice;
	private final ReturnType returnType;
	private final String recordName, fieldName;
	private boolean initialised = false;
	private final boolean callListenerOnSwingEventThread;

	/**
	 * @param returnType
	 * @param deviceName
	 * @param recordName
	 * @param fieldName
	 * @param observer
	 * @param callListenerOnSwingEventThread
	 */
	private EpicsMonitor(ReturnType returnType, String deviceName, String recordName, String fieldName,
			EpicsMonitorListener observer, boolean callListenerOnSwingEventThread, boolean autoStart) {
		if (observer == null)
			throw new IllegalArgumentException("Observer is null");
		this.returnType = returnType;
		this.recordName = recordName;
		this.fieldName = fieldName;
		this.observer = observer;
		this.callListenerOnSwingEventThread = callListenerOnSwingEventThread;
		experimentEpicsDevice = (IEpicsDevice) Finder.getInstance().find(deviceName);
		if (experimentEpicsDevice == null)
			throw new IllegalArgumentException("Cannot find device " + StringUtils.quote(deviceName));
		
		if (autoStart) {
			start();
		}
	}
	
	private boolean started;
	
	public void start() {
		if (!started) {
			Thread t = uk.ac.gda.util.ThreadManager.getThread(this);
			t.setPriority(java.lang.Thread.MIN_PRIORITY);
			t.start();
			started = true;
		}
	}

	public EpicsMonitor(ReturnType returnType, String deviceName, String recordName, String fieldName, EpicsMonitorListener observer, boolean callListenerOnSwingEventThread) {
		this(returnType, deviceName, recordName, fieldName, observer, callListenerOnSwingEventThread, true);
	}
	
	/**
	 * @param returnType
	 * @param deviceName
	 * @param recordName
	 * @param fieldName
	 * @param observer
	 */
	public EpicsMonitor(ReturnType returnType, String deviceName, String recordName, String fieldName,
			EpicsMonitorListener observer) {
		this(returnType, deviceName, recordName, fieldName, observer, true);
	}

	/**
	 * Creates an {@link EpicsMonitor} but does not automatically start it; calling code must do this by calling {@link #start()}.
	 */
	public static EpicsMonitor create(ReturnType returnType, String deviceName, String recordName, String fieldName, EpicsMonitorListener observer) {
		return new EpicsMonitor(returnType, deviceName, recordName, fieldName, observer, true, false);
	}
	
	/**
	 * @param theObserved
	 * @param changeCode
	 */
	public void updateSwingEventThread(Object theObserved, Object changeCode) {
		if (theObserved instanceof EpicsRegistrationRequest && changeCode instanceof EpicsMonitorEvent) {
			observer.update(this, (EpicsRegistrationRequest) theObserved, (EpicsMonitorEvent) changeCode);
			return;
		}
		logger.error("Unexpected - " + (theObserved != null ? theObserved.toString() : "None") + ":"
				+ (changeCode != null ? changeCode.toString() : "None"));
	}

	@Override
	public void update(final Object theObserved, final Object changeCode) {
		if (callListenerOnSwingEventThread) {
			final EpicsMonitor mon = this;
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					mon.updateSwingEventThread(theObserved, changeCode);
				}
			});
		} else {
			updateSwingEventThread(theObserved, changeCode);
		}
	}

	/**
	 * @return value
	 * @throws DeviceException
	 */
	public Object getValue() throws DeviceException {
		if (epicsChannel == null)
			throw new DeviceException("EpicsMonitor:" + recordName + fieldName + ". epicsChannel == null");
		return epicsChannel.getValue();
	}

	@Override
	public void run() {
		epicsChannel = experimentEpicsDevice.createEpicsChannel(returnType, recordName, fieldName);
		epicsChannel.addIObserver(this);
		initialised = true;
		// Object v = null;
		/*
		 * do not call monitorIniaited as the same functionality can be done by caller changing state on first update
		 */
		if (true)
			return;
		// /*
		// donot get value as this can hang up the device on the ObjectServer
		// -rather wait for first update
		// try{
		// v = epicsChannel.getValue();
		// }
		// catch(DeviceException e){
		// ;
		// }*/
		// final Object val = v;
		//		
		// final DeviceException e = null;
		//
		// if(callListenerOnSwingEventThread){
		// final EpicsMonitor mon = this;
		// SwingUtilities.invokeLater(new Runnable(){
		// public void run(){
		// observer.monitorInitiated(mon, val, e);
		// }
		// });
		//	    	
		// }
		// else{
		// observer.monitorInitiated(this, val, e);
		// }
	}

	/**
	 * 
	 */
	public void dispose() {
		if (initialised) {
			epicsChannel.dispose();
			initialised = false;
		}
	}
}