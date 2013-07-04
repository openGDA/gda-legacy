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

import gda.device.epicsdevice.EpicsCtrlEnum;
import gda.device.epicsdevice.EpicsMonitorEvent;
import gda.device.epicsdevice.EpicsRegistrationRequest;

import java.awt.Color;
import java.util.HashMap;

import javax.swing.JLabel;

/**
 * EpicsStringLabel
 */
public class EpicsStringLabel implements EpicsMonitorListener {
	private final JLabel lbl;
	private final HashMap<String, String> lblTextMap;
	final private HashMap<String, Color> colourMap;
	final private Color defColour;

	/**
	 * @param lbl
	 */
	public EpicsStringLabel(JLabel lbl) {
		this(lbl, null, null, null);
	}

	/**
	 * @param lbl
	 * @param lblTextMap
	 * @param colourMap
	 * @param defColour
	 */
	public EpicsStringLabel(JLabel lbl, HashMap<String, String> lblTextMap, HashMap<String, Color> colourMap,
			Color defColour) {
		if (lbl == null) {
			throw new IllegalArgumentException("SimpleLabelMonitor : label == null");
		}
		this.lbl = lbl;
		this.lblTextMap = lblTextMap;
		this.defColour = defColour;
		this.colourMap = colourMap;
	}

	// public void monitorInitiated(EpicsMonitor monitor, Object value,
	// DeviceException excpt) {
	// if(value != null && value instanceof EpicsCtrlEnum)
	// _update((EpicsCtrlEnum)value);
	// else if( excpt != null)
	// logger.error("EpicsStringLabel: " + excpt.getMessage());
	// }
	private void _update(EpicsCtrlEnum ctrlEnum) {
		String s = ctrlEnum.getValueAsString();
		String newLabel = s;
		if (lblTextMap != null) {
			newLabel = lblTextMap.get(s);
			if (newLabel == null)
				newLabel = s;
		}
		lbl.setText(newLabel);
		if (colourMap != null) {
			Color c = colourMap.get(s);
			if (c == null)
				c = defColour;
			if (c != null)
				lbl.setForeground(c);
		}
		lbl.repaint();
	}

	@Override
	public void update(EpicsMonitor monitor, EpicsRegistrationRequest request, EpicsMonitorEvent event) {
		if (event.epicsDbr instanceof EpicsCtrlEnum) {
			_update((EpicsCtrlEnum) event.epicsDbr);
		}
	}

}
