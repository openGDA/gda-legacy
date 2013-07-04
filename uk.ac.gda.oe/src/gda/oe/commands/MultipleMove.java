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
import gda.oe.util.UndulatorMoveCalculator.Move;
import gda.util.CommandId;

import java.util.ArrayList;

import org.jscience.physics.quantities.Quantity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Does a multiple move for the specified DOF with checking and locking
 */
public class MultipleMove extends DOFCommand implements Runnable {
	private static final Logger logger = LoggerFactory.getLogger(MultipleMove.class);

	private volatile boolean doingALeg;

	private Moveable[] toBeMoved;

	private ArrayList<Move> moves;

	private volatile boolean flagOne;

	private volatile boolean flagTwo;

	private volatile boolean multipleMovesInProgress;

	private int counter = 0;

	private Thread runner;

	private int commandID;

	private MultipleMoveWatcher watcher;

	private volatile boolean legSucceeded = true;

	private MoveableException toBeThrownAsSoonAsPossible;

	/**
	 * Creates a move command for multiple moves for a single DOF. Only works at present for Undulator moves. At present
	 * it only works for two moveables.
	 * 
	 * @param dof
	 *            is the DOF that appears to move.
	 * @param toBeMoved
	 *            contains the moveables that will actually move. (e.g. dof = UndulatorPolarizationDOF and toBeMoved =
	 *            UndulatorGapDOF & UndulatorPhaseDOF).
	 * @param moves
	 *            an arrayList containing the positions of the Moves (an inner class of the UndulatorMoveMediator)
	 */
	public MultipleMove(Moveable dof, Moveable[] toBeMoved, ArrayList<Move> moves) {
		super(dof, null);
		this.toBeMoved = toBeMoved;
		for (int i = 0; i < toBeMoved.length; i++)
			toBeMoved[i].addIObserver(this);
		this.moves = moves;
	}

	@Override
	public void execute() throws MoveableException {
		// Locking the DOF here is essential as creating the thread takes a
		// substantial time allowing a gap where polling thread might find that
		// the dof is not moving @see gda.oe.OEBase.isMoving().
		getDof().lock(this);
		runner = uk.ac.gda.util.ThreadManager.getThread(this, getClass().getName());
		runner.start();
	}

	@Override
	public synchronized void run() {
		int check;
		try {
			executing = true;
			legSucceeded = true;
			for (int i = 0; i < moves.size(); i++) {
				commandID = CommandId.next();
				Quantity[] qs = moves.get(i).getPositions();

				// NB All moves have already been checked during their
				// creation but checkMoveTo is the only way to get
				// a move set and the dof locked.
				check = toBeMoved[0].checkMoveTo(qs[0], this);
				if (check == MoveableStatus.SUCCESS)
					check = toBeMoved[1].checkMoveTo(qs[1], this);
				if (check == MoveableStatus.SUCCESS) {
					doingALeg = true;
					multipleMovesInProgress = true;
					flagOne = true;
					flagTwo = true;
					toBeMoved[0].doMove(this, commandID);
					toBeMoved[1].doMove(this, commandID);

					// The doingALeg flag is set false in the method
					// legDone() which
					// is called from the update method() only when both
					// moves are
					// completed.
					while (doingALeg) {
						wait();
					}

					// If the leg failed then throw an exception (caught
					// below)
					if (legSucceeded == false) {
						logger.error("!!!! leg failed about to throw " + toBeThrownAsSoonAsPossible);
						executing = false;
						throw toBeThrownAsSoonAsPossible;
					}
				} else {
					// If move cannot be started throw an exception (caught
					// below)
					executing = false;
					throw new MoveableException(new MoveableStatus(check, getDof().getName()),
							"MultipleMove.run: checkMoveTo != SUCCESS");
				}
			}// End of for loop
		} catch (InterruptedException ie) {
			logger.error("MultipleMove wait loop interrupted " + ie.getMessage());
		} catch (MoveableException e) {
			logger.error("MultipleMove caught MoveableException " + e.getMessage() + " " + e.getMoveableStatus());
			if (watcher != null)
				watcher.inform(this, e);
		} finally {
			unlock();
			for (int i = 0; i < toBeMoved.length; i++)
				toBeMoved[i].deleteIObserver(this);
			executing = false;
			logger.debug("MultipleMove: run thread move over");
		}
	}

	@Override
	public void update(Object theObserved, Object changeCode) {
		if (theObserved instanceof Moveable) {
			Moveable m = (Moveable) theObserved;
			MoveableStatus d = (MoveableStatus) changeCode;
			logger.debug("MultipleMove.update() called with m " + m + " d " + d + " d.value " + d.value());
			if (m == toBeMoved[0] && d.value() != MoveableStatus.BUSY && d.id() == commandID) {
				flagOne = false;
				// NB that AWAY_FROM_LIMIT is not an error
				if (d.value() != MoveableStatus.READY && d.value() != MoveableStatus.AWAY_FROM_LIMIT) {
					legSucceeded = false;
					toBeThrownAsSoonAsPossible = new MoveableException(d, "Error while moving "
							+ toBeMoved[0].getName() + "\n" + d.getMessage(), null);
				}
			}
			if (m == toBeMoved[1] && d.value() != MoveableStatus.BUSY && d.id() == commandID) {
				flagTwo = false;
				// NB that AWAY_FROM_LIMIT is not an error
				if (d.value() != MoveableStatus.READY && d.value() != MoveableStatus.AWAY_FROM_LIMIT) {
					legSucceeded = false;
					toBeThrownAsSoonAsPossible = new MoveableException(d, "Error while moving "
							+ toBeMoved[1].getName() + "\n" + d.getMessage());
				}
			}

			// multipleMovesInProgress flag is used to prevent legDone()
			// being
			// called twice as there is every possibility that there is more
			// than
			// one MoveableStatus.READY sent.
			if (flagOne == false && flagTwo == false && multipleMovesInProgress == true) {
				multipleMovesInProgress = false;
				legDone();
			}
		}
	}

	private synchronized void legDone() {
		doingALeg = false;
		counter++;
		logger.debug("legDone called doingALeg is now " + doingALeg + " " + counter);
		notifyAll();
	}

	private void unlock() {
		toBeMoved[0].unLock(this);
		toBeMoved[1].unLock(this);
		logger.debug("" + this + " both locks now removed");
		if (getDof().unLock(this))
			logger.debug("" + this + " lock on " + getDof() + " now removed");
		else
			logger.debug("" + this + " lock on " + getDof() + " NOT removed !!!!!");
	}

	/**
	 * Since MultipleMoves do their actual moving in a separate thread exceptions do not get through to the Moveable
	 * which started the move. This method allows things which want to be informed to do so. This could have been done
	 * simply with IObserver and IObservable but since this already operates the other way between MultipleMoves and
	 * their Moveables it seemed less confusing (and less dangerous) to implement a separate mechanism.
	 * 
	 * @param watcher
	 *            the object to be informed of exceptions
	 */
	public void addWatcher(MultipleMoveWatcher watcher) {
		this.watcher = watcher;
	}
}