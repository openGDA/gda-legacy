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

//
// GenericOETest uses its own local properties and xml files. VM args
// "gda.propertiesFile" & "jacorb.config.dir" SHOULD NOT BE SET
//

package gda.oe;

import gda.factory.Findable;
import gda.jscience.physics.units.NonSIext;
import gda.oe.GenericOE;
import gda.oe.Moveable;
import gda.oe.dofs.DOF;
import gda.util.ObjectServer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestListener;
import junit.framework.TestResult;
import junit.framework.TestSuite;

import org.jscience.physics.quantities.Quantity;
import org.jscience.physics.units.NonSI;
import org.jscience.physics.units.SI;
import org.jscience.physics.units.Unit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OETest Class
 */
public class OETest extends TestCase implements TestListener {
	private static final Logger logger = LoggerFactory.getLogger(OETest.class);

	ArrayList<Unit<? extends Quantity>> validAngularUnits = new ArrayList<Unit<? extends Quantity>>();

	ArrayList<Unit<? extends Quantity>> validLinearUnits = new ArrayList<Unit<? extends Quantity>>();

	ArrayList<Unit<? extends Quantity>> validEnergyUnits = new ArrayList<Unit<? extends Quantity>>();

	ObjectServer os;

	final int timeout = 50000;

	final int sleepTime = 4000;

	// FIXME think using userDir only works within Eclipse !
	static String userDir = System.getProperty("user.dir").replace('\\', '/');

	// static String userDir = System.getProperty("user.dir");
	static String oeDir = "/gda/oe";

	static String testXmlFile = userDir + oeDir + "/testSimpleGenericOE.xml";

	static String propertiesFile = userDir + oeDir + "/java.properties";

	static String jacorbDir = userDir + oeDir;

	static String oeMoveDir = userDir + oeDir + "/oemove";

	static String motorDir = userDir + oeDir + "/motpos";

	static TestSuite suite = new TestSuite("Test All OEs in xml file");

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		junit.textui.TestRunner.run(suite());
	}

	/**
	 * @return Test Suite
	 */
	public static Test suite() {
		// suite.addTest(new GenericOETest());
		return new TestSuite(OETest.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		System.setProperty("gda.propertiesFile", propertiesFile);
		System.setProperty("jacorb.config.dir", jacorbDir);
		System.setProperty("gda.oe.oemoveDir", oeMoveDir);
		System.setProperty("gda.motordir", motorDir);

		validLinearUnits = getValidLinearUnits();
		validAngularUnits = getValidAngularUnits();
		validEnergyUnits = getValidEnergyUnits();

		os = ObjectServer.createLocalImpl(testXmlFile);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	private ArrayList<Unit<? extends Quantity>> getValidLinearUnits() {
		ArrayList<Unit<? extends Quantity>> validLinearUnits = new ArrayList<Unit<? extends Quantity>>();

		validLinearUnits.add(NonSI.ANGSTROM);
		validLinearUnits.add(NonSI.ASTRONOMICAL_UNIT);
		validLinearUnits.add(NonSI.BOHR);
		validLinearUnits.add(SI.CENTI(SI.METER));
		validLinearUnits.add(NonSI.CICERO);
		validLinearUnits.add(NonSI.COMPUTER_POINT);
		validLinearUnits.add(SI.DECI(SI.METER));
		validLinearUnits.add(NonSI.DIDOT);
		validLinearUnits.add(SI.FEMTO(SI.METER));
		validLinearUnits.add(NonSI.FOOT);
		validLinearUnits.add(NonSI.INCH);
		validLinearUnits.add(SI.KILO(SI.METER));
		validLinearUnits.add(NonSI.LIGHT_YEAR);
		validLinearUnits.add(SI.METER);
		validLinearUnits.add(SI.MICRO(SI.METER));
		validLinearUnits.add(NonSI.MILE);
		validLinearUnits.add(SI.MILLI(SI.METER));
		validLinearUnits.add(SI.NANO(SI.METER));
		validLinearUnits.add(NonSI.NAUTICAL_MILE);
		validLinearUnits.add(NonSI.PARSEC);
		validLinearUnits.add(NonSI.PICA);
		validLinearUnits.add(SI.PICO(SI.METER));
		validLinearUnits.add(NonSI.POINT);
		validLinearUnits.add(NonSI.PIXEL);
		validLinearUnits.add(NonSI.YARD);

		return validLinearUnits;
	}

	private ArrayList<Unit<? extends Quantity>> getValidAngularUnits() {
		ArrayList<Unit<? extends Quantity>> validAngularUnits = new ArrayList<Unit<? extends Quantity>>();

		validAngularUnits.add(SI.CENTI(SI.RADIAN));
		validAngularUnits.add(NonSIext.DEG_ANGLE);
		validAngularUnits.add(NonSI.GRADE);
		validAngularUnits.add(NonSIext.mDEG_ANGLE);
		validAngularUnits.add(SI.MICRO(SI.RADIAN));
		validAngularUnits.add(NonSI.MINUTE);
		validAngularUnits.add(SI.RADIAN);
		validAngularUnits.add(SI.MILLI(SI.RADIAN));
		validAngularUnits.add(NonSI.REVOLUTION);
		validAngularUnits.add(NonSI.SECOND_ANGLE);

		return validAngularUnits;
	}

	private ArrayList<Unit<? extends Quantity>> getValidEnergyUnits() {
		ArrayList<Unit<? extends Quantity>> validEnergyUnits = new ArrayList<Unit<? extends Quantity>>();

		validEnergyUnits.add(NonSI.BTU);
		validEnergyUnits.add(NonSI.BTU_MEAN);
		validEnergyUnits.add(NonSI.BTU_TH);
		validEnergyUnits.add(NonSI.CALORIE);
		validEnergyUnits.add(NonSI.C);
		validEnergyUnits.add(NonSI.ERG);
		validEnergyUnits.add(NonSI.ELECTRON_VOLT);
		validEnergyUnits.add(NonSI.POUND_FORCE);
		validEnergyUnits.add(SI.GIGA(NonSI.ELECTRON_VOLT));
		validEnergyUnits.add(SI.KILO(NonSI.ELECTRON_VOLT));
		validEnergyUnits.add(SI.KILO(NonSI.CALORIE));
		validEnergyUnits.add(NonSI.KILOGRAM_FORCE);
		validEnergyUnits.add(SI.KILO(SI.JOULE));
		validEnergyUnits.add(SI.MEGA(NonSI.ELECTRON_VOLT));
		validEnergyUnits.add(SI.NEWTON);
		validEnergyUnits.add(NonSI.THERM);
		validEnergyUnits.add(SI.JOULE);
		validEnergyUnits.add(SI.MEGA(SI.JOULE));

		return validEnergyUnits;
	}

	/**
	 * 
	 */
	public void testSetUp() {
		assertTrue(new File(testXmlFile).exists());
	}

	/**
	 * 
	 */
	public void testAllGenericOEs() {
		List<String> findableNames = os.getFindableNames();
		ArrayList<Findable> oeList = new ArrayList<Findable>();

		// Extract all OEs from XML
		int i;
		for (i = 0; i < findableNames.size(); i++) {
			Findable o = os.getFindable(findableNames.get(i));
			if (o.getClass().getName().equals("gda.oe.GenericOE")) {
				oeList.add(o);
			}

			// get all DOFs from OE list
			ArrayList<Moveable> dofs = new ArrayList<Moveable>();
			int j;
			for (j = 0; j < oeList.size(); j++) {
				GenericOE oe = (GenericOE) oeList.get(j);
				assertNotNull(oe);
				assertNotNull(oe.getName());

				// testGenericClassName(oe);

				dofs = oe.getUseableMoveableList();
				assertNotNull(dofs);
				logger.debug(oe.getName());
				int k;
				for (k = 0; k < dofs.size(); k++) {
					DOF dof = (DOF) dofs.get(k);
					assertNotNull(dof);
					String dofName = dof.getName();
					assertNotNull(dofName);
					logger.debug("    " + dofName);
					TestCase test = new GenericOETest();
					TestResult result = new TestResult();
					result.addListener(this);
					result = test.run();
					assertTrue(result.wasSuccessful());
					System.out.println(result.toString());
					// testDofAttributes(dof);
					// testOEinterface(oe, dof);
				}
			}
		}
	}

	@Override
	public void addError(Test arg0, Throwable arg1) {
		System.out.println("Test " + arg0 + " exception " + arg1);
	}

	@Override
	public void addFailure(Test arg0, AssertionFailedError arg1) {
		System.out.println("Test " + arg0 + " AssertionFailedError " + arg1);
	}

	@Override
	public void endTest(Test arg0) {
	}

	@Override
	public void startTest(Test arg0) {
	}
}