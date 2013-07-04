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
 * StringColumn abstract class
 * 
 * @param <T>
 */
public abstract class StringColumn<T extends Column<String, T>> extends Column<String, T> {

	/**
	 * @param s
	 * @param maxLen
	 */
	public StringColumn(String s, int maxLen) {
		super(s != null ? (maxLen == 0 || s.length() <= maxLen ? s : s.substring(0, maxLen - 1)) : "");
	}

	/**
	 * @param s
	 */
	public StringColumn(String s) {
		this(s, 0);
	}
}
