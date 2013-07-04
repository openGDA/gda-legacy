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

package gda.oe.positioners;

import gda.device.Motor;
import gda.device.motor.DummyMotor;
import gda.factory.Finder;
import gda.factory.ObjectFactory;
import gda.oe.MoveableException;
import gda.oe.MoveableStatus;
import gda.testlib.GDASetup;
import gda.util.Sleep;
import junit.framework.TestCase;

import org.jscience.physics.quantities.Quantity;
import org.jscience.physics.units.NonSI;
import org.jscience.physics.units.SI;

/**
 * LinearPositionerTest Class
 */
public class LinearPositionerTest extends TestCase {
	LinearPositioner lp = null;

	private double delta = 0.001;

	private double upperLimit = 10000;

	private double lowerLimit = -10000;

	private Quantity q;

	private double ul1, ul2, ll1, ll2;

	private double stepsPerUnit = 100;

	private String motorName = "SS_YMotor";

	/**
	 * @param name
	 */
	public LinearPositionerTest(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		GDASetup gdaSetup = GDASetup.getInstance();
		gdaSetup.setUpSimProperties();

		DummyMotor dm = new DummyMotor();
		dm.setName(motorName);
		dm.setBacklashSteps(0);
		dm.setFastSpeed(50);
		dm.setMediumSpeed(25);
		dm.setSlowSpeed(5);

		ObjectFactory objectFactory = new ObjectFactory();
		Finder.getInstance().addFactory(objectFactory);
		objectFactory.addFindable(dm);

		createLinearPositioner();
	}

	private void initialiseLimits() throws MoveableException {
		lp.setSoftLimitLow(lowerLimit);
		lp.setSoftLimitHigh(upperLimit);
	}

	private void createLinearPositioner() throws Exception {
		lp = new LinearPositioner();
		lp.setName("testPositioner01");
		lp.setMotorName(motorName);
		lp.setPoll(true);
		lp.setStepsPerUnit(stepsPerUnit);
		lp.configure();
	}

	/**
	 * 
	 */
	public void testSetup() {
		assertEquals("Name Error", "testPositioner01", lp.getName());
		assertEquals("StepPerUnit Error", stepsPerUnit, lp.getStepsPerUnit(), delta);
	}

	/**
	 * @throws Exception
	 */
	public void testSetWithLimitsSaveOn() throws Exception {
		initialiseLimits();

		ul1 = lp.getSoftLimitHigh();
		ll1 = lp.getSoftLimitLow();
		setTest(1.0);
		setTest(3.0);
		setTest(upperLimit - 10.0);
		setTest(upperLimit);
		setTest(upperLimit + 10.0);
		setTest(150);

		// Check the limits are persistent after re-creating the positioner.
		double sll = lp.getSoftLimitLow();
		double sul = lp.getSoftLimitHigh();
		createLinearPositioner();
		assertEquals("Lower Limit not persistent", sll, lp.getSoftLimitLow(), delta);
		assertEquals("Upper Limit not persistent", sul, lp.getSoftLimitHigh(), delta);
	}

	private void setTest(double position) throws Exception {
		lp.setPosition(position);
		Sleep.sleep((int) lp.getPollTime() * 2);
		q = Quantity.valueOf(position, SI.MILLI(SI.METER));
		assertEquals(q, lp.getPosition());
		ul2 = lp.getSoftLimitHigh();
		ll2 = lp.getSoftLimitLow();
		assertEquals("UPPER LIMIT SHIFT FAILED " + position, position, ul2 - ul1, delta);
		assertEquals("LOWER LIMIT SHIFT FAILED " + position, position, ll2 - ll1, delta);
	}

	/**
	 * @throws MoveableException
	 */
	public void testCheckMoveTo() throws MoveableException {
		initialiseLimits();

		q = lp.getPosition();
		q = q.plus(Quantity.valueOf(1.0, SI.MILLI(SI.METER)));
		assertEquals("CheckMove failed", MoveableStatus.SUCCESS, lp.checkMoveTo(q, this));
		q = Quantity.valueOf(upperLimit, SI.MILLI(SI.METER));
		assertEquals("CheckMove failed", MoveableStatus.SUCCESS, lp.checkMoveTo(q, this));
		q = Quantity.valueOf(upperLimit + 1, SI.MILLI(SI.METER));
		assertEquals("CheckMove failed", MoveableStatus.SOFT_LIMIT, lp.checkMoveTo(q, this));
		q = Quantity.valueOf(lowerLimit, SI.MILLI(SI.METER));
		assertEquals("CheckMove failed", MoveableStatus.SUCCESS, lp.checkMoveTo(q, this));
		q = Quantity.valueOf(lowerLimit - 1, SI.MILLI(SI.METER));
		assertEquals("CheckMove failed", MoveableStatus.SOFT_LIMIT, lp.checkMoveTo(q, this));
		q = Quantity.valueOf(0.0, SI.MILLI(SI.METER));
		assertEquals("CheckMove failed", MoveableStatus.SUCCESS, lp.checkMoveTo(q, this));
		q = Quantity.valueOf(Double.NaN, SI.MILLI(SI.METER));
		assertEquals("CheckMove failed", MoveableStatus.SOFT_LIMIT, lp.checkMoveTo(q, this));
	}

	/**
	 * @throws Exception
	 */
	public void testWithinLimits() throws Exception {
		initialiseLimits();

		assertTrue(lp.withinLimits(0));
		assertTrue(lp.withinLimits(lowerLimit));
		assertTrue(lp.withinLimits(upperLimit));
		assertFalse(lp.withinLimits(lowerLimit - 10));
		assertFalse(lp.withinLimits(upperLimit + 10));
	}

	/**
	 * @throws Exception
	 */
	public void testMoveContinuously() throws Exception {
		int status;

		lp.moveContinuously(+1);
		Sleep.sleep((int) lp.getPollTime() * 2);
		status = lp.getStatus().value();
		// Motors are not mandated to implement this function. They may just do
		// nothing. So check for busy OR ready.
		assertTrue(status == MoveableStatus.BUSY || status == MoveableStatus.READY);

		lp.stop();
		Sleep.sleep((int) lp.getPollTime() * 2);
		status = lp.getStatus().value();
		assertTrue(status == MoveableStatus.READY);
	}

	/**
	 * 
	 */
	public void testSpeedLevel() {
		if (lp.isSpeedLevelSettable()) {
			try {
				lp.setSpeedLevel(Motor.MEDIUM);
			} catch (MoveableException e) {
				fail("FAILED setting speed level");
			}
		}
	}

	/**
	 * 
	 */
	public void testCheckHome() {
		q = Quantity.valueOf(1.0, SI.RADIAN);
		assertEquals("CheckHome failed ", MoveableStatus.INCORRECT_QUANTITY, lp.checkHome(q, this));
		q = Quantity.valueOf(1.0, NonSI.ANGSTROM);
		assertEquals("CheckHome failed ", MoveableStatus.SUCCESS, lp.checkHome(q, this));
	}
}
