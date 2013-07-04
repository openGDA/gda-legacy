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

package gda.gui.polarimetry;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * A JPanel to allow the title and condition fields for a Polarimeter type header to be entered.
 */
public class PolarimeterHeaderPanel extends JPanel {
	private JPanel titlePanel;
	private JTextField titleField;
	private JTextField commentField;

	private static final String LAYOUT_HORIZONTAL = BorderLayout.EAST;

	/**
	 * Creates a Polarimeter header panel.
	 */
	public PolarimeterHeaderPanel() {
		this(LAYOUT_HORIZONTAL);
	}

	/**
	 * Creates a Polarimeter header panel using the specified layout.
	 * 
	 * @param layout
	 *            the layout
	 */
	public PolarimeterHeaderPanel(@SuppressWarnings("unused") String layout) {
		constructBits();
		add(titlePanel, BorderLayout.CENTER);
	}

	private void constructBits() {
		setLayout(new FlowLayout());
		titleField = new JTextField(35);
		titleField.setToolTipText("Text to appear in Polarimeter Header title field");
		commentField = new JTextField(35);
		commentField.setToolTipText("Text to appear in Polarimeter Header comment field");

		titlePanel = new JPanel(new FlowLayout());
		titlePanel.add(new JLabel(" Title "));
		titlePanel.add(titleField);
		titlePanel.add(new JLabel(" Comment "));
		titlePanel.add(commentField);

	}

	/**
	 * Returns the header.
	 * 
	 * @return the header
	 */
	public ArrayList<String> getHeader() {
		ArrayList<String> header = new ArrayList<String>();
		header.add(titleField.getText());
		header.add(commentField.getText());
		return header;

	}
}
