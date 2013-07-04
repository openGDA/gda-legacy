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

package gda.gui.dv.panels;

import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;

/**
 * Basic model for a panel which wishes to be updated but doesn't do anything else.
 */
public interface IMainPlotNewImageUpdater {

	/**
	 * The main function to apply, this is called whenever a new image is transfered from the server and
	 * plotted. It receives a pointer to the image information
	 * 
	 * @param pix
	 */
	public void newImageUpdate(DoubleDataset pix);

}
