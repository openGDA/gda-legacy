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

import gda.device.Motor;
import gda.gui.oemove.DOFSpeedLevel;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.border.TitledBorder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The default JComboBox implementation of DOFSpeedLevel - allows speeds for DOFs to be set to one of three values
 * Motor.SLOW, Motor.MEDIUM and Motor.FAST. The interface-implementations-factory business is a bit over the top for the
 * simple speedLevel case but this was used as a test bed for the more complicated position and input cases.
 * 
 * @see DOFSpeedLevel
 */
public class DefaultSpeedLevel extends JComboBox implements DOFSpeedLevel {
	private static final Logger logger = LoggerFactory.getLogger(DefaultSpeedLevel.class);

	private int speedLevel;

	private boolean doNothing = false;

	/**
	 * 
	 */
	public DefaultSpeedLevel() {
		// This ActionListens to itself
		addActionListener(this);

		speedLevel = Motor.SLOW;
		addItem("Slow");
		addItem("Medium");
		addItem("Fast");
		setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Speed", TitledBorder.CENTER,
				TitledBorder.TOP, null, Color.black));
	}

	/**
	 * Implements the ActionListener interface - this ActionListens to itself. When the selected item is changed the
	 * value of speedLevel is changed to match.
	 * 
	 * @param ae
	 *            the
	 * @see ActionEvent
	 */
	@Override
	public void actionPerformed(ActionEvent ae) {
		if (!doNothing) {
			String selection = (String) getSelectedItem();

			if (selection == "Slow") {
				speedLevel = Motor.SLOW;
				logger.debug("speedLevel now SLOW");
			} else if (selection == "Medium") {
				speedLevel = Motor.MEDIUM;
				logger.debug("speedLevel now TWO");
			} else if (selection == "Fast") {
				speedLevel = Motor.FAST;
				logger.debug("speedLevel now FAST");
			}
		}
	}

	/**
	 * Returns the current speed level
	 * 
	 * @return speedLevel
	 */
	@Override
	public int getSpeedLevel() {
		return speedLevel;
	}

	/**
	 * Sets the speed level (indirectly by setting the selected item in the JComboBox)
	 * 
	 * @param speedLevel
	 *            the new speedLevel
	 */
	@Override
	public void setSpeedLevel(int speedLevel) {
		switch (speedLevel) {
		case Motor.SLOW:
			setSelectedItem("Slow");
			break;
		case Motor.MEDIUM:
			setSelectedItem("Medium");
			break;
		case Motor.FAST:
			setSelectedItem("Fast");
			break;
		default:
			logger.error("Unexpected value given to DefaultSpeedLevel.setSpeedLevel: " + speedLevel);
			break;
		}
	}

	@Override
	public void setSpeedNames(ArrayList<String> speedNames) {
		if (!speedNames.isEmpty()) {
			doNothing = true;
			removeAllItems();
			for (String item : speedNames)
				addItem(item);
			doNothing = false;
		}
	}
}
