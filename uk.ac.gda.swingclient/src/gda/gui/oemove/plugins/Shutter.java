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

import gda.factory.Configurable;
import gda.factory.FactoryException;
import gda.factory.Findable;
import gda.gui.oemove.Pluggable;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Shutter class
 */
public class Shutter implements Pluggable, Findable, Configurable {
	private String name;

	private JPanel controlComponent;

	private JPanel displayComponent;

	private boolean open = false;

	private JButton shutterButton = new JButton();

	private JLabel label = new JLabel();

	/**
	 * Constructor
	 */
	public Shutter() {
	}

	@Override
	public JComponent getDisplayComponent() {
		return displayComponent;
	}

	@Override
	public JComponent getControlComponent() {
		return controlComponent;
	}

	private void setLabelText() {
		String labelText = (open) ? "Shutter Open" : "Shutter Closed";
		label.setText(labelText);
	}

	private void setButtonText() {
		String text = (open) ? "Closed" : "Open";
		shutterButton.setText(text);
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	private void createDisplayComponent() {
		displayComponent = new JPanel();
		setLabelText();
		displayComponent.add(label);
	}

	private void createControlComponent() {
		controlComponent = new JPanel();
		controlComponent.setLayout(new FlowLayout());

		setButtonText();
		shutterButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				open = !open;
				setButtonText();
				setLabelText();
			}
		});
		controlComponent.add(shutterButton);
	}

	@Override
	public void configure() throws FactoryException {
		createDisplayComponent();
		createControlComponent();
	}
}
