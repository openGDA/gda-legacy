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

import gda.factory.Finder;
import gda.gui.AcquisitionPanel;
import gda.gui.generalscan.PreScanSetupExecutor.PreScanStatus;
import gda.jython.Jython;
import gda.jython.JythonServerFacade;
import gda.jython.JythonServerStatus;
import gda.observable.IObservable;
import gda.observable.IObserver;
import gda.observable.ObservableComponent;
import gda.scan.ScanDataPoint;
import gda.util.PleaseWaitWindow;
import gda.util.Sleep;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Create a Panel for setting up simple scans of any combinations of DOF and/or detector
 */
public class ConfigurableExperimentPanel extends AcquisitionPanel implements IObservable, IObserver, Runnable {
	
	private static final Logger logger = LoggerFactory.getLogger(ConfigurableExperimentPanel.class);
	
	protected GeneralDataHandler generalDataHandler;

	private String disAllow[][] = {};

	protected JPanel dimensionsPanel;

	protected JPanel controlPanel;

	private JPanel backgroundPanel;

	protected JButton checkButton;

	protected JButton startButton;

	protected JButton stopButton;

	private JButton detectorChoice;

	// might need to set this inactive in subClass
	protected JButton addDimensionButton;

	protected JButton deleteDimensionButton;

	protected JLabel totalTimeLabel;

	protected JLabel totalPointsLabel;

	protected JTextField repeatField;

	private JPopupMenu popup;

	private ArrayList<JCheckBox> jcb = new ArrayList<JCheckBox>();

	protected JTabbedPane overallPane;

	protected ArrayList<GeneralScan> generalScans = new ArrayList<GeneralScan>();

	protected ArrayList<String> scanNames = new ArrayList<String>();

	// might need to add buttons to this in subClass
	protected JPanel buttonPanel;

	private SRSHeaderPanel headerPanel = new SRSHeaderPanel();

	// protected ArrayList<String> scannableNames;
	protected ArrayList<String> detectorNames;

	// a list of all OEs available
	protected ArrayList<String> oeNames;

	// OE names & corresponding DOF names listed in XML file as default
	// scans
	protected ArrayList<String> allOeNames = new ArrayList<String>();

	protected ArrayList<String> dofNames = new ArrayList<String>();

	protected int totalPoints;

	protected PreScanSetupExecutor preScanSetupExecutor = null;

	protected ScanCommandEditor scanCommandEditor = null;

	private JDialog additionalSetUpDialog;

	private static String warnString = "You cannot scan these two DOFs together.\nThey move the same motors";

	protected JythonServerFacade scriptingMediator = null;

	protected String beamMonitorName = null;

	protected int beamMonitorChannel = 0;

	protected double beamMonitorThreshold = 1.0;

	protected int beamMonitorWaitTime = 1;

	protected double beamMonitorCountTime = 1000;

	protected int beamMonitorConsecutiveCountsAboveThreshold = 6;

	protected int jythonScanStatus = -1;

	protected int repeatsToDo = -1;

	protected int repeatsDone = -1;

	protected PleaseWaitWindow pww = new PleaseWaitWindow("Starting scan please wait...");

	protected boolean addDimensionEnabled = true;

	GeneralScan firstScan = null;

	private boolean addRegionEnabled = true;

	@Override
	public void configure() {
		try {
			Finder finder = Finder.getInstance();
			scriptingMediator = JythonServerFacade.getInstance();
			// NB Panels must be IObservers of the JythonServerFacade or
			// they
			// never get updates about the status of the JythonServer, only
			// updates containing data while there is a scan running.
			scriptingMediator.addIObserver(this);

			// all available OEs & timeScan (to be added later)
			oeNames = finder.listAllNames("OE");
			oeNames.add(GeneralScan.TIMESCAN);

			detectorNames = finder.listAllNames("Detector");

			createDataHandler();
			init();
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}

	/**
	 * Find out if add scan dimension function is enabled
	 * 
	 * @return boolean true if addDimension function is enabled
	 */
	public boolean getAddDimensionEnabled() {
		return addDimensionEnabled;
	}

	/**
	 * Enable or disable the add scan dimension function
	 * 
	 * @param b
	 *            boolean true if add scan dimension function to be enabled else false
	 */
	public void setAddDimensionEnabled(boolean b) {
		addDimensionEnabled = b;
	}

	/**
	 * Find out if add scan region function is enabled
	 * 
	 * @return boolean true if add Region function is enabled
	 */
	public boolean getAddRegionEnabled() {
		return addRegionEnabled;
	}

	/**
	 * Set whether or no add scan region function is enabled
	 * 
	 * @param b
	 *            boolean true if add Region function is to be enabled
	 */
	public void setAddRegionEnabled(boolean b) {
		addRegionEnabled = b;
	}

	/**
	 * Get a list of DOF names available
	 * 
	 * @return ArrayList<String> containing dof names
	 */
	public ArrayList<String> getDofNames() {
		return dofNames;
	}

	/**
	 * Set the list of DOF names
	 * 
	 * @param names
	 *            ArrayList<String> containing all DOF names
	 */
	public void setDofNames(ArrayList<String> names) {
		dofNames = new ArrayList<String>();
		for (String name : names)
			dofNames.add(name);
	}

	/**
	 * Add a DOF name to the list of DOF names
	 * 
	 * @param name
	 *            String name of DOF to be added to list
	 */
	public void addDofName(String name) {
		dofNames.add(name);
	}

	/**
	 * Get OE names for this scan
	 * 
	 * @return an ArrayList<String> of OE names
	 */
	public ArrayList<String> getOeNames() {
		return allOeNames;
	}

	/**
	 * Set OE names for this scan
	 * 
	 * @param names
	 *            an ArrayList<String> of OE names
	 */
	public void setOeNames(ArrayList<String> names) {
		allOeNames = new ArrayList<String>();
		for (String name : names)
			allOeNames.add(name);
	}

	/**
	 * Add OE name to list available for this scan
	 * 
	 * @param name
	 *            String name of OE to be added
	 */
	public void addOeName(String name) {
		allOeNames.add(name);
	}

	/**
	 * Create the data handler. Subclasses which wish to create their own data handlers should override this method.
	 * This dataHandler will handle the data within the GUI. The scan itself will have a dataHandler which handles data
	 * to file (see MultiRegionScan.createDataHandler & java property LocalProperties.GDA_DATA_SCAN_DATAWRITER_DATAFORMAT (which
	 * should be set to SrsDataFile in this case).
	 */
	public void createDataHandler() {
		generalDataHandler = new GeneralDataHandler();
	}

	/**
	 * Create GUI panels
	 */
	protected void createPanels() {
		setLayout(new BorderLayout());

		dimensionsPanel = new JPanel(new GridLayout(0, 1));

		JPanel anotherPanel = new JPanel(new BorderLayout());
		JPanel leftHandBit = createLeftHandBit();
		if (leftHandBit != null)
			anotherPanel.add(leftHandBit, BorderLayout.WEST);

		anotherPanel.add(dimensionsPanel, BorderLayout.CENTER);
		anotherPanel.add(constructControlPanel(), BorderLayout.SOUTH);

		overallPane = new JTabbedPane();
		overallPane.addTab("Scans", anotherPanel);
		overallPane.addTab("Run", generalDataHandler);

		add(overallPane, BorderLayout.CENTER);
		buttonPanel = constructButtonPanel();
		add(buttonPanel, BorderLayout.SOUTH);

		setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLoweredBevelBorder(), BorderFactory
				.createEmptyBorder(0, 0, 0, 0)));
	}

	/**
	 * Construct the parts of the GUI.
	 */
	protected void init() {
		createPanels();
		initScanNames();

		ArrayList<String> scannableNames;
		// if not XML configured make the default option all available OEs
		if (allOeNames.size() == 0)
			scannableNames = oeNames;
		else {
			scannableNames = new ArrayList<String>();
			scannableNames.add(allOeNames.get(0));
		}
		doAddDimension(scanNames.get(0), scannableNames);

		for (int i = 1; i < allOeNames.size(); i++) {
			scannableNames = new ArrayList<String>();
			scannableNames.add(allOeNames.get(i));
			scanNames.add("Dimension " + (i + 1) + " - Inner Scan (" + allOeNames.get(i) + ")");
			doAddDimension(scanNames.get(i), scannableNames);
		}

		updateTotals();
		controlPanel.updateUI();
	}

	/**
	 * Initialise the names of the san dimension in the GUI scan panel
	 */
	protected void initScanNames() {
		if (allOeNames.size() == 0)
			scanNames.add("Dimension 1 - Outer Scan ");
		else
			scanNames.add("Dimension 1 - Outer Scan (" + allOeNames.get(0) + ")");

		for (int i = 1; i < allOeNames.size(); i++)
			scanNames.add("Dimension " + (i + 1) + " - Inner Scan (" + allOeNames.get(i) + ")");
	}

	/**
	 * Get array of disallowed Strings. Used in checkScan to check scan validity.
	 * 
	 * @return String[][] array of disallowed
	 */
	public String[][] getDisAllowed() {
		return disAllow;
	}

	/**
	 * @return Returns the beamMonitorChannel.
	 */
	public int getBeamMonitorChannel() {
		return beamMonitorChannel;
	}

	/**
	 * @param beamMonitorChannel
	 *            The beamMonitorChannel to set.
	 */
	public void setBeamMonitorChannel(int beamMonitorChannel) {
		this.beamMonitorChannel = beamMonitorChannel;
	}

	/**
	 * @return Returns the beamMonitorConsecutiveCountsAboveThreshold.
	 */
	public int getBeamMonitorConsecutiveCountsAboveThreshold() {
		return beamMonitorConsecutiveCountsAboveThreshold;
	}

	/**
	 * @param beamMonitorConsecutiveCountsAboveThreshold
	 *            The beamMonitorConsecutiveCountsAboveThreshold to set.
	 */
	public void setBeamMonitorConsecutiveCountsAboveThreshold(int beamMonitorConsecutiveCountsAboveThreshold) {
		this.beamMonitorConsecutiveCountsAboveThreshold = beamMonitorConsecutiveCountsAboveThreshold;
	}

	/**
	 * @return Returns the beamMonitorCountTime.
	 */
	public double getBeamMonitorCountTime() {
		return beamMonitorCountTime;
	}

	/**
	 * @param beamMonitorCountTime
	 *            The beamMonitorCountTime to set.
	 */
	public void setBeamMonitorCountTime(double beamMonitorCountTime) {
		this.beamMonitorCountTime = beamMonitorCountTime;
	}

	/**
	 * @return Returns the beamMonitorName.
	 */
	public String getBeamMonitorName() {
		return beamMonitorName;
	}

	/**
	 * @param beamMonitorName
	 *            The beamMonitorName to set.
	 */
	public void setBeamMonitorName(String beamMonitorName) {
		this.beamMonitorName = beamMonitorName;
	}

	/**
	 * @return Returns the beamMonitorThreshold.
	 */
	public double getBeamMonitorThreshold() {
		return beamMonitorThreshold;
	}

	/**
	 * @param beamMonitorThreshold
	 *            The beamMonitorThreshold to set.
	 */
	public void setBeamMonitorThreshold(double beamMonitorThreshold) {
		this.beamMonitorThreshold = beamMonitorThreshold;
	}

	/**
	 * @return Returns the beamMonitorWaitTime.
	 */
	public int getBeamMonitorWaitTime() {
		return beamMonitorWaitTime;
	}

	/**
	 * @param beamMonitorWaitTime
	 *            The beamMonitorWaitTime to set.
	 */
	public void setBeamMonitorWaitTime(int beamMonitorWaitTime) {
		this.beamMonitorWaitTime = beamMonitorWaitTime;
	}

	/**
	 * Construct the Scan control Panel
	 * 
	 * @return JPanel, the control panel
	 */
	protected JPanel constructControlPanel() {
		controlPanel = new JPanel();
		backgroundPanel = new JPanel(new BorderLayout());

		controlPanel.add(new JLabel(" Total time ( Sec ) "));
		totalTimeLabel = new JLabel(" 20 ");
		controlPanel.add(totalTimeLabel);
		controlPanel.add(new JLabel(" Total Points "));
		totalPointsLabel = new JLabel(" 1000 ");
		controlPanel.add(totalPointsLabel);
		controlPanel.add(new JLabel(" Repeats "));
		repeatField = new JTextField("1", 4);
		controlPanel.add(repeatField);

		addDimensionButton = new JButton("Add Dimension");
		addDimensionButton.setEnabled(false);
		addDimensionButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				doAddDimension("Scan Dimension", oeNames);
			}
		});

		deleteDimensionButton = new JButton("Delete Dimension");
		deleteDimensionButton.setEnabled(false);
		deleteDimensionButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				doDeleteDimension();
			}
		});

		createPopup();
		detectorChoice = new JButton("Detectors ...");
		detectorChoice.addActionListener(new ActionListener() {
			@SuppressWarnings("unchecked")
			@Override
			public void actionPerformed(ActionEvent ev) {
				for (int i = 0; i < detectorNames.size(); i++)
					jcb.get(i).setSelected(false);

				scriptingMediator
						.runCommand("defaultscannables=finder.find(\"command_server\").getDefaultScannableNames()");
				Sleep.sleep(200);
				Vector<String> results = (Vector<String>) scriptingMediator.getFromJythonNamespace("defaultscannables");
				for (int i = 0; i < results.size(); i++) {
					String tok = results.get(i);
					for (int j = 0; j < detectorNames.size(); j++)
						if (detectorNames.get(j).equals(tok))
							jcb.get(j).setSelected(true);
				}
				JComponent jc = (JComponent) ev.getSource();
				popup.show(jc, 0, jc.getHeight());
			}
		});

		if (addDimensionEnabled) {
			controlPanel.add(addDimensionButton);
			controlPanel.add(deleteDimensionButton);
			addDimensionButton.setEnabled(true);
		}

		controlPanel.add(detectorChoice);

		// This button is created only if generalDataHandler has a
		// Normalize dialog. It should be part of the easily overrideable
		// parts of the class rather than doing this.
		if (generalDataHandler.hasNormalize()) {
			JButton normalizeButton = new JButton("Normalize");
			normalizeButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					generalDataHandler.remoteNormalize();
				}
			});
			controlPanel.add(normalizeButton);
		}
		backgroundPanel.add(controlPanel, BorderLayout.CENTER);
		backgroundPanel.add(headerPanel, BorderLayout.SOUTH);

		return backgroundPanel;
	}

	/**
	 * Create a pop-up check box for selection of detectors
	 */
	private void createPopup() {
		popup = new JPopupMenu();
		popup.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createRaisedBevelBorder(), BorderFactory
				.createEmptyBorder(3, 3, 3, 3)));
		for (int i = 0; i < detectorNames.size(); i++) {
			String detName = detectorNames.get(i);
			JCheckBox jcbox = new JCheckBox(detName);
			jcb.add(jcbox);
			jcbox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent ev) {
					JCheckBox c = (JCheckBox) ev.getSource();
					String name = c.getText();
					for (int i = 0; i < detectorNames.size(); i++) {
						String detName = detectorNames.get(i);
						if (detName.equals(name)) {
							if (c.isSelected()) {
								scriptingMediator.runCommand("add default " + detName, getName());
							} else {
								scriptingMediator.runCommand("remove default " + detName, getName());
							}
							break;
						}
					}
				}
			});
			popup.add(jcbox);
		}
	}

	/**
	 * Subclasses may override this to create a panel to be used by e.g. their PreScanSetupExecutors or
	 * ScanCommandEditors.
	 * 
	 * @return the created panel
	 */
	protected JPanel createLeftHandBit() {
		return null;
	}

	/**
	 * Add a scan dimension
	 * 
	 * @param dimensionName
	 *            the name of the dimension to add
	 * @param scannableNames
	 *            the list of optional scannables for this dimension
	 */
	protected void doAddDimension(String dimensionName, ArrayList<String> scannableNames) {
		GeneralScan thisScan = null;
		thisScan = new GeneralScan(dimensionName, addRegionEnabled);
		thisScan.setScannableNames(scannableNames);

		if (firstScan == null)
			firstScan = thisScan;
		generalScans.add(thisScan);

		GeneralScan previousScan = null;
		if (generalScans.size() > 1) {
			previousScan = generalScans.get(generalScans.size() - 2);

			// If the previous scan is not a time (detector) scan then
			// remove the
			// TIMESCAN option from the oeNameCombo, this is because
			// TimeScans can
			// only be the innermost scan (undefined behaviour otherwise)
			thisScan.setOENamesComparator(firstScan.getOENamesComparator());
			thisScan.setDofNamesComparator(firstScan.getDofNamesComparator());
			if (!previousScan.getOEName().equals(GeneralScan.TIMESCAN))
				previousScan.removeOeName(GeneralScan.TIMESCAN);

			// since we have more than one scan it's OK to delete a
			// dimension
			if (addDimensionEnabled)
				deleteDimensionButton.setEnabled(true);
		}

		// If this Scan is not a time (detector) scan then use the collect
		// time of previous scan and disable the time column in this one
		if (!thisScan.getOEName().equals(GeneralScan.TIMESCAN)) {
			if (!(previousScan == null))
				previousScan.disableTimeColumn();
			if (addDimensionEnabled)
				addDimensionButton.setEnabled(true);
		} else {
			// TimeScans can't have children
			addDimensionButton.setEnabled(false);
		}

		thisScan.addIObserver(this);
		dimensionsPanel.add(thisScan);

		updateTotals();
		controlPanel.updateUI();
	}

	/**
	 * Delete a scan dimension
	 */
	protected void doDeleteDimension() {
		dimensionsPanel.remove(generalScans.get(generalScans.size() - 1));
		generalScans.remove(generalScans.size() - 1);
		generalScans.get(generalScans.size() - 1).enableTimeColumn();
		generalScans.get(generalScans.size() - 1).addOeName(GeneralScan.TIMESCAN);
		if (addDimensionEnabled) {
			// addDimension will have been disabled for a TimeScan so enable
			// it
			addDimensionButton.setEnabled(true);
			if (generalScans.size() == 1)
				deleteDimensionButton.setEnabled(false);
		}

		controlPanel.updateUI();
		updateTotals();
	}

	/**
	 * Constructs a JPanel containing the check, start and stop buttons.
	 * 
	 * @return the new JPanel
	 */
	protected JPanel constructButtonPanel() {
		JPanel buttonPanel = new JPanel(new FlowLayout());

		checkButton = new JButton("Check Scan");
		checkButton.setEnabled(true);
		checkButton.setToolTipText("Click to check scan");
		checkButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent ev) {
				checkScan();
			}
		});

		startButton = new JButton("Start Scan");
		startButton.setEnabled(false);
		startButton.setToolTipText("Click to start scan");
		startButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				doStart();
			}
		});

		stopButton = new JButton("Stop Scan");
		stopButton.setToolTipText("Click to stop scan");
		stopButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				scriptingMediator.haltCurrentScan();
				startButton.setEnabled(true);
				stopButton.setEnabled(false);
			}
		});

		buttonPanel.add(checkButton);
		buttonPanel.add(startButton);
		buttonPanel.add(stopButton);

		buttonPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createRaisedBevelBorder(), BorderFactory
				.createEmptyBorder(5, 3, 5, 3)));
		return buttonPanel;
	}

	/**
	 * Check the validity of the scan(s).
	 */
	private void checkScan() {
		if (overallPane.getSelectedIndex() == 0) {
			// BFI big time - checks whether any disallowed pair of DOFs is
			// being
			// scanned. In this base class the dissallow array is empty so
			// the
			// only disallowed pairs are those where both are the same DOF.
			boolean namesSame = false;
			if (generalScans.size() > 1) {
				if (generalScans.get(0).getOEName().equals(generalScans.get(1).getOEName())) {
					String firstDofName = generalScans.get(0).getDofName();
					String secondDofName = generalScans.get(1).getDofName();
					namesSame = firstDofName.equals(secondDofName);
					if (!namesSame) {
						String[][] disAllow = getDisAllowed();
						for (int i = 0; i < disAllow.length; i++) {
							if (disAllow[i][0].equals(firstDofName))
								for (int j = 1; j < disAllow[i].length; j++)
									if (disAllow[i][j].equals(secondDofName)) {
										namesSame = true;
										break;
									}
						}
					}
				}
			}

			// If the first test is passed then the scans check themselves
			// for
			// numerical problems.
			if (!namesSame) {
				generalScans.get(0).check();
				if (generalScans.size() > 1) {
					generalScans.get(1).check();
				}
			} else {
				JOptionPane.showOptionDialog(getTopLevelAncestor(), warnString, "Duplicate DOF Names",
						JOptionPane.OK_OPTION, JOptionPane.ERROR_MESSAGE, null, new String[] { "OK" }, "OK");
			}
		}
	}

	/**
	 * Add a quit button. Only used by GeneralScanGUI.
	 */
	public void addQuitButton() {
		JButton quitButton = new JButton("Quit");
		quitButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				scriptingMediator.haltCurrentScan();
			}
		});
		buttonPanel.add(quitButton);
	}

	/**
	 * Calculat and displays the total time and total number of points.
	 */
	protected void updateTotals() {
		totalPoints = generalScans.get(0).getTotalNumberOfPoints();
		double totalTime = generalScans.get(0).getTotalTime();

		for (int i = 1; i < generalScans.size(); i++) {
			totalTime = totalPoints * generalScans.get(i).getTotalTime();
			totalPoints *= generalScans.get(i).getTotalNumberOfPoints();
		}

		totalTimeLabel.setText("" + totalTime);
		totalPointsLabel.setText("" + totalPoints);
	}

	/**
	 * Starts the PreScanSetupExecutor or the scan itself.
	 */
	private void doStart() {
		startButton.setEnabled(false);
		stopButton.setEnabled(true);

		if (preScanSetupExecutor != null) {
			additionalSetUpDialog = (new JOptionPane("Carrying out pre-scan setup.", JOptionPane.INFORMATION_MESSAGE))
					.createDialog(this, null);
			additionalSetUpDialog.setModal(false);
			additionalSetUpDialog.setVisible(true);
			preScanSetupExecutor.execute();
			(uk.ac.gda.util.ThreadManager.getThread(this, getClass().getName())).start();
		} else {
			startTheScan();
		}
	}

	/**
	 * Start the scan. The generalDataHandler is initialized for each new scan at present. There should be options for
	 * overlaying, adding etc.
	 */
	protected void startTheScan() {
		pww.setVisible(true);

		repeatsToDo = 1;
		ArrayList<String> header = createHeader();
		repeatsDone = 0;

		String thisCommand = null;
		String nextCommand = generalScans.get(0).getCommand();
		String firstScanName = nextCommand.substring(0, nextCommand.indexOf(" ="));
		String nextScanName = null;
		String command = null;
		int nextScan;

		// assume all scans are nested, don't allow nested scan inside timeScan
		for (int i = 0; i < generalScans.size(); i++) {
			thisCommand = nextCommand;
			nextScan = i + 1;
			if (generalScans.size() > i + 1) {
				nextScanName = "innerScan" + nextScan;
				String scanName = generalScans.get(nextScan).getScanName();
				nextCommand = generalScans.get(nextScan).getCommand().replaceAll(scanName, nextScanName);
				thisCommand = thisCommand.replaceAll("\\)\\);", ", " + nextScanName + "));");
			}
			if (i == 0)
				command = thisCommand;
			else
				command = thisCommand + command;
		}

		if (scanCommandEditor != null)
			command = scanCommandEditor.editCommand(command);
		logger.debug("scanToDo " + command);

		scriptingMediator.placeInJythonNamespace("scanheader", header);
		command += firstScanName + ".getDataWriter().setHeader(scanheader);";
		if (beamMonitorName != null)
			command += "beammonitor=BeamMonitor(\"" + beamMonitorName + "\"," + beamMonitorChannel + ","
					+ beamMonitorThreshold + "," + beamMonitorWaitTime + ","
					+ beamMonitorConsecutiveCountsAboveThreshold + "," + beamMonitorCountTime + ");";
		command += firstScanName + ".run();";

		repeatsToDo = Integer.valueOf(repeatField.getText()).intValue();
		if (repeatsToDo > 1) {
			command = "for i in range(" + repeatsToDo + "):\n\t" + command;
			command += "\n\ttry:\n\t\tScanBase.checkForInterrupts()";
			command += "\n\texcept:\n\t\tbreak";
		}

		generalDataHandler.init(totalPoints, repeatsToDo);

		logger.debug("ConfigurableExperimentPanel command being run is: " + command);

		scriptingMediator.runCommand(command, getName());

		startButton.setEnabled(false);
		stopButton.setEnabled(true);
	}

	@Override
	public void update(Object theObserved, Object theArgument) {
		// Should be called either by the GeneralScan when it is changed
		// by the user or by the JythonServer when it carries out commands
		// (both those started here and those started elsewhere).

		// If called by a GeneralScan then the totals part of the window
		// needs updating and the buttons are enabled according to the
		// validity or otherwise of the scan(s).
		if (theObserved instanceof GeneralScan) {
			if (((GeneralScan) theObserved).thisIsATimeScan()) {
				addDimensionButton.setEnabled(false);
				if (generalScans.size() > 1)
					generalScans.get(generalScans.size() - 2).enableTimeColumn();
			} else {
				if (addDimensionEnabled)
					addDimensionButton.setEnabled(true);
				else
					addDimensionButton.setEnabled(false);

				generalScans.get(generalScans.size() - 1).enableTimeColumn();
			}

			updateTotals();
			updateUI();
			checkButton.setEnabled(!(generalScans.get(0).getValid() && (generalScans.size() == 1 || generalScans.get(1)
					.getValid())));
			startButton.setEnabled(generalScans.get(0).getValid()
					&& (generalScans.size() == 1 || generalScans.get(1).getValid()));
			stopButton.setEnabled(false);
		}
		// If theArgument is a JythonServerStatus then the update is being
		// called
		// as result of being a normal IObserver of the JythonServer so will
		// happen when other things are causing scans as well. The value of
		// repeatsToDo is used to determine whether this is the starter of the
		// scan. If it is then the stopButton is kept enabled and the
		// startButton
		// disabled until the JythonServer status changes from RUNNING to IDLE
		// for the repeatsToDo'th time. It would be nice to be able to use the
		// JythonServerStatus.scriptStatus to determine that multiple scans were
		// going on however this is not possible because its value changes as
		// other jython commands, e.g. those to write the data files, are done.
		else if (theArgument instanceof JythonServerStatus) {
			JythonServerStatus jss = (JythonServerStatus) theArgument;
			if (repeatsToDo > 0) {
				if (jss.scanStatus == Jython.IDLE && jythonScanStatus == Jython.RUNNING) {
					pww.setVisible(false);
					++repeatsDone;
					if (repeatsDone == repeatsToDo) {
						startButton.setEnabled(true);
						stopButton.setEnabled(false);
						repeatsToDo = -1;
					}
				}
				generalDataHandler.update(theArgument);
			}

			// This is kept up to date at all times, even when not scanning
			// from
			// here..
			jythonScanStatus = jss.scanStatus;
		}
		// The JythonServer will also be the cause of this case but only when
		// this
		// is registered as the scanObserver of the current scan. So there is
		// bound to be a scan going on started by this and so it is safe to pass
		// on theArgument to the generalDataHandler.
		else if (theArgument instanceof ScanDataPoint) {
			pww.setVisible(false);
			generalDataHandler.update(theArgument);
		} else if (theArgument == null) {
			// ignore
		} else {
			logger.debug("Unexpected update argument in ConfigurableExperimentPanel " + getName());
		}
	}

	/**
	 * If there is a PreScanSetupExecutor then a thread is started to monitor it and carry on with the scan when it is
	 * done.
	 */
	@Override
	public synchronized void run() {
		PreScanStatus status;
		do {
			logger.debug("waiting");
			try {
				synchronized (this) {
					wait(1000);
				}
			} catch (InterruptedException e) {
				// Deliberately do nothing
			}
			status = preScanSetupExecutor.getStatus();
		} while (status == PreScanStatus.BUSY);

		additionalSetUpDialog.dispose();

		// If the PreScanSetupExecutor succeeded then start the scan otherwise
		// popup an error message.
		if (status == PreScanStatus.SUCCESS) {
			startTheScan();
		} else {
			additionalSetUpDialog = (new JOptionPane(preScanSetupExecutor.getErrorMessage(), JOptionPane.ERROR_MESSAGE))
					.createDialog(this, null);
			additionalSetUpDialog.setModal(false);
			additionalSetUpDialog.setVisible(true);
			while (additionalSetUpDialog.isShowing()) {
				try {
					wait(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			checkButton.setEnabled(true);
			startButton.setEnabled(false);
			stopButton.setEnabled(false);
		}
	}

	/**
	 * Create a data file header
	 * 
	 * @return the header
	 */
	public ArrayList<String> createHeader() {
		return headerPanel.getHeader();
	}
	// IObservable interface

	private ObservableComponent observableComponent = new ObservableComponent();

	@Override
	public void addIObserver(IObserver anIObserver) {
		observableComponent.addIObserver(anIObserver);
	}

	@Override
	public void deleteIObserver(IObserver anIObserver) {
		observableComponent.deleteIObserver(anIObserver);
	}

	/**
	 * 
	 */
	@Override
	public void deleteIObservers() {
		observableComponent.deleteIObservers();
	}

	/**
	 * @param theObserver
	 * @param theArgument
	 */
	public void notifyIObservers(Object theObserver, Object theArgument) {
		observableComponent.notifyIObservers(theObserver, theArgument);
	}
}
