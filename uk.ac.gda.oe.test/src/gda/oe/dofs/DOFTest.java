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

package gda.oe.dofs;

import gda.device.motor.TotalDummyMotor;
import gda.factory.Finder;
import gda.factory.ObjectFactory;
import gda.jscience.physics.units.NonSIext;
import gda.observable.IObserver;
import gda.oe.Moveable;
import gda.oe.MoveableException;
import gda.oe.MoveableStatus;
import gda.oe.dofs.SingleAxisLinearDOF;
import gda.oe.positioners.LinearPositioner;
import gda.util.QuantityFactory;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.jscience.physics.quantities.Length;
import org.jscience.physics.quantities.Quantity;
import org.jscience.physics.units.ConversionException;
import org.jscience.physics.units.SI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * To change the template for this generated type comment go to Window - Preferences - Java - Code Generation - Code and
 * Comments
 */
public class DOFTest extends TestCase implements IObserver {
	
	private static final Logger logger = LoggerFactory.getLogger(DOFTest.class);
	
	private SingleAxisLinearDOF dof = new SingleAxisLinearDOF();

	private LinearPositioner lp = new LinearPositioner();

	static String userDir = System.getProperty("user.dir").replace('\\', '/');

	static String dofDir = "/tests/gda/oe/dofs";

	static String propertiesFile = userDir + dofDir + "/java.properties";

	static String jacorbDir = userDir + dofDir;

	static String motorDir = userDir + dofDir + "/motpos";

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		junit.textui.TestRunner.run(suite());
	}

	/**
	 * @return test suite
	 */
	public static Test suite() {
		return new TestSuite(DOFTest.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		System.setProperty("gda.propertiesFile", propertiesFile);
		System.setProperty("jacorb.config.dir", jacorbDir);
		System.setProperty("gda.motordir", motorDir);

		TotalDummyMotor dm = new TotalDummyMotor();
		dm.setName("dummyMotor");
		dm.configure();
		ObjectFactory factory = new ObjectFactory();
		factory.addFindable(dm);
		Finder.getInstance().addFactory(factory);

		lp.setMotorName("dummyMotor");
		lp.setName("linearPositioner");
		lp.setStepsPerUnit(1);
		lp.setSoftLimitHigh(100.0);
		lp.setSoftLimitLow(-100.0);
		lp.configure();

		dof.setName("dof");
		dof.setReportingUnits(QuantityFactory.createUnitFromString("mm"));
		dof.addMoveable(lp);
		dof.addMoveableName("linearPositioner");
		dof.configure();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/**
	 * This method is tested in testMoveBy()
	 */
	public void testCheckMoveBy() {
		// This method is tested in testMoveBy()
	}

	/**
	 * This method is tested in testMoveTo()
	 */
	public void testCheckMoveTo() {
		// This method is tested in testMoveTo()
	}

	/**
	 * This method is tested in testSetPosition()
	 */
	public void testCheckSetPosition() {
		// This method is tested in testSetPosition()
	}

	/**
	 * 
	 */
	public void testCheckHome() {
	}

	/**
	 * This method is tested in testMoveBy() and testMoveTo()
	 */
	public void testDoMove() {
		// This method is tested in testMoveBy() and testMoveTo()
	}

	/**
	 * This method is tested in testSetPosition()
	 */
	public void testDoSet() {
		// This method is tested in testSetPosition()
	}

	/**
	 * 
	 */
	public void testDoHome() {
	}

	/**
	 * 
	 */
	public void testStop() {
	}

	/**
	 * 
	 */
	public void testIsMoving() {
	}

	/**
	 * 
	 */
	public void testGetStatus() {
	}

	/**
	 * 
	 */
	public void testSetSpeedLevel() {
	}

	/**
	 * 
	 */
	public void testIsSpeedLevelSettable() {
	}

	/**
	 * 
	 */
	public void testIsHomeable() {
	}

	/**
	 * 
	 */
	public void testSetOffset() {
	}

	/**
	 * 
	 */
	public void testGetOffset() {
	}

	/**
	 * 
	 */
	public void testSetPositionCorrection() {
	}

	/**
	 * 
	 */
	public void testRefresh() {
	}

	/**
	 * 
	 */
	public void testUnLock() {
	}

	/**
	 * 
	 */
	public void testConfigure() {
	}

	/**
	 * 
	 */
	public void testAddMoveable() {
	}

	/**
	 * 
	 */
	public void testGetMoveables() {
	}

	/**
	 * 
	 */
	public void testAddMoveableName() {
	}

	/**
	 * 
	 */
	public void testGetMoveableNames() {
	}

	/**
	 * 
	 */
	public void testAddMoveables() {
	}

	/**
	 * 
	 */
	public void testGetMoveableNames_db() {
	}

	/**
	 * 
	 */
	public void testSetMoveableNames_db() {
	}

	/**
	 * 
	 */
	public void testAddMoveables_db() {
	}

	/**
	 * 
	 */
	public void testSetProtectionLevel() {
	}

	/**
	 * 
	 */
	public void testGetProtectionLevel() {
	}

	/**
	 * 
	 */
	public void testSetReportingUnits() {
	}

	/**
	 * 
	 */
	public void testGetReportingUnits() {
	}

	/**
	 * 
	 */
	public void testSetFormatting() {
	}

	/**
	 * 
	 */
	public void testFormatPosition() {
	}

	/**
	 * 
	 */
	public void testCalculateDecimalPlaces() {
	}

	/**
	 * 
	 */
	public void testGetAcceptableUnits() {
	}

	/**
	 * 
	 */
	public void testIsPositionValid() {
	}

	/**
	 * 
	 */
	public void testUpdate() {
	}

	/**
	 * 
	 */
	public void testUpdateStatus() {
	}

	/**
	 * 
	 */
	public void testIsScannable() {
	}

	/**
	 * 
	 */
	public void testGetDOFType() {
	}

	/**
	 * 
	 */
	public void testSetPosition() {
		Quantity q = Quantity.valueOf(0.0, SI.MILLI(SI.METER));

		try {
			assertEquals(MoveableStatus.SUCCESS, dof.checkSetPosition(q, this));
			dof.doSet(this);
			while (dof.getStatus().value() == MoveableStatus.BUSY)
				continue;
		} catch (MoveableException e) {
			logger.error("Caught exception: " + e);
			logger.debug(e.getStackTrace().toString());
		}

		assertEquals(q, dof.getPosition());
	}

	/**
	 * 
	 */
	public void testMoveBy() {
		Quantity q = Quantity.valueOf(0.0, SI.MILLI(SI.METER));

		try {
			assertEquals(MoveableStatus.SUCCESS, dof.checkSetPosition(q, this));
			dof.doSet(this);
			while (dof.getStatus().value() == MoveableStatus.BUSY) {
				this.wait(100);
				assertNotSame(q, dof.getPosition());
			}
		} catch (Exception e) {
			logger.error("Caught exception: " + e.getMessage());
			fail();
		}

		assertEquals(q, dof.getPosition());
		Quantity move = Quantity.valueOf(10.0, SI.MILLI(SI.METER));
		assertEquals(MoveableStatus.SUCCESS, dof.checkMoveBy(move, this));
		doTheMove();
		assertEquals(move, dof.getPosition());
	}

	/**
	 * 
	 */
	public void testMoveTo() {
		Quantity move = Quantity.valueOf(10.0, SI.MILLI(SI.METER));
		assertEquals(MoveableStatus.SUCCESS, dof.checkMoveTo(move, this));
		doTheMove();
		assertEquals(move, dof.getPosition());
		move = Quantity.valueOf(lp.getSoftLimitHigh(), SI.MILLI(SI.METER));
		assertEquals(MoveableStatus.SUCCESS, dof.checkMoveTo(move, this));
		doTheMove();
		assertEquals(move, dof.getPosition());
		move = Quantity.valueOf(lp.getSoftLimitLow() - 1, SI.MILLI(SI.METER));
		assertEquals(MoveableStatus.SOFT_LIMIT, dof.checkMoveTo(move, this));
		move = Quantity.valueOf(lp.getSoftLimitHigh() + 1, SI.MILLI(SI.METER));
		assertEquals(MoveableStatus.SOFT_LIMIT, dof.checkMoveTo(move, this));
	}

	/**
	 * Class under test for Quantity getPosition(Unit). NB this test is last because there currently is a bug.
	 */
	@SuppressWarnings("static-access")
	public void testGetPositionUnit() {
		double len = 10.0;
		Quantity q = Quantity.valueOf(len, SI.MILLI(SI.METER));
		Quantity answer = Length.valueOf(len, SI.MILLI(SI.METER));

		try {
			assertEquals(MoveableStatus.SUCCESS, dof.checkSetPosition(q, this));
			dof.doSet(this);
			Thread.sleep(lp.getPollTime() * 2);
		} catch (MoveableException e) {
			logger.error("Caught exception: " + e);
			logger.debug(e.getStackTrace().toString());
		} catch (InterruptedException e) {
			logger.error("Caught exception: " + e);
			logger.debug(e.getStackTrace().toString());
		}

		Quantity pos = dof.getPosition(SI.METER);
		assertEquals(answer, pos);

		// Trying to get with units which are not Length.Units should
		// cause a ConversionException
		try {
			pos = dof.getPosition(NonSIext.DEG_ANGLE);
			fail("testGetPosition(Unit) ConversionException expected");
		} catch (ConversionException cce) {
		}

		// This currently fails because reporting units are left set
		// at Angle.DEG by the failed getPosition(Angle.DEG)
		pos = null;
		pos = dof.getPosition();
		assertNotNull("Failure due to bug in getPosition(Unit)", pos);

	}

	private void doTheMove() {

		try {
			dof.doMove(this, 1);
			while (dof.isMoving())
				continue;
		} catch (MoveableException e1) {
			logger.error("Caught exception: " + e1);
			logger.debug(e1.getStackTrace().toString());
		}
		try {
			Thread.sleep(lp.getPollTime() * 2);
		} catch (InterruptedException e2) {
			logger.error("Caught exception: " + e2);
			logger.debug(e2.getStackTrace().toString());
		}

	}

	@Override
	public void update(Object theObserved, Object changeCode) {
		if (changeCode instanceof MoveableStatus) {
			MoveableStatus ds = (MoveableStatus) changeCode;
			if ((ds.value() != MoveableStatus.BUSY) && (ds.value() != MoveableStatus.AWAY_FROM_LIMIT)) {
				((Moveable) theObserved).unLock(this);
			}
		}
	}

}