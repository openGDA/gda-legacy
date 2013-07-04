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
import gda.analysis.plotmanager.corba.CorbaIPlotManager;
import gda.analysis.plotmanager.corba.CorbaIPlotManagerHelper;
import gda.device.DeviceException;
import gda.device.corba.CorbaDeviceException;
import gda.device.corba.impl.DeviceAdapter;
import gda.factory.FactoryException;
import gda.factory.Findable;
import gda.factory.corba.util.EventService;
import gda.factory.corba.util.EventSubscriber;
import gda.factory.corba.util.NameFilter;
import gda.factory.corba.util.NetService;
import gda.observable.IObserver;
import gda.util.LoggingConstants;

import org.omg.CORBA.Any;
import org.omg.CORBA.COMM_FAILURE;
import org.omg.CORBA.TRANSIENT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;

/**
 * PlotmanagerAdapter Class
 */
public class PlotmanagerAdapter extends DeviceAdapter implements IPlotManager, Findable, EventSubscriber {

	/**
	 * Setup the logging facilities
	 */
	private static final Logger logger = LoggerFactory.getLogger(PlotmanagerAdapter.class);

	CorbaIPlotManager iPlotManager;

	/**
	 * Create client side interface to the CORBA package.
	 * 
	 * @param obj
	 *            the CORBA object
	 * @param name
	 *            the name of the object
	 * @param netService
	 *            the CORBA naming service
	 */
	public PlotmanagerAdapter(org.omg.CORBA.Object obj, String name, NetService netService) {
		super(obj, name, netService);
		iPlotManager = CorbaIPlotManagerHelper.narrow(obj);
		this.netService = netService;

		this.name = name;

		EventService eventService = EventService.getInstance();
		if (eventService != null) {
			eventService.subscribe(this, new NameFilter(name, this.observableComponent));
		}

	}

	@Override
	public void plot(String panelName, DoubleDataset xAxis, DoubleDataset... dataSets) {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {

				Any any1 = org.omg.CORBA.ORB.init().create_any();

				logger.debug("Starting the any.insertvalue command");
				any1.insert_Value(xAxis);
				logger.debug("ending the any.insertvalue cammand");

				Any[] any2 = new Any[dataSets.length];
				for (int j = 0; j < any2.length; j++) {
					any2[j] = org.omg.CORBA.ORB.init().create_any();
					any2[j].insert_Value(dataSets[j]);
				}

				iPlotManager.plot(any1, any2);
				return;

			} catch (COMM_FAILURE cf) {
				iPlotManager = CorbaIPlotManagerHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				iPlotManager = CorbaIPlotManagerHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				// throw new IOException(ex.message);
			}
		}
		// throw new IOException("Communication failure: retry failed");

	}

	@Override
	public void plotImage(String panelName, DoubleDataset... dataSets) {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {

				Any[] any1 = new Any[dataSets.length];
				for (int j = 0; j < any1.length; j++) {
					any1[j] = org.omg.CORBA.ORB.init().create_any();
					any1[j].insert_Value(dataSets[j]);
				}

				iPlotManager.plotImage(any1);
				return;

			} catch (COMM_FAILURE cf) {
				iPlotManager = CorbaIPlotManagerHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				iPlotManager = CorbaIPlotManagerHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				// throw new IOException(ex.message);
			}
		}
	}

	// throw new IOException("Communication failure: retry failed");

	@Override
	public void plotOver(String panelName, DoubleDataset xAxis, DoubleDataset... dataSets) {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {

				Any any1 = org.omg.CORBA.ORB.init().create_any();
				any1.insert_Value(xAxis);

				Any[] any2 = new Any[dataSets.length];
				for (int j = 0; j < any2.length; j++) {
					any2[j] = org.omg.CORBA.ORB.init().create_any();
					any2[j].insert_Value(dataSets[j]);
				}
				iPlotManager.plotOver(any1, any2);
				return;

			} catch (COMM_FAILURE cf) {
				iPlotManager = CorbaIPlotManagerHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				iPlotManager = CorbaIPlotManagerHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				// throw new IOException(ex.message);
			}
		}
		// throw new IOException("Communication failure: retry failed");

	}

	// Any GetPlotPackage(key)
	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		// not setting here, as it could cause problems.
	}

	@Override
	public void inform(Object obj) {
		if (obj != null)
			logger.debug(LoggingConstants.FINEST, "DeviceAdapter: Received event for " + obj.getClass());
		else
			logger.debug(LoggingConstants.FINEST, "DeviceAdapter: Received event for NULL");
		notifyIObservers(this, obj);
		logger.debug(LoggingConstants.FINEST, "DeviceAdapter: Notified observers");
	}

	/**
	 * Notify observers of this class.
	 * 
	 * @param theObserved
	 *            the observed class
	 * @param changeCode
	 *            the changed code
	 */
	@Override
	public void notifyIObservers(java.lang.Object theObserved, java.lang.Object changeCode) {
		observableComponent.notifyIObservers(theObserved, changeCode);
	}

	@Override
	public void addIObserver(IObserver anIObserver) {
		observableComponent.addIObserver(anIObserver);
	}

	@Override
	public void deleteIObserver(IObserver anIObserver) {
		observableComponent.deleteIObserver(anIObserver);
	}

	@Override
	public void deleteIObservers() {
		observableComponent.deleteIObservers();
	}

	@Override
	public void configure() throws FactoryException {
		//
	}

	@Override
	public boolean isLocal() {
		return false;
	}

	@Override
	public void setLocal(boolean local) {
		// 
	}

	@Override
	public Object getAttribute(String attributeName) throws DeviceException {
		return super.getAttribute(attributeName);
	}

	@Override
	public void plot3D(String panelName, DoubleDataset... dataSets) {
		// Nothing to implement				
	}

	@Override
	public void plot3D(String panelName, boolean useWindow, DoubleDataset... dataSets) {
		// Nothing to implement		
	}

	@Override
	public void addPlot3D(String panelName, DoubleDataset... dataSets) {
		// Nothing to implement				
	}

	@Override
	public void plotImages(String panelName, DoubleDataset... dataSets) {
		// Nothing to implement
		
	}

	

}
