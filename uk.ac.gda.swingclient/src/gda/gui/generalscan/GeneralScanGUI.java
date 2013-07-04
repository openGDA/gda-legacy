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

import gda.util.MessageOutput;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * GUI for setting up simple scans of any DOF
 */

public class GeneralScanGUI extends JPanel {

	private static MessageOutput messageOutput;

	private GeneralScanPanel generalscanPanel;

	/**
	 * Main program to run the GUI standalone
	 * 
	 * @param args
	 *            the program arguments
	 */
	public static void main(String[] args) {
		JFrame frame = new JFrame();

		messageOutput = new MessageOutput("Messages");

		frame.getContentPane().add(new GeneralScanGUI());
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
	 * Constructor for use by general GUI.
	 */
	public GeneralScanGUI() {
		generalscanPanel = new GeneralScanPanel();
		init();
	}

	private void init() {
		setLayout(new BorderLayout());

		add(generalscanPanel, BorderLayout.CENTER);
		add(new JScrollPane(messageOutput), BorderLayout.SOUTH);

		generalscanPanel.addQuitButton();
	}
}
