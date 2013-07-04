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

import java.io.StringWriter;
import java.text.DecimalFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a single region of a scan.
 */

public class TimeRegion {
	private static final Logger logger = LoggerFactory.getLogger(TimeRegion.class);

	private double start;

	private double end;

	protected double time;

	protected int points;

	/**
	 * 
	 */
	public static final int END = 0;

	/**
	 * 
	 */
	public static final int TIME = 1;

	/**
	 * 
	 */
	public static final int POINTS = 2;

	/**
	 * @param end
	 * @param time
	 * @param points
	 */
	public TimeRegion(double end, double time, int points) {
		this.start = 0.0;
		this.time = time;
		this.end = end;
		this.points = points;
	}

	/**
	 * Sets a value.
	 * 
	 * @param value
	 *            new values (as a String)
	 * @param valueNumber
	 *            which value to set
	 */
	public void setValue(Object value, int valueNumber) {

		switch (valueNumber) {
		case END:
			setEnd(((Double) value).doubleValue());
			break;
		case TIME:
			setTime(((Double) value).doubleValue());
			break;
		case POINTS:
			// setNumberOfPoints(Integer.parseInt((String) value));
			setNumberOfPoints(((Integer) value).intValue());
			break;
		default:
			logger.error("Error in TimeRegion setValue");
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
		case END:
			value = new Double(getEnd());
			break;
		case TIME:
			value = new Double(getTime());
			break;
		case POINTS:
			value = new Integer(getNumberOfPoints());
			break;
		default:
			logger.error("Error in TimeRegion getValue");
			break;
		}

		return value;
	}

	/**
	 * Sets the end value
	 * 
	 * @param newValue
	 *            the new value
	 */
	public void setEnd(double newValue) {
		end = newValue;
		calculateNumberOfPoints();
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
	 * Sets the time
	 * 
	 * @param newValue
	 *            the new value
	 */
	public void setTime(double newValue) {
		time = newValue;
		calculateEnd();
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
	 * Sets the number of points
	 * 
	 * @param newValue
	 *            the new value
	 */
	public void setNumberOfPoints(int newValue) {
		points = newValue;
		calculateEnd();
	}

	/**
	 * Gets the number of points
	 * 
	 * @return the number of points
	 */
	public int getNumberOfPoints() {
		return points;
	}

	/**
	 * Calculates the number of points
	 */
	private void calculateNumberOfPoints() {
		points = (int) ((end - start) / time);
	}

	/**
	 * Calculates the increment
	 */
	private void calculateEnd() {
		end = start + points * time;
	}

	/**
	 * Returns command to do a scan for this region
	 * 
	 * @return String
	 */
	public String getInterpreterCommand() {
		StringWriter sw = new StringWriter();
		DecimalFormat f = new DecimalFormat();
		f.applyPattern("0.0");
		f.setMinimumFractionDigits(3);
		f.setMaximumFractionDigits(3);

		sw.write("TimeScan(");
		sw.append(new Integer(points).toString());
		sw.append(",");
		sw.append(f.format(0.0));
		sw.append(",");
		sw.append(f.format(time));
		sw.append(")");

		sw.flush();

		String command = sw.toString();
		logger.debug("getInterpreterCommand return " + command);

		return command;
	}
}
