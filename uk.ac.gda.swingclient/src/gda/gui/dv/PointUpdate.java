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

package gda.gui.dv;

import gda.analysis.plotmanager.IPlotWindow;
import gda.gui.dv.panels.DataSetPlot;

/**
 * PointUpdate Class
 */
public class PointUpdate implements Runnable {

	private IPlotWindow a = null;
	private double[] xdata = null;
	private double[] ydata = null;
	private int[] tags = null;

	/**
	 * @param perant
	 * @param xValues
	 * @param yValues
	 * @param yTags
	 */
	public void init(IPlotWindow perant, double[] xValues, double[] yValues, int[] yTags) {

		a = perant;

		tags = new int[yTags.length];
		xdata = new double[yTags.length];
		ydata = new double[yTags.length];
		for (int i = 0; i < tags.length; i++) {
			tags[i] = yTags[i];
			xdata[i] = xValues[i];
			ydata[i] = yValues[i];
		}
	}

	@Override
	public void run() {
		System.out.println("Beggining the GUI point update on " + Thread.currentThread());

		DataSetPlot dataSetPlot = a.getGraphDisplay();

		dataSetPlot.setBatching(true);

		for (int j = 0; j < tags.length; j++) {

			int lineNumber = a.getTag(tags[j]);

			dataSetPlot.addPointToLine(lineNumber, xdata[j], ydata[j]);

			if (dataSetPlot.getXAxisMax() < xdata[j]) {
				dataSetPlot.setXAxisLimits(dataSetPlot.getXAxisMin(), xdata[j]);
			}
			if (dataSetPlot.getXAxisMin() > xdata[j]) {
				dataSetPlot.setXAxisLimits(xdata[j], dataSetPlot.getXAxisMax());
			}
			if (dataSetPlot.getYAxisMax() < ydata[j]) {
				dataSetPlot.setYAxisLimits(dataSetPlot.getYAxisMin(), ydata[j]);
			}
			if (dataSetPlot.getYAxisMin() > ydata[j]) {
				dataSetPlot.setYAxisLimits(ydata[j], dataSetPlot.getYAxisMax());
			}
		}

		dataSetPlot.setBatching(false);
		dataSetPlot.invalidate();
	}
}
