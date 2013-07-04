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
import gda.util.CommandId;

import org.jscience.physics.quantities.Quantity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * class to carry out a move to command on a DOF
 */

public class RelativeMove extends DOFCommand {
	private static final Logger logger = LoggerFactory.getLogger(RelativeMove.class);

	/**
	 * @param dof
	 * @param quantity
	 */
	public RelativeMove(Moveable dof, Quantity quantity) {
		super(dof, quantity);
	}

	@Override
	public void execute() throws MoveableException {
		int check = MoveableStatus.SUCCESS;
		int id = CommandId.next();

		logger.debug("\n\nExecuting the move, id = " + id);

		// check the move and if it is alright then start it
		// If successful the DOF/moveable will be locked for this (ie.
		// RelativeMove)
		// @see update for unlocking of this.
		if ((check = getDof().checkMoveBy(getQuantity(), this)) == MoveableStatus.SUCCESS) {
			getDof().doMove(this, id);
		} else {
			String errorMessage = MoveableStatus.mapIntToMessage(check);
			throw new MoveableException(new MoveableStatus(check, getDof().getName(), getQuantity()),
					"Relative move of DOF " + getDof().getName() + " not allowed: " + errorMessage);
		}
	}
}
