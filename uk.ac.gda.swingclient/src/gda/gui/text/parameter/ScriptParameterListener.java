/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package gda.gui.text.parameter;

import gda.jython.InterfaceProvider;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;

import javax.swing.JComponent;
import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * class to respond to ParametersPanelBuilder.ParameterChangeEventSource events to run a script to handle the new value
 */
public class ScriptParameterListener implements VetoableChangeListener {

	private static final Logger logger = LoggerFactory.getLogger(ScriptParameterListener.class);

	final private String command, observerName;

	private final JComponent component;

	/**
	 * @param command
	 * @param observerName 
	 * @param component
	 */
	public ScriptParameterListener(String command, String observerName, JComponent component) {
		this.command = command;
		this.observerName = observerName;
		if (command == null) {
			throw new IllegalArgumentException("ScriptParameterListener. command  is null");
		}
		this.component = component;
	}

	@Override
	public void vetoableChange(PropertyChangeEvent e) throws PropertyVetoException {
		Object source = e.getSource();
		if (source == null)
			throw new IllegalArgumentException("ScriptParameterListener.propertyChange - source == null ");
		final Object newObject = e.getNewValue();
		if (newObject == null) {
			throw new IllegalArgumentException("ScriptParameterListener.propertyChange -  (newObject == null ) ");
		}
		try {
			final String s = command + "(" + newObject.toString() + ")";
			if( observerName == null || observerName.isEmpty()){
				InterfaceProvider.getCommandRunner().runCommand(s);
			} else {
				InterfaceProvider.getCommandRunner().runCommand(s,observerName);
			}
				
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(component.getTopLevelAncestor(), ex.getMessage(), "ScriptParameterListener",
					JOptionPane.ERROR_MESSAGE);
			logger.error("Error in vetoableChange :", ex);
		}
	}
}
