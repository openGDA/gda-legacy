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

import gda.oe.MoveableException;
import gda.oe.MoveableStatus;
import gda.oe.OE;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import org.jscience.physics.quantities.Quantity;
import org.jscience.physics.units.Unit;

/**
 * General Scan Model to provide a data model for scan
 */
public class GeneralScanModel extends ScanModel implements Serializable {
	private String[] columnNames = { "Region", "Start", "End", "Increment", "Time(mSec)", "Steps" };

	/* Most of the workings involve manipulating an array of Regions */
	/* to provide an implementation of AbstractTableModel for the JTable */
	/* used to display them. */

	private ArrayList<Region> regionList;

	private double totalTime;

	private int totalPoints;

	private String dofName;

	private String detectorName;

	// private Object header;

	private boolean addTime = true;

	private boolean valid = false;

	/* These are the names of the values which are accessible using */
	/* the ValueModel interface. */

	/**
	 * 
	 */
	public static final String TOTALPOINTS = "totalPoints";

	/**
	 * 
	 */
	public static final String TOTALTIME = "totalTime";

	/**
	 * Constructor.
	 * 
	 * @param dofName
	 *            the DOF name
	 * @param detectorName
	 *            the Detector name
	 */
	public GeneralScanModel(String dofName, String detectorName) {
		regionList = new ArrayList<Region>();
		setDofName(dofName);
		setDetectorName(detectorName);
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
		return columnNames.length;
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

		// Region otherRegion;

		if (row != 0 && col == Region.START) {
			/* also change the end of the previous region */
			// otherRegion = regionList.get(row - 1);
			// FIXME: temporarily removed - whether or not to do
			// this should be optional
			// otherRegion.setValue(value, Region.END);
			// fireTableRowsUpdated(row - 1, row - 1);
		}

		if (row != getRowCount() - 1 && col == Region.END) {
			/* also change the start of the next region */
			// otherRegion = regionList.get(row + 1);
			// FIXME: temporarily removed - whether or not to do
			// this should be optional
			// otherRegion.setValue(value, Region.START);
			// fireTableRowsUpdated(row + 1, row + 1);
		}

		calculateTotalTimeAndPoints();
		if (col != Region.TIME)
			valid = false;
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
		/* Editing of the name is never allowed. */
		return !(col == Region.NAME);
	}

	/* end of AbstractTableModel methods */

	/**
	 * Sets the display units.
	 * 
	 * @param units
	 *            the new display units
	 */
	public void setDisplayUnits(Unit<? extends Quantity> units) {
		for (Region region : regionList) {
			region.setDisplayUnits(units);
		}
		fireTableDataChanged();
	}

	/**
	 * Sets the dof name
	 * 
	 * @param dofName
	 *            the new dofname
	 */
	public void setDofName(String dofName) {
		this.dofName = dofName;

		for (Region region : regionList) {
			region.setDofName(dofName);
		}
		fireTableDataChanged();
	}

	/**
	 * Sets the detector name
	 * 
	 * @param detectorName
	 *            the new counter timer name
	 */
	public void setDetectorName(String detectorName) {
		this.detectorName = detectorName;

		for (Region region : regionList) {
			region.setDetectorName(detectorName);
		}
		fireTableDataChanged();
	}

	/**
	 * gets the dof name
	 * 
	 * @return the dof name
	 */
	public String getDofName() {
		return regionList.get(0).getDofName();
	}

	/**
	 * Adds a new row (at the end).
	 */
	public void addRow() {
		int row = regionList.size() - 1;

		Region lastRegion = regionList.get(row);

		// newRegion has same number of points and increment as lastRegion
		// but starts one increment on from where it ends (and so ends number of
		// points + 1 increments from end of lastRegion)
		Region newRegion = new Region(String.valueOf(row + 1), lastRegion.getEnd() + lastRegion.getIncrement(),
				lastRegion.getEnd() + lastRegion.getIncrement() * lastRegion.getSteps() + lastRegion.getIncrement(),
				lastRegion.getIncrement(), lastRegion.getTime(), lastRegion.getSteps(), lastRegion.getDisplayUnits(),
				dofName, detectorName);

		addRegion(newRegion);
	}

	/**
	 * @param newRegion
	 *            add a new region
	 */
	public void addRegion(Region newRegion) {
		int n = regionList.size();

		logger.debug("calling fireTableRowsInserted with " + n);
		regionList.add(newRegion);
		fireTableRowsInserted(n, n);

		calculateTotalTimeAndPoints();
		valid = false;
		notifyIObservers(this, null);
	}

	/**
	 * Deletes last row
	 */
	public void deleteLastRow() {
		int row = regionList.size() - 1;

		regionList.remove(row);
		fireTableRowsDeleted(row, row);

		calculateTotalTimeAndPoints();
		notifyIObservers(this, null);
	}

	/**
	 * 
	 */
	private void calculateTotalTimeAndPoints() {
		totalPoints = 0;
		totalTime = 0.0;

		for (Region region : regionList) {
			totalPoints += region.getSteps() + 1;
			totalTime += region.getTotalTime();
		}

		/* totalTime is in seconds rather than milliseconds */
		totalTime = totalTime / 1000.0;
	}

	@Override
	public int getTotalPoints() {
		return totalPoints;
	}

	/**
	 * @return the total time
	 */
	@Override
	public double getTotalTime() {
		return totalTime;
	}

	/**
	 * @param header
	 *            The data file header to set.
	 */
	// public void setHeader(Object header)
	// {
	// // FIXME why is this method required. header is unused.
	// // this.header = header;
	// }
	/**
	 * 
	 */
	public void clear() {
		regionList.clear();
	}

	/**
	 * @param currentPosition
	 */
	@SuppressWarnings("unchecked")
	public void setDefaultScan(Quantity currentPosition) {
		Region region;

		// Lets try 5% either side of the current position
		double position = currentPosition.getAmount();
		double start = position - (position * 0.05);
		double end = position + (position * 0.05);
		int steps = 100;
		double time = 1000.0;
		double increment = (end - start) / steps;

		/* clear out the existing regions */
		regionList.clear();

		// Cant return a typesafe unit from a quantity as the method does not
		// exist
		// yet in the JScience classes. Hence @suppressed warnings.
		Unit<? extends Quantity> unit = currentPosition.getUnit();
		region = new Region("0", start, end, increment, time, steps, unit, dofName, detectorName);

		/* add the new region to the list and inform the table */

		regionList.add(region);
		fireTableRowsInserted(0, 0);

		/* recalculate the totals and inform IObservers */
		calculateTotalTimeAndPoints();
		notifyIObservers(this, null);
	}

	/**
	 * Creates a suitable default scan for the given currentPosition
	 * 
	 * @param currentPosition
	 */
	@Override
	public void setDefaultScan(double currentPosition) {

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

		command += "stepScan = MultiRegionScan();";

		for (Region region : regionList) {
			command += "stepScan.addScan(" + region.getInterpreterCommand(addTime) + ");";
		}
		return command;
	}

	/**
	 * @param addTime
	 */
	public void setAddTime(boolean addTime) {
		this.addTime = addTime;
	}

	/**
	 * Returns whether or not the scan is valid.
	 * 
	 * @return scan validity
	 */
	@Override
	public boolean getValid() {
		/* More checking needed here obviously. */
		return valid;
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

	/**
	 * @param oe
	 * @return int
	 */
	public int check(OE oe) {
		int scanOK = MoveableStatus.ERROR;
		try {
			for (Region region : regionList) {
				Quantity position = Quantity.valueOf(region.getStart(), region.getDisplayUnits());

				if ((scanOK = oe.moveCheck(dofName, position)) != MoveableStatus.SUCCESS)
					break;
				for (int j = 0; j < region.getSteps(); j++) {
					position = position.plus(Quantity.valueOf(region.getIncrement(), region.getDisplayUnits()));

					if ((scanOK = oe.moveCheck(dofName, position)) != MoveableStatus.SUCCESS)
						break;
				}
				if (scanOK != MoveableStatus.SUCCESS)
					break;
			}
		} catch (MoveableException e) {
			logger.error("GeneralScanModel check() exception " + e.getMessage());
			scanOK = MoveableStatus.ERROR;
		}
		logger.debug("GeneralScanModel check() returning " + scanOK);
		if (scanOK != MoveableStatus.SUCCESS) {
			valid = false;
			JOptionPane.showMessageDialog(null, "Scan contains invalid region paramters");
		} else {
			valid = true;
		}
		return scanOK;
	}
}
