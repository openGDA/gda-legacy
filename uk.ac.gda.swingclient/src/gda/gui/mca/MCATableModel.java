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

package gda.gui.mca;

import javax.swing.table.AbstractTableModel;

/**
 * MCATableModel Class
 */
public class MCATableModel extends AbstractTableModel {

	private static final long serialVersionUID = 1L;

	private String[] columnNames;

	private Object[][] data;

	private Class<?>[] columnTypes;

	private boolean editable[];

	/**
	 * @param colNames
	 * @param colTypes
	 * @param rowCount
	 */
	public MCATableModel(String[] colNames, Class<?>[] colTypes, int rowCount) {
		columnNames = colNames;
		columnTypes = colTypes;
		data = new Object[rowCount][colNames.length];
	}

	@Override
	public int getColumnCount() {
		return columnNames.length;
	}

	@Override
	public int getRowCount() {
		return data.length;
	}

	/**
	 * @param rowCount
	 */
	public void setRowCount(int rowCount) {
		Object[][] newData = new Object[rowCount][columnNames.length];
		int arrayLength = (data.length >= rowCount ? rowCount : data.length);
		for (int i = 0; i < arrayLength; i++) {
			for (int j = 0; j < columnNames.length; j++) {
				newData[i][j] = data[i][j];
			}
		}
		data = newData;
		fireTableStructureChanged();
	}

	@Override
	public String getColumnName(int col) {
		return columnNames[col];
	}

	@Override
	public Object getValueAt(int row, int col) {
		return data[row][col];
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Class getColumnClass(int c) {
		return (columnTypes != null) ? columnTypes[c] : String.class;
	}

	@Override
	public boolean isCellEditable(int row, int col) {
		// Note that the data/cell address is constant,
		// no matter where the cell appears onscreen.
		return editable[col];
	}

	@Override
	public void setValueAt(Object value, int row, int col) {
		data[row][col] = value;
		fireTableCellUpdated(row, col);
	}

	/**
	 * @param editable
	 */
	public void setEditValues(boolean[] editable) {
		this.editable = editable;

	}

}
