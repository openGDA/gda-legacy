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
 * performs an absolute move for the specified DOF after checking and locking
 */
public class AbsoluteMove extends DOFCommand {
	private static final Logger logger = LoggerFactory.getLogger(AbsoluteMove.class);
	private Object locker = null;

	public AbsoluteMove(Moveable dof, Quantity quantity) {
		super(dof, quantity);
		locker = this;
	}

	@Override
	public void execute() throws MoveableException {

		int check = MoveableStatus.SUCCESS;
		int id = CommandId.next();

		logger.debug("AbsoluteMove.Execute() executing the move id = " + id + " for " + getDof().getName() + " to "
				+ getQuantity());

		// check the move and if it is alright then start it
		if ((check = getDof().checkMoveTo(getQuantity(), locker)) == MoveableStatus.SUCCESS) {
			executing = true;
			boolean moveStarted = false;
			MoveableException moveableException = null;
			try {
				getDof().doMove(locker, id);
				moveStarted = true;
			} catch (MoveableException e) {
				moveableException = e;
			} finally {
				// For all exceptions update the mover to unlock the moveables.
				// need to handle AccessDeniedException and RuntimeExceptions
				if (!moveStarted) {
					MoveableStatus ms = moveableException != null ? moveableException.getMoveableStatus()
							: new MoveableStatus(check, getDof().getName(), getQuantity());
					update(getDof(), ms);
				}
			}
		} else {
			executing = false;
			MoveableStatus ms = new MoveableStatus(check, getDof().getName(), getQuantity());
			update(getDof(), ms);
			String errorMessage = MoveableStatus.mapIntToMessage(check);
			throw new MoveableException(ms, "Move of DOF " + getDof().getName() + " not allowed: " + errorMessage);
		}
	}
}