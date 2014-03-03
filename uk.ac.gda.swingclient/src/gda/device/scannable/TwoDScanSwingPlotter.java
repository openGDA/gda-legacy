/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

import gda.analysis.Plotter;
import gda.device.DeviceException;
import gda.jython.IAllScanDataPointsObserver;
import gda.jython.IScanDataPointProvider;
import gda.jython.InterfaceProvider;
import gda.scan.ScanDataPoint;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.SDAPlotter;
import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;

/**
 * Plots a 2D graph of the current scan into an RCP plot window as the scan progresses.
 * <p>
 * To use this scannable, give it the names of the x,y and z columns and the plot view to send the plot to ("Plot 1" by
 * default. Then simply include in the scan command you wish to plot.
 */
public class TwoDScanSwingPlotter extends TwoDScanPlotter {

	private static final Logger logger = LoggerFactory.getLogger(TwoDScanSwingPlotter.class);

	private String swingPlotViewName = null;

	@Override
	public void plot() throws Exception {
		super.plot();
		
		if (getSwingPlotViewName() != null) {
			logger.debug("Plotting to Swing client plot named:" + getSwingPlotViewName());
			Plotter.plotImage(getSwingPlotViewName(), intensity);
//			SDAPlotter.imagePlot(plotViewname, x, y, intensity);
//			Plotter.plotImage(getSwingPlotViewName(), x, y, intensity);
			
		} else {
			logger.warn("No swingPlotViewName is set (set one or just use TwoDScanPlotter)");
		}
	}

	public String getSwingPlotViewName() {
		return swingPlotViewName;
	}

	public void setSwingPlotViewName(String swingPlotViewName) {
		this.swingPlotViewName = swingPlotViewName;
	}

}
