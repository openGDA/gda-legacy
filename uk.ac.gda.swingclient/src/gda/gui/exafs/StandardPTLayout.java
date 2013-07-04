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
 * A class which implements PTLayout to produce a standard shaped PeriodicTable with separate sub-table for lanthanides
 * and actinides.
 */

public class StandardPTLayout implements PTLayout {
	private static int MAIN = 0;

	private static int SUBSIDIARY = 1;

	private int subsidiaryTableVOffset = 1;

	private int subsidiaryTableHOffset = 3;

	/**
	 * Returns whether or not element of given atomicNumber is a lanthanide.
	 * 
	 * @param atomicNumber
	 *            the atomic number of the element
	 * @return true if lanthanide, false otherwise
	 */
	private static boolean isLanthanide(int atomicNumber) {
		return (atomicNumber >= 58 && atomicNumber <= 71);
	}

	/**
	 * Returns whether or not element of given atomicNumber is an actinnide.
	 * 
	 * @param atomicNumber
	 *            the atomic number of the element
	 * @return true if actinide, false otherwise
	 */
	private static boolean isActinide(int atomicNumber) {
		return (atomicNumber >= 90 && atomicNumber <= 103);
	}

	/**
	 * Returns which table a particular element should be in. (This layout has a main table and a susidiary
	 * actinide/lanthanide table.)
	 * 
	 * @param atomicNumber
	 *            the atomic number of the element
	 * @return SUBSIDIARY if actinide or lanthanide, MAIN otherwise
	 */
	private static int getTable(int atomicNumber) {
		if (isLanthanide(atomicNumber) || isActinide(atomicNumber)) {
			return (SUBSIDIARY);
		}
		return (MAIN);
	}

	/**
	 * 
	 */
	public StandardPTLayout() {
	}

	/* Methods to implement PTLayout */

	/**
	 * Gets gridY value (y position in GridBagLayout) for given element
	 * 
	 * @param element
	 *            the element
	 * @return grid Y value to use
	 */
	@Override
	public int getGridY(Element element) {
		int atomicNumber = element.getAtomicNumber();
		int row;

		if (atomicNumber > 0 && atomicNumber <= 2)
			row = 0;
		else if (atomicNumber >= 3 && atomicNumber <= 10)
			row = 1;
		else if (atomicNumber >= 11 && atomicNumber <= 18)
			row = 2;
		else if (atomicNumber >= 19 && atomicNumber <= 36)
			row = 3;
		else if (atomicNumber >= 37 && atomicNumber <= 54)
			row = 4;
		else if (atomicNumber >= 55 && atomicNumber <= 86) {
			if (isLanthanide(atomicNumber))
				row = 6 + subsidiaryTableVOffset;
			else
				row = 5;
		} else if (atomicNumber >= 87 && atomicNumber <= 103) {
			if (isActinide(atomicNumber))
				row = 6 + 1 + subsidiaryTableVOffset;
			else
				row = 6;
		} else
			row = -1;

		return (row);
	}

	/**
	 * Gets gridX value (x position in GridBagLayout) for given element
	 * 
	 * @param element
	 *            the element
	 * @return grid X value to use
	 */
	@Override
	public int getGridX(Element element) {
		int atomicNumber = element.getAtomicNumber();
		int table = getTable(atomicNumber);
		int row = getGridY(element);
		int column;

		if (table == SUBSIDIARY) {
			if (row == 6 + subsidiaryTableVOffset)
				column = atomicNumber - 58 + subsidiaryTableHOffset;
			else
				column = atomicNumber - 90 + subsidiaryTableHOffset;
		} else {
			switch (row) {
			case 0:
				column = (atomicNumber - 1) * 17;
				break;
			case 1:
				if (atomicNumber <= 4)
					column = atomicNumber - 3;
				else
					column = atomicNumber + 7;
				break;
			case 2:
				if (atomicNumber <= 12)
					column = atomicNumber - 11;
				else
					column = atomicNumber - 1;
				break;
			case 3:
				column = atomicNumber - 19;
				break;
			case 4:
				column = atomicNumber - 37;
				break;
			case 5:
				if (atomicNumber <= 57)
					column = atomicNumber - 55;
				else
					column = atomicNumber - 69;
				break;
			case 6:
				if (atomicNumber <= 89)
					column = atomicNumber - 87;
				else
					column = atomicNumber - 101;
				break;
			default:
				column = -1;
				break;
			}
		}
		return (column);
	}

	@Override
	public boolean includeElement(Element element) {
		return true;
	}
}
