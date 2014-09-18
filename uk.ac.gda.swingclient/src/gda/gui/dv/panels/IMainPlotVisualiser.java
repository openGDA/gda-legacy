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

import org.eclipse.dawnsci.analysis.dataset.impl.DoubleDataset;

import gda.gui.dv.ImageData;

/**
 * This interface specifies the functions required for converting raw data in the DataSetImage panel to the actual pixel
 * RGB values.
 */
public interface IMainPlotVisualiser {

	/**
	 * This function should use the raw pixel information to produce a RGB map which can be drawn by the DataSetImage
	 * object
	 * 
	 * @param raw
	 *            The raw data in the form of a double array
	 * @return the corrected image data in the form of an int array.
	 */
	public ImageData cast(DoubleDataset raw);

}
