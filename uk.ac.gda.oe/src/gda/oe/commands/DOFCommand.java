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

import gda.observable.IObservable;
import gda.observable.IObserver;
import gda.oe.Moveable;
import gda.oe.MoveableCommandExecutor;
import gda.oe.MoveableException;
import gda.oe.MoveableStatus;
import gda.oe.dofs.DOF;

import org.jscience.physics.quantities.Quantity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for commands given to DOFs
 */

public abstract class DOFCommand implements IObserver, MoveableCommandExecutor {
	private static final Logger logger = LoggerFactory.getLogger(DOFCommand.class);
	private Quantity quantity;

	private DOF dof;

	protected volatile boolean executing = false;

	/**
	 * @param dof
	 * @param quantity
	 */
	public DOFCommand(Moveable dof, Quantity quantity) {
		this.dof = (DOF) dof;
		this.quantity = quantity;
	}

	/*
	 * returns the DOF (only needed by subclasses) @return dof the DOF
	 */
	protected DOF getDof() {
		return (dof);
	}

	/*
	 * returns the Quantity (only needed by subclasses) @return quantity the Quantity
	 */
	protected Quantity getQuantity() {
		return (quantity);
	}

	/*
	 * when they actually start the move or setPosition the sub classes set themselves as Observers of the DOF, when the
	 * MoveableStatus stops being busy (which could mean either a succesful or an unsuccesful move))
	 */

	@Override
	public void update(Object o, Object arg) {
		if (arg instanceof MoveableStatus) {
			MoveableStatus ds = (MoveableStatus) arg;
			logger.debug("received update for {} is {}", ((Moveable) o).getName(), ds.getMessage());

			if ((ds.value() != MoveableStatus.BUSY) && (ds.value() != MoveableStatus.AWAY_FROM_LIMIT)) {
				// if status is anything other than ready need to stop
				// the dof in case any other dofs/positioners are still
				// moving
				if (ds.value() != MoveableStatus.READY) {
					try {
						logger.debug("update stopping DOF");
						((Moveable) o).stop();
					} catch (MoveableException de) {
						logger.debug("!!!!! exception trying to stop");
					}
				}
				logger.debug("update releasing lock on {}", ((Moveable) o).getName());
				// Unlock the moveable using 'this'. @see the subclasses of
				// DOFCommand for locking
				((Moveable) o).unLock(this);
				((IObservable) o).deleteIObserver(this);
				executing = false;
				
			}

			// more should be done here obviously
			if (ds.value() == MoveableStatus.READY)
				logger.debug("Move command completed.");
			else if (ds.value() != MoveableStatus.BUSY)
				logger.error("Unsuccesful move, MoveableStatus was {}", ds.getMessage());

		}
	}

	@Override
	public String toString() {
		// (rdw) changed output due to formatting exception can be thrown by
		// quantity object
		return ("DOFCommand for " + super.toString() + " DOF " + dof.getName() + " and Quantity "
				+ quantity.getAmount() + " " + quantity.getUnit());
	}

	/**
	 * @return boolean
	 */
	public boolean isExecuting() {
		return executing;
	}

}
