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

package gda.gui.oemove;

import java.util.ArrayList;

/**
 * This is the interface which must be implemented by Objects which allow the displaying and setting of DOFSpeedLevels.
 * The interface-implementations- factory business is a bit over the top for the simple speedLevel case but this was
 * used as a test bed for the more complicated position and input cases.
 */
public interface DOFSpeedLevel {
	/**
	 * @return This should return the current speed level (should be one of Motor.SLOW, Motor.MEDIUM, Motor.FAST - can
	 *         this be forced by interface specification?)
	 */
	public int getSpeedLevel();

	/**
	 * This should allow the speed level to be changed to a new value
	 * 
	 * @param newSpeedLevel
	 */
	public void setSpeedLevel(int newSpeedLevel);

	/**
	 * @param enabled
	 */
	public void setEnabled(boolean enabled);

	/**
	 * @param speedNames
	 */
	public void setSpeedNames(ArrayList<String> speedNames);
}
