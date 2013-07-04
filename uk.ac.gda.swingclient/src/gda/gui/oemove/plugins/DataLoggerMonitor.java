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

// package gda.device.datalogger;

package gda.gui.oemove.plugins;

import gda.device.DataLogger;
import gda.device.DeviceException;
import gda.factory.Configurable;
import gda.factory.FactoryException;
import gda.factory.Findable;
import gda.factory.Finder;
import gda.gui.oemove.Pluggable;
import gda.observable.IObserver;
import gda.observable.UpdateDelayer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Monitor Panel for a Data Logger that implements Pluggable thereby appearing as a free floating panel in OEMovePanel.
 * Observes the dataLogger and updates the display fields at regular intervals. Currently displays 8 channels of
 * information.
 */
public class DataLoggerMonitor extends JPanel implements IObserver, Configurable, Findable, Pluggable {
	
	private static final Logger logger = LoggerFactory.getLogger(DataLoggerMonitor.class);
	
	private JTextField noOfChannelsField;

	private JTextField[] channelFields;

	private JTextField portField;

	private JTextField powerField;

	private JLabel portLabel;

	private JLabel noOfChannelsLabel;

	private JButton startButton;

	private JButton stopButton;

	private JPanel controlPanel;

	private JPanel displayPanel;

	private JPanel gridPanel;

	private DataLogger dataLogger;

	private final int CHANNELS = 8;

	private int noOfChannels = CHANNELS;

	private ArrayList<String> labelList;

	private String loggerName;

	private JPanel controlComponent = new JPanel();

	/**
	 * Default constructor, getter and setter methods for CASTOR
	 */
	public DataLoggerMonitor() {
	}

	@Override
	public JPanel getDisplayComponent() {
		return this;
	}

	@Override
	public JPanel getControlComponent() {
		return controlComponent;
	}

	/**
	 * @return Returns the labelList.
	 */
	public ArrayList<String> getLabelList() {
		return labelList;
	}

	/**
	 * @param labelList
	 *            The labelList to set.
	 */
	public void setLabelList(ArrayList<String> labelList) {
		this.labelList = labelList;
	}

	/**
	 * @return Returns the loggerName.
	 */
	public String getLoggerName() {
		return loggerName;
	}

	/**
	 * @param loggerName
	 *            The loggerName to set.
	 */
	public void setLoggerName(String loggerName) {
		this.loggerName = loggerName;
	}

	// **** Methods for the DataLoggerMonitor itself **** //

	/**
	 * Adds a new label and a text field to a panel
	 * 
	 * @param panel
	 * @param labelName
	 * @param textField
	 */
	private void addComponents(JPanel panel, String labelName, JTextField textField) {
		panel.add(new JLabel(labelName));
		panel.add(textField);
	}

	/**
	 * The configure method finds the DataLogger to observe and sets the Layout of the main and component panels. Adds
	 * an UpdateDelayer and gets the number of Channels from the attached logger device.
	 * 
	 * @throws FactoryException
	 */
	@SuppressWarnings("unused")
	@Override
	public void configure() throws FactoryException {
		dataLogger = (gda.device.DataLogger) Finder.getInstance().find(loggerName);
		logger.debug("The data logger being configured is " + dataLogger);

		// README The panels must be created before communication with the
		// Data Logger updates the values
		setLayout(new BorderLayout());
		add(getControlPanel(), BorderLayout.NORTH);
		add(getDisplayPanel(), BorderLayout.CENTER);

		if (dataLogger != null) {
			new UpdateDelayer(this, dataLogger);

			powerField.setBackground(Color.GREEN);
			try {
				noOfChannels = dataLogger.getNoOfChannels();
				noOfChannelsField.setText("" + noOfChannels);
			} catch (DeviceException e) {
				JOptionPane.showMessageDialog(displayPanel,
						"DataLoggerMonitor: Exception occurred configuring the data logger. " + e.getMessage());
			}
		}
	}

	/**
	 * Iterates through the given double array, setting text fields to correspond to the values within the array.
	 * 
	 * @param values
	 *            The double array of values from the dataLogger
	 */
	private void displayValues(double[] values) {
		for (int i = 0; i < values.length; i++) {
			channelFields[i].setText(Double.toString(values[i]));
		}
	}

	/**
	 * Iterates through the given String array, setting text fields to the text within the array.
	 * 
	 * @param values
	 *            The String array of values from the dataLogger
	 */
	private void displayValues(String[] values) {
		for (int i = 0; i < values.length; i++) {
			channelFields[i].setText(values[i]);
		}
	}

	/**
	 * Creates and sets layout for the panel that controls and shows communication status with the attached logger
	 * device.
	 * 
	 * @return controlPanel
	 */
	private JPanel getControlPanel() {
		controlPanel = new JPanel();
		controlPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Data Logger",
				TitledBorder.LEFT, TitledBorder.TOP, null, Color.black));
		controlPanel.setLayout(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 20;
		c.gridy = 0;
		c.weightx = 0.5;
		c.gridx = GridBagConstraints.RELATIVE;
		c.anchor = GridBagConstraints.WEST;

		powerField = new JTextField("CONNECTED");
		powerField.setHorizontalAlignment(SwingConstants.CENTER);
		powerField.setEditable(false);
		powerField.setBackground(Color.BLACK);

		noOfChannelsLabel = new JLabel("Channels");
		noOfChannelsField = new JTextField(5);
		noOfChannelsField.setHorizontalAlignment(SwingConstants.CENTER);
		noOfChannelsField.setEditable(false);
		noOfChannelsField.setBackground(Color.WHITE);

		portLabel = new JLabel("Port ID");
		portField = new JTextField(5);
		portField.setText("ComPort");
		portField.setEditable(false);
		portField.setBackground(Color.WHITE);

		startButton = new JButton("Start");
		startButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				if (dataLogger != null) {
					try {
						dataLogger.connect();
						powerField.setBackground(Color.GREEN);
					} catch (DeviceException e) {
						JOptionPane.showMessageDialog(displayPanel,
								"An exception occurred while connecting to the data logger." + e.getMessage());
					}
				} else {
					JOptionPane.showMessageDialog(displayPanel, "The data logger is not connected.");
				}
			}
		});

		stopButton = new JButton("Stop");
		stopButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				if (dataLogger != null) {
					terminateConnection();
					powerField.setBackground(Color.BLACK);
				} else {
					JOptionPane.showMessageDialog(displayPanel, "The data logger is not connected.");
				}
			}
		});

		controlPanel.add(portLabel, c);
		controlPanel.add(portField, c);
		c.gridy++;
		controlPanel.add(noOfChannelsLabel, c);
		controlPanel.add(noOfChannelsField, c);
		c.gridy++;
		c.gridwidth = 2;
		controlPanel.add(startButton, c);
		c.gridy++;
		controlPanel.add(powerField, c);
		c.gridy++;
		controlPanel.add(stopButton, c);
		return controlPanel;
	}

	/**
	 * Creates and sets layout for displaying the readouts from the attached logger device
	 * 
	 * @return resultPanel
	 */
	private JPanel getDisplayPanel() {
		displayPanel = new JPanel();
		displayPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Display values",
				TitledBorder.LEFT, TitledBorder.TOP, null, Color.black));
		displayPanel.setLayout(new BorderLayout());

		gridPanel = new JPanel();
		gridPanel.setLayout(new GridLayout(0, 2));

		channelFields = new JTextField[noOfChannels];
		for (int i = 0; i < noOfChannels; i++) {
			channelFields[i] = new JTextField(5);
			channelFields[i].setEditable(false);
			channelFields[i].setBackground(Color.WHITE);
		}
		for (int i = 0; i < channelFields.length; i++) {
			String label = getLabelString(i);
			addComponents(gridPanel, label, channelFields[i]);
		}

		displayPanel.add(gridPanel, BorderLayout.NORTH);
		return displayPanel;
	}

	/**
	 * Gets a String from the ArrayList<String> to use on the display labels
	 * 
	 * @param i
	 *            the index position
	 * @return The String at the given index OR a default label
	 */
	private String getLabelString(int i) {
		String labelString = null;
		if (i >= labelList.size()) {
			labelString = "Channel " + (i + 1);
		} else {
			labelString = "" + labelList.get(i);
		}
		return labelString;
	}

	/**
	 * 
	 */
	public void terminateConnection() {
		try {
			dataLogger.disconnect();
		} catch (DeviceException e) {
			JOptionPane.showMessageDialog(displayPanel, "Error in closing DataLogger Connection, " + e.getMessage());
		}
	}

	@Override
	public void update(Object theObserved, Object arg) {
		if (arg instanceof String[]) {
			displayValues((String[]) arg);
		} else if (arg instanceof double[]) {
			displayValues((double[]) arg);
		} else if (arg instanceof DeviceException) {
			JOptionPane.showMessageDialog(displayPanel,
					"An exception occurred while displaying values from the data logger. "
							+ ((DeviceException) arg).getMessage());
		}
	}
}