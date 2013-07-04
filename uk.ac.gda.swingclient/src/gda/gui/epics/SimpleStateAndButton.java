/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package gda.gui.epics;

import gda.device.epicsdevice.ReturnType;

import java.awt.Color;
import java.awt.Font;
import java.awt.LayoutManager;
import java.awt.Dimension;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;

/**
 * SimpleStateAndButton Class
 */
public class SimpleStateAndButton extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * @param border
	 * @param layout
	 * @param constraints
	 * @param buttonText
	 * @param lblText
	 * @param buttonFirst
	 * @param buttonFont
	 * @param lblFont
	 * @param device
	 * @param record
	 * @param field
	 * @param commands
	 * @param defCommand
	 * @param lblTextMap
	 * @param labelColours
	 * @param defColour
	 * @param runner
	 */
	public SimpleStateAndButton(Border border, LayoutManager layout, Object constraints, String buttonText,
			String lblText, boolean buttonFirst, Font buttonFont, Font lblFont, String device, String record,
			String field, HashMap<String, String> commands, String defCommand, HashMap<String, String> lblTextMap,
			HashMap<String, Color> labelColours, Color defColour, ActionEventRunner runner) {
		this(false, border, layout, constraints, buttonText, lblText, buttonFirst, buttonFont, lblFont, device, record,
				field, commands, defCommand, lblTextMap, labelColours, defColour, runner, null);
	}

	/**
	 * @param testLayout
	 * @param border
	 * @param layout
	 * @param constraints
	 * @param buttonText
	 * @param lblText
	 * @param buttonFirst
	 * @param buttonFont
	 * @param lblFont
	 * @param device
	 * @param record
	 * @param field
	 * @param commands
	 * @param defCommand
	 * @param lblTextMap
	 * @param labelColours
	 * @param defColour
	 * @param runner
	 * @param minSize
	 */
	@SuppressWarnings("unused")
	public SimpleStateAndButton(boolean testLayout, Border border, LayoutManager layout, Object constraints,
			String buttonText, String lblText, boolean buttonFirst, Font buttonFont, Font lblFont, String device,
			String record, String field, HashMap<String, String> commands, String defCommand,
			HashMap<String, String> lblTextMap, HashMap<String, Color> labelColours, Color defColour,
			ActionEventRunner runner, Dimension minSize) {
		setLayout(layout);
		if (border != null)
			setBorder(border);
		JButton button = new JButton(buttonText);
		if (buttonFont != null)
			button.setFont(buttonFont);
		if (minSize != null)
			button.setMinimumSize(minSize);

		JLabel lbl = new JLabel(lblText);
		if (lblFont != null)
			lbl.setFont(lblFont);
		if (buttonFirst) {
			add(button, constraints);
			add(lbl);
		} else {
			add(lbl, constraints);
			add(button);
		}
		if (!testLayout) {
			button.addActionListener(runner != null ? runner : new ActionEventRunner(new SimpleEnumActionHandler(
					device, record, field, commands, defCommand)));
			new EpicsMonitor(ReturnType.DBR_CTRL, device, record, field, new EpicsStringLabel(lbl,
					lblTextMap, labelColours, defColour));
		}
	}
}
