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

package gda.gui.text.parameter;

import java.awt.Component;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.VetoableChangeListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.text.JTextComponent;

import org.junit.Ignore;

/**
 * ParametersPanelBuilderTest
 */
@Ignore("Not a JUnit test class")
public class ParametersPanelBuilderTest {
	/**
	 * ParametersPanelTestListener
	 */
	public class ParametersPanelTestListener implements VetoableChangeListener {

		private final JTextComponent textComponent;

		/**
		 * @param textComponent
		 */
		public ParametersPanelTestListener(JTextComponent textComponent) {
			this.textComponent = textComponent;
		}

		@Override
		public void vetoableChange(PropertyChangeEvent e) {
			Object source = e.getSource();
			if ((source == null) || !(source instanceof ParametersPanelBuilder.ParameterChangeEventSource)) {
				throw new IllegalArgumentException(
						"ParametersPanelTest.propertyChange -  (source == null ) || !(source instanceof ParameterChangeEventSource)");
			}
			ParametersPanelBuilder.ParameterChangeEventSource parameterChangeEventSource = (ParametersPanelBuilder.ParameterChangeEventSource) source;

			Double newValue = null;
			Object newObject = e.getNewValue();
			if ((newObject != null) && (newObject instanceof Double)) {
				newValue = (Double) newObject;
			}

			Double oldValue = null;
			Object oldObject = e.getOldValue();
			if ((oldObject != null) && (oldObject instanceof Double)) {
				oldValue = (Double) oldObject;
			}

			if (oldValue != null && newValue != null) {
				java.util.List<Limited> limiteds = parameterChangeEventSource.parametersPanelBuilder
						.getParameters();
				Double total = 0.;
				for (Limited l : limiteds) {
					if (l != null && l.val instanceof Double)
						total = total + (Double) l.val;
				}
				total = total - oldValue;
				total = total + newValue;

				if (total > 100) {
					textComponent.setText("No good - total is more than 100");
				} else {
					textComponent.setText("OK - total is <= 100");
				}

			}
			parameterChangeEventSource.parameterField.setNewValue(newValue);

		}
	}

	/**
	 * Create the GUI and show it. For thread safety, this method should be invoked from the event dispatch thread.
	 */

	private static java.util.List<Limited> createAndShowGUI(
			java.util.List<Limited> parameters) {
		// Create and set up the window.
		// Add contents to the window.
		JComponent panel1;
		JComponent panel2;
		JPanel panelShowValidity = new JPanel();
		JTextField fieldShowValidity = new JTextField("Displays whether the  values are valid");
		panelShowValidity.add(fieldShowValidity);
		ParametersPanelBuilderTest parametersPanelTest = new ParametersPanelBuilderTest();
		ParametersPanelTestListener parametersPanelTestListener = parametersPanelTest.new ParametersPanelTestListener(
				fieldShowValidity);
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.insets = new Insets(0, 0, 0, 10);
		ParametersPanelBuilder builder1 = new ParametersPanelBuilder();
		builder1.setLimiteds(parameters);
		builder1.setSpacer(":");
		builder1.setTitle("Fluorescence Scan Parameters");
		builder1.setPropertyChangeListener(parametersPanelTestListener);
		builder1.configure();
		panel1 = builder1;
		ParametersPanelBuilder builder2 = new ParametersPanelBuilder();
		builder2.setLimiteds(parameters);
		builder2.setSpacer(null);
		builder2.setTitle(null);
		builder2.setPropertyChangeListener(null);
		builder2.setEditableFieldBorder(null);
		builder2.setNoneditableFieldBorder(null);
		builder2.setHorzSpace(0);
		builder2.configure();
		panel2 = builder2;
		ParametersPanelBuilder parametersPanel = new ParametersPanelBuilder( parameters, 9, ":",
				"GridBagLayout", BorderFactory.createBevelBorder(BevelBorder.LOWERED), null, null, 2, 50, 5, 20, 10,
				null, null);
		JDialog dialog = new JDialog((Frame) SwingUtilities.getAncestorOfClass(Frame.class, null), true);
		JPanel mainPanel = new JPanel();
		mainPanel.setBorder(BorderFactory.createTitledBorder("Test of ParameterPanelBuilder"));
		mainPanel.setLayout(new GridLayout(0, 1));
		mainPanel.add(panel1);
		mainPanel.add(panel2);
		mainPanel.add(parametersPanel);
		mainPanel.add(panelShowValidity);
		dialog.add(mainPanel);
		dialog.pack();
		fieldShowValidity.setMaximumSize(panelShowValidity.getSize());
		dialog.pack();
		dialog.setResizable(true);
		dialog.setContentPane(mainPanel);
		dialog.setVisible(true);
		return parametersPanel.getParameters(); // returns the value from panel4
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// Schedule a job for the event dispatch thread:
		// creating and showing this application's GUI.
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				// Turn off metal's use of bold fonts
				UIManager.put("swing.boldMetal", Boolean.FALSE);
				ArrayList<Limited> parameters = new ArrayList<Limited>();
				parameters.add(new Limited(1, 0., 0., 100., "%.3f", "Start", "Start of scan", "Angstroms", 'S',
						Component.LEFT_ALIGNMENT, true));
				parameters.add(new Limited(2, 0., 0., 100., "%.3f", "End", "End of scan", "Angstroms", 'E',
						Component.CENTER_ALIGNMENT, true));
				parameters.add(null); // space
				parameters.add(new Limited(3, 0., 0., 100., "%.3f", "End", "End of scan", "Angstroms", 'A',
						Component.RIGHT_ALIGNMENT, true));
				parameters.add(new Limited(4, 0., 0., 100., "%.3f", "End", "End of scan", "Angstroms", 'B',
						Component.TOP_ALIGNMENT, true));
				parameters.add(new Limited(5, 0., 0., 100., "*", "End4", "End of scan", "Angstroms", 'C',
						Component.BOTTOM_ALIGNMENT, true));
				parameters.add(new Limited(6, 0., 0., 100., "", "End", "End of scan", "Angstroms", 'D',
						Component.LEFT_ALIGNMENT, true));
				parameters.add(null); // space
				parameters.add(new Limited(7, 0., 0., 100., null, "End", "End of scan", "Angstroms", 'F',
						SwingConstants.CENTER, true));
				parameters.add(new Limited(8, 0, 0, 100, null, "Number", "Number of scans", "Counts", 'N',
						SwingConstants.CENTER, true));
				parameters.add(new Limited(9, 0, 0, 100, "%x", "Hex", "Hex of scans", "Counts", 'H',
						SwingConstants.CENTER, true));
				parameters.add(new Limited(10, "s", "", "",null , "String", "name of scans", "", 'H',
						SwingConstants.CENTER, true)); //does not handle strings yet
				createAndShowGUI(createAndShowGUI(parameters));
				System.exit(0);
			}
		});
	}

}
