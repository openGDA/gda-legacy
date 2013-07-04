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

package gda.oe.util;

import org.jscience.physics.quantities.Quantity;

/**
 * Classes must implement this interface if they want to request moves from an UndulatorMoveCalculator. The inform
 * method will be called when the move has been done.
 */
public interface UndulatorMoveCalculatorWatcher {
	/**
	 * Undulator move calculator will call this to inform of new positions.
	 * 
	 * @param newEnergy
	 * @param newHarmonic
	 * @param newPolarization
	 * @param valid
	 */
	public void inform(Quantity newEnergy, Quantity newHarmonic, Quantity newPolarization, boolean valid);
}
