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

import gda.gui.oemove.DOFPositionDisplay;
import gda.observable.UpdateDelayer;
import gda.oe.MoveableException;
import gda.oe.MoveableStatus;
import gda.oe.OE;
import gda.oe.dofs.PolarizationValue;

import java.awt.Color;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class observes a DOF and displays its position in a text field.
 */

public class PolarizationDOFPositionDisplay extends JTextField implements DOFPositionDisplay {
	
	private static final Logger logger = LoggerFactory.getLogger(PolarizationDOFPositionDisplay.class);
	
	private OE oe;

	private String dofName;

	private Color defaultBackground = getBackground();

	private Color invalidBackground = Color.orange;

	// private JTextField textField;
	private boolean reportExceptions = true;

	/**
	 * Constructor which creates a dummy DOFPositionDisplay
	 * 
	 * @param oe
	 *            the OE
	 * @param dofName
	 *            the DOF name
	 */
	@SuppressWarnings("unused")
	public PolarizationDOFPositionDisplay(OE oe, String dofName) {
		this.oe = oe;
		this.dofName = dofName;

		setHorizontalAlignment(SwingConstants.CENTER);
		setToolTipText("Dummy");
		setEditable(false);
		setFont(new Font("Monospaced", Font.BOLD, 14));
		setColumns(16);

		setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Current Value",
				TitledBorder.CENTER, TitledBorder.TOP, null, Color.black));

		display();
		// Observe the OE via an UpdateDelayer so that the
		// update method does not need to bother with InvokeLater
		// oe.addIObserver(this);
		new UpdateDelayer(this, oe);
	}

	/**
	 * @param oe
	 * @param dofName
	 * @param columns
	 * @param border
	 */
	@SuppressWarnings("unused")
	public PolarizationDOFPositionDisplay(OE oe, String dofName, int columns, boolean border) {
		this.oe = oe;
		this.dofName = dofName;

		setHorizontalAlignment(SwingConstants.CENTER);
		setToolTipText("Current Position of DOF");
		setEditable(false);
		setFont(new Font("Monospaced", Font.BOLD, 14));

		setColumns(columns + 1);

		display();

		if (border) {
			setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Current Position",
					TitledBorder.CENTER, TitledBorder.TOP, null, Color.black));
		}
		// Foolish but essential kludge (see bug #364) - currently OEMove
		// creates
		// one instance with a border (in fact using the other constructor) and
		// another without. Then the slavery panel creates another instance. We
		// only want one of these to report exceptions so the reportExceptions
		// flag is determined by the border flag. This mecahnism causes least
		// disturbance to non-undulator code but is obviously deranged.
		reportExceptions = border;

		// Observe the OE via an UpdateDelayer so that the
		// update method does not need to bother with InvokeLater
		// oe.addIObserver(this);
		new UpdateDelayer(this, oe);
	}

	private void display() {
		try {
			double value = oe.getPosition(dofName).to(oe.getReportingUnits(dofName)).getAmount();
			setText(PolarizationValue.doubleToString(value));
			if (!oe.isPositionValid(dofName))
				setBackground(invalidBackground);
			else
				setBackground(defaultBackground);
		} catch (MoveableException dex) {
			logger.error("DOFPositionDisplay error accessing DOF " + dofName);
		}
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
		// DOFs such as UndulatorPolarizationDOF which use MultipleMover to
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

	/**
	 * @see gda.gui.oemove.DOFPositionDisplay#refresh()
	 */
	@Override
	public void refresh() {
		try {
			oe.refresh(dofName);
		} catch (MoveableException dex) {
			logger.error("DOFPositionDisplay error accessing DOF " + dofName);
		}
	}
}
