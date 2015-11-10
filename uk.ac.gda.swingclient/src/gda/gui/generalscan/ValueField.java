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

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;

import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A JTexField which displays and allows editing of a value from a ValueModel.
 */

public class ValueField extends JTextField implements ActionListener, DocumentListener, ValueDisplayer {
	
	private static final Logger logger = LoggerFactory.getLogger(ValueField.class);
	
	private ValueModel valueModel;

	private String valueName;

	private String confirmedValue;

	private Color normalBackground;

	private Color unconfirmedBackground = Color.ORANGE;

	private NumberFormat nf = NumberFormat.getInstance();

	/**
	 * Constructor.
	 * 
	 * @param valueModel
	 *            the valueModel whose value this displays
	 * @param valueName
	 *            the name of the value which this displays
	 */
	public ValueField(ValueModel valueModel, String valueName) {
		this(valueModel, valueName, 8);
	}

	/**
	 * Constructor.
	 * 
	 * @param valueModel
	 *            the valueModel whose value this displays
	 * @param valueName
	 *            the name of the value which this displays
	 * @param columns
	 */
	public ValueField(ValueModel valueModel, String valueName, int columns) {
		super(columns);
		normalBackground = getBackground();

		this.valueModel = valueModel;
		this.valueName = valueName;

		// ValueField listens for its own actions, listens for mouse actions on
		// itself,
		// listens for changes to its contents and observes its valueModel.

		addActionListener(this);
		valueModel.addIObserver(this); //FIXME: potential race condition
		addMouseListener(ValueFieldMouseInputAdapter.getInstance());
		getDocument().addDocumentListener(this); //FIXME: potential race condition

		// The default number of decimal places.
		nf.setMinimumFractionDigits(3);
		nf.setMaximumFractionDigits(3);

		display();
	}

	/**
	 * Implements the ActionListener interface. Called when user presses RETURN or ENTER in the ValueField. Sets the
	 * value in the model to what this currently displays
	 * 
	 * @param ae
	 *            the ActionEvent causing the call
	 */
	@Override
	public void actionPerformed(ActionEvent ae) {
		logger.debug("ValueField actionPerformed called");
		String value = getText();
		valueModel.setValue(valueName, value);
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
	private void display() {
		String newValue = valueModel.getValue(valueName);
		setText(newValue);
		confirmedValue = newValue;
		decorate();
	}

	/**
	 * Implements DocumentListener interface. Called if the text changes.
	 * 
	 * @param e
	 *            document event
	 */
	@Override
	public void insertUpdate(DocumentEvent e) {
		decorate();
	}

	/**
	 * Implements DocumentListener interface. Called if the text changes.
	 * 
	 * @param e
	 *            document event
	 */
	@Override
	public void removeUpdate(DocumentEvent e) {
		decorate();
	}

	/**
	 * Implements DocumentListener interface. Called if the text changes.
	 * 
	 * @param e
	 *            document event
	 */
	@Override
	public void changedUpdate(DocumentEvent e) {
		decorate();
	}

	/**
	 * Decorates according to current state.
	 */
	private void decorate() {
		if (getText().equals(confirmedValue)) {
			setBackground(normalBackground);
		} else {
			setBackground(unconfirmedBackground);
		}
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

	/**
	 * Sets the number of decimal places
	 * 
	 * @param decimalPlaces
	 *            the new number of places
	 */
	public void setDecimalPlaces(int decimalPlaces) {
		nf.setMinimumFractionDigits(decimalPlaces);
		nf.setMaximumFractionDigits(decimalPlaces);
	}
}
