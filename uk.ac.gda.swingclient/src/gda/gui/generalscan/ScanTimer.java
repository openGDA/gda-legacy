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

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

/**
 * Displays progress through a scan
 */

public class ScanTimer extends JPanel {
	private JProgressBar jProgressBar;

	private int current;

	/**
	 * Constructor
	 */
	public ScanTimer() {
		jProgressBar = new JProgressBar();
		jProgressBar.setOrientation(SwingConstants.HORIZONTAL);
		jProgressBar.setMinimum(0);
		jProgressBar.setMaximum(100);
		jProgressBar.setBorderPainted(true);
		jProgressBar.setStringPainted(true);
		add(jProgressBar);

		Border b = BorderFactory.createEtchedBorder();
		setBorder(BorderFactory.createTitledBorder(b, "Points Done", TitledBorder.TOP, TitledBorder.CENTER));

	}

	/**
	 * @param numberOfPoints
	 */
	public void init(int numberOfPoints) {
		if (numberOfPoints < 0) {
			jProgressBar.setIndeterminate(true);
			jProgressBar.setString("Cannot tell");
			current = 0;
		} else {
			jProgressBar.setIndeterminate(false);
			jProgressBar.setMaximum(numberOfPoints);
			jProgressBar.setString(null);
			current = 0;
			jProgressBar.setValue(current);
		}
	}

	/**
	 * 
	 */
	public void increment() {
		current++;
		jProgressBar.setValue(current);
		if (jProgressBar.isIndeterminate())
			jProgressBar.setString("" + current);
	}
}
