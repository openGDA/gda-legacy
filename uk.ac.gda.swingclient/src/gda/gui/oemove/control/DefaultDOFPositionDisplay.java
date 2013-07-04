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

import java.awt.Color;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import org.jscience.physics.quantities.Quantity;
import org.jscience.physics.units.Unit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class observes a DOF and displays its position in a text field.
 */
public class DefaultDOFPositionDisplay extends JTextField implements DOFPositionDisplay {
	
	private static final Logger logger = LoggerFactory.getLogger(DefaultDOFPositionDisplay.class);
	
	private OE oe;

	protected String dofName;

	private Color defaultBackground;

	private Color invalidBackground = Color.orange;

	/**
	 * Constructor which creates a dummy DOFPositionDisplay
	 * 
	 * @param oe
	 *            the oe
	 * @param dofName
	 *            the name of the DOF
	 */
	@SuppressWarnings("unused")
	public DefaultDOFPositionDisplay(OE oe, String dofName) {
		this.oe = oe;
		this.dofName = dofName;

		JLabel jl = new JLabel();
		defaultBackground = jl.getBackground();

		setHorizontalAlignment(SwingConstants.CENTER);
		setToolTipText("Current position");
		setEditable(false);
		setFont(new Font("Monospaced", Font.BOLD, 14));
		setColumns(14);
		setBackground(defaultBackground);

		setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Current Position",
				TitledBorder.CENTER, TitledBorder.TOP, null, Color.black));

		if (oe != null) {
			display();

			// Observe the OE via an UpdateDelayer so that the
			// update method does not need to bother with InvokeLater
			// oe.addIObserver(this);
			new UpdateDelayer(this, oe);
		}
	}

	/**
	 * @param oe
	 * @param dofName
	 * @param columns
	 * @param border
	 */
	@SuppressWarnings("unused")
	public DefaultDOFPositionDisplay(OE oe, String dofName, int columns, boolean border) {
		this.oe = oe;
		this.dofName = dofName;

		JLabel jl = new JLabel();
		defaultBackground = jl.getBackground();
		setHorizontalAlignment(SwingConstants.CENTER);
		setToolTipText("Current Position of DOF");
		setEditable(false);
		setFont(new Font("Monospaced", Font.BOLD, 14));
		setBackground(defaultBackground);

		setColumns(columns + 1);
		display();

		if (border) {
			setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Current Position",
					TitledBorder.CENTER, TitledBorder.TOP, null, Color.black));
		}

		// Observe the OE via an UpdateDelayer so that the
		// update method does not need to bother with InvokeLater
		// oe.addIObserver(this);
		new UpdateDelayer(this, oe);
	}

	protected void display() {
		try {
			Unit<? extends Quantity> unit = oe.getReportingUnits(dofName);
			double value = oe.getPosition(dofName, unit).to(unit).getAmount();

			if (Double.isNaN(value)) {
				setText("NaN");
				setBackground(invalidBackground);
			} else {
				setText(oe.formatPosition(dofName, value));

				if (!oe.isPositionValid(dofName))
					setBackground(invalidBackground);
				else
					setBackground(defaultBackground);
			}
		} catch (MoveableException e) {
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
	}

	/**
	 * @see gda.gui.oemove.DOFPositionDisplay#refresh()
	 */
	@Override
	public void refresh() {
		try {
			oe.refresh(dofName);
		} catch (MoveableException e) {
			logger.error("DOFPositionDisplay error accessing DOF " + dofName);
		}
	}
}