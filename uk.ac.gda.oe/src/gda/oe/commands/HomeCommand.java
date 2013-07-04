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
import gda.oe.Moveable;
import gda.oe.MoveableException;
import gda.oe.MoveableStatus;

import org.jscience.physics.quantities.Quantity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Does Home for the specified DOF after checking and locking The goal of the homing operation is to return the load to
 * a repeatable starting location. This is done by 1. Obtain the home position(physical Zero index)distance from the
 * desirable starting location from the user through xml or interface 2. Call home method in the motor which brings the
 * load to the home position 3. set the position to be equal to the distance obtained from step 1 Finally it notifies
 * its observers when the homing operation is complete
 * </p>
 */

public class HomeCommand extends DOFCommand implements Runnable {
	private static final Logger logger = LoggerFactory.getLogger(HomeCommand.class);

	private final int HOMING = 1;

	private final int SETTINGPOSITION = 2;

	private final int IDLE = 0;

	private int commandStatus = IDLE;

	private Thread offsetSetter;

	/**
	 * @param dof
	 * @param quantity
	 */
	public HomeCommand(Moveable dof, Quantity quantity) {
		super(dof, quantity);
	}

	@Override
	public void execute() throws MoveableException {
		int check = MoveableStatus.SUCCESS;

		// check the move and if it is alright then start it
		if ((check = getDof().checkHome(getQuantity(), this)) == MoveableStatus.SUCCESS) {
			commandStatus = HOMING;
			getDof().doHome(this);
			// new thread is started to wait for the motor
			// to reach the home position
			offsetSetter = uk.ac.gda.util.ThreadManager.getThread(this, getClass().getName());
			offsetSetter.start();

		} else {
			throw new MoveableException(new MoveableStatus(check, getDof().getName(), getQuantity()),
					"AbsoluteMove.execute: getDof().checkHome did not return SUCCESS");
		}
	}

	/*
	 * This was not in the pre merge release, so have commented it out public void deleteIObservers() {
	 * observableComponent.deleteIObservers(); } public void notifyIObservers(Object theObserver, Object theArgument) {
	 * observableComponent.notifyIObservers(theObserver, theArgument); }
	 */
	@Override
	public void update(Object o, Object arg) {

		if (arg instanceof MoveableStatus) {
			MoveableStatus ds = (MoveableStatus) arg;
			if ((ds.value() != MoveableStatus.BUSY)) {
				// in some cases the home position(physical zero index)
				// may be at one of the limits.
				// setting the position should be allowed , though setting
				// position at limits is not normally allowed.
				if (commandStatus == HOMING && ds.value() == MoveableStatus.READY
						|| ds.value() == MoveableStatus.AWAY_FROM_LIMIT || ds.value() == MoveableStatus.UPPERLIMIT
						|| ds.value() == MoveableStatus.LOWERLIMIT) {

					commandStatus = SETTINGPOSITION;
					synchronized (this) {
						notifyAll();
					}
				} else if (ds.value() != MoveableStatus.AWAY_FROM_LIMIT) {
					// if status is anything other than ready need to stop
					// the dof in case any other dofs/positioners are still
					// moving
					if (ds.value() != MoveableStatus.READY) {
						try {
							logger.debug("DOFCommand.update stopping DOF");
							((Moveable) o).stop();
						} catch (MoveableException de) {
							logger.debug("!!!!! exception trying to stop");
						}
					}
					logger.debug("DOFCommand.update releasing lock");
					commandStatus = IDLE;
					((Moveable) o).unLock(this);
					((IObservable) o).deleteIObserver(this);

					// deleteIObservers();
				}
			}

			if (commandStatus == SETTINGPOSITION)
				logger.debug("setting position and motor status" + ds.value());
			else if (commandStatus == HOMING)
				logger.debug("Homing and motor status" + ds.value());
			else
				logger.debug("Doing Nothing and motor status" + ds.value());

		}

	}

	@Override
	public void run() {
		synchronized (this) {
			// waits till the motor stops moving after reaching the
			// the home position
			try {
				wait();
			} catch (Exception ie) {
				logger.debug("caught Exception " + ie.getMessage());
			}
		}

		if (commandStatus == SETTINGPOSITION) {
			try {
				getDof().doSet(HomeCommand.this);
			} catch (Exception e) {
				logger.debug("exception in setting offset" + e.getMessage());
			}
		}

	}

}
