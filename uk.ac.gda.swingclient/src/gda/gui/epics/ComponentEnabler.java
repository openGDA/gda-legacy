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
import gda.device.epicsdevice.EpicsDBR;
import gda.device.epicsdevice.EpicsMonitorEvent;
import gda.device.epicsdevice.EpicsRegistrationRequest;
import javax.swing.JComponent;
import java.util.*;

/**
 * ComponentEnabler Class
 */
public class ComponentEnabler {
	private final java.util.Vector<JComponent> components;
	final private Vector<Enabler> enablers;

	/**
	 * @param components
	 */
	public ComponentEnabler(java.util.Vector<JComponent> components) {
		this.components = new java.util.Vector<JComponent>();
		for (JComponent source : components) {
			this.components.add(source);
		}
		this.enablers = new Vector<Enabler>();
	}

	/**
	 * @param component
	 */
	public ComponentEnabler(JComponent component) {
		this.components = new java.util.Vector<JComponent>();
		this.components.add(component);
		this.enablers = new Vector<Enabler>();
	}

	/**
	 * @param reqdValue
	 * @return listener
	 */
	public EpicsMonitorListener makeListener(String reqdValue) {
		Enabler enabler = new Enabler(this, reqdValue);
		enablers.add(enabler);
		return enabler;
	}

	void enablerUpdate() {
		boolean enabled = true;
		for (Enabler enabler : enablers) {
			if (!enabler.enabled) {
				enabled = false;
				break;
			}
		}
		for (JComponent component : components) {
			component.setEnabled(enabled);
		}
	}
}

class Enabler implements EpicsMonitorListener {
	final String reqdValue;
	boolean enabled = false;
	private final ComponentEnabler parent;

	Enabler(ComponentEnabler parent, String reqdValue) {
		this.reqdValue = reqdValue;
		this.parent = parent;
	}

	@Override
	public void update(EpicsMonitor monitor, EpicsRegistrationRequest request, EpicsMonitorEvent event) {
		if (event.epicsDbr instanceof EpicsCtrlEnum) {
			_update((EpicsCtrlEnum) event.epicsDbr);
		} else if (event.epicsDbr instanceof EpicsDBR) {
			String s = ((EpicsDBR) event.epicsDbr)._toString();
			enabled = reqdValue.equals(s);
			parent.enablerUpdate();
		}
	}

	private void _update(EpicsCtrlEnum ctrlEnum) {
		String s = ctrlEnum.getValueAsString();
		enabled = reqdValue.equals(s);
		parent.enablerUpdate();
	}
}