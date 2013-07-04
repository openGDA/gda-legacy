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

/**
 * Paints a circle onto an image showing the size of the aperture.
 */
public interface AperturePainter extends SampleImageModifier {
	
	/**
	 * Sets whether the aperture circle should be displayed.
	 * 
	 * @param display {@code true} to display the aperture circle
	 */
	public void setDisplayAperture (boolean display);
	
	/**
	 * Sets the size of the apertures.
	 * 
	 * @param apertureSize size of the aperture
	 */
	public void setApertureSize(int apertureSize);
}