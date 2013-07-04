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

/**
 * DoubleColumn Class
 * 
 * @param <T>
 */
abstract public class DoubleColumn<T extends Column<Double, T>> extends Column<Double, T> implements ValidityChecker,
		UnitsProvider {
	protected final String units, formatString;

	private final gda.gui.text.Formatter.SimpleFormatter formatter;

	/**
	 * @param s
	 * @param units
	 * @param formatString
	 */
	public DoubleColumn(String s, String units, String formatString) {
		this(fromString(s), units, formatString);
	}
	protected static Double fromString(String s){
		return s.length() == 0 ? 0 : Double.valueOf(s.trim());
	}
	/**
	 * @param val
	 * @param units
	 * @param formatString
	 */
	public DoubleColumn(Double val, String units, String formatString) {
		super(val);
		this.units = units;
		this.formatString = formatString;
		formatter = new gda.gui.text.Formatter.SimpleFormatter(formatString);
	}
	
	@Override
	public String getUnits() {
		return units;
	}

	/**
	 * @return format string
	 */
	public String getFormatString() {
		return formatString;
	}

	@Override
	public String toString() {
		try {
			return formatter.valueToString(val);
		} catch (java.text.ParseException e) {
			return e.getMessage();
		} catch (java.util.MissingFormatWidthException e) {
			return e.getMessage();
		} catch (java.util.MissingFormatArgumentException e) {
			return e.getMessage();
		}
	}
}
