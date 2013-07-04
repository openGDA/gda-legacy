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

package gda.plots;

import gda.plots.SimplePlot;

import javax.swing.JFrame;

import junit.framework.TestCase;

/**
 * Tests for SimplePlot. When run as a JUnit test no plots actually appear so only testing for errors and that bugs are
 * fixed. Run the main program and observe for a real test.
 */
public class SimplePlotTest extends TestCase {
	private int dataSize = 1000;

	private JFrame jf = null;

	private boolean display = false;

	long sleepTime = 1;

	/**
	 * Constructor.
	 * 
	 * @param arg0
	 */
	public SimplePlotTest(String arg0) {
		super(arg0);
	}

	/**
	 * @param display
	 */
	public void setDisplay(boolean display) {
		this.display = display;
		if (display == true)
			sleepTime = 2000;
	}

	private void actuallyDisplay(SimplePlot sp) {
		if (display) {
			jf = new JFrame();
			jf.getContentPane().add(sp);
			jf.pack();
			jf.setVisible(true);
		}
	}

	/**
	 * Dispose.
	 */
	public void actuallyDispose() {
		if (jf != null) {
			jf.dispose();
			jf = null;
		}
	}

	/**
	 * Sets the points individually.
	 * 
	 * @param fixedAxes
	 */
	public void setPointsIndividually(boolean fixedAxes) {
		SimplePlot sp = new SimplePlot();
		actuallyDisplay(sp);

		if (fixedAxes) {
			sp.setXAxisLimits(0, dataSize);
			sp.setYAxisLimits(0, 1.0);
		}
		sp.initializeLine(0);
		sp.setTitle("setPointsIndividually fixedAxes = " + fixedAxes + " dataSize = " + dataSize);
		for (int i = 0; i < dataSize; i++) {
			sp.addPointToLine(0, i, Math.random());

		}
		try {
			Thread.sleep(sleepTime);
		} catch (InterruptedException e) {
			// Deliberately do nothing
		}
		actuallyDispose();
	}

	/**
	 * Sets all the points together.
	 * 
	 * @param fixedAxes
	 */
	public void setPointsAllAtOnce(boolean fixedAxes) {
		double[] xVals = new double[dataSize];
		double[] yVals = new double[dataSize];

		for (int i = 0; i < dataSize; i++) {
			xVals[i] = i;
			// xVals[i] = Math.random() * dataSize;
			yVals[i] = Math.random();
		}

		SimplePlot sp = new SimplePlot();
		actuallyDisplay(sp);
		if (fixedAxes) {
			sp.setXAxisLimits(0, dataSize);
			sp.setYAxisLimits(0, 1.0);
		}
		sp.initializeLine(0);
		sp.setTitle("setPointsAllAtOnce fixedAxes = " + fixedAxes + " dataSize = " + dataSize);
		sp.setLinePoints(0, xVals, yVals);
		try {
			Thread.sleep(sleepTime);
		} catch (InterruptedException e) {
			// Deliberately do nothing
		}
		actuallyDispose();
	}

	/**
	 * Set the data size.
	 * 
	 * @param dataSize
	 */
	public void setDataSize(int dataSize) {
		this.dataSize = dataSize;
	}

	/**
	 * test basic operation.
	 */
	public void testBasicOperation() {
		SimplePlot sp = new SimplePlot();
		actuallyDisplay(sp);

		try {
			sp.initializeLine(0);
			sp.addPointToLine(0, 1.0, 2.0);
			sp.addPointToLine(0, 2.0, 3.0);
			sp.setTitle("initialized line 0");
			Thread.sleep(sleepTime);

			sp.setLineColor(0, "blue");
			sp.setTitle("line 0 colour to blue");
			Thread.sleep(sleepTime);

			sp.setLineType(0, "lineandpoints");
			sp.setTitle("line 0 type to lineandpoints");
			Thread.sleep(sleepTime);

			sp.setLineMarkerColor(0, "yellow");
			sp.setTitle("line 0 marker colour to yellow");
			Thread.sleep(sleepTime);

			sp.setLinePattern(0, "Dotted");
			sp.setTitle("line 0 style to dotted");
			Thread.sleep(sleepTime);

			sp.setLineMarker(0, "Square");
			sp.setTitle("line 0 marker to square");
			Thread.sleep(sleepTime);

			sp.setLineName(0, "ethelred");
			sp.setTitle("line 0 name to ethelred");
			Thread.sleep(sleepTime);

			sp.initializeLine(1);
			sp.addPointToLine(1, 1.0, 1.0);
			sp.addPointToLine(1, 2.0, 2.0);
			sp.setTitle("initializedline 1");
			Thread.sleep(sleepTime);

			sp.setLineVisibility(0, false);
			sp.setTitle("line 0 invisible");
			Thread.sleep(sleepTime);

			sp.setLineVisibility(0, true);
			sp.setTitle("line 0 visible again");
			Thread.sleep(sleepTime);

			sp.deleteLine(0);
			sp.setTitle("deleted line 0");
			Thread.sleep(sleepTime);

			sp.initializeLine(0);
			sp.addPointToLine(0, 1.0, 0.5);
			sp.addPointToLine(0, 2.0, 1.5);
			sp.setTitle("re-initialized line 0");
			Thread.sleep(sleepTime);

			sp.addYAxisTwo();
			sp.setTitle("added second y axis");
			Thread.sleep(sleepTime);

			sp.initializeLine(2, 1);
			sp.addPointToLine(2, 1.0, 100.0);
			sp.addPointToLine(2, 2.0, 101.0);
			sp.setTitle("initialized line 2 on right y axis");
			Thread.sleep(sleepTime);
			Thread.sleep(sleepTime);

			actuallyDispose();
		} catch (InterruptedException e) {
			// Deliberately do nothing
		}
	}

	/**
	 * Test for Bug 46.
	 */
	public void testForBug46() {
		setDataSize(1000);
		setPointsIndividually(false);
		setDataSize(10000);
		setPointsIndividually(true);
		setDataSize(1000);
		setPointsAllAtOnce(false);
		setDataSize(10000);
		setPointsAllAtOnce(true);
	}

	/**
	 * Bar Chart Test.
	 */
	public void testBarChart() {
		setDataSize(20);
		double[] xVals = new double[dataSize];
		double[] yVals = new double[dataSize];

		for (int i = 0; i < dataSize; i++) {
			xVals[i] = i;
			yVals[i] = Math.random();
		}

		SimplePlot sp = new SimplePlot(SimplePlot.BARCHART);

		actuallyDisplay(sp);

		sp.initializeLine(0);
		sp.setTitle("testBarChart 0 of 11");
		sp.setLinePoints(0, xVals, yVals);
		for (int i = 0; i < 10; i++) {
			try {
				Thread.sleep(sleepTime);
				for (int j = 0; j < dataSize; j++) {
					yVals[j] = Math.random();
				}
				sp.setTitle("testBarChart " + (i + 1) + " of 11");
				sp.setLinePoints(0, xVals, yVals);
			} catch (InterruptedException e) {
				// Deliberately do nothing
			}
		}
		actuallyDispose();
	}

	/**
	 * Test Main Method.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		SimplePlotTest spt = new SimplePlotTest("hello");

		spt.setDisplay(true);
		spt.testBasicOperation();
		spt.testForBug46();
		spt.testBarChart();
	}
}