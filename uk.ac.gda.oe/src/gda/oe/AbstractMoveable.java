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

import gda.lockable.LockableComponent;
import gda.observable.IObserver;
import gda.observable.ObservableComponent;

import java.util.ArrayList;
import java.util.Stack;

import org.jscience.physics.quantities.Quantity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An incomplete implementation of Moveable which may be used as a base class.
 */
public abstract class AbstractMoveable implements Moveable {
	
	private static final Logger logger = LoggerFactory.getLogger(AbstractMoveable.class);
	
	private String name;

	private volatile LockableComponent moveLock = new LockableComponent();

	private ObservableComponent observableComponent = new ObservableComponent();

	private Stack<Quantity> speedStack = new Stack<Quantity>();

	// variables to hold the information for the DOF configuration.
	double[] lowerGdaLimits;
	double[] tolerances;
	double[] upperGdaLimits;
	
	// Variables to allow for automatic documentation
	private String docString = "";
	
	

	/**
	 * @return Returns the docString.
	 */
	@Override
	public String getDocString() {
		return docString;
	}

	/**
	 * @param docString The docString to set.
	 */
	@Override
	public void setDocString(String docString) {
		this.docString = docString;
	}

	/**
	 * The default implementation of this method does nothing because it should not really exist and is only necessary
	 * for DOFs for historical reasons.
	 * 
	 * @param moveableList
	 *            a list of moveables
	 */
	@Override
	public void setMoveableList(ArrayList<Moveable> moveableList) {
		// Deliberately do nothing.
	}

	/**
	 * Sets name of this Moveable.
	 * 
	 * @param name
	 *            Name of Moveable.
	 */
	@Override
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets name of this Moveable.
	 * 
	 * @return Name of Moveable.
	 */
	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean lockedFor(Object locker) {
		boolean locked = moveLock.lockedFor(locker);
		return locked;
	}

	@Override
	public boolean lock(Object locker) {
		return (moveLock.lock(locker));
	}

	@Override
	public boolean unLock(Object unLocker) {
		return (moveLock.unLock(unLocker));
	}

	@Override
	public void addIObserver(IObserver anIObserver) {
		observableComponent.addIObserver(anIObserver);
	}

	@Override
	public void deleteIObserver(IObserver anIObserver) {
		observableComponent.deleteIObserver(anIObserver);
	}

	@Override
	public void deleteIObservers() {
		observableComponent.deleteIObservers();
	}

	/**
	 * Notify observers of a change
	 * 
	 * @param theObserver
	 *            the object to be notified
	 * @param theArgument
	 *            the change to communicate to the observing object
	 */
	public void notifyIObservers(Object theObserver, Object theArgument) {
		observableComponent.notifyIObservers(theObserver, theArgument);
	}

	@Override
	public void pushSpeed() {
		Quantity speed = getSpeed();
		logger.debug("Moveable " + getName() + " pushing speed " + speed);
		speedStack.push(speed);
	}

	@Override
	public void popSpeed() {
		Quantity poppedSpeed = speedStack.pop();
		if (poppedSpeed != null) {
			try {
				setSpeed(poppedSpeed);
				logger.debug("Moveable " + getName() + "speed now set to popped speed " + poppedSpeed);
			} catch (MoveableException me) {
				logger.error("MoveableException in " + getName() + ".popSpeed(). Message: " + me.getMessage());
			}
		}
	}

	@Override
	public double[] getLowerGdaLimits() {
		return lowerGdaLimits;
	}

	@Override
	public double[] getTolerance() {
		return tolerances;
	}

	@Override
	public double[] getUpperGdaLimits() {
		return upperGdaLimits;
	}

	@Override
	public void setLowerGdaLimits(double lowerLim) throws MoveableException {
		lowerGdaLimits = new double[] {lowerLim};		
	}

	@Override
	public void setLowerGdaLimits(double[] lowerLim) throws MoveableException {
		lowerGdaLimits = lowerLim;		
	}
	
	@Override
	public void setTolerance(double tolerence) throws MoveableException {
		tolerances = new double[] {tolerence};		
	}

	@Override
	public void setTolerance(double[] tolerance) throws MoveableException {
		tolerances = tolerance;		
	}

	@Override
	public void setUpperGdaLimits(double upperLim) throws MoveableException {
		upperGdaLimits = new double[] {upperLim};		
	}

	@Override
	public void setUpperGdaLimits(double[] upperLim) throws MoveableException {
		upperGdaLimits = upperLim;		
	}
	
	
	
	
	
	
	
}
