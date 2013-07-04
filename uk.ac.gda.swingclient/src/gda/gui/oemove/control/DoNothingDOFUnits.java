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

package gda.gui.oemove.control;

import gda.gui.oemove.DOFUnitsDisplay;
import gda.observable.IObserver;

import javax.swing.JLabel;

import org.jscience.physics.quantities.Quantity;
import org.jscience.physics.units.Unit;

/**
 * DoNothingDOFUnits JLabel Class
 */
public class DoNothingDOFUnits extends JLabel implements DOFUnitsDisplay {
	private Unit<? extends Quantity> units = null;

	/**
	 * @param units
	 */
	public DoNothingDOFUnits(Unit<? extends Quantity> units) {
		this.units = units;
	}

	@Override
	public Unit<? extends Quantity> getUnits() {
		return units;
	}

	@Override
	public void setUnits(int mode) {
		// Deliberately do nothing.
	}

	@Override
	public void setEnabled(boolean enabled) {
		// Deliberately do nothing
	}

	@Override
	public void addIObserver(IObserver anIObserver) {
		// Deliberately do nothing
	}

	@Override
	public void deleteIObserver(IObserver anIObserver) {
		// Deliberately do nothing
	}

	@Override
	public void deleteIObservers() {
		// Deliberately do nothing
	}

}
