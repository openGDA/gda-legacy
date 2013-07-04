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

package gda.oe.dofs;

import gda.observable.IObserver;
import gda.oe.MoveableException;
import gda.oe.MoveableStatus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class extends the MonoDOF class. It waits after every move to allow a settle time for the mono. The value is in
 * seconds and is altered by accessor functions.
 */
public class MonoWithSettleTimeDOF extends MonoDOF {
	private static final Logger logger = LoggerFactory.getLogger(MonoWithSettleTimeDOF.class);

	// the settle time in seconds
	private double settleTime = 0.0;

	/**
	 * @param settleTime
	 */
	public void setSettleTime(double settleTime) {
		this.settleTime = settleTime;
	}

	/**
	 * @return settleTime
	 */
	public double getSettleTime() {
		return settleTime;
	}

	@Override
	public synchronized void doMove(Object mover, int id) throws MoveableException {
		logger.debug("DOF doMove called mover is " + mover + " id is " + id);
		if (lockedFor(mover)) {
			this.id = id;
			this.addIObserver((IObserver) mover);
			for (int i = 0; i < moveables.length; i++) {
				moveables[i].doMove(this, id);
			}

			// extra code

			try {
				// wait until all moveables have finished
				for (int i = 0; i < moveables.length; i++) {
					while (moveables[i].isMoving()) {
						Thread.sleep(250);
					}
				}

				// wait for the settle time
				Thread.sleep((int) settleTime * 1000);
			} catch (InterruptedException ex) {
			}
		} else {
			throw new MoveableException(new MoveableStatus(MoveableStatus.NOTLOCKED, getName()),
					"MonoWithSettleTimeDOF.doMove: lockedFor(mover) returned false");
		}
	}
}
