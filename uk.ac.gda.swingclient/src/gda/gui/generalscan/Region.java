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

import gda.jscience.physics.quantities.PhotonEnergy;
import gda.jscience.physics.quantities.Wavelength;

import java.io.StringWriter;
import java.text.DecimalFormat;

import org.jscience.physics.quantities.Angle;
import org.jscience.physics.quantities.Energy;
import org.jscience.physics.quantities.Length;
import org.jscience.physics.quantities.Quantity;
import org.jscience.physics.units.Unit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a single region of a scan.
 */
public class Region {
	private static final Logger logger = LoggerFactory.getLogger(Region.class);

	private String name;

	private double start;

	private double end;

	private double increment;

	private double time;

	private int steps;

	private Unit<? extends Quantity> displayUnits = null;

	private String dofName = null;

	private String detectorName = null;

	/* integers which can be used to set and get the various values */
	/**
	 * 
	 */
	public static final int NAME = 0;

	/**
	 * 
	 */
	public static final int START = 1;

	/**
	 * 
	 */
	public static final int END = 2;

	/**
	 * 
	 */
	public static final int INCREMENT = 3;

	/**
	 * 
	 */
	public static final int TIME = 4;

	/**
	 * 
	 */
	public static final int STEPS = 5;

	/**
	 * Constructor to create an Experiment Region for Scan
	 * 
	 * @param name
	 *            region name
	 * @param start
	 *            start value
	 * @param end
	 *            end value
	 * @param increment
	 *            increment value
	 * @param time
	 *            the time spent for each increment
	 * @param steps
	 *            the experiment point number
	 * @param displayUnits
	 *            the region's displayUnits
	 * @param dofName
	 *            the Dof name
	 * @param detectorName
	 *            the Detector name
	 */
	public Region(String name, double start, double end, @SuppressWarnings("unused") double increment, double time, int steps,
			Unit<? extends Quantity> displayUnits, String dofName, String detectorName) {
		this.name = name;
		this.start = start;
		this.end = end;
		this.time = time;
		this.steps = steps;
		this.displayUnits = displayUnits;
		this.dofName = dofName;
		this.detectorName = detectorName;
		calculateIncrement();
	}

	/**
	 * Sets a value (called by JTable via GeneralScanModel). NB now that GeneralScanModel returns correct class in
	 * getColumnClass the value objects will be Strings or Doubles or Integers as appropriate.
	 * 
	 * @param value
	 *            new values which
	 * @param valueNumber
	 *            which value to set
	 */
	public void setValue(Object value, int valueNumber) {

		// NB Really should check class of value is correct in each case,
		// in case Region has been used in some other model than
		// GeneralScanModel.
		switch (valueNumber) {
		case START:
			setStart(((Double) value).doubleValue());
			break;
		case END:
			setEnd(((Double) value).doubleValue());
			break;
		case INCREMENT:
			setIncrement(((Double) value).doubleValue());
			break;
		case TIME:
			setTime(((Double) value).doubleValue());
			break;
		case STEPS:
			setSteps(((Integer) value).intValue());
			break;
		default:
			logger.error("Error in Region setValue");
			break;
		}
	}

	/**
	 * Gets a value
	 * 
	 * @param valueNumber
	 *            which value to get
	 * @return either a Double or an Integer
	 */
	public Object getValue(int valueNumber) {
		Object value = null;

		switch (valueNumber) {
		case NAME:
			value = name;
			break;
		case START:
			value = new Double(getStart());
			break;
		case END:
			value = new Double(getEnd());
			break;
		case INCREMENT:
			value = new Double(getIncrement());
			break;
		case TIME:
			value = new Double(getTime());
			break;
		case STEPS:
			value = new Integer(getSteps());
			break;
		default:
			logger.error("Error in Region setValue");
			break;
		}

		return value;
	}

	/**
	 * Sets the start value
	 * 
	 * @param start
	 *            the new value
	 */
	public void setStart(double start) {
		this.start = start;
		logger.debug("Region:setStart to " + start);
		calculateSteps();
	}

	/**
	 * Gets the start value
	 * 
	 * @return start value
	 */
	public double getStart() {
		return start;
	}

	/**
	 * Sets the end value
	 * 
	 * @param end
	 *            the new value
	 */
	public void setEnd(double end) {
		this.end = end;
		logger.debug("setEnd end is " + end);
		calculateSteps();
	}

	/**
	 * Gets the end value
	 * 
	 * @return end value
	 */
	public double getEnd() {
		return end;
	}

	/**
	 * Sets the increment
	 * 
	 * @param increment
	 *            the new value
	 */
	public void setIncrement(double increment) {
		// Even though this is a double comparison it is valid.
		// Its purpose is to stop users typing in increments of
		// 0.0
		if (increment != 0.0) {
			this.increment = increment;
			calculateSteps();
		}
	}

	/**
	 * Gets the increment
	 * 
	 * @return increment
	 */
	public double getIncrement() {
		return increment;
	}

	/**
	 * Sets the time
	 * 
	 * @param time
	 *            the new value
	 */
	public void setTime(double time) {
		this.time = time;
	}

	/**
	 * Gets the time value
	 * 
	 * @return time value
	 */
	public double getTime() {
		return time;
	}

	/**
	 * Gets the time value
	 * 
	 * @return time value
	 */
	public double getTotalTime() {
		return time * (steps + 1);
	}

	/**
	 * Sets the number of steps
	 * 
	 * @param steps
	 *            the new value
	 */
	public void setSteps(int steps) {
		// We do not allow negative numbers of steps - this stops user typing in
		// a
		// negative value.
		this.steps = steps < 0 ? -steps : steps;
		// Or 0 steps at all
		if (this.steps == 0)
			this.steps = 1;
		calculateIncrement();
	}

	/**
	 * Gets the number of steps
	 * 
	 * @return the number of steps
	 */
	public int getSteps() {
		return steps;
	}

	/**
	 * Gets the region displayUnits
	 * 
	 * @return region displayUnits
	 */
	public Unit<? extends Quantity> getDisplayUnits() {
		return displayUnits;
	}

	/**
	 * sets the region displayUnits
	 * 
	 * @param displayUnits
	 *            new displayUnitss
	 */
	public void setDisplayUnits(Unit<? extends Quantity> displayUnits) {
		logger.debug("Region setDisplayUnits called with " + displayUnits);
		start = convertTheDisplayedUnits(start, this.displayUnits, displayUnits);
		end = convertTheDisplayedUnits(end, this.displayUnits, displayUnits);
		increment = (end - start) / steps;
		this.displayUnits = displayUnits;
	}

	/**
	 * Calculates the number of steps
	 */
	private void calculateSteps() {
		if (increment != 0.0) {
			logger.debug(" changing steps from " + steps);
			steps = (int) ((end - start) / increment);
			// If steps comes out negative change signs to make steps
			// positive and
			// increment negative (fix for bug #184).
			if (steps < 0) {
				steps = -steps;
				increment = -increment;
			}
			logger.debug(" to " + steps);
		}
	}

	/**
	 * Calculates the increment
	 */
	private void calculateIncrement() {
		if (steps != 0) {
			logger.debug(" changing increment from " + increment);
			increment = ((end - start) / steps);
			logger.debug(" to " + increment);
		}
	}

	/**
	 * Returns command to do a scan for this region
	 * 
	 * @param includeTime
	 *            boolean to indicate whether the time should be included in the jython command
	 * @return String
	 */
	public String getInterpreterCommand(boolean includeTime) {
		StringWriter sw = new StringWriter();
		DecimalFormat f = new DecimalFormat();
		f.applyPattern("0.0");
		f.setMinimumFractionDigits(3);
		f.setMaximumFractionDigits(3);

		sw.write("GridScan(");
		sw.append(dofName);
		sw.append(",");
		sw.append(f.format(start));
		sw.append(",");
		sw.append(f.format(end));
		sw.append(",");
		sw.append(f.format(increment));
		if (includeTime) {
			sw.append(",");
			sw.append(f.format(time));
		}
		sw.append(",\"");
		sw.append(displayUnits.toString());
		sw.append("\")");

		sw.flush();

		String command = sw.toString();
		logger.debug("getInterpreterCommand return " + command);

		return command;
	}

	/**
	 * @param dofName
	 */
	public void setDofName(String dofName) {
		this.dofName = dofName;
	}

	/**
	 * @param detectorName
	 */
	public void setDetectorName(String detectorName) {
		this.detectorName = detectorName;
	}

	/**
	 * @return Returns the detectorName.
	 */
	public String getDetectorName() {
		return detectorName;
	}

	/**
	 * @return dof name
	 */
	public String getDofName() {
		return dofName;
	}

	/**
	 * Converts from display units to internal units
	 * 
	 * @param value
	 *            the value to convert
	 * @param oldUnits
	 *            the units to convert from
	 * @param newUnits
	 *            the units to convert to
	 * @return the converted value
	 */
	private double convertTheDisplayedUnits(double value, Unit<? extends Quantity> oldUnits,
			Unit<? extends Quantity> newUnits) {
		Quantity oldq = Quantity.valueOf(1.0, oldUnits);
		Quantity newq = Quantity.valueOf(1.0, newUnits);
		double d = 0.0;
		if (oldq instanceof Length && newq instanceof Length) {
			d = Quantity.valueOf(value, oldUnits).to(newUnits).getAmount();
		} else if (oldq instanceof Angle && newq instanceof Angle) {
			d = Quantity.valueOf(value, oldUnits).to(newUnits).getAmount();
		} else if (oldq instanceof Length && newq instanceof Energy) {
			Length w = (Length) Quantity.valueOf(value, oldUnits);
			d = PhotonEnergy.photonEnergyOf(w).to(newUnits).getAmount();
		} else if (oldq instanceof Energy && newq instanceof Length) {
			Energy e = (Energy) Quantity.valueOf(value, oldUnits);
			d = Wavelength.wavelengthOf(e).to(newUnits).getAmount();
		} else if (oldq instanceof Energy && newq instanceof Energy) {
			d = Quantity.valueOf(value, oldUnits).to(newUnits).getAmount();
		}
		return d;
	}
}