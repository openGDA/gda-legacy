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

package gda.analysis;

import gda.analysis.functions.CompositeFunction;
import gda.analysis.functions.FunctionOutput;
import gda.analysis.functions.Gaussian;
import gda.analysis.functions.StraightLine;
import gda.analysis.utils.DatasetMaths;
import gda.analysis.utils.GeneticAlg;
import gda.analysis.utils.IOptimizer;
import gda.analysis.utils.NelderMead;
import uk.ac.diamond.scisoft.analysis.fitting.functions.AFunction;

import java.util.List;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class will basically be a set of static classes to allow anyone to use
 * the fitting algorithms, and to wrapper them all up nicely.
 */
public class Fitter {

	/**
	 * Setup the logging facilities
	 */
	private static final Logger logger = LoggerFactory.getLogger(Fitter.class);

	/**
	 * @param xAxis
	 * @param yAxis
	 * @return parameters
	 */
	@Deprecated
	public static Vector<Double> line(DataSet xAxis, DataSet yAxis) {
		logger.warn("The method fitter.line is deprecated, please use fitter.lineFit(xAxis, yAxis) instead.");
		return lineFit(xAxis, yAxis, false);
	}

	/**
	 * Basic function that takes a set of x values and a set of y values and
	 * Fits a Straight line through them, it returns a list containing the M and
	 * C parameters of the y=mx+c equation fitter tough them
	 * 
	 * @param xAxis
	 *            The dataset containing the X data
	 * @param yAxis
	 *            The dataset containing the Y data
	 * @return A list containing the M and C elements.
	 */
	public static Vector<Double> lineFit(DataSet xAxis, DataSet yAxis) {

		return lineFit(xAxis, yAxis, false);
	}

	/**
	 * Basic function that takes a set of x values and a set of y values and
	 * Fits a Straight line through them, it returns a list containing the M and
	 * C parameters of the y=mx+c equation fitter tough them It then plots the
	 * information to the DataVector Window.
	 * 
	 * @param xAxis
	 *            The dataset containing the X data
	 * @param yAxis
	 *            The dataset containing the Y data
	 * @return A list containing the M and C elements.
	 */
	public static Vector<Double> linePlot(DataSet xAxis, DataSet yAxis) {

		return lineFit(xAxis, yAxis, true);
	}

	private static Vector<Double> lineFit(DataSet xAxis, DataSet yAxis,
			boolean plotOut) {

		// At this stage use the GA to calculate the Angle as its the most
		// reliable fitter

		double span = 100.0;

		// first create the composite function
		CompositeFunction comp = new CompositeFunction();
		comp.addFunction(new StraightLine(-span, span, -span, span));

		GeneticAlg GA = new GeneticAlg(0.01);

		GA.Optimize(new DataSet[] {xAxis}, yAxis, comp);

		if (plotOut) {
			comp.display(xAxis, yAxis);
		}

		Vector<Double> result = new Vector<Double>();

		result.add(comp.getParameter(0).getValue());
		result.add(comp.getParameter(1).getValue());

		return result;
	}

	/**
	 * This function takes a pair of datasets and some other inputs, and then
	 * fits the function specified using the method specified.
	 * 
	 * @param xAxis
	 *            The DataSet containing all the x values of the data
	 * @param yAxis
	 *            The DataSet containing all the y values of the data
	 * @param Optimizer
	 *            The Optimiser which implements IOptimizer, which is to be used
	 * @param functions
	 *            A list of functions which inherit from AFunction which are
	 *            used to make up the function to be fit.
	 * @return A FunctionOutput object which records all the output parameters
	 * @throws Exception 
	 */
	public static FunctionOutput fit(DataSet xAxis, DataSet yAxis,
			IOptimizer Optimizer, AFunction... functions) throws Exception {
		return fit(xAxis, yAxis, false, Optimizer, functions);
	}

	/**
	 * This function takes a pair of datasets and some other inputs, and then
	 * fits the function specified using the method specified. It also plots the
	 * output in the DataVector window.
	 * 
	 * @param xAxis
	 *            The DataSet containing all the x values of the data
	 * @param yAxis
	 *            The DataSet containing all the y values of the data
	 * @param Optimizer
	 *            The Optimiser which implements IOptimizer, which is to be used
	 * @param functions
	 *            A list of functions which inherit from AFunction which are
	 *            used to make up the function to be fit.
	 * @return A FunctionOutput object which records all the output parameters
	 * @throws Exception 
	 */
	public static FunctionOutput plot(DataSet xAxis, DataSet yAxis,
			IOptimizer Optimizer, AFunction... functions) throws Exception {
		return fit(xAxis, yAxis, true, Optimizer, functions);
	}


	
	private static FunctionOutput fit(DataSet xAxis, DataSet yAxis,
			boolean plotOut, IOptimizer Optimizer, AFunction... functions) throws Exception {
		FitterDataSetFunctionFitterResult result = new DataSetFunctionFitter().fit(xAxis, yAxis, Optimizer, functions);
		if (plotOut) {
			Plotter.plot("Data Vector", xAxis,result.datasets);
		}
		
		return result.functionOutput;

	}
	
	/**
	 * @param xAxis
	 * @param yAxis
	 * @param peakEstimate
	 * @return function output
	 * @throws Exception
	 */
	public static FunctionOutput fitLocal1DGaussian(DataSet xAxis, DataSet yAxis, double peakEstimate) throws Exception {
		double height = yAxis.range();
		List<Double> crossings = DatasetMaths.crossings(xAxis, yAxis, (yAxis.min()+(height)/2.0));
		double plus = crossings.get(0);
		double minus = crossings.get(0);
		
		for(int i = 1, imax = crossings.size(); i < imax; i++) {
			double val = crossings.get(i);
			if(val > peakEstimate) {
				if (val < plus) {
					plus = val; 
				}
			} else {
				if (val > minus) {
					minus = val;
				}
			}
		}
		
		double width = plus-minus;
		
		double[] params = {peakEstimate,width,height*width};
		
		return fit(xAxis, yAxis, new NelderMead(0.001), new Gaussian(params));
		
	}

}
