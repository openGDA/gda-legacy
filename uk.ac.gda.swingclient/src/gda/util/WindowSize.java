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

import gda.configuration.properties.LocalProperties;

import java.awt.Dimension;
import java.awt.Toolkit;

/**
 * A class for setting window sizes based on screen size.
 */
public class WindowSize extends Dimension {
	/**
	 * Determine the window size and calculate a fractional size of it.
	 * 
	 * @param fraction
	 *            a fractional size change.
	 */
	public WindowSize(double fraction) {
		int nosOfScreens = LocalProperties.getInt("gda.gui.nosOfScreens", 1);
		String displayingScreen = LocalProperties.get("gda.gui.displayingScreen", "top");
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		width = screenSize.width;
		height = screenSize.height;
		if (nosOfScreens == 2) {
			if ("bottom".equalsIgnoreCase(displayingScreen) || "top".equalsIgnoreCase(displayingScreen)) {
				height = screenSize.height / 2;
			} else if ("right".equalsIgnoreCase(displayingScreen) || "left".equalsIgnoreCase(displayingScreen)) {
				width = screenSize.width / 2;
			}
		} else if (nosOfScreens == 4) {
			height = screenSize.height / 2;
			width = screenSize.width / 2;

		}

		height = (int) (height * fraction);
		width = (int) (width * fraction);
	}
}
