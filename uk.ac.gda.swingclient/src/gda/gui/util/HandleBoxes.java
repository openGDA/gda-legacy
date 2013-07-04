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

import gda.gui.util.HandleBox;

/**
 * Grouping handle boxes to a List
 *
 */
public class HandleBoxes {
	int handleside = 10; // length of side of handle boxes

	List<HandleBox> handleBoxList = null;
	int handlethickness = 2;

	int current = -1; // current selected handle box
	HandleBox currentBox = null;


	/**
	 * Create list
	 */
	public HandleBoxes() {
		handleBoxList = new ArrayList<HandleBox>();
	}

	/**
	 * @return length of handle box side
	 */
	public int getHandleside() {
		return handleside;
	}

	/**
	 * @return thickness of handle box outline
	 */
	public int getHandlethickness() {
		return handlethickness;
	}

	/**
	 * @return index of currently selected handle box
	 */
	public int getCurrent() {
		return current;
	}

	/**
	 * @return currently selected handle box
	 */
	public HandleBox getCurrentBox() {
		return currentBox;
	}

	
	/**
	 * @param coords is list of corner coordinates of handle boxes
	 * 
	 */
	public void addHandleBoxes(int[] coords) {
		handleBoxList.clear();
		for (int i = 0; i<coords.length; i+=4) {
			handleBoxList.add(i/4,
					new HandleBox(i/4,coords[i], coords[i+1], coords[i+2], coords[i+3]));
		}
	}

	/**
	 * @return List of handle boxes
	 */
	public List<HandleBox> getHandleBoxList() {
		return handleBoxList;
	}

	/**
	 * Checks whether current coordinates are in a list of boxes
	 * 
	 * @param x x
	 * @param y y
	 */
	public void whichHandleBox(int x, int y) {
		current = -1;
		currentBox = null;
		if (!handleBoxList.isEmpty()) {
			for (HandleBox hb: handleBoxList) {
				if (hb.isInBox(x, y)) {
					current = handleBoxList.indexOf(hb);
					currentBox = hb;
					break;
				}
			}
		}
	}
}