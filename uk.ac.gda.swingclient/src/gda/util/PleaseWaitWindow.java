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

package gda.util;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

/**
 * PleaseWaitWindow Class
 */
public class PleaseWaitWindow extends JFrame {
	private JLabel jl;

	private Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

	/**
	 * @param label
	 */
	public PleaseWaitWindow(String label) {
		JPanel jp = new JPanel(new BorderLayout());
		jl = new JLabel(label);
		JProgressBar jpb = new JProgressBar();
		jp.add(jl, BorderLayout.NORTH);
		jpb.setIndeterminate(true);
		jp.add(jpb, BorderLayout.SOUTH);
		getContentPane().add(jp);
		packAndPosition();
	}

	/**
	 * @param newLabel
	 */
	public void setLabel(String newLabel) {
		jl.setText(newLabel);
		packAndPosition();
	}

	private void packAndPosition() {
		pack();
		setLocation((screenSize.width - getWidth()) / 2, (screenSize.height - getHeight()) / 2);
		setAlwaysOnTop(true);
	}
}
