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

package gda.util.converters;

import gda.oe.Moveable;

import java.util.ArrayList;

import org.jscience.physics.quantities.Quantity;
import org.jscience.physics.units.Unit;

/**
 * Used by <code>gda.oe.dofs.GenQuantitiesConverter</code> to provide a converter that simply returns the source
 * quantity,
 */
public class NullConverter implements IQuantitiesConverter {
	private final Moveable moveable;

	/**
	 * @param moveable
	 */
	public NullConverter(Moveable moveable) {
		this.moveable = moveable;
	}

	@Override
	public ArrayList<ArrayList<Unit<? extends Quantity>>> getAcceptableUnits() {
		ArrayList<ArrayList<Unit<? extends Quantity>>> units = new ArrayList<ArrayList<Unit<? extends Quantity>>>();
		units.add(moveable.getAcceptableUnits());
		return units;
	}

	@Override
	public Quantity[] calculateMoveables(Quantity[] sources, Object[] moveables) {
		return sources;
	}

	@Override
	public Quantity[] toSource(Quantity[] targets, Object[] moveables) {
		return targets;
	}

	@Override
	public ArrayList<ArrayList<Unit<? extends Quantity>>> getAcceptableMoveableUnits() {
		return getAcceptableUnits();
	}

	@Override
	public boolean sourceMinIsTargetMax() {
		return false;
	}

}
