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

package gda.oe.positioners;

import gda.configuration.properties.LocalProperties;
import gda.device.DeviceException;
import gda.device.Motor;
import gda.device.MotorException;
import gda.device.MotorStatus;
import gda.device.MotorProperties.MotorProperty;
import gda.device.MotorProperties.MotorEvent;
import gda.device.motor.MotorBase;
import gda.factory.Configurable;
import gda.factory.FactoryException;
import gda.factory.Findable;
import gda.factory.Finder;
import gda.observable.IObserver;
import gda.oe.AbstractMoveable;
import gda.oe.Moveable;
import gda.oe.MoveableCommandExecutor;
import gda.oe.MoveableException;
import gda.oe.MoveableStatus;
import gda.oe.commands.DOFCommand;
import gda.util.exceptionUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.jscience.physics.quantities.Quantity;
import org.jscience.physics.units.Unit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the 'leaf' class of the DOF/Positioner Composite pattern
 * implementation of Moveable. Each Positioner controls a single Motor (any
 * class implementing the Motor interface). Positioners are responsible for
 * either polling or observing their motor to keep up to date knowledge of
 * position and status. They also translate between positions specified in real
 * world units ("mm", "mDeg" etc.) and motor unit. (For historical reasons the
 * Motor units are called steps throughout the Positioner package but they can
 * in fact be anything - conversion between real world and motor units is a
 * simple multiplication by the stepsPerUnit field.) Positioners are also
 * responsible for (optionally) saving the soft limits and home offset of their
 * motors.
 */
public abstract class Positioner extends AbstractMoveable implements Runnable,
		Findable, Configurable, IObserver, Moveable {

	private static final Logger logger = LoggerFactory
			.getLogger(Positioner.class);

	// variables used in poll mode of operation
	private volatile boolean waiting = false;

	private volatile boolean startMonitoring = false;

	private Thread runner;

	private long pollTime = 100;

	// default operation mode is set to poll
	private boolean poll = true;

	// variables for motor parameters
	private volatile boolean backlashRequired = false;

	private String motorName;

	private int lastDirection = 0;

	private int id = -1;

	protected Motor motor = null;

	protected double currentPosition = Double.NaN;

	protected double targetPosition;

	protected double softLimitLow = -Double.MAX_VALUE;

	protected double softLimitHigh = Double.MAX_VALUE;

	private double stepsPerUnit = 1;

	// distance of the home position(physical zero index) from
	// the desirable zero position
	protected double homeOffset = 0.0;

	// difference from the motor position and the position reported by this
	// object
	protected double positionOffset = 0.0;

	private volatile MoveableStatus dofStatus;

	// for software position correction/maintanence
	// public final boolean ON = true;
	// public final boolean OFF = false;
	/**
	 * 
	 */
	public final int NONE = 0;

	/**
	 * 
	 */
	public final int START = 1;

	private int iterationMaxValue = 0;

	private int iterationCount = NONE;

	private double iterationDeadBand = 0.0;

	private double correctionTarget = 0.0;

	private boolean positionCorrection = false;

	protected boolean positionCorrectionRequired = false;

	private boolean configured = false;

	// protected HashMap<MoveableProperty, Object> moveableProperty =
	// new HashMap<MoveableProperty, Object>();

	protected boolean softLimitsSaveable = false;

	private PositionalValues positionalValues;

	private String separator = System.getProperty("file.separator");

	private String limitsStore = null;

	private int[] limitDirection = new int[2];

	private static final int UPPERLIMIT = 0;

	private static final int LOWERLIMIT = 1;

	// These two fields effectively pass messages out of the polling thread
	// and
	// so must be volatile.
	private volatile boolean exceptionCaughtInPollingThread = false;

	private volatile MoveableException toBeThrownAsSoonAsPossible = null;

	/**
	 * move motor to the position specified in GDA drive unit
	 * 
	 * @param position
	 * @throws MotorException
	 */
	protected abstract void _moveTo(double position) throws MotorException;

	/**
	 * updates current motor position from machine unit to GDA drive Unit.
	 * 
	 * @param motorPosition
	 */
	protected abstract void _updatePosition(double motorPosition);

	/**
	 * updates current motor low limit from machine unit to GDA drive Unit.
	 * 
	 * @param motorLowLimit
	 */
	protected abstract void _updateLimitLow(double motorLowLimit);

	/**
	 * updates current motor high limit from machine unit to GDA drive Unit.
	 * 
	 * @param motorHighLimit
	 */
	protected abstract void _updateLimitHigh(double motorHighLimit);

	/**
	 * sets current position value to the input value without moving motor.
	 * 
	 * @param position
	 * @throws MotorException
	 */
	protected abstract void _setPosition(double position) throws MotorException;

	/**
	 * coverts the target value from reporting unit to drive unit
	 * 
	 * @param target
	 * @return true if OK else false
	 */
	protected abstract boolean checkTarget(Quantity target);

	/**
	 * converts the increment value from reporting unit to drive unit, set the
	 * target.
	 * 
	 * @param increment
	 * @return true if OK else false
	 */
	protected abstract boolean checkIncrement(Quantity increment);

	/**
	 * sets the soft limits of motor in machine specified units.
	 * 
	 * @param lowLimit
	 * @param highLimit
	 * @throws MotorException
	 */
	protected abstract void _setSoftLimits(double lowLimit, double highLimit)
			throws MotorException;

	/**
	 * converts the value of soft limit high from reporting unit to drive unit.
	 * 
	 * @param highLimit
	 * @return true if limit is OK else false
	 */
	protected abstract boolean checkSoftLimitHigh(Quantity highLimit);

	/**
	 * converts the value of soft limit low from reporting unit to drive unit.
	 * 
	 * @param lowLimit
	 * @return true if limit is OK else false
	 */
	protected abstract boolean checkSoftLimitLow(Quantity lowLimit);

	/**
	 * sets the homePosition distance/offset
	 * 
	 * @param offset
	 *            to be set
	 * @return true if offset is OK else false
	 */
	protected abstract boolean _setHomeOffset(Quantity offset);

	/**
	 * sets the user->motor position offset
	 * 
	 * @param offset
	 *            to be set
	 * @return true if offset is OK else false
	 */
	protected abstract boolean _setPositionOffset(Quantity offset);

	/**
	 * gets the lower soft limit quantity in motor drive unit.
	 * 
	 * @return the lower soft limit quantity in motor drive unit.
	 */
	protected abstract Quantity getLowerSoftLimit();

	/**
	 * gets the upper soft limit quantity in motor drive unit.
	 * 
	 * @return the upper soft limit quantity in motor drive unit.
	 */
	protected abstract Quantity getUpperSoftLimit();

	protected abstract void _initialisation();

	/**
	 * Constructs a Positioner
	 */
	public Positioner() {
		dofStatus = MoveableStatusFactory
				.createMoveableStatus(MotorStatus.UNKNOWN);
	}
	
	/**
	 * Sets the motor used by this positioner.
	 * 
	 * @param motor the motor
	 */
	public void setMotor(Motor motor) {
		this.motor = motor;
	}

	/**
	 * Returns whether this is a polling or non-polling positioner.
	 * 
	 * @return true if this is a polling positioner
	 */
	public boolean getPoll() {
		return poll;
	}

	/**
	 * Set whether to poll or receive updates from a motor.
	 * 
	 * @param poll
	 *            set this true for polling else false for non-polling
	 */
	public void setPoll(boolean poll) {
		this.poll = poll;
	}

	@Override
	public void configure() throws FactoryException {
		if (!configured) {
			
			if (motor == null) {
				Findable findable = Finder.getInstance().find(motorName);
				
				if (findable == null) {
					logger.error("Motor " + motorName + " not found");
					throw new FactoryException("Motor " + motorName + " not found");
				}
				
				if (!(findable instanceof Motor)) {
					throw new FactoryException(
							"Positioner.configure - object named " + motorName
									+ " is not a Motor");
				}
				motor = (Motor) findable;
			}
			
			limitDirection[UPPERLIMIT] = 0;
			limitDirection[LOWERLIMIT] = 0;
			if (!poll) {
				motor.addIObserver(this);
				_initialisation();
				try {
					propertyInitialisation();
				} catch (Exception e) {
					throw new FactoryException(
							"Positioner.configure -exception caught for "
									+ getName(), e);
				}
				configured = true;
			} else {
				runner = uk.ac.gda.util.ThreadManager.getThread(this, getClass().getName() + " "
						+ getName());

				MotorStatus status;

				// The status is set to what is returned by
				// motor.getStatus. If
				// this causes an exception status is set to fault but
				// with the
				// message from the exception. Even if the status is
				// fault we
				// still go on to start the polling thread.
				try {
					status = motor.getStatus();
					dofStatus = MoveableStatusFactory
							.createMoveableStatus(status);
				} catch (MotorException me) {
					status = MotorStatus.FAULT;
					dofStatus = new MoveableStatus(MoveableStatus.ERROR,
							id, me.getMessage());
				}
				logger.debug(getClass().getName() + ": " + getName()
						+ " initial status is " + status.value());

				// new code
				// the following method call returns false if no
				// property is
				// found
				softLimitsSaveable = LocalProperties
						.check("gda.softlimits.save");
				if (softLimitsSaveable) {
					try {
						setLimitsStore();
						loadLimitsAndOffset();
					} catch (FileNotFoundException fnfe) {
						// do nothing - the file not existing is possible
					} catch (IOException e) {
						setStatus(MotorStatus.FAULT);
						logger
								.error("Positioner: Exception caught setting Limits Store"
										+ e.getMessage());
					}
				}
				// end of new code

				// README. There used to be an if status != FAULT around
				// the bit
				// between here and _initialisation(). This is not
				// needed. The
				// thread should be started even if the motor is faulty
				// otherwise motors which recover (e.g. switched on when
				// previously off, allowed by external forces when
				// previously
				// disallowed) will not work with a Refresh but will
				// require a
				// restart.

				runner.start();

				/*
				 * We have to be sure that the monitoring thread is ready
				 * for duty. Problems have occured by things being notified
				 * before they are waiting and thus they never come out of
				 * their waits when they finally get there.
				 */

				while (!waiting) {
					logger.debug("Positioner thread awake");
					Thread.yield();
				}
				logger.debug("Positioner thread now waiting");
				Thread.yield();
				_initialisation();
				configured = true;
			}

		}// End of if(!configured)
	}

	/**
	 * Sets the motor name (used by Castor to interpret XML file)
	 * 
	 * @param motor_Name
	 *            the name of the motor
	 */
	public void setMotorName(String motor_Name) {
		this.motorName = motor_Name;
	}

	/**
	 * Gets the motor name (used by Castor to interpret XML file)
	 * 
	 * @return the name of the motor
	 */
	public String getMotorName() {
		return motorName;
	}

	/**
	 * Sets the polling time interval (used by Castor to interpret XML file)
	 * 
	 * @param poll_Time
	 *            the polling time interval in mS
	 */
	public void setPollTime(long poll_Time) {
		this.pollTime = poll_Time;
	}

	/**
	 * Gets the polling time interval (used by Castor to interpret XML file)
	 * 
	 * @return the polling time interval
	 */
	public long getPollTime() {
		return pollTime;
	}

	/**
	 * Gets the lower soft limit (used by Castor to interpret XML file)
	 * 
	 * @return the lower soft limit
	 */
	public double getSoftLimitLow() {
		return getLowerSoftLimit().to(getReportingUnits()).getAmount();
	}

	/**
	 * sets soft limit high in GDA drive unit. Actual setting is done by
	 * subclass.
	 * 
	 * @param highLimit
	 *            the higher soft limit
	 * @throws MoveableException
	 */
	private void setSoftLimitHigh(Quantity highLimit) throws MoveableException {
		if (checkSoftLimitHigh(highLimit)) {
			notifyIObservers(this, dofStatus);
			if (softLimitsSaveable) {
				saveLimitsAndOffset();
			}
		} else {
			throw new MoveableException(new MoveableStatus(
					MoveableStatus.INCORRECT_QUANTITY, getName(), highLimit),
					"Positioner.setHSoftLimitHigh: returned false. highLimit = "
							+ highLimit.toString());
		}
	}

	/**
	 * Sets the higher soft limit (used by Castor to interpret XML file)
	 * 
	 * @param highLimit
	 * @throws MoveableException
	 */
	public void setSoftLimitHigh(double highLimit) throws MoveableException {
		Quantity high_limit = Quantity.valueOf(highLimit, getReportingUnits());
		setSoftLimitHigh(high_limit);
	}

	/**
	 * Sets the lower soft limit (used by Castor to interpret XML file)
	 * 
	 * @param limitLow
	 *            the lower soft limit
	 * @throws MoveableException
	 */
	private void setSoftLimitLow(Quantity limitLow) throws MoveableException {
		if (checkSoftLimitLow(limitLow)) {
			notifyIObservers(this, dofStatus);
			if (softLimitsSaveable) {
				saveLimitsAndOffset();
			}
		} else {
			throw new MoveableException(new MoveableStatus(
					MoveableStatus.INCORRECT_QUANTITY, getName(), limitLow),
					"Positioner.setHSoftLimitHigh: returned false. highLimit = "
							+ limitLow.toString());
		}
	}

	/**
	 * Sets the lower soft limit (used by Castor to interpret XML file)
	 * 
	 * @param lowLimit
	 * @throws MoveableException
	 */
	public void setSoftLimitLow(double lowLimit) throws MoveableException {
		Quantity low_limit = Quantity.valueOf(lowLimit, getReportingUnits());
		setSoftLimitLow(low_limit);
	}

	/**
	 * Gets the higher soft limit (used by Castor to interpret XML file)
	 * 
	 * @return the higher soft limit
	 */
	public double getSoftLimitHigh() {
		return getUpperSoftLimit().to(getReportingUnits()).getAmount();
	}

	/**
	 * Sets the user to motor units conversion factor (used by Castor to
	 * interpret XML file). NB that although this factor is called stepsPerUnit
	 * this is only for historical reasons. It is simply a conversion factor.
	 * For example if you have a LinearPositioner (moves in mm) and a
	 * NewportMotor (also moves in mm) then you can just set this factor to 1.0.
	 * 
	 * @param steps_Per_Unit
	 *            the conversion factor
	 */
	public void setStepsPerUnit(double steps_Per_Unit) {
		this.stepsPerUnit = steps_Per_Unit;
	}

	/**
	 * Gets the user to motor units conversion factor (used by Castor to
	 * interpret XML file)
	 * 
	 * @return the conversion factor
	 */
	public double getStepsPerUnit() {
		return stepsPerUnit;
	}

	/**
	 * Returns the value of the positionCorrection flag (used by Castor to
	 * interpret XML file). NB The rule about get methods for booleans being
	 * called "is..." has led to the stupid name for this method.The field
	 * positionCorrection determines whether or not positionCorrection is done.
	 * 
	 * @return the value of the position correction flag
	 */
	public boolean isPositionCorrection() {
		return positionCorrection;
	}

	/**
	 * Sets the iteration dead band (used by Castor to interpret XML file). This
	 * is the amount of error in position allowed if positionCorrection is on.
	 * 
	 * @param iteration_DeadBand
	 *            the iterationDeadBand
	 */
	public void setIterationDeadBand(double iteration_DeadBand) {
		this.iterationDeadBand = iteration_DeadBand;
	}

	/**
	 * Sets the iterationMaxValue (used by Castor to interpret XML file). This
	 * is the maximum number of iterations allowed if positionCorrection is on.
	 * 
	 * @param iteration_MaxValue
	 *            the iterationMaxValue
	 */
	public void setIterationMaxValue(int iteration_MaxValue) {
		this.iterationMaxValue = iteration_MaxValue;
	}

	/**
	 * Sets the position correction flag (used by Castor to interpret XML file)
	 * The position correction flag determines whether or not positionCorrection
	 * is done.
	 * 
	 * @param position_Correction
	 *            the position correction flag
	 */
	public void setPositionCorrection(boolean position_Correction) {
		this.positionCorrection = position_Correction;
	}

	/**
	 * Gets the iterationMaxValue (used by Castor to interpret XML file). This
	 * is the maximum number of iterations if position correction is on.
	 * 
	 * @return the iterationMaxValue
	 */
	public int getIterationMaxValue() {
		return iterationMaxValue;
	}

	/**
	 * Gets the iteration dead band (used by Castor to interpret XML file) This
	 * is the maximum error in position allowed if position correction is on.
	 * 
	 * @return the iteration dead band
	 */
	public double getIterationDeadBand() {
		return iterationDeadBand;
	}

	/**
	 * Sets the position offset value. This is a constant used by the moveable
	 * when calculating position e.g. it could be a value to add to the motor
	 * position ('dial value') to calculate the 'user' position.
	 * 
	 * @param position_Offset
	 *            The position offset.
	 */
	public void setPositionOffsetValue(double position_Offset) {
		this.positionOffset = position_Offset;
	}

	/**
	 * Gets the position offset value. This is a constant used by the moveable
	 * when calculating position e.g. it could be a value to add to the motor
	 * position ('dial value') to calculate the 'user' position.
	 * 
	 * @return double
	 */
	public double getPositionOffsetValue() {
		return positionOffset;
	}

	/**
	 * Sets the numerical value of the offset
	 * 
	 * @param home_Offset
	 */
	public void setHomeOffsetValue(double home_Offset) {
		this.homeOffset = home_Offset;
	}

	/**
	 * Sets the numerical value of the high soft limit.
	 * 
	 * @param highLimit
	 */
	public void setSoftLimitHighValue(double highLimit) {
		this.softLimitHigh = highLimit;
	}

	/**
	 * @return softLimitHigh
	 */
	public double getSoftLimitHighValue() {
		return softLimitHigh;
	}

	/**
	 * Sets the numerical value of the low soft limit.
	 * 
	 * @param lowLimit
	 */
	public void setSoftLimitLowValue(double lowLimit) {
		this.softLimitLow = lowLimit;
	}

	/**
	 * Gets the home offset (used by Castor to interpret XML file) This is the
	 * value that the motor's position will be set to after a homing move. NB If
	 * homing is not allowed this value can still be used for other purposes.
	 * 
	 * @return the home offset
	 */
	public double getHomeOffsetValue() {
		return homeOffset;
	}

	/**
	 * Calculates the direction for moving to the specified newPosition - only
	 * used internally
	 * 
	 * @param newPosition
	 *            the proposed new position
	 * @return the direction of movement to that position
	 */
	private int calculateDirection(double newPosition) {
		double temp;

		temp = ((newPosition - currentPosition) / Math
				.abs((newPosition - currentPosition)));
		return (int) Math.round(temp);
	}

	/**
	 * Gets the current position in the specified units. The reporting units of
	 * a Positioner cannot be changed but this method must exist anyway to
	 * fulfill the Moveable interface. So the best thing for it to do is to
	 * return the position.
	 * 
	 * @param units
	 *            the Unit to use to return the position.
	 * @return a Quantity representing the current position
	 */
	@Override
	public Quantity getPosition(Unit<? extends Quantity> units) {
		return getPosition().to(units);
	}

	/**
	 * Checks a proposed move to a position - checks that the Positioner status
	 * allows a move, that the move is within the soft limits and that the
	 * Positioner can be locked for the specified mover.
	 * 
	 * @param mover
	 *            the object which is going to do the moving
	 * @return MoveableStatus.SUCCESS if the move is possible and the Positioner
	 *         is successfully locked or a MoveableStatus indicating a problem.
	 */
	private int checkMove(Object mover) {
		int check = MoveableStatus.SUCCESS;

		if ((check = statusAllowsMove(targetPosition)) == MoveableStatus.SUCCESS) {
			if (!withinLimits(targetPosition)) {
				check = MoveableStatus.SOFT_LIMIT;
			} else if (!lock(mover)) {
				check = MoveableStatus.ALREADY_LOCKED;
			}
		}

		return check;
	}

	/**
	 * Checks whether a move by the specified increment and controlled by the
	 * specified mover is allowed. NB this method is not currently used in the
	 * project but must exist to fulfill the Moveable interface and so may as
	 * well do the right thing.
	 * 
	 * @param increment
	 *            the required move
	 * @param mover
	 *            the object trying to control the move
	 * @return Moveable.Status.SUCCESS if the move is allowed and the Positioner
	 *         is successfully locked for the specified mover or a
	 *         MoveableStatus indicating a problem.
	 */
	@Override
	public int checkMoveBy(Quantity increment, Object mover) {
		int check = MoveableStatus.SUCCESS;

		if (!checkIncrement(increment)) {
			check = MoveableStatus.INCORRECT_QUANTITY;
		} else {
			check = checkMove(mover);
		}
		return check;
	}

	/**
	 * Checks whether a move to the specified position and controlled by the
	 * specified mover is allowed.
	 * 
	 * @param position
	 *            the required position
	 * @param mover
	 *            the object trying to control the move
	 * @return Moveable.Status.SUCCESS if the move is allowed and the Positioner
	 *         is successfully locked for the specified mover or a
	 *         MoveableStatus indicating a problem.
	 */
	@Override
	public int checkMoveTo(Quantity position, Object mover) {
		int check = MoveableStatus.SUCCESS;

		if (!checkTarget(position)) {
			check = MoveableStatus.INCORRECT_QUANTITY;
		} else {
			check = checkMove(mover);
		}
		if(check != MoveableStatus.SUCCESS){
			logger.warn("move of " + getName() + " to " + position.toString() + " is not possible");
		}
		return check;
	}

	/**
	 * Checks whether setting the position to the specified value and controlled
	 * by the specified mover is allowed.
	 * 
	 * @param position
	 *            the required position
	 * @param setter
	 *            the object trying to control the set
	 * @return Moveable.Status.SUCCESS if the set is allowed and the Positioner
	 *         is successfully locked for the specified mover or a
	 *         MoveableStatus indicating a problem.
	 */
	@Override
	public int checkSetPosition(Quantity position, Object setter) {
		int check = MoveableStatus.SUCCESS;

		if (!checkTarget(position)) {
			check = MoveableStatus.INCORRECT_QUANTITY;
		} else if (!withinLimits(targetPosition)) {
			check = MoveableStatus.SOFT_LIMIT;
		} else if (!lock(setter)) {
			check = MoveableStatus.ALREADY_LOCKED;

		}
		return check;
	}

	/**
	 * Checks whether homing and then setting the position to the specified
	 * value and controlled by the specified mover is allowed.
	 * 
	 * @param position
	 *            the required position
	 * @param mover
	 *            the object trying to control the homing
	 * @return Moveable.Status.SUCCESS if the homing is allowed and the
	 *         Positioner is successfully locked for the specified mover or a
	 *         MoveableStatus indicating a problem.
	 */
	@Override
	public int checkHome(Quantity position, Object mover) {
		int check = MoveableStatus.SUCCESS;

		if (!checkTarget(position)) {
			check = MoveableStatus.INCORRECT_QUANTITY;
		} else if (!lock(mover)) {
			check = MoveableStatus.ALREADY_LOCKED;

		}
		return check;
	}

	/**
	 * Actually carries out a previously checked and locked set position
	 * operation.
	 * 
	 * @param setter
	 *            the object controlling the operation
	 * @throws MoveableException
	 *             if not locked for setter or if the operation causes a
	 *             MotorException
	 */
	@Override
	public void doSet(Object setter) throws MoveableException {
		if (lockedFor(setter)) {
			try {
				setPosition(targetPosition);
			}

			// If an exception is caught status should be set immediately rather
			// than waiting for the polling (or observing) of the motor to do
			// it.This also ensures that unlocking occurs because notification
			// of the bad status will pass upwards through the Moveable tree.
			// The MotorException is converted to a suitable MoveableException
			// which is thrown on.
			catch (MotorException me) {
				logger.debug("Positioner " + getName()
						+ " caught MotorException in doSet");
				setStatus(me.status);
				throw new MoveableException(MoveableStatusFactory
						.createMoveableStatus(me.status, getName()), me
						.getMessage());
			}
		} else {
			throw new MoveableException(new MoveableStatus(
					MoveableStatus.NOTLOCKED, getName()),
					"Positioner.doSet: lockedFor(setter) = false");
		}
	}

	/**
	 * Actually carries out a previously checked and locked move.
	 * 
	 * @param mover
	 *            the object controlling the move
	 * @param _id
	 *            the id of the move (potentially used in when updating the
	 *            status)
	 * @throws MoveableException
	 *             if not locked for setter or if the operation causes a
	 *             MotorException
	 */

	@Override
	public void doMove(Object mover, int _id) throws MoveableException {
		if (lockedFor(mover)) {
			try {
				this.id = _id;
				moveTo(targetPosition);
			}

			// If an exception is caught status should be set immediately rather
			// than waiting for the polling (or observing) of the motor to do
			// it.This also ensures that unlocking occurs because notification
			// of the bad status will pass upwards through the Moveable tree.
			// The MotorException is converted to a suitable MoveableException
			// which is thrown on.
			catch (MotorException me) {
				String msg = "doMove(): "
						+ getName()
						+ " "
						+ me.getMessage()
						+ " "
						+ (me.getCause() != null ? me.getCause().getMessage()
								: "- no cause given");
				logger.debug(msg);
				// me.status null is an error - we rely on updating the status
				// with
				// a valid value to clear the lock of the mover over the dof
				if (me.status == null) {
					throw new MoveableException(MoveableStatusFactory
							.createMoveableStatus(MotorStatus.UNKNOWN,
									getName()),
							"Positioner.doMove: me.status == null - " + msg);
				}
				setStatus(me.status);
				throw new MoveableException(MoveableStatusFactory
						.createMoveableStatus(me.status, getName()), msg, me);
			}
		} else {
			throw new MoveableException(new MoveableStatus(
					MoveableStatus.NOTLOCKED, getName()),
					"Positioner.doMove: lockedFor(mover) retuned false");
		}
	}

	/**
	 * Actually carries out a previously checked and locked homing operation.
	 * 
	 * @param mover
	 *            the object controlling the operation
	 * @throws MoveableException
	 *             if not locked for setter or if the operation causes a
	 *             MotorException
	 */
	@Override
	public void doHome(Object mover) throws MoveableException {
		if (lockedFor(mover)) {
			try {
				homeMotor();
			}

			// If an exception is caught status should be set immediately rather
			// than waiting for the polling (or observing) of the motor to do
			// it.This also ensures that unlocking occurs because notification
			// of the bad status will pass upwards through the Moveable tree.
			// The MotorException is converted to a suitable MoveableException
			// which is thrown on.
			catch (MotorException me) {
				logger.debug("Positioner " + getName()
						+ " caught MotorException in doMove");
				setStatus(me.status);
				throw new MoveableException(MoveableStatusFactory
						.createMoveableStatus(me.status, getName()), me
						.getMessage());
			}
		} else {
			throw new MoveableException(new MoveableStatus(
					MoveableStatus.NOTLOCKED, getName()),
					"Positioner.doHome: lockedFor(mover) == false");
		}
	}

	/**
	 * Gets the status.
	 * 
	 * @return the status
	 */
	@Override
	public MoveableStatus getStatus() {
		return dofStatus;
	}

	/**
	 * Gets the target position of the positioner
	 * 
	 * @return the position at which the positioner is set to stop moving
	 */

	public double getTargetPosition() {
		return targetPosition;
	}

	/**
	 * Returns whether or not the Positioner is moving.
	 * 
	 * @return true if the Positioner is moving, false if not
	 * @throws MoveableException
	 */
	@Override
	public boolean isMoving() throws MoveableException {
		// If the poll loop is waiting then no move is in progress
		if (poll) {
			if (waiting && exceptionCaughtInPollingThread) {
				logger.info("Positioner " + getName()
						+ " isMoving() throwing previously caught exception");
				exceptionCaughtInPollingThread = false;
				throw toBeThrownAsSoonAsPossible;
			}
			return !waiting;
		}
		// If not polling then must query the motor directly. (The
		// status may be out of date.)
		try {
			boolean motorMoving = motor.isMoving();
			boolean statusBusy = getStatus().value() == MoveableStatus.BUSY;
			if (!motorMoving && statusBusy)
				logger.warn("!motorMoving &&  statusBusy");
			return (motorMoving || statusBusy);
		} catch (MotorException ex) {
			logger.error(getName() + " " + ex, ex);
			throw new MoveableException(MoveableStatusFactory.createMoveableStatus(
					ex.status, getName()), ex.getMessage());
		}
	}

	/**
	 * Sets the soft limits of the Positioner and calls the subclass to set
	 * motor limits
	 * 
	 * @param softLimitOne
	 *            one of the limits
	 * @param softLimitTwo
	 *            the other limit
	 * @throws MotorException
	 */
	public void setSoftLimits(double softLimitOne, double softLimitTwo)
			throws MotorException {
		if (softLimitOne != softLimitTwo) {
			softLimitLow = Math.min(softLimitOne, softLimitTwo);
			softLimitHigh = Math.max(softLimitOne, softLimitTwo);
		}
		logger.debug("positioner soft limits are" + softLimitLow + "and "
				+ softLimitHigh);

		_setSoftLimits(softLimitLow, softLimitHigh);

		// record the change to the LimitsAndOffsets file if flag is true.
		if (softLimitsSaveable) {
			saveLimitsAndOffset();
		}
	}

	/**
	 * @param direction
	 *            the direction of travel
	 * @throws MoveableException
	 */
	@Override
	public void moveContinuously(int direction) throws MoveableException {
		try {
			lastDirection = direction;
			// If positive units require negative steps change the sign
			// of direction. The motor should send out negative steps for
			// a continuous positive direction. The lastDirection SHOULD
			// be saved before this as it is in Positioner units NOT
			// Motor steps.
			if (stepsPerUnit < 0.0) {
				direction *= -1;

			}
			motor.moveContinuously(direction);
		} catch (MotorException me) {
			setStatus(me.status);
			throw new MoveableException(MoveableStatusFactory
					.createMoveableStatus(me.status, getName()),
					"Positioner.moveContinuously: caught MotorException"
							+ me.getMessage(), me);
		}
		if (poll) {
			startMonitoring = true;
			synchronized (this) {
				notifyAll(); // Awaken the polling thread to update
				// positions.
			}
		}
	}

	/**
	 * Checks if the underlying motor is capable of homing
	 * 
	 * @return boolean
	 */
	@Override
	public boolean isHomeable() {
		boolean ishomeable = false;
		try {
			ishomeable = motor.isHomeable();
		} catch (MotorException me) {
			logger.debug("Positioner " + getName()
					+ " caught MotorException in isHomeable");
		}
		return ishomeable;
	}

	/**
	 * This method should only be used where the softLimitsSaveable flag is true
	 * 
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private void setLimitsStore() throws FileNotFoundException, IOException {
		String limitsDirectory = LocalProperties.get("gda.limitsdir");
		if (limitsDirectory != null) {
			File dir = new File(limitsDirectory);
			if (!dir.exists())
				dir.mkdir();
			try {
				if (motor != null) {
					limitsStore = limitsDirectory + separator + motor.getName();
				} else {
					limitsStore = "";
				}
			} catch (Exception e) {
				logger
						.error("Positioner: Exception caught in getting motor name");
			}
			// check if the file really exist
			File file = new File(limitsStore);
			if (!file.exists()) {
				throw new FileNotFoundException("Cannot find the limit file "
						+ limitsStore);
			} else if (file.isDirectory()) {
				throw new IOException(
						"Directory found rather than file of name "
								+ limitsStore);
			}
		} else {
			throw new FileNotFoundException("Cannot find the limit directory");
		}
	}

	/**
	 * This method should only be used where the softLimitsSaveable flag is true
	 */
	protected void loadLimitsAndOffset() {
		ObjectInputStream in = null;
		try {
			in = new ObjectInputStream(new BufferedInputStream(
					new FileInputStream(limitsStore)));

			positionalValues = (PositionalValues) in.readObject();
			in.close();
			// now try to read the values from the file.
			softLimitLow = positionalValues.getLowerLimit();
			softLimitHigh = positionalValues.getUpperLimit();
			homeOffset = positionalValues.getHomeOffset();
			positionOffset = positionalValues.getPositionOffset();

		} catch (FileNotFoundException fnfe) {
			logger
					.error("Positioner.loadLimitsAndOffset: FileNotFoundException "
							+ fnfe.getMessage());
		} catch (IOException ioe) {
			logger.error("Positioner.loadLimitsAndOffset: IOException "
					+ ioe.getMessage());
		} catch (ClassNotFoundException e) {
			logger.error("Positioner.loadLimitsAndOffset: " + e.getMessage());
		}
	}

	/**
	 * Checks whether a move to a new position is in the opposite direction to
	 * the previous one (to allow moving away from limits).
	 * 
	 * @param newPosition
	 *            the new position
	 * @return true if the move is in the opposite direction to the previous
	 *         move false otherwise
	 */
	private boolean moveIsAway(double newPosition) {
		// Calculate the new direction
		int newDirection = calculateDirection(newPosition);

		// The starting value of lastDirection is 0, the move should be reckoned
		// be away from the limit in this case otherwise there is trouble moving
		// away from limits at start up.
		return (lastDirection == 0 || newDirection != lastDirection);
	}

	/**
	 * initiates a move by calling the subclass method
	 * 
	 * @param position
	 *            the position to move to
	 * @throws MotorException
	 */

	private void moveTo(double position) throws MotorException {
		logger.debug("Positioner " + getName()
				+ " setting backlashRequired to true");
		targetPosition = position;
		backlashRequired = true;
		// if the position correction/maintanence is to be done
		// increment the iteration count to keep the iterations
		// to a maximum number decided by the user
		if (positionCorrection) {
			positionCorrectionRequired = true;
			iterationCount++;
			// if it is the start of iterations store
			// the position for comparison
			if (iterationCount == START) {
				correctionTarget = targetPosition;
			}
		}

		lastDirection = calculateDirection(position);
		_moveTo(position);

		// startMonitoring is the flag used by the general (no time given)
		// wait to determine whether to continue or go back to waiting
		if (poll) {
			startMonitoring = true;
			synchronized (this) {
				notifyAll();
				logger.debug("notifyAll called by positioner " + this);
			}
		}
	}

	/**
	 * Makes the adjustment move if the motor hasn't reached the required
	 * position
	 * 
	 * @throws MotorException
	 */
	public void correctPosition() throws MotorException {
		// check if the iteration number of the adjustment move
		// is lesser than the maximum value
		if (iterationCount <= iterationMaxValue) {
			double diffPosition = correctionTarget - currentPosition;
			// check if the difference between the currentposition and
			// the required position is greater than the allowable deadband
			if (Math.abs(diffPosition) > iterationDeadBand) {
				moveTo(targetPosition + diffPosition);

			} else {
				iterationCount = NONE;
				positionCorrectionRequired = false;
			}
		} else {
			iterationCount = NONE;
			positionCorrectionRequired = false;
		}
	}

	/**
	 * initiates a home move by calling the motor home method
	 * 
	 * @throws MotorException
	 */
	private void homeMotor() throws MotorException {
		logger.debug("Positioner " + getName()
				+ " setting backlashRequired to true");
		backlashRequired = false;
		lastDirection = 0;
		motor.home();
		startMonitoring = true;
		synchronized (this) {
			notifyAll();
		}
	}

	/**
	 * This method should only be used where the softLimitsSaveable flag is true
	 */
	private void saveLimitsAndOffset() {
		ObjectOutputStream out = null;
		positionalValues.setLowerLimit(softLimitLow);
		positionalValues.setUpperLimit(softLimitHigh);
		positionalValues.setHomeOffset(homeOffset);
		positionalValues.setPositionOffset(positionOffset);
		try {
			out = new ObjectOutputStream(new BufferedOutputStream(
					new FileOutputStream(limitsStore)));

			out.writeObject(positionalValues);
			out.flush();
			out.close();
		} catch (IOException ioe) {
			logger.error("Positioner.saveLimitsAndOffset: IOException "
					+ ioe.getMessage());
		}

	}

	@Override
	public void setHomeOffset(Quantity offset) throws MoveableException {

		if (_setHomeOffset(offset)) {
			notifyIObservers(this, dofStatus);
			if (softLimitsSaveable) {
				saveLimitsAndOffset();
			}
		} else {
			throw new MoveableException(new MoveableStatus(
					MoveableStatus.INCORRECT_QUANTITY, getName(), offset),
					"Positioner.setHomeOffset: _setHomeOffset(offset) returned false. offset = "
							+ offset.toString());
		}
	}

	@Override
	public void setPositionOffset(Quantity offset) throws MoveableException {
		if (_setPositionOffset(offset)) {
			notifyIObservers(this, dofStatus);
			if (softLimitsSaveable) {
				saveLimitsAndOffset();
			}
		} else {
			throw new MoveableException(
					new MoveableStatus(MoveableStatus.INCORRECT_QUANTITY,
							getName(), offset),
					"Positioner.setPositionOffset: _setPositionOffset(offset) returned false. offset = "
							+ offset.toString());
		}

	}

	/**
	 * Sets a new value of dofStatus AND notifies IObservers of new status. NB
	 * Incorrectly named and confusingly used and vice versa.
	 * 
	 * @param dof_Status
	 *            the new dofStatus
	 */
	private void notify(MoveableStatus dof_Status) {
		this.dofStatus = dof_Status;
		logger.debug("Positioner " + getName()
				+ " notifying observers with status " + dof_Status);
		notifyIObservers(this, dof_Status);
	}

	/**
	 * Sets the current position of the Positioner by calling the subclass
	 * method
	 * 
	 * @param position
	 *            the new position
	 * @throws MotorException
	 */
	public void setPosition(double position) throws MotorException {
		double positionChange = 0.0;
		try {
			backlashRequired = false;
			positionChange = position - currentPosition;
			_setPosition(position);
			if (poll) {
				startMonitoring = true;
				synchronized (this) {
					notifyAll();
				}
			}
		} catch (MotorException me) {
			throw me;
		}

		setSoftLimits((softLimitLow + positionChange),
				(softLimitHigh + positionChange));

		// save the new limits to the store
		if (softLimitsSaveable) {
			saveLimitsAndOffset();
		}

		// update position and status
		refresh();
	}

	/**
	 * Sets a new status value from a new MotorStatus.NB This method
	 * (confusingly) does more than it say on the tin since it calls (the
	 * confusingly named) notify method which as well as actually setting the
	 * dofStatus field does a notifyIObservers.
	 * 
	 * @param newStatus
	 *            the new MotorStatus
	 */
	protected void setStatus(MotorStatus newStatus) {
		setStatus(newStatus, null);
	}

	/**
	 * Sets a new status value from a new MotorStatus. NB This method
	 * (confusingly) does more than it say on the tin since it calls (the
	 * confusingly named) notify method which as well as actually setting the
	 * dofStatus field does a notifyIObservers.
	 * 
	 * @param newStatus
	 *            the new MotorStatus
	 * @param message
	 *            a message we hope eventually to get into the DofStatus (so
	 *            don't remove it even though it's not used at the moment)
	 */
	private void setStatus(MotorStatus newStatus, String message) {
		MoveableStatus ds;

		if (message == null) {
			int msv = newStatus.value();
			int thisLimit = -1;
			ds = MoveableStatusFactory.createMoveableStatus(newStatus, id);

			if (msv == MotorStatus._UPPERLIMIT
					|| msv == MotorStatus._LOWERLIMIT) {
				if (msv == MotorStatus._UPPERLIMIT) {
					thisLimit = UPPERLIMIT;
				} else {
					thisLimit = LOWERLIMIT;

				}
				if (limitDirection[thisLimit] == 0) {
					// This is the first time a limit has been seen for this
					// Positioner in this direction, store the knowledge.
					limitDirection[thisLimit] = lastDirection;
				} else {
					// This combination of Positioner and limit has been seen
					// before, are we moving away from the limit.
					if (limitDirection[thisLimit] != lastDirection) {
						ds = new MoveableStatus(MoveableStatus.AWAY_FROM_LIMIT,
								getName(), id);
					}
				}
			}
		} else {
			ds = new MoveableStatus(MoveableStatus.ERROR, id, message);
			logger.debug("Positioner " + getName()
					+ " notifying with exception message \"" + message
					+ "\" in the status");
		}

		notify(ds);
	}

	/**
	 * Sets a new status value from a new MoveableStatus value. NB This method
	 * (confusingly) does more than it say on the tin since it calls (the
	 * confusingly named) notify method which as well as actually setting the
	 * dofStatus field does a notifyIObservers.
	 * 
	 * @param newStatus
	 *            the new MoveableStatus value
	 */
	private void setStatus(int newStatus) {
		notify(new MoveableStatus(newStatus, getName(), id));
	}

	/**
	 * Checks whether the current status of the Positiner allows a move to the
	 * specified new position
	 * 
	 * @param newPosition
	 *            the new position
	 * @return MoveableStatus.SUCCESS if the move is allowed or a MoveableStatus
	 *         indicating why it is not allowed.
	 */
	private int statusAllowsMove(double newPosition) {
		int check = MoveableStatus.SUCCESS;
		// if status from last move was anything other than positive, then
		// refresh the dofstatus
		// The motor may have recovered or it may throw another exception.
		// Note - do not call one of the (misnamed) setStatus methods with the
		// new status because we do not want to notifyIObservers at this point.
		if (exceptionCaughtInPollingThread
				|| (dofStatus.value() != MoveableStatus.SUCCESS && dofStatus
						.value() != MoveableStatus.READY)) {
			logger
					.warn("Positioner "
							+ getName()
							+ " trying to recover from exception caught in polling thread or rechecking STATUS_ERROR");
			exceptionCaughtInPollingThread = false;
			try {

				dofStatus = MoveableStatusFactory.createMoveableStatus(motor
						.getStatus());
				logger
						.info(
								"Positioner {} - motor has recovered to a sensible status {}",
								getName(), motor.getStatus());
			} catch (MotorException me) {
				dofStatus = MoveableStatusFactory
						.createMoveableStatus(me.status);
				logger.info(
						"Positioner {} - motor has thrown another exception",
						getName(), me);
			}
		}
		// SUCCESS is returned if the status is READY. If the status
		// is not READY or UPPERLIMIT or LOWERLIMIT then STATUS_ERROR
		// is returned. If the status is UPPERLIMIT or LOWERLIMIT then
		// the direction of the proposed move is checked. If it is away
		// from the limit then SUCCESS is returned. If not then INTO_LIMIT
		// is returned.
		if (dofStatus.value() != MoveableStatus.READY) {
			if (dofStatus.value() == MoveableStatus.UPPERLIMIT
					|| dofStatus.value() == MoveableStatus.LOWERLIMIT || dofStatus.value() == MoveableStatus.AWAY_FROM_LIMIT || dofStatus.value() == MoveableStatus.INTO_LIMIT) {
				iterationCount = NONE;
				if (!moveIsAway(newPosition)) {
					check = MoveableStatus.INTO_LIMIT;
				}
			} else {
				boolean motorMoving = false;
				try {
					motorMoving = motor.isMoving();

				} catch (Exception e) {
					exceptionUtils.logException(logger,
							"calling motor.isMoving", e);
				}
				if (motorMoving) {
					logger
							.error("Positioner.statusAllowsMove: Status busy returned from Positioner "
									+ this.getName()
									+ " - statusAllowsMove(). DOFstatus is "
									+ dofStatus.value()
									+ " "
									+ dofStatus.getMessage()
									+ (motorMoving ? "\nMotor is Moving"
											: "\nMotor is Moving"));
					check = MoveableStatus.BUSY;
				} else {
					// if(motor.)
					logger
							.error("Positioner.statusAllowsMove: Status error returned from Positioner "
									+ this.getName()
									+ " - statusAllowsMove(). DOFstatus is "
									+ dofStatus.value()
									+ " "
									+ dofStatus.getMessage()
									+ (motorMoving ? "\nMotor is Moving"
											: "\nMotor is not Moving"));
					check = MoveableStatus.STATUS_ERROR;
				}
			}
		}

		logger.debug("{} returning from statusAllowsMove {}", this.getName(),
				check);
		return check;
	}

	/**
	 * checks whether motor is still moving to determine whether polling loop
	 * should continue or notifyObservers should be called
	 * 
	 * @return true if moving else false
	 */
	private boolean stillMoving() {
		boolean isMoving = false;

		try {
			isMoving = motor.isMoving();
		} catch (MotorException mex) {
			logger.debug("Positioner " + getName()
					+ " caught MotorException in stillMoving");
		}

		return isMoving;
	}

	/**
	 * Tries to stop moving.
	 * 
	 * @throws MoveableException
	 */
	@Override
	public void stop() throws MoveableException {
		try {
			// These flags must be switched off otherwise if the motor does stop
			// the Positioner immediately begins the correction or backlash
			// move.
			positionCorrectionRequired = false;
			backlashRequired = false;
			motor.stop();
		}
		// If an exception is caught status should be set immediately rather
		// than waiting for the polling (or observing) of the motor to do
		// it.This also ensures that unlocking occurs because notification
		// of the bad status will pass upwards through the Moveable tree.
		// The MotorException is converted to a suitable MoveableException
		// which is thrown on.
		catch (MotorException me) {
			logger.debug("Positioner " + getName()
					+ " caught MotorException in stop");
			setStatus(me.status);
			throw new MoveableException(
					MoveableStatusFactory.createMoveableStatus(me.status,
							getName()),
					"Positioner.stop: caught MotorException " + me.getMessage(),
					me);
		}
	}

	/**
	 * Checks whether a position is within the soft limits.
	 * 
	 * @param newPosition
	 *            the new position
	 * @return true if within bounds; false otherwise
	 */
	public boolean withinLimits(double newPosition) {
		return (newPosition <= softLimitHigh && newPosition >= softLimitLow);
	}

	/**
	 * Sets the speed level. The value specified here is just passed on so it is
	 * the Motor which interprets the meaning of these speeds.
	 * 
	 * @param speed
	 *            the new speed level.
	 * @throws MoveableException
	 */
	@Override
	public void setSpeedLevel(int speed) throws MoveableException {
		try {
			motor.setSpeedLevel(speed);
		} catch (MotorException mex) {
			throw new MoveableException(new MoveableStatus(
					MoveableStatus.SPEEDLEVEL_ERROR, getName()),
					"Positioner.setSpeedlevel: caught MotorException "
							+ mex.getMessage(), mex);
		}
	}

	/**
	 * Implements the Runnable interface, used only when the motor is being
	 * polled.
	 */
	@Override
	public void run() {
		boolean first = true;

		MotorStatus motorStatus;
		if (runner == null) {
			logger.debug("runner null");

		}
		while (runner != null) {
			synchronized (this) {
				try {
					_updatePosition(motor.getPosition());
				} catch (MotorException me) {
					dofStatus = new MoveableStatus(MoveableStatus.ERROR, id, me
							.getMessage());
				}
				// NB DO NOT combine these two try - catch blocks. The wait must
				// happen
				// even if the getPosition() causes a MotorException.
				try {
					/*
					 * This 'first' test added to overcome a problem with
					 * program controlled multiple moves (e.g. by
					 * UndulatorMoveMediator). Not having it can lead to
					 * multiple notifications of the end of a move. This is
					 * normally alright when moving from OEMove but under
					 * program control another move may start immediately the
					 * first notify is done and then this second notification
					 * causes problems.
					 */
					if (first) {
						notifyIObservers(this, dofStatus);
						first = false;
					}
					/*
					 * Setting the waiting flag to true must be the last thing
					 * done before waiting The flag startMonitoring is set true
					 * by moveTo or setPosition
					 */
					waiting = true;
					while (!startMonitoring) {
						wait();
					}
					/* Setting the waiting flag to false must be the first */
					/* thing done before continuing. */
					waiting = false;
					startMonitoring = false;
				} catch (InterruptedException ex) {
					logger.error("Exception in Positioner infinite wait " + ex);
				}
			}

			/*
			 * this thread should only be awakened by the notify() calls in the
			 * moveTo and setPosition methods, it begins with the
			 * wait(pollTime), this implies that setPosition takes at least
			 * pollTime and moveTo takes at least 2 pollTime because the
			 * backlash check also takes pollTime
			 */

			do {
				synchronized (this) {
					try {
						wait(pollTime);
					} catch (Exception e) {
						logger.error("Exception in Positioner poll loop " + e);
					}
				}

				try {

					motorStatus = motor.getStatus();
					logger.debug("Positioner " + this.getName()
							+ " got status " + motorStatus.value()
							+ " from motor " + motor);
					_updatePosition(motor.getPosition());
					((MotorBase) motor).savePosition(motor.getName());

					/*
					 * If the motor returns a READY and backlash/ position
					 * correction has not been done then setStatus to BUSY and
					 * do the backlash/position correction
					 */
					if (motorStatus.value() == MotorStatus.READY.value()) {
						if (backlashRequired) {
							logger.debug(getName() + " starting backlash bit");

							backlashRequired = false;
							// The direction changes temporarily.
							lastDirection *= -1;
							_updatePosition(motor.getPosition());
							setStatus(MoveableStatus.BUSY);
							motor.correctBacklash();
							logger.debug(getName() + " ending backlash bit");
						}

						else if (positionCorrectionRequired) {
							positionCorrectionRequired = false;
							_updatePosition(motor.getPosition());
							try {
								setStatus(MoveableStatus.BUSY);
								correctPosition();
							} catch (MotorException me) {
								// abandon the position correction
								positionCorrectionRequired = false;
								setStatus(me.status);
							}
						}

						else {
							/*
							 * NB setStatus causes a notify and it is difficult
							 * to see why this one here does not cause
							 * additional READY notifies (see not at start of
							 * loop) but it seems not too.
							 */
							// The _updatePosition was added at version 6554
							// probably
							// in error as the run method is not used by
							// non-polling
							// motors
							// _updatePosition(motor.getPosition());
							setStatus(motorStatus);
						}
					} else if (motorStatus.value() == MotorStatus.BUSY.value()) {
						setStatus(motorStatus);
					}
					/*
					 * If the motor returns any other status then abandon
					 * backlash UNLESS the motor has returned a LIMIT but the
					 * move is away from it. The easiest way to determine if the
					 * latter has happened is to use setStatus to determine the
					 * new status for the Positioner and check its value
					 */
					else {
						setStatus(motorStatus);
						if (dofStatus.value() != MoveableStatus.AWAY_FROM_LIMIT) {
							backlashRequired = false;
						}
					}

				}
				/*
				 * if one of the motor methods generates an exception then
				 * setStatus based on that instead of the value returned by
				 * getStatus
				 */
				catch (MotorException me) {
					logger.error("Positioner " + getName()
							+ " caught MotorException in polling loop");
					motorStatus = me.status;
					exceptionCaughtInPollingThread = true;
					toBeThrownAsSoonAsPossible = new MoveableException(
							MoveableStatusFactory.createMoveableStatus(
									me.status, getName()), me.getMessage());
					break;
				}
			} while (stillMoving() || backlashRequired
					|| positionCorrectionRequired);

			if (!exceptionCaughtInPollingThread) {
				try {
					motorStatus = motor.getStatus();
					_updatePosition(motor.getPosition());
					((MotorBase) motor).savePosition(motor.getName());

				} catch (MotorException me) {
					logger.debug("Positioner " + getName()
							+ " caught MotorException after polling loop");
					motorStatus = me.status;
					exceptionCaughtInPollingThread = true;
					toBeThrownAsSoonAsPossible = new MoveableException(
							MoveableStatusFactory.createMoveableStatus(
									me.status, getName()), me.getMessage());
				}
			}
			// The notify within this setStatus should normally be the
			// one which tells observers that a move is over.
			if (exceptionCaughtInPollingThread) {
				setStatus(motorStatus, toBeThrownAsSoonAsPossible.getMessage());
			} else {
				setStatus(motorStatus);
			}
		}
	}

	/**
	 * Returns whether or not speed level is settable.
	 * 
	 * @return true
	 */
	@Override
	public boolean isSpeedLevelSettable() {
		return true;
	}

	/**
	 * Update motor position, status, and limits should they are changed in
	 * EPICS server.
	 * 
	 * @param theObserved
	 * @param changeCode
	 */
	@Override
	public void update(Object theObserved, Object changeCode) {
		synchronized (changeCode) {
			if (theObserved instanceof MotorProperty) {
				if ((MotorProperty) theObserved == MotorProperty.STATUS) {
					MotorStatus status = (MotorStatus) changeCode;
					setStatus(status);
				} else if (((MotorProperty) theObserved == MotorProperty.POSITION)) {
					_updatePosition((Double) changeCode);
				} else if (((MotorProperty) theObserved == MotorProperty.LOWLIMIT)) {
					Double ull = (Double) changeCode;
					// check if the steps per unit does not reverse this motor
					if (getStepsPerUnit() > 0) {
						_updateLimitLow(ull);
					} else {
						_updateLimitHigh(ull);
					}
				} else if (((MotorProperty) theObserved == MotorProperty.HIGHLIMIT)) {
					Double uhl = (Double) changeCode;
					// check if the steps per unit does not reverse this motor
					if (getStepsPerUnit() > 0) {
						_updateLimitHigh(uhl);
					} else {
						_updateLimitLow(uhl);
					}
				}
			} else if (theObserved instanceof Motor
					&& changeCode instanceof MotorEvent) {
				if ((MotorEvent) changeCode == MotorEvent.MOVE_COMPLETE
						|| (MotorEvent) changeCode == MotorEvent.REFRESH) {
					refresh();
				}
			}
		}
	}

	/**
	 * Forces an update of position and status by contacting the motor
	 * immediately.
	 */
	@Override
	public void refresh() {
		// If configuration has failed we want refresh to do nothing so that the
		// orginal error message can appear in OEMove.
		if (configured) {
			try {
				MotorStatus motorStatus = motor.getStatus();
				double motorPosition = motor.getPosition();
				_updatePosition(motorPosition);
				((MotorBase) motor)
						.savePosition(motor.getName(), motorPosition);
				setStatus(motorStatus);
			} catch (MotorException e) {
				logger.error(e + getClass().getName() + " " + getName()
						+ " method refresh()");
				setStatus(MotorStatus.FAULT, e.getMessage());
			}
		}
	}

	@Override
	public boolean isDirectlyUseable() {
		return false;
	}

	@Override
	public boolean isScannable() {
		return false;
	}

	@Override
	public int getProtectionLevel() {
		return 0;
	}

	/**
	 * Sets the reporting units. Reporting units of Positioners cannot be
	 * changed but this method must exist to fulfill the Moveable interface.
	 * 
	 * @param units
	 *            the new reporting units
	 */
	@Override
	public void setReportingUnits(Unit<? extends Quantity> units) {
		// Deliberately does nothing
	}

	/**
	 * Returns whether or not the current position is valid.
	 * 
	 * @return true (Positioner position is always valid).
	 */
	@Override
	public boolean isPositionValid() {
		return true;
	}

	/**
	 * Creates a MoveableCommandExecutor to carry out an absolute move to the
	 * specified position. Positioners can only be moved by DOFs but this method
	 * must exist for the Moveable interface so it returns null.
	 * 
	 * @param position
	 *            the position to move to
	 * @return a moveable command executor
	 * @throws MoveableException
	 */
	@Override
	public MoveableCommandExecutor createAbsoluteMover(Quantity position)
			throws MoveableException {
		return null;
	}

	/**
	 * Creates a MoveableCommandExecutor to carry out a relative move by the
	 * specified increment. Positioners can only be moved by DOFs but this
	 * method must exist for the Moveable interface so it returns null.
	 * 
	 * @param increment
	 *            the increment to move by
	 * @return a DOF command
	 * @throws MoveableException
	 */
	@Override
	public DOFCommand createRelativeMover(Quantity increment)
			throws MoveableException {
		return null;
	}

	/**
	 * Double position = (Double) changeCode;
	 * Message.debug("New Position in update() in Positioner " +
	 * position.doubleValue(), Message.Level.FOUR);
	 * _updatePosition(position.doubleValue()); Creates a
	 * MoveableCommandExecutor to set the current position to the specified
	 * value. Positioners can only have their positions set DOFs but this method
	 * must exist for the Moveable interface so it returns null.
	 * 
	 * @param position
	 *            the position to move to
	 * @return a DOF command
	 * @throws MoveableException
	 */
	@Override
	public DOFCommand createPositionSetter(Quantity position)
			throws MoveableException {
		return null;
	}

	/**
	 * Creates a MoveableCommandExecutor to carry out a homing move. specified
	 * position. Positioners cannot be homed but this method must exist for the
	 * Moveable interface so it returns null.
	 * 
	 * @param position
	 *            the position to move to
	 * @return a DOF command
	 * @throws MoveableException
	 */
	@Override
	public DOFCommand createHomer(Quantity position) throws MoveableException {
		return null;
	}

	@Override
	public String formatPosition(double position) {
		return null;
	}

	@Override
	public Quantity getSoftLimitLower() {
		return Quantity.valueOf(getSoftLimitLow(), getReportingUnits());
	}

	@Override
	public Quantity getSoftLimitUpper() {
		return Quantity.valueOf(getSoftLimitHigh(), getReportingUnits());
	}

	/**
	 * initialise motor properties from hardware or EPICS. Limited to 10 tries.
	 * 
	 * @throws MotorException
	 */
	public void propertyInitialisation() throws MotorException {
		try {
			final int LIMIT = 10;
			int i = 0;
			for (; i < LIMIT && !motor.isInitialised(); i++) {
				Thread.sleep(100);
			}
			if (i == LIMIT) {
				logger.error("Motor - " + motor.getName()
						+ " fails to initialise");
			}
		} catch (MotorException e1) {
			logger.error("Can NOT access motor.isInitialised() method.");
			throw e1;
		} catch (InterruptedException e1) {
			// noop
		}

		try {
			if (motor.isInitialised()) {
				_updatePosition(motor.getPosition()); // conversion using
				// steps per
				// unit and offset done in
				// _updatePosition

				// if steps per unit is negative, then need to reverse raw
				// limits
				// from motor
				if (getStepsPerUnit() > 0) {
					_updateLimitHigh(motor.getMaxPosition());
					_updateLimitLow(motor.getMinPosition());
				} else {
					_updateLimitHigh(motor.getMinPosition());
					_updateLimitLow(motor.getMaxPosition());
				}
				setStatus(motor.getStatus());
			}
		} catch (MotorException e) {
			exceptionUtils.logException(logger,
					"Motor Properties Initialisation failed !!!!", e);
			throw e;
		}
	}

	@Override
	public Object getAttribute(String name) throws MoveableException {
		Object attr = null;
		try {
			attr = motor.getAttribute(name);
		} catch (DeviceException e) {
			throw new MoveableException(MoveableStatusFactory
					.createMoveableStatus(MotorStatus.UNKNOWN, getName()), e
					.getMessage());
		}
		return attr;
	}

	@Override
	public void setAttribute(String name, Object value)
			throws MoveableException {
		try {
			motor.setAttribute(name, value);
		} catch (DeviceException e) {
			throw new MoveableException(MoveableStatusFactory
					.createMoveableStatus(MotorStatus.UNKNOWN, getName()), e
					.getMessage());
		}
	}
}
