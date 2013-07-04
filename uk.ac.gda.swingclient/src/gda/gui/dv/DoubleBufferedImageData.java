/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council
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

package gda.gui.dv;

/**
 * This subclasses ImageData to provide a double-buffered version
 * 
 * All drawing operations go through to the superclass, all queries go via
 * a reference that can point to a new data buffer or the old one during a flip
 */
public class DoubleBufferedImageData extends ImageData {
	int[] newdata = null;
	int[] visdata = null;

	/**
	 * @param width
	 * @param height
	 */
	public DoubleBufferedImageData(int width, int height) {
		super(width, height);
		newdata = new int[w * h];
		visdata = newdata;
	}

	/**
	 * @param width
	 * @param height
	 * @param datain
	 */
	public DoubleBufferedImageData(int width, int height, int[] datain) {
		super(width, height, datain);
		newdata = new int[w * h];
		visdata = newdata;
	}

	/**
	 * Flip image from hidden data
	 */
	public void flipImage() {
		// Hopefully this is atomic
		visdata = data;
		for (int i = 0; i < data.length; i++) {
			newdata[i] = data[i];
		}
		visdata = newdata;
	}

	/**
	 * getter for the data
	 * 
	 * @return the data integer array
	 */
	@Override
	public int[] getData() {
		return visdata;
	}

	/**
	 * gets the value at a position
	 * 
	 * @param position
	 * @return the value at that point
	 */
	@Override
	public int get(int position) {
		return visdata[position];
	}

	/**
	 * gets the value at x and y
	 * 
	 * @param x
	 * @param y
	 * @return the value at that point
	 */
	@Override
	public int get(int x, int y) {
		return visdata[x + y * w];
	}

	/**
	 * gets an integer array of the [R,G,B,A] values at a point
	 * 
	 * @param position
	 *            the position of the data in the dataset
	 * @return the [R,G,B,A] integer array all from 0-255
	 */
	@Override
	public int[] getRGBA(int[] position) {
		int[] result = new int[4];

		// Sanity check
		if (position[0] < 0)
			position[0] = 0;
		if (position[0] >= w)
			position[0] = w - 1;
		if (position[1] < 0)
			position[1] = 0;
		if (position[1] >= h)
			position[1] = h - 1;

		int image = visdata[position[0] + position[1] * w];
		result[0] = image & 0xff;
		result[1] = (image >> 8) & 0xff;
		result[2] = (image >> 16) & 0xff;
		result[3] = (image >> 24) & 0xff;

		return result;
	}

}
