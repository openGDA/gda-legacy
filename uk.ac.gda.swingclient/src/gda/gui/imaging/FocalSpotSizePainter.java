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

package gda.gui.imaging;

import java.awt.Point;

/**
 * Paints a box onto an image showing the size of the focal spot of the beam.
 */
public interface FocalSpotSizePainter extends SampleImageModifier {
	
	/**
	 * Sets whether the focal spot should be displayed.
	 * 
	 * @param display {@code true} to display the focal spot box/ellipse
	 */
	public void setDisplayFocalSpotSize(boolean display);
	/**
	 * Sets whether the focal spot is displayed as a rectangle or ellipse.
	 * 
	 * @param display {@code true} to display the focal spot as an ellipse
	 */
	public void setDisplayFocalSpotSizeAsEllipse(boolean display);
	/**
	 * Sets the position and size of a shape representing the focal spot. The
	 * points are relative to the top left hand corner of the panel.
	 * 
	 * @param topLeft position of the top left corner of the focal spot box
	 * @param bottomRight position of the bottom right corner of the focal spot box
	 */
	public void setFocalSpotSize(Point topLeft, Point bottomRight);

}
