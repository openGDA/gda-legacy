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
import gda.observable.IObserver;
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
 * Panel for setting up simple scans of any DOF
 */
public class GeneralScanPanel extends AcquisitionPanel implements IObserver, Runnable {
	
	private static final Logger logger = LoggerFactory.getLogger(GeneralScanPanel.class);
	
	protected GeneralDataHandler generalDataHandler;

	private String disAllow[][] = {};

	protected JPanel dimensionsPanel;

	private JPanel panel;

	private JPanel backgroundPanel;

	private JButton detectorChoice;

	private JButton checkButton;

	protected JButton startButton;

	protected JButton stopButton;

	// might need to set this inactive in subClass
	protected JButton addButton;

	private JButton deleteButton;

	private JLabel totalTimeLabel;

	private JLabel totalPointsLabel;

	protected JTextField repeatField;

	private JPopupMenu popup;

	private ArrayList<JCheckBox> jcb = new ArrayList<JCheckBox>();

	protected JTabbedPane overallPane;

	protected GeneralScan[] generalScans = new GeneralScan[3];

	protected TimeScanPanel timeScan;

	// might need to add buttons to this in subClass
	protected JPanel buttonPanel;

	private SRSHeaderPanel headerPanel = new SRSHeaderPanel();

	// private JPanel headerPanel;
	protected ArrayList<String> oeNames = new ArrayList<String>();

	// private boolean moveToPeakRequired = false;
	// private String peakCommand = null;
	protected Scan scanToDo;

	protected int totalPoints;

	protected ArrayList<String> detectorNames;

	protected PreScanSetupExecutor preScanSetupExecutor = null;

	protected ScanCommandEditor scanCommandEditor = null;

	private JDialog additionalSetUpDialog;

	private static String warnString = "You cannot scan these two DOFs together.\nThey move the same motors";

	protected JythonServerFacade scriptingMediator = null;

	private String beamMonitorName = null;

	private int beamMonitorChannel = 0;

	private double beamMonitorThreshold = 1.0;

	private int beamMonitorWaitTime = 1;

	private double beamMonitorCountTime = 1000;

	private int beamMonitorConsecutiveCountsAboveThreshold = 6;

	private int jythonScanStatus = -1;

	protected int repeatsToDo = -1;

	protected int repeatsDone = -1;

	protected PleaseWaitWindow pww = new PleaseWaitWindow("Starting scan please wait...");

	protected final int PLOT_PANE = 1;

	/**
	 * Constructor
	 */
	public GeneralScanPanel() {
	}

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
	 * Creates the data handler. Subclasses which wish to create their own data handlers should override this method.
	 */
	public void createDataHandler() {
		generalDataHandler = new GeneralDataHandler();
	}

	/**
	 * Constructs the parts of the GUI.
	 */
	protected void init() {
		setLayout(new BorderLayout());

		dimensionsPanel = new JPanel(new GridLayout(0, 1));

		generalScans[0] = new GeneralScan();
		generalScans[0].addIObserver(this);
		dimensionsPanel.add(generalScans[0]);
		scanToDo = generalScans[0];

		JPanel anotherPanel = new JPanel(new BorderLayout());
		JPanel leftHandBit = createLeftHandBit();
		if (leftHandBit != null)
			anotherPanel.add(leftHandBit, BorderLayout.WEST);

		anotherPanel.add(dimensionsPanel, BorderLayout.CENTER);
		anotherPanel.add(constructControlPanel(), BorderLayout.SOUTH);

		timeScan = new TimeScanPanel();
		timeScan.addIObserver(this);

		overallPane = new JTabbedPane();
		overallPane.addTab("GeneralScan", anotherPanel);
		overallPane.addTab("Run", generalDataHandler);

		add(overallPane, BorderLayout.CENTER);
		buttonPanel = constructButtonPanel();
		add(buttonPanel, BorderLayout.SOUTH);

		generalScans[0].setScannableNames(oeNames);
		setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLoweredBevelBorder(), BorderFactory
				.createEmptyBorder(0, 0, 0, 0)));

		if (oeNames != null && oeNames.size() > 0) {
			addButton.setEnabled(true);
		}
	}

	/**
	 * @return String[][]
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

	protected JPanel constructControlPanel() {
		panel = new JPanel();
		backgroundPanel = new JPanel(new BorderLayout());

		panel.add(new JLabel(" Total time ( Sec ) "));
		totalTimeLabel = new JLabel(" 20 ");
		panel.add(totalTimeLabel);
		panel.add(new JLabel(" Total Points "));
		totalPointsLabel = new JLabel(" 1000 ");
		panel.add(totalPointsLabel);
		panel.add(new JLabel(" Repeats "));
		repeatField = new JTextField("1", 4);
		panel.add(repeatField);

		addButton = new JButton("Add Dimension");
		addButton.setEnabled(false);
		addButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				doAddDimension();
			}
		});

		deleteButton = new JButton("Delete Dimension");
		deleteButton.setEnabled(false);
		deleteButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				dimensionsPanel.remove(generalScans[1]);
				generalScans[1] = null;
				deleteButton.setEnabled(false);
				addButton.setEnabled(true);
				generalScans[0].enableTimeColumn();
				panel.updateUI();
				updateTotals();
			}
		});

		createPopup();
		detectorChoice = new JButton("Detectors ...");
		detectorChoice.addActionListener(new ActionListener() {
			@SuppressWarnings("unchecked")
			@Override
			public void actionPerformed(ActionEvent ev) {
				scriptingMediator
						.runCommand("defaultscannables=finder.find(\"command_server\").getDefaultScannableNames()");
				for (int i = 0; i < detectorNames.size(); i++)
					jcb.get(i).setSelected(false);

				// gross dirty hack cos we dont know when the runCommad has
				// completed
				Sleep.sleep(500);
				Vector<String> results = (Vector<String>) scriptingMediator.getFromJythonNamespace("defaultscannables");
				for (int i = 0; i < results.size(); i++) {
					String tok = results.get(i);
					for (int j = 0; j < detectorNames.size(); j++) {
						if (detectorNames.get(j).equals(tok)) {
							jcb.get(j).setSelected(true);
						}
					}
				}
				JComponent jc = (JComponent) ev.getSource();
				popup.show(jc, 0, jc.getHeight());
			}
		});

		panel.add(addButton);
		panel.add(deleteButton);
		panel.add(detectorChoice);

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
			panel.add(normalizeButton);
		}
		backgroundPanel.add(panel, BorderLayout.CENTER);
		backgroundPanel.add(headerPanel, BorderLayout.SOUTH);

		if (oeNames != null && oeNames.size() > 0) {
			addButton.setEnabled(true);
		}
		return backgroundPanel;
	}

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
								scriptingMediator.runCommand("add_default " + detName, getName());
							} else {
								scriptingMediator.runCommand("remove_default " + detName, getName());
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
	private JPanel createLeftHandBit() {
		return null;
	}

	protected void doAddDimension() {
		generalScans[1] = new GeneralScan();
		generalScans[1].setOENamesComparator(generalScans[0].getOENamesComparator());
		generalScans[1].setDofNamesComparator(generalScans[0].getDofNamesComparator());
		generalScans[1].setScannableNames(oeNames);
		generalScans[0].disableTimeColumn();
		generalScans[1].addIObserver(this);
		dimensionsPanel.add(generalScans[1]);
		deleteButton.setEnabled(true);
		addButton.setEnabled(false);
		panel.updateUI();
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
				scriptingMediator.requestFinishEarly();
				startButton.setEnabled(true);
				stopButton.setEnabled(false);
				pww.setVisible(false);
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
	 * Checks the validity of the scan(s).
	 */
	private void checkScan() {

		if (overallPane.getSelectedIndex() == 0) {
			// BFI big time - checks whether any disallowed pair of DOFs is
			// being
			// scanned. In this base class the dissallow array is empty so
			// the
			// only disallowed pairs are those where both are the same DOF.
			boolean namesSame = false;
			if (generalScans[1] != null) {
				if (generalScans[0].getOEName().equals(generalScans[1].getOEName())) {
					String firstDofName = generalScans[0].getDofName();
					String secondDofName = generalScans[1].getDofName();
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
				generalScans[0].check();
				if (generalScans[1] != null) {
					generalScans[1].check();
				}
			} else {
				JOptionPane.showOptionDialog(getTopLevelAncestor(), warnString, "Duplicate DOF Names",
						JOptionPane.OK_OPTION, JOptionPane.ERROR_MESSAGE, null, new String[] { "OK" }, "OK");
			}
		}
	}

	/**
	 * Adds a quit button. Only used by GeneralScanGUI.
	 */
	public void addQuitButton() {
		JButton quitButton = new JButton("Quit");
		quitButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				scriptingMediator.abortCommands();
			}
		});
		buttonPanel.add(quitButton);
	}

	/**
	 * Calculates and displays the total time and total number of points.
	 */
	protected void updateTotals() {
		double totalTime;
		if (generalScans[1] != null)
			totalTime = generalScans[1].getTotalTime() * generalScans[0].getTotalNumberOfPoints();
		else
			totalTime = generalScans[0].getTotalTime();

		if (generalScans[1] != null)
			totalPoints = generalScans[1].getTotalNumberOfPoints() * generalScans[0].getTotalNumberOfPoints();
		else
			totalPoints = generalScans[0].getTotalNumberOfPoints();

		totalTimeLabel.setText("" + totalTime);
		totalPointsLabel.setText("" + totalPoints);
	}

	private void startTheScan() {
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
			} else {
				if (scanCommandEditor != null)
					command = scanCommandEditor.editCommand(command);
				logger.debug("scanToDo " + command);
			}

			scriptingMediator.placeInJythonNamespace("scanheader", header);
			command += "stepScan.getDataWriter().setHeader(scanheader);";
			if (beamMonitorName != null)
				command += "beammonitor=BeamMonitor(\"" + beamMonitorName + "\"," + beamMonitorChannel + ","
						+ beamMonitorThreshold + "," + beamMonitorWaitTime + ","
						+ beamMonitorConsecutiveCountsAboveThreshold + "," + beamMonitorCountTime + ");";
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
		logger.debug("GeneralScanPanel command being run is: " + command);
		scriptingMediator.runCommand(command, getName());

		startButton.setEnabled(false);
		stopButton.setEnabled(true);
	}

	/**
	 * Starts the PreScanSetupExecutor or the scan itself.
	 */
	private void doStart() {
		startButton.setEnabled(false);

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
	 * Implements the IObserver interface. Called by IObservables when they change.
	 * 
	 * @param theObserved
	 *            the IObservable which has changed
	 * @param theArgument
	 *            the argument it has sent
	 */
	@Override
	public void update(Object theObserved, Object theArgument) {
		// Should be called either by the GeneralScan when it is changed
		// by the user or by the JythonServer when it carries out commands
		// (both those started here and those started elsewhere).

		// If called by a GeneralScan then the totals part of the window
		// needs updating and the buttons are enabled according to the
		// validity or otherwise of the scan(s).
		if (theObserved instanceof GeneralScan) {
			updateTotals();
			checkButton.setEnabled(!(generalScans[0].getValid() && (generalScans[1] == null || generalScans[1]
					.getValid())));
			startButton.setEnabled(generalScans[0].getValid()
					&& (generalScans[1] == null || generalScans[1].getValid()));
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
					logger.info("GeneralScanPanel.update() in " + getName() + " repeat over? - repeatsDone is "
							+ repeatsDone + "  repeatsToDo is " + repeatsToDo);
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
		}
		// Note that TimeScanPanel is misnamed, it actual parallels GeneralScan
		// since it extends Scan and so should be called TimeScan. Anyway if it
		// is notifying we need do nothing.
		else if (theObserved instanceof TimeScanPanel) {
			// Deliberately do nothing
		} else {
			//ignore anything not expected
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
	 * @return ArrayList<String>
	 */
	public ArrayList<String> createHeader() {
		return headerPanel.getHeader();
	}
}
