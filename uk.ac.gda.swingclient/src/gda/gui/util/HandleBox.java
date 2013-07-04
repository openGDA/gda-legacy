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

import java.awt.Cursor;

/**
 * Class for handle box:
 *   Each handle box is defined for its corner coordinates as
 *   an array of four integers: left x, top y, right x, bottom y
 *  
 */
public class HandleBox {
	private int[] handleCoords = null;
	Cursor myCursor = null;
	
	protected static int[] cursorsList = { Cursor.NW_RESIZE_CURSOR, Cursor.NE_RESIZE_CURSOR,
			Cursor.SW_RESIZE_CURSOR, Cursor.SE_RESIZE_CURSOR,
			Cursor.N_RESIZE_CURSOR, Cursor.W_RESIZE_CURSOR,
			Cursor.E_RESIZE_CURSOR, Cursor.S_RESIZE_CURSOR , Cursor.HAND_CURSOR
	};

	/**
	 * Get cursor shape
	 * 
	 * @return cursor shape
	 */
	public Cursor getMyCursor() {
		return myCursor;
	}

	/**
	 * Set the cursor shape according to list defined above
	 * 
	 * @param index index
	 */
	public void setMyCursor(int index) {
		if (index < cursorsList.length) {
			myCursor = new Cursor(cursorsList[index]);
		}
	}

	/**
	 * @return array of corner coordinate
	 */
	public int[] getHandleCoords() {
		return handleCoords;
	}

	/**
	 * Set coordinates of corners of handle box
	 * 
	 * @param handleCoords array of coordinates
	 */
	public void setHandleCoords(final int[] handleCoords) {
		this.handleCoords = handleCoords;
	}

	/**
	 * @param index index
	 * @param sx sx
	 * @param sy sy
	 * @param ex ex
	 * @param ey ey
	 */
	public HandleBox(int index, int sx, int sy, int ex, int ey) {
		if (index < cursorsList.length) {
			myCursor = new Cursor(cursorsList[index]);
			handleCoords = new int [] {sx, sy, ex, ey };
		}
	}

	/**
	 * @param x x
	 * @param y y
	 * @return boolean
	 */
	public boolean isInBox(int x, int y) {
		return (x >= handleCoords[0] && x < handleCoords[2] &&
					y >= handleCoords[1] && y < handleCoords[3]);
	}
}
