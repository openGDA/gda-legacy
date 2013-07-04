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

package gda.gui.mca;

// import gda.device.AsynEpicsTca;
import gda.device.Device;
import gda.device.DeviceException;
import gda.device.EpicsTca;
import gda.factory.Finder;
import gda.gui.AcquisitionPanel;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TcaPanel Class
 */
public class TcaPanel extends AcquisitionPanel {
	
	private static final Logger logger = LoggerFactory.getLogger(TcaPanel.class);
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private JComboBox polarityCombo;

	private JComboBox thresholdCombo;

	private JComboBox scaEnableCombo;

	// private JComboBox statusRateCombo;
	private JComboBox purEnableCombo;

	private JComboBox purAmpCombo;

	private JComboBox selectTcaCombo;

	// private JComboBox statusCombo;
	private JLabel statusValueLabel;

	private JComboBox scaler1GateCombo;

	private JComboBox scaler2GateCombo;

	private JComboBox scaler3GateCombo;

	private JComboBox scaler1PurCombo;

	private JComboBox scaler2PurCombo;

	private JComboBox scaler3PurCombo;

	private JTextField scaler1LowField;

	private JTextField scaler2LowField;

	private JTextField scaler3LowField;

	private JTextField scaler1HighField;

	private JTextField scaler2HighField;

	private JTextField scaler3HighField;

	private JComboBox roiCombo;

	private JSlider scalerCalSlider;

	private JTextField scalerCalField;

	private boolean configured;

	private String tcaName;

	private Device tca;

	private JButton refreshButton;

	/**
	 * Constructor
	 */
	public TcaPanel() {
		makeTcaPanel();
	}

	@Override
	public void configure() {
		if (!configured) {
			// logger.debug("configuring tca");
			tca = (Device) Finder.getInstance().find(tcaName);
			if (tca != null) {
				SwingUtilities.invokeLater(uk.ac.gda.util.ThreadManager.getThread(new Runnable() {
					@Override
					public void run() {
						updateAllValues();
					}

				}));
			}
		}
		configured = true;
	}

	protected void updateAllValues() {
		updatePolarity();
		updateThreshold();
		updateScaEnable();
		updateStatusRate();
		updatePurEnable();
		updatePurAmp();
		updateSelectTca();
		updateStatus();
		updateScaler1Gate();
		updateScaler2Gate();
		updateScaler3Gate();
		updateScaler1Pur();
		updateScaler2Pur();
		updateScaler3Pur();
		updateScaler1Low();
		updateScaler2Low();
		updateScaler3Low();
		updateScaler1High();
		updateScaler2High();
		updateScaler3High();
		updateRoi();
		updateScalerCal();

	}

	private void updateScalerCal() {
		if (tca != null)
			try {
				// logger.debug("updating scalercal");
				Double value = (Double) tca.getAttribute("SCACAL");
				scalerCalField.setText(value.toString());
				scalerCalSlider.setValue((int) (value.doubleValue() * 100));
			} catch (DeviceException e) {
				e.printStackTrace();
			}

	}

	private void updateRoi() {
		if (tca != null)
			try {
				// logger.debug("updating roi scal enable");
				roiCombo.setSelectedItem(EpicsTca.roiScaEnableEnum.valueOf((String) tca.getAttribute("ROISCAENABLE")));

			} catch (DeviceException e) {
				e.printStackTrace();
			}
	}

	private void updateScaler3High() {
		if (tca != null)
			try {
				// logger.debug("updating scaler3High");
				scaler3HighField.setText(((Double) tca.getAttribute("SCA3HI")).toString());
			} catch (DeviceException e) {
				e.printStackTrace();
			}
	}

	private void updateScaler2High() {
		if (tca != null)
			try {
				// logger.debug("updating scaler2High");
				scaler2HighField.setText(((Double) tca.getAttribute("SCA2HI")).toString());
			} catch (DeviceException e) {
				e.printStackTrace();
			}

	}

	private void updateScaler1High() {
		if (tca != null)
			try {
				// logger.debug("updating scaler1High");
				scaler1HighField.setText(((Double) tca.getAttribute("SCA1HI")).toString());
			} catch (DeviceException e) {
				e.printStackTrace();
			}
	}

	private void updateScaler3Low() {
		if (tca != null)
			try {
				// logger.debug("updating scaler3Low");
				scaler3LowField.setText(((Double) tca.getAttribute("SCA3LOW")).toString());
			} catch (DeviceException e) {
				e.printStackTrace();
			}
	}

	private void updateScaler2Low() {
		if (tca != null)
			try {
				// logger.debug("updating scaler2Low");
				scaler2LowField.setText(((Double) tca.getAttribute("SCA2LOW")).toString());
			} catch (DeviceException e) {
				e.printStackTrace();
			}
	}

	private void updateScaler1Low() {
		if (tca != null)
			try {
				// logger.debug("updating scaler1Low");
				scaler1LowField.setText(((Double) tca.getAttribute("SCA1LOW")).toString());
			} catch (DeviceException e) {
				e.printStackTrace();
			}

	}

	private void updateScaler3Pur() {
		if (tca != null)
			try {
				// logger.debug("updating scaler3Pur");
				scaler3PurCombo.setSelectedItem(EpicsTca.scalerPurEnum.valueOf((String) tca.getAttribute("SCA3PUR")));

			} catch (DeviceException e) {
				e.printStackTrace();
			}
	}

	private void updateScaler2Pur() {
		if (tca != null)
			try {
				// logger.debug("updating scaler2Pur");
				scaler2PurCombo.setSelectedItem(EpicsTca.scalerPurEnum.valueOf((String) tca.getAttribute("SCA2PUR")));

			} catch (DeviceException e) {
				e.printStackTrace();
			}
	}

	private void updateScaler1Pur() {
		if (tca != null)
			try {
				// logger.debug("updating scaler1Pur");
				scaler1PurCombo.setSelectedItem(EpicsTca.scalerPurEnum.valueOf((String) tca.getAttribute("SCA1PUR")));

			} catch (DeviceException e) {
				e.printStackTrace();
			}

	}

	private void updateScaler3Gate() {
		if (tca != null)
			try {
				// logger.debug("updating scaler3Gate");
				scaler3GateCombo
						.setSelectedItem(EpicsTca.scalerGateEnum.valueOf((String) tca.getAttribute("SCA3GATE")));

			} catch (DeviceException e) {
				e.printStackTrace();
			}
	}

	private void updateScaler2Gate() {
		if (tca != null)
			try {
				// logger.debug("updating scaler2Gate");
				scaler2GateCombo
						.setSelectedItem(EpicsTca.scalerGateEnum.valueOf((String) tca.getAttribute("SCA2GATE")));

			} catch (DeviceException e) {
				e.printStackTrace();
			}
	}

	private void updateScaler1Gate() {
		if (tca != null)
			try {
				// logger.debug("updating scaler1Gate");
				scaler1GateCombo
						.setSelectedItem(EpicsTca.scalerGateEnum.valueOf((String) tca.getAttribute("SCA1GATE")));

			} catch (DeviceException e) {
				e.printStackTrace();
			}
	}

	private void updateStatus() {
		if (tca != null)
			try {
				// logger.debug("updating tcastatus");
				statusValueLabel.setText((String) tca.getAttribute("STATUS"));

			} catch (DeviceException e) {
				e.printStackTrace();
			}
	}

	private void updateSelectTca() {
		if (tca != null)
			try {
				// logger.debug("updating selectTca");
				selectTcaCombo.setSelectedItem(EpicsTca.tcaSelectEnum.valueOf((String) tca.getAttribute("TCASELECT")));

			} catch (DeviceException e) {
				e.printStackTrace();
			}
	}

	private void updatePurAmp() {
		if (tca != null)
			try {
				// logger.debug("updating purAmp");
				purAmpCombo.setSelectedItem(EpicsTca.purAmpEnum.valueOf((String) tca.getAttribute("PURAMP")));

			} catch (DeviceException e) {
				e.printStackTrace();
			}

	}

	private void updatePurEnable() {
		if (tca != null)
			try {
				// logger.debug("updating purEnable");
				purEnableCombo.setSelectedItem(EpicsTca.purEnableEnum.valueOf((String) tca.getAttribute("PURENABLE")));

			} catch (DeviceException e) {
				e.printStackTrace();
			}
	}

	private void updateStatusRate() {

	}

	private void updateScaEnable() {
		if (tca != null)
			try {
				// logger.debug("updating scalerEnable");
				scaEnableCombo.setSelectedItem(EpicsTca.scalerEnableEnum
						.valueOf((String) tca.getAttribute("SCAENABLE")));

			} catch (DeviceException e) {
				e.printStackTrace();
			}
	}

	private void updateThreshold() {
		if (tca != null)
			try {
				// logger.debug("updating threshold ");
				thresholdCombo.setSelectedItem(EpicsTca.thresholdEnum.valueOf((String) tca.getAttribute("THRESHOLD")));

			} catch (DeviceException e) {
				e.printStackTrace();
			}

	}

	private void updatePolarity() {
		if (tca != null)
			try {
				// logger.debug("updating polarity "+
				// EpicsTca.polarityEnum.valueOf((String)tca.getAttribute("POLARITY"))
				// );
				polarityCombo.setSelectedItem(EpicsTca.polarityEnum.valueOf((String) tca.getAttribute("POLARITY")));

			} catch (DeviceException e) {
				e.printStackTrace();
			}
	}

	private void makeTcaPanel() {
		JLabel polarityLabel = new JLabel("Output Polarity");
		JLabel thresholdLabel = new JLabel("ICR Threshold");
		JLabel scaEnableLabel = new JLabel("Enable SCA's");
		// JLabel statusRateLabel = new JLabel("Status Rate");
		JLabel purEnableLabel = new JLabel("Pur Enable");
		JLabel purAmpLabel = new JLabel("Pur Amp");
		JLabel selectTcaLabel = new JLabel("Select TCA");
		JLabel statusLabel = new JLabel("Status");
		JLabel scaler1Label = new JLabel("SCA 1");
		JLabel scaler2Label = new JLabel("SCA 2");
		JLabel scaler3Label = new JLabel("SCA 3");
		JLabel gateLabel = new JLabel("Gate");
		JLabel purLabel = new JLabel("Pur");
		JLabel lowLabel = new JLabel("Low");
		JLabel highLabel = new JLabel("High");
		JLabel roiLabel = new JLabel("ROI to Sca Enable");
		JLabel scaCalLabel = new JLabel("Sca Calibration");

		Box labelBox1 = Box.createHorizontalBox();
		labelBox1.add(polarityLabel);
		labelBox1.add(Box.createHorizontalStrut(20));
		labelBox1.add(thresholdLabel);
		labelBox1.add(Box.createHorizontalStrut(20));
		labelBox1.add(scaEnableLabel);
		// labelBox1.add(Box.createHorizontalStrut(20));
		// labelBox1.add(statusRateLabel);
		// ////labelBox1.add(Box.createHorizontalStrut(20));

		Box labelBox2 = Box.createHorizontalBox();
		labelBox2.add(purEnableLabel);
		labelBox2.add(Box.createHorizontalStrut(20));
		labelBox2.add(purAmpLabel);
		labelBox2.add(Box.createHorizontalStrut(20));
		labelBox2.add(selectTcaLabel);
		labelBox2.add(Box.createHorizontalStrut(20));
		labelBox2.add(statusLabel);

		Box labelBox3 = Box.createHorizontalBox();
		labelBox3.add(gateLabel);
		labelBox3.add(Box.createHorizontalStrut(20));
		labelBox3.add(purLabel);
		labelBox3.add(Box.createHorizontalStrut(20));
		labelBox3.add(lowLabel);
		labelBox3.add(Box.createHorizontalStrut(20));
		labelBox3.add(highLabel);

		polarityCombo = new JComboBox(EpicsTca.polarityEnum.values());
		polarityCombo.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() != ItemEvent.DESELECTED) {
					try {
						// tca.setAttribute("POLARITY",(AsynEpicsTca.polarityEnum)e.getItem());
						tca.setAttribute("POLARITY", e.getItem().toString());
					} catch (DeviceException e1) {
						e1.printStackTrace();
					}
				}

			}

		});
		thresholdCombo = new JComboBox(EpicsTca.thresholdEnum.values());
		thresholdCombo.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() != ItemEvent.DESELECTED) {
					try {
						// tca.setAttribute("POLARITY",(AsynEpicsTca.polarityEnum)e.getItem());
						tca.setAttribute("THRESHOLD", e.getItem().toString());
					} catch (DeviceException e1) {
						e1.printStackTrace();
					}
				}

			}

		});
		scaEnableCombo = new JComboBox(EpicsTca.scalerEnableEnum.values());
		scaEnableCombo.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() != ItemEvent.DESELECTED) {
					try {
						// tca.setAttribute("POLARITY",(AsynEpicsTca.polarityEnum)e.getItem());
						tca.setAttribute("SCAENABLE", e.getItem().toString());
					} catch (DeviceException e1) {
						e1.printStackTrace();
					}
				}

			}

		});
		// statusRateCombo = new JComboBox();
		purEnableCombo = new JComboBox(EpicsTca.purEnableEnum.values());
		purEnableCombo.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() != ItemEvent.DESELECTED) {
					try {
						// tca.setAttribute("POLARITY",(AsynEpicsTca.polarityEnum)e.getItem());
						tca.setAttribute("PURENABLE", e.getItem().toString());
					} catch (DeviceException e1) {
						e1.printStackTrace();
					}
				}

			}

		});
		purAmpCombo = new JComboBox(EpicsTca.purAmpEnum.values());
		purAmpCombo.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() != ItemEvent.DESELECTED) {
					try {
						// tca.setAttribute("POLARITY",(AsynEpicsTca.polarityEnum)e.getItem());
						tca.setAttribute("PURAMP", e.getItem().toString());
					} catch (DeviceException e1) {
						e1.printStackTrace();
					}
				}

			}

		});
		selectTcaCombo = new JComboBox(EpicsTca.tcaSelectEnum.values());
		selectTcaCombo.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() != ItemEvent.DESELECTED) {
					try {
						// tca.setAttribute("POLARITY",(AsynEpicsTca.polarityEnum)e.getItem());
						tca.setAttribute("TCASELECT", e.getItem().toString());
					} catch (DeviceException e1) {
						e1.printStackTrace();
					}
				}

			}

		});
		// statusCombo = new JComboBox(EpicsTca.statusEnum.values());
		statusLabel = new JLabel("   ");

		scaler1GateCombo = new JComboBox(EpicsTca.scalerGateEnum.values());
		scaler1GateCombo.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() != ItemEvent.DESELECTED) {
					try {
						// tca.setAttribute("POLARITY",(AsynEpicsTca.polarityEnum)e.getItem());
						tca.setAttribute("SCA1GATE", e.getItem().toString());
					} catch (DeviceException e1) {
						e1.printStackTrace();
					}
				}

			}

		});
		scaler1PurCombo = new JComboBox(EpicsTca.scalerPurEnum.values());
		scaler1PurCombo.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() != ItemEvent.DESELECTED) {
					try {
						// tca.setAttribute("POLARITY",(AsynEpicsTca.polarityEnum)e.getItem());
						tca.setAttribute("SCA1PUR", e.getItem().toString());
					} catch (DeviceException e1) {
						e1.printStackTrace();
					}
				}

			}

		});
		scaler1LowField = new JTextField(10);
		scaler1LowField.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (tca != null)
					try {
						tca.setAttribute("SCA1LOW", Double.parseDouble(scaler1LowField.getText()));
					} catch (NumberFormatException e1) {
						logger.warn("TcaPanel cannot set the offset value" + e1.getMessage());
						updateScaler1Low();
					} catch (DeviceException e1) {
						logger.warn("TcaPanel cannot Set the offset values" + e1.getMessage());
						updateScaler1Low();
					}
			}
		});
		scaler1HighField = new JTextField(10);
		scaler1HighField.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (tca != null)
					try {
						tca.setAttribute("SCA1HIGH", Double.parseDouble(scaler1HighField.getText()));
					} catch (NumberFormatException e1) {
						logger.warn("TcaPanel cannot set the sca 1 high value" + e1.getMessage());
						updateScaler1High();
					} catch (DeviceException e1) {
						logger.warn("TcaPanel cannot Set the sca 1 high values" + e1.getMessage());
						updateScaler1High();
					}
			}
		});
		scaler2GateCombo = new JComboBox(EpicsTca.scalerGateEnum.values());
		scaler2GateCombo.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() != ItemEvent.DESELECTED) {
					try {
						// tca.setAttribute("POLARITY",(AsynEpicsTca.polarityEnum)e.getItem());
						tca.setAttribute("SCA2GATE", e.getItem().toString());
					} catch (DeviceException e1) {
						e1.printStackTrace();
					}
				}

			}

		});
		scaler2PurCombo = new JComboBox(EpicsTca.scalerPurEnum.values());
		scaler2PurCombo.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() != ItemEvent.DESELECTED) {
					try {
						// tca.setAttribute("POLARITY",(AsynEpicsTca.polarityEnum)e.getItem());
						tca.setAttribute("SCA2PUR", e.getItem().toString());
					} catch (DeviceException e1) {
						e1.printStackTrace();
					}
				}

			}

		});
		scaler2LowField = new JTextField(10);
		scaler2LowField.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (tca != null)
					try {
						tca.setAttribute("SCA2LOW", Double.parseDouble(scaler2LowField.getText()));
					} catch (NumberFormatException e1) {
						logger.warn("TcaPanel cannot set the scaler 2 low value" + e1.getMessage());
						updateScaler2Low();
					} catch (DeviceException e1) {
						logger.warn("TcaPanel cannot Set the scaler 2 low values" + e1.getMessage());
						updateScaler2Low();
					}
			}
		});
		scaler2HighField = new JTextField(10);
		scaler2HighField.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (tca != null)
					try {
						tca.setAttribute("SCA2HIGH", Double.parseDouble(scaler2HighField.getText()));
					} catch (NumberFormatException e1) {
						logger.warn("TcaPanel cannot set the scaler 2 High value" + e1.getMessage());
						updateScaler2High();
					} catch (DeviceException e1) {
						logger.warn("TcaPanel cannot Set the scaler 2 High values" + e1.getMessage());
						updateScaler2High();
					}
			}
		});
		scaler3GateCombo = new JComboBox(EpicsTca.scalerGateEnum.values());
		scaler3GateCombo.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() != ItemEvent.DESELECTED) {
					try {
						// tca.setAttribute("POLARITY",(AsynEpicsTca.polarityEnum)e.getItem());
						tca.setAttribute("SCA3GATE", e.getItem().toString());
					} catch (DeviceException e1) {
						e1.printStackTrace();
					}
				}

			}

		});
		scaler3PurCombo = new JComboBox(EpicsTca.scalerPurEnum.values());
		scaler3PurCombo.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() != ItemEvent.DESELECTED) {
					try {
						// tca.setAttribute("POLARITY",(AsynEpicsTca.polarityEnum)e.getItem());
						tca.setAttribute("SCA3PUR", e.getItem().toString());
					} catch (DeviceException e1) {
						e1.printStackTrace();
					}
				}

			}

		});
		scaler3LowField = new JTextField(10);
		scaler3LowField.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (tca != null)
					try {
						tca.setAttribute("SCA3LOW", Double.parseDouble(scaler3LowField.getText()));
					} catch (NumberFormatException e1) {
						logger.warn("TcaPanel cannot set the scaler 3 low value" + e1.getMessage());
						updateScaler3Low();
					} catch (DeviceException e1) {
						logger.warn("TcaPanel cannot Set the scaler 3 low values" + e1.getMessage());
						updateScaler3Low();
					}
			}
		});
		scaler3HighField = new JTextField(10);
		scaler3HighField.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (tca != null)
					try {
						tca.setAttribute("SCA3HIGH", Double.parseDouble(scaler3HighField.getText()));
					} catch (NumberFormatException e1) {
						logger.warn("TcaPanel cannot set the scaler 3 high value" + e1.getMessage());
						updateScaler3High();
					} catch (DeviceException e1) {
						logger.warn("TcaPanel cannot Set the scaler 3 High values" + e1.getMessage());
						updateScaler3High();
					}
			}
		});
		roiCombo = new JComboBox(EpicsTca.roiScaEnableEnum.values());
		roiCombo.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() != ItemEvent.DESELECTED) {
					try {
						// tca.setAttribute("POLARITY",(AsynEpicsTca.polarityEnum)e.getItem());
						tca.setAttribute("ROISCAENABLE", e.getItem().toString());
					} catch (DeviceException e1) {
						e1.printStackTrace();
					}
				}

			}

		});

		scalerCalField = new JTextField(10);
		scalerCalField.setEditable(false);
		scalerCalSlider = new JSlider(50, 150, 50);
		scalerCalSlider.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				int fps = scalerCalSlider.getValue();
				scalerCalField.setText(String.valueOf(fps / 100.0));
				if (!scalerCalSlider.getValueIsAdjusting()) { // done
					// adjusting

					try {
						// logger.debug("the value is "
						// +scalerCalField.getText() );
						tca.setAttribute("SCACAL", new Double(scalerCalField.getText()));
					} catch (DeviceException e1) {
						e1.printStackTrace();
					}
				}

			}

		});
		statusValueLabel = new JLabel("     ");

		refreshButton = new JButton("Refresh");
		refreshButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				updateAllValues();

			}

		});
		Box valueBox1 = Box.createHorizontalBox();
		valueBox1.add(polarityCombo);
		valueBox1.add(Box.createHorizontalStrut(20));
		valueBox1.add(thresholdCombo);
		valueBox1.add(Box.createHorizontalStrut(20));
		valueBox1.add(scaEnableCombo);
		valueBox1.add(Box.createHorizontalStrut(20));
		valueBox1.add(refreshButton);

		Box valueBox2 = Box.createHorizontalBox();
		valueBox2.add(purEnableCombo);
		valueBox2.add(Box.createHorizontalStrut(20));
		valueBox2.add(purAmpCombo);
		valueBox2.add(Box.createHorizontalStrut(20));
		valueBox2.add(selectTcaCombo);
		valueBox2.add(Box.createHorizontalStrut(20));
		valueBox2.add(statusValueLabel);

		Box valueBox3 = Box.createHorizontalBox();
		valueBox3.add(scaler1Label);
		valueBox3.add(Box.createHorizontalStrut(10));
		valueBox3.add(scaler1GateCombo);
		valueBox3.add(Box.createHorizontalStrut(10));
		valueBox3.add(scaler1PurCombo);
		valueBox3.add(Box.createHorizontalStrut(10));
		valueBox3.add(scaler1LowField);
		valueBox3.add(Box.createHorizontalStrut(10));
		valueBox3.add(scaler1HighField);

		Box valueBox4 = Box.createHorizontalBox();
		valueBox4.add(scaler2Label);
		valueBox4.add(Box.createHorizontalStrut(10));
		valueBox4.add(scaler2GateCombo);
		valueBox4.add(Box.createHorizontalStrut(10));
		valueBox4.add(scaler2PurCombo);
		valueBox4.add(Box.createHorizontalStrut(10));
		valueBox4.add(scaler2LowField);
		valueBox4.add(Box.createHorizontalStrut(10));
		valueBox4.add(scaler2HighField);

		Box valueBox5 = Box.createHorizontalBox();
		valueBox5.add(scaler3Label);
		valueBox5.add(Box.createHorizontalStrut(10));
		valueBox5.add(scaler3GateCombo);
		valueBox5.add(Box.createHorizontalStrut(10));
		valueBox5.add(scaler3PurCombo);
		valueBox5.add(Box.createHorizontalStrut(10));
		valueBox5.add(scaler3LowField);
		valueBox5.add(Box.createHorizontalStrut(10));
		valueBox5.add(scaler3HighField);

		Box roiBox = Box.createVerticalBox();
		roiBox.add(roiLabel);
		roiBox.add(roiCombo);
		roiBox.add(Box.createVerticalStrut(20));
		roiBox.add(scaCalLabel);
		roiBox.add(scalerCalField);
		roiBox.add(scalerCalSlider);

		Box scaBox = Box.createVerticalBox();
		scaBox.add(labelBox3);
		scaBox.add(Box.createVerticalStrut(20));
		scaBox.add(valueBox3);
		scaBox.add(Box.createVerticalStrut(20));
		scaBox.add(valueBox4);
		scaBox.add(Box.createVerticalStrut(20));
		scaBox.add(valueBox5);

		Box parBox = Box.createHorizontalBox();
		parBox.add(scaBox);
		parBox.add(Box.createHorizontalStrut(20));
		parBox.add(roiBox);

		// labelBox3.add(roiBox);
		Box wholeBox = Box.createVerticalBox();
		wholeBox.add(labelBox1);
		wholeBox.add(valueBox1);
		wholeBox.add(Box.createVerticalStrut(20));
		wholeBox.add(labelBox2);
		wholeBox.add(valueBox2);
		wholeBox.add(Box.createVerticalStrut(20));
		wholeBox.add(parBox);

		this.setLayout(new BorderLayout());
		this.add(wholeBox);

	}

	/**
	 * @param args
	 */
	public static void main(String args[]) {
		JFrame frame = new JFrame();
		frame.add(new TcaPanel());
		// frame.setSize(400, 500);
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

	/**
	 * @return tca name
	 */
	public String getTcaName() {
		return tcaName;
	}

	/**
	 * @param tcaName
	 */
	public void setTcaName(String tcaName) {
		this.tcaName = tcaName;
	}
}
