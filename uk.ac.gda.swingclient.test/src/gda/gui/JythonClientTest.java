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

import gda.configuration.properties.LocalProperties;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class JythonClientTest {

	@Before
	public void setUp() {
		System.clearProperty("jyconsole.pref.script.dir");
		System.clearProperty("jyconsole.pref.loading.script");
		LocalProperties.set("gda.jython.gdaScriptDir", "gda.jython.gdaScriptDir");
		JythonClient.setJythonContextForTesting(new MockJythonContext());
		JythonClient.setCommandRunnerForTesting(new MockCommandRunner());
		JythonClient.configureSystemProperties();
	}

	@After
	public void tearDown() {
		System.clearProperty("jyconsole.pref.script.dir");
		System.clearProperty("jyconsole.pref.loading.script");
		LocalProperties.clearProperty("gda.jython.gdaScriptDir");
		JythonClient.setJythonContextForTesting(null);
	}

	@Test
	public void testScriptDirDoesNotGetSetViaLocalProperties() {
		Assert.assertThat(System.getProperty("jyconsole.pref.script.dir"), is(not("gda.jython.gdaScriptDir")));
		Assert.assertThat(System.getProperty("jyconsole.pref.script.dir"), is("MockJythonContext"));
	}

	@Test
	public void testLoadingScriptDoesNotGetSetViaLocalProperties() {
		Assert.assertThat(System.getProperty("jyconsole.pref.loading.script"), is(not("gda.jython.gdaScriptDir" + File.separator + "start_gda.py")));
		Assert.assertThat(System.getProperty("jyconsole.pref.loading.script"), is("MockCommandRunner" + File.separator + "start_gda.py"));
	}
}
