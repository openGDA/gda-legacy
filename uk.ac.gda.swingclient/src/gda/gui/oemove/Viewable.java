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

import java.util.ArrayList;

import javax.swing.JComponent;

/**
 * Viewable interface
 */
public interface Viewable {
	/**
	 * Return the complete list of Viewables.
	 * 
	 * @return an arrayList of viewables
	 */
	public ArrayList<Viewable> getViewableList();

	/**
	 * Add a viewable to the list of Viewables.
	 * 
	 * @param viewable
	 */
	public void addViewable(Viewable viewable);

	/**
	 * Set or change the name of the object (as defined in XML).
	 * 
	 * @param name
	 *            the object name
	 */
	public void setName(String name);

	/**
	 * Get the object name. Used by Castor to check if the object name has been set before calling the
	 * {@link  #setName(String)} method.
	 * 
	 * @return a String containing the object name.
	 */
	public String getName();

	/**
	 * @return JComponent
	 */
	public JComponent getDisplayComponent();

	/**
	 * @param viewable
	 */
	public void setParent(Viewable viewable);

	// public JComponent getControlComponent();
}
