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

import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;

import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.mapping.MappingException;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;

import uk.ac.gda.util.beans.xml.XMLObjectConfigFileValidator;

/**
 * A class to load, validate and create java objects from their xml representation.
 */
public class Loader {
	private static final Logger logger = LoggerFactory.getLogger(Loader.class);

	private String mappingFile;

	private RepresentationFactory representationFactory = null;

	private String xmlFile;

	/**
	 * Load the configuration XML file, validate it against the schema and unmarshall the contents into java objects
	 * using Castor.
	 * 
	 * @param xmlFile
	 */
	public Loader(String xmlFile) {
		this.xmlFile = xmlFile;
		URL url = getClass().getResource("mapping.xml");
		if (url != null)
			this.mappingFile = url.toString();

		if (xmlFile != null && mappingFile != null) {
			try {
				Mapping mapping = new Mapping(getClass().getClassLoader());
				mapping.loadMapping(mappingFile);
				
				Unmarshaller unmarshaller = new Unmarshaller(mapping);
				unmarshaller.setReuseObjects(true);

				// Create the local objects.
				InputSource source;
				URI uri = new URI(xmlFile);
				if (uri.getScheme() != null && (uri.getScheme().equals("http") || uri.getScheme().equals("file")))
					source = new InputSource(uri.toURL().openStream());
				else
					source = new InputSource(new FileReader(xmlFile));

				// check whether XML instance file is valid
				source = doSchemaValidation(source);

				representationFactory = (RepresentationFactory) unmarshaller.unmarshal(source);

			} catch (URISyntaxException e) {
				logger.error("URL error: " + e.getMessage(), e);
			} catch (IOException e) {
				logger.error("IO error: " + e.getMessage(), e);
			} catch (MappingException e) {
				logger.error("Mapping error: " + e.getMessage(), e);
			} catch (MarshalException e) {
				logger.error("Unmarshalling error: " + e.getMessage(), e);
			} catch (ValidationException e) {
				logger.error("Validation error: " + e.getMessage(), e);
			}
		} else {
			if (xmlFile == null)
				logger.error("XML configuration file not found");
			if (mappingFile == null)
				logger.error("XML Mapping file not found");
		}
	}

	private InputSource doSchemaValidation(InputSource source) throws ValidationException {
		String gdaSchemaPathName = null;

		URL url = ClassLoader.getSystemResource("gda/gui/oemove/schema.xsd");
		if (url != null) {
			gdaSchemaPathName = url.toString();
		}

		if (gdaSchemaPathName != null) {
			XMLObjectConfigFileValidator validator = new XMLObjectConfigFileValidator();

			boolean useXercesValidation = true;
			try {
				source = validator.validateSource(gdaSchemaPathName, source, useXercesValidation);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				throw new ValidationException("Validation Failed: " + xmlFile, e);
			}

			// if source came back null, then validation occurred, but it failed.
			if (source == null) {
				logger.error("Loader.doSchemaValidation - XML instance file invalid: " + xmlFile);

				throw new ValidationException("Loader.doSchemaValidation - XML instance file invalid: " + xmlFile);
			}
			logger.debug("Loader.doSchemaValidation - XML instance file is valid: " + xmlFile);
		}
		return source;
	}

	/**
	 * @return a list of the object names in the factory
	 */
	public ArrayList<String> getRepresentationNames() {
		return representationFactory.getRepresentationNames();
	}

	/**
	 * @return the factory name into which the objects have been instantiated.
	 */
	public RepresentationFactory getRepresentationFactory() {
		return representationFactory;
	}
}
