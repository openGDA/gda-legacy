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

package gda.gui.util;


import java.util.ArrayList;
import java.util.List;

/**
 * Grouping handle sectors to a List
 * 
 */
public class HandleSectors extends HandleBoxes {
	List<HandleSector> handleSectorList = null;
	HandleSector currentSector = null;


	/**
	 * Create list
	 */
	public HandleSectors() {
		handleSectorList = new ArrayList<HandleSector>();
	}

	/**
	 * @return currently selected handle sector
	 */
	public HandleSector getCurrentSector() {
		return currentSector;
	}

	
	/**
	 * @param coords is list of corner coordinates of handle sectors
	 */
	public void addHandleSectors(double[] coords) {
		handleSectorList.clear();
		for (int i = 0; i<coords.length; i+=4) {
			handleSectorList.add(i/4,
					new HandleSector(i/4,coords[i], coords[i+1], coords[i+2], coords[i+3]));
		}
	}

	/**
	 * @return List of handle sectors
	 */
	public List<HandleSector> getHandleSectorList() {
		return handleSectorList;
	}

	/**
	 * Checks whether current coordinates (relative to centre mark)
	 *  are in a list of sectors
	 * 
	 * @param x x
	 * @param y y
	 */
	public void whichHandleSector(int x, int y) {
		current = -1;
		currentSector = null;
		if (!handleSectorList.isEmpty()) {
			for (HandleSector hb: handleSectorList) {
				if (hb.isInSector(x, y)) {
					current = handleSectorList.indexOf(hb);
					currentSector = hb;
					break;
				}
			}
		}
	}
}