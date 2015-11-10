/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package gda.gui.generalscan;

import javax.swing.JTextField;

/**
 * A JTextField which displays a value from a ValueModel. Compare ValueField which can both display and edit a value. A
 * JTextField is used rather than a JLabel because the value still stands out from the panel background.
 */

public class ValueLabel extends JTextField implements ValueDisplayer {
	private ValueModel valueModel;

	private String valueName;

	/**
	 * Constructor.
	 * 
	 * @param valueModel
	 *            the valueModel whose value this displays
	 * @param valueName
	 *            the name of the value which this displays
	 */
	public ValueLabel(ValueModel valueModel, String valueName) {
		this.valueModel = valueModel;
		this.valueName = valueName;

		/* Observe the model */
		valueModel.addIObserver(this); //FIXME: potential race condition

		// Setting enabled rather than editable to false makes the
		// field look better. Setting the disabledTextColor to the
		// standard foreground makes it look better still.
		setEnabled(false);
		setDisabledTextColor(getForeground());
		setColumns(6);

		display();
	}

	/**
	 * Implements the IObserver interface. If the ValueModel is notifying a change then call the display method.
	 * 
	 * @param theObserved
	 *            the observed class
	 * @param theArgument
	 *            the change code
	 */
	@Override
	public void update(Object theObserved, Object theArgument) {
		if (theObserved instanceof ValueModel)
			display();
	}

	/**
	 * Implements the ValueDisplayer interface. Gets the value from the model and displays it.
	 */
	public void display() {
		setText(valueModel.getValue(valueName));
	}

	/**
	 * @param newModel
	 */
	public void setModel(ValueModel newModel) {
		valueModel.deleteIObserver(this);
		valueModel = newModel;
		valueModel.addIObserver(this);
		display();
	}
}