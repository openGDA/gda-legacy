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

import java.io.Serializable;
import java.util.Date;
import java.util.Random;

/**
 * Placeholder for Corba so a Locker object can exist both sides of a Corba bridge, usually any object can be a Locker
 * and call the Lockable methods.
 * 
 * @see gda.lockable.Lockable
 * @see gda.lockable.LockableComponent
 */
public class Locker implements Serializable {
	/**
	 * Not Locked
	 */
	public static final int NOT_LOCKED = 1;

	/**
	 * Lock failure
	 */
	public static final int LOCK_FAILED = 2;
	/**
	 * Unknown lock
	 */
	public static final int UNKNOWN = 3;

	private static Random random = new Random((new Date()).getTime());

	private int lockId = LOCK_FAILED;

	/**
	 * 
	 */
	public Locker() {
		do {
			lockId = random.nextInt();
		} while (lockId == LOCK_FAILED || lockId == NOT_LOCKED);
	}

	/**
	 * This constructor is used to reconstruct a locker when the lockId is passed across the network with Corba. Don't
	 * call this constructor directly on the client side, always use Locker() constructor.
	 * 
	 * @param lockId
	 *            the lock id
	 */
	public Locker(int lockId) {
		this.lockId = lockId;
	}

	/**
	 * This is called by Corba before reconstructing a locker when the lockId is passed across the network.
	 * 
	 * @return current unique locker id
	 */
	public int getId() {
		return lockId;
	}
}
