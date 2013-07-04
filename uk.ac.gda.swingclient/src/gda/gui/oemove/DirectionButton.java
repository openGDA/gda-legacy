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

import java.awt.Dimension;

import javax.swing.JButton;

/**
 * This class provides button functionality for the ContinuousMove Mode.
 * <p>
 * By Default this button is disabled, it is only enabled if mode of movement selected is "Continuous".
 * <p>
 * It is set to represent a direction of continuous movement.
 */
public class DirectionButton extends JButton {
	private int direction;

	private boolean selected;

	/**
	 * The constructor takes a string parameter.
	 * 
	 * @param direction
	 *            this button represents(positive or negative)
	 */
	public DirectionButton(int direction) {
		super();
		setEnabled(false);
		this.direction = direction;
	}

	/**
	 * Gets the preferred size of this component.
	 * 
	 * @return the dimension containing the preferred size of the component
	 */
	@Override
	public Dimension getPreferredSize() {
		return new Dimension(20, 20);
	}

	/**
	 * Gets the direction.
	 * 
	 * @return the direction
	 */
	public int getDirection() {
		return direction;
	}

	/**
	 * Return a boolean specifying if this component current state.
	 * 
	 * @return true if the button is selected
	 */
	@Override
	public boolean isSelected() {
		return selected;
	}

	/**
	 * Sets this component selected
	 * 
	 * @param selected
	 *            if true enables the button
	 */
	@Override
	public void setSelected(boolean selected) {
		this.selected = selected;
	}
}
