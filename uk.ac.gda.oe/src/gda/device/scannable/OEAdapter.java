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

package gda.device.scannable;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.lockable.Locker;
import gda.observable.IObserver;
import gda.oe.MoveableException;
import gda.oe.MoveableStatus;
import gda.oe.OE;
import gda.util.QuantityFactory;

import java.util.ArrayList;

import org.apache.commons.lang.ArrayUtils;
import org.jscience.physics.quantities.Quantity;
import org.jscience.physics.units.Unit;
import org.python.core.PyString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A wrapper for OEs such that OEs can be operated on in the Jython environment. This is a composite of the Scannable
 * and OE interfaces.
 */
public class OEAdapter extends ScannableBase implements Scannable, OE {
	
	private static final Logger logger = LoggerFactory.getLogger(OEAdapter.class);
	
	private OE theOE;

	/**
	 * This is the Jython documentation. Use it in the GDA Jython via the help command.
	 */
	static {
		__doc__ = "This is an OE. This is a logical group of movements (DOFs).";
	}
	/**
	 * @param theOE
	 */
	public OEAdapter(OE theOE) {
		this.theOE = theOE;

		// set the output format correctly
		String[] format = new String[0];
		for (int i = 0; i < theOE.getDOFNames().length; i++) {
			format = (String[]) ArrayUtils.add(format, "%5.5g");
		}
		this.setOutputFormat(format);

		// set the input names
		this.inputNames = this.getDOFNames();
	}

	// methods to fulfil the Scannable interface

	@Override
	public boolean isBusy() {
		try {
			return theOE.isMoving();
		} catch (MoveableException e) {
			logger.debug("OEAdapter: error while calling OE " + theOE.getName() + " isMoving()");
		}
		return false;
	}

	@Override
	public Object getPosition() throws DeviceException {

		double[] positions = new double[this.getDOFNames().length];

		try {
			for (int i = 0; i < this.getDOFNames().length; i++) {
				positions[i] = this.getPosition(this.getDOFNames()[i]).getAmount();
			}
		} catch (MoveableException e) {
			throw new DeviceException(this.getName() + ": getPositions: " + e.getMessage());
		}

		return positions;

	}

	@Override
	public void asynchronousMoveTo(Object position) throws DeviceException {

		Double[] target = ScannableUtils.objectToArray(position);

		try {
			if (target.length == this.inputNames.length) {
				for (int i = 0; i < this.inputNames.length; i++) {
					Quantity thisTarget = QuantityFactory.createFromTwoStrings(target[i].toString(), this.theOE
							.getReportingUnits(this.theOE.getDOFNames()[i]).toString());
					this.theOE.moveTo(this.theOE.getDOFNames()[i], thisTarget);
				}
			} else {
				throw new DeviceException("Cannot move OE as supplied array had " + target.length
						+ " elements but expected " + this.inputNames.length);
			}
		} catch (MoveableException e) {
			throw new DeviceException("Exception while moving OE: " + e.getMessage());
		}
	}

	/**
	 * @return the OE this object encapsulates
	 */
	public OE getOE() {
		return theOE;
	}

	@Override
	public String toFormattedString() {
		try {
			if (theOE.getDOFNames() == null) {
				return getName();
			}
			String output = getName() + ":\n";

			for (String name : theOE.getDOFNames()) {
				output += name + " : " + this.formatPosition(name, this.getPosition(name, getReportingUnits(name)).to(getReportingUnits(name)).getAmount());

				if (getReportingUnits(name) != null) {
					output += " " + getReportingUnits(name).toText();
				}

				if (getSoftLimitLower(name) != null && getSoftLimitUpper(name) != null) {
					output += " (" + this.formatPosition(name, this.getSoftLimitLower(name).getAmount()) + " : "+ this.formatPosition(name, this.getSoftLimitUpper(name).getAmount()) + ")";
				output +="\n";
				}
			}
			return output;
		} catch (MoveableException e) {
			return this.getName();
		}
	}
	
	/**
	 * Jython method to return a string representation of the object
	 * 
	 * @return the result of the toString method
	 */
	@Override
	public PyString __repr__() {
		return __str__();
	}

	@Override
	public String[] getInputNames() {
		return this.getDOFNames();
	}

	// to fulfil the OE interface

	/**
	 * @param dofName
	 * @return String
	 * @throws MoveableException
	 */
	@Override
	public String getDOFType(String dofName) throws MoveableException {
		return theOE.getDOFType(dofName);
	}

	@Override
	public String[] getDOFNames() {
		return theOE.getDOFNames();
	}

	@Override
	public void moveBy(String dofname, Quantity increment) throws MoveableException {
		theOE.moveBy(dofname, increment);
	}

	@Override
	public void moveTo(String dofname, Quantity position) throws MoveableException {
		theOE.moveTo(dofname, position);
	}

	@Override
	public int moveLockedTo(String dofname, Quantity position, int lockId) throws MoveableException {
		return theOE.moveLockedTo(dofname, position, lockId);
	}

	@Override
	public void moveContinuously(String dofname, int direction) throws MoveableException {
		theOE.moveContinuously(dofname, direction);
	}

	@Override
	public void stop(String dofname) throws MoveableException {
		theOE.stop(dofname);
	}

	@Override
	public void stop() {
		try {
			theOE.stop();
		} catch (MoveableException e) {
			// cannot throw anything as there is clash between OE and
			// Scannable interfaces (at present)
			logger.warn(getName() + ": exception during stop(): " + e.getMessage());
		}
	}

	@Override
	public boolean isMoving() throws MoveableException {
		return theOE.isMoving();
	}

	@Override
	public boolean isMoving(String dofname) throws MoveableException {
		return theOE.isMoving(dofname);
	}

	@Override
	public void setPosition(String dofname, Quantity position) throws MoveableException {
		theOE.setPosition(dofname, position);
	}

	@Override
	public Quantity getPosition(String dofname) throws MoveableException {
		return theOE.getPosition(dofname);
	}

	@Override
	public Quantity getPosition(String dofname, Unit<? extends Quantity> units) throws MoveableException {
		return theOE.getPosition(dofname, units);
	}

	@Override
	public boolean isPositionValid(String dofname) throws MoveableException {
		return theOE.isPositionValid(dofname);
	}

	@Override
	public boolean isSpeedLevelSettable(String dofname) throws MoveableException {
		return theOE.isSpeedLevelSettable(dofname);
	}

	@Override
	public ArrayList<Unit<? extends Quantity>> getAcceptableUnits(String dofname) throws MoveableException {
		return theOE.getAcceptableUnits(dofname);
	}

	@Override
	public void setReportingUnits(String dofname, Unit<? extends Quantity> units) throws MoveableException {
		theOE.setReportingUnits(dofname, units);
	}

	@Override
	public Unit<? extends Quantity> getReportingUnits(String dofname) throws MoveableException {
		return theOE.getReportingUnits(dofname);
	}

	@Override
	public int getProtectionLevel(String dofname) throws MoveableException {
		return theOE.getProtectionLevel(dofname);
	}
	
	@Override
	public int getProtectionLevel() throws DeviceException {
		return 0;
	}
	
	@Override
	public void setProtectionLevel(int newLevel) throws DeviceException{
		//do nothing
	}
	
	@Override
	public MoveableStatus getStatus(String dofname) throws MoveableException {
		return theOE.getStatus(dofname);
	}

	@Override
	public void setSpeed(String dofname, Quantity speed) throws MoveableException {
		theOE.setSpeed(dofname, speed);
	}

	@Override
	public void setSpeed(String dofname, Quantity start, Quantity end, Quantity time) throws MoveableException {
		theOE.setSpeed(dofname, start, end, time);
	}

	@Override
	public Quantity getSpeed(String dofname) throws MoveableException {
		return theOE.getSpeed(dofname);
	}

	@Override
	public void setSpeedLevel(String dofname, int speed) throws MoveableException {
		theOE.setSpeedLevel(dofname, speed);
	}

	@Override
	public void home(String dofName) throws MoveableException {
		theOE.home(dofName);
	}

	@Override
	public Quantity getHomeOffset(String dofName) throws MoveableException {
		return theOE.getHomeOffset(dofName);
	}

	@Override
	public void setHomeOffset(String dofName, Quantity offset) throws MoveableException {
		theOE.setHomeOffset(dofName, offset);
	}

	@Override
	public Quantity getSoftLimitLower(String dofname) throws MoveableException {
		return theOE.getSoftLimitLower(dofname);
	}

	@Override
	public Quantity getSoftLimitUpper(String dofname) throws MoveableException {
		return theOE.getSoftLimitUpper(dofname);
	}

	@Override
	public int lock(String dofname) throws MoveableException {
		return theOE.lock(dofname);
	}

	@Override
	public int lock(String dofname, Locker locker) throws MoveableException {
		return theOE.lock(dofname, locker);
	}

	@Override
	public void pushSpeed(String dofname) throws MoveableException {
		theOE.pushSpeed(dofname);
	}

	@Override
	public void popSpeed(String dofname) throws MoveableException {
		theOE.popSpeed(dofname);
	}

	@Override
	public void unlock(String dofname, int lockId) throws MoveableException {
		theOE.unlock(dofname, lockId);
	}

	@Override
	public int moveCheck(String dofname, Quantity position) throws MoveableException {
		return theOE.moveCheck(dofname, position);
	}

	@Override
	public boolean isScannable(String dofname) throws MoveableException {
		return theOE.isScannable(dofname);
	}

	@Override
	public void refresh(String dofname) throws MoveableException {
		theOE.refresh(dofname);
	}

	@Override
	public void addIObserver(IObserver anIObserver) {
		theOE.addIObserver(anIObserver);
	}

	@Override
	public void deleteIObserver(IObserver anIObserver) {
		theOE.deleteIObserver(anIObserver);
	}

	@Override
	public void deleteIObservers() {
		theOE.deleteIObservers();
	}

	@Override
	public String[] getMoveableNames(String name) throws MoveableException {
		return theOE.getMoveableNames(name);
	}

	@Override
	public Quantity getPositionOffset(String dofName) throws MoveableException {
		return theOE.getPositionOffset(dofName);
	}

	@Override
	public void setPositionOffset(String dofName, Quantity offset) throws MoveableException {
		theOE.setPositionOffset(dofName, offset);
	}

	@Override
	public void setDeviceAttribute(String dofname, String name, Object value) throws MoveableException {
		theOE.setDeviceAttribute(dofname, name, value);
	}

	@Override
	public Object getDeviceAttribute(String dofname, String name) throws MoveableException {
		return theOE.getDeviceAttribute(dofname, name);
	}

	@Override
	public String formatPosition(String dofname, double position) throws MoveableException {
		return String.format(getOutputFormat()[0], position);
	}
	
	// Methods for propagating beamline configuration manager methods
	
	/**
	 * @see gda.oe.OE#getLowerGdaLimits(java.lang.String)
	 */
	@Override
	public double[] getLowerGdaLimits(String dofname) throws MoveableException {
		return theOE.getLowerGdaLimits(dofname);
	}

	/**
	 * @see gda.oe.OE#getTolerance(java.lang.String)
	 */
	@Override
	public double[] getTolerance(String dofname) throws MoveableException {
		return theOE.getTolerance(dofname);
	}

	/**
	 * @see gda.oe.OE#getUpperGdaLimits(java.lang.String)
	 */
	@Override
	public double[] getUpperGdaLimits(String dofname) throws MoveableException {
		return theOE.getUpperGdaLimits(dofname);
	}

	/**
	 * @see gda.oe.OE#setLowerGdaLimits(java.lang.String, double)
	 */
	@Override
	public void setLowerGdaLimits(String dofname, double lowerLim) throws MoveableException {
		theOE.setLowerGdaLimits(dofname, lowerLim);	
	}

	/**
	 * @see gda.oe.OE#setLowerGdaLimits(java.lang.String, double[])
	 */
	@Override
	public void setLowerGdaLimits(String dofname, double[] lowerLim) throws MoveableException {
		theOE.setLowerGdaLimits(dofname, lowerLim);
	}

	/**
	 * @see gda.oe.OE#setTolerance(java.lang.String, double)
	 */
	@Override
	public void setTolerance(String dofname, double tolerance) throws MoveableException {
		theOE.setTolerance(dofname, tolerance);
	}

	/**
	 * @see gda.oe.OE#setTolerance(java.lang.String, double[])
	 */
	@Override
	public void setTolerance(String dofname, double[] tolerance) throws MoveableException {
		theOE.setTolerance(dofname, tolerance);			
	}

	/**
	 * @see gda.oe.OE#setUpperGdaLimits(java.lang.String, double)
	 */
	@Override
	public void setUpperGdaLimits(String dofname, double upperLim) throws MoveableException {
		theOE.setUpperGdaLimits(dofname, upperLim);	
	}

	/**
	 * @see gda.oe.OE#setUpperGdaLimits(java.lang.String, double[])
	 */
	@Override
	public void setUpperGdaLimits(String dofname, double[] upperLim) throws MoveableException {
		theOE.setUpperGdaLimits(dofname, upperLim);	
	}

	@Override
	public String getDocString(String dofname) throws MoveableException {
		return theOE.getDocString(dofname);
	}

	@Override
	public void setDocString(String dofname, String docString)
			throws MoveableException {
		theOE.setDocString(dofname, docString);
		
	}
}