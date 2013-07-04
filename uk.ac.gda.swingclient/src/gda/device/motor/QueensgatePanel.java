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

package gda.device.motor;

import gda.device.DeviceException;
import gda.device.Motor;
import gda.device.MotorException;
import gda.device.MotorStatus;
import gda.device.motor.corba.impl.MotorAdapter;
import gda.factory.Finder;
import gda.gui.AcquisitionPanel;
import gda.observable.IObserver;
import gda.observable.UpdateDelayer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.NumberFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to provide a GUI to control a Queensgate piezo
 */
public class QueensgatePanel extends AcquisitionPanel {
	
	private static final Logger logger = LoggerFactory.getLogger(QueensgatePanel.class);
	
	/**
	 * Inner class which extends JTextField so that it can display 'e' and 'c' for 'expand' and 'contract'
	 */
	private class OffsetDisplayer extends JTextField {
		/**
		 * constructor
		 * 
		 * @param text
		 *            the string to be displayed intially
		 */
		public OffsetDisplayer(String text) {
			super(text);
		}

		/**
		 * Displays the specified value as an offset
		 * 
		 * @param value
		 *            the value to be displayed
		 */
		public void setValue(double value) {
			int absValue;
			String newText;

			absValue = Math.abs((int) value);
			newText = String.valueOf(absValue);

			if (value < 0)
				newText = "e" + newText;
			else if (value > 0)
				newText = "c" + newText;

			super.setText(newText);
		}
	}

	/**
	 * Inner class to implement GUI functionality for an individual Queensgate Device.
	 */
	private class QueensgatePanelDevice implements ActionListener, ChangeListener, DocumentListener, IObserver {
		private String motorName;

		private Motor queensgate;

		private JLabel motorNameLabel;

		private JButton expandButton;

		private JButton contractButton;

		private JFormattedTextField sizeOfMoveField;

		private OffsetDisplayer offsetField;

		private JTextField voltageField;

		private JSlider sizeOfMoveSlider;

		private int currentMoveSize = 1;

		private JPanel deviceSubPanel;

		/*
		 * public static final int MIN_MOVE = 0; public static final int MAX_MOVE = 100; public static final int
		 * INIT_MOVE = 50;
		 */
		// TODO - Do we want 0-8192 move range on slider?
		/**
		 * min range on slider
		 */
		public static final int MIN_MOVE = 0;

		/**
		 * max range on slider
		 */
		public static final int MAX_MOVE = 8192;

		/**
		 * 
		 */
		public static final int INIT_MOVE = 0;

		/**
		 * 
		 */
		public static final int MAJOR_TICK_SPACING = 2048/* 10 */;

		/**
		 * 
		 */
		public static final int MINOR_TICK_SPACING = 1024/* 1 */;

		private synchronized void updatePositionAndStatus() {
			try {
				if ((queensgate != null)
						&& ((queensgate instanceof Queensgate) || (queensgate instanceof DummyQueensgate) || (queensgate instanceof MotorAdapter))
				// N.B. this test wont work, since qg name
				// = eg "dev_7_6_C21.An_FTCPiezo"
				// and motorName = eg "An_FTCPiezo"
				// && queensgate.getName().equals(motorName)
				) {
					if (queensgate.getStatus() == MotorStatus.READY) {
						offsetField.setBackground(Color.green);
						offsetField.setForeground(Color.black);
					} else {
						offsetField.setBackground(Color.red);
						offsetField.setForeground(Color.white);
					}

					offsetField.setValue(queensgate.getPosition());
					Double fpv = (Double) queensgate.getAttribute("FrontPanelVoltage");
					displayFPV(fpv.doubleValue());
				}
			} catch (MotorException me) {
				logger.error("QueensgatePanel: Exception caught getting position.");
			} catch (DeviceException de) {
				logger.error("QueensgatePanel: Exception caught getting FPV attribute.");
			}
		}

		/**
		 * @param motorName
		 */
		public QueensgatePanelDevice(String motorName) {
			this.motorName = motorName;

			jbInit();
		}

		/**
		 * @return motor
		 */
		public Motor getMotor() {
			return queensgate;
		}

		/**
		 * @param motor
		 */
		public void setMotor(Motor motor) {
			queensgate = motor;

			// N.B. this wont work, since motor.getName() returns
			// eg "dev_7_6_C21.An_FTCPiezo"
			// and motorName = eg "An_FTCPiezo"
			// Update name and text label for this device
			// motorName = motor.getName();
			// motorNameLabel.setText(motorName);

			// Setup sub panel as IObserver of the QG device, via the
			// UpdateDelayer
			new UpdateDelayer(this, motor);

			updatePositionAndStatus();
		}

		/**
		 * Method required for editing GUI in JBuilder. Sets out the display for the Panel.
		 */
		private void jbInit() {
			java.text.NumberFormat numberFormat = java.text.NumberFormat.getIntegerInstance();
			numberFormat.setGroupingUsed(false);
			NumberFormatter formatter = new NumberFormatter(numberFormat);
			formatter.setMinimum(new Integer(MIN_MOVE));
			formatter.setMaximum(new Integer(MAX_MOVE));

			sizeOfMoveField = new JFormattedTextField(formatter);
			sizeOfMoveField.setValue(new Integer(INIT_MOVE));
			sizeOfMoveField.setColumns(5);
			sizeOfMoveField.setHorizontalAlignment(SwingConstants.CENTER);
			sizeOfMoveField.addActionListener(this);
			sizeOfMoveField.getDocument().addDocumentListener(this);

			sizeOfMoveSlider = new JSlider(SwingConstants.HORIZONTAL, MIN_MOVE, MAX_MOVE, INIT_MOVE);
			sizeOfMoveSlider.addChangeListener(this);
			sizeOfMoveSlider.setPaintTicks(true);
			sizeOfMoveSlider.setMajorTickSpacing(MAJOR_TICK_SPACING);
			sizeOfMoveSlider.setMinorTickSpacing(MINOR_TICK_SPACING);
			sizeOfMoveSlider.setPaintLabels(true);

			setLayout(new BorderLayout());
			setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Queensgate piezo device",
					TitledBorder.LEFT, TitledBorder.TOP, null, Color.black));

			deviceSubPanel = new JPanel(new GridBagLayout());
			deviceSubPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), null,
					TitledBorder.LEFT, TitledBorder.TOP, null, Color.black));

			GridBagConstraints c = new GridBagConstraints();

			c.fill = GridBagConstraints.BOTH;
			c.weightx = 1.0;
			c.weighty = 0.0;

			// Add Device Name label
			c.gridwidth = GridBagConstraints.REMAINDER;
			motorNameLabel = new JLabel(motorName);
			deviceSubPanel.add(motorNameLabel, c);

			// Set out the Offset display
			c.gridwidth = GridBagConstraints.RELATIVE;
			deviceSubPanel.add(new JLabel("Offset position"), c);

			c.gridwidth = GridBagConstraints.REMAINDER;
			offsetField = new OffsetDisplayer(null);
			offsetField.setEditable(false);
			offsetField.setBackground(Color.BLACK);
			offsetField.setForeground(Color.GREEN);

			offsetField.setHorizontalAlignment(SwingConstants.RIGHT);
			deviceSubPanel.add(offsetField, c);

			// Set out the voltage display
			c.gridwidth = GridBagConstraints.RELATIVE;
			deviceSubPanel.add(new JLabel("Expected Front Panel Voltage"), c);

			c.gridwidth = GridBagConstraints.REMAINDER;
			voltageField = new JTextField("0.0");
			voltageField.setEditable(false);
			voltageField.setBackground(Color.BLACK);
			voltageField.setForeground(Color.GREEN);
			voltageField.setHorizontalAlignment(SwingConstants.RIGHT);
			deviceSubPanel.add(voltageField, c);

			// Set out the movement buttons and text field
			c.gridwidth = 1;
			contractButton = new JButton("Contract");
			contractButton.addActionListener(this);
			deviceSubPanel.add(contractButton, c);

			c.gridwidth = GridBagConstraints.RELATIVE;
			deviceSubPanel.add(sizeOfMoveField, c);

			c.gridwidth = GridBagConstraints.REMAINDER;
			expandButton = new JButton("Expand");
			expandButton.addActionListener(this);
			deviceSubPanel.add(expandButton, c);

			// Set out the slider
			c.weightx = 0.0; // reset to the default
			deviceSubPanel.add(sizeOfMoveSlider, c);

			// Set size of sub panel
			deviceSubPanel.setSize(260/* 400 */, 100/* 100 */);
			deviceSubPanel.validate();

			// setup constraints to get subpanel into background panel
			c.gridx = GridBagConstraints.RELATIVE;
			c.gridy = GridBagConstraints.RELATIVE;

			c.gridwidth = GridBagConstraints.REMAINDER;
			c.gridheight = GridBagConstraints.RELATIVE;

			c.weightx = 0;
			c.weighty = 0;

			c.anchor = GridBagConstraints.NORTH;
			c.fill = GridBagConstraints.HORIZONTAL;

			// add deviceSubPanel into parent class backgroundPanel
			backgroundPanel.add(deviceSubPanel, c);
		}

		/**
		 * Executes an expansion
		 */
		private void executeExpand() {
			logger.debug("QueensgatePanel expanding to " + (-currentMoveSize));
			try {
				queensgate.moveTo(-currentMoveSize);
			} catch (MotorException de) {
				logger.debug("Expand caused device exception " + de);
			}
		}

		/**
		 * Executes a contraction
		 */
		private void executeContract() {
			logger.debug("QueensgatePanel contracting to " + currentMoveSize);
			try {
				queensgate.moveTo(currentMoveSize);
			} catch (MotorException de) {
				logger.debug("Contract caused device exception " + de);
			}
		}

		/**
		 * Implements the ChangeListener interface to react to changes to the slider position
		 * 
		 * @param evt
		 *            the change event
		 */
		@Override
		public void stateChanged(ChangeEvent evt) {
			JSlider source = (JSlider) evt.getSource();

			if (source == sizeOfMoveSlider) {
				getMoveSizeFromSlider();
				setFieldFromMoveSize();
			}
		}

		/**
		 * Sets the JSlider when the move size has been changed by the JTextField.
		 */
		private void setSliderFromMoveSize() {
			int value;
			logger.debug("setSliderFromMoveSize called with " + currentMoveSize);

			// TODO Do we want displayed movesize same as currentMoveSize?
			value = currentMoveSize;// (int) Math.sqrt((currentMoveSize - 1)
			// *
			// 10.0);
			logger.debug("corresponding value is " + value);
			sizeOfMoveSlider.removeChangeListener(this);
			sizeOfMoveSlider.setValue(value);
			sizeOfMoveSlider.addChangeListener(this);
		}

		/**
		 * Sets the JTextField when the move size has been changed by the slider.
		 */
		private void setFieldFromMoveSize() {
			logger.debug("setFieldFromMoveSize called with " + currentMoveSize);

			sizeOfMoveField.removeActionListener(this);
			sizeOfMoveField.getDocument().removeDocumentListener(this);
			sizeOfMoveField.setText(String.valueOf(currentMoveSize));
			sizeOfMoveField.addActionListener(this);
			sizeOfMoveField.getDocument().addDocumentListener(this);
		}

		/**
		 * Implements the ActionListener interface
		 * 
		 * @param event
		 *            the action event
		 */
		@Override
		public void actionPerformed(ActionEvent event) {
			logger.debug("actionPerformed called with ");

			if (event.getSource() == expandButton)
				executeExpand();
			else if (event.getSource() == contractButton)
				executeContract();
			else if (event.getSource() == sizeOfMoveField)
				textFieldChanged();
		}

		/**
		 * Gets the move size from the JSlider
		 */
		private void getMoveSizeFromSlider() {
			int value;

			value = sizeOfMoveSlider.getValue();
			logger.debug("slider value is " + value);

			// TODO Do we want displayed movesize same as currentMoveSize?
			currentMoveSize = value;// 1 + (value * value) / 10;
			logger.debug("corresponding move size is " + currentMoveSize);
		}

		/**
		 * Gets the move size from the JTextField
		 */
		private void getMoveSizeFromTextField() {
			try {
				currentMoveSize = Integer.parseInt(sizeOfMoveField.getText());
			} catch (NumberFormatException nfe) {
				logger.debug("NumberFormatException " + nfe + " caught");
				currentMoveSize = 0;
			}
		}

		/**
		 * Gets the move size from the JTextField and sets it on the slider.
		 */
		private void textFieldChanged() {
			getMoveSizeFromTextField();
			setSliderFromMoveSize();
		}

		/**
		 * Called when the observed JTextField changes, required to implement DocumentListener interface
		 * 
		 * @param evt
		 *            the document changed event
		 */
		@Override
		public void changedUpdate(DocumentEvent evt) {
			logger.debug("changedUpdate called ");
			textFieldChanged();
		}

		/**
		 * Called when the observed JTextField changes, required to implement DocumentListener interface
		 * 
		 * @param evt
		 *            the insert document event
		 */
		@Override
		public void insertUpdate(DocumentEvent evt) {
			logger.debug("insertUpdate called ");
			textFieldChanged();
		}

		/**
		 * Called when the observed JTextField changes, required to implement DocumentListener interface
		 * 
		 * @param evt
		 *            the remove document changed event
		 */
		@Override
		public void removeUpdate(DocumentEvent evt) {
			logger.debug("removeUpdate called ");
			textFieldChanged();
		}

		/**
		 * Implements the IObserver interface
		 * 
		 * @param obs
		 *            the calling IObservable
		 * @param arg
		 *            the argument passed
		 */
		@Override
		public synchronized void update(Object obs, Object arg) {
			logger.debug("QueensgatePanel IObserver update called");
			logger.debug(" ");

			try {
				if ((obs instanceof Motor)
						&& ((obs instanceof Queensgate) || (obs instanceof DummyQueensgate) || (obs instanceof MotorAdapter))
				// N.B. this test wont work, since qg name
				// = eg "dev_7_6_C21.An_FTCPiezo"
				// and motorName = eg "An_FTCPiezo"
				// && queensgate.getName().equals(motorName)
				) {
					if (queensgate.getStatus() == MotorStatus.READY) {
						offsetField.setBackground(Color.green);
						offsetField.setForeground(Color.black);
					} else {
						offsetField.setBackground(Color.red);
						offsetField.setForeground(Color.white);
					}

					offsetField.setValue(queensgate.getPosition());
					Double fpv = (Double) queensgate.getAttribute("FrontPanelVoltage");
					displayFPV(fpv.doubleValue());
				}
			} catch (MotorException me) {
				logger.error("QueensgatePanel: Exception caught getting position.");
			} catch (DeviceException de) {
				logger.error("QueensgatePanel: Exception caught getting FPV attribute.");
			}
		}

		/**
		 * Displays the expected front panel voltage
		 * 
		 * @param fpv
		 *            the voltage to display
		 */
		private void displayFPV(double fpv) {
			NumberFormat numform;

			numform = NumberFormat.getInstance();

			numform.setMinimumFractionDigits(2);
			numform.setMaximumFractionDigits(2);
			voltageField.setText(numform.format(fpv));
		}

	}

	// Queensgate controller can have three devices
	private String motorNameA;

	private String motorNameB;

	private String motorNameC;

	// private QueensgatePanelDevice subPanelDeviceA = null;
	// private QueensgatePanelDevice subPanelDeviceB = null;
	// private QueensgatePanelDevice subPanelDeviceC = null;

	private JPanel backgroundPanel;

	/**
	 * Constructor
	 */
	public QueensgatePanel() {
		jbInit();
	}

	/**
	 * Method required for editing GUI in JBuilder. Sets out the display for the Panel.
	 */
	private void jbInit() {
		setLayout(new BorderLayout());
		setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Queensgate piezo",
				TitledBorder.LEFT, TitledBorder.TOP, null, Color.black));

		backgroundPanel = new JPanel(new GridBagLayout());
		backgroundPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), null,
				TitledBorder.LEFT, TitledBorder.TOP, null, Color.black));

		// add backgroundPanel into AcquisitionPanel GUI
		backgroundPanel.setSize(260/* 400 */, 600/* 100 */);

		// README - The constraint specified here seems to get ignored
		// as the adding of subpanels by QueensgatePanelDevice is using
		// different
		// constraints, which seems to screw up the constraint of the parent.
		add(backgroundPanel, BorderLayout.WEST);
	}

	/**
	 * @return motor name A
	 */
	public String getMotorNameA() {
		return motorNameA;
	}

	/**
	 * @return motor name B
	 */
	public String getMotorNameB() {
		return motorNameB;
	}

	/**
	 * @return motor name C
	 */
	public String getMotorNameC() {
		return motorNameC;
	}

	/**
	 * @param motorName
	 */
	public void setMotorNameA(String motorName) {
		motorNameA = motorName;
	}

	/**
	 * @param motorName
	 */
	public void setMotorNameB(String motorName) {
		motorNameB = motorName;
	}

	/**
	 * @param motorName
	 */
	public void setMotorNameC(String motorName) {
		motorNameC = motorName;
	}

	private QueensgatePanelDevice addSubPanel(String motorName) {
		QueensgatePanelDevice subPanelDevice = null;

		if ((motorName != "") && (motorName != null)) {
			Motor queensgate = (Motor) Finder.getInstance().find(motorName);

			// N.B. this test wont work, since qg name = eg
			// "dev_7_6_C21.An_FTCPiezo"
			// and motorName = eg "An_FTCPiezo"
			// if(queensgate.getName().equals(motorName))
			{
				subPanelDevice = new QueensgatePanelDevice(motorName);

				logger.error("QueensgatePanel: The Queensgate device is " + motorName);

				// Assign motor to Queensgate sub panel
				subPanelDevice.setMotor(queensgate);
			}
		}

		return subPanelDevice;
	}

	@Override
	public void configure() {
		try {
			// try to find each device and hook it up to a Gui subpanel
			// subPanelDeviceA = addSubPanel(motorNameA);
			// subPanelDeviceB = addSubPanel(motorNameB);
			// subPanelDeviceC = addSubPanel(motorNameC);
			addSubPanel(motorNameA);
			addSubPanel(motorNameB);
			addSubPanel(motorNameC);
		} catch (Exception e) {
			logger.error("Queensgate panel: Exception occurred" + e.getMessage());
			logger.debug(e.getStackTrace().toString());
		}
	}

}