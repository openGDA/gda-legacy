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

import gda.factory.FactoryException;
import gda.function.Function;
import gda.function.IdentityFunction;
import gda.oe.MoveableException;
import gda.oe.MoveableStatus;

import java.util.ArrayList;

import org.jscience.physics.quantities.Quantity;
import org.jscience.physics.units.Unit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Connects the movements of a set of Moveables. Functions are used to calculate the movements of all other Moveables
 * from that specified for the first Moveable. NB not yet completely general, the Moveables must actually be DOFs,
 * Positioners will not work.
 */
public class CoupledDOF extends DOF {
	private static final Logger logger = LoggerFactory.getLogger(CoupledDOF.class);

	private Function[] functions;

	private ArrayList<Function> functionList = new ArrayList<Function>();

	private ArrayList<String> functionNameList;

	/**
	 * Constructor.
	 */
	public CoupledDOF() {
	}

	/**
	 * @see gda.factory.Configurable#configure()
	 */
	@Override
	public void configure() throws FactoryException {
		super.configure();

		setFunctions();
		// the reporting units of moveable[0] will actually be used so
		// need to overide Castor's setting of acceptableUnits from XML and also
		// DOF's configuration
		setAcceptableUnits(moveables[0].getAcceptableUnits());
		setReportingUnits(getAcceptableUnits().get(0));
		initialiseFormatting();
		setCurrentQuantity(moveables[0].getPosition());

		updatePosition();
		updateStatus();
	}

	/**
	 * Adds a Function to the list of functions for this DOF. This method is specified in the mapping xml as the one
	 * which Castor will use to set Functions for the DOF.
	 * 
	 * @param function
	 *            the Function object to call
	 */
	public void addFunction(Function function) {
		logger.debug("Adding function " + function.getName() + " to function list");
		functionList.add(function);
	}

	/**
	 * Returns the list of Functions for this DOF. This method is specified in the mapping xml as the one which Castor
	 * will used to get Functions.
	 * 
	 * @return the list of functions as ArrayList<Function>
	 */
	public ArrayList<Function> getFunctionList() {
		return functionList;
	}

	/**
	 * Returns the list of function names for this DOF. This will be used if the database method of setting up is
	 * brought into operation.
	 * 
	 * @return the list of function names as ArrayList<String>
	 */
	public ArrayList<String> getFunctionNameList() {
		return functionNameList;
	}

	/**
	 * Sets the list of Functions. This method is not used directly in the project but has to exist for Castor.
	 * 
	 * @param functionList
	 *            list of functions for this DOF as ArrayList<Function>
	 */
	public void setFunctionList(ArrayList<Function> functionList) {
		this.functionList = functionList;
	}

	/**
	 * Sets the list of Function names. This will be used if the database method of setting up is brought into
	 * operation.
	 * 
	 * @param functionNameList
	 *            list of function names for this DOF as passed in as ArrayList<String>
	 */
	public void setFunctionNameList(ArrayList<String> functionNameList) {
		this.functionNameList = functionNameList;
	}

	@Override
	public void moveContinuously(int direction) throws MoveableException {
		// Deliberately does nothing (continuous movement would be far
		// too difficult for this DOF).
	}

	/**
	 * Sets the array of functions, checking that the first one is an identity function. This is called by configure in
	 * order to interpret the list of Functions created by Castor into an arrary.
	 * 
	 * @throws FactoryException
	 */
	private void setFunctions() throws FactoryException {
		int nosOfFunctions = functionList.size();
		functions = new Function[nosOfFunctions];

		for (int i = 0; i < functions.length; i++) {
			functions[i] = functionList.get(i);
			// The functions will have been created by Castor but
			// since they are not Findables will not have been
			// configured so we do that here.
			functions[i].configure();
		}

		if (!(functions[0] instanceof IdentityFunction))
			functions[0] = new IdentityFunction();
	}

	/**
	 * Called (via the general update method in DOF) when Moveables notify.
	 */
	@Override
	protected void updatePosition() {
		setCurrentQuantity(moveables[0].getPosition());

		// Create a Quantity of the correct subclass and calculate what the
		// positions of the moveables should be for that position. If any
		// moveable is not where it should be then mark the position as invalid.
		// Not currently in use but do not remove - see below and bug #79.
		// Quantity shouldBe[] =
		// calculateMoveables(checkTarget(getCurrentQuantity()));

		setPositionValid(true);
		for (int i = 1; i < moveables.length; i++)
			// This is what should happen, however this almost always
			// results in
			// positionValid false because of inaccuracies in the positions.
			// if (!moveables[i].getPosition().equals(shouldBe[i]))
			// So instead we do this, otherwise CoupledDOFs almost always
			// have
			// invalid position - see bug #79 for further information.
			if (!(moveables[i].getStatus().value() == MoveableStatus.READY)) {
				setPositionValid(false);
				break;
			}
	}

	/**
	 * Calculates the positions the moveables need to move to in order to go to position fromQuantity NB since this
	 * class specifies that the checkTarget and checkAndAddIncrement of moveable[0] are used then fromQuantity will have
	 * the correct units for moveable[0] (or be null).
	 * 
	 * @param fromQuantity
	 *            the Quantity for which Moveable positions are to be calculated
	 * @return an array of Quantities, the positions to which the individual Moveables must move in order to achieve
	 *         position of fromQuantity for the whole DOF.
	 */
	@Override
	protected Quantity[] calculateMoveables(Quantity fromQuantity) {
		Quantity rtrn[] = new Quantity[moveables.length];

		logger.debug("CoupledDOF calculateMoveables returning ");

		for (int i = 0; i < rtrn.length; i++) {
			rtrn[i] = functions[i].evaluate(fromQuantity);
			logger.debug("     " + rtrn[i]);
		}

		return (rtrn);
	}

	/**
	 * Returns the current position of the DOF
	 * 
	 * @return the current position
	 */
	@Override
	public Quantity getPosition() {
		// This DOF has the same acceptable units as moveable[0],
		// however at any given moment the reportingUnits here may be
		// different from those in moveables[0] and, more importantly
		// this DOF does not necessarily know how to do conversions.
		// For example if moveables[0] was a MonoDOF it could have
		// reportingUnits of Length, this could have reportingUnits
		// of Angle and in order to do the conversion this would need to
		// know twoD. The easiest solution is for this not to keep its
		// currentQuantity correct but to do this (i.e. get moveables[0]
		// to return its current position in the reporting units of this
		// DOF):
		return moveables[0].getPosition();
	}

	@Override
	public Quantity getPosition(Unit<? extends Quantity> reportingUnits) {
		// This DOF has the same acceptable units as moveable[0],
		// however at any given moment the reportingUnits here may be
		// different from those in moveables[0] and, more importantly
		// this DOF does not necessarily know how to do conversions.
		// For example if moveables[0] was a MonoDOF it could have
		// reportingUnits of Length, this could have reportingUnits
		// of Angle and in order to do the conversion this would need to
		// know twoD. The easiest solution is for this not to keep its
		// currentQuantity correct but to do this (i.e. get moveables[0]
		// to return its current position in the reporting units of this
		// DOF):
		return moveables[0].getPosition(reportingUnits);
	}

	/**
	 * Checks whether a target Quantity is allowed as a position for the DOF. This DOF delegates this to moveables[0].
	 * 
	 * @param target
	 *            the specified target
	 * @return the target if move would be allowed, null if target is invalid or move would not be allowed.
	 */
	@Override
	protected Quantity checkTarget(Quantity target) {
		return ((DOF) moveables[0]).checkTarget(target);
	}

	/**
	 * Check whether a specified increment is of the correct type and if it is return a target quantity for a move. This
	 * DOF delegates this to moveables[0].
	 * 
	 * @param increment
	 *            the increment requested
	 * @return the target position (or null if error or not possible).
	 */
	@Override
	protected Quantity checkAndAddIncrement(Quantity increment) {
		return ((DOF) moveables[0]).checkAndAddIncrement(increment);
	}

	/**
	 * Gets the units that this DOF will accept for specifiying movements. This DOF simply allows the same types as
	 * moveables[0].
	 * 
	 * @return an array of acceptable Unit classes //
	 */
	// public ArrayList<Unit<? extends Quantity>> getAcceptableUnits()
	// {
	// return ((DOF) moveables[0]).getAcceptableUnits();
	// }
	/**
	 * To allow checking of the DOFControl functionality CoupledDOFs do not allow their speedLevel to be set
	 * 
	 * @return true if the speed level is settable
	 */
	@Override
	public boolean isSpeedLevelSettable() {
		return false;
	}

	@Override
	protected void setDefaultAcceptableUnits() {
		defaultAcceptableUnits = moveables[0].getAcceptableUnits();
	}

	@Override
	protected void setValidAcceptableUnits() {
		validAcceptableUnits = new ArrayList<Unit<? extends Quantity>>();
		// acceptableUnits of moveables[0] will actually be used so we
		// must artificially make any XML entry invalid

		// validAcceptableUnits.add(SI.METER.getBaseUnits());
	}

	@Override
	public Quantity getSoftLimitLower() {
		// FIXME see commment in getPosition()
		return moveables[0].getSoftLimitLower();
	}

	@Override
	public Quantity getSoftLimitUpper() {
		return moveables[0].getSoftLimitUpper();
	}

	@Override
	public void setSpeed(Quantity speed) throws MoveableException {
		logger.debug("CoupledDOF setSpeed called with speed: " + speed);
	}

	@Override
	public void setSpeed(Quantity start, Quantity end, Quantity time) throws MoveableException {
		Quantity[] starts = calculateMoveables(start);
		Quantity[] ends = calculateMoveables(end);
		logger.debug("CoupledDOF setSpeed called with start, end, time: " + start + " " + end + " " + time);
		for (int i = 0; i < moveables.length; i++) {
			moveables[i].setSpeed(starts[i], ends[i], time);
		}
	}

	/**
	 * Gets the function list, needed by subclasses - shouldn't be needed elsewhere
	 * 
	 * @return Function[] array of IdentityFunctions
	 * @see gda.function.IdentityFunction
	 */
	protected Function[] getFunctions() {
		return functions;
	}
}