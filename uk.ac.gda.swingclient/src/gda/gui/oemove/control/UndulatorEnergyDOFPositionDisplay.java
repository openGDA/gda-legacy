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

import gda.oe.MoveableException;
import gda.oe.MoveableStatus;
import gda.oe.OE;

import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * UndulatorEnergyDOFPositionDisplay Class
 */
public class UndulatorEnergyDOFPositionDisplay extends DefaultDOFPositionDisplay {
	
	private static final Logger logger = LoggerFactory.getLogger(UndulatorEnergyDOFPositionDisplay.class);
	
	private boolean reportExceptions = true;

	/**
	 * @param oe
	 * @param dofName
	 */
	public UndulatorEnergyDOFPositionDisplay(OE oe, String dofName) {
		super(oe, dofName);
	}

	/**
	 * @param oe
	 * @param dofName
	 * @param columns
	 * @param border
	 */
	public UndulatorEnergyDOFPositionDisplay(OE oe, String dofName, int columns, boolean border) {
		super(oe, dofName, columns, border);
		// Foolish but essential kludge (see bug #364) - currently OEMove
		// creates
		// one instance with a border (in fact using the other constructor) and
		// another without. Then the slavery panel creates another instance. We
		// only want one of these to report exceptions so the reportExceptions
		// flag is determined by the border flag. This mecahnism causes least
		// disturbance to non-undulator code but is obviously deranged.
		reportExceptions = border;
	}

	@Override
	public void update(Object iObservable, Object arg) {
		logger.debug("DOFPositionDisplay (for DOF " + dofName + ") update called with " + iObservable + " " + arg);
		if (arg instanceof MoveableStatus) {
			MoveableStatus ds = ((MoveableStatus) arg);

			if (ds.getMoveableName().equals(dofName)) {
				display();
			}
		}
		// DOFs such as UndulatorEnergyDOF which use MultipleMover to
		// do their moves report errors in the move by notifying with a
		// MoveableException which contains the error message passed up from
		// below.
		else if (reportExceptions && arg instanceof MoveableException) {
			MoveableException me = (MoveableException) arg;
			if (me.getMoveableStatus().getMoveableName().equals(dofName)) {
				JOptionPane.showMessageDialog(getTopLevelAncestor(), me.getMessage(), "Error Message",
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}
}
