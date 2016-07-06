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

import org.eclipse.january.dataset.DoubleDataset;

import gda.analysis.plotmanager.IPlotWindow;
import gda.configuration.properties.LocalProperties;
import gda.gui.dv.panels.DataSetPlot;
import gda.plots.Marker;
import gda.plots.SimplePlot;
import gda.plots.SimpleXYSeries;
import gda.plots.Type;

/**
 * GraphUpdate Class
 */
public class GraphUpdate implements Runnable {

	private IPlotWindow a = null;
	private DoubleDataset xdata = null;
	private DoubleDataset[] ydata = null;
	boolean clearFirst = true;
	private int[] tags = null;

	/**
	 * @param perant
	 * @param xValues
	 * @param yValues
	 * @param clear
	 */
	public void init(IPlotWindow perant, DoubleDataset xValues, DoubleDataset yValues, boolean clear) {
		a = perant;
		xdata = xValues;
		ydata = new DoubleDataset[1];
		ydata[0] = yValues;
		clearFirst = clear;
	}

	/**
	 * @param perant
	 * @param clear
	 * @param xValues
	 * @param dataSets
	 */
	public void init(IPlotWindow perant, boolean clear, DoubleDataset xValues, DoubleDataset... dataSets) {
		a = perant;
		xdata = xValues;
		ydata = new DoubleDataset[dataSets.length];

		for (int i = 0; i < ydata.length; i++) {
			ydata[i] = dataSets[i];
		}

		clearFirst = clear;
	}

	/**
	 * @param perant
	 * @param clear
	 * @param xValues
	 * @param yTags
	 * @param dataSets
	 */
	public void init(IPlotWindow perant, boolean clear, DoubleDataset xValues, int[] yTags, DoubleDataset... dataSets) {
		a = perant;
		xdata = xValues;
		ydata = new DoubleDataset[dataSets.length];

		for (int i = 0; i < ydata.length; i++) {
			ydata[i] = dataSets[i];
		}

		tags = new int[yTags.length];
		for (int i = 0; i < tags.length; i++) {
			tags[i] = yTags[i];
		}

		clearFirst = clear;
	}

	@Override
	public void run() {

		// logger.info("Beginning the GI update on " + Thread.currentThread());
		DataSetPlot dataSetPlot = a.getGraphDisplay();
		if (dataSetPlot.isFreezePlot()) {
			return;
		}
		a.setGraphDisplay(clearFirst, xdata.getName());
		dataSetPlot.setBatching(true);
		if (ydata.length > 0) {
			if (xdata.getSize() > LocalProperties.getInt("gda.gui.dv.GraphUpdate.turboModeSwitchLevel", 1000))
				dataSetPlot.setTurboMode(true);
		}

		for (int j = 0; j < ydata.length; j++) {
			final int lineNumber = dataSetPlot.getNextAvailableLine();
			
			// add the line number into the tagging system if its needed.
			if (tags != null) {
				a.setTag(tags[j], lineNumber);
			}

			SimpleXYSeries xySeries = new SimpleXYSeries(ydata[j].getName(), lineNumber, SimplePlot.LEFTYAXIS, xdata
					.getData(), ydata[j].getData());
			xySeries.setType(Type.LINEANDPOINTS);
			xySeries.setMarker(Marker.fromCounter(j));

			dataSetPlot.initializeLine(xySeries);
			dataSetPlot.setYAxisLabel("y-axis");
			dataSetPlot.setXAxisLabel("x-axis");

			if ((clearFirst) && (j == 0)) {
				dataSetPlot.setXAxisLimits(xdata.min().doubleValue(), xdata.max().doubleValue());
				dataSetPlot.setYAxisLimits(ydata[j].min().doubleValue(), ydata[j].max().doubleValue());
			}

			if (dataSetPlot.getXAxisMax() < xdata.max().doubleValue()) {
				dataSetPlot.setXAxisLimits(dataSetPlot.getXAxisMin(), xdata.max().doubleValue());
			}
			if (dataSetPlot.getXAxisMin() > xdata.min().doubleValue()) {
				dataSetPlot.setXAxisLimits(xdata.min().doubleValue(), dataSetPlot.getXAxisMax());
			}
			if (dataSetPlot.getYAxisMax() < ydata[j].max().doubleValue()) {
				dataSetPlot.setYAxisLimits(dataSetPlot.getYAxisMin(), ydata[j].max().doubleValue());
			}
			if (dataSetPlot.getYAxisMin() > ydata[j].min().doubleValue()) {
				dataSetPlot.setYAxisLimits(ydata[j].min().doubleValue(), dataSetPlot.getYAxisMax());
			}
		}

		dataSetPlot.setBatching(false);

		dataSetPlot.invalidate();
	}
}