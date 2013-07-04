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

import gda.observable.IObserver;
import gda.observable.UpdateDelayer;
import gda.oe.MoveableException;
import gda.oe.MoveableStatus;
import gda.oe.OE;

import javax.swing.JLabel;
import javax.swing.SwingConstants;

import org.jscience.physics.quantities.Quantity;
import org.jscience.physics.units.Unit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A JLabel extension which keeps an up to date display of the units of a Moveable but does not allow them to be
 * changed. Can be used in panels other than OEMove which display positions of Moveables
 */
public class DOFUnitsDisplayRepeater extends JLabel implements IObserver {
	
	private static final Logger logger = LoggerFactory.getLogger(DOFUnitsDisplayRepeater.class);

	private OE oe;

	private String dofName;

	/**
	 * @param oe
	 * @param dofName
	 */
	@SuppressWarnings("unused")
	public DOFUnitsDisplayRepeater(OE oe, String dofName) {
		this.oe = oe;
		this.dofName = dofName;
		setHorizontalAlignment(SwingConstants.LEFT);
		if (oe != null) {
			display();

			// Observe the OE via an UpdateDelayer so that the
			// update method does not need to bother with InvokeLater
			new UpdateDelayer(this, oe);
		}
	}

	private void display() {
		try {
			Unit<? extends Quantity> unit = oe.getReportingUnits(dofName);
			setText(" " + unit.toString());
		} catch (MoveableException e) {
			logger.error("DOFUnitsDisplayRepeater error accessing DOF " + dofName);
		}
	}

	@Override
	public void update(Object iObservable, Object arg) {
		if (arg instanceof MoveableStatus) {
			MoveableStatus ds = ((MoveableStatus) arg);

			if (ds.getMoveableName().equals(dofName)) {
				display();
			}
		}
	}
}
