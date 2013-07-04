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

import gda.gui.oemove.DOFInputDisplay;
import gda.oe.MoveableException;
import gda.oe.OE;

import java.awt.Color;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

/**
 */
public class DefaultDOFInputDisplay extends JTextField implements DOFInputDisplay {
	private String[] borderTitles = { "Input Increment", "Input Increment", "Input Position", "Input Position",
			"Home Position", "Home Position" };

	private int currentMode;

	private int previousMode;

	private String savedValue;

	private OE oe;

	private String dofName;

	/**
	 * @param oe
	 * @param dofName
	 */
	public DefaultDOFInputDisplay(OE oe, String dofName) {
		super(14);
		this.oe = oe;
		this.dofName = dofName;
		setHorizontalAlignment(SwingConstants.CENTER);
		setFont(new Font("Monospaced", Font.BOLD, 14));
		setToolTipText("Sign specifies direction to move in.");
		// setMode(DOFMode.ABSOLUTE);
		// setActionCommand("Start");
	}

	@Override
	public void setValue(String value) {
		setText(value);
	}

	@Override
	public Double getValue() {
		Double value = null;
		try {

			value = new Double(getText());
		} catch (NumberFormatException nex) {
			if (getText().equals(""))
				JOptionPane.showMessageDialog(getTopLevelAncestor(), "No increment or position has been specified.\n"
						+ "Input a numeric value.", "No Increment or Position", JOptionPane.ERROR_MESSAGE);
			else
				JOptionPane.showMessageDialog(getTopLevelAncestor(),
						"The required increment or position can not be interpreted.\n" + "Input a numeric value.",
						"Increment or Position Error", JOptionPane.ERROR_MESSAGE);
		}
		return value;
	}

	@Override
	public void setMode(int newMode) {
		previousMode = currentMode;
		currentMode = newMode;

		setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), borderTitles[currentMode],
				TitledBorder.CENTER, TitledBorder.TOP, null, Color.black));

		try {
			if (currentMode == DOFMode.HOME || currentMode == DOFMode.HOME_SET) {
				if (previousMode != DOFMode.HOME && previousMode != DOFMode.HOME_SET)
					savedValue = getText();

				setText(oe.formatPosition(dofName, oe.getHomeOffset(dofName).doubleValue()));
			} else if (previousMode == DOFMode.HOME || previousMode == DOFMode.HOME_SET) {
				setText(savedValue);
			}
		} catch (MoveableException de) {
			setText("");
		}
	}

	@Override
	public String toString() {
		return getText();
	}
}
