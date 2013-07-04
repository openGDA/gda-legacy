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

import gda.factory.FactoryException;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;

import javax.swing.JLabel;

/**
 * A notification panel for monitoring the value of a DOF
 * 
 */
public class MetadataPanel extends NotificationPanel {

	private ArrayList<MetadataRow> metadataRows;
	
	@Override
	public void configure() throws FactoryException {
		
		setLayout(new GridBagLayout());
		try{
			for (MetadataRow row: this.metadataRows) {
				add(new JLabel(row.getGuiLabel() + ": "), getFirstColumnContraints());
				add(new JLabel("value"), getSecondColumnContraints());
			}
		} catch (Exception e) {
			add(new JLabel("PROBLEM FINDING METADATA: " + e.toString())); 
		}
		super.configure();
	}
	/**
	 * Get gridbag constraints for first column containing metadata labels 
	 */
	private GridBagConstraints getFirstColumnContraints() {
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.anchor = GridBagConstraints.EAST;
		constraints.gridwidth = GridBagConstraints.RELATIVE;
		constraints.fill = GridBagConstraints.NONE;
		constraints.weightx = 0.0;
		return constraints;
	}
	/**
	 * Get gridbag constraints for second column containing metadata values 
	 */
	private GridBagConstraints getSecondColumnContraints() {
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.weightx = 1.0;
		return constraints;
	}
	
	/**
	 * @return the rows of metadata
	 */
	public ArrayList<MetadataRow> getMetadataRows() {
		return metadataRows;
	}

	/**
	 * @param metadataRows the rows of metadata
	 */
	public void setMetadataRows(ArrayList<MetadataRow> metadataRows) {
		this.metadataRows = metadataRows;
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String text = "\npanel: " + getName() + "(" + isPlacePanelOnNewLine() + ", " + getTextPositioning();
		for (MetadataRow row: this.metadataRows) {
			text += ", (" + row.getName() + ", " + row.getGuiLabel() + ")";
		}
		text += ") ";
		return text;
	}

}


