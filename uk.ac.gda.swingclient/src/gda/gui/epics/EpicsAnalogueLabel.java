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

import gda.device.epicsdevice.EpicsDBR;
import gda.device.epicsdevice.EpicsGR;
import gda.device.epicsdevice.EpicsMonitorEvent;
import gda.device.epicsdevice.EpicsRegistrationRequest;
import gda.device.epicsdevice.ReturnType;

import javax.swing.JLabel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * EpicsAnalogueLabel Class
 */
public class EpicsAnalogueLabel implements EpicsMonitorListener {
	
	private static final Logger logger = LoggerFactory.getLogger(EpicsAnalogueLabel.class);
	
	private final JLabel lbl;

	private final String format;

	private final boolean showUnits;

	private EpicsMonitor _monitor;

	/**
	 * @param deviceName
	 * @param recordName
	 * @param fieldName
	 * @param lbl
	 * @param format
	 * @param showUnits
	 */
	public EpicsAnalogueLabel(String deviceName, String recordName, String fieldName, JLabel lbl, String format,
			boolean showUnits) {
		if (lbl == null) {
			throw new IllegalArgumentException("EpicsAnalogueLabel : label == null");
		}
		if (format == null) {
			throw new IllegalArgumentException("EpicsAnalogueLabel : format == null");
		}
		this.lbl = lbl;
		this.format = format;
		this.showUnits = showUnits;
		_monitor = new EpicsMonitor(ReturnType.DBR_CTRL, deviceName, recordName, fieldName, this);
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
	// logger.error("EpicsAnalogueLabel: " + excpt.getMessage());
	// }
	//			
	// }

	private void _update(EpicsDBR dbr) {
		String units = "";
		if (showUnits && dbr instanceof EpicsGR)
			units = ((EpicsGR) dbr)._unit;

		String valStr = null;

		int _count = dbr._count;
		if (_count != 1) {
			logger.error("EpicsAnalogueLabel : update - count!=0");
			return;
		}
		Object _value = dbr._value;
		if (_value instanceof String[]) {
			valStr = ((String[]) _value)[0];
		} else {
			double _val = 0.;
			if (_value instanceof double[] && _count == 1) {
				_val = ((double[]) _value)[0];
			} else if (_value instanceof float[] && _count == 1) {
				_val = ((float[]) _value)[0];
			} else if (_value instanceof int[] && _count == 1) {
				_val = ((int[]) _value)[0];
			} else if (_value instanceof byte[] && _count == 1) {
				_val = ((byte[]) _value)[0];
			} else if (_value instanceof short[] && _count == 1) {
				_val = ((short[]) _value)[0];
			} else {
				logger.error("EpicsAnalogueLabel : update - unsupported type " + _value.toString());
			}
			valStr = String.format(format, _val);
		}

		lbl.setText(valStr + ((units != null && !units.equals("")) ? units : ""));
	}

	@Override
	public void update(final EpicsMonitor monitor, final EpicsRegistrationRequest request, final EpicsMonitorEvent event) {
		if (event.epicsDbr instanceof EpicsDBR)
			_update((EpicsDBR) event.epicsDbr);
	}
}
