/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

import java.io.File;

import gda.jython.ICommandRunner;
import gda.jython.commandinfo.CommandThreadEvent;

public class MockCommandRunner implements ICommandRunner {

	@Override
	public void runCommand(String command) {
		// TODO Auto-generated method stub

	}

	@Override
	public void runCommand(String command, String scanObserver) {
		// TODO Auto-generated method stub

	}

	@Override
	public String evaluateCommand(String command) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CommandThreadEvent runScript(File script, String sourceName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean runsource(String command, String source) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String locateScript(String scriptToRun) {
		return "MockCommandRunner" + File.separator + scriptToRun;
	}

}
