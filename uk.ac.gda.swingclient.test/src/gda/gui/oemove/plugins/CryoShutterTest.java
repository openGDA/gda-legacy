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

package gda.gui.oemove.plugins;


import java.io.File;

import gda.configuration.properties.LocalProperties;
import gda.gui.MockCommandRunner;

import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.Is.is;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class CryoShutterTest {

	private CryoShutter cryoShutter;
	@Before
	public void setUp() {
		LocalProperties.set("gda.jython.gdaScriptDir", "gda.jython.gdaScriptDir");
		cryoShutter = new CryoShutter();
		cryoShutter.setCommandRunnerForTesting(new MockCommandRunner());
	}

	@After
	public void tearDown() {
		LocalProperties.clearProperty("gda.jython.gdaScriptDir");
		cryoShutter = null;
	}

	@Test
	public void testScriptFileIsNotFoundUsingLocalProperties() {
		File scriptFile = cryoShutter.configureScriptFile("cryoshutter.py");
		Assert.assertThat(scriptFile.getPath(), is(not("gda.jython.gdaScriptDir/cryoshutter.py")));
		Assert.assertThat(scriptFile.getPath(), is("MockCommandRunner/cryoshutter.py"));
	}
}
