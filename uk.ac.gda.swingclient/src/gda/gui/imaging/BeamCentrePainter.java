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
 * Paints a crosshair onto an image showing the location of the beam.
 */
public interface BeamCentrePainter extends SampleImageModifier {
	
	/**
	 * Sets whether to display the crosshair showing the centre of the beam.
	 * 
	 * @param display whether to show the crosshair
	 */
	public void setDisplayCrossHair(boolean display);
	
	/**
	 * Sets the position of the beam centre.
	 * 
	 * @param centre the position of the beam centre
	 */
	public void setCentre(Point centre);

}
