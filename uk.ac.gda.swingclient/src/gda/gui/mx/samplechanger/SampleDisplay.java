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

package gda.gui.mx.samplechanger;

import javax.swing.JLayeredPane;

import gda.gui.imaging.SampleImageModifier;

/**
 * An interface for panels which display the sample image and overlay the image with a cross-hair and rectangle to show
 * the beam position and size.
 */
public interface SampleDisplay extends Runnable {

	/**
	 * Starts the thread which collects and displays the images
	 */
	public abstract void start();

	/**
	 * Stops the thread which collects and displays the images.
	 */
	public abstract void stop();
	
	/**
	 * Returns the pane into which sample images are drawn.
	 * 
	 * @return the image pane
	 */
	public JLayeredPane getImagePane();
	
	/**
	 * Returns the image modifier that paints onto sample images.
	 * 
	 * @return the image modifier
	 */
	public SampleImageModifier getImageModifier();

}