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

package gda.gui;

import gda.factory.FactoryException;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class to integrate plugins
 */
public class Plugin extends AcquisitionPanel {

	private static final Logger logger = LoggerFactory.getLogger(Plugin.class);

	private String pluginName;

	/**
	 * Null constructor (is this necessary for Castor?)
	 */
	public Plugin() {
	}

	@Override
	public void configure() throws FactoryException {
		setLayout(new BorderLayout());
		try {
			JPanel jpanel;
			jpanel = (JPanel) Class.forName(pluginName).newInstance();
			add(jpanel, BorderLayout.CENTER);

			setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLoweredBevelBorder(), BorderFactory
					.createEmptyBorder(0, 0, 0, 0)));
		} catch (ClassNotFoundException e) {
			logger.error("Class file for "+pluginName+" plugin not found. It will not be available.",e);
		} catch (InstantiationException e) {
			logger.error("Plugin "+pluginName+" cannot be instantiated. ",e);
		} catch (Throwable e) {
			logger.error("Error initialising "+pluginName+" plugin: " , e);
		}

	}

	/**
	 * @return Returns the pluginName.
	 */
	public String getPluginName() {
		return pluginName;
	}

	/**
	 * @param pluginName
	 *            The pluginName to set.
	 */
	public void setPluginName(String pluginName) {
		this.pluginName = pluginName;
	}
}