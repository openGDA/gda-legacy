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

package gda.gui.oemove;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A class to instantiate Representations defined in the XML, using Castor. Representations are stored in a
 * LinkedHashMap, with the OE name as the key, to preserve the order of creation as defined by the XML.
 */
public class RepresentationFactory implements PropertyChangeListener {
	private Map<String, Representation> representationList = new LinkedHashMap<String, Representation>();

	private String name;

	/**
	 * Null argument constructor required by Castor in the instantation phase.
	 */
	public RepresentationFactory() {
	}

	/**
	 * Add a Representation to the list of stored Representations using the name as an index into the store.
	 * 
	 * @param representation
	 *            the representation.
	 */
	public void addRepresentation(Representation representation) {
		representationList.put(representation.getName(), representation);
	}

	/**
	 * Return the list of stored representations.
	 * 
	 * @return the complete representation list.
	 */
	public ArrayList<Representation> getRepresentationList() {
		return new ArrayList<Representation>(representationList.values());
	}

	/**
	 * Get the specified representation from the stored representations.
	 * 
	 * @param name
	 *            the name of the required representation.
	 * @return the requested representation
	 */
	public Representation getRepresentation(String name) {
		return representationList.get(name);
	}

	/**
	 * Returns the list of representation names stored in this factory.
	 * 
	 * @return the list of stored representation names.
	 */
	public ArrayList<String> getRepresentationNames() {
		return new ArrayList<String>(representationList.keySet());
	}

	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            The name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public void propertyChange(PropertyChangeEvent e) {
		representationList.remove(e.getNewValue());
	}
}
