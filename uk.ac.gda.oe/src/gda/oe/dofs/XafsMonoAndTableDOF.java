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

import gda.factory.FactoryException;
import gda.function.Function;
import gda.function.IdentityFunction;
import gda.function.SimpleTrigFunction;

import org.jscience.physics.quantities.Quantity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A highly specific form of CoupledDOF. It may be possible to assimilate this into CoupledDOF with a new kind of
 * function in the future.
 */
public class XafsMonoAndTableDOF extends CoupledDOF {
	private static final Logger logger = LoggerFactory.getLogger(XafsMonoAndTableDOF.class);

	private double crystalSpacing = 0.0;

	/**
	 * @see gda.factory.Configurable#configure()
	 */
	@Override
	public void configure() throws FactoryException {
		// Make a fixed list of functions and specify them as
		// they would normally be by XML. Then call the super
		// configure method.
		IdentityFunction one = new IdentityFunction();
		SimpleTrigFunction two = new SimpleTrigFunction();
		two.setInnerConstant("1.0");
		double value = crystalSpacing * 2.0;
		two.setOuterConstant("" + value + "mm");
		two.setTrigFunc("cos");
		addFunction(one);
		addFunction(two);

		// Should check at this point that the Moveables are of the correct
		// type.
		super.configure();
	}

	/**
	 * @return crystalSpacing
	 */
	public double getCrystalSpacing() {
		return crystalSpacing;
	}

	/**
	 * @param crystalSpacing
	 */
	public void setCrystalSpacing(double crystalSpacing) {
		this.crystalSpacing = crystalSpacing;
	}

	@Override
	protected Quantity[] calculateMoveables(Quantity fromQuantity) {
		Function[] functions = getFunctions();
		Quantity rtrn[] = new Quantity[moveables.length];

		Quantity notionalTableAtStart = functions[1].evaluate(moveables[0].getPosition());
		Quantity notionalTableAtEnd = functions[1].evaluate(fromQuantity);
		Quantity actualTableAtStart = moveables[1].getPosition();

		logger.debug("XafsMonoAndTableDOF calculateMoveables notional at start " + notionalTableAtStart);
		logger.debug("XafsMonoAndTableDOF calculateMoveables notional at end " + notionalTableAtEnd);
		logger.debug("XafsMonoAndTableDOF calculateMoveables actual at start " + actualTableAtStart);

		rtrn[0] = fromQuantity;
		rtrn[1] = actualTableAtStart.plus((notionalTableAtEnd.minus(notionalTableAtStart)));

		logger.debug("XafsMonoAndTableDOF calculateMoveables returning ");

		for (int i = 0; i < rtrn.length; i++) {
			logger.debug("     " + rtrn[i]);
		}

		return (rtrn);
	}
}
