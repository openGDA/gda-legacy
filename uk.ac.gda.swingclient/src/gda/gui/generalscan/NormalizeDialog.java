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
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

/**
 * NormalizeDialog Class
 */
public class NormalizeDialog extends JDialog {

	private NormalizeDialogUser gdh;

	private ArrayList<String> lineNames;

	private RadioButtonPanel radioButtonPanel;

	/**
	 * @param gdh
	 * @param lineNames
	 */
	public NormalizeDialog(NormalizeDialogUser gdh, ArrayList<String> lineNames) {

		// NB if you use null you must cast because there are
		// two JDialog constructors which would match
		super((Frame) null, "Normalize to", false);

		this.gdh = gdh;
		JPanel overallPanel = new JPanel(new BorderLayout());
		this.lineNames = new ArrayList<String>(lineNames);
		this.lineNames.add(0, "None");
		radioButtonPanel = new RadioButtonPanel(this.lineNames);
		radioButtonPanel.addObserver(new RadioButtonPanelObserver() {
			@Override
			public void radioButtonPanelChanged(RadioButtonPanel nd, int selectedButton) {
				setWhichToNormalize(selectedButton - 1);
			}
		});

		overallPanel.add(radioButtonPanel, BorderLayout.CENTER);
		JPanel buttonPanel = new JPanel();
		JButton closeButton = new JButton("Close");
		closeButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				close();

			}
		});

		buttonPanel.add(closeButton);

		overallPanel.add(buttonPanel, BorderLayout.SOUTH);

		add(overallPanel);
		pack();
	}

	private void close() {
		setVisible(false);
	}

	private void setWhichToNormalize(int which) {
		gdh.setWhichToNormalizeTo(which);
	}

}
