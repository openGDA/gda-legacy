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

package gda.oe;

import gda.factory.Configurable;
import gda.factory.FactoryException;
import gda.factory.Localizable;
import gda.lockable.Locker;
import gda.observable.IObserver;
import gda.observable.ObservableComponent;
import gda.oe.commands.AbsoluteMove;
import gda.oe.dofs.DOF;
import gda.util.LoggingConstants;

import java.util.ArrayList;
import java.util.List;

import org.jscience.physics.quantities.Quantity;
import org.jscience.physics.units.Unit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A base implementation for all Optical Elements.
 */
public abstract class OEBase implements IObserver, Finisher, OE, Configurable, Localizable {
	private static final Logger logger = LoggerFactory.getLogger(OEBase.class);

	private String name;

	private ArrayList<Moveable> useableMoveableList = new ArrayList<Moveable>();

	private ArrayList<Moveable> moveableList = new ArrayList<Moveable>();

	private ArrayList<String> moveableNameList = new ArrayList<String>();

	private boolean local = false;

	private Moveable[] useableMoveables;

	private String[] useableMoveableNames;

	private int[] lockIds;

	private Locker[] lockers;

	private boolean configured = false;

	private ObservableComponent observableComponent = new ObservableComponent();

	/**
	 * 
	 */
	static public final int SAVELIMITS = 1;

	/**
	 * 
	 */
	static public final int SAVELIMITANDOFFSET = 1;

	/**
	 * 
	 */
	static public final int SAVEOFFSET = 1;

	private int numberOfUseableMoveables = 0;

	/**
	 * configures the OE
	 * 
	 * @throws FactoryException
	 */
	@Override
	public void configure() throws FactoryException {
		logger.debug("Configure called for " + name);

		// The value of numberOfUseableMoveables should have been set correctly
		// by
		// the addMoveable
		// method during the Castor phase.

		useableMoveableNames = new String[numberOfUseableMoveables];
		useableMoveables = new DOF[numberOfUseableMoveables];
		lockIds = new int[numberOfUseableMoveables];
		lockers = new Locker[numberOfUseableMoveables];
		int i = 0;

		// Go through the list of Moveables. Give each one the
		// list of Moveables and configure it. If the Moveable
		// is one of the useable ones then add it to the relevant
		// lists.
		for (Moveable moveable : moveableList) {
			moveable.setMoveableList(moveableList);
		}
		for (Moveable moveable : moveableList) {
			moveable.configure();
			if (moveable.isDirectlyUseable()) {
				useableMoveables[i] = moveable;
				useableMoveableNames[i] = moveable.getName();
				moveable.addIObserver(this);
				i++;
			}
		}

		configured = true;
	}

	private boolean isConfigured() {
		return configured;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	/**
	 * @return Returns the local.
	 */
	@Override
	public boolean isLocal() {
		return local;
	}

	/**
	 * @param local
	 *            The local to set.
	 */
	@Override
	public void setLocal(boolean local) {
		this.local = local;
	}

	/**
	 * Sets the list of moveables in this OE.
	 * 
	 * @param moveables
	 *            the moveables
	 */
	public void setMoveables(List<Moveable> moveables) {
		this.moveableList = new ArrayList<Moveable>();
		for (Moveable moveable : moveables) {
			addMoveable(moveable);
		}
	}

	/**
	 * Adds a Moveable to the list. This method is called by the Castor mechanism and the list ends up containing ALL
	 * the Moveables.
	 * 
	 * @param moveable
	 *            the Moveable to add
	 */
	public void addMoveable(Moveable moveable) {
		moveableList.add(moveable);

		// Moveables which return true here will be available via
		// the OE interface. Those which return false will only be
		// used by other Moveables. (See configure()).

		if (moveable.isDirectlyUseable())
			numberOfUseableMoveables++;
	}

	/**
	 * Returns the list of Moveables available via the OE interface.
	 * 
	 * @return the ArrayList of Moveables available via the OE interface
	 */
	public ArrayList<Moveable> getUseableMoveableList() {
		return useableMoveableList;
	}

	/**
	 * Returns the list of all Moveables known to the OE
	 * 
	 * @return the ArrayList of all Moveables
	 */
	public ArrayList<Moveable> getMoveableList() {
		return moveableList;
	}

	@Override
	public String getDOFType(String moveableName) throws MoveableException {
		return ((DOF) findUseableMoveable(moveableName)).getDOFType();
	}

	@Override
	public String[] getDOFNames() {
		return useableMoveableNames;
	}

	/**
	 * @param name
	 * @return Moveable
	 */
	public Moveable getMoveable(String name) {
		Moveable m = null;

		try {
			m = findUseableMoveable(name);
		} catch (MoveableException e) {
			logger.error("MoveableException in OEBase.getMoveable for {}", name);
			e.printStackTrace();
		}

		return m;
	}

	/**
	 * Returns the array of moveable names
	 * 
	 * @return ArrayList
	 */
	public ArrayList<String> getMoveableNames() {
		return moveableNameList;
	}

	@Override
	public String[] getMoveableNames(String name) {
		logger.debug("inside oebase the list is  +" + moveableNameList.size());
		for (Moveable moveable : moveableList) {
			logger.debug("the list value is  +" + moveable);
			if (moveable.getName().equals(name)) {
				if (moveable instanceof DOF) {
					ArrayList<String> list = ((DOF) moveable).getMoveableNames();
					logger.debug("the list is  +" + list);
					String[] array = new String[list.size()];
					int j = 0;
					for (String n : list) {
						array[j++] = n;
						logger.debug(" " + n);
					}

					return array;
				}

			}
		}
		return null;
	}

	/**
	 * Finds one of the useableMoveables given its name.
	 * 
	 * @param moveableName
	 *            the name to look for
	 * @return the corresponding useableMoveable
	 * @throws MoveableException
	 */
	private Moveable findUseableMoveable(String moveableName) throws MoveableException {
		for (int i = 0; i < useableMoveableNames.length; i++) {
			if (useableMoveableNames[i].equals(moveableName)) {
				return useableMoveables[i];
			}
		}
		throw new MoveableException(new MoveableStatus(MoveableStatus.MOVEABLENAME_INVALID),
				"OEBase.findUseableMoveable - unable to find a useable moveable");

	}

	@Override
	public void setSpeed(String moveableName, Quantity speed) throws MoveableException {
		ensureIsConfigured("setSpeed");
		findUseableMoveable(moveableName).setSpeed(speed);
	}

	@Override
	public void setSpeed(String moveableName, Quantity start, Quantity end, Quantity time) throws MoveableException {
		ensureIsConfigured("setSpeed");
		findUseableMoveable(moveableName).setSpeed(start, end, time);
	}

	@Override
	public Quantity getSpeed(String moveableName) throws MoveableException {
		return findUseableMoveable(moveableName).getSpeed();
	}

	@Override
	public void setSpeedLevel(String moveableName, int level) throws MoveableException {
		findUseableMoveable(moveableName).setSpeedLevel(level);
	}

	@Override
	public void moveBy(String moveableName, Quantity increment) throws MoveableException {
		ensureIsConfigured("moveBy");
		// Get the Moveable to construct a suitable move and then execute it
		MoveableCommandExecutor rm = findUseableMoveable(moveableName).createRelativeMover(increment);
		rm.execute();
	}

	@Override
	public void moveTo(String moveableName, Quantity position) throws MoveableException {
		ensureIsConfigured("moveTo");
		// Get the Moveable to construct a suitable move and then execute it
		// (the Moveable is allowed
		// to return null if it does not want to move - see for example
		// UndulatorHarmonicDOF.
		MoveableCommandExecutor am = findUseableMoveable(moveableName).createAbsoluteMover(position);
		if (am != null)
			am.execute();
	}

	@Override
	public void home(String moveableName) throws MoveableException {
		ensureIsConfigured("home");
		// Get the Moveable to construct a suitable move and then execute it
		MoveableCommandExecutor hc = findUseableMoveable(moveableName).createHomer(getHomeOffset(moveableName));
		hc.execute();
		logger.debug(LoggingConstants.FINEST, "****return of control after homing ****");
	}

	private void ensureIsConfigured(String caller) throws MoveableException {
		if (!isConfigured()) {
			throw new MoveableException(new MoveableStatus(MoveableStatus.CONFIGURATION_FAILURE), "OEBase." + caller
					+ " : isConfigured returned false for " + getName());
		}
	}

	@Override
	public void moveContinuously(String moveableName, int direction) throws MoveableException {
		// FIXME: change direction into some kind of enumeration (but note
		// some useableMoveables e.g. DoubleAxisGapWidth need to be able to
		// do the equivalent of -1 * direction).
		ensureIsConfigured("moveContinuously");
		findUseableMoveable(moveableName).moveContinuously(direction);
	}

	@Override
	public void stop(String moveableName) throws MoveableException {
		ensureIsConfigured("stop");
		findUseableMoveable(moveableName).stop();
	}

	@Override
	public void stop() throws MoveableException {
		ensureIsConfigured("stop");
		for (int i = 0; i < useableMoveables.length; i++)
			useableMoveables[i].stop();
	}

	@Override
	public boolean isMoving() throws MoveableException {
		boolean rtrn = false;

		for (int i = 0; i < useableMoveables.length; i++)
			rtrn = rtrn || useableMoveables[i].isMoving();

		return rtrn;
	}

	@Override
	public boolean isMoving(String moveableName) throws MoveableException {
		return findUseableMoveable(moveableName).isMoving();
	}

	@Override
	public void setPosition(String moveableName, Quantity position) throws MoveableException {
		ensureIsConfigured("setPosition");
		logger.debug(LoggingConstants.FINEST, "calling oe setPostion");
		// Get the Moveable to construct a suitable move and then execute it
		MoveableCommandExecutor sp = findUseableMoveable(moveableName).createPositionSetter(position);
		sp.execute();
	}

	@Override
	public Quantity getPosition(String moveableName) throws MoveableException {
		return findUseableMoveable(moveableName).getPosition();
	}

	@Override
	public void refresh(String moveableName) throws MoveableException {
		findUseableMoveable(moveableName).refresh();
	}

	@Override
	public Quantity getPosition(String moveableName, Unit<? extends Quantity> units) throws MoveableException {
		return findUseableMoveable(moveableName).getPosition(units);
	}

	@Override
	public String formatPosition(String moveableName, double position) throws MoveableException {
		return findUseableMoveable(moveableName).formatPosition(position);
	}

	@Override
	public boolean isPositionValid(String moveableName) throws MoveableException {
		return findUseableMoveable(moveableName).isPositionValid();
	}

	@Override
	public boolean isSpeedLevelSettable(String moveableName) throws MoveableException {
		return findUseableMoveable(moveableName).isSpeedLevelSettable();
	}

	@Override
	public Unit<? extends Quantity> getReportingUnits(String moveableName) throws MoveableException {
		return findUseableMoveable(moveableName).getReportingUnits();
	}

	@Override
	public void setReportingUnits(String moveableName, Unit<? extends Quantity> units) throws MoveableException {
		findUseableMoveable(moveableName).setReportingUnits(units);
	}

	@Override
	public ArrayList<Unit<? extends Quantity>> getAcceptableUnits(String moveableName) throws MoveableException {
		return findUseableMoveable(moveableName).getAcceptableUnits();
	}

	@Override
	public int getProtectionLevel(String moveableName) throws MoveableException {
		return findUseableMoveable(moveableName).getProtectionLevel();
	}

	@Override
	public MoveableStatus getStatus(String moveableName) throws MoveableException {
		return findUseableMoveable(moveableName).getStatus();
	}

	@Override
	public boolean isFinished() {
		return false;
	}

	@Override
	public void update(Object o, Object arg) {
		notifyIObservers(this, arg);
	}

	@Override
	public Quantity getHomeOffset(String moveableName) throws MoveableException {
		return findUseableMoveable(moveableName).getHomeOffset();
	}

	@Override
	public void setHomeOffset(String moveableName, Quantity offset) throws MoveableException {
		findUseableMoveable(moveableName).setHomeOffset(offset);
	}

	@Override
	public Quantity getPositionOffset(String moveableName) throws MoveableException {
		return findUseableMoveable(moveableName).getPositionOffset();
	}

	@Override
	public void setPositionOffset(String moveableName, Quantity offset) throws MoveableException {
		findUseableMoveable(moveableName).setPositionOffset(offset);
	}

	private int getLockId(String moveableName) throws MoveableException {
		for (int i = 0; i < useableMoveableNames.length; i++) {
			if (useableMoveableNames[i].equals(moveableName)) {
				return lockIds[i];
			}
		}
		throw new MoveableException(new MoveableStatus(MoveableStatus.MOVEABLENAME_INVALID),
				"OEBase.getLockId: unable to find useable moveable that matches the name " + moveableName);
	}

	private void setLockId(String moveableName, int lockId) throws MoveableException {
		for (int i = 0; i < useableMoveableNames.length; i++) {
			if (useableMoveableNames[i].equals(moveableName)) {
				lockIds[i] = lockId;
				// return added 6/10/04 but how did it ever work without it??
				return;
			}
		}
		throw new MoveableException(new MoveableStatus(MoveableStatus.MOVEABLENAME_INVALID),
				"OEBase.setLockId: unable to find useable moveable that matches the name " + moveableName);
	}

	private Locker getLocker(String moveableName) throws MoveableException {
		for (int i = 0; i < useableMoveableNames.length; i++) {
			if (useableMoveableNames[i].equals(moveableName)) {
				return lockers[i];
			}
		}
		throw new MoveableException(new MoveableStatus(MoveableStatus.MOVEABLENAME_INVALID),
				"OEBase.getLocker: unable to find useable moveable that matches the name " + moveableName);
	}

	private void setLocker(String moveableName, Locker locker) throws MoveableException {
		for (int i = 0; i < useableMoveableNames.length; i++) {
			if (useableMoveableNames[i].equals(moveableName)) {
				lockers[i] = locker;
				// return added 6/10/04 but how did it ever work without it??
				return;
			}
		}
		throw new MoveableException(new MoveableStatus(MoveableStatus.MOVEABLENAME_INVALID),
				"OEBase.setLocker: unable to find useable moveable that matches the name " + moveableName);
	}

	@Override
	public synchronized int lock(String moveableName) throws MoveableException {
		int lockId = Locker.LOCK_FAILED;

		Locker locker = new Locker();
		if (findUseableMoveable(moveableName).lock(locker)) {
			setLocker(moveableName, locker);
			lockId = locker.getId();
			setLockId(moveableName, lockId);
		}

		return lockId;
	}

	/**
	 * Synchronized because it is used in monitoring threads see e.g. UndulatorMoveMediator
	 * 
	 * @param moveableName
	 *            the name of the moveable
	 * @param locker
	 *            the locker
	 * @return the lock id
	 * @throws MoveableException
	 */
	@Override
	public synchronized int lock(String moveableName, Locker locker) throws MoveableException {
		int lockId = Locker.LOCK_FAILED;

		if (findUseableMoveable(moveableName).lock(locker)) {
			setLocker(moveableName, locker);
			lockId = locker.getId();
			setLockId(moveableName, lockId);
		} else {
			logger.debug(moveableName + " " + locker.getId());
		}

		return lockId;
	}

	/**
	 * Synchronized because it is used in monitoring threads see e.g. UndulatorMoveMediator
	 * 
	 * @param moveableName
	 *            the name of the moveable
	 * @param lockId
	 *            the lock ID
	 * @throws MoveableException
	 */
	@Override
	public synchronized void unlock(String moveableName, int lockId) throws MoveableException {
		if (lockId == Locker.UNKNOWN) {
			this.overideLock(moveableName);
		} else if (lockId == getLockId(moveableName)) {
			findUseableMoveable(moveableName).unLock(getLocker(moveableName));
			setLockId(moveableName, Locker.NOT_LOCKED);
		} else {
			logger.debug("Invalid lock ID " + lockId + " for unLocking " + moveableName + "(required lockId is "
					+ getLockId(moveableName) + ")");
			throw new MoveableException(new MoveableStatus(MoveableStatus.ERROR), "Cannot unLock for id " + lockId,
					null);
		}
	}

	/**
	 * @param moveableName
	 * @param locker
	 * @throws MoveableException
	 */
	public synchronized void unlock(String moveableName, Object locker) throws MoveableException {
		findUseableMoveable(moveableName).unLock(locker);
	}

	@Override
	public synchronized int moveLockedTo(String moveableName, Quantity position, int lockId) throws MoveableException {
		Locker locker = new Locker();
		int newLockId = Locker.LOCK_FAILED;

		if (lockId == getLockId(moveableName)) {
			unlock(moveableName, lockId);
			AbsoluteMove am = new AbsoluteMove(findUseableMoveable(moveableName), position);
			logger.debug("" + this + "DOF " + moveableName + " about to call am.execute()");
			am.execute();
			newLockId = locker.getId();
		} else {
			logger.debug("Attempting to move DOF " + moveableName + " but don't have " + "a lock on it");
		}

		return newLockId;
	}

	/**
	 * @param moveableName
	 * @param position
	 * @return dof status
	 * @throws MoveableException
	 */
	@Override
	public int moveCheck(String moveableName, Quantity position) throws MoveableException {
		Locker locker = new Locker();
		int code = findUseableMoveable(moveableName).checkMoveTo(position, locker);
		if (code == MoveableStatus.SUCCESS)
			findUseableMoveable(moveableName).unLock(locker);

		logger.debug("OEBase moveCheck() returning " + code);
		return code;
	}

	@Override
	public boolean isScannable(String moveableName) throws MoveableException {
		return findUseableMoveable(moveableName).isScannable();
	}

	@Override
	public Quantity getSoftLimitLower(String dofname) throws MoveableException {
		return findUseableMoveable(dofname).getSoftLimitLower();
	}

	@Override
	public void pushSpeed(String dofname) throws MoveableException {
		findUseableMoveable(dofname).pushSpeed();
	}

	@Override
	public void popSpeed(String dofname) throws MoveableException {
		findUseableMoveable(dofname).popSpeed();
	}

	/**
	 * @see gda.oe.OE#getSoftLimitUpper(java.lang.String)
	 */
	@Override
	public Quantity getSoftLimitUpper(String dofname) throws MoveableException {
		return findUseableMoveable(dofname).getSoftLimitUpper();
	}

	@Override
	public Object getDeviceAttribute(String dofName, String name) throws MoveableException {
		Object value = null;
		try {
			value = ((DOF) findUseableMoveable(dofName)).getAttribute(name);
		} catch (MoveableException e) {
			throw new MoveableException(new MoveableStatus(MoveableStatus.ERROR),
					"ERROR in OEBase.getAttribute(): can't get attribute ", e);
		}
		return value;
	}

	@Override
	public void setDeviceAttribute(String dofName, String name, Object value) throws MoveableException {
		try {
			((DOF) findUseableMoveable(dofName)).setAttribute(name, value);
		} catch (MoveableException e) {
			throw new MoveableException(new MoveableStatus(MoveableStatus.ERROR),
					"ERROR in OEBase.setAttribute(): can't set attribute ", e);
		}
	}

	/**
	 * @see gda.observable.IObservable#addIObserver(gda.observable.IObserver)
	 */
	@Override
	public void addIObserver(IObserver anIObserver) {
		observableComponent.addIObserver(anIObserver);
	}

	/**
	 * deletes an IObsrver from the list
	 * 
	 * @param anIObserver
	 *            the IObserver to be deleted
	 */
	@Override
	public void deleteIObserver(IObserver anIObserver) {
		observableComponent.deleteIObserver(anIObserver);
	}

	/**
	 * deletes all IObservers
	 */
	@Override
	public void deleteIObservers() {
		observableComponent.deleteIObservers();
	}

	/**
	 * Notify all IObservers of the same message
	 * 
	 * @param theObserver
	 * @param theArgument
	 */
	public void notifyIObservers(Object theObserver, Object theArgument) {
		observableComponent.notifyIObservers(theObserver, theArgument);
	}

	/**
	 * Unlocks a motor without knowing the lock number. Use instead of restarting gda if lock is not given up, due
	 * perhaps to underlying problem communicating with hardware. Synchronized because it is used in monitoring threads
	 * see e.g. UndulatorMoveMediator
	 * 
	 * @param moveableName
	 *            the name of the moveable
	 * @throws MoveableException
	 */
	public synchronized void overideLock(String moveableName) throws MoveableException {

		findUseableMoveable(moveableName).unLock(getLocker(moveableName));
		setLockId(moveableName, Locker.NOT_LOCKED);
	}

	// Methods for propogating beamline configuration manager methods

	/**
	 * @see gda.oe.OE#getLowerGdaLimits(java.lang.String)
	 */
	@Override
	public double[] getLowerGdaLimits(String dofname) throws MoveableException {
		return ((DOF) findUseableMoveable(dofname)).getLowerGdaLimits();
	}

	/**
	 * @see gda.oe.OE#getTolerance(java.lang.String)
	 */
	@Override
	public double[] getTolerance(String dofname) throws MoveableException {
		return ((DOF) findUseableMoveable(dofname)).getTolerance();
	}

	/**
	 * @see gda.oe.OE#getUpperGdaLimits(java.lang.String)
	 */
	@Override
	public double[] getUpperGdaLimits(String dofname) throws MoveableException {
		return ((DOF) findUseableMoveable(dofname)).getUpperGdaLimits();
	}

	/**
	 * @see gda.oe.OE#setLowerGdaLimits(java.lang.String, double)
	 */
	@Override
	public void setLowerGdaLimits(String dofname, double lowerLim) throws MoveableException {
		((DOF) findUseableMoveable(dofname)).setLowerGdaLimits(lowerLim);
	}

	/**
	 * @see gda.oe.OE#setLowerGdaLimits(java.lang.String, double[])
	 */
	@Override
	public void setLowerGdaLimits(String dofname, double[] lowerLim) throws MoveableException {
		((DOF) findUseableMoveable(dofname)).setLowerGdaLimits(lowerLim);
	}

	/**
	 * @see gda.oe.OE#setTolerance(java.lang.String, double)
	 */
	@Override
	public void setTolerance(String dofname, double tolerance) throws MoveableException {
		((DOF) findUseableMoveable(dofname)).setTolerance(tolerance);
	}

	/**
	 * @see gda.oe.OE#setTolerance(java.lang.String, double[])
	 */
	@Override
	public void setTolerance(String dofname, double[] tolerance) throws MoveableException {
		((DOF) findUseableMoveable(dofname)).setTolerance(tolerance);
	}

	/**
	 * @see gda.oe.OE#setUpperGdaLimits(java.lang.String, double)
	 */
	@Override
	public void setUpperGdaLimits(String dofname, double upperLim) throws MoveableException {
		((DOF) findUseableMoveable(dofname)).setUpperGdaLimits(upperLim);
	}

	/**
	 * @see gda.oe.OE#setUpperGdaLimits(java.lang.String, double[])
	 */
	@Override
	public void setUpperGdaLimits(String dofname, double[] upperLim) throws MoveableException {
		((DOF) findUseableMoveable(dofname)).setUpperGdaLimits(upperLim);
	}

	/**
	 * docstring getter for a dof
	 * 
	 * @param dofname
	 * @return the docString
	 * @throws MoveableException
	 */
	@Override
	public String getDocString(String dofname) throws MoveableException {
		return findUseableMoveable(dofname).getDocString();
	}

	/**
	 * docString setter for a dof
	 * 
	 * @param dofname
	 * @param docString
	 * @throws MoveableException
	 */
	@Override
	public void setDocString(String dofname, String docString) throws MoveableException {
		findUseableMoveable(dofname).setDocString(docString);
	}

}