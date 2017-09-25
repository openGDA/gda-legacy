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

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;

import org.jscience.physics.quantities.Quantity;
import org.jscience.physics.units.Unit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DataLogger;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.factory.Finder;
import gda.jython.Jython;
import gda.jython.JythonServerStatus;
import gda.plots.SimplePlot;
import gda.scan.ScanDataPoint;

/**
 * Deals with plotting of data and timing during general scans. NB Assumes that all Detector objects implement
 * CounterTimer.
 */
public class GeneralDataHandler extends JPanel implements WhatToPlotDialogUser, NormalizeDialogUser {

	private static final Logger logger = LoggerFactory.getLogger(GeneralDataHandler.class);

	private static final int NO_NORMALIZATION = -1;

	protected int whichToNormalizeTo = NO_NORMALIZATION;

	private SimplePlot simplePlot;

	protected SpecialProgressBar scanTimer;

	protected SpecialProgressBar repeatCounter;

	protected boolean configured = false;

	protected boolean firstPoint = false;

	protected Unit<? extends Quantity> scanUnits;

	private String scannedObjName = "";

	private JMenuItem normalizeButton;

	private JMenuItem selectAndMoveButton;

	private JMenuItem whatToPlotButton;

	protected WhatToPlotDialog whatToPlotDialog = null;

	private NormalizeDialog normalizeDialog = null;

	private SelectAndMoveDialog selectAndMoveDialog = null;

	private ArrayList<String> lineNames;

	protected int scanStatus = Jython.IDLE;

	protected int numberOfPoints;

	protected int numberOfRepeats;

	protected int repeatsDone;

	private JPanel scanTimerPanel;

	protected boolean hasWhatToPlot = false;

	protected boolean hasSelectAndMove = false;

	private boolean hasNormalize = false;

	private JButton clearButton;

	private JCheckBox clearCBox;
	protected boolean clearGraphForNewScan = false;
	protected int scriptStatus = Jython.IDLE;

	/**
	 * @param hasWhatToPlot
	 * @param hasSelectAndMove
	 * @param hasNormalize
	 */
	public GeneralDataHandler(boolean hasWhatToPlot, boolean hasSelectAndMove, boolean hasNormalize) {
		this.hasWhatToPlot = hasWhatToPlot;
		this.hasSelectAndMove = hasSelectAndMove;
		this.hasNormalize = hasNormalize;

		setLayout(new BorderLayout());

		scanTimerPanel = createScanTimerPanel();
		add(scanTimerPanel, BorderLayout.NORTH);

		// Create plot
		simplePlot = new SimplePlot();

		add(simplePlot, BorderLayout.CENTER);

		// create clear button and clear checkbox
		clearButton = new JButton("Clear");
		clearButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// to make sure that a clear of the graph is not done during a scan or a script
				if (scanStatus == Jython.IDLE && (scriptStatus == Jython.IDLE || repeatsDone == numberOfRepeats))
					clearGraph();

			}

		});
		clearCBox = new JCheckBox("New scans clear graph");
		clearCBox.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (clearCBox.isSelected()) {
					clearGraphForNewScan = true;
				} else {
					clearGraphForNewScan = false;
				}

			}

		});
		JPanel clearPanel = new JPanel();
		clearPanel.add(clearButton);
		clearPanel.add(clearCBox);
		add(clearPanel, BorderLayout.SOUTH);
	}

	protected void clearGraph() {

		if (scanStatus == Jython.IDLE)// && repeatsDone == (numberOfRepeats))
		{
			simplePlot.deleteAllLines();
			simplePlot.setTitle("title");
			simplePlot.setYAxisLabel("yaxis");
			simplePlot.setXAxisLabel("xaxis");
			getSimplePlot().setXAxisAutoScaling(true);
			getSimplePlot().setYAxisAutoScaling(true);
			repeatCounter.clear();
			scanTimer.clear();
		}

	}

	/**
	 * Constructor - the no arguments constructor builds a panel with WhatToPlot and SelectAndMove but without
	 * Normalize.
	 */
	public GeneralDataHandler() {
		this(true, true, false);
	}

	/**
	 * Configures. Subclasses will probably want to override the createLineNamesList and configurePlot methods for their
	 * own purposes. NB. GeneralDataHandler is not Configurable, this method exists only for historical reasons.
	 */
	protected void configure() {
		createLineNamesList();

		// Create supplementary dialogs
		if (hasWhatToPlot) {
			whatToPlotDialog = new WhatToPlotDialog(this, lineNames);
		}
		if (hasSelectAndMove) {
			selectAndMoveDialog = new SelectAndMoveDialog(simplePlot, lineNames);
		}
		if (hasNormalize) {
			normalizeDialog = new NormalizeDialog(this, lineNames);
		}

		// Add items to control supplementary dialogs to plot popup menu
		createPlotMenuItems();

		configurePlot();
		configured = true;
	}

	/**
	 * Initializes for a scan or set of scans.
	 *
	 * @param numberOfPoints
	 *            the number of points in the scan
	 * @param numberOfRepeats
	 *            the number of times the scan will be repeated
	 */
	public void init(int numberOfPoints, int numberOfRepeats) {
		if (!configured)
			configure();

		this.numberOfRepeats = numberOfRepeats;
		this.numberOfPoints = numberOfPoints;
		firstPoint = true;
		repeatsDone = 0;
		scanTimer.init(numberOfPoints);
		scanTimer.increment();
		repeatCounter.init(numberOfRepeats);
		repeatCounter.increment();
	}

	/**
	 * Implements the WhatToPlotDialogUser interface, the WhatToPlotDialog will call this when a button setting changes.
	 * Overrides the GeneralDataHandler method in order to set axis labels correctly.
	 *
	 * @param isSelected
	 *            new array of button settings.
	 */
	@Override
	public void setWhatToPlot(boolean[] isSelected) {
		for (int i = 0; i < lineNames.size(); i++) {
			simplePlot.setLineVisibility(i, isSelected[i]);
		}
	}

	private JPanel createScanTimerPanel() {
		JPanel jPanel = new JPanel(new GridLayout(1, 0));
		repeatCounter = new SpecialProgressBar();
		repeatCounter.setBeingCounted("scan");
		jPanel.add(repeatCounter);
		scanTimer = new SpecialProgressBar();
		scanTimer.setBeingCounted("point");
		jPanel.add(scanTimer);
		jPanel.setBorder(BorderFactory.createEtchedBorder());
		return jPanel;
	}

	/**
	 * Adds new data to the lines.
	 *
	 * @param xVal
	 *            the x xvalue
	 * @param yVals
	 *            array of y values
	 * @param start
	 */
	// FIXME: this method is public because it is used by
	// MicroFocusXafsPanel
	// sort this out
	public void addData(double xVal, double[] yVals, int start) {
		double yVal = 0;

		for (int j = 0; j < yVals.length; j++) {
			int i = j + start;
			if (whichToNormalizeTo != NO_NORMALIZATION) {
				if (yVals[whichToNormalizeTo] != 0) {
					yVal = yVals[j] / yVals[whichToNormalizeTo];
				} else {
					yVal = 0;
				}
			} else {
				yVal = yVals[j];
			}
			simplePlot.addPointToLine(i, xVal, yVal);
		}
	}

	/**
	 * Create plot menu items - subclasses may want to override this.
	 */

	protected void createPlotMenuItems() {
		if (whatToPlotDialog != null || selectAndMoveDialog != null || normalizeDialog != null) {
			simplePlot.addPopupMenuItem(new JSeparator());
		}

		if (whatToPlotDialog != null) {
			whatToPlotButton = new JMenuItem("Plot shows...");
			whatToPlotButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					whatToPlotDialog.setVisible(true);
				}
			});
			simplePlot.addPopupMenuItem(whatToPlotButton);
		}

		if (selectAndMoveDialog != null) {
			selectAndMoveButton = new JMenuItem("SelectPointAndMove...");
			selectAndMoveButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					selectAndMoveButton.setEnabled(false);
					selectAndMoveDialog.setVisible(true);
				}

			});
			simplePlot.addPopupMenuItem(selectAndMoveButton);
		}

		if (normalizeDialog != null) {
			normalizeButton = new JMenuItem("Normalize...");
			normalizeButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					normalizeDialog.setVisible(true);
				}
			});
			simplePlot.addPopupMenuItem(normalizeButton);
		}

	}

	/**
	 * Sets the starting values for plot axis labels etc. and which line are visible. Subclasses will probably want to
	 * override this.
	 */
	protected void configurePlot() {
		simplePlot.setTitle(" ");
		simplePlot.setXAxisLabel("xaxis");
		simplePlot.setTrackPointer(true);
		simplePlot.setYAxisLabel("Counts");
		simplePlot.setXAxisAutoScaling(true);

		for (int i = 0; i < lineNames.size(); i++)
			whatToPlotDialog.setSelected(i, true);
	}

	/**
	 * NB This method is called directly by GeneralScanPanel and not via an IObserver/IObservable mechanism.
	 *
	 * @param newData
	 */
	public void update(Object newData) {
		if (newData instanceof JythonServerStatus) {
			setStatus((JythonServerStatus) newData);
		} else if (newData instanceof ScanDataPoint) {
			// recast the source of this update to a ScanBase
			ScanDataPoint point = (ScanDataPoint) newData;
			scannedObjName = point.getScannableNames().get(0);
			if (!scannedObjName.equals("Time from start")) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						setScanParameters(scannedObjName, scanUnits);
					}
				});

			}

			// get the position of the scannable that was just scanned
			double xVal = point.getPositionsAsDoubles()[0];

			if (firstPoint) {
				firstPoint = false;
				simplePlot.setTitle(point.getCurrentFilename());
				if (scanUnits != null)
					simplePlot.setXAxisLabel(scanUnits.toString());
				initializeLines();
				setWhatToPlot(whatToPlotDialog.getSelected());
			}

			logger.info("ScanDataPoint received with detectors: ");
			for (String name : point.getDetectorNames())
				logger.info(name);

			if (point.getDetectorData() != null) {
				// loop though all the detectors
				int start = 0;
				Iterator<Object> i = point.getDetectorData().iterator();
				while (i.hasNext()) {
					Object obj = i.next();
					// GeneralDataHandler only works for Detectors which return
					// their data as double[] hence the catching of the run-time
					// ClassCastException.
					try {
						double[] yVal = (double[]) obj;
						addData(xVal, yVal, start);
						logger.debug("data " + i + " has length " + yVal.length);
						start += yVal.length;
					} catch (ClassCastException cce) {
						// This is a LOG because it is not really an error,
						// Detectors which do not return double[] will indeed
						// exist
						logger.warn("GeneralDataHandler found a detector which did not have double[] data");
					}
				}
			} else {
				logger.warn("GeneralDataHandler.update - point.data = null");
			}
			scanTimer.increment();
		}
	}

	/**
	 * Called when a JythonServerStatus arrives, used BFI to determine when a scan in a set of repeats has ended.
	 *
	 * @param newStatus
	 *            the new status
	 */
	protected void setStatus(JythonServerStatus newStatus) {
		// getting the script status as well to enable the clearing of the graph
		this.scriptStatus = newStatus.scriptStatus;
		if (newStatus.scanStatus != scanStatus) {
			// If the status is changing from RUNNING to IDLE then the scan
			// is
			// ending
			// so the repeatCounter is incremented and if there are more
			// repeats
			// firstPoint is set back to true.
			logger.debug("newStatus.scanStatus " + newStatus.scanStatus + " current scanStatus " + scanStatus);
			if (newStatus.scanStatus == Jython.IDLE && scanStatus == Jython.RUNNING) {
				repeatsDone++;
				if (repeatsDone < numberOfRepeats) {
					repeatCounter.increment();
					scanTimer.init(numberOfPoints);
					scanTimer.increment();
					firstPoint = true;
				} else {
					scanTimer.setDone();
					repeatCounter.setDone();
					// getSimplePlot().setBatching(false);
				}
			}

			scanStatus = newStatus.scanStatus;
			if (selectAndMoveButton != null) {
				selectAndMoveButton.setEnabled(scanStatus == Jython.IDLE && repeatsDone == numberOfRepeats);
			}
		}
	}

	/**
	 * @param scanUnits
	 */
	public void setScanUnits(Unit<? extends Quantity> scanUnits) {
		this.scanUnits = scanUnits;
	}

	protected void initializeLines() {
		for (int i = 0; i < lineNames.size(); i++) {
			simplePlot.initializeLine(i);
			simplePlot.setLineName(i, lineNames.get(i));
		}
	}

	/**
	 * Creates a list of line names to be used on the plot. This implementation just takes the names of the counter
	 * timer channels. Subclasses may want to override this.
	 */
	protected void createLineNamesList() {
		lineNames = new ArrayList<String>();

		try {
			// get the list of active detectors
			List<String> detectorNames = Finder.getInstance().listAllNames("Detector");
			for (String name : detectorNames) {
				Detector d = (Detector) Finder.getInstance().find(name);
				if (d instanceof DataLogger) {
					DataLogger dl = (DataLogger) d;
					logger.info("DataLogger " + dl.getName() + " has " + dl.getNoOfChannels() + " channels.");
					for (int i = 0; i < dl.getNoOfChannels(); i++)
						lineNames.add("channel " + i);
				} else {
					//now handle all detectors, not just countertimers
					for (int i = 0; i < d.getExtraNames().length; i++){
							lineNames.add(d.getExtraNames()[i]);
					}
				}

			}
		} catch (DeviceException de) {
			logger.error("GeneralDataHandler.setLineNames() caught exception" + de.getMessage());
		}

	}

	@Override
	public void setWhichToNormalizeTo(int whichToNormalizeTo) {
		this.whichToNormalizeTo = whichToNormalizeTo;
		logger.debug("whichToNormalizeTo is now " + whichToNormalizeTo);

	}

	/**
	 * Get the SimplePlot
	 *
	 * @return the SimplePlot
	 */
	public SimplePlot getSimplePlot() {
		return simplePlot;
	}

	/**
	 * @return the ArrayList of line names
	 */
	public ArrayList<String> getLineNames() {
		return lineNames;
	}

	/**
	 * Sets the ArrayList of line names
	 *
	 * @param lineNames
	 *            the new ArrayList
	 */
	public void setLineNames(ArrayList<String> lineNames) {
		this.lineNames = lineNames;
	}

	/**
	 * Gets and individual line name from the list.
	 *
	 * @param which
	 *            index of required line name
	 * @return the name
	 */
	public String getLineName(int which) {
		return lineNames.get(which);
	}

	/**
	 * Allows normalizeButton to be used from elsewhere since it has to done before scanning.
	 */
	public void remoteNormalize() {
		// Have to do the configure because it is normally done at scan
		// initialization time.
		if (!configured)
			configure();

		if (normalizeDialog != null) {
			normalizeDialog.setVisible(true);
		}
	}

	/**
	 * @param scannedObjName
	 * @param scanUnits
	 */
	public void setScanParameters(String scannedObjName, Unit<? extends Quantity> scanUnits) {
		this.scannedObjName = scannedObjName;
		if (selectAndMoveDialog != null) {
			selectAndMoveDialog.setScannedDOFName(scannedObjName);
			selectAndMoveDialog.setScanUnits(scanUnits);
		}
	}

	/**
	 * Returns the hasNormalize flag, needed to go with remoteNormalize but not really a satisfactory solution. NB I
	 * (PCS) refuse to allow this method to be called 'isHasNormalize' whatever the conventions.
	 *
	 * @return hasNormalize flag
	 */
	public boolean hasNormalize() {
		return hasNormalize;
	}
}
