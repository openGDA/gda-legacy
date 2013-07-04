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
 * A class for something
 */
public class MessagePlugin extends MessagePanel {
	
	private static final Logger logger = LoggerFactory.getLogger(MessagePlugin.class);
	
	private String pluginName;

	/**
	 * Constructor
	 */
	public MessagePlugin() {
	}


	@Override
	public void configure() throws FactoryException {
		setLayout(new BorderLayout());
		try {
			JPanel jpanel = (JPanel) Class.forName(pluginName).newInstance();
			add(jpanel, BorderLayout.CENTER);

			setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLoweredBevelBorder(), BorderFactory
					.createEmptyBorder(0, 0, 0, 0)));

		} catch (InstantiationException e) {
			logger.error("Can NOT find the required panel " + e.getMessage());
		} catch (IllegalAccessException e) {
			logger.error(e.getMessage());
		} catch (ClassNotFoundException e) {
			logger.error(e.getMessage());
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