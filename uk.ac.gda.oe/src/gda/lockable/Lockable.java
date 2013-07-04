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

/**
 * Implemented by objects such as motors to ensure that they are only accessed by one controlling object at a time.
 * 
 * @see gda.lockable.LockableComponent for an example
 */

public interface Lockable {
	/**
	 * Checks whether a lockable object is locked for a given controller object
	 * 
	 * @param locker
	 *            the controller object whose access is to be checked
	 * @return true if the object is locked by this controller
	 */
	public boolean lockedFor(Object locker);

	/**
	 * Request a controlling lock used by a Locker object
	 * 
	 * @param locker
	 *            the controller object that is requesting access
	 * @return true if the object is locked by this controller
	 */
	public boolean lock(Object locker);

	/**
	 * @param unLocker
	 *            controlling object requesting release of lock
	 * @return true only if this controller has previously locked the Lockable before this call and unlock was a success
	 */
	public boolean unLock(Object unLocker);
}
