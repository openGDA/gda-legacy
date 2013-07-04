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

package gda.gui;

import gda.configuration.properties.LocalProperties;
import gda.factory.FactoryException;
import gda.jython.ICommandRunner;
import gda.jython.IJythonContext;
import gda.jython.JythonServerFacade;

import java.awt.BorderLayout;

import com.artenum.jyconsole.JyConsole;

/**
 * 
 */
public class JythonClient extends GUIMessagePanel {
	@Override
	public void configure() throws FactoryException {
		setLayout(new BorderLayout());

		configureSystemProperties();

		JyConsole console = new JyConsole();
		add(console, BorderLayout.CENTER);
	}

	private static IJythonContext jythonContextForTesting;
	private static IJythonContext getJythonContext() {
		return jythonContextForTesting != null ? jythonContextForTesting : JythonServerFacade.getInstance();
	}
	
	static void setJythonContextForTesting(IJythonContext ctx) {
		jythonContextForTesting = ctx;
	}
	
	private static ICommandRunner commandRunnerForTesting;
	private static ICommandRunner getCommandRunner() {
		return commandRunnerForTesting != null ? commandRunnerForTesting : JythonServerFacade.getInstance();
	}
	
	static void setCommandRunnerForTesting(ICommandRunner runner) {
		commandRunnerForTesting = runner;
	}
	
	static void configureSystemProperties() {
		// set system properties for the JyConsole
		//Base dir used to set the default open directory for script loading
		System.setProperty("jyconsole.pref.file.path",LocalProperties.getVarDir()+"preference.data");
		System.setProperty("jyconsole.pref.script.dir", getJythonContext().getDefaultScriptProjectFolder());
		System.setProperty("jyconsole.pref.txt.color.error","#FF0000");
		System.setProperty("jyconsole.pref.txt.color.normal","#000000");
		System.setProperty("jyconsole.pref.bg.color","#EEEEEE");
		// Select thread behavior between the two class name
		// - com.artenum.jyconsole.command.ThreadPerCommandRunner
		// - com.artenum.jyconsole.command.SingleThreadCommandRunner
		System.setProperty("jyconsole.pref.commandRunner.className","com.artenum.jyconsole.command.ThreadPerCommandRunner");
		//Choose to overide the standard output into the JyConsole or not.
		System.setProperty("jyconsole.pref.print.std.stream","false");
		//Choose to overide the standard output error into the JyConsole or not
		System.setProperty("jyconsole.pref.print.error.stream","false");
		//Loading script for initial configuration
		System.setProperty("jyconsole.pref.loading.script",getLoadingScript());
	}

	private static String getLoadingScript() {
		return getCommandRunner().locateScript("start_gda.py");
	}

}
