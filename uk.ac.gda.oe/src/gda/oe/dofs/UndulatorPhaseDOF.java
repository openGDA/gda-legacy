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
import gda.oe.MoveableStatus;
import gda.oe.positioners.UndulatorPhasePositioner;

import org.jscience.physics.quantities.Quantity;

/**
 * A DOF responsible for linear motion using one linear moveable.
 */
public class UndulatorPhaseDOF extends SingleAxisLinearDOF {
	private String mode;

	@Override
	public void configure() throws FactoryException {
		super.configure();
	}

	/**
	 * @return Returns the mode.
	 */
	public String getMode() {
		return mode;
	}

	/**
	 * @param mode
	 *            The mode to set.
	 */
	public void setMode(String mode) {
		this.mode = mode;
	}

	/**
	 * UpdatePosition method used to calculate currentQuantity from position of moveabless, called from update method of
	 * DOF
	 */
	@Override
	protected void updatePosition() {
		// FIXME: this is one of several instances of DOFs needing to know
		// that their Moveables are actually positioners. In this case a
		// particular subclass of Positioner - this may be unavoidable.
		UndulatorPhasePositioner upp = (UndulatorPhasePositioner) moveables[0];

		// If this DOF has the same mode as that currently set in the
		// UndulatorPhasePositioner then position is valid. If the
		// position of the UndulatorPhasePositioner is within the
		// zero phase tolerance then both PhaseDOFs show valid.
		// IS THIS REALLY CORRECT BEHAVIOUR?

		setCurrentQuantity(moveables[0].getPosition());
		if (mode.equals(upp.getMode()) || upp.positionIsZero())
			setPositionValid(true);
		else
			setPositionValid(false);
	}

	@Override
	public synchronized int checkMoveTo(Quantity position, Object mover) {
		UndulatorPhasePositioner upp = (UndulatorPhasePositioner) moveables[0];

		int check = MoveableStatus.MOVE_NOT_ALLOWED;

		if (mode.equals(upp.getMode())) {
			check = super.checkMoveTo(position, mover);
		} else {
			if (upp.positionIsZero()) {
				upp.setMode(mode);
				check = super.checkMoveTo(position, mover);
			}
		}

		return check;
	}

	@Override
	public boolean isSpeedLevelSettable() {
		return false;
	}
}