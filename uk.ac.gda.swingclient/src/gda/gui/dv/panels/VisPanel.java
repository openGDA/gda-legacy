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

package gda.gui.dv.panels;

import javax.swing.JPanel;

/**
 * Base class for all panels which are to be in the side panel which affect the visualisation should inherit from.
 */
public class VisPanel extends JPanel {

	protected MainPlot owner = null;

	/**
	 * Constructor which points the standard panel at the main plot to which it references
	 * 
	 * @param main
	 *            The main plot which this panel affects.
	 */
	public VisPanel(MainPlot main) {
		owner = main;
	}

}
