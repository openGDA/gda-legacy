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

package gda.gui.polarimetry;

import gda.factory.Finder;
import gda.gui.generalscan.GeneralDataHandler;
import gda.gui.generalscan.GeneralScan;
import gda.gui.generalscan.GeneralScanPanel;
import gda.gui.generalscan.TimeScanPanel;
import gda.gui.xuv.DataPlotter;
import gda.jython.JythonServerFacade;
import gda.observable.IObserver;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.io.Serializable;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Panel for setting scans on the flexible polarimeter end station
 * 
 * README This class contains hard coded DOF names because it needs to know DOF
 * names for control or to acquire information from them. Currently no sensible
 * solution has been found to get round hard coding.
 */
public class PolarimeterScanPanel extends GeneralScanPanel implements
		IObserver, Runnable, Serializable {
	private static final Logger logger = LoggerFactory
			.getLogger(PolarimeterScanPanel.class);

	private DataPlotter dataPlotter;

	private String disAllow[][] = { { "Dummy" } };

	/**
	 * Creates a Polarimeter scan panel.
	 */
	public PolarimeterScanPanel() {

	}

	@Override
	public void configure() {
		try {
			Finder finder = Finder.getInstance();
			scriptingMediator = JythonServerFacade.getInstance();
			// NB Panels must be IObservers of the JythonServerFacade or they
			// never get updates about the status of the JythonServer, only
			// updates containing data while there is a scan running.
			scriptingMediator.addIObserver(this);
			oeNames = finder.listAllNames("OE");
			oeNames.add(GeneralScan.TIMESCAN);
			detectorNames = finder.listAllNames("Detector");
			createDataHandler();
			init();
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}

	protected void startTheScan() {
		pww.setVisible(true);

		repeatsToDo = 1;
		String command;
		ArrayList<String> header = createHeader();
		command = scanToDo.getCommand();

		// FIXME: this is not a good way to determine whether it is a time scan
		// indeed the whole appearance and handling of the TimeScan is
		// inadequate.
		boolean thisIsATimeScan = command.indexOf("TimeScan") != -1;

		if (!thisIsATimeScan) {
			if (generalScans[1] != null) {

				String secondBit = generalScans[1].getCommand();
				secondBit = secondBit.replaceAll("stepScan", "innerScan");
				secondBit = secondBit.replaceAll("timeScan", "innerScan");
				command = command.replaceAll("\\)\\);", ", innerScan));");
				command = secondBit + command;
			}

			if (scanCommandEditor != null)
				command = scanCommandEditor.editCommand(command);
			logger.debug("scanToDo {}", command);

			scriptingMediator.placeInJythonNamespace("scanheader", header);
			command += "stepScan.getDataWriter().setHeader(scanheader);";
			command += "stepScan.run();";
		} else {
			command += "timeScan.run();";
		}
		repeatsToDo = Integer.valueOf(repeatField.getText()).intValue();
		if (repeatsToDo > 1) {
			command = "for i in range(" + repeatsToDo + "):\n\t" + command;
			command += "\n\ttry:\n\t\tScanBase.checkForInterrupts()";
			command += "\n\texcept:\n\t\tbreak";
		}
		repeatsDone = 0;
		// The generalDataHandler is initialized for each new scan
		// at present. There should be options for overlaying,
		// adding etc.
		if (thisIsATimeScan) {
			generalDataHandler.init(timeScan.getModel().getTotalPoints(), 1);
		} else {
			generalDataHandler.init(totalPoints, repeatsToDo);
		}
		int which = (generalScans[1] == null) ? 0 : 1;
		generalDataHandler.setScanUnits(generalScans[which].getScanUnits());
		// setHeader does nothing except set unused local varaiable
		// ((GeneralScanModel)
		// generalScans[which].getModel()).setHeader(header);

		overallPane.setSelectedIndex(PLOT_PANE); // Bring plot to the top
		logger.debug("PolarimeterScanPanel command being run is: {}",command);
		scriptingMediator.runCommand(command, getName());

		startButton.setEnabled(false);
		stopButton.setEnabled(true);
	}

	@Override
	public String[][] getDisAllowed() {

		return disAllow;
	}

	@Override
	public void createDataHandler() {
		generalDataHandler = new GeneralDataHandler();
	}

	/**
	 * Constructs the parts of the GUI.
	 */
	@Override
	protected void init() {
		setLayout(new BorderLayout());

		dimensionsPanel = new JPanel(new GridLayout(0, 1));
		timeScan = new TimeScanPanel();

		generalScans[0] = new GeneralScan();
		generalScans[0].addIObserver(this);
		generalScans[0].setDofNamesComparator(String.CASE_INSENSITIVE_ORDER);
		generalScans[0].setOENamesComparator(String.CASE_INSENSITIVE_ORDER);
		generalScans[0].increaseRowHeight(4);
		dimensionsPanel.add(generalScans[0]);
		scanToDo = generalScans[0];

		JPanel anotherPanel = new JPanel(new BorderLayout());
		JPanel leftHandBit = createLeftHandBit();
		if (leftHandBit != null)
			anotherPanel.add(leftHandBit, BorderLayout.WEST);

		anotherPanel.add(dimensionsPanel, BorderLayout.CENTER);
		anotherPanel.add(constructControlPanel(), BorderLayout.SOUTH);

		dataPlotter = new DataPlotter();

		// Another JTabbedPane contains the SetUp and Run parts.
		overallPane = new JTabbedPane();
		overallPane.addTab("GeneralScan", anotherPanel);
		overallPane.addTab("Run", generalDataHandler);
		overallPane.addTab("Data plot", dataPlotter);

		add(overallPane, BorderLayout.CENTER);
		add(constructButtonPanel(), BorderLayout.SOUTH);

		generalScans[0].setScannableNames(oeNames);
		setBorder(BorderFactory.createCompoundBorder(BorderFactory
				.createLoweredBevelBorder(), BorderFactory.createEmptyBorder(0,
				0, 0, 0)));

		// Add this to jython name space so that scripts can use it
		scriptingMediator.placeInJythonNamespace("scanPanelPolarimeter", this);
	}

	private JPanel createLeftHandBit() {
		JPanel leftHandBit = new JPanel(new BorderLayout());

		// FIXME: whether or not there is a scanCommandEditor (and what
		// type it is) should be XML specified.
		scanCommandEditor = new PolarimeterScanCommandEditor();
		if (scanCommandEditor.getComponent() != null)
			leftHandBit.add(scanCommandEditor.getComponent(),
					BorderLayout.CENTER);

		return leftHandBit;
	}

	/**
	 * Returns an array list of strings to be used in the header.
	 * 
	 * @return an array list of strings
	 */
	@Override
	public ArrayList<String> createHeader() {
		// The super class method makes a list containing the SRSTitle
		// and three conditions from the SRSHeaderPanel.
		ArrayList<String> header = super.createHeader();

		return header;

	}
}
