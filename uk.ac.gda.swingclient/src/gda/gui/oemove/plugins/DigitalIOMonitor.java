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

import gda.device.DeviceException;
import gda.device.DigitalIO;
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

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DigitalIOMonitor Class
 */
public class DigitalIOMonitor extends JPanel implements IObserver, Configurable, Findable, Pluggable {
	
	private static final Logger logger = LoggerFactory.getLogger(DigitalIOMonitor.class);
	
	// README may need to add a field for this to xml file if ever more than
	// one
	private static final String CHANNEL_NAME = "chan1";

	private static final String HIGH = "HIGH";

	private static final String LOW = "LOW";

	private JLabel stateLabel;

	private JLabel lastStateSentLabel;

	private JLabel selectStateLabel;

	private JTextField lastStateSentField;

	private JTextField stateField;

	private JComboBox selectStateCombo;

	private JButton getStateButton;

	private JButton sendStateButton;

	private JPanel controlPanel;

	private JPanel displayPanel;

	private JPanel gridPanel;

	private JPanel controlComponent = new JPanel();

	private DigitalIO digitalIO;

	private String ioName;

	private int state = -1;

	/**
	 * Default constructor, getter and setter methods for CASTOR
	 */
	public DigitalIOMonitor() {
	}

	/**
	 * @param ioName
	 */
	public void setIoName(String ioName) {
		this.ioName = ioName;
	}

	/**
	 * @return io name
	 */
	public String getIoName() {
		return ioName;
	}

	@SuppressWarnings("unused")
	@Override
	public void configure() throws FactoryException {
		digitalIO = (gda.device.DigitalIO) Finder.getInstance().find(ioName);
		logger.debug("The digital io being configured is " + ioName);

		// README The panels must be created before communication with the
		// Digital IO updates the values
		setLayout(new BorderLayout());
		add(createDisplayPanel(), BorderLayout.NORTH);
		add(createControlPanel(), BorderLayout.CENTER);

		if (digitalIO != null) {
			new UpdateDelayer(this, digitalIO);

			try {
				getAndDisplayState();
			} catch (Exception e) {
				JOptionPane.showMessageDialog(displayPanel,
						"DigitalIOMonitor: Exception occurred configuring the digital io. " + e.getMessage());
			}
		} else {
			logger.error("DigitalIoMonitor: DigitalIO device not found");
		}
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
	 * Creates and sets layout for the panel that controls and shows communication status with the attached logger
	 * device.
	 * 
	 * @return controlPanel
	 */
	private JPanel createControlPanel() {
		controlPanel = new JPanel();
		controlPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Digital IO",
				TitledBorder.LEFT, TitledBorder.TOP, null, Color.black));
		controlPanel.setLayout(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 20;
		c.gridy = 0;
		c.weightx = 0.5;
		c.gridx = GridBagConstraints.RELATIVE;
		c.anchor = GridBagConstraints.WEST;

		selectStateLabel = new JLabel("Select state to send");
		selectStateCombo = new JComboBox();
		selectStateCombo.addItem(HIGH);
		selectStateCombo.addItem(LOW);

		sendStateButton = new JButton("Send State");
		sendStateButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				if (digitalIO != null) {
					sendAndDisplayState();
				} else {
					JOptionPane.showMessageDialog(displayPanel, "The digital io is not connected.");
				}
			}
		});

		getStateButton = new JButton("Get State");
		getStateButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				if (digitalIO != null) {
					getAndDisplayState();
				} else {
					JOptionPane.showMessageDialog(displayPanel, "The digital io is not connected.");
				}
			}
		});

		controlPanel.add(selectStateLabel, c);
		controlPanel.add(selectStateCombo, c);
		c.gridy++;

		controlPanel.add(sendStateButton, c);
		controlPanel.add(getStateButton, c);
		return controlPanel;
	}

	/**
	 * Creates and sets layout for displaying the state from the attached digital io device
	 * 
	 * @return resultPanel
	 */
	private JPanel createDisplayPanel() {
		displayPanel = new JPanel();
		displayPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Current IO state",
				TitledBorder.LEFT, TitledBorder.TOP, null, Color.black));
		displayPanel.setLayout(new BorderLayout());

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 0.5;
		c.gridx = GridBagConstraints.RELATIVE;
		c.anchor = GridBagConstraints.WEST;

		stateLabel = new JLabel("Current state");
		stateField = new JTextField("OPEN");
		stateField.setHorizontalAlignment(SwingConstants.CENTER);
		stateField.setEditable(false);
		stateField.setBackground(Color.BLACK);

		lastStateSentLabel = new JLabel("Last state sent");
		lastStateSentField = new JTextField(5);
		lastStateSentField.setText("None");
		lastStateSentField.setEditable(false);
		lastStateSentField.setForeground(Color.GREEN);
		lastStateSentField.setBackground(Color.BLACK);

		gridPanel = new JPanel();
		gridPanel.setLayout(new GridLayout(2, 2));
		gridPanel.add(stateLabel, c);
		gridPanel.add(stateField, c);
		c.gridy++;
		c.gridy++;
		gridPanel.add(lastStateSentLabel, c);
		gridPanel.add(lastStateSentField, c);

		displayPanel.add(gridPanel, BorderLayout.NORTH);
		return displayPanel;
	}

	private void getAndDisplayState() {
		try {
			state = digitalIO.getState(CHANNEL_NAME);

			if (state < 0) {
				stateField.setBackground(Color.BLACK);
				lastStateSentField.setText("ERROR");
				JOptionPane.showMessageDialog(displayPanel,
						"DigitalIOMonitor: Exception occurred getting digital io state.");
				throw new DeviceException("Error reply from Digital IO: " + state);
			} else if (state == DigitalIO.HIGH_STATE) {
				stateField.setBackground(Color.GREEN);
				stateField.setText("OPEN");
				lastStateSentField.setText(HIGH);
			} else {
				stateField.setBackground(Color.BLACK);
				lastStateSentField.setText(LOW);
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(displayPanel,
					"DigitalIOMonitor: Exception occurred getting digital io state. " + e.getMessage());
		}
	}

	private void sendAndDisplayState() {
		try {
			String selectedState = (String) selectStateCombo.getSelectedItem();
			if (selectedState == HIGH) {
				digitalIO.setState(CHANNEL_NAME, DigitalIO.HIGH_STATE);
			} else {
				digitalIO.setState(CHANNEL_NAME, DigitalIO.LOW_STATE);
			}

			getAndDisplayState();
		} catch (DeviceException e) {
			JOptionPane.showMessageDialog(displayPanel, "An exception occurred while connecting to the digital io."
					+ e.getMessage());
		}
	}

	@Override
	public void update(Object theObserved, Object arg) {
		if (arg instanceof int[]) {
			getAndDisplayState();
		} else if (arg instanceof DeviceException) {
			JOptionPane.showMessageDialog(displayPanel,
					"An exception occurred while displaying values from the digital io. "
							+ ((DeviceException) arg).getMessage());
		}
	}
}
