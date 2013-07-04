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

import java.awt.Component;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;

/**
 * FocussedCheckBoxCellEditor Class
 */
public final class FocussedCheckBoxCellEditor extends FocussedCellEditor {
	/**
	 * @param factory
	 * @param tipField
	 */
	public FocussedCheckBoxCellEditor(FactoryFromString factory, JLabel tipField) {
		super(factory, tipField);
	}

	JCheckBox field;

	@Override
	String getValue() {
		return Boolean.toString(field.isSelected());
	}

	@Override
	void setInvalidate() {
	}

	@Override
	Component getComponent() {
		return field;
	}

	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		// TODO Auto-generated method stub
		if (field == null) {
			field = new JCheckBox("", new Boolean(value.toString()));
			field.setHorizontalAlignment(SwingConstants.CENTER);
		}
		field.setSelected(new Boolean(value.toString()));
		if (value instanceof ToolTip)
			field.setToolTipText(((ToolTip) value).getToolTip(table.getModel(), row));
		tipField.setText(field.getToolTipText());
		iRow = row;
		_table = table;
		return field;
	}
}
