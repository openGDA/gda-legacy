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

import gda.configuration.properties.LocalProperties;
import gda.gui.oemove.DOFModeDisplay;
import gda.observable.IObserver;
import gda.observable.ObservableComponent;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.border.TitledBorder;

/**
 * 
 */
public class DOFMode extends JComboBox implements ActionListener, DOFModeDisplay {
	/**
	 * Move relative to a position
	 */
	public static final int RELATIVE = 0;

	/**
	 * Move continuously
	 */
	public static final int CONTINUOUS = 1;

	/**
	 * Move to an absolute position
	 */
	public static final int ABSOLUTE = 2;

	/**
	 * Set the position
	 */
	public static final int SET = 3;

	/**
	 * Set the home position
	 */
	public static final int HOME_SET = 4;

	/**
	 * move to the home position
	 */
	public static final int HOME = 5;

	private String[] propertyNames = { "gda.gui.oemove.relative", "gda.gui.oemove.continuous",
			"gda.gui.oemove.absolute", "gda.gui.oemove.set", "gda.gui.oemove.homeSet", "gda.gui.oemove.home" };

	// private int mode;
	private boolean doNothing = false;

	private ObservableComponent observableComponent = new ObservableComponent();

	/**
	 * 
	 */
	public DOFMode() {
		addActionListener(this);
		// default mode is ABSOLUTE unless the property is explicitly set to false;
		// addItem("To");
		if (LocalProperties.check(propertyNames[ABSOLUTE], true)
				&& LocalProperties.check(propertyNames[RELATIVE], true)) {
			if (LocalProperties.get("gda.gui.oemove.defaultDofMode", "absolute").compareToIgnoreCase("relative") == 0) {
				addItem("By");
				addItem("To");
			} else {
				addItem("To");
				addItem("By");
			}
		} else {
			if (LocalProperties.check(propertyNames[ABSOLUTE], true))
				addItem("To");
			if (LocalProperties.check(propertyNames[RELATIVE], true))
				addItem("By");
		}

		if (LocalProperties.check(propertyNames[CONTINUOUS], true))
			addItem("Continuous");
		if (LocalProperties.check(propertyNames[SET], true))
			addItem("Set");
		if (LocalProperties.check(propertyNames[HOME_SET], true))
			addItem("Home Set");
		if (LocalProperties.check(propertyNames[HOME], true))
			addItem("Home");

		setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Mode", TitledBorder.CENTER,
				TitledBorder.TOP, null, Color.black));
	}

	@Override
	public void actionPerformed(ActionEvent ae) {
		if (!doNothing) {
			int mode;
			JComboBox combo = (JComboBox) ae.getSource();
			String selection = (String) combo.getSelectedItem();
			if (selection.equals("To"))
				mode = ABSOLUTE;
			else if (selection.equals("By"))
				mode = RELATIVE;
			else if (selection.equals("Set"))
				mode = SET;
			else if (selection.equals("Home Set"))
				mode = HOME_SET;
			else if (selection.equals("Home"))
				mode = HOME;
			else
				mode = CONTINUOUS;

			observableComponent.notifyIObservers(this, mode);
		}
	}

	@Override
	public void addIObserver(IObserver anIObserver) {
		observableComponent.addIObserver(anIObserver);
	}

	@Override
	public void deleteIObserver(IObserver anIObserver) {
		observableComponent.deleteIObserver(anIObserver);
	}

	@Override
	public void deleteIObservers() {
		observableComponent.deleteIObservers();
	}

	@Override
	public int getMode() {
		int mode;
		String selection = (String) getSelectedItem();
		if (selection.equals("To"))
			mode = ABSOLUTE;
		else if (selection.equals("By"))
			mode = RELATIVE;
		else if (selection.equals("Set"))
			mode = SET;
		else if (selection.equals("Home Set"))
			mode = HOME_SET;
		else if (selection.equals("Home"))
			mode = HOME;
		else
			mode = CONTINUOUS;
		return mode;
	}

	@Override
	public void setMode(int mode) {
		switch (mode) {
		case ABSOLUTE:
			setSelectedItem("To");
			break;
		case RELATIVE:
			setSelectedItem("By");
			break;
		case SET:
			setSelectedItem("Set");
			break;
		case CONTINUOUS:
			setSelectedItem("Continuous");
			break;
		case HOME_SET:
			setSelectedItem("Home Set");
			break;
		case HOME:
			setSelectedItem("Home");
			break;
		}
	}

	@Override
	public void setModeNames(ArrayList<String> modeList) {
		if (!modeList.isEmpty()) {
			doNothing = true;
			removeAllItems();
			for (String item : modeList)
				addItem(item);
			doNothing = false;
		}
	}

	@Override
	public String toString() {
		return "" + getSelectedIndex();
	}
}
