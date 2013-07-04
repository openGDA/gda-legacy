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

import java.awt.Color;

import javax.swing.JProgressBar;
import javax.swing.SwingConstants;

/**
 * Extends JProgress bar to provide some special features for using in counting scans and points.
 */

public class SpecialProgressBar extends JProgressBar {
	private int current;

	private int numberOfPoints;

	private String beingCounted = null;

	/**
	 * Constructor
	 */
	public SpecialProgressBar() {
		setOrientation(SwingConstants.HORIZONTAL);
		setMinimum(0);
		setMaximum(100);
		setBorderPainted(true);
		setStringPainted(true);
		setBackground(Color.LIGHT_GRAY);
		setForeground(new Color(90, 200, 90));
		setString("Idle");
	}

	/**
	 * 
	 */
	public void clear() {
		setString("Idle");
		setValue(0);
	}

	/**
	 * Initializes.
	 * 
	 * @param numberOfPoints
	 *            the total number of points expected - if less than 0 indeterminate mode is set
	 */
	public void init(int numberOfPoints) {
		this.numberOfPoints = numberOfPoints;

		if (numberOfPoints < 0) {
			setIndeterminate(true);
			setString("Cannot tell");
			current = 0;
		} else {
			setIndeterminate(false);
			setMaximum(numberOfPoints);
			setString(null);
			current = 0;
			setValue(current);
		}
	}

	/**
	 * Display string to indicate that counting is over.
	 */
	public void setDone() {
		setValue(getMaximum());
		setString("Done");
	}

	/**
	 * Add one more count
	 */
	public void increment() {
		current++;
		setValue(current);
		if (isIndeterminate())
			setString("Doing " + beingCounted + " " + current);
		else
			setString("Doing " + beingCounted + " " + current + " of " + numberOfPoints);
	}

	/**
	 * Set the name of the thing being counted, used in JProgressBar string.
	 * 
	 * @param beingCounted
	 *            the name of the thing being counted, e.g. "scan" or "point"
	 */
	public void setBeingCounted(String beingCounted) {
		this.beingCounted = beingCounted;
	}
}
