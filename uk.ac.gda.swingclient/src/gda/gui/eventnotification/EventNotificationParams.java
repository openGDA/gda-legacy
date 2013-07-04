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

import java.net.URL;
import java.util.ArrayList;

import uk.ac.gda.util.beans.xml.XMLHelpers;

/**
 * Class used for generating notification panels from XML 
 */
public class EventNotificationParams {

	static private URL mappingURL = EventNotificationParams.class.getResource("ENParameterMapping.xml");
	static private URL schemaUrl = EventNotificationParams.class.getResource("ENParameterMapping.xsd");

	private String name;
	private ArrayList<NotificationPanel> panels = new ArrayList<NotificationPanel>();

	/**
	 * @param filename
	 * @return EventNotificationParams
	 * @throws Exception
	 */
	public static EventNotificationParams createFromXML(String filename) throws Exception {
		return (EventNotificationParams) XMLHelpers.createFromXML(mappingURL, EventNotificationParams.class, schemaUrl, filename);
	}
	/**
	 * @return the name 
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name name of parameters
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the notification panels
	 */
	public ArrayList<NotificationPanel> getPanels() {
		return panels;
	}
	/**
	 * @param panels the notification panels
	 */
	public void setPanels(ArrayList<NotificationPanel> panels) {
		this.panels = panels;
	}
	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String text = "EventNotification parameters: ";
		text += "\nname: " + name;
		for (NotificationPanel panel: this.panels) {
			text += "\n" + panel.toString();
		}
		return text;
	}
	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (!(o instanceof EventNotificationParams)) {
			return false;
		}

		EventNotificationParams other = (EventNotificationParams) o;
		if (this.toString().equals(other.toString())) {
			return true;
		}

		return false;
	}
	@Override
	public int hashCode() {
		return super.hashCode();
	}
}

	