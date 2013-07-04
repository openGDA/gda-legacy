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

package gda.gui.dv.panels.vispanels;

import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
import gda.gui.dv.ImageData;
import gda.gui.dv.panels.IMainPlotVisualiser;

/**
 * This is a basic colour filter which converts the data into a green colour map.
 */
public class GreenColourCast implements IMainPlotVisualiser {

	/**
	 * Main colour cast function
	 * 
	 * @param raw
	 *            raw data
	 * @return Colour corrected data
	 */
	@Override
	public ImageData cast(DoubleDataset raw) {
		/*
		 * double max = raw[0]; double min = raw[0]; for(int i = 1; i < raw.length; i++) { if(max < raw[i] ) { max =
		 * raw[i]; } else if ( min > raw[i]) { min = raw[i]; } }
		 */
		double max = raw.max().doubleValue();
		double min = raw.min().doubleValue();

		ImageData result = new ImageData(raw.getShape()[1], raw.getShape()[0]);

		double scale = (255.0 / (max - min));
		double [] buffer = raw.getData();
		for (int i = 0; i < raw.getSize(); i++) {

			int val = ((int) ((buffer[i] - min) * scale));
			result.set((val * 256), i);
		}

		return result;
	}

}
