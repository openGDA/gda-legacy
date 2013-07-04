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

package gda.gui.oemove.plugins;

import gda.factory.Configurable;
import gda.factory.FactoryException;
import gda.factory.Findable;
import gda.factory.Finder;
import gda.gui.oemove.Pluggable;

import java.lang.reflect.Method;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GenericPlugin Class
 */
public class GenericPlugin implements Pluggable, Findable, Configurable {
	private static final Logger logger = LoggerFactory.getLogger(GenericPlugin.class);

	private String name;

	private String controlComponentName;

	private String displayComponentName = null;

	private String displayComponentClass = null;

	private JPanel controlComponent = new JPanel();

	private JPanel displayComponent = new JPanel();

	/**
	 * Constructor
	 */
	public GenericPlugin() {
	}

	@Override
	public JComponent getDisplayComponent() {
		return displayComponent;
	}

	@Override
	public JComponent getControlComponent() {
		return controlComponent;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return controlComponentName
	 */
	public String getControlComponentName() {
		return controlComponentName;
	}

	/**
	 * @param controlComponentName
	 */
	public void setControlComponentName(String controlComponentName) {
		this.controlComponentName = controlComponentName;
	}

	/**
	 * @return displayComponentName
	 */
	public String getDisplayComponentName() {
		return displayComponentName;
	}

	/**
	 * @param displayComponentName
	 */
	public void setDisplayComponentName(String displayComponentName) {
		this.displayComponentName = displayComponentName;
	}

	/**
	 * @return displayComponentClass
	 */
	public String getDisplayComponentClass() {
		return displayComponentClass;
	}

	/**
	 * @param displayComponentClass
	 */
	public void setDisplayComponentClass(String displayComponentClass) {
		this.displayComponentClass = displayComponentClass;
	}

	@Override
	public void configure() throws FactoryException {
		try {
			if (displayComponentName != null) {
				displayComponent = (JPanel) Finder.getInstance().find(displayComponentName);
			} else if (displayComponentClass != null) {
				displayComponent = (JPanel) Class.forName(displayComponentClass).newInstance();
				// Does it implement configurable? Call the configure method if
				// so.
				if (displayComponent instanceof Configurable) {
					((Configurable) displayComponent).configure();
				} else {
					// Does the class have a configure method with no
					// arguments?
					// If so call it.
					try {
						Method configure = displayComponent.getClass().getMethod("configure", (Class[]) null);
						configure.invoke(displayComponent, (Object[]) null);
					} catch (NoSuchMethodException e) {
						// We don't really care if it doesn't have a configure
						// method.
					}
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}
}
