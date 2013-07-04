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

package gda.lockable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Carries out proper locking for an object using the Lockable interface.
 * 
 * @see gda.lockable.Lockable
 */
public class LockableComponent implements Lockable {
	private static final Logger logger = LoggerFactory.getLogger(LockableComponent.class);

	volatile private Object locker = null;

	@Override
	public boolean lockedFor(Object locker) {
		return (this.locker == locker);
	}

	@Override
	public boolean lock(Object locker) {
		logger.debug("{} is currently locked for {}", this, this.locker);

		if (lockedFor((Object) null) || lockedFor(locker)) {
			logger.debug("{} is now locked for {}", this, locker);
			this.locker = locker;
			return (true);
		}
		return false;
	}

	@Override
	public boolean unLock(Object unLocker) {
		if (lockedFor(unLocker)) {
			this.locker = null;
			return true;
		}
		return false;
	}
}
