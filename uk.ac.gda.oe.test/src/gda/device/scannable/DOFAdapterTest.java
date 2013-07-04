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

package gda.device.scannable;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import gda.device.DeviceException;
import gda.device.scannable.ScannableUtils.ScannableValidationException;
import gda.oe.MoveableException;
import gda.oe.OE;
import static org.jscience.physics.units.SI.*;
import org.jscience.physics.quantities.Length;
import org.jscience.physics.quantities.Quantity;
import org.jscience.physics.units.Unit;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
public class DOFAdapterTest {


	private DOFAdapter da;
	private OE oe;

	@SuppressWarnings("unchecked")
	@Before
	public void setUp() throws ScannableValidationException, MoveableException {
		oe = mock(OE.class);
		when(oe.getUpperGdaLimits("dof")).thenReturn(new double[]{10.});
		when(oe.getLowerGdaLimits("dof")).thenReturn(new double[]{-10.});
		when(oe.getTolerance("dof")).thenReturn(new double[]{.1});
		when(oe.getSoftLimitLower("dof")).thenReturn(Quantity.valueOf(-11, METER));
		when(oe.getSoftLimitUpper("dof")).thenReturn(Quantity.valueOf(11, METER));
		when((Unit<Length>)oe.getReportingUnits("dof")).thenReturn(METER);
		when(oe.getPosition("dof", METER)).thenReturn(Quantity.valueOf(1, METER));

		
		da = new DOFAdapter(oe, "dof");
	}

	@Test
	public void testCheckPositionWhenValid() {
		assertEquals(null, da.checkPositionValid(0));
	}
	
	@Test
	public void testCheckPositionWhenOutsideScannableLimits() {
		assertEquals("Scannable limit violation on dof.dof: 11 > 10.0 (internal/hardware/dial values).", da.checkPositionValid(11));
	}
	
	@Test
	public void testCheckPositionWhenOutsideSoftLimits() {
		assertEquals("target position (21.0000000000000 ) is outside of DOF/EPICs limits (-11.0000000000000 m, 11.0000000000000 m )", da.checkPositionValid(21));
	}

	@Test
	public void testAsynchronousMoveToDouble() throws DeviceException, MoveableException {
		da.asynchronousMoveTo(1.);
		verify(oe).moveTo("dof", Quantity.valueOf(1, METER));
	}
	
	@Test
	public void testAsynchronousMoveToQuantity() throws DeviceException, MoveableException {
		da.asynchronousMoveTo(Quantity.valueOf(1, MILLI(METER)));
		verify(oe).moveTo("dof", Quantity.valueOf(1, MILLI(METER)));
	}
	
	@Test
	public void testAsynchronousMoveToQuantityStringMm() throws DeviceException, MoveableException {
		da.asynchronousMoveTo("1 mm");
		verify(oe).moveTo("dof", Quantity.valueOf(1, MILLI(METER)));
	}
	
	@Ignore
	@Test
	public void testAsynchronousMoveToQuantityStringUm() throws DeviceException, MoveableException {
		da.asynchronousMoveTo("1 um");
		verify(oe).moveTo("dof", Quantity.valueOf(1, MICRO(METER)));
	}
	
	@Test
	public void testAsynchronousMoveToQuantityStringM() throws DeviceException, MoveableException {
		da.asynchronousMoveTo("1 m");
		verify(oe).moveTo("dof", Quantity.valueOf(1, METER));
	}
	
	
	@Test
	public void testAsynchronousMoveToQuantityString1() throws DeviceException, MoveableException {
		da.asynchronousMoveTo("1mm");
		verify(oe).moveTo("dof", Quantity.valueOf(1, MILLI(METER)));
	}
	
	@Test
	public void testAsynchronousMoveToQuantityString2() throws DeviceException, MoveableException {
		da.asynchronousMoveTo("1 mm ");
		verify(oe).moveTo("dof", Quantity.valueOf(1, MILLI(METER)));
	}
	
	
	@Ignore
	@Test
	public void testAsynchronousMoveToQuantityString3() throws DeviceException, MoveableException {
		da.asynchronousMoveTo(" 1 mm ");
		verify(oe).moveTo("dof", Quantity.valueOf(1, MILLI(METER)));
	}

	@Test
	public void testGetPosition() throws MoveableException {
		when(oe.getPosition("dof", METER)).thenReturn(Quantity.valueOf(1, METER));
		assertEquals(1., da.getPosition());
		//da.setReportingUnits(MILLI(METER)); Doesn't work as expected
		//assertEquals(1000., da.getPosition());
	}

}
