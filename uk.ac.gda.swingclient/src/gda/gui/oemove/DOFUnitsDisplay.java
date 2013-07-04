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

package gda.gui.oemove;

import gda.observable.IObservable;

import org.jscience.physics.quantities.Quantity;
import org.jscience.physics.units.Unit;

/**
 * DOFUnitsDisplay interface
 */
public interface DOFUnitsDisplay extends IObservable {
	/**
	 * @param mode
	 */
	public void setUnits(int mode);

	/**
	 * @return units
	 */
	public Unit<? extends Quantity> getUnits();

	/**
	 * @param enabled
	 */
	public void setEnabled(boolean enabled);

}
