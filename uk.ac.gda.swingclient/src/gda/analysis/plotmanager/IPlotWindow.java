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

import gda.gui.dv.panels.DataSetImage;
import gda.gui.dv.panels.DataSetPlot;
import gda.gui.dv.panels.DataSetPlot3D;
import gda.gui.dv.panels.DataSetImages;

/**
 * Interface for the PlotPackage which will make sure that it can plot to the device that is passed to it.
 */
public interface IPlotWindow {

	/**
	 * Makes sure that the IPlotWindow is set to display Images
	 * 
	 * @return The DataSetImage for use by the plotPackage
	 */
	DataSetImage setImageDisplay();

	/**
	 * Makes sure that the IPlotWindow is set to Plot3D
	 * 
	 * @return The DataSetPlot3D for use by the plotPackage
	 */
	
	DataSetPlot3D setPlot3DDisplay();
	
	/**
	 * Makes sure that the IPlotWindow is set to Images
	 * @return the DataSetImages for use by the plotPackage
	 */
	
	DataSetImages setImagesDisplay();
	
	/**
	 * Sets the IPlotWindow to display graphs
	 * 
	 * @param clearFirst
	 *            specifies weather the plot should be cleared or not
	 * @param xName
	 *            Specifies the name for the x axis of the plot
	 * @return The DataSetPlot for use by the plotPackage
	 */
	DataSetPlot setGraphDisplay(boolean clearFirst, String xName);

	/**
	 * Simply returns the graphDisplay when it is required
	 * 
	 * @return The Display for the plot package to plot to.
	 */
	DataSetPlot getGraphDisplay();

	/**
	 * Sets a tag in the hashmap for remembering which line is plotted with which.
	 * 
	 * @param tag
	 *            The number of the lines to be plotted
	 * @param lineNumber
	 *            the number of the line which is to plot the data.
	 */
	void setTag(int tag, int lineNumber);

	/**
	 * Gets the linenumber of a particular tag
	 * 
	 * @param tag
	 *            The tag to retrive
	 * @return The associated line number
	 */
	int getTag(int tag);

}
