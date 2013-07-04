/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council
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

import gda.device.ControlPoint;
import gda.device.DeviceException;
import gda.factory.FactoryException;
import gda.factory.Findable;
import gda.factory.Finder;
import gda.gui.oemove.Pluggable;
import gda.observable.IObserver;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An oemove plugin for a ControlPoint
 */
public class ControlPointPlugin implements Findable, Pluggable, IObserver {
	private static final Logger logger = LoggerFactory.getLogger(ControlPointPlugin.class);
	String name= "";
	String controlPointName="";
	private ControlPoint controlPoint;
	private JLabel controlLabel;
	private JTextField controlSetText;
	private JButton setButton;
	/**
	 * @return control point name
	 */
	public String getControlPointName() {
		return controlPointName;
	}

	/**
	 * @param controlPointName
	 */
	public void setControlPointName(String controlPointName) {
		this.controlPointName = controlPointName;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public JComponent getControlComponent() {
		JPanel p = new JPanel();
		controlSetText = new JTextField(5);
		p.add(controlSetText);
		setButton = new JButton("Set");
		setButton.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e) {
				
				try {
					double valueToSet = Double.parseDouble(controlSetText.getText());
					controlPoint.setValue(valueToSet);
				} catch (DeviceException e1) {
					logger.error("Error setting the value for " + controlPoint.getName(), e1);
				}
				
			}
			
		});
		p.add(setButton);
		return p;
			}

	@Override
	public JComponent getDisplayComponent() {
		JPanel q = new JPanel();
		controlLabel = new JLabel();
		try {
			controlLabel.setText((String.valueOf(controlPoint.getValue())));
		} catch (DeviceException e) {
			controlLabel.setText("unknown");
			e.printStackTrace();
		}
		q.add(controlLabel);
		return q;
	}

	@Override
	public void configure() throws FactoryException {
		controlPoint = (ControlPoint) Finder.getInstance().find(controlPointName);
		controlPoint.addIObserver(this);

	}

	@Override
	public void update(Object theObserved, Object changeCode) {
		logger.info("controlpoint update");
		if (controlLabel != null) {
			String previousPosition = controlLabel.getText();
			try {
				controlLabel.setText((String) controlPoint.getPosition());
			} catch (DeviceException e) {
				controlLabel.setText(previousPosition);
				logger.error("Error getting position for " + controlPointName + " in  EpicsControlPoint", e);
			}
		}


	}

}
