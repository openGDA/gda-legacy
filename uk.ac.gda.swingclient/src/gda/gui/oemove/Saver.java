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

import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;

import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.mapping.MappingException;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Saver Class
 */
public class Saver {
	private static final Logger logger = LoggerFactory.getLogger(Saver.class);

	private String mappingFile;

	/**
	 * @param xmlFile
	 * @param factory
	 */
	public Saver(String xmlFile, RepresentationFactory factory) {
		URL url = getClass().getResource("mapping.xml");
		if (url != null)
			this.mappingFile = url.toString();

		if (xmlFile != null && mappingFile != null) {
			try {
				Mapping mapping = new Mapping();
				mapping.loadMapping(mappingFile);

				Marshaller marshaller = new Marshaller(new FileWriter(xmlFile));
				marshaller.setMapping(mapping);
				marshaller.setSuppressXSIType(true);
				marshaller.marshal(factory);
			} catch (IOException e) {
				logger.error("IO error: " + e.getMessage());
			} catch (MappingException e) {
				logger.error("Mapping error: " + e.getMessage());
			} catch (MarshalException e) {
				logger.error("Unmarshalling error: " + e.getMessage());
			} catch (ValidationException e) {
				logger.error("Validation error: " + e.getMessage());
			}
		}
	}
}
