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
import gda.oe.MoveableException;
import gda.oe.dofs.DOF;
import gda.util.ObjectServer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.jscience.physics.quantities.Quantity;
import org.jscience.physics.units.NonSI;
import org.jscience.physics.units.SI;
import org.jscience.physics.units.Unit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GenericOETest Class
 */
public class GenericOETest extends TestCase {
	private static final Logger logger = LoggerFactory.getLogger(GenericOETest.class);

	ArrayList<Unit<? extends Quantity>> validAngularUnits = new ArrayList<Unit<? extends Quantity>>();

	ArrayList<Unit<? extends Quantity>> validLinearUnits = new ArrayList<Unit<? extends Quantity>>();

	ArrayList<Unit<? extends Quantity>> validEnergyUnits = new ArrayList<Unit<? extends Quantity>>();

	ObjectServer os;

	final int timeout = 100000;

	final int sleepTime = 5000;

	// set up test environment
	static String gdaRoot = System.getProperty("gda.src.java");

	static String oeDir = gdaRoot + File.separator /*
													 * +"src" + File.separator + "java" + File.separator
													 */+ "tests" + File.separator + "gda" + File.separator + "oe";

	// static String xmlFile = oeDir + File.separator +
	// "testSimpleGenericOE.xml";
	static String xmlFile = oeDir + File.separator + "test.xml";

	static String propertiesFile = oeDir + File.separator + "java.properties";

	static String jacorbDir = oeDir;

	static String oeMoveDir = oeDir + File.separator + "oemove";

	static String motorDir = oeDir + File.separator + "motpos";

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		GenericOETest test = new GenericOETest();
		try {
			test.setUp();
		} catch (Exception e) {
			logger.debug(e.getStackTrace().toString());
		}
		test.testAllGenericOEs();
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		logger.debug("gdaRoot=" + gdaRoot);
		logger.debug("oeDir=" + oeDir);
		logger.debug("xmlFile=" + xmlFile);
		logger.debug("propertiesFile=" + propertiesFile);
		logger.debug("jacorbDir=" + jacorbDir);
		logger.debug("oeMoveDir=" + oeMoveDir);
		logger.debug("motorDir=" + motorDir);

		System.setProperty("gda.propertiesFile", propertiesFile);
		System.setProperty("jacorb.config.dir", jacorbDir);
		System.setProperty("gda.oe.oemoveDir", oeMoveDir);
		System.setProperty("gda.motordir", motorDir);

		validLinearUnits = getValidLinearUnits();
		validAngularUnits = getValidAngularUnits();
		validEnergyUnits = getValidEnergyUnits();

		os = ObjectServer.createServerImpl(xmlFile.replace('\\', '/'), null);
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

		// test these three
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
		assertNotNull(gdaRoot);
		assertTrue(new File(xmlFile).exists());
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

				testGenericClassName(oe);

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

					testDofAttributes(dof);
					// testOEinterface(oe, dof);
				}
			}
		}
	}

	private void testGenericClassName(GenericOE oe) {
		assertEquals("gda.oe.GenericOE", oe.getClass().getName());
	}

	private void testDofAttributes(DOF dof) {
		logger.debug("\ntestDofAttributes(), DOF = " + dof.getName());
		String name = dof.getName();
		assertNotNull(name);
		testGetAcceptableUnits(dof);
		testDofReportingUnit(dof);
		testDofReportingUnits(dof);
		testDofProtectionLevel(dof);
	}

	/**
	 * @param dof
	 *            the DOF to obtain acceptable units from
	 */
	private void testGetAcceptableUnits(DOF dof) {
		logger.debug("   testGetAcceptableUnits(), DOF = " + dof.getName());
		boolean validLength;
		boolean validAngle;
		boolean validEnergy;

		ArrayList<Unit<? extends Quantity>> units = dof.getAcceptableUnits();
		assertNotNull(units);
		assertNotNull(units.get(0));
		for (Unit<? extends Quantity> unit : units) {
			validLength = validLinearUnits.contains(unit);
			validAngle = validAngularUnits.contains(unit);
			validEnergy = validEnergyUnits.contains(unit);
			assertTrue(validLength || validAngle || validEnergy);
		}
	}

	private void testDofReportingUnit(DOF dof) {
		logger.debug("   testDofReportingUnit(), DOF = " + dof.getName());
		int i, j;
		boolean stringMatch = false;
		ArrayList<?> list[] = { validLinearUnits, validAngularUnits, validEnergyUnits };

		for (j = 0; j < 3; j++) {
			for (i = 0; i < list[j].size(); i++) {
				if (list[j].get(i).toString().equalsIgnoreCase(dof.getReportingUnits().toString())) {
					stringMatch = true;
					break;
				}
			}
			if (stringMatch)
				break;
		}
		if (!stringMatch) {
			System.out.println("units of dof under test: " + dof.getReportingUnits().toString());
		}
		assertTrue(stringMatch);
	}

	private void testDofReportingUnits(DOF dof) {
		logger.debug("   testDofReportingUnits(), DOF = " + dof.getName());
		boolean validLength = validLinearUnits.contains(dof.getReportingUnits());
		boolean validAngle = validAngularUnits.contains(dof.getReportingUnits());
		boolean validEnergy = validEnergyUnits.contains(dof.getReportingUnits());

		assertTrue(validLength || validAngle || validEnergy);
	}

	private void testDofProtectionLevel(DOF dof) {
		logger.debug("   testDofProtectionLevel(), DOF = " + dof.getName());

		int protectionLevel = dof.getProtectionLevel();
		assertTrue(protectionLevel == 0 || protectionLevel == 1);
	}

	/**
	 * @param oe
	 * @param dof
	 * @throws InterruptedException
	 */
	public void testOEinterface(GenericOE oe, DOF dof) throws InterruptedException {
		logger.debug("\ntestOEinterface(), DOF = " + dof.getName());
		assertNotNull(oe);
		assertNotNull(dof);
		assertNotNull(dof.getName());

		try {
			oe.getPosition(dof.getName());
		} catch (MoveableException e) {
			logger.debug(e.getStackTrace().toString());
		}
		Unit<? extends Quantity> unit = dof.getReportingUnits();
		Quantity value = Quantity.valueOf(5, unit);
		testMoveTo(oe, dof, value);
		testMoveBy(oe, dof, value);

		value = Quantity.valueOf(-2, unit);
		testMoveTo(oe, dof, value);
		testMoveBy(oe, dof, value);

		value = Quantity.valueOf(0.5, unit);
		testMoveTo(oe, dof, value);
		testMoveBy(oe, dof, value);

		Quantity increment = Quantity.valueOf(-0.5, unit);
		testMoveTo(oe, dof, increment);
		testMoveBy(oe, dof, increment);

		value = Quantity.valueOf(0, unit);
		testMoveTo(oe, dof, value);
		testMoveBy(oe, dof, value);
	}

	private final void testMoveBy(GenericOE oe, DOF dof, Quantity increment) throws InterruptedException {
		logger.debug("   testMoveBy(), DOF = " + dof.getName());
		logger.debug("      Moving " + oe.getName() + " " + dof.getName() + " by " + increment.doubleValue());
		try {
			Quantity pos = oe.getPosition(dof.getName());
			logger.debug(" Posn = " + pos.doubleValue());
			Quantity target = oe.getPosition(dof.getName()).plus(increment);
			oe.moveBy(dof.getName(), increment);

			int timeSlept = 0;
			while (dof.isMoving() && timeSlept < timeout) {
				pos = oe.getPosition(dof.getName());
				logger.debug("      Moving..." + pos.doubleValue());
				Thread.sleep(sleepTime);
				timeSlept += sleepTime;
			}
			if (dof.isMoving()) {
				fail();
			} else {
				pos = oe.getPosition(dof.getName());
				logger.debug(" Move Complete, pos = " + pos);
				double diff = pos.floatValue() - target.floatValue();
				double tolerance = 0.02;
				assertTrue(diff > -tolerance && diff < tolerance);
			}
		} catch (MoveableException e) {
			logger.debug(e.getStackTrace().toString());
			fail();
		}
	}

	private final void testMoveTo(GenericOE oe, DOF dof, Quantity target) throws InterruptedException {
		logger.debug("   testMoveTo(), DOF = " + dof.getName());
		logger.debug("      Moving " + oe.getName() + " " + dof.getName() + " to " + target.doubleValue());
		try {
			Quantity pos = oe.getPosition(dof.getName());
			logger.debug(" Posn = " + pos.doubleValue());
			assertTrue(!oe.isMoving(dof.getName()));

			oe.moveTo(dof.getName(), target);

			int timeSlept = 0;
			while (oe.isMoving(dof.getName()) && timeSlept < timeout) {
				pos = oe.getPosition(dof.getName());
				logger.debug("      Moving..." + pos.doubleValue());
				Thread.sleep(sleepTime);
				timeSlept += sleepTime;
			}
			if (oe.isMoving(dof.getName())) {
				logger.debug(" oe.moveTo timed out");
				fail();
			} else {
				pos = oe.getPosition(dof.getName());
				logger.debug(" Move Complete, pos = " + pos);
				double diff = pos.floatValue() - target.floatValue();
				double tolerance = 0.02;
				assertTrue(diff > -tolerance && diff < tolerance);
			}
		} catch (MoveableException e) {
			logger.debug(e.getStackTrace().toString());
			fail();
		}
	}
}