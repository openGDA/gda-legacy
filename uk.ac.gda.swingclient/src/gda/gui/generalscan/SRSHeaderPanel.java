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

package gda.gui.generalscan;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * A JPanel to allow the title and condition fields for an SRS type header to be entered.
 */
public class SRSHeaderPanel extends JPanel {
	private JPanel titlePanel;

	private JPanel conditionsPanel;

	private JTextField titleField;

	private JTextField conditionOneField;

	private JTextField conditionTwoField;

	private JTextField conditionThreeField;

	/**
	 * 
	 */
	public static final String LAYOUT_HORIZONTAL = BorderLayout.EAST;

	/**
	 * 
	 */
	public static final String LAYOUT_VERTICAL = BorderLayout.SOUTH;

	/**
	 * 
	 */
	public SRSHeaderPanel() {
		this(LAYOUT_HORIZONTAL);
	}

	/**
	 * @param layout
	 */
	public SRSHeaderPanel(String layout) {
		constructBits(layout);
		add(titlePanel, BorderLayout.CENTER);
		add(conditionsPanel, layout);
	}

	private void constructBits(String layout) {
		setLayout(new BorderLayout());
		titleField = new JTextField(20);
		titleField.setToolTipText("Text to appear in SRS Header SRSTLE field");
		conditionOneField = new JTextField(8);
		conditionOneField.setToolTipText("Text to appear in SRS Header SRSCN1 field");
		conditionTwoField = new JTextField(8);
		conditionTwoField.setToolTipText("Text to appear in SRS Header SRSCN2 field");
		conditionThreeField = new JTextField(8);
		conditionThreeField.setToolTipText("Text to appear in SRS Header SRSCN3 field");

		titlePanel = new JPanel(new BorderLayout());
		titlePanel.add(new JLabel(" Title "), BorderLayout.WEST);
		titlePanel.add(titleField, BorderLayout.CENTER);

		if (layout.equals(LAYOUT_HORIZONTAL)) {
			conditionsPanel = new JPanel(new GridLayout(1, 0));
		} else {
			conditionsPanel = new JPanel(new GridLayout(0, 2));
		}
		conditionsPanel.add(new JLabel(" Condition 1"));
		conditionsPanel.add(conditionOneField);
		conditionsPanel.add(new JLabel(" Condition 2"));
		conditionsPanel.add(conditionTwoField);
		conditionsPanel.add(new JLabel(" Condition 3"));
		conditionsPanel.add(conditionThreeField);
	}

	/**
	 * @return ArrayList<String>
	 */
	public ArrayList<String> getHeader() {
		ArrayList<String> header = new ArrayList<String>();
		header.add(titleField.getText());
		header.add(conditionOneField.getText());
		header.add(conditionTwoField.getText());
		header.add(conditionThreeField.getText());
		return header;

	}
}
