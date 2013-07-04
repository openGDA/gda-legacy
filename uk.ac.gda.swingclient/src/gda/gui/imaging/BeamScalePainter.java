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
 * Paints a beam scale box onto an image.
 */
public interface BeamScalePainter extends SampleImageModifier {
	
	/**
	 * Sets whether the display scale box should be displayed in the bottom
	 * right-hand corner.
	 * 
	 * @param displayScale {@code true} to show the scale box; {@code false}
	 *        otherwise
	 */
	public void setDisplayScale(boolean displayScale);
	
	/**
	 * Returns the number of microns per pixel in the horizontal direction.
	 * 
	 * @return number of microns per pixel in the horizontal direction
	 */
	public double getXScale();
	
	/**
	 * Sets the number of microns per pixel in the horizontal direction.
	 * 
	 * @param scale number of microns per pixel in the horizontal direction
	 */
	public void setXScale(double scale);
	
	/**
	 * Returns the number of microns per pixel in the vertical direction.
	 * 
	 * @return number of microns per pixel in the vertical direction
	 */
	public double getYScale();
	
	/**
	 * Sets the number of microns per pixel in the vertical direction.
	 * 
	 * @param scale number of microns per pixel in the vertical direction
	 */
	public void setYScale(double scale);
	
}
