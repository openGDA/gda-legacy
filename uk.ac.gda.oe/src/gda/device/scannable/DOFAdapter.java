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

import gda.data.nexus.NexusException;
import gda.data.nexus.NexusFileInterface;
import gda.data.nexus.NexusUtils;
import gda.device.DeviceException;
import gda.device.scannable.ScannableUtils.ScannableValidationException;
import gda.jython.accesscontrol.AccessDeniedException;
import gda.oe.MoveableException;
import gda.oe.OE;
import gda.util.QuantityFactory;
import gda.util.exceptionUtils;

import java.util.regex.Pattern;

import org.jscience.physics.quantities.Quantity;
import org.jscience.physics.units.Unit;
import org.python.core.Py;
import org.python.core.PyException;
import org.python.core.PyFloat;
import org.python.core.PyInteger;
import org.python.core.PyNone;
import org.python.core.PyObject;
import org.python.core.PySlice;
import org.python.core.PyString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Acts as a wrapper for a DOF. This makes interaction with an OE simpler, especially when interacting via Jython. For
 * example in Jython, declare:
 * </p>
 * <p>
 * myMono = DOFAdapter(mono,'WaveLength')
 * </p>
 * <p>
 * and
 * </p>
 * <p>
 * mono.moveTo('WaveLength',Quantity(10.0,length.MM))
 * </p>
 * <p>
 * becomes:
 * </p>
 * <p>
 * myMono.moveTo(10.0)
 * </p>
 */
public class DOFAdapter extends ScannableMotionBase {
	
	private static final Logger logger = LoggerFactory.getLogger(DOFAdapter.class);

	/**
	 * This is the Jython documentation. Use it in the GDA Jython via the help command.
	 */
	@SuppressWarnings("hiding")
	public String __doc__ = "This is a DOF. This controls the operation of a single motor or a group of motors whose position is represented by a single number.";

	String dofname = null;

	OE oe = null;

	/**
	 * Simple helper function to convert a double[] to a Double[]
	 * @param array
	 * @return the new Array
	 */
	private Double[] toDouble(double[] array) {
		
		Double[] result = new Double[array.length];
		for(int i = 0; i < array.length; i++) {
			result[i] = array[i];
		}
		return result;
	}
	
	/**
	 * @param oe
	 * @param dofname
	 * @throws ScannableValidationException
	 */
	public DOFAdapter(OE oe, String dofname) throws ScannableValidationException {
		this.oe = oe;
		this.dofname = dofname;
		setName(dofname);
		
		// on construction, we also need to tag through the parameters which are relevent to the 
		// beamline configuration manager
		try {
			this.setLowerGdaLimits(toDouble(oe.getLowerGdaLimits(dofname)));
		} catch (Exception e) {
			// don't worry about this, if the value hasent been set in the CASTOR, then it isnt a loss here
		}
		
		try {
			double[] oeTolerance = oe.getTolerance(dofname);
			Double[] tolerence = new Double[oeTolerance.length];
			for (int element  = 0; element < oeTolerance.length; element++){
				tolerence[element] = oeTolerance[element];
			}
			this.setTolerances(tolerence);
		} catch (Exception e) {
			// don't worry about this, if the value hasent been set in the CASTOR, then it isnt a loss here
		}
		
		try {
			this.setUpperGdaLimits(toDouble(oe.getUpperGdaLimits(dofname)));
		} catch (Exception e) {
			// don't worry about this, if the value hasent been set in the CASTOR, then it isnt a loss here
		}
		
		// As a last step, lets try to pull out the documentation.
		try {
			__doc__ = this.oe.getDocString(dofname);
		} catch (MoveableException e) {
			// This isnt a major loss if it dosn't find it, log the error however
			logger.warn("Cannot find documentation for "+ dofname + " due to exception" + exceptionUtils.getFullStackMsg(e));
		}
		
		ScannableUtils.validate(this);
	}

	/**
	 * {@inheritDoc} Will check checkPositionValid() method which has been overridden from ScannableMotionBase to also
	 * check DOF limits. Will throw an exception if move is illegal.
	 * 
	 * @see gda.device.scannable.ScannableBase#asynchronousMoveTo(java.lang.Object)
	 */
	@Override
	public void asynchronousMoveTo(Object position) throws DeviceException {
		Quantity newPosition = null;
		String s = position.toString();

		//FIXME Somebody should look at this!		
		// * Do some converting (cross fingers!)

		// somewhere between the tempScript2 file (which contains the
		// correct string) and the attempt by jython to invoke this method
		// string for micrometers loses its mu and gains an unprintable
		// character.
		// This superkludge gets round this but urgently needs fixing properly.
		// See bug #352.

		if (s.indexOf(65533) > 0) {
			StringBuffer superKludge = new StringBuffer(s.length());
			for (int i = 0; i < s.length(); i++)
				if (s.charAt(i) != 65533)
					superKludge.append(s.charAt(i));
				else
					superKludge.append("\u00B5");
			s = new String(superKludge);
		}

		try {
			// check if position pure number or a mixture of letters and
			// numbers
			CharSequence temp = s.subSequence(0, s.length());

			if (Pattern.matches("\\-?\\d+\\.?\\d*[eE\\-]*\\d*\\D+", temp)) {
				newPosition = QuantityFactory.createFromString(s);
			} else {
				newPosition = QuantityFactory.createFromTwoStrings(s, oe.getReportingUnits(dofname).toString());
			}

			// * Check the position is valid (limits and so on)
			String reply = this.checkPositionValid(newPosition);
			if (reply != null) {
				throw new DeviceException(getName() + ": invalid asynchMoveTo() position; problem is: " + reply);
			}

			// * Do the move
			oe.moveTo(dofname, newPosition);

		} catch (MoveableException e) {
			throw new DeviceException(getName() + ": error during asynchronousMoveTo: " + e.getMessage()
					+ (e.getCause() != null ? (" " + e.getCause().getMessage()) : "") + ". MoveableStatus =  "
					+ e.getMoveableStatus().getMessage(), e);
		} catch (AccessDeniedException e) {
			throw new AccessDeniedException(getName() + ": error during asynchronousMoveTo: " + e.getMessage()
					+ (e.getCause() != null ? (" " + e.getCause().getMessage()) : "") + ".");
		} catch (Exception e) {
			throw new DeviceException(getName() + ": error during asynchronousMoveTo: " + e.getMessage()
					+ (e.getCause() != null ? (" " + e.getCause().getMessage()) : ""), e);
		}
	}

	@Override
	public void stop() {
		// calls the underlying OE's stop method for one dof
		try {
			oe.stop(dofname);
		} catch (Exception ex) {
		}
	}

	/**
	 * Returns the dof's current position. This should be in the form of a double or an array of doubles to help users
	 * in the scripting environment. {@inheritDoc}
	 * 
	 * @see gda.device.scannable.ScannableBase#getPosition()
	 */
	@Override
	public Object getPosition() {
		try {
			// bug #964 refresh to ensure reported position upto date.
			oe.refresh(dofname);
			Unit<? extends Quantity> unit = oe.getReportingUnits(dofname);
			return oe.getPosition(dofname, unit).to(unit).getAmount();
		} catch (Exception ex) {
			logger.error("DOFAdapter: getPosition " + ex.getMessage());
			return null;
		}
	}

	@Override
	public boolean isBusy() throws DeviceException {
		try {
			return oe.isMoving(this.dofname);
		} catch (MoveableException e) {
			throw new DeviceException("Error in isBusy for " + getName(),e);
		}
	}
	
	@Override
	public int getProtectionLevel() throws DeviceException{
		try {
			return oe.getProtectionLevel(dofname);
		} catch (MoveableException e) {
			throw new DeviceException(e.getMessage());
		}
	}
	
	@Override
	public void setProtectionLevel(int newLevel) throws DeviceException{
		//do nothing
	}

	/**
	 * Returns the OE this DOF is part of.
	 * 
	 * @return OE
	 */
	public OE getOE() {
		return oe;
	}

	/**
	 * Returns the name of the DOF.
	 * 
	 * @return String
	 */
	public String getDofname() {
		return dofname;
	}

	/**
	 * Updates the reporting units currently in use.
	 * 
	 * @param unit
	 */
	public void setReportingUnits(Unit<? extends Quantity> unit) {
		try {
			oe.setReportingUnits(dofname, unit);
		} catch (MoveableException e) {
			logger.error("DOFAdapter " + e.getMessage());
		}
	}

	/**
	 * @return the reporting units used by the underlying DOF
	 */
	public Unit<? extends Quantity> getReportingUnits() {
		try {
			return oe.getReportingUnits(dofname);
		} catch (MoveableException e) {
			logger.error("DOFAdapter.getReportingUnits " + e.getMessage());
		}
		return null;
	}

	@Override
	public String[] getInputNames() {
		String[] temp = new String[1];
		temp[0] = dofname;
		return temp;
	}

	@Override
	public PyObject __getitem__(PyObject index) throws PyException {
		if (index instanceof PyInteger) {
			final PyInteger pyInt = (PyInteger) index;
			if ((pyInt).getValue() < __len__()) {
				return getJythonPosition();
			} 
			PyException ex = new PyException();
			ex.value = new PyString( String.format("index out of range: %d", pyInt.getValue()));
			ex.type = Py.TypeError;
			throw ex;
		} else if (index instanceof PySlice) {
			// only react if the command was [0] or [:]
			PySlice slice = (PySlice) index;

			int start, stop, step;

			// start
			if (slice.start instanceof PyNone) {
				start = 0;
			} else {
				start = ((PyInteger) slice.start).getValue();
			}

			// stop
			if (slice.stop instanceof PyNone) {
				stop = this.__len__() - 1;
			} else {
				stop = ((PyInteger) slice.stop).getValue();
			}

			// step
			if (slice.step instanceof PyNone) {
				step = 1;
			} else {
				step = ((PyInteger) slice.step).getValue();
			}

			if (stop < this.__len__() && start == 0 && step <= 1) {
				return getJythonPosition();
			}
		}
		PyException ex = new PyException();
		ex.value = new PyString("__getitem()__ parameter was not PyInteger or PySlice");
		ex.type = Py.TypeError;
		throw ex;
	}

	@Override
	public String toFormattedString() {
		try {
			String output = "";

			if (getReportingUnits() == null) {
				output = getName() + " : " + String.format(getOutputFormat()[0], getPosition());
			} else {
				output = getName() + " : "
						+ String.format(getOutputFormat()[0], Double.parseDouble(getPosition().toString())) + " "
						+ getReportingUnits().toString();
			}

			// Display the scannable limits
			// Print limits if they are set for this input
			// Check which are set being very careful of nulls!
			Double lowerLim = null;
			Double upperLim = null;
			{
				Double [] limits=getLowerGdaLimits();
				if (limits != null && limits[0] != null) {
					lowerLim = limits[0];
				}
			}
			{
				Double [] limits=getUpperGdaLimits();
				if (limits != null && limits[0] != null) {
					upperLim = limits[0];
				}
			}

			if (lowerLim != null | upperLim != null) {
				output += " gda(";
				if (lowerLim != null) {
					output += lowerLim;
				} else {
					output += " ";
				}
				output += " : ";
				if (upperLim != null) {
					output += upperLim;
				} else {
					output += " ";
				}
				output += ")";

			}

			// Display the DOF limits
			if (oe.getSoftLimitLower(dofname) != null && oe.getSoftLimitUpper(dofname) != null) {
				output += " dof(" + String.format(getOutputFormat()[0], oe.getSoftLimitLower(dofname).getAmount())
						+ " : " + String.format(getOutputFormat()[0], oe.getSoftLimitUpper(dofname).getAmount()) + ")";
			}

			return output;
		} catch (Exception e) {
			return getName() + ". DOF.toString:exception = " + e.getMessage();
		}
	}

	private PyFloat getJythonPosition() {
		try {
			Unit<? extends Quantity> unit = oe.getReportingUnits(dofname);
			return new PyFloat(oe.getPosition(dofname, unit).to(unit).getAmount());
		} catch (Exception ex) {
			logger.error("DOFAdapter: getPosition " + ex.getMessage());
			PyException pyEx = new PyException();
			pyEx.value = new PyString("DOFAdatper: getPosition" + ex.getMessage());
			pyEx.type = Py.TypeError;
			throw pyEx;
		}
	}

	/**
	 * @see gda.oe.OE#getSoftLimitLower(String)
	 * @return the lower soft limit from the DOF in string form.
	 */
	public String getSoftLimitLower() {
		try {
			Quantity limit = oe.getSoftLimitLower(dofname);
			return limit.toString();
		} catch (MoveableException e) {
			logger.debug("Error while retrieving soft limit for: " + oe.getName() + " " + e.getMessage());
			return null;
		}
	}

	/**
	 * @see gda.oe.OE#getSoftLimitUpper(String)
	 * @return the upper soft limit from the DOF in string form.
	 */
	public String getSoftLimitUpper() {
		try {
			Quantity limit = oe.getSoftLimitUpper(dofname);
			return limit.toString();
		} catch (MoveableException e) {
			logger.debug("Error while retrieving soft limit for: " + oe.getName() + " " + e.getMessage());
			return null;
		}
	}

	/**
	 * Home the dof.
	 */
	public void home() {
		try {
			this.oe.home(this.dofname);
		} catch (MoveableException e) {
			logger.debug("Error while retrieving homing: " + oe.getName() + " " + e.getMessage());
		}
	}

	/**
	 * {@inheritDoc} Overrides ScanabbleMotionBase, to also check DOF limits its(including EPICs soft limits).
	 * 
	 * @see gda.device.Scannable#checkPositionValid(java.lang.Object)
	 */
	// TODO: finalise super.checkPositionValid() when possible
	@Override
	public String checkPositionValid(Object position) {
		// Check against oe's soft limits, and then against the standard scannable limits.

		try {
			Quantity newPosition = Quantity.valueOf(position.toString());
			Quantity upper = this.oe.getSoftLimitUpper(this.dofname);
			Quantity lower = this.oe.getSoftLimitLower(this.dofname);
			

			if (lower.getAmount() > upper.getAmount()) {
				Quantity temp = upper;
				upper = lower;
				lower = temp;
			}
			//Make sure it is not a dimensionless Quantity
			if( newPosition.getUnit() != Unit.ONE && !newPosition.getUnit().equals(upper.getUnit()))
			{
				if(newPosition.isGreaterThan(upper) || newPosition.isLessThan(lower))
					return String.format("target position (%s) is outside of DOF/EPICs limits (%s, %s )", newPosition
							.toText(), lower.toText(), upper.toText());
					
			}

			else if (!(newPosition.getAmount() >= lower.getAmount() && newPosition.getAmount() <= upper.getAmount())) {
				return String.format("target position (%s) is outside of DOF/EPICs limits (%s, %s )", newPosition
						.toText(), lower.toText(), upper.toText());
			}
			
			try {
				return super.checkPositionValid(position);
			} catch (DeviceException e) {
				throw new RuntimeException("error occurred during validation: " + e.getMessage());
			}

		}
		catch (MoveableException e) {
			throw new RuntimeException("error occurred during validation: " + e.getMessage());
		}
	}

	/**
	 * Implements the setPosition method from the OE interface for the DOF wrapped by this object.
	 * 
	 * @param position
	 */
	public void setPosition(double position) {
		try {
			Quantity newPosition = Quantity.valueOf(position + " " + oe.getReportingUnits(dofname).toString());

			this.oe.setPosition(this.dofname, newPosition);
		} catch (MoveableException e) {
			logger.debug("Error while calling setPosition: " + oe.getName() + " " + e.getMessage());
			logger.error(getName() + " : setPosition failed: " + e.getMessage());
		}
	}

	@Override
	public Object getAttribute(String name) throws DeviceException {
		try {
			return oe.getDeviceAttribute(dofname, name);
		} catch (MoveableException e) {
			throw new DeviceException(e.getMessage(), e.getCause());
		}
	}

	@Override
	public void setAttribute(String name, Object o) throws DeviceException {
		try {
			if (name != null && o != null) {
				oe.setDeviceAttribute(dofname, name, o);
			}
		} catch (MoveableException e) {
			throw new DeviceException(e.getMessage(), e.getCause());
		}

	}

	@Override
	protected void writeNeXusInformationLimits(NexusFileInterface file) throws NexusException {
		NexusUtils.writeNexusString(file, "soft_limit_min", getSoftLimitLower());
		NexusUtils.writeNexusString(file, "soft_limit_max", getSoftLimitUpper());
	}
}