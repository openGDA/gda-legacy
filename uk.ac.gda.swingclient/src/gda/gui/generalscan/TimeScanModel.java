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

import java.lang.reflect.Field;
import java.util.ArrayList;

/**
 * TimeScan Model to provide a data model for time scans
 */
public class TimeScanModel extends ScanModel {
	/**
	 * 
	 */
	public String[] columnNames = { "TotalTime", "Time(mSec)", "Points" };

	/* Most of the workings involve manipulating an array of Regions */
	/* to provide an implementation of AbstractTableModel for the JTable */
	/* used to display them. */

	protected ArrayList<TimeRegion> regionList;

	/* These are the names of the values which are accessible using */
	/* the ValueModel interface. */

	/**
	 * 
	 */
	public static final String TOTALNPOINTS = "totalNPoints";

	/**
	 * 
	 */
	public static final String TOTALTIME = "totalTime";

	/**
	 * Constructor.
	 */
	public TimeScanModel() {
		regionList = new ArrayList<TimeRegion>();
	}

	/* Methods to implement the AbstractTableModel interface */

	/**
	 * The JTable will call this to find out how many rows there are. This is abstract in AbstractTableModel.
	 * 
	 * @return number of rows
	 */
	@Override
	public int getRowCount() {
		return regionList.size();
	}

	/**
	 * The JTable will call this to find out how many columns there are. This is abstract in AbstractTableModel.
	 * 
	 * @return number of columns
	 */
	@Override
	public int getColumnCount() {
		return (columnNames.length);
	}

	/**
	 * The JTable will call this to find out what it should display at (row, col). This is abstract in
	 * AbstractTableModel.
	 * 
	 * @param row
	 * @param col
	 * @return the value at the given column
	 */
	@Override
	public Object getValueAt(int row, int col) {
		return regionList.get(row).getValue(col);
	}

	/**
	 * The JTable will call this to find a name for the given column. This overrides the method in AbstractTableModel.
	 * 
	 * @param col
	 *            the column
	 * @return name of the column
	 */
	@Override
	public String getColumnName(int col) {
		return columnNames[col];
	}

	/**
	 * The JTable will call this to determine what class of object appears in a particular column so that it can choose
	 * the correct editor.
	 * 
	 * @param c
	 *            the column in question
	 * @return the class of object stored in that column
	 */
	@Override
	public Class<? extends Object> getColumnClass(int c) {
		return getValueAt(0, c).getClass();
	}

	/**
	 * The JTable will call this if the user carries out an action which changes the value at (row, col). This overrides
	 * the method in AbstractTableModel.
	 * 
	 * @param value
	 *            an object to be set to the fixed table cell
	 * @param row
	 *            choosed table row
	 * @param col
	 *            choosed table column
	 */
	@Override
	public void setValueAt(Object value, int row, int col) {
		/* make the specified change */
		regionList.get(row).setValue(value, col);
		fireTableRowsUpdated(row, row);
		notifyIObservers(this, null);
	}

	/**
	 * The JTable will call this to determine whether a particular cell is editable.
	 * 
	 * @param row
	 * @param col
	 * @return true if the table cell is editable
	 */
	@Override
	public boolean isCellEditable(int row, int col) {
		return true;
	}

	/* end of AbstractTableModel methods */

	/**
	 * Clear the region list
	 */
	public void clear() {
		regionList.clear();
	}

	@Override
	public void setDefaultScan(double currentPosition) {
		TimeRegion region;

		/* clear out the existing regions */
		regionList.clear();

		region = new TimeRegion(100000.0, 1000.0, 100);

		/* add the new region to the list and inform the table */

		regionList.add(region);
		fireTableRowsInserted(0, 0);
		notifyIObservers(this, null);
	}

	/* methods to implement the CommandSupplier interface */

	/**
	 * Constructs a Jython command which will carry out the scan specified by this table's values.
	 * 
	 * @return the command
	 */
	@Override
	public String getCommand() {
		String command = "";

		command += "timeScan = MultiRegionScan();";

		for (int i = 0; i < regionList.size(); i++) {
			command = command + "timeScan.addScan(" + regionList.get(i).getInterpreterCommand() + ");";
		}
		return command;
	}

	/**
	 * Returns whether or not the scan is valid.
	 * 
	 * @return scan validity
	 */
	@Override
	public boolean getValid() {
		/* More checking needed here obviously. */
		return regionList.size() > 0;
	}

	/**
	 * Set the value of a field by giving its name and a String value suitable to set it.
	 * 
	 * @param name
	 *            the name of the field
	 * @param value
	 *            the value to set
	 * @throws NoSuchFieldException
	 * @throws IllegalAccessException
	 */
	@Override
	public void _setValue(String name, String value) throws NoSuchFieldException, IllegalAccessException {
		/* The ValueModel type values in this class are read only */
	}

	/**
	 * Get a string representation of the value of a field by giving its name.
	 * 
	 * @param name
	 *            the name of the field
	 * @return a String value of the field
	 * @throws NoSuchFieldException
	 * @throws IllegalAccessException
	 */
	@Override
	public String _getValue(String name) throws NoSuchFieldException, IllegalAccessException {
		String value = null;

		Field field = getClass().getDeclaredField(name);
		if (field.getType() == double.class)
			value = String.valueOf(field.getDouble(this));
		else
			value = String.valueOf(field.getInt(this));

		return value;
	}

	/**
	 * Recalculates after a ValueModel setValue type operation
	 */
	@Override
	public void reCalculate() {
		/* The ValueModel type values in this class are read only */
	}

	@Override
	public int getTotalPoints() {
		int totalNumberOfPoints = 0;

		for (TimeRegion r : regionList)
			totalNumberOfPoints += r.getNumberOfPoints();

		return totalNumberOfPoints;
	}

	@Override
	public double getTotalTime() {
		int totalTime = 0;

		for (TimeRegion r : regionList)
			totalTime += r.getNumberOfPoints() * r.getTime();

		/* totalTime is in seconds rather than milliseconds */
		totalTime = (int) (totalTime / 1000.0);
		return totalTime;
	}
}
