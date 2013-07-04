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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.epicsdevice.EpicsCtrlEnum;
import gda.device.epicsdevice.EpicsDBR;
import gda.device.epicsdevice.EpicsMonitorEvent;
import gda.device.epicsdevice.EpicsRegistrationRequest;
import gda.device.epicsdevice.ReturnType;
import gda.gui.epics.EpicsMonitor;
import gda.gui.epics.EpicsMonitorListener;

/**
 * EpicsParameterMonitor Class
 */
public class EpicsParameterMonitor implements EpicsMonitorListener {
	
	private static final Logger logger = LoggerFactory.getLogger(EpicsParameterMonitor.class);
	
	final private ParametersPanelBuilder builder;

	final private long limitedId;

	private EpicsMonitor _monitor;
	
	private DoubleConverter converter = null;
	
	private String deviceName;
	private String recordName;
	private String fieldName;

	/**
	 * @param deviceName
	 * @param recordName
	 * @param fieldName
	 * @param builder
	 * @param limitedId
	 */
	private EpicsParameterMonitor(String deviceName, String recordName, String fieldName,
			ParametersPanelBuilder builder, long limitedId, boolean autoStart) {
		this.builder = builder;
		this.limitedId = limitedId;
		if (builder == null) {
			throw new IllegalArgumentException("EpicsParameterMonitor: builder = null");
		}
		// disable the field until we get our first monitor event
		builder.setParameterConnectedState(limitedId, false);
		
		this.deviceName = deviceName;
		this.recordName = recordName;
		this.fieldName = fieldName;
		
		if (autoStart) {
			start();
		}
	}
	
	public void start() {
		_monitor = new EpicsMonitor(ReturnType.DBR_CTRL, deviceName, recordName, fieldName, this);
	}

	public EpicsParameterMonitor(String deviceName, String recordName, String fieldName, ParametersPanelBuilder builder, long limitedId) {
		this(deviceName, recordName, fieldName, builder, limitedId, true);
	}

	/**
	 * Creates an {@link EpicsParameterMonitor} but does not automatically start it; calling code must do this by calling {@link #start()}.
	 */
	public static EpicsParameterMonitor create(String deviceName, String recordName, String fieldName, ParametersPanelBuilder builder, long limitedId) {
		return new EpicsParameterMonitor(deviceName, recordName, fieldName, builder, limitedId, false);
	}
	
	/**
	 * 
	 */
	public void dispose() {
		_monitor.dispose();
		_monitor = null;
	}

	// public void monitorInitiated(EpicsMonitor monitor, Object val,
	// DeviceException excpt) {
	// if(val != null && val instanceof EpicsDBR)
	// _update((EpicsDBR)val);
	// else if( excpt != null){
	// logger.error("EpicsParameterMonitor: " + excpt.getMessage());
	// }
	//		
	// }
	
	private void _update(EpicsDBR dbr) {
		if (dbr instanceof EpicsCtrlEnum) {
			String s = ((EpicsCtrlEnum) dbr).getValueAsString();
			builder.setParameterConnectedState(limitedId, true);
			builder.setParameterFromMonitor(limitedId, s);
			return;
		}

		int _count = dbr._count;
		if (_count != 1) {
			logger.warn("EpicsParameterMonitor : update - count!=0. Are you trying to monitor a multi element record.");
			return;
		}
		Object _value = dbr._value;
		double _val = 0.;
		if (_value instanceof double[] && _count == 1) {
			_val = ((double[]) _value)[0];
			
			if (converter != null) {
				_val = converter.convertValue(_val);
			}
		} else if (_value instanceof float[] && _count == 1) {
			_val = ((float[]) _value)[0];
		} else if (_value instanceof int[] && _count == 1) {
			_val = ((int[]) _value)[0];
		} else if (_value instanceof byte[] && _count == 1) {
			_val = ((byte[]) _value)[0];
		} else if (_value instanceof short[] && _count == 1) {
			_val = ((short[]) _value)[0];
		} else if (_value instanceof Object[] && _count == 1) {
			if (((Object[]) _value)[0] instanceof Integer) {
				_val = (Integer) ((Object[]) _value)[0];
			} else if (((Object[]) _value)[0] instanceof Double) {
				_val = (Double) ((Object[]) _value)[0];
			} else {
				logger.error("EpicsParameterMonitor : update - unsupported type " + ((Object[]) _value)[0].getClass());
			}
		} else {
			logger.error("EpicsParameterMonitor : update - unsupported type " + _value.getClass());
		}
		builder.setParameterConnectedState(limitedId, true);
		builder.setParameterFromMonitor(limitedId, _val);

	}

	@Override
	public void update(EpicsMonitor monitor, EpicsRegistrationRequest request, EpicsMonitorEvent event) {
		if (event.epicsDbr instanceof EpicsDBR) {
			_update((EpicsDBR) event.epicsDbr);
		}
	}
	
	public void setConverter(DoubleConverter dc) {
		converter = dc;
	}
}
