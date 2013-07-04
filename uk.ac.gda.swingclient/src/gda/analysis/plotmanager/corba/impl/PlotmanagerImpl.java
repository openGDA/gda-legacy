/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

package gda.analysis.plotmanager.corba.impl;

import gda.analysis.plotmanager.IPlotManager;
import gda.analysis.plotmanager.corba.CorbaIPlotManagerPOA;
import gda.device.corba.CorbaDeviceException;
import gda.device.corba.impl.DeviceImpl;
import gda.factory.corba.CorbaFactoryException;

import org.omg.CORBA.Any;

/**
 * PlotmanagerImpl Class
 */
public class PlotmanagerImpl extends CorbaIPlotManagerPOA {

	private DeviceImpl deviceImpl;

	/**
	 * Create server side implementation to the CORBA package.
	 * 
	 * @param iPlotManager
	 * @param poa
	 *            the portable object adapter
	 */
	public PlotmanagerImpl(IPlotManager iPlotManager, org.omg.PortableServer.POA poa) {
		deviceImpl = new DeviceImpl(iPlotManager, poa);
	}

	/**
	 * @param panelName
	 * @param arg0
	 * @param arg1
	 * @throws CorbaDeviceException
	 */
	public void plot( @SuppressWarnings("unused")Any panelName, @SuppressWarnings("unused") Any arg0, @SuppressWarnings("unused") Any[] arg1) throws CorbaDeviceException {
		throw new CorbaDeviceException("Unsupported");

	}

	/**
	 * @param panelName
	 * @param arg0
	 * @param arg1
	 * @throws CorbaDeviceException
	 */
	public void plotOver( @SuppressWarnings("unused")Any panelName, @SuppressWarnings("unused") Any arg0, @SuppressWarnings("unused") Any[] arg1) throws CorbaDeviceException {
		throw new CorbaDeviceException("Unsupported");

	}

	/**
	 * @param panelName
	 * @param arg0
	 * @throws CorbaDeviceException
	 */
	public void plotImage(@SuppressWarnings("unused") Any panelName,@SuppressWarnings("unused")  Any[] arg0) throws CorbaDeviceException {
		throw new CorbaDeviceException("Unsupported");

	}

	/**
	 * @param panelName
	 * @param arg0
	 * @throws CorbaDeviceException
	 */
	
	public void plot3D(@SuppressWarnings("unused") Any panelName, @SuppressWarnings("unused") Any[] arg0) throws CorbaDeviceException {
		throw new CorbaDeviceException("Unsupported");
	}
	
	@Override
	public void plot(Any arg0, Any[] arg1) throws CorbaDeviceException {
		throw new CorbaDeviceException("Unsupported");

	}

	@Override
	public void plotImage(Any[] arg0) throws CorbaDeviceException {
		throw new CorbaDeviceException("Unsupported");

	}

	@Override
	public void plotOver(Any arg0, Any[] arg1) throws CorbaDeviceException {
		throw new CorbaDeviceException("Unsupported");

	}

	@Override
	public void setAttribute(String attributeName, Any value) throws CorbaDeviceException {
		deviceImpl.setAttribute(attributeName, value);
	}

	@Override
	public Any getAttribute(String attributeName) throws CorbaDeviceException {
		return deviceImpl.getAttribute(attributeName);
	}

	@Override
	public void reconfigure() throws CorbaFactoryException {
		deviceImpl.reconfigure();
	}

	@Override
	public void close() throws CorbaDeviceException {
		deviceImpl.close();
	}

	@Override
	public int getProtectionLevel() throws CorbaDeviceException {
		return deviceImpl.getProtectionLevel();
	}

	@Override
	public void setProtectionLevel(int newLevel) throws CorbaDeviceException {
		deviceImpl.setProtectionLevel(newLevel);
	}

}
