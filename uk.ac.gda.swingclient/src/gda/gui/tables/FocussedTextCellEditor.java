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

package gda.gui.tables;

import gda.gui.ValidatingDocument;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

/**
 * FocussedTextCellEditor Class
 */
final public class FocussedTextCellEditor extends FocussedCellEditor {

	JTextField textField;

	/**
	 * @param factory
	 * @param tipField
	 */
	public FocussedTextCellEditor(FactoryFromString factory, JLabel tipField) {
		super(factory, tipField);
	}

	@Override
	String getValue() {
		return textField.getText();
	}

	@Override
	void setInvalidate() {
		textField.setForeground(Color.RED);
		textField.setSelectedTextColor(Color.RED);
		textField.invalidate();
	}

	@Override
	Component getComponent() {
		return textField;
	}

	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		// TODO Auto-generated method stub
		if (textField == null) {
			textField = new JTextField();
			foreground = textField.getForeground();
			selectTextColour = textField.getSelectedTextColor();
			textField.addFocusListener(new SelectTextOnFocus());
			textField.setHorizontalAlignment(SwingConstants.CENTER);
			
			ValidatingDocument document = new ValidatingDocument();
			if (hasMaximumLength) {
				document.setMaximumLength(maximumLength);
			}
			if (characterValidator != null) {
				document.setCharacterValidator(characterValidator);
			}
			textField.setDocument(document);
		}
		textField.setForeground(foreground);
		textField.setSelectedTextColor(selectTextColour);
		textField.setText(value.toString());
		if (value instanceof ToolTip)
			textField.setToolTipText(((ToolTip) value).getToolTip(table.getModel(), row));
		tipField.setText(textField.getToolTipText());
		iRow = row;
		_table = table;
		return textField;
	}
	
	private boolean hasMaximumLength;
	
	private int maximumLength;
	
	public void setMaximumLength(int maximumLength) {
		this.maximumLength = maximumLength;
		hasMaximumLength = true;
	}
	
	private CharacterValidator characterValidator;
	
	public void setCharacterValidator(CharacterValidator validator) {
		this.characterValidator = validator;
	}
	
}
