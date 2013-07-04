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

package gda.gui.exafs;

import gda.util.exafs.Element;

/**
 * Interface which must be implemented by classes claiming to know how to layout a PeriodicTable.
 */

public interface PTLayout {
	/**
	 * Returns a suitable GridBagConstraints gridx value
	 * 
	 * @param element
	 *            the element to be laid out
	 * @return GridBagConstraints gridx value
	 */
	public int getGridX(Element element);

	/**
	 * Returns a suitable GridBagConstraints gridy value
	 * 
	 * @param element
	 *            the element to be laid out
	 * @return GridBagConstraints gridy value
	 */
	public int getGridY(Element element);

	/**
	 * @param element
	 * @return boolean
	 */
	public boolean includeElement(Element element);
}
