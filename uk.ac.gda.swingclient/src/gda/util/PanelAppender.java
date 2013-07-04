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

package gda.util;

import java.awt.Color;

/**
 * Interface implemented by panels that display log messages.
 */
public interface PanelAppender {
	
	/**
	 * Appends text in the specified colour.
	 * 
	 * @param text the text
	 * @param color the text colour
	 */
	public void appendText(String text, Color color);

	/**
	 * Appends text later in the specified colour.
	 * 
	 * @param text the text
	 * @param color the text colour
	 */
	public void appendTextLater(String text, Color color);
	
	/**
	 * Prepends text in the specified colour.
	 * 
	 * @param text the text
	 * @param color the text colour
	 */
	public void prependText(String text, Color color);
	
}
