/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council
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

package gda.analysis;

import gda.analysis.functions.Gaussian;
import gda.analysis.functions.Offset;
import gda.analysis.utils.GeneticAlg;
import uk.ac.diamond.scisoft.analysis.fitting.functions.AFunction;

import org.junit.Test;

/**
 *
 */
public class FitterTest {
	static final double max = 1e3;

	/**
	 * 
	 */
	@Test
	public void testFitter() {
		Gaussian gaussian = new Gaussian(-3., 3., 3., 2);
		gaussian.getParameter(2).setLowerLimit(0);
		DataSet dsy = DataSet.zeros(12);
		DataSet dsyaxis = dsy.getIndexDataSet();
		
		try {
			Fitter.fit( dsyaxis, dsy, new GeneticAlg(.001),
					new AFunction[] { gaussian, new Offset( dsy.min(),dsy.max() ) } );
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
