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

package gda.oe.positioners;

import java.io.Serializable;

/**
 * 
 */
public class PositionalValues implements Serializable {
	// bugzilla bug #829 added to make independent of version & class changes.
	static final long serialVersionUID = 271524613371028663L;

	// private double position = Double.NaN;
	private double upperLimit = Double.NaN;
	private double lowerLimit = Double.NaN;
	private double homeOffset = Double.NaN;
	private double positionOffset = Double.NaN;

	/**
	 * @return lowerLimit
	 */
	public double getLowerLimit() {
		return lowerLimit;
	}

	/**
	 * @param lLimit
	 */
	public void setLowerLimit(double lLimit) {
		lowerLimit = lLimit;
	}

	/**
	 * @return upperLimit
	 */
	public double getUpperLimit() {
		return upperLimit;
	}

	/**
	 * @param uLimit
	 */
	public void setUpperLimit(double uLimit) {
		upperLimit = uLimit;
	}

	/**
	 * @return homeOffset
	 */
	public double getHomeOffset() {
		return homeOffset;
	}

	/**
	 * @param hOffset
	 */
	public void setHomeOffset(double hOffset) {
		homeOffset = hOffset;
	}

	/**
	 * @return positionOffset
	 */
	public double getPositionOffset() {
		return positionOffset;
	}

	/**
	 * @param pOffset
	 */
	public void setPositionOffset(double pOffset) {
		positionOffset = pOffset;
	}

}
