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

package gda.gui.text.TextArea;

import junit.framework.TestCase;

import java.io.*;
import javax.swing.*;

/**
 * SimpleLoggerTest
 */
public class SimpleLoggerTest extends TestCase {

	/**
	 * @param name
	 */
	public SimpleLoggerTest(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/**
	 * 
	 */
	public final void testSimpleLogger() {
		JTextArea ta = new JTextArea();
		SimpleLogger simpleLogger = new SimpleLogger(ta);
		PrintWriter x = new PrintWriter(simpleLogger);
		x.print("Hello");
		x.print(' ');
		x.print("World");
		assertEquals("Hello World", ta.getText());
		simpleLogger.clear();
		assertEquals("", ta.getText());

	}
}
