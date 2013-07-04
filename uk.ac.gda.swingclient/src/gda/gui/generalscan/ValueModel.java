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

import gda.observable.IObservable;

/**
 * An interface which allows the getting and setting of values by name. Meant to be analagous to the way JTable works
 * with values (which are Objects) being set and got by row and column. Extends IObservable so that ValueModels are also
 * forced to be IObservable
 */
public interface ValueModel extends IObservable {
	/**
	 * Should return the named value as a string
	 * 
	 * @param name
	 *            the name of the value to get
	 * @return the value for this name
	 */
	public String getValue(String name);

	/**
	 * Should set the named value using the string value
	 * 
	 * @param name
	 *            the name of the value to set
	 * @param value
	 *            the new value to set for this name
	 */
	public void setValue(String name, String value);
}
