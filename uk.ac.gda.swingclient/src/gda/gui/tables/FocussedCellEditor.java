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
import java.awt.Toolkit;

import javax.swing.AbstractCellEditor;
import javax.swing.CellEditor;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellEditor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class FocussedCellEditor extends AbstractCellEditor implements CellEditor, TableCellEditor {
	
	/**
	 * Returns True if the editor checks validity of the new value 
	 * @return True if the editor checks validity of the new value 
	 */
	public boolean isCheckValidity() {
		return checkValidity;
	}

	/**
	 * Controls whether the editor checks validity of the new value 
	 * @param checkValidity
	 */
	public void setCheckValidity(boolean checkValidity) {
		this.checkValidity = checkValidity;
	}

	private static final Logger logger = LoggerFactory.getLogger(FocussedCellEditor.class);
	
	final JLabel tipField;

	final FactoryFromString factory;

	int iRow;

	JTable _table;

	Color foreground, selectTextColour;
	boolean checkValidity = false;

	FocussedCellEditor(FactoryFromString factory, JLabel tipField) {
		super();
		this.factory = factory;
		this.tipField = tipField;
	}

	Object createValueObject(String s) {
		try {
			return factory.createObject(s);
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return null;
	}

	abstract String getValue();

	abstract void setInvalidate();

	abstract Component getComponent();

	@Override
	public Object getCellEditorValue() {
		return createValueObject(getValue());
	}

	@Override
	public boolean stopCellEditing() {
		String s = getValue();
		Object valObject = createValueObject(s);
		String invalidityReason = s + " is invalid. ";
		if (valObject != null) {
			if (checkValidity && valObject instanceof ValidityChecker) {
				ValidityChecker checker = (ValidityChecker) valObject;
				if (checker.isValid(_table.getModel(), iRow)) {
					return super.stopCellEditing();
				}

				invalidityReason = checker.reasonForInvalidity(_table.getModel(), iRow);

			} else {
				return super.stopCellEditing();
			}
		}
		try {
			Toolkit.getDefaultToolkit().beep();
			JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(getComponent()), invalidityReason,
					"Invalid", JOptionPane.WARNING_MESSAGE);
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		setInvalidate();
		return false;
	}

}
