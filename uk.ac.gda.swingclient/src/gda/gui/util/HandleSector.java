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

import org.eclipse.dawnsci.analysis.dataset.coords.SectorCoords;

/**
 * Class for handle sector:
 *   Each handle sector is defined for its corner coordinates as
 *   an array of four doubles: 1st r, 1st phi, 2nd r, 2nd phi
 *   where r_1 < r_2 and phi_1 < phi_2
 */
public class HandleSector extends HandleBox {
	private double[] handleSectorCoords = null;

	/**
	 * 
	 * @param index index
	 * @param sr start radius
	 * @param sp start azimuthal (polar) angle
	 * @param er end radius
	 * @param ep end angle
	 */
	public HandleSector(int index, double sr, double sp, double er, double ep) {
		super(index, 0, 0, 0, 0);
		if (index < cursorsList.length) {
			this.handleSectorCoords = new double[] {sr, sp, er, ep };
		}
	}

	/**
	 * @return array of corner coordinate
	 */
	public double[] getHandleSectorCoords() {
		return handleSectorCoords;
	}

	/**
	 * Check to see if point defined in Cartesian coordinates is in handle box
	 * 
	 * @param x x
	 * @param y y
	 * @return boolean
	 */
	public boolean isInSector(int x, int y) {
		SectorCoords sc = new SectorCoords(x, y, true);
		double[] rp = sc.getPolar();

		return (rp[0] >= handleSectorCoords[0] && rp[0] < handleSectorCoords[2] &&
				rp[1] >= handleSectorCoords[1] && rp[1] < handleSectorCoords[3]);
	}
}
