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

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a simple extension of JComboBox which allows fixed polarization types to be displayed and changed.
 */
public class HarmonicDOFInputDisplay extends JComboBox implements DOFInputDisplay {
	
	private static final Logger logger = LoggerFactory.getLogger(HarmonicDOFInputDisplay.class);
	
	/**
	 * Constructor
	 */
	public HarmonicDOFInputDisplay() {
		// Kludge the spaces in the items are to keep the
		// size big enough to see the border's "Change To"
		addItem("   1   ");
		addItem("   2   ");
		addItem("   3   ");
		setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Change To",
				TitledBorder.CENTER, TitledBorder.TOP, null, Color.black));

		// This stops the selecting action from actually starting a move
		// (see OEControl).
		setActionCommand("Do-nothing");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		final JFrame frame = new JFrame();

		JPanel jPanel = new JPanel();

		final HarmonicDOFInputDisplay pde = new HarmonicDOFInputDisplay();
		jPanel.add(pde);
		JButton jButton = new JButton("PressMe");
		jPanel.add(jButton);

		jButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				logger.debug("the value is " + pde.getValue());
			}
		});
		frame.getContentPane().add(jPanel);

		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent ev) {
				System.exit(0);
			}
		});
		frame.pack();
		frame.setVisible(true);
	}

	/**
	 * {@inheritDoc} Returns harmonic (1, 2 or 3)
	 * 
	 * @return harmonic
	 * @see gda.gui.oemove.DOFInputDisplay#getValue()
	 */
	@Override
	public Double getValue() {
		return new Double(getSelectedIndex() + 1);
	}

	@Override
	public void setValue(String value) {
		logger.info("setting harmonic to " + value);
		setSelectedItem(value);
	}

	@Override
	public void setMode(int newMode)

	{
		// Does nothing deliberately
	}

	@Override
	public String toString() {
		return NOTSAVEABLE;
	}

	/**
	 * {@inheritDoc} Overrides the super class method to ensure that "Start" cannot be set as the actionCommand. (See
	 * OEControlPanel.update()).
	 * 
	 * @param command
	 *            the command
	 * @see javax.swing.JComboBox#setActionCommand(java.lang.String)
	 */
	@Override
	public void setActionCommand(String command) {
		String toSet;

		if (command.equals("Start")) {
			toSet = "Do-nothing";
		} else {
			toSet = command;
		}
		// logger.debug("AHA " + command + " -> " + toSet);
		super.setActionCommand(toSet);
	}

}
