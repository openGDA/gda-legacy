/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

import javax.swing.AbstractCellEditor;
import javax.swing.CellEditor;
import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellEditor;

/**
 * {@link CellEditor} for {@link BooleanColumn}s.
 */
public class BooleanColumnEditor extends AbstractCellEditor implements TableCellEditor {

	final JCheckBox checkBox = new JCheckBox();
	
	{
		checkBox.setHorizontalAlignment(SwingConstants.CENTER);
	}
	
	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		BooleanColumn<?> booleanValue = (BooleanColumn<?>) value;
		checkBox.setSelected(booleanValue.getIsSelected());
		return checkBox;
	}

	@Override
	public Object getCellEditorValue() {
		return checkBox.isSelected();
	}

}
