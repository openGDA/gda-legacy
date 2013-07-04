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

package gda.util;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.EventObject;

import javax.swing.DefaultCellEditor;
import javax.swing.JTable;
import javax.swing.table.TableModel;

/**
 * Standardises the editing behaviour of tables. Sets up Integer, Double and String editors with the following
 * properties. Renders the cell to be left aligned during editing (same as default rendering so no change seen). Allows
 * editing to begin with one click. Completes editing and updates model on cell focus lost. For other types of cell
 * users must create their own editors which should be based on these.
 */
public class StandardJTable extends JTable {
	private LeftRenderer leftRenderer = null;

	private DecimalField doubleField = null;

	private DecimalField integerField = null;

	private DefaultCellEditor doubleEditor = null;

	private DefaultCellEditor integerEditor = null;

	/**
	 * Constructor
	 */
	public StandardJTable() {
		configureTable();
	}

	/**
	 * Constructor with table model.
	 * 
	 * @param tm
	 */
	public StandardJTable(TableModel tm) {
		super(tm);
		configureTable();
	}

	private void configureTable() {
		setUpIntegerEditor();
		setUpDoubleEditor();
		leftRenderer = new LeftRenderer(doubleField.getDecimalFormat());

		setDefaultRenderer(String.class, leftRenderer);
		setDefaultRenderer(Double.class, leftRenderer);
		setDefaultRenderer(Integer.class, leftRenderer);

		// Ensures table updates data model when cells are exited, EVEN when
		// tabbed to!
		putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

	}

	private void setUpDoubleEditor() {
		doubleField = new DecimalField(0.0, 8, 1);
		doubleEditor = new DefaultCellEditor(doubleField) {
			/**
			 * Override DefaultCellEditor's getCellEditorValue method to return an Integer, not a String: {@inheritDoc}
			 * 
			 * @see javax.swing.DefaultCellEditor#getCellEditorValue()
			 */
			@Override
			public Object getCellEditorValue() {
				return new Double(doubleField.getValue());
			}

			/**
			 * Override DefaultCellEditor's isCellEditable method to make cell editable by a single click{@inheritDoc}
			 * 
			 * @see javax.swing.DefaultCellEditor#isCellEditable(java.util.EventObject)
			 */
			@Override
			public boolean isCellEditable(EventObject event) {
				return true;
			}

			// Overrides the DefaultCellEditor's getTableCellEditorComponent
			// so that the formatting of the doubleField is used (so that
			// numbers to edit appear the same as numbers when not editing).
			@Override
			public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row,
					int column) {
				doubleField.setValue(((Double) value).doubleValue());
				return doubleField;
			}
		};
		// doubleField.addMouseListener(this);
		doubleField.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseExited(MouseEvent e) {
				doubleEditor.stopCellEditing();
			}
		});
		setDefaultEditor(Double.class, doubleEditor);
	}

	private void setUpIntegerEditor() {
		integerField = new DecimalField(8, 8, 1);
		integerEditor = new DefaultCellEditor(integerField) {
			// Override DefaultCellEditor's getCellEditorValue method
			// to return an Integer, not a String:
			@Override
			public Object getCellEditorValue() {
				return new Integer((int) integerField.getValue());
			}

			// Override DefaultCellEditor's isCellEditable method
			// to make cell editable by a single click
			@Override
			public boolean isCellEditable(EventObject event) {
				return true;
			}
		};
		// integerField.addMouseListener(this);
		integerField.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseExited(MouseEvent e) {
				integerEditor.stopCellEditing();
			}
		});
		setDefaultEditor(Integer.class, integerEditor);
	}
}
