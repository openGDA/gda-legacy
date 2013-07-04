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

import gda.gui.generalscan.ScanCommandEditor;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

/**
 * An implementor of ScanCommandEditor used by PolarimeterScanPanel5U to add flux monitoring to scans if required.
 */
public class PolarimeterScanCommandEditor implements ActionListener, ScanCommandEditor {

	private boolean requiredMonitoring = false;
	private boolean enable = false;
	private JPanel jPanel;
	private JRadioButton[] fluxMonitorButtons;
	private JRadioButton[] pinholeButtons;
	private JPanel fluxMonitorPanel = null;
	private JPanel pinholePanel = null;

	private JLabel pinholeLabel;

	/**
	 * Creates a Polarimeter scan command editor.
	 */
	public PolarimeterScanCommandEditor() {

		// Create Flux monitoring panel
		int numFluxMonitorButtons = 2;
		fluxMonitorPanel = new JPanel(new GridLayout(0, 1));
		fluxMonitorPanel.setBorder(BorderFactory.createEtchedBorder());
		fluxMonitorPanel.add(new JLabel("Flux Monitoring"));
		ButtonGroup fluxMonitorButtonsGroup = new ButtonGroup();
		fluxMonitorButtons = new JRadioButton[numFluxMonitorButtons];
		fluxMonitorButtons[0] = new JRadioButton("On");
		fluxMonitorButtons[1] = new JRadioButton("Off");
		for (int i = 0; i < numFluxMonitorButtons; i++) {
			fluxMonitorButtons[i].setActionCommand("MONITORING");
			fluxMonitorButtons[i].addActionListener(this);
			fluxMonitorButtonsGroup.add(fluxMonitorButtons[i]);
			fluxMonitorPanel.add(fluxMonitorButtons[i]);
		}
		fluxMonitorButtons[1].setSelected(true);

		// Create pinhole panel
		// TODO need to get number of pinholes and labels from frontPinhole Enum but needs corbarising(?)
		// Hard code for now
		/*
		 * if ((frontPinhole = (PolarimeterPinholeEnumPositioner) finder.find("frontPinholeEnum")) == null) {
		 * Message.alarm("PolarimeterScanCommandEditor: Front pinhole enumerator not found"); }
		 */
		int numPinholeButtons = 5;// frontPinhole.getNumPinholes();
		ArrayList<String> labels = new ArrayList<String>();
		labels.add("0.05mm");
		labels.add("0.1mm");
		labels.add("0.2mm");
		labels.add("0.5mm");
		labels.add("3mm");

		pinholePanel = new JPanel(new GridLayout(0, 1));
		pinholePanel.setBorder(BorderFactory.createEtchedBorder());
		pinholeLabel = new JLabel("Pinholes");
		pinholePanel.add(pinholeLabel);
		ButtonGroup pinholeButtonsGroup = new ButtonGroup();
		pinholeButtons = new JRadioButton[numPinholeButtons];
		for (int i = 0; i < numPinholeButtons; i++) {
			pinholeButtons[i] = new JRadioButton(labels.get(i));
			pinholeButtons[i].setActionCommand("PINHOLE");
			pinholeButtons[i].addActionListener(this);
			pinholeButtonsGroup.add(pinholeButtons[i]);
			pinholePanel.add(pinholeButtons[i]);
		}
		pinholeButtons[0].setSelected(true);

		// Create panel to return
		jPanel = new JPanel();
		jPanel.setLayout(new GridLayout(7, 1));
		Border b = BorderFactory.createEmptyBorder();
		jPanel.setBorder(BorderFactory.createTitledBorder(b, "", TitledBorder.TOP, TitledBorder.CENTER));
		jPanel.add(fluxMonitorPanel);
		jPanel.add(pinholePanel);
	}

	/**
	 * Returns the Combined Panel.
	 * 
	 * @return the jPanel
	 */
	@Override
	public JComponent getComponent() {
		return jPanel;
	}

	/**
	 * Inherited method not required here
	 * 
	 * @param dofName
	 *            the DOF name
	 * @return true if some editing will be done
	 */
	@Override
	public boolean enableDofName(String dofName) {
		return enable;
	}

	/**
	 * @return boolean - requiredMonitoring
	 */
	public boolean getRequiredMonitoPolarimeterGridAommandring()// , BorderLayout.NORTH);
	{
		return requiredMonitoring;
	}

	/**
	 * Edits the given scan command PolarimeterGridAommandto turn GridScan into a PolarimeterGridScan and appends need
	 * for flux monitoring or not
	 * 
	 * @param command
	 *            the original c // build the command line (adding calls to gda.scan.PolarimeterGridAommand
	 * @return the edited command
	 */
	@Override
	public String editCommand(String command) {
		String newCommand = command;

		newCommand = command.replaceAll("GridScan", "PolarimeterGridScan");

		// Add flux monitoring details if required
		if (fluxMonitorButtons[0].isSelected())
		// build the command line (adding calls to gda.scan.PolarimeterGridAisSelected())
		{
			// Append size of pinhole to string
			String s = null;
			for (int i = 0; i < this.pinholeButtons.length; i++) {
				if (this.pinholeButtons[i].isSelected()) {
					s = this.pinholeButtons[i].getText();
					s.trim();
					s = s.replace("mm", "");
					break;
				}
			}
			newCommand = newCommand.replace("));", "," + s + "));");
		} else // If no monitoring append a zero
		{
			newCommand = newCommand.replace("));", ",0));");
		}

		return newCommand;
	}

	/**
	 * Called when one of the tuningButtons is selected.
	 * 
	 * @param ae
	 *            the ActionEvent caused by the button selectiHARMONICon.
	 */
	@Override
	public void actionPerformed(ActionEvent ae) {
		// Handle flux monitoring radio buttons
		if (ae.getActionCommand().equals("MONITORING")) {
			if (this.fluxMonitorButtons[0].isSelected()) {
				enablePinholePanel(true);
			} else {
				enablePinholePanel(false);
			}
		}

	}

	/**
	 * Enables/disables PinholePanel
	 */
	private void enablePinholePanel(Boolean choice) {
		for (int i = 0; i < pinholeButtons.length; i++) {
			pinholeButtons[i].setEnabled(choice);
			pinholeButtons[i].setOpaque(choice);
		}
		pinholeLabel.setEnabled(choice);
	}
}
