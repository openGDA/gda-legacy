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

package gda.gui.dv.panels;

import java.util.List;

import org.eclipse.january.dataset.DoubleDataset;

/**
 * Implement this interface if you wish to be informed when a plot is updated on the main plot. *
 */
public interface IMainPlotNewPlotUpdater {


	/**
	 * The main function to apply, this is called whenever a new image is transfered from the server and
	 * plotted. It receives a pointer to the image information
	 * @param xAxis The x axis of the updated plot
	 * @param yAxis All the y axis of the updated plots
	 */
	public void newPlotUpdate(DoubleDataset xAxis, List<DoubleDataset> yAxis);
	
}
