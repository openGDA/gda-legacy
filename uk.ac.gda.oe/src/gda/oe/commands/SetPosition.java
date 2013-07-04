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

import gda.oe.Moveable;
import gda.oe.MoveableException;
import gda.oe.MoveableStatus;

import org.jscience.physics.quantities.Quantity;

/**
 * does a SetPosition for the specified DOF after checking and locking
 */

public class SetPosition extends DOFCommand {
	/**
	 * @param dof
	 * @param quantity
	 */
	public SetPosition(Moveable dof, Quantity quantity) {
		super(dof, quantity);
	}

	@Override
	public void execute() throws MoveableException {
		int check = MoveableStatus.SUCCESS;

		// check the move and if it is alright then start it
		if ((check = getDof().checkSetPosition(getQuantity(), this)) == MoveableStatus.SUCCESS) {
			getDof().doSet(this);
		} else {
			throw new MoveableException(new MoveableStatus(check, getDof().getName(), getQuantity()),
					"SetPosition.execute: getDof().checkSetPosition != SUCCESS. dof = " + getDof().getName()
							+ " quantity = " + getQuantity().toString());
		}
	}
}
