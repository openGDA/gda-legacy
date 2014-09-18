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

import gda.gui.dv.GraphUpdate;
import gda.gui.dv.PointUpdate;
import gda.gui.dv.panels.DataSetImage;
import gda.gui.dv.panels.DataSetImages;
import gda.gui.dv.panels.DataSetPlot3D;

import java.io.Serializable;

import javax.swing.SwingUtilities;

import org.eclipse.dawnsci.analysis.dataset.impl.DoubleDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PlotPackage Class
 */
public class PlotPackage implements Serializable, Cloneable {

	private static final Logger logger = LoggerFactory.getLogger(PlotPackage.class);
	
	private int plotType = 0;

	private final static int XY_PLOT = 0;

	private final static int XY_OVER_PLOT = 1;

	private final static int IMAGE_PLOT = 2;

	private final static int ADD_POINTS = 3;

	private final static int XY_TAGGED_PLOT = 4;

	private final static int XY_TAGGED_OVER_PLOT = 5;
	
	private final static int XY_PLOT3D = 6;
	
	private final static int XY_PLOT3D_WINDOW = 7;
	
	private final static int XY_PLOT3D_ADD_1D = 8;
	
	private final static int XY_IMAGE_PLOTS = 9;

	// for passing full datasets around
	private DoubleDataset[] data = null;

	// for passing individual points about
	private int[] tags = null;

	private double[] xDoubles = null;

	private double[] yDoubles = null;

	private String plotPanelName = "";

	/**
	 * @return plot panel name
	 */
	public String getPlotPanelName() {
		return plotPanelName;
	}

	/**
	 * @param plotPanelName
	 */
	public void setPlotPanelName(String plotPanelName) {
		this.plotPanelName = plotPanelName;
	}

	/**
	 * @return plot type
	 */
	public int getPlotType() {
		return plotType;
	}

	/**
	 * @param plotType
	 */
	public void setPlotType(int plotType) {
		this.plotType = plotType;
	}

	// SECTION OF CODE WHICH IS FOR THE SERVER SIDE, THIS SHOWS HOW TO PUT
	// THINGS
	// INTO THE PLOTPACKAGE OBJECT

	/**
	 * @param xAxis
	 * @param dataSets
	 */
	public void setXYPlot(DoubleDataset xAxis, DoubleDataset... dataSets) {
		// first set up the tag correctly
		plotType = XY_PLOT;

		// now set up the data array
		data = new DoubleDataset[dataSets.length + 1];

		data[0] = new DoubleDataset(xAxis);

		for (int i = 0; i < dataSets.length; i++) {
			data[i + 1] = new DoubleDataset(dataSets[i]);
		}

	}

	/**
	 * @param xAxis
	 * @param dataSets
	 */
	public void setXYOverPlot(DoubleDataset xAxis, DoubleDataset... dataSets) {
		// first set up the tag correctly
		plotType = XY_OVER_PLOT;

		// now set up the data array
		data = new DoubleDataset[dataSets.length + 1];

		data[0] = new DoubleDataset(xAxis);

		for (int i = 0; i < dataSets.length; i++) {
			data[i + 1] = new DoubleDataset(dataSets[i]);
		}

	}

	/**
	 * @param xAxis
	 * @param dataSets
	 * @param yTags
	 */
	public void setXYUpdatablePlot(DoubleDataset xAxis, DoubleDataset[] dataSets, int[] yTags) {

		// first set up the tag correctly
		plotType = XY_TAGGED_PLOT;

		// now set up the data array
		data = new DoubleDataset[dataSets.length + 1];

		data[0] = new DoubleDataset(xAxis);

		for (int i = 0; i < dataSets.length; i++) {
			data[i + 1] = new DoubleDataset(dataSets[i]);
		}

		tags = new int[yTags.length];
		for (int i = 0; i < yTags.length; i++) {
			tags[i] = yTags[i];
		}

	}

	/**
	 * @param xAxis
	 * @param dataSets
	 * @param yTags
	 */
	public void setXYUpdatableOverPlot(DoubleDataset xAxis, DoubleDataset[] dataSets, int[] yTags) {

		// first set up the tag correctly
		plotType = XY_TAGGED_OVER_PLOT;

		// now set up the data array
		data = new DoubleDataset[dataSets.length + 1];

		data[0] = new DoubleDataset(xAxis);

		for (int i = 0; i < dataSets.length; i++) {
			data[i + 1] = new DoubleDataset(dataSets[i]);
		}

		tags = new int[yTags.length];
		for (int i = 0; i < yTags.length; i++) {
			tags[i] = yTags[i];
		}

	}

	/**
	 * @param yTags
	 * @param xValue
	 * @param yValues
	 */
	public void setAddPoints(int[] yTags, double xValue, double[] yValues) {

		// first set up the tag correctly
		plotType = ADD_POINTS;

		tags = new int[yTags.length];
		xDoubles = new double[yTags.length];
		yDoubles = new double[yTags.length];

		for (int i = 0; i < yTags.length; i++) {
			tags[i] = yTags[i];
			xDoubles[i] = xValue;
			yDoubles[i] = yValues[i];
		}
	}

	/**
	 * @param dataSets
	 */
	public void setImagePlot(DoubleDataset... dataSets) {
		// first set up the tag correctly
		plotType = IMAGE_PLOT;

		// now set up the data array
		data = new DoubleDataset[dataSets.length];

		for (int i = 0; i < dataSets.length; i++) {
			data[i] = new DoubleDataset(dataSets[i]);
		}
	}
	
	/**
	 * @param useWindow
	 * @param dataSets
	 */
	
	public void setPlot3D(boolean useWindow, DoubleDataset... dataSets) {
		
		if (useWindow)
			plotType = XY_PLOT3D_WINDOW;
		else
			plotType = XY_PLOT3D;
		
		data = new DoubleDataset[dataSets.length];
		for (int i = 0; i < dataSets.length; i++) {
			data[i] = new DoubleDataset(dataSets[i]);
		}
	}
	
	/**
	 * Plot a series of images
	 * @param dataSets
	 */
	
	public void setPlotImages(DoubleDataset...dataSets)
	{
		plotType = XY_IMAGE_PLOTS;
		data = new DoubleDataset[dataSets.length];
		for (int i = 0; i < dataSets.length; i++)
		{
			data[i]= new DoubleDataset(dataSets[i]);
		}
	}
	
	/**
	 * Add another 1D Plot to the 3D plots
	 * @param dataSets
	 */
	public void addPlot3D(DoubleDataset... dataSets) {
		
		plotType = XY_PLOT3D_ADD_1D;
		
		data = new DoubleDataset[dataSets.length];
		for (int i = 0; i < dataSets.length; i++) {
			data[i] = new DoubleDataset(dataSets[i]);
		}
	}
	
	// SECTION OF CODE WHICH IS FOR THE CLIENT SIDE, IT CONTAINS FUNCTIONS WHICH
	// USE THE PACKAGE TO PLOT ONTO THE APPROPRIATE PANELS.

	private void plotXYPlot(IPlotWindow dvp) {

		GraphUpdate doGUIUpdate = new GraphUpdate();

		DoubleDataset ydata[] = new DoubleDataset[data.length - 1];
		
		for (int i = 0; i < ydata.length; i++) {
			ydata[i] = data[i + 1];
		}

		// This sets up the plot so that other panels can know the raw data held in the plot.
		dvp.getGraphDisplay().setXAxis(data[0]);
		dvp.getGraphDisplay().setYAxis(ydata);
		
		doGUIUpdate.init(dvp, true, data[0], ydata);

		SwingUtilities.invokeLater(doGUIUpdate);

	}

	private void plotXYOverPlot(IPlotWindow dvp) {

		logger.debug("Entering the plotXYOverPlot");

		GraphUpdate doGUIUpdate = new GraphUpdate();

		DoubleDataset ydata[] = new DoubleDataset[data.length - 1];

		for (int i = 0; i < ydata.length; i++) {
			ydata[i] = data[i + 1];
		}
		
		// This sets up the plot so that other panels can know the raw data held in the plot.
		dvp.getGraphDisplay().addYAxis(ydata);

		doGUIUpdate.init(dvp, false, data[0], ydata);

		SwingUtilities.invokeLater(doGUIUpdate);
		
		logger.debug("Leaving the plotXYOverPlot");

	}

	private void plotXYTaggedPlot(IPlotWindow dvp) {

		GraphUpdate doGUIUpdate = new GraphUpdate();

		DoubleDataset ydata[] = new DoubleDataset[data.length - 1];

		for (int i = 0; i < ydata.length; i++) {
			ydata[i] = data[i + 1];
		}

		doGUIUpdate.init(dvp, true, data[0], tags, ydata);
		
		// This sets up the plot so that other panels can know the raw data held in the plot.
		dvp.getGraphDisplay().setXAxis(data[0]);
		dvp.getGraphDisplay().setYAxis(ydata);

		SwingUtilities.invokeLater(doGUIUpdate);

	}

	private void plotXYTaggedOverPlot(IPlotWindow dvp) {

		GraphUpdate doGUIUpdate = new GraphUpdate();

		DoubleDataset ydata[] = new DoubleDataset[data.length - 1];

		for (int i = 0; i < ydata.length; i++) {
			ydata[i] = data[i + 1];
		}
		
		doGUIUpdate.init(dvp, false, data[0], tags, ydata);
		
		// This sets up the plot so that other panels can know the raw data held in the pl
		dvp.getGraphDisplay().addYAxis(ydata);

		SwingUtilities.invokeLater(doGUIUpdate);

	}

	private void plotAddPoints(IPlotWindow dvp) {

		PointUpdate doGUIUpdate = new PointUpdate();

		doGUIUpdate.init(dvp, xDoubles, yDoubles, tags);

		SwingUtilities.invokeLater(doGUIUpdate);

	}

	private void plotImage(IPlotWindow dvp) {

		DataSetImage dataSetImage = dvp.setImageDisplay();

		// remember to use row-major storage
		dataSetImage.setPixWidth((data[0].getShape()[1]));
		dataSetImage.setPixHeight((data[0].getShape()[0]));

		// this whole section should be able to be converted into a single call
		// if pix is made into a dataset.
		dataSetImage.pix = new DoubleDataset(data[0]);

		// now flush the information to the drawing buffer appropriately
		dataSetImage.applyColorCast();

		dataSetImage.repaint();
	}

	private void plot3D(IPlotWindow dvp, boolean subsample) {
		DataSetPlot3D dataSetPlot3D = dvp.setPlot3DDisplay();
		dataSetPlot3D.setPlot(new DoubleDataset(data[0]),subsample);		
	}
	
	private void plotImages(IPlotWindow dvp)
	{
		DataSetImages dataSetImages = dvp.setImagesDisplay();
		dataSetImages.plotImages(data);
	}
	
	private void additionalPlot3D(IPlotWindow dvp)
	{
		DataSetPlot3D dataSetPlot3D = dvp.setPlot3DDisplay();		
		dataSetPlot3D.addPlot(data);
	}
	/**
	 * @param dvp
	 */
	public void plot(IPlotWindow dvp) {
		logger.debug("Entering the plot mathod on the PlotPasser");
		switch(plotType)
		{
			case XY_PLOT:
				plotXYPlot(dvp);
			break;
			case XY_OVER_PLOT:
				plotXYOverPlot(dvp);
			break;
			case XY_TAGGED_PLOT:
				plotXYTaggedPlot(dvp);
			break;
			case ADD_POINTS:
				plotAddPoints(dvp);
			break;
			case IMAGE_PLOT:
				plotImage(dvp);
			break;
			case XY_TAGGED_OVER_PLOT:
				plotXYTaggedOverPlot(dvp);
			break;	
			case XY_PLOT3D:
				plot3D(dvp,true);
			break;
			case XY_PLOT3D_WINDOW:
				plot3D(dvp,false);
			break;
			case XY_PLOT3D_ADD_1D:
				additionalPlot3D(dvp);
			break;
			case XY_IMAGE_PLOTS:
		        plotImages(dvp);		
			break;
			default:
				logger.warn("Tried to Plot unknown type");
		}

	}

	@Override
	public Object clone() {

		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			// FIXME Auto-generated catch block Should be updated, if this is being used
			e.printStackTrace();
		}
		return null;
	}
}
