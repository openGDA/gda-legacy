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

package gda.oe.commands;

import gda.oe.MoveableException;

/**
 * Classes which need to be informed of MoveableExceptions which occur during MultipleMoves should implement this
 * interface and call addWatcher on the MultipleMove passing themselves. This mechanism is simply another
 * Observer/Observable but it seemed less confusing to reimplements it separately.
 */
public interface MultipleMoveWatcher {
	/**
	 * @param visitor
	 * @param me
	 */
	public void inform(MultipleMove visitor, MoveableException me);
}
