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

import gda.device.Monitor;
import gda.factory.FactoryException;
import gda.factory.Finder;

import javax.swing.JCheckBox;
import javax.swing.JLabel;

/**
 * A notification panel for monitoring a PV value via an EpicsMonitor.
 * 
 */
public class PvPanel extends NotificationPanel {

	private String description;
	private JLabel label;
	
	/**
	 * @return description of the pvPanel
	 */
	public String getDescription() {
		return description;
	}
	/**
	 * @param description description of the pvPanel
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
		c.anchor = GridBagConstraints.LINE_START;
		
		try{
			Monitor epicsMonitor = (Monitor)Finder.getInstance().find(this.getName());
			this.label = new JLabel(this.getDescription() + ": " + epicsMonitor.getPosition());
		} catch (Exception e) {
			this.label = new JLabel("PROBLEM FINDING EpicsMonitor: " + e.toString()); 
		}
		this.label.setAlignmentX(Component.CENTER_ALIGNMENT);
		add(this.label, c);

		JCheckBox ignoreCheckBox = new JCheckBox("Ignore future updates");
		ignoreCheckBox.setVisible(true);
		c.gridx ++;
		c.anchor = GridBagConstraints.CENTER;
		add(ignoreCheckBox, c);
		this.setIgnoreCheckBox(ignoreCheckBox);
		
		super.configure();
	}

	@Override
	public void update(Object theObserved, Object changeCode) {
		try{
			Monitor epicsMonitor = (Monitor)theObserved;
			this.label.setText(this.getDescription() + ": " + epicsMonitor.getPosition().toString());
		} catch (Exception e) {
			this.label.setText("PROBLEM SETTING EpicsMonitor: " + e.toString()); 
		}
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
