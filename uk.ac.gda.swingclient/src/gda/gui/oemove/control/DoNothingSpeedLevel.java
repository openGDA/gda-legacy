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

package gda.gui.oemove.control;

import gda.gui.oemove.DOFSpeedLevel;

import java.util.ArrayList;

import javax.swing.JLabel;

/**
 * The implementation of DOFSpeedLevel for DOFs which do not allow speed changes. The interface-implementations-factory
 * business is a bit over the top for the simple speedLevel case but this was used as a test bed for the more
 * complicated position and input cases.
 * 
 * @see DOFSpeedLevel
 */
public class DoNothingSpeedLevel extends JLabel implements DOFSpeedLevel {
	/**
	 * The DoNothingSpeedLevel is simply an empty JLabel
	 */
	public DoNothingSpeedLevel() {
		super("");
	}

	/**
	 * Returns 0 always (this may not be correct - perhaps there should be some special DO_NOT_CHANGE value)
	 * 
	 * @return always returns 0
	 */
	@Override
	public int getSpeedLevel() {
		return 0;
	}

	/**
	 * Does nothing on purpose
	 * 
	 * @param newSpeedLevel
	 *            the speedLevel to set
	 */
	@Override
	public void setSpeedLevel(int newSpeedLevel) {
		// Does nothing deliberately
	}

	@Override
	public void setSpeedNames(ArrayList<String> speedNames) {
	}
}
