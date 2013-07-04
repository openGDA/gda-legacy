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

package gda.gui.oemove;

import java.awt.event.ActionListener;

/**
 * DOFInputDisplay interface
 */
public interface DOFInputDisplay {
	/**
	 * 
	 */
	public static String NOTSAVEABLE = "Input value not saveable";

	/**
	 * @param value
	 */
	public void setValue(String value);

	/**
	 * @return value
	 */
	public Double getValue();

	/**
	 * @param newMode
	 */
	public void setMode(int newMode);

	/**
	 * @param al
	 */
	public void addActionListener(ActionListener al);

	/**
	 * @param al
	 */
	public void removeActionListener(ActionListener al);

	/**
	 * @param b
	 */
	public void setEnabled(boolean b);

	/**
	 * @param cmd
	 */
	public void setActionCommand(String cmd);
}