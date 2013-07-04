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


/**
 * Metatdata row for use in a notification panel
 */
public class MetadataRow {

	private String name;
	private String guiLabel;
	
	/**
	 * @return name of metadata row
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name name of metadata row
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return GUI label
	 */
	public String getGuiLabel() {
		return guiLabel;
	}
	/**
	 * @param guiLabel GUI label
	 */
	public void setGuiLabel(String guiLabel) {
		this.guiLabel = guiLabel;
	}
}
