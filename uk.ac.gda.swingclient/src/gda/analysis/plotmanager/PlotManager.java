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

package gda.analysis.plotmanager;

import gda.device.DeviceBase;
import gda.device.DeviceException;
import gda.factory.FactoryException;
import gda.factory.Findable;
import gda.factory.Finder;
import gda.observable.IObserver;
import gda.observable.ObservableComponent;
import gda.util.exceptionUtils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DoubleDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PlotManager Class
 */
public class PlotManager extends DeviceBase implements IPlotManager {

	/**
	 * Setup the logging facilities
	 */
	private static final Logger logger = LoggerFactory.getLogger(PlotManager.class);

	String finderName = "";

	private ObservableComponent observableComponent = new ObservableComponent();

	boolean transferInProgress = false;

	HashMap<String, PlotPackage> plots = new HashMap<String, PlotPackage>();

	HashMap<String, Long> time = new HashMap<String, Long>();
	
	protected static final Object PLOT_PACKAGES_LOCK = new Object();

	Vector<String> taggedForDeletion = new Vector<String>();

	Integer sendCount = 0;

	/**
	 *
	 */
	public PlotPackage buffer = null;

	/**
	 * @return instance of PlotManager
	 */
	public static PlotManager getInstance() {

		logger.debug("Entering the getInstance method");

		Findable object = Finder.getInstance().find("Plot_Manager");
		if (object == null || !(object instanceof PlotManager)) {
			throw new IllegalArgumentException(
					"cannot find the Plot_Manager object of type PlotManager, make sure it is"
							+ " added to the server XML (see 7.4 release notes)");
		}

		logger.debug("leaving the getInstance method");
		return (PlotManager) object;
	}

	/**
	 * @return IPlotManager
	 */
	public static IPlotManager getIPlotManager() {

		logger.debug("Entering the getIPlotManager method");

		Findable object = Finder.getInstance().find("Plot_Manager");
		if (object == null || !(object instanceof IPlotManager)) {
			throw new IllegalArgumentException(
					"cannot find the Plot_Manager object ot type IPlotManager, make sure it is"
							+ " added to the server XML (see 7.4 release notes)");
		}
		logger.debug("leaving the getIplotManager method");
		return (IPlotManager) object;
	}

	@Override
	public String getName() {
		return finderName;
	}

	@Override
	public void setName(String name) {
		finderName = name;
	}

	private void addPlotPackage(String plotPackageKey, long timestamp, PlotPackage plotPackage) {
		synchronized (PLOT_PACKAGES_LOCK) {
			time.put(plotPackageKey, timestamp);
			plots.put(plotPackageKey, plotPackage);
		}
	}
	
	@Override
	public void plot(String panelName, Dataset xAxis, Dataset... datasets) {

		logger.debug("Entering the  plot(String panelName, DataSet xAxis, DataSet... dataSets) Method");
		logger.debug("The panelname to plot is '" + panelName + "'");
		// generate the appropriate PlotPackage to pass across
		PlotPackage pp = new PlotPackage();
		pp.setPlotPanelName(panelName);
		int[] tags = new int[datasets.length];
		for (int i = 0; i < tags.length; i++) {
			tags[i] = i;
		}
		//pp.setXYUpdatablePlot(xAxis, dataSets, tags);
		pp.setXYPlot(xAxis, datasets);
		// add to map and get key back
		long key = System.currentTimeMillis();
		PlotPasser send = new PlotPasser(key + panelName, panelName);
		logger.debug("plot package key : " + send.getKey());
		addPlotPackage(send.getKey(), key, pp);
		notifyIObservers(this, send);
		logger.debug("Leaving the plot method");
	}

	@Override
	public void plotOver(String panelName, Dataset xAxis, Dataset... dataSets) {
		// generate the appropriate PlotPackage to pass across
		logger.debug("Entering the plotover method");
		PlotPackage pp = new PlotPackage();
		pp.setPlotPanelName(panelName);
		int[] tags = new int[dataSets.length];
		for (int i = 0; i < tags.length; i++) {
			tags[i] = i;
		}
		//pp.setXYUpdatableOverPlot(xAxis, dataSets, tags);
		pp.setXYOverPlot(xAxis, dataSets);
		// add to map and get key back
		long key = System.currentTimeMillis();
		PlotPasser send = new PlotPasser(key + panelName, panelName);
		logger.debug("plot package key : " + send.getKey());
		addPlotPackage(send.getKey(), key, pp);
		notifyIObservers(this, send);
		logger.debug("leaving the plotover method");

	}

	/**
	 * @param panelName
	 * @param filename
	 */
	public void plot(String panelName, String filename) {
		logger.debug("Entering the  plot(String panelName, String filename) Method");
		logger.debug("The panelname to plot is '" + panelName + "'");
		logger.info("notify IObservers " + filename);
		notifyIObservers(this, filename);
		logger.debug("Leaving the plot method");

	}

	@Override
	public void plotImage(String panelName, Dataset... dataSets) {

		logger.debug("Entering the  plotImage(String panelName, DataSet... dataSets) Method for panel '" + panelName
				+ "'");

		// set up the plot package to deal with moving the data around
		PlotPackage pp = null;
		try {
			clearHashmap();
			pp = new PlotPackage();
			pp.setPlotPanelName(panelName);
			pp.setImagePlot(dataSets);
		} catch (OutOfMemoryError ex) {
			exceptionUtils.logException(logger, ex);
			clearHashmap(0);
			try {
				pp = new PlotPackage();
				pp.setPlotPanelName(panelName);
				pp.setImagePlot(dataSets);
			} catch (OutOfMemoryError ex1) {
				exceptionUtils.logException(logger, ex1);
			}
		}
		if (pp == null) {
			return;
		}
		// add to map and get key back
		long key = System.currentTimeMillis();
		PlotPasser send = new PlotPasser(key + panelName, panelName);
		logger.debug("plot package key : " + send.getKey());
		addPlotPackage(send.getKey(), key, pp);
		notifyIObservers(this, send);
		logger.debug("Leaving the plotImage method");
	}

	/**
	 * @see gda.analysis.plotmanager.IPlotManager#plot3D(String, DoubleDataset...)
	 */

	@Override
	public void plot3D(String panelName, Dataset... dataSets) {
		plot3D(panelName, false, dataSets);
	}

	/**
	 * @see gda.analysis.plotmanager.IPlotManager#plot3D(String, boolean, DoubleDataset...)
	 */
	@Override
	public void plot3D(String panelName, boolean useWindow, Dataset... dataSets) {
		PlotPackage pp = null;
		try {
			clearHashmap();
			pp = new PlotPackage();
			pp.setPlotPanelName(panelName);
			pp.setPlot3D(useWindow, dataSets);
		} catch (OutOfMemoryError ex) {
			exceptionUtils.logException(logger, ex);
			clearHashmap(0);
			try {
				pp = new PlotPackage();
				pp.setPlotPanelName(panelName);
				pp.setPlot3D(useWindow, dataSets);
			} catch (OutOfMemoryError ex1) {
				exceptionUtils.logException(logger, ex1);
			}
		}
		if (pp == null) {
			return;
		}
		// add to map and get key back
		PlotPasser send = new PlotPasser(sendCount.toString(), panelName);
		addPlotPackage(send.getKey(), System.currentTimeMillis(), pp);
		notifyIObservers(this, send);
		sendCount++;
	}

	@Override
	public void plotImages(String panelName, Dataset... dataSets)
	{
		PlotPackage pp = null;
		try {
			clearHashmap();
			pp = new PlotPackage();
			pp.setPlotPanelName(panelName);
			pp.setPlotImages(dataSets);
		} catch (OutOfMemoryError ex) {
			exceptionUtils.logException(logger, ex);
			clearHashmap(0);
			try {
				pp = new PlotPackage();
				pp.setPlotPanelName(panelName);
				pp.setPlotImages(dataSets);
			} catch (OutOfMemoryError ex1) {
				exceptionUtils.logException(logger, ex1);
			}
		}
		if (pp == null) {
			return;
		}
		// add to map and get key back
		PlotPasser send = new PlotPasser(sendCount.toString(), panelName);
		addPlotPackage(send.getKey(), System.currentTimeMillis(), pp);
		notifyIObservers(this, send);
		sendCount++;		
	}
	
	@Override
	public void addPlot3D(String panelName, Dataset... dataSets) {
		PlotPackage pp = null;
		try {
			clearHashmap();
			pp = new PlotPackage();
			pp.setPlotPanelName(panelName);
			pp.addPlot3D(dataSets);
		} catch (OutOfMemoryError ex) {
			exceptionUtils.logException(logger, ex);
			clearHashmap(0);
			try {
				pp = new PlotPackage();
				pp.setPlotPanelName(panelName);
				pp.addPlot3D(dataSets);
			} catch (OutOfMemoryError ex1) {
				exceptionUtils.logException(logger, ex1);
			}
		}
		if (pp == null) {
			return;
		}
		// add to map and get key back
		PlotPasser send = new PlotPasser(sendCount.toString(), panelName);
		addPlotPackage(send.getKey(), System.currentTimeMillis(), pp);
		notifyIObservers(this, send);
		sendCount++;
	}


	/**
	 * @param panelName
	 * @param filename
	 */
	@SuppressWarnings("unused")
	public void plotOver( String panelName, String filename) {

		notifyIObservers(this, filename);

	}

	/**
	 * @param panelName
	 * @param filename
	 */
	@SuppressWarnings("unused")
	public void plotImage( String panelName, String filename) {

		notifyIObservers(this, filename);

	}

	/**
	 * @param panelName
	 * @param lines
	 * @param xValues
	 * @param yValues
	 */
	public void updatablePlot(String panelName, int[] lines, DoubleDataset xValues, DoubleDataset[] yValues) {

		PlotPackage pp = new PlotPackage();
		pp.setPlotPanelName(panelName);
		pp.setXYUpdatablePlot(xValues, yValues, lines);
		notifyIObservers(this, pp);

	}

	/**
	 * @param panelName
	 * @param lines
	 * @param xValues
	 * @param yValues
	 */
	public void updatableOverPlot(String panelName, int[] lines, DoubleDataset xValues, DoubleDataset[] yValues) {

		PlotPackage pp = new PlotPackage();
		pp.setPlotPanelName(panelName);
		pp.setXYUpdatableOverPlot(xValues, yValues, lines);
		notifyIObservers(this, pp);

	}

	/**
	 * @param panelName
	 * @param lines
	 * @param xValue
	 * @param yValues
	 */
	public void addPlotPoints(String panelName, int[] lines, double xValue, double[] yValues) {

		PlotPackage pp = new PlotPackage();
		pp.setPlotPanelName(panelName);
		pp.setAddPoints(lines, xValue, yValues);
		notifyIObservers(this, pp);

	}

	// Code to deal with the buffering of things

	//TODO comment should be removed as this function is clearly not being used.
	
//	/**
//	 * This is a wrapper for the notifyIObservers function call which includes a single point buffering for large
//	 * dataset passing.
//	 *
//	 * @param pp
//	 *            The plot package to send to the client.
//	 */
//	private void bufferedNotify(PlotPackage pp) {
//
//		logger.debug("Start of bufferedNotify(PlotPackage pp) ");
//
//		// check to see if the transfer thread is busy.
//		if (transferInProgress) {
//
//			logger.debug("buffering");
//
//			// then place the plot package in the buffer
//			buffer = pp;
//
//		} else {
//
//			// fill the buffer
//			buffer = pp;
//
//			// now create a new thread to try to pass the package
//			BufferedThread thread = new BufferedThread(this);
//
//			// Start the thread
//			thread.start();
//
//			// now this is in motion just return
//
//		}
//
//		logger.debug("End of bufferedNotify(PlotPackage pp) ");
//
//	}

	class BufferedThread extends Thread {

		PlotManager parent = null;

		/**
		 * Basic thread for data passing functionality
		 *
		 * @param sender
		 */
		public BufferedThread(PlotManager sender) {
			parent = sender;
		}

		/**
		 * The run command that passes the data across.
		 */
		@Override
		public void run() {

			logger.debug("In the BufferedThread run method");

			// set the
			parent.transferInProgress = true;

			while (parent.buffer != null) {

				// needs to be a copy
				PlotPackage pp = (PlotPackage) parent.buffer.clone();

				// set the buffer to null so that it doesn't get repeated
				parent.buffer = null;

				// notify the observers with the copy
				notifyIObservers(parent, pp);

			}

			// set the flag to say that the transfer is over.
			parent.transferInProgress = false;

			logger.debug("leaving the BufferedThread run method");

		}

	}

	@Override
	public void configure() throws FactoryException {
	}

	@Override
	public boolean isLocal() {
		return false;
	}

	@Override
	public void setLocal(boolean local) {

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

	/**
	 * Notify all observers on the list of the requested change.
	 *
	 * @param theObserved
	 *            the observed component
	 * @param theArgument
	 *            the data to be sent to the observer.
	 */
	@Override
	public void notifyIObservers(Object theObserved, Object theArgument) {
		observableComponent.notifyIObservers(theObserved, theArgument);
	}

	/**
	 *
	 */
	public void testup() {
		notifyIObservers(this, "Hello again");
	}

	/**
	 * Proper corba methodology
	 *
	 * @param attributeName
	 * @return The PlotPackage that has been requested
	 * @throws DeviceException
	 */
	@Override
	public Object getAttribute(String attributeName) throws DeviceException {
		PlotPackage tPP;
		logger.debug("Entering getAttribute(String attributeName)");
		synchronized (PLOT_PACKAGES_LOCK) {
			tPP = plots.get(attributeName);
		}
		clearHashmap();
		// add the new key to the deletion table
		// taggedForDeletion.add(attributeName);
		logger.debug("Leaving getAttribute(String attributeName)");
		return tPP;

	}

	private void clearHashmap() {
		logger.debug("Entering clearHashmap() ");
		clearHashmap(2000);
		logger.debug("Leaving clearHashmap() ");
	}

	private void clearHashmap(long age) {

		logger.debug("Entering clearHashmap(long age) ");

		synchronized (PLOT_PACKAGES_LOCK) {

			// first check for any really old data which can be removed.
			Iterator<String> keys = plots.keySet().iterator();
	
			while (keys.hasNext()) {
				// check the time difference
				String key = keys.next();
				long dt = System.currentTimeMillis() - time.get(key);
				if (dt > age) {
					// tag the data for removal
					taggedForDeletion.add(key);
				}
			}
	
			// could just keep last N packages
			
			// now go through the deletion list and remove all data from the
			// hashmaps that correspond to the keys
			for (int i = 0; i < taggedForDeletion.size(); i++) {
				plots.remove(taggedForDeletion.get(i));
				time.remove(taggedForDeletion.get(i));
			}
		}

		// now clean out the array
		taggedForDeletion.clear();

		logger.debug("Leaving clearHashmap(long age) ");
	}
}