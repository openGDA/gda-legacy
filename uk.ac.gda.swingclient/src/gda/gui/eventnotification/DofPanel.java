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

package gda.gui.eventnotification;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import gda.device.Motor;
import gda.factory.FactoryException;
import gda.factory.Finder;

import javax.swing.JLabel;

/**
 * A notification panel for monitoring the value of a DOF
 * 
 */
public class DofPanel extends NotificationPanel {

	private String description;
	private JLabel label;

	/**
	 * @return description of the dofPanel
	 */
	public String getDescription() {
		return description;
	}
	/**
	 * @param description description of the dofPanel
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public void configure() throws FactoryException {

		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.weightx = 0.1;
		c.weighty = 0.1;
		c.anchor = GridBagConstraints.CENTER;
		
		try {
			Motor motor = (Motor)Finder.getInstance().find(this.getName());
			this.label = new JLabel(this.getDescription() + ": " + motor.getPosition());
		} catch (Exception e) {
			this.label = new JLabel("PROBLEM FINDING motor: " + e.toString());
		}
		this.label.setAlignmentX(Component.CENTER_ALIGNMENT);
		add(this.label, c);
		
		super.configure();
	}
	
	@Override
	public void update(Object theObserved, Object changeCode) {
		try{
			Motor motor = (Motor)theObserved;
			this.label.setText(this.getDescription() + ": " + motor.getPosition());
		} catch (Exception e) {
			this.label.setText("PROBLEM SETTING motor: " + e.toString()); 
		}
		super.update(theObserved, changeCode);
	}
	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String text = "\npanel: " + getName() + "(" + isPlacePanelOnNewLine() + ", " + getTextPositioning();
		text += ", " + this.description + ", " + this.label + ")"; 
		return text;
	}
}
