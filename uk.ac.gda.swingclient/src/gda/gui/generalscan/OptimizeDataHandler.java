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

package gda.gui.generalscan;

import gda.jython.JythonServerStatus;
import gda.plots.SimplePlot;
import gda.scan.ScanDataPoint;

import java.util.Iterator;

import org.jscience.physics.quantities.Quantity;
import org.jscience.physics.units.Unit;

/**
 * OptimizeDataHandler Class
 */
public class OptimizeDataHandler extends GeneralDataHandler {
	private SimplePlot simplePlot = getSimplePlot();

	private boolean[] lineList;

	@Override
	public void configure() {
		createLineNamesList();
		configured = true;
	}

	protected void setLineArray() {
		lineList = new boolean[8];
		lineList[0] = true;
		lineList[1] = true;
	}

	@Override
	protected void configurePlot() {
		// Deliberately empty, overriding. DON'T REMOVE.
	}

	protected void configurePlot(String name) {
		simplePlot.setTitle("Optimization of " + name);
		simplePlot.setXAxisLabel("DOF Position");
	}

	@Override
	public void setScanParameters(String scannedObjName, Unit<? extends Quantity> scanUnits) {
		// Deliberately empty, overriding. DON'T REMOVE.
	}

	/**
	 * @param line
	 * @return peak position
	 */
	public double getPeakPosition(String line) {
		return simplePlot.getLineXValueOfPeak(line);
	}

	@Override
	public void update(Object newData) {
		String scannedObjName = null;

		if (newData instanceof JythonServerStatus) {
			setStatus((JythonServerStatus) newData);
		} else if (newData instanceof ScanDataPoint) {
			// recast the source of this update to a ScanBase
			ScanDataPoint point = (ScanDataPoint) newData;
			scannedObjName = point.getScannableNames().get(0);
			if (!scannedObjName.equals("Time from start")) {
				setScanParameters(scannedObjName, scanUnits);
			}

			// get the position of the scannable that was just scanned
			double xVal = point.getPositionsAsDoubles()[0];

			if (firstPoint) {
				firstPoint = false;
				if (scanUnits != null)
					simplePlot.setXAxisLabel(scanUnits.toString());
				initializeLines();
				setWhatToPlot(lineList);
			}

			// loop though all the detectors
			int start = 0;
			Iterator<Object> i = point.getDetectorData().iterator();
			while (i.hasNext()) {
				Object obj = i.next();
				// NB GeneralDataHandler only works when all the Detectors are
				// CounterTimers so the data will be a double[]
				double[] yVal = (double[]) obj;
				addData(xVal, yVal, start);
				start += yVal.length;
			}

			scanTimer.increment();
		}
	}
}
