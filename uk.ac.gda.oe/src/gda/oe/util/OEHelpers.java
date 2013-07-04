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

package gda.oe.util;

import gda.factory.Finder;
import gda.oe.OE;

/**
 *
 */
public class OEHelpers {
	/**
	 * @param oeName
	 * @param dofName
	 * @return OE
	 */
	static public OE getOEForDOF(String oeName, String dofName) {
		Object o = Finder.getInstance().find(oeName);
		if (o == null)
			throw new IllegalArgumentException("OEHelpers.getOEForDOF. unable to find " + oeName);
		if (!(o instanceof OE)) {
			throw new IllegalArgumentException("OEHelpers.getOEForDOF. " + oeName + " is not an OE");
		}
		OE oe = (OE) o;
		String names[] = oe.getDOFNames();
		boolean found = false;
		for (String name : names) {
			if (name.equals(dofName)) {
				found = true;
				break;
			}
		}
		if (!found) {
			throw new IllegalArgumentException("OEHelpers.getOEForDOF. OE:" + oeName + " does not contain dof " + dofName);
		}
		return oe;
	}	
}
