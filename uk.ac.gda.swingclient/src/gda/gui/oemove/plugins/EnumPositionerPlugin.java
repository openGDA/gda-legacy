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

package gda.gui.oemove.plugins;

import gda.device.DeviceException;
import gda.device.EnumPositioner;
import gda.factory.FactoryException;
import gda.factory.Findable;
import gda.factory.Finder;
import gda.gui.oemove.Pluggable;
import gda.observable.IObserver;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A plugin for OEMove which observes an EnumPositioner device (normally an Epics pv) and displays/switches its state between two possible values.
 */
public class EnumPositionerPlugin implements Pluggable, Findable, IObserver {

	private String name;

	private static final Logger logger = LoggerFactory.getLogger(EnumPositionerPlugin.class);
	private JPanel controlComponent;
	private JPanel displayComponent;

	private EnumPositioner shutter;
	private String shutterName;
	private JComboBox stateSelect;

	private JPanel pnlControl;
	private JLabel shutterState;

	/**
	 * Constructor.
	 */
	public EnumPositionerPlugin() {
	}

	@Override
	public void configure() throws FactoryException {
		shutter = (EnumPositioner) Finder.getInstance().find(shutterName);
	}

	@Override
	public JComponent getControlComponent() {
		if (controlComponent == null) {
			createControlComponent();
		}
		return controlComponent;
	}

	@Override
	public JComponent getDisplayComponent() {
		if (displayComponent == null) {
			createDisplayComponent();
		}
		return displayComponent;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	private void createControlComponent() {

		controlComponent = new JPanel();
		controlComponent.add(getPnlControl());
	}

	private void createDisplayComponent() {

		displayComponent = new JPanel();
		displayComponent.add(getLblCurrentState());
	}

	private JLabel getLblCurrentState() {
		try {
			shutterState = new JLabel((String) shutter.getPosition());
			shutter.addIObserver(this);

		} catch (DeviceException e) {

		}
		return shutterState;
	}

	private JPanel getPnlControl() {
		if (pnlControl == null) {
			pnlControl = new JPanel();
			pnlControl.setBorder(BorderFactory.createTitledBorder("Change the state"));
			pnlControl.setPreferredSize(new java.awt.Dimension(152, 57));
			pnlControl.setMinimumSize(new java.awt.Dimension(152, 57));
			try {
				stateSelect = new JComboBox(shutter.getPositions());
			} catch (DeviceException e2) {
				stateSelect = new JComboBox();
			}
			stateSelect.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						shutter.moveTo(stateSelect.getSelectedItem());
					} catch (DeviceException e1) {
						logger.error("Error in EPicsShutterControl " + e1.getMessage());
					}

				}

			});

			pnlControl.add(stateSelect);
		}
		return pnlControl;
	}

	/**
	 * @return String
	 */
	public String getShutterName() {
		return shutterName;
	}

	/**
	 * @param shutterName
	 */
	public void setShutterName(String shutterName) {
		this.shutterName = shutterName;
	}

	/**
	 * @see gda.observable.IObserver#update(java.lang.Object, java.lang.Object)
	 */
	@Override
	public void update(Object theObserved, Object changeCode) {
		if (shutterState != null) {
			String previousPosition = shutterState.getText();
			try {
				shutterState.setText((String) shutter.getPosition());
			} catch (DeviceException e) {
				shutterState.setText(previousPosition);
				logger.error("Error getting position for " + shutterName + " in  EpicsShutterControl");
			}
		}

	}

}
