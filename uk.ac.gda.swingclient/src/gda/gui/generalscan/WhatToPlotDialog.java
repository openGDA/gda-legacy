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
 * WhatToPlotDialog Class
 */
public class WhatToPlotDialog extends JDialog implements CheckBoxPanelObserver {
	private WhatToPlotDialogUser gdh;

	private CheckBoxPanel checkBoxPanel;

	/**
	 * @param gdh
	 * @param names
	 */
	public WhatToPlotDialog(WhatToPlotDialogUser gdh, ArrayList<String> names) {

		super((Frame) null, "", false);

		this.gdh = gdh;
		JPanel overallPanel = new JPanel(new BorderLayout());
		checkBoxPanel = new CheckBoxPanel(names);
		checkBoxPanel.addObserver(this);
		overallPanel.add(checkBoxPanel, BorderLayout.CENTER);

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

		getContentPane().add(overallPanel);
		pack();
	}

	private void close() {
		setVisible(false);
	}

	@Override
	public void checkBoxPanelChanged(CheckBoxPanel changed, boolean[] isSelectedArray) {
		gdh.setWhatToPlot(isSelectedArray);
	}

	/**
	 * @param i
	 * @param b
	 */
	public void setSelected(int i, boolean b) {
		checkBoxPanel.setSelected(i, b);
	}

	/**
	 * @return boolean[]
	 */
	public boolean[] getSelected() {
		return checkBoxPanel.getSelected();
	}

}
