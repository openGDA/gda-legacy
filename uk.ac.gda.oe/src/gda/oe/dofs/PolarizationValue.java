/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

package gda.oe.dofs;

import gda.util.QuantityFactory;

import org.jscience.physics.quantities.Quantity;

/**
 */
public class PolarizationValue {
	// Multiples of 180 from 360 Degrees upwards are used to represent the
	// fixed values of polarization.
	private static String[] strings = { "none", "none", "LeftCircular", "RightCircular", "Horizontal", "Vertical (M+)",
			"Vertical (M-)", "Vertical (O+)", "Vertical (O-)" };

	/**
	 * @param value
	 * @return String of value
	 */
	public static String doubleToString(double value) {
		String rtrn = null;
		if (Math.abs(value) <= 180.0) {
			rtrn = String.valueOf(value);
		} else {
			rtrn = strings[(int) Math.round(value) / 180];
		}
		return rtrn;
	}

	/**
	 * @param string
	 * @return double of string
	 */
	public static Double stringToDouble(String string) {
		Double value = null;
		boolean stringFound = false;
		for (int i = 0; i < strings.length; i++)
			if (strings[i].equals(string)) {
				value = new Double(180.0 * i);
				stringFound = true;
			}

		if (stringFound == false) {
			value = Double.valueOf(string);
		}
		return value;
	}

	/**
	 * @param string
	 * @return Quantity from String
	 */
	public static Quantity stringToQuantity(String string) {
		return QuantityFactory.createFromTwoStrings("" + stringToDouble(string), "Deg");
	}

	/**
	 * @param value
	 * @return Quantity from double
	 */
	public static Quantity doubleToQuantity(double value) {
		return stringToQuantity(doubleToString(value));
	}
}