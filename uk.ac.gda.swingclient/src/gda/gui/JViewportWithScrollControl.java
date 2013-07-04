/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

package gda.gui;

import java.awt.Rectangle;

import javax.swing.JViewport;

/**
 * A subclass of {@link JViewport} for which scrolling can be enabled/disabled.
 */
public class JViewportWithScrollControl extends JViewport {
	
	private boolean scrollingEnabled = true;
	
	public void setScrollingEnabled(boolean scrollingEnabled) {
		this.scrollingEnabled = scrollingEnabled;
	}
	
	@Override
	public void scrollRectToVisible(Rectangle contentRect) {
		if (scrollingEnabled) {
			super.scrollRectToVisible(contentRect);
		}
	}
}
