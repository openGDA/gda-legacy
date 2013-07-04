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
 * A class which displays an OE presentation inside an internal frame.
 */

public interface Representation {
	/**
	 * Return the complete list of Representations.
	 * 
	 * @return an arrayList of Representations
	 */
	public ArrayList<Representation> getRepresentationList();

	/**
	 * Add a Representation to the list of Representations.
	 * 
	 * @param representation
	 *            the representation to be added.
	 */
	public void addRepresentation(Representation representation);

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
	 * @return the thumbnail icon
	 */
	public String getThumbNail();

	/**
	 * @return the x position
	 */
	public int getXPosition();

	/**
	 * @param x
	 *            set the x position
	 */
	public void setXPosition(int x);

	/**
	 * @return the y position
	 */
	public int getYPosition();

	/**
	 * @param y
	 *            set the Y position
	 */
	public void setYPosition(int y);

	/**
	 * @return the frame height
	 */
	public int getFrameHeight();

	/**
	 * @return the frame width
	 */
	public int getFrameWidth();

	/**
	 * @return the component which is display in the internal frame
	 */
	public JComponent getDisplayComponent();

	/**
	 * @return the control panel component
	 */
	public JComponent getControlComponent();

	/**
	 * @return true if the representation is displayed at startup
	 */
	public boolean isShowAtStartup();

	/**
	 * @param representation
	 *            the parent of this representation
	 */
	public void setParent(Representation representation);

	/**
	 * @param resizeable
	 *            disable/enable resizability
	 */
	public void setResizeable(boolean resizeable);

	/**
	 * @return true if resizable
	 */
	public boolean isResizeable();

	/**
	 * @param width
	 *            set the frame width
	 */
	public void setFrameWidth(int width);

	/**
	 * @param height
	 *            set the frame height
	 */
	public void setFrameHeight(int height);

	/**
	 * @param editable
	 *            disable/enable editability
	 */
	public void setEditable(boolean editable);

	/**
	 * @return true if editable
	 */
	public boolean isEditable();

	/**
	 * @return the protection level
	 */
	public int getProtectionLevel();

	/**
	 * @param protectionLevel
	 *            set the protection level (Castor implementation)
	 */
	public void setProtectionLevel(int protectionLevel);
}
