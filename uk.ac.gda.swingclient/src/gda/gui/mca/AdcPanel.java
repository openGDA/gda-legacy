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

import gda.device.Adc;
import gda.device.DeviceException;
import gda.device.adc.EpicsADC;
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
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AdcPanel Class
 */
public class AdcPanel extends AcquisitionPanel {
	
	private static final Logger logger = LoggerFactory.getLogger(AdcPanel.class);
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private JComboBox gainCombo;

	private JTextField offsetField;

	private JComboBox amodCombo;

	private JComboBox cmodCombo;

	private JTextField lldField;

	private JTextField uldField;

	private JTextField zeroField;

	private JComboBox pmodCombo;

	private JComboBox gmodCombo;

	private JComboBox tmodCombo;

	private String adcName;

	private Adc adc;

	private boolean configured;

	private JButton refreshButton;

	private ComboBoxListener gainComboListener;

	/**
	 * Constructor
	 */
	public AdcPanel() {
		makeAdcPanel();
	}

	/**
	 * makeAdcPanel
	 */
	public void makeAdcPanel() {
		JLabel gainLabel = new JLabel("GAIN/RANGE");
		JLabel offsetLabel = new JLabel("OFFSET");
		JLabel amodLabel = new JLabel("Acqg.Mode");
		JLabel cmodLabel = new JLabel("coinc. mode");
		JLabel lldLabel = new JLabel("Lower LEv.Disc.");
		JLabel uldLabel = new JLabel("Upper Lev Desc");
		JLabel zeroLabel = new JLabel("ZERO");
		JLabel pmodeLabel = new JLabel("Peak Detect Mode");
		JLabel gmodeLabel = new JLabel("GateMode");
		JLabel tmodeLabel = new JLabel("Data Transfer Mode");

		gainCombo = new JComboBox();
		gainCombo.addItemListener(gainComboListener = new ComboBoxListener("GAIN", false));

		offsetField = new JTextField(10);
		offsetField.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (adc != null) {
					try {
						adc.setAttribute("OFFSET", Double.parseDouble(offsetField.getText()));
					} catch (NumberFormatException e1) {
						logger.warn("AdcPanel cannot set the offset value" + e1.getMessage());
						updateOffset();
					} catch (DeviceException e1) {
						logger.warn("AdcPanel cannot Set the offset values" + e1.getMessage());
						updateOffset();
					}
				}
			}

		});
		amodCombo = new JComboBox(EpicsADC.amodEnum.values());
//		amodCombo.addItemListener(amodComboListener = new ComboBoxListener("AMOD", true));

		cmodCombo = new JComboBox(EpicsADC.cmodEnum.values());
//		cmodCombo.addItemListener(cmodComboListener = new ComboBoxListener("CMOD", true));

		lldField = new JTextField(10);
		lldField.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					adc.setAttribute("LLD", Double.parseDouble(lldField.getText()));
				} catch (NumberFormatException e1) {
					logger.warn("AdcPanel cannot set adc lld values " + e1.getMessage());
					updateLld();
				} catch (DeviceException e1) {
					logger.warn("AdcPanel cannot set adc lld values " + e1.getMessage());
					updateLld();
				}

			}

		});
		uldField = new JTextField(10);
		uldField.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					adc.setAttribute("ULD", Double.parseDouble(uldField.getText()));
				} catch (NumberFormatException e1) {
					logger.warn("AdcPanel cannot set adc uld values " + e1.getMessage());
					updateUld();
				} catch (DeviceException e1) {
					logger.warn("AdcPanel cannot set adc uld values " + e1.getMessage());
					updateUld();
				}

			}

		});
		zeroField = new JTextField(10);
		zeroField.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					adc.setAttribute("ZERO", Double.parseDouble(zeroField.getText()));
				} catch (NumberFormatException e1) {
					logger.warn("AdcPanel cannot set adc zero values " + e1.getMessage());
					updateZero();
				} catch (DeviceException e1) {
					logger.warn("AdcPanel cannot set adc zero values " + e1.getMessage());
					updateZero();
				}

			}

		});
		pmodCombo = new JComboBox(EpicsADC.pmodEnum.values());
//		pmodCombo.addItemListener(pmodComboListener = new ComboBoxListener("PMOD", true));

		gmodCombo = new JComboBox(EpicsADC.gmodEnum.values());
//		gmodCombo.addItemListener(gmodComboListener = new ComboBoxListener("GMOD", true));

		tmodCombo = new JComboBox(EpicsADC.tmodEnum.values());
//		tmodCombo.addItemListener(tmodComboListener = new ComboBoxListener("TMOD", true));

		refreshButton = new JButton("Refresh");
		refreshButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				updateAllValues();

			}

		});
		Box labelBox1 = Box.createHorizontalBox();
		labelBox1.add(Box.createHorizontalStrut(10));
		labelBox1.add(gainLabel);
		labelBox1.add(Box.createHorizontalStrut(30));
		labelBox1.add(offsetLabel);
		labelBox1.add(Box.createHorizontalStrut(300));
		labelBox1.add(amodLabel);
		labelBox1.add(Box.createHorizontalStrut(60));
		labelBox1.add(cmodLabel);

		Box valueBox1 = Box.createHorizontalBox();
		valueBox1.add(gainCombo);
		valueBox1.add(Box.createHorizontalStrut(20));
		valueBox1.add(offsetField);
		valueBox1.add(Box.createHorizontalStrut(20));
		valueBox1.add(amodCombo);
		valueBox1.add(Box.createHorizontalStrut(20));
		valueBox1.add(cmodCombo);

		Box labelBox2 = Box.createHorizontalBox();
		labelBox2.add(lldLabel);
		labelBox2.add(Box.createHorizontalStrut(60));
		labelBox2.add(uldLabel);
		labelBox2.add(Box.createHorizontalStrut(60));
		labelBox2.add(zeroLabel);

		Box valueBox2 = Box.createHorizontalBox();
		valueBox2.add(lldField);
		valueBox2.add(Box.createHorizontalStrut(30));
		valueBox2.add(uldField);
		valueBox2.add(Box.createHorizontalStrut(30));
		valueBox2.add(zeroField);

		Box labelBox3 = Box.createHorizontalBox();
		labelBox3.add(pmodeLabel);
		labelBox3.add(Box.createHorizontalStrut(60));
		labelBox3.add(gmodeLabel);
		labelBox3.add(Box.createHorizontalStrut(60));
		labelBox3.add(tmodeLabel);

		Box valueBox3 = Box.createHorizontalBox();
		valueBox3.add(pmodCombo);
		valueBox3.add(Box.createHorizontalStrut(30));
		valueBox3.add(gmodCombo);
		valueBox3.add(Box.createHorizontalStrut(30));
		valueBox3.add(tmodCombo);
		valueBox3.add(Box.createHorizontalStrut(30));
		valueBox3.add(refreshButton);
		Box wholeBox = Box.createVerticalBox();
		wholeBox.add(labelBox1);
		wholeBox.add(valueBox1);
		wholeBox.add(Box.createVerticalStrut(30));
		wholeBox.add(labelBox2);
		wholeBox.add(valueBox2);
		wholeBox.add(Box.createVerticalStrut(30));
		wholeBox.add(labelBox3);
		wholeBox.add(valueBox3);
		wholeBox.add(Box.createVerticalStrut(30));

		this.setLayout(new BorderLayout());
		this.add(wholeBox);

	}

	@Override
	public void configure() {
		if (!configured) {
			// ///////logger.debug("inside adc configure");
			adc = (Adc) Finder.getInstance().find(adcName);
			if (adc != null) {
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
		updateGain_Values();
		updateGain();
		updateLld();
		updateUld();
		updateOffset();
		updateAmod();
		updatePmod();
		updateCmod();
		updateTmod();
		updateGmod();
		updateZero();
	}

	private void updateGain_Values() {
		if (adc != null) {
			try {
				// When the first item is added to a combo box this becomes the
				// selected which can cause any listeners to the combobox to
				// think
				// that the user has made a selected. We need to read the
				// current value from the hardware and reset the selected item
				// afterwards
				// There is still a problem so disable the itemListener whilst
				// adding values
				Object currentValue = adc.getAttribute("GAIN");
				gainComboListener.activate(false);
				double[] gainValues = (double[]) adc.getAttribute("Gain_Values");
				for (int i = 0; i < gainValues.length; i++)
					gainCombo.addItem(gainValues[i]);
				gainComboListener.activate(true);
				gainCombo.setSelectedItem(currentValue);
			} catch (DeviceException e) {
				e.printStackTrace();
			}
		}
	}

	private void updateGmod() {
		if (adc != null) {
			try {
				gmodCombo.setSelectedItem(EpicsADC.gmodEnum.valueOf((String) adc.getAttribute("GMOD")));
			} catch (DeviceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void updateTmod() {
		if (adc != null)
			try {
				tmodCombo.setSelectedItem(EpicsADC.tmodEnum.valueOf((String) adc.getAttribute("TMOD")));
			} catch (DeviceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

	}

	private void updateCmod() {
		if (adc != null)
			try {
				cmodCombo.setSelectedItem(EpicsADC.cmodEnum.valueOf((String) adc.getAttribute("CMOD")));
			} catch (DeviceException e) {
				e.printStackTrace();
			}
	}

	private void updatePmod() {
		if (adc != null)
			try {
				pmodCombo.setSelectedItem(EpicsADC.pmodEnum.valueOf((String) adc.getAttribute("PMOD")));
			} catch (DeviceException e) {
				e.printStackTrace();
			}
	}

	private void updateAmod() {
		if (adc != null)
			try {
				amodCombo.setSelectedItem(EpicsADC.amodEnum.valueOf((String) adc.getAttribute("AMOD")));
			} catch (DeviceException e) {
				e.printStackTrace();
			}

	}

	private void updateOffset() {
		if (adc != null)
			try {
				offsetField.setText(((Double) adc.getAttribute("OFFSET")).toString());
			} catch (DeviceException e) {
				e.printStackTrace();
			}

	}

	private void updateUld() {
		if (adc != null)
			try {
				uldField.setText(((Double) adc.getAttribute("ULD")).toString());
			} catch (DeviceException e) {
				e.printStackTrace();
			}

	}

	private void updateLld() {
		if (adc != null)
			try {
				lldField.setText(((Double) adc.getAttribute("LLD")).toString());
			} catch (DeviceException e) {
				e.printStackTrace();
			}

	}

	private void updateZero() {
		if (adc != null)
			try {
				zeroField.setText(((Double) adc.getAttribute("ZERO")).toString());
			} catch (DeviceException e) {
				e.printStackTrace();
			}

	}

	private void updateGain() {
		if (adc != null)
			try {
				gainCombo.setSelectedItem(adc.getAttribute("GAIN"));
			} catch (DeviceException e) {
				e.printStackTrace();
			}

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		JFrame f = new JFrame();
		f.add(new AdcPanel());
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.pack();
		f.setVisible(true);

	}

	/**
	 * @return ADC name
	 */
	public String getAdcName() {
		return adcName;
	}

	/**
	 * @param adcName
	 */
	public void setAdcName(String adcName) {
		this.adcName = adcName;
	}

	class ComboBoxListener implements ItemListener {
		private final String attributeName;

		private boolean active = true;

		private final boolean useStringValue;

		ComboBoxListener(String attributeName, boolean useStringValue) {
			this.attributeName = attributeName;
			this.useStringValue = useStringValue;

		}

		void activate(boolean activate) {
			active = activate;
		}

		@Override
		public void itemStateChanged(ItemEvent e) {
			if (e.getStateChange() != ItemEvent.DESELECTED && active) {
				try {
					adc.setAttribute(attributeName, useStringValue ? e.getItem().toString() : e.getItem());
				} catch (DeviceException e1) {
					e1.printStackTrace();
				}
			}

		}
	}

}
