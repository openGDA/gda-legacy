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

package gda.util;

import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;

/**
 * provides support for multiple screen display configuration.
 */
public final class MultiScreenSupport {

	private GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();

	private GraphicsDevice[] gs = ge.getScreenDevices();

	private int[] screenWidth = new int[gs.length];

	private int[] screenHeight = new int[gs.length];

	private int[] screenXoffs = new int[gs.length];

	private int[] screenYoffs = new int[gs.length];

	private int[] screenBitDepth = new int[gs.length];

	private int[] screenRefreshRate = new int[gs.length];

	private Dimension[] screenSize = new Dimension[gs.length];

	private GraphicsDevice primaryScreen;

	/**
	 * constructor - handle only the default graphics configuration of the platform.
	 */
	public MultiScreenSupport() {
		for (int i = 0; i < gs.length; i++) {
			GraphicsDevice gd = gs[i];

			GraphicsConfiguration dgc = gd.getDefaultConfiguration();
			Rectangle gcBounds = dgc.getBounds();
			screenXoffs[i] = gcBounds.x;
			screenYoffs[i] = gcBounds.y;
		}
		// Get size of each screen
		for (int i = 0; i < gs.length; i++) {
			DisplayMode dm = gs[i].getDisplayMode();
			screenWidth[i] = dm.getWidth();
			screenHeight[i] = dm.getHeight();
			screenBitDepth[i] = dm.getBitDepth();
			screenRefreshRate[i] = dm.getRefreshRate();
			screenSize[i] = new Dimension(screenWidth[i], screenHeight[i]);
		}
		primaryScreen = getDefaultScreen();
	}

	/**
	 * returns number of display screen available on current platform.
	 * 
	 * @return number of screen
	 */
	public int getNumberOfScreens() {
		return gs.length;
	}

	/**
	 * returns an array of all available screen widths in the default graphics configuration.
	 * 
	 * @return array of width
	 */
	public int[] getScreenWidth() {
		return screenWidth;
	}

	/**
	 * returns an array of all available screen heights in the default graphics configuration.
	 * 
	 * @return array of height
	 */
	public int[] getScreenHeight() {
		return screenHeight;
	}

	/**
	 * returns an array of all available screen sizes in the default graphics configuration.
	 * 
	 * @return the array of sizes
	 */
	public Dimension[] getScreenSize() {
		return screenSize;
	}

	/**
	 * returns the width of the specified screen in its default graphics configuration.
	 * 
	 * @param index
	 * @return the width of the screen
	 */
	public int getScreenWidth(int index) {
		return screenWidth[index];
	}

	/**
	 * returns the height of the specified screen in its default graphics configuration.
	 * 
	 * @param index
	 * @return the height of the screen.
	 */
	public int getScreenHeight(int index) {
		return screenHeight[index];
	}

	/**
	 * returns the size of the specified screen in its default graphics configuration.
	 * 
	 * @param index
	 * @return screen dimension
	 */
	public Dimension getScreenSize(int index) {
		return screenSize[index];
	}

	/**
	 * returns the X offset of the specified screen in the default graphics configuration.
	 * 
	 * @param index
	 * @return X offset of the screen
	 */
	public int getScreenXoffset(int index) {
		return screenXoffs[index];
	}

	/**
	 * returns the Y offset of the specified screen in the default graphics configuration.
	 * 
	 * @param index
	 * @return Y offset of the screen
	 */
	public int getScreenYoffset(int index) {
		return screenYoffs[index];
	}

	/**
	 * returns the default screen GraphicsDevice
	 * 
	 * @return the default screen
	 */
	public GraphicsDevice getDefaultScreen() {
		return ge.getDefaultScreenDevice();
	}

	/**
	 * set the primary screen by index
	 * 
	 * @param index
	 */
	public void setPrimaryScreen(int index) {
		primaryScreen = gs[index];
	}

	/**
	 * returns the primary screen
	 * 
	 * @return the primary screen as GraphicsDevice
	 */
	public GraphicsDevice getPrimaryScreen() {
		return primaryScreen;
	}

}
