/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council
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

package gda.gui.dv.panels;

import gda.gui.dv.ImageData;

import java.util.LinkedList;

import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;

import de.jreality.scene.SceneGraphComponent;

/**
 * Interface for any 3D Plotting operation that can be done on the DataSetPlot3D panel this is the minimum set of
 * functionality any of the different that a specific 3D plotting needs to implement
 */

public interface IDataSet3DCorePlot {

	/**
	 * Maximum X coordinate axis length
	 */
	public static final double MAXX = 15.0;
	/**
	 * Maximum Y coordinate axis length
	 */
	public static final double MAXY = 15.0;
	/**
	 * Maximum Z coordinate axis length
	 */
	public static final double MAXZ = 15.0;
	/**
	 * Maximum number of ticks on the coordinate axis
	 */
	public static final int MAXTICKS = 64;
	/**
	 * Maximum number of labels on the coordinate axis
	 */
	public static final int MAXLABELS = 16;

	/**
	 * @param zMin
	 *            minimum z value
	 * @param zMax
	 *            maximum z value
	 * @return SceneGraph node that contains the Z coordinate axis labels
	 */
	public SceneGraphComponent buildZCoordLabeling(double zMin, double zMax);

	/**
	 * @param yAxis
	 *            y coordinate axis size
	 * @param yDim
	 *            number of data set entries on the y axis
	 * @return SceneGraph node that contains the Y coordinate axis labels
	 */
	public SceneGraphComponent buildYCoordLabeling(double yAxis, int yDim);

	/**
	 * @param xAxis
	 *            x coordinate axis size
	 * @param xDim
	 *            number of data set entries on the x axis
	 * @return SceneGraph node that contains the X coordinate axis labels
	 */

	public SceneGraphComponent buildXCoordLabeling(double xAxis, int xDim);

	/**
	 * @param xAxis
	 *            x coordinate axis size
	 * @param yAxis
	 *            y coordinate axis size
	 * @param xDim
	 *            number of data set entries on the x axis
	 * @param yDim
	 *            number of data set entries on the y axis
	 * @return SceneGraph node that contains a grid representation of the x-y axis
	 */
	public SceneGraphComponent buildCoordGrid(double xAxis, double yAxis, int xDim, int yDim);

	/**
	 * @param xAxis
	 *            x coordinate axis size
	 * @param yAxis
	 *            y coordinate axis size
	 * @param zAxis
	 *            z coordinate axis size
	 * @param zMin
	 *            minimum z value
	 * @param zMax
	 *            maximum z value
	 * @param xDim
	 *            number of data set entries on the x axis
	 * @param yDim
	 *            number of data set entries on the y axis
	 * @return SceneGraph node that contains all coordinate axis ticks
	 */

	public SceneGraphComponent buildCoordAxesTicks(double xAxis, double yAxis, double zAxis, double zMin, double zMax,
			int xDim, int yDim);

	/**
	 * @param currentDataSets
	 *            List of DataSets containing the raw data for the graph
	 * @param graph
	 *            SceneGraphComponent of the graph object
	 * @param xScale
	 *            x coordinate axis scaling
	 * @param yScale
	 *            y coordinate axis scaling
	 * @param zScale
	 *            z coordinate axis scaling
	 * @param display
	 *            displaying mode
	 * @return SceneGraph node that contains the actual plot
	 */
	public SceneGraphComponent buildGraph(final LinkedList<DoubleDataset> currentDataSets, SceneGraphComponent graph, double xScale,
			double yScale, double zScale, DisplayType display);

	/**
	 * @param rootNode
	 *            SceneGraph root node that contains the graph node
	 * @param graphNode
	 *            SceneGraph node that contains the plot
	 * @param datasets
	 *            List of dataSets to plot
	 * @param newDisplayMode
	 *            new displaying mode
	 * @return SceneGraph node that contains the new plot
	 */
	public SceneGraphComponent setDisplayMode(SceneGraphComponent rootNode, SceneGraphComponent graphNode,
			final LinkedList<DoubleDataset> datasets, int newDisplayMode);

	/**
	 * @param newScaling
	 *            newScaling type (see enum in DataSetPlot3D.java)
	 */
	public void setScaling(ScaleType newScaling);

	/**
	 * @param colourTable
	 *            the colourTable that should be applied to the data
	 * @param graph
	 *            SceneGraph node that contains the plot
	 * @param xDim
	 *            number of data entries on x axis
	 * @param yDim
	 *            number of data entries on y axis
	 */

	public void handleColourCast(ImageData colourTable, SceneGraphComponent graph, int xDim, int yDim);

	/**
	 * @param windowX
	 *            new window x position
	 * @param windowY
	 *            new window y position
	 */
	public void setNewWindowPos(int windowX, int windowY);

	/**
	 * @param maxXsize
	 *            maximum number of data elements on the x coordinate axis
	 * @param maxYsize
	 *            maximum number of data elements on the y coordinate axis
	 */

	public void setDataDimensions(int maxXsize, int maxYsize);

	/**
	 * Get the current displaying type
	 * 
	 * @return the current displaying type
	 */
	public DisplayType getCurrentDisplay();

}
