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

import gda.factory.Configurable;

import javax.swing.JComponent;

/**
 * An interface for plugins for OEMove
 * <p>
 * Extends Configurable as all Pluggables are assumed to be Configurable in the OEMove code.
 */
public interface Pluggable extends Configurable {
	/**
	 * Returns the GUI component which displays the details of what this plugin represents
	 * 
	 * @return JComponent
	 */
	public JComponent getDisplayComponent();

	/**
	 * Return the GUI component which holds the controls for this plugin
	 * 
	 * @return JComponent
	 */
	public JComponent getControlComponent();
}
