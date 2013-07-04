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
import gda.oe.MoveableException;
import gda.oe.OE;
import gda.scan.ScanDataPoint;
import gda.util.PleaseWaitWindow;
import gda.util.QuantityFactory;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.jscience.physics.quantities.Quantity;
import org.jscience.physics.units.Unit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Panel for setting up simple scans of any DOF
 */
public class GeneralOptimizePanel extends AcquisitionPanel implements IObserver, Runnable {
	
	private static final Logger logger = LoggerFactory.getLogger(GeneralOptimizePanel.class);
	
	protected OptimizeDataHandler optimizeDataHandler;

	private String disAllow[][] = {};

	private JButton startButton;

	private JButton stopButton;

	protected GeneralScan[] generalScans = new GeneralScan[2];

	protected PreScanSetupExecutor preScanSetupExecutor = null;

	private JDialog additionalSetUpDialog;

	protected JythonServerFacade scriptingMediator = null;

	private int jythonScanStatus = -1;

	private PleaseWaitWindow pww = new PleaseWaitWindow("Starting scan please wait...");

	private ArrayList<String> displayLineList = new ArrayList<String>();

	private String oeName = null;

	private String dofName = null;

	private String lineToMaximize = null;

	private double increment = 0.0;

	private int increments = 1;

	private String units = null;

	private double time = 1000.0;

	private OE oe = null;

	private String lineName;

	private double requiredPosition;

	private Quantity requiredQuantity;

	private String preScanSetupExecutorClass = null;

	private boolean optimizing = false;

	/**
	 * Constructor
	 */
	public GeneralOptimizePanel() {
	}

	@Override
	public void configure() {
		try {
			Finder finder = Finder.getInstance();
			oe = (OE) finder.find(oeName);
			scriptingMediator = JythonServerFacade.getInstance();
			scriptingMediator.addIObserver(this);
			optimizeDataHandler = new OptimizeDataHandler();
			optimizeDataHandler.setLineArray();
			optimizeDataHandler.configurePlot(oeName + ":" + dofName);
			if (preScanSetupExecutorClass != null) {
				preScanSetupExecutor = (PreScanSetupExecutor) Class.forName(preScanSetupExecutorClass).newInstance();
			}
			init();
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}

	private void init() {
		setLayout(new BorderLayout());

		add(optimizeDataHandler, BorderLayout.CENTER);
		add(constructButtonPanel(), BorderLayout.SOUTH);

		setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLoweredBevelBorder(), BorderFactory
				.createEmptyBorder(0, 0, 0, 0)));
	}

	/**
	 * @return String[][]
	 */
	public String[][] getDisAllowed() {
		return disAllow;
	}

	/**
	 * Constructs a JPanel containing the check, start and stop buttons.
	 * 
	 * @return the new JPanel
	 */
	private JPanel constructButtonPanel() {
		JPanel buttonPanel = new JPanel(new FlowLayout());

		startButton = new JButton("Optimize");
		startButton.setToolTipText("Click to start optimize");
		startButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				doStart();
			}
		});

		stopButton = new JButton("Stop Scan");
		stopButton.setToolTipText("Click to stop scan scion of optimization");
		stopButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				scriptingMediator.haltCurrentScan();
				setButtonStates(false);
			}
		});

		setButtonStates(false);
		buttonPanel.add(startButton);
		buttonPanel.add(stopButton);

		buttonPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createRaisedBevelBorder(), BorderFactory
				.createEmptyBorder(5, 3, 5, 3)));
		return buttonPanel;
	}

	private void setButtonStates(boolean scanning) {
		if (dofName != null) {
			startButton.setEnabled(!(true & scanning));
			stopButton.setEnabled(true & scanning);
		} else {
			startButton.setEnabled(false);
			stopButton.setEnabled(false);
		}
	}

	private void startTheScan() {
		pww.setVisible(true);
		double currentPosition;

		try {
			Quantity q = oe.getPosition(dofName);
			Unit<? extends Quantity> u = QuantityFactory.createUnitFromString(units);
			currentPosition = q.to(u).getAmount();

			double start = currentPosition - increments * increment;
			double end = start + 2 * increments * increment;
			String command = "stepScan = MultiRegionScan();stepScan.addScan(GridScan(" + dofName + "," + start + ","
					+ end + "," + increment + "," + time + ",\"" + units + "\"));stepScan.run();";

			optimizeDataHandler.init(2 * increments + 1, 1);

			scriptingMediator.runCommand(command, getName());

			optimizing = true;
			setButtonStates(true);
		} catch (MoveableException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Starts the PreScanSetupExecutor or the scan itself.
	 */
	private void doStart() {
		if (preScanSetupExecutor != null) {
			additionalSetUpDialog = (new JOptionPane("Optimizing Beam Position Monitor.",
					JOptionPane.INFORMATION_MESSAGE)).createDialog(this, null);
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
		if (theArgument instanceof JythonServerStatus) {
			JythonServerStatus jss = (JythonServerStatus) theArgument;
			if (optimizing) {
				if ((jss.scanStatus == Jython.IDLE) && jythonScanStatus == Jython.RUNNING) {
					pww.setVisible(false);
					{
						setButtonStates(false);
						Iterator<String> i = displayLineList.iterator();
						while (i.hasNext()) {
							lineName = i.next();
							logger.info("Maximum of " + lineName + " value at "
									+ optimizeDataHandler.getPeakPosition(lineName));
						}
						requiredPosition = optimizeDataHandler.getPeakPosition(lineToMaximize);
						requiredQuantity = QuantityFactory.createFromTwoStrings(Double.toString(requiredPosition),
								units);
						logger.info("Moving " + oeName + ":" + dofName + " to " + requiredQuantity);
						try {
							oe.moveTo(dofName, requiredQuantity);
						} catch (MoveableException e) {
							e.printStackTrace();
						}
					}
				}
				optimizeDataHandler.update(theArgument);
			}

			// This is kept up to date at all times, even when not scanning
			// from
			// here..
			jythonScanStatus = jss.scanStatus;
		} else if (theArgument instanceof ScanDataPoint) {
			/*
			 * The JythonServer will also be the cause of this case but only when this is registered as the scanObserver
			 * of the current scan. So there is bound to be a scan going on started by this and so it is safe to pass on
			 * theArgument to the generalDataHandler.
			 */
			pww.setVisible(false);
			optimizeDataHandler.update(theArgument);
		} else {
			logger.warn("Unexpected update argument in GeneralScanPanel " + getName());
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
			startButton.setEnabled(false);
			stopButton.setEnabled(false);
		}
	}

	/**
	 * @param displayLine
	 */
	public void addDisplayLine(String displayLine) {
		displayLineList.add(displayLine);
	}

	/**
	 * @return displayLineList
	 */
	public ArrayList<String> getDisplayLineList() {
		return displayLineList;
	}

	/**
	 * @param dofName
	 */
	public void setDofName(String dofName) {
		this.dofName = dofName;
	}

	/**
	 * @return dofName
	 */
	public String getDofName() {
		return dofName;
	}

	/**
	 * @return time
	 */
	public double getTime() {
		return time;
	}

	/**
	 * @param time
	 */
	public void setTime(double time) {
		this.time = time;
	}

	/**
	 * @return units
	 */
	public String getUnits() {
		return units;
	}

	/**
	 * @param units
	 */
	public void setUnits(String units) {
		this.units = units;
	}

	/**
	 * @return increment
	 */
	public double getIncrement() {
		return increment;
	}

	/**
	 * @param increment
	 */
	public void setIncrement(double increment) {
		this.increment = increment;
	}

	/**
	 * @return increments
	 */
	public int getIncrements() {
		return increments;
	}

	/**
	 * @param increments
	 */
	public void setIncrements(int increments) {
		this.increments = increments;
	}

	/**
	 * @return OE name
	 */
	public String getOeName() {
		return oeName;
	}

	/**
	 * @param oeName
	 */
	public void setOeName(String oeName) {
		this.oeName = oeName;
	}

	/**
	 * @return String lineToMaximize
	 */
	public String getLineToMaximize() {
		return lineToMaximize;
	}

	/**
	 * @param lineToMaximize
	 */
	public void setLineToMaximize(String lineToMaximize) {
		this.lineToMaximize = lineToMaximize;
	}

	/**
	 * @return String preScanSetupExecutorClass
	 */
	public String getPreScanSetupExecutorClass() {
		return preScanSetupExecutorClass;
	}

	/**
	 * @param preScanSetupExecutorClass
	 */
	public void setPreScanSetupExecutorClass(String preScanSetupExecutorClass) {
		this.preScanSetupExecutorClass = preScanSetupExecutorClass;
	}
}
