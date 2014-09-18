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

package gda.analysis;

import gda.analysis.plotmanager.PlotManager;
import gda.device.DeviceException;
import gda.factory.Finder;
import gda.gui.dv.DataVectorPlot;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.SDAPlotter;

/**
 * Class which allows plotting straight to the Data Vector Window
 */
public class Plotter {

	/**
	 * Setup the logging facilities
	 */
	private static final Logger logger = LoggerFactory.getLogger(Plotter.class);

	/**
	 * Documentation for java help
	 */
	public static String __doc__ = "GDA Plotting functionality\n"
			+ "--------------------------\n"
			+ "Wrapper to all the plotting functionality for post processing in the GDA RCP enviroment\n"
			+ "--------------------------\n"
			+ "Functions avalialbe\n"
			+ "\trcpPlot(xAxis,yAxis):\n"
			+ "\t\tPlots the given yAxis dataset against the given xAxis dataset\n"
			+ "\trcpPlot(xAxis,[yAxis1,yAxis2]):\n"
			+ "\t\tPlots all of the given yAxis datasets against the xAxis";


	private static DataSet[] convert(IDataset... set) {
		DataSet[] array = new DataSet[set.length];
		for (int i = 0; i < set.length; i++)
			array[i] = DataSet.convertToDataSet(set[i]);
		return array;
	}

	/**
	 * Plots the x axis against the y axis
	 * 
	 * @param panelName
	 *            The name of the panel which the data is to be sent to
	 * @param xAxis
	 *            The Dataset containing the x Axis data
	 * @param yAxis
	 *            The Dataset containing the y Axis data
	 */
	public static void plot(String panelName, IDataset xAxis, IDataset yAxis) {
		PlotManager.getInstance().plot(panelName, DataSet.convertToDataSet(xAxis), DataSet.convertToDataSet(yAxis));
	}

	/**
	 * Plots the x axis against the y axis
	 * 
	 * @param panelName
	 *            The name of the panel which the data is to be sent to
	 * @param xAxis
	 *            The Dataset containing the x Axis data
	 * @param yAxis
	 *            The Dataset containing the y Axis data
	 */
	public static void plotOver(String panelName, IDataset xAxis, IDataset yAxis) {
		PlotManager.getInstance().plotOver(panelName, DataSet.convertToDataSet(xAxis), DataSet.convertToDataSet(yAxis));
	}

	/**
	 * Plots the x axis against various y values
	 * 
	 * @param panelName
	 *            The name of the panel which the data is to be sent to
	 * @param xAxis
	 *            The Dataset containing the x Axis data
	 * @param yAxis
	 *            The Dataset containing the y Axis data
	 */
	public static void plot(String panelName, IDataset xAxis, IDataset... yAxis) {
		PlotManager.getInstance().plot(panelName, DataSet.convertToDataSet(xAxis), convert(yAxis));
	}

	/**
	 * Plots the x axis against various y values
	 * 
	 * @param panelName
	 *            The name of the panel which the data is to be sent to
	 * @param xAxis
	 *            The Dataset containing the x Axis data
	 * @param yAxis
	 *            The Dataset containing the y Axis data
	 */
	public static void plotOver(String panelName, IDataset xAxis,
			IDataset... yAxis) {
		PlotManager.getInstance().plotOver(panelName, DataSet.convertToDataSet(xAxis), convert(yAxis));
	}

	/**
	 * Plots the dataset as an image on the appropriate panel
	 * 
	 * @param panelName
	 *            the name of the panel to be plotted to
	 * @param data
	 *            the dataset containing all the data.
	 */
	public static void plotImage(String panelName, IDataset data) {
		PlotManager.getInstance().plotImage(panelName, DataSet.convertToDataSet(data));
	}

	/**
	 * Plot a series of images on the appropriate panel
	 * 
	 * @param panelName
	 *            the name of the panel to be plotted to
	 * @param data
	 *            the datasets containing all the data.
	 */

	public static void plotImages(String panelName, IDataset... data) {
		PlotManager.getInstance().plotImages(panelName, convert(data));
	}

	/**
	 * Plots the dataset as an 3D Plot on the appropriate panel
	 * 
	 * @param panelName
	 *            the name of the panel to be plotted to
	 * @param data
	 *            the dataset containing all the data.
	 */

	public static void plot3D(String panelName, IDataset data) {
		PlotManager.getInstance().plot3D(panelName, DataSet.convertToDataSet(data));
	}

	/**
	 * Plots the dataset as an 3D Plot on the appropriate panel
	 * 
	 * @param panelName
	 *            the name of the panel to be plotted to
	 * @param data
	 *            the dataset containing all the data.
	 * @param useWindow
	 *            use a window on the dataset if too large otherwise subsample
	 */

	public static void plot3D(String panelName, IDataset data, boolean useWindow) {
		PlotManager.getInstance().plot3D(panelName, useWindow, DataSet.convertToDataSet(data));
	}

	/**
	 * Add more 1D plots to an already existing 3D Plot
	 * 
	 * @param panelName
	 * @param data
	 */
	public static void addPlot3D(String panelName, IDataset... data)
	// Plotting routines for the Client
	{
		PlotManager.getInstance().addPlot3D(panelName, convert(data));
	}

	/**
	 * Plots the x axis against the y axis, this is for use on the Client only,
	 * and therefore is written specifically for use in Java
	 * 
	 * @param panelName
	 *            The name of the panel which the data is to be sent to
	 * @param xAxis
	 *            The Dataset containing the x Axis data
	 * @param yAxis
	 *            The Dataset containing the y Axis data
	 */
	public static void clientPlot(String panelName, IDataset xAxis,
			IDataset... yAxis) {
		DataVectorPlot dvp = (DataVectorPlot) (Finder.getInstance()
				.find(panelName));
		if (dvp != null) {
			dvp.plotXY(DataSet.convertToDataSet(xAxis), convert(yAxis));
		} else {
			logger.error("Finder cannot find " + panelName);
		}
	}

	/**
	 * Plots the x axis against the y axis, this is for use on the Client only,
	 * and therefore is written specifically for use in Java
	 * 
	 * @param panelName
	 *            The name of the panel which the data is to be sent to
	 * @param xAxis
	 *            The Dataset containing the x Axis data
	 * @param yAxis
	 *            The Dataset containing the y Axis data
	 */
	public static void clientPlotOver(String panelName, IDataset xAxis,
			IDataset... yAxis) {
		DataVectorPlot dvp = (DataVectorPlot) (Finder.getInstance()
				.find(panelName));
		if (dvp != null) {
			dvp.plotOverXY(DataSet.convertToDataSet(xAxis), convert(yAxis));
		} else {
			logger.error("Finder cannot find " + panelName);
		}
	}

	/*
	 * Functionality for the RCPClient, all the other plotting functions will
	 * eventually be pointed to these new methods
	 */

	/**
	 * The method Plotter.rcpPlot is now deprecated, this has been replaced with
	 * RCPPlotter.plot()
	 * 
	 * @param xAxis
	 *            The dataset to use as the X axis
	 * @param yAxis
	 *            The dataset to use as the Y axis
	 * @throws DeviceException
	 */
	@Deprecated
	public static void rcpPlot(IDataset xAxis, IDataset yAxis)
			throws Exception {
		logger.warn("The method Plotter.rcpPlot is now deprecated, this has been replaced with RCPPlotter.plot()");
		SDAPlotter.plot("Plot 1", xAxis, yAxis);
	}

	/**
	 * The method Plotter.rcpPlot is now deprecated, this has been replaced with
	 * RCPPlotter.plot()
	 * 
	 * @param xAxis
	 *            The dataset to use as the X axis
	 * @param yAxis
	 *            The dataset to use as the Y axis
	 * @throws DeviceException
	 */
	@Deprecated
	public static void rcpPlot(IDataset xAxis, IDataset[] yAxis)
			throws Exception {
		logger.warn("The method Plotter.rcpPlot is now deprecated, this has been replaced with RCPPlotter.plot()");
		SDAPlotter.plot("Plot 1", xAxis, yAxis);
	}

}
