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

import gda.device.epicsdevice.EpicsMonitorEvent;
import gda.device.epicsdevice.EpicsRegistrationRequest;

/**
 * EpicsMonitorListener
 */
public interface EpicsMonitorListener {
	/**
	 * @param monitor
	 * @param request
	 * @param event
	 */
	public void update(final EpicsMonitor monitor, final EpicsRegistrationRequest request, final EpicsMonitorEvent event);
	/*
	 * Do not callback once registration has completed as this is done in a different thread in the object server so
	 * there is no point
	 */
	// public void monitorInitiated(EpicsMonitor monitor, Object value,
	// DeviceException excpt); //do not call getvalue if in event thread
	// public void dispose();
}
