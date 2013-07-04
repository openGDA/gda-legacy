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

import java.awt.Color;
import java.awt.Component;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * EditWhenSelectedRenderer Class
 */
public final class EditWhenSelectedRenderer extends DefaultTableCellRenderer {

	boolean addUnits=false;
	JCheckBox checkBox;
	private Color normalForegroundColor;

	/**
	 * Constructor
	 */
	public EditWhenSelectedRenderer() {
		super();
	}

	@Override
	@SuppressWarnings("unchecked")
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		if (isSelected && hasFocus) {
			table.editCellAt(row, column);
		}
		Component comp = null;
		if (value instanceof BooleanColumn) {
			if (checkBox == null) {
				checkBox = new JCheckBox();
			}
			checkBox.setSelected(((BooleanColumn<? extends BooleanColumn>) value).getIsSelected());
			checkBox.setHorizontalAlignment(SwingConstants.CENTER);
			checkBox.invalidate();
			comp = checkBox;
		} else {
			comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			if (comp instanceof JLabel) {
				((JLabel) comp).setHorizontalAlignment(CENTER);

				String label = value.toString();
				if ( isAddUnits() && value instanceof UnitsProvider) {
					label += " " + ((UnitsProvider) value).getUnits();
				}
				((JLabel) comp).setText(label);
			}

			if (value instanceof ValidityChecker && comp instanceof JComponent) {
				if (normalForegroundColor == null)
					normalForegroundColor = ((JComponent) comp).getForeground();
				if (!((ValidityChecker) value).isValid(table.getModel(), row)) {
					((JComponent) comp).setForeground(Color.RED);// .setBackground(Color.RED);
				} else {
					((JComponent) comp).setForeground(normalForegroundColor);
				}
			}

		}
		return comp;
	}

	/**
	 * @return Returns the addUnits.
	 */
	public boolean isAddUnits() {
		return addUnits;
	}

	/**
	 * @param addUnits The addUnits to set.
	 */
	public void setAddUnits(boolean addUnits) {
		this.addUnits = addUnits;
	}
}
