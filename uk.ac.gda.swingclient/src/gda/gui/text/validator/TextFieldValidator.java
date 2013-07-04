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

package gda.gui.text.validator;

import java.awt.Component;

import javax.swing.JOptionPane;

/**
 * class used by the ParameterPanelBuilder class to validate user changes in text fields based on the limits of the
 * field If the value is invalid a JOptionPanel message dialog is displayed.
 * 
 * @param <V>
 */
public class TextFieldValidator<V extends Comparable<V>> {

	/**
	 * Constructor
	 */
	public TextFieldValidator() {
	}

	/**
	 * @param component
	 * @param value
	 * @param minimum
	 * @param maximum
	 * @param messageTitle
	 * @return true if valid
	 */
	public boolean isValid(Component component, V value, V minimum, V maximum, String messageTitle) {
		boolean ok = false;
		if (value.compareTo(minimum) < 0) {
			JOptionPane.showMessageDialog(component, "Value must not be less than " + minimum, messageTitle,
					JOptionPane.WARNING_MESSAGE);

		} else if (value.compareTo(maximum) > 0) {
			JOptionPane.showMessageDialog(component, "Value must not be greater than " + maximum, messageTitle,
					JOptionPane.WARNING_MESSAGE);

		} else {
			ok = true;
		}
		return ok;
	}

}
