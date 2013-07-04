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

package gda.util;

import java.util.ArrayList;

import gda.function.Function;
import gda.function.IdentityFunction;
import gda.oe.dofs.CoupledDOF;
import gda.oe.dofs.DOF;
import gda.oe.dofs.SingleAxisLinearDOF;
import junit.framework.TestCase;

/**
 *
 */
public class CollectionSetterTests extends TestCase{
	/**
	 * 
	 */
	public void testDofMoveableNames() {
		DOF dof = new SingleAxisLinearDOF();
		assertEquals(0, dof.getMoveableNames().size());
		dof.setMoveableNames(arrayListOf("one", "two", "three"));
		assertEquals(3, dof.getMoveableNames().size());
	}
	
	/**
	 * 
	 */
	public void testDofAcceptableUnits() {
		DOF dof = new SingleAxisLinearDOF();
		assertEquals(0, dof.getAcceptableUnits().size());
		dof.setAcceptableUnitNames(arrayListOf("kg", "m"));
		assertEquals(2, dof.getAcceptableUnits().size());
	}
	
	/**
	 * 
	 */
	public void testCoupledDofFunctions() {
		CoupledDOF cd = new CoupledDOF();
		assertEquals(0, cd.getFunctionList().size());
		Function f = new IdentityFunction();
		cd.setFunctionList(arrayListOf(f));
		assertEquals(1, cd.getFunctionList().size());
	}
	
	private <T> ArrayList<T> arrayListOf(T...items) {
		ArrayList<T> list = new ArrayList<T>();
		for (T i : items) {
			list.add(i);
		}
		return list;
	}
}
