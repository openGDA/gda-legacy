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
 * Paints a box onto an image showing the size of the beam.
 */
public interface BeamSizePainter extends SampleImageModifier {
	
	/**
	 * Sets whether the beam size rectangle should be displayed.
	 * 
	 * @param display {@code true} to display the beam size box
	 */
	public void setDisplayBeamSize(boolean display);
}
