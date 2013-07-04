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

package gda.gui.text.TextArea;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.junit.Ignore;
import org.junit.Test;

/**
 * SimpleScrollableLoggerPaneTest Class
 */
public class SimpleScrollableLoggerPaneTest implements ActionListener {
	static JButton testButton = null;

	static JButton testClearButton = null;

	static SimpleScrollableLoggerPane simpleScrollableLoggerPane = null;

	static SimpleScrollableLoggerPaneTest me = new SimpleScrollableLoggerPaneTest();

	/**
	 * This is a dummy test, since no real JUnit tests exist in this class.
	 */
	@Ignore("Incomplete test class - it does contain not any Junit-runnable methods.")
	@Test
	public void dummyTest() {
	// TODO: Remove dummyTest and add some Junit-runnable methods (add @Test where appropriate).
	}

	private static void createAndShowGUI() {
		// Create and set up the window.

		// Add contents to the window.
		Frame frame = (Frame) SwingUtilities.getAncestorOfClass(Frame.class, null);
		JDialog dialog = new JDialog(frame, true);
		JPanel panel = new JPanel();
		simpleScrollableLoggerPane = new SimpleScrollableLoggerPane("Test");
		testButton = new JButton("test");
		testButton.addActionListener(me);
		testClearButton = new JButton("clear");
		testClearButton.addActionListener(me);
		panel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = c.gridy = 0;
		c.fill = GridBagConstraints.BOTH;
		panel.add(simpleScrollableLoggerPane, c);
		c.gridx++;
		panel.add(testButton, c);
		c.gridy++;
		panel.add(testClearButton, c);
		dialog.add(panel);
		dialog.pack();
		dialog.setResizable(true);
		dialog.setContentPane(panel);
		dialog.setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		if (e.getSource() == testButton)
			for (int i = 1; i < 10; i++)
				simpleScrollableLoggerPane.getPrintWriter().println("This is a test");
		if (e.getSource() == testClearButton)
			simpleScrollableLoggerPane.getSimpeLogger().clear();
		simpleScrollableLoggerPane.ResizeContent();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				// Turn off metal's use of bold fonts
				UIManager.put("swing.boldMetal", Boolean.FALSE);
				createAndShowGUI();
			}
		});
	}

}
