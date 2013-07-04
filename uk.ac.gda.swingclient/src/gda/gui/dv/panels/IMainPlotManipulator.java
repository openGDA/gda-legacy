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

import gda.gui.dv.DoubleBufferedImageData;

import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;

/**
 * This interface should pass through most of the base events, but in terms of the reference frame of the image.
 */
public interface IMainPlotManipulator extends MouseListener, MouseMotionListener {

	/**
	 * Main point of the interface which is called when the tab is selected or the image changed, and its this which
	 * tells the plot what the data is
	 * @param overlay 
	 *            old overlay
	 * @param pix
	 *            the image data
	 * @return the created overlay.
	 */
	public DoubleBufferedImageData getOverlay(DoubleBufferedImageData overlay, DoubleDataset pix);

	/**
	 * Make sure there is no reference to any overlay in the manipulator 
	 */
	public void releaseOverlay();
}
