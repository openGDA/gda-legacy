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
 * BooleanColumn Class
 * 
 * @param <T>
 */
public abstract class BooleanColumn<T extends Column<Boolean, T>> extends Column<Boolean, T> {
	/**
	 * @param s
	 */
	public BooleanColumn(String s) {
		super(fromString(s));
	}

	/**
	 * @param b
	 */
	public BooleanColumn(Boolean b) {
		super(b);
	}

	protected static Boolean fromString(String s){
		return s.length() == 0 ? false : Boolean.valueOf(s.trim());
	}		
	
	/**
	 * @return boolean
	 */
	public boolean getIsSelected() {
		return val;
	}
}
