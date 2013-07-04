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

package gda.gui.oemove.plugins;

import gda.device.Detector;
import gda.device.DeviceException;
import gda.factory.Configurable;
import gda.factory.FactoryException;
import gda.factory.Findable;
import gda.factory.Finder;
import gda.gui.generalscan.WhatToPlotDialog;
import gda.gui.generalscan.WhatToPlotDialogUser;
import gda.gui.oemove.Pluggable;
import gda.jython.Jython;
import gda.jython.JythonServerFacade;
import gda.jython.JythonServerStatus;
import gda.observable.IObserver;
import gda.plots.SimplePlot;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CounterTimerMonitor class
 */
public class CounterTimerMonitor implements IObserver, Pluggable, Findable, Configurable, WhatToPlotDialogUser,
		Runnable {
	
	private static final Logger logger = LoggerFactory.getLogger(CounterTimerMonitor.class);
	
	private String name;

	private JythonServerFacade scriptingMediator;

	private JButton startButton;

	private JButton stopButton;

	private JTextField countingTimeField;

	private double countingTime;

	private String counterTimerName;

	private volatile boolean carryOnMonitoring = false;

	private volatile boolean statusAllowsMonitoring = true;

	private Detector counterTimer;

	private JLabel[] labels;

	private int numberOfChannels;

	private JTextField[] currentValues;

	private String[] labelStrings;

	private JTabbedPane displayComponent;

	private JPanel controlComponent;

	private NumberFormat nf;

	private int decimalPlaces = 0;

	private int valueFontSize = 30;

	private SimplePlot plot;

	private int pointCounter = 1;

	private WhatToPlotDialog whatToPlotDialog;

	private JTextArea historyTxt = new JTextArea(null, 10, 10);

	private boolean showHistory = false;

	/**
	 * Constructor
	 */
	public CounterTimerMonitor() {
	}

	/**
	 * Get whether or not to show the History tabbed pane
	 * 
	 * @return boolean true if panel containing history of monitor values is to be shown
	 */
	public boolean getShowHistory() {
		return showHistory;
	}

	/**
	 * Set whether or not to show the History tabbed pane
	 * 
	 * @param b
	 *            boolean true if panel containing history of monitor values is to be shown
	 */
	public void setShowHistory(boolean b) {
		showHistory = b;
	}

	@Override
	public JComponent getDisplayComponent() {
		return displayComponent;
	}

	/**
	 * Returns the control component.
	 * 
	 * @return the control component
	 */
	@Override
	public JComponent getControlComponent() {
		return controlComponent;
	}

	/**
	 * Starts monitoring.
	 */
	private void startMonitoring() {
		startButton.setEnabled(false);
		countingTime = Double.valueOf(countingTimeField.getText());
		initializePlot();
		pointCounter = 1;
		carryOnMonitoring = true;
		uk.ac.gda.util.ThreadManager.getThread(this).start();
	}

	/**
	 * Sets the flag which tells monitoring to stop.
	 */
	private void stopMonitoring() {
		carryOnMonitoring = false;
	}

	@Override
	public void update(Object theObserved, Object changeCode) {

		if (changeCode instanceof JythonServerStatus) {
			logger.debug("CounterTimerMonitor update called with " + theObserved + " " + changeCode);
			JythonServerStatus newStatus = (JythonServerStatus) changeCode;

			// The additional statusAllowsMonitoring flag is used in the run
			// method to determine whether or not the startButton should be
			// enabled when monitoring ends.
			if (newStatus.scriptStatus == Jython.IDLE && newStatus.scanStatus == Jython.IDLE) {
				statusAllowsMonitoring = true;
				startButton.setEnabled(true);
			} else {
				carryOnMonitoring = false;
				statusAllowsMonitoring = false;
				startButton.setEnabled(false);
			}
		}
	}

	/**
	 * Displays a set of readings on the numbersPanels and plots them on the plot.
	 * 
	 * @param readings
	 *            the array of readings.
	 */
	private void displayReadings(double[] readings) {
		for (int i = 0; i < numberOfChannels; i++) {
			currentValues[i].setText(nf.format(readings[i]));
			plot.addPointToLine(i, pointCounter, readings[i]);
			if (showHistory) {
				historyTxt.append(currentValues[i].getText());
				historyTxt.append("           ");
			}
		}
		pointCounter++;
		if (showHistory)
			historyTxt.append("\n");
	}

	/**
	 * Returns the counter timer name
	 * 
	 * @return the counter timer name
	 */
	public String getCounterTimerName() {
		return counterTimerName;
	}

	/**
	 * Sets the counter timer name
	 * 
	 * @param counterTimerName
	 *            the counter timer name
	 */
	public void setCounterTimerName(String counterTimerName) {
		this.counterTimerName = counterTimerName;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	/**
	 * @throws FactoryException
	 */
	@Override
	public void configure() throws FactoryException {
		nf = NumberFormat.getInstance();
		nf.setGroupingUsed(false);
		nf.setMaximumFractionDigits(decimalPlaces);
		nf.setMinimumFractionDigits(decimalPlaces);

		scriptingMediator = JythonServerFacade.getInstance();
		scriptingMediator = JythonServerFacade.getInstance();
		scriptingMediator.addIObserver(this);

		counterTimer = (Detector) Finder.getInstance().find(counterTimerName);

		numberOfChannels = counterTimer.getExtraNames().length;
		labelStrings = counterTimer.getExtraNames();

		createDisplayComponent();
		createControlComponent();
	}

	/**
	 * Creates the display component (i.e. the one which will appear in an internal frame in OEMove).
	 */
	private void createDisplayComponent() {
		JPanel tablePanel;
		JPanel plotPanel;
		JPanel historyPanel;

		tablePanel = new JPanel();
		tablePanel.add(createTable());

		plotPanel = new JPanel(new BorderLayout());
		plotPanel.add(createPlot(), BorderLayout.CENTER);

		initializePlot();
		// By default the Numbers display should govern the size of the plugin
		// otherwise huge plots appear overwhelming everything else.
		plotPanel.setPreferredSize(tablePanel.getPreferredSize());

		displayComponent = new JTabbedPane();
		displayComponent.addTab("Numbers", tablePanel);
		displayComponent.addTab("Plot", plotPanel);

		if (showHistory) {
			historyPanel = new JPanel(new BorderLayout());
			historyPanel.add(createHistoryPanel());
			displayComponent.addTab("History", historyPanel);
		}
	}

	/**
	 * Creates the label and field table.
	 * 
	 * @return the component
	 */
	private JComponent createTable() {
		JPanel extraPanel = new JPanel(new BorderLayout());

		// Use temporary JLabel to derive the fonts
		Font font = new JLabel().getFont();
		Font labelFont = font.deriveFont(Font.BOLD);
		Font valueFont = font.deriveFont((float) valueFontSize);

		labels = new JLabel[numberOfChannels];
		currentValues = new JTextField[numberOfChannels];

		JPanel leftColumn = new JPanel(new GridLayout(numberOfChannels, 0));
		JPanel rightColumn = new JPanel(new GridLayout(numberOfChannels, 0));

		for (int i = 0; i < numberOfChannels; i++) {
			labels[i] = new JLabel(" " + labelStrings[i] + " ", SwingConstants.TRAILING);
			labels[i].setFont(labelFont);
			currentValues[i] = new JTextField(8);
			labels[i].setLabelFor(currentValues[i]);
			leftColumn.add(labels[i]);
			currentValues[i].setOpaque(true);
			currentValues[i].setForeground(Color.green);
			currentValues[i].setBackground(Color.black);
			currentValues[i].setFont(valueFont);
			currentValues[i].setEditable(false);
			currentValues[i].setHorizontalAlignment(SwingConstants.TRAILING);
			rightColumn.add(currentValues[i]);
		}

		extraPanel.add(leftColumn, BorderLayout.WEST);
		extraPanel.add(rightColumn, BorderLayout.CENTER);

		return extraPanel;
	}

	/**
	 * Creates the JPanel with header and scroll pane of previous readings for history table.
	 * 
	 * @return the component
	 */
	private JComponent createHistoryPanel() {
		JPanel historyPanel = new JPanel(new BorderLayout());
		JPanel headerPanel = new JPanel(new GridLayout());

		JScrollPane sp = new JScrollPane(this.historyTxt, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		sp.setAutoscrolls(true);
		sp.setDoubleBuffered(true);

		// Use temporary JLabel to derive the fonts
		Font font = new JLabel().getFont();
		JLabel[] labels = new JLabel[numberOfChannels];
		Font labelFont = font.deriveFont(Font.BOLD, 10);
		sp.setFont(font.deriveFont(Font.LAYOUT_RIGHT_TO_LEFT, 12));

		// assumes createPanel() already called and labelStrings already initialized
		for (int i = 0; i < numberOfChannels; i++) {
			labels[i] = new JLabel(labelStrings[i], SwingConstants.CENTER);
			labels[i].setFont(labelFont);

			headerPanel.add(labels[i], 0, i);
		}

		historyPanel.add(headerPanel, BorderLayout.NORTH);
		historyPanel.add(sp, BorderLayout.CENTER);

		return historyPanel;
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
		for (int i = 0; i < isSelected.length; i++) {
			plot.setLineVisibility(i, isSelected[i]);
		}
	}

	/**
	 * Creates the plot and its accompanying WhatToPlotDialog.
	 * 
	 * @return the component
	 */
	private JComponent createPlot() {
		plot = new SimplePlot();
		plot.setTitleVisible(false);
		plot.setYAxisLabel("Counts");
		plot.setXAxisLabel("Point Number");

		// Create the WhatToPlotDialog and switch on all the lines
		ArrayList<String> labels = new ArrayList<String>();
		for (String label : labelStrings){
			labels.add(label);
		}
		whatToPlotDialog = new WhatToPlotDialog(this, labels);
		for (int i = 0; i < labelStrings.length; i++)
			whatToPlotDialog.setSelected(i, true);

		// Add a separator and a button to operate the WhatToPlotDialog
		// to the plot's popup menu.
		plot.addPopupMenuItem(new JSeparator());
		JMenuItem whatToPlotButton = new JMenuItem("Plot shows...");
		whatToPlotButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				whatToPlotDialog.setVisible(true);
			}
		});
		plot.addPopupMenuItem(whatToPlotButton);

		return plot;
	}

	/**
	 * Creates the control component (i.e. the one which will appear in the control area of OEMove).
	 */
	private void createControlComponent() {
		controlComponent = new JPanel();
		controlComponent.setLayout(new FlowLayout());

		countingTimeField = new JTextField(16);
		countingTimeField.setText("1000.0");
		countingTimeField.setHorizontalAlignment(SwingConstants.CENTER);
		countingTimeField.setFont(new Font("Monospaced", Font.BOLD, 14));
		countingTimeField.setToolTipText("Counting time");
		countingTimeField.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
				"CountingTime(mS)", TitledBorder.CENTER, TitledBorder.TOP, null, Color.black));
		controlComponent.add(countingTimeField);

		startButton = new JButton("Start");
		startButton.setToolTipText("Start monitoring " + counterTimerName);
		startButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				startMonitoring();
			}
		});
		controlComponent.add(startButton);

		stopButton = new JButton("Stop");
		stopButton.setToolTipText("Stop monitoring " + counterTimerName);
		stopButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				stopMonitoring();
			}
		});
		controlComponent.add(stopButton);
	}

	/**
	 * Gets the number of decimal places currently specified for the NumberFormat.
	 * 
	 * @return the number of decimal places
	 */
	public int getDecimalPlaces() {
		return decimalPlaces;
	}

	/**
	 * Sets the number of decimal places to be used by the NumberFormat.
	 * 
	 * @param decimalPlaces
	 *            the number of decimal places
	 */
	public void setDecimalPlaces(int decimalPlaces) {
		this.decimalPlaces = decimalPlaces;
	}

	/**
	 * Returns the value font size.
	 * 
	 * @return the value font size
	 */
	public int getValueFontSize() {
		return valueFontSize;
	}

	/**
	 * Sets the size of font used in the value displaying fields
	 * 
	 * @param valueFontSize
	 *            the font size
	 */
	public void setValueFontSize(int valueFontSize) {
		this.valueFontSize = valueFontSize;
	}

	/**
	 * Intializes the plot by initializing the relevant number of lines.
	 */
	private void initializePlot() {
		for (int i = 0; i < numberOfChannels; i++) {
			plot.initializeLine(i);
			plot.setLineName(i, labelStrings[i]);
		}
	}

	/**
	 * Implements Runnable, this is where the monitoring is actually carried out by the by now well respected BFI
	 * method.
	 */
	@Override
	public void run() {
		logger.debug("Monitoring CounterTimer " + counterTimerName);

		while (carryOnMonitoring) {
			try {
				counterTimer.setCollectionTime(countingTime/1000.0);
				counterTimer.readout();
				// The additional carryOnMonitoring in the while condition means
				// that the stop button will work after 10mS rather than having
				// to wait for a whole timing loop - this may still not be good
				// enough.
				do {
					synchronized (this) {
						wait(10);
					}
				} while (counterTimer.getStatus() == Detector.BUSY && carryOnMonitoring);
				// Only display the reads if carrying on (otherwise you
				// can get spurious values from some types of Counter).
				if (carryOnMonitoring) {
					displayReadings((double[]) counterTimer.readout());
				}
			} catch (DeviceException e) {
				carryOnMonitoring = false;
			} catch (InterruptedException e) {
				carryOnMonitoring = false;
			}

		}

		logger.debug("Stopped monitoring CounterTimer " + counterTimerName);
		if (statusAllowsMonitoring) {
			startButton.setEnabled(true);
		}
	}

}
