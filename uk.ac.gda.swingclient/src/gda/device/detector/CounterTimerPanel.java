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

package gda.device.detector;

import gda.device.Detector;
import gda.device.DeviceException;
import gda.factory.FactoryException;
import gda.factory.Finder;
import gda.gui.AcquisitionPanel;
import gda.plots.SimplePlot;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

/**
 * Provides basic user panel to drive up to 2 detectors
 */
public class CounterTimerPanel extends AcquisitionPanel implements Runnable {
	
	private Thread runner;
	private boolean running = false;

	private Detector counter1 = null;
	private Detector counter2 = null;
	private int numChannels1 = 0;
	private int numChannels2 = 0;
	private int totalChannels = 0;
	private double[] xdata;
	private double[] ydata;
	private String[] labels;

	private JPanel jButtonPanel = null;
	private JButton jButtonStart = null;
	private JButton jButtonStop = null;
	private JCheckBox jCheckAccumulate = null;
	private JPanel jDataPanel = null;
	private JTextField[] jTextChannel;
	private JTextField jTextSystem = null;
	private JTextField jTextTime = null;
	private JPanel jPlotPanel = null;
	private String counter1Name;
	private String counter2Name;

	private SimplePlot simplePlot = new SimplePlot(SimplePlot.BARCHART);

	/**
	 * This is the default constructor
	 */
	public CounterTimerPanel() {
	}

	@Override
	public void configure() throws FactoryException {
		Finder finder = Finder.getInstance();
		counter1 = (Detector) finder.find(counter1Name);
		counter2 = (Detector) finder.find(counter2Name);

		if (counter1 != null) {
			numChannels1 = counter1.getExtraNames().length;
		}

		if (counter2 != null) {
			numChannels2 = counter2.getExtraNames().length;
		}

		totalChannels = numChannels1 + numChannels2;

		xdata = new double[totalChannels];
		ydata = new double[totalChannels];
		labels = new String[totalChannels];
		int i = 0;
		if (counter1 != null) {
			for (int j = 0; j < numChannels1; j++) {
				labels[i++] = counter1.getExtraNames()[j];
			}
		}
		if (counter2 != null) {
			for (int j = 0; j < numChannels2; j++) {
				labels[i++] = counter2.getExtraNames()[j];
			}
		}

		setLayout(new BorderLayout());
		add(getJButtonPanel(), java.awt.BorderLayout.SOUTH);
		add(getJPlotPanel(), BorderLayout.CENTER);
		add(getJDataPanel(), BorderLayout.WEST);
	}

	/**
	 * @return Returns the counter1Name.
	 */
	public String getCounter1Name() {
		return counter1Name;
	}

	/**
	 * @param counter1Name
	 *            The counter1Name to set.
	 */
	public void setCounter1Name(String counter1Name) {
		this.counter1Name = counter1Name;
	}

	/**
	 * @return Returns the counter2Name.
	 */
	public String getCounter2Name() {
		return counter2Name;
	}

	/**
	 * @param counter2Name
	 *            The counter2Name to set.
	 */
	public void setCounter2Name(String counter2Name) {
		this.counter2Name = counter2Name;
	}

	@Override
	public void run() {
		int busy = Detector.IDLE;
		boolean finished = true;
		// Acquisition thread
		while (running) {
			// Get status values, start if not busy
			busy = getStatii();
			if (busy != Detector.BUSY && finished) {
				if (startCounters()) {
					jTextSystem.setText("Counting");
					finished = false;
				} else {
					jButtonStop.setEnabled(false);
					jButtonStart.setEnabled(true);
					running = false;
				}
			}
			// Get status values again, if busy sleep
			busy = getStatii();
			if (busy == Detector.BUSY) {
				jTextSystem.setText(jTextSystem.getText() + ".");
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					jTextSystem.setText("Sleep failed");
				}
			} else {
				jTextSystem.setText(this.jTextSystem.getText() + "finished");
				displayCounts();
				finished = true;
			}
		}
		jButtonStop.setEnabled(false);
		jButtonStart.setEnabled(true);
	}

	private int getStatii() {
		// Reads status of counter(s) and returns it
		int status = Detector.IDLE;
		int status1 = Detector.IDLE;
		int status2 = Detector.IDLE;
		try {
			if (counter1 != null) {
				status1 = counter1.getStatus();
			}
			if (counter2 != null) {
				status2 = counter2.getStatus();
			}
		} catch (DeviceException de) {
			jTextSystem.setText("Error reading counter status");
		}

		if (status1 == Detector.BUSY || status2 == Detector.BUSY) {
			status = Detector.BUSY;
		}

		return status;
	}

	private boolean startCounters() {
		boolean success = false;
		// initialise timing channel(s)
		String s = jTextTime.getText();
		double val = Double.valueOf(s).doubleValue();
		try {
			if (counter1 != null) {
				counter1.setCollectionTime(val/1000.0);
				counter1.collectData();
//				counter1.countAsync(val);
				success = true;
			}
			if (counter2 != null) {
				counter2.setCollectionTime(val/1000.0);
				counter2.collectData();
//				counter2.countAsync(val);
				success = true;
			}
		} catch (DeviceException de) {
			jTextSystem.setText(de.getMessage());
		}
		return success;
	}

	private void displayCounts() {
		double[] data = null;

		try {
			// Read in data from counter...
			if (counter1 != null) {
				data = (double[]) counter1.readout();

				for (int i = 0; i < numChannels1; i++) {
					xdata[i] = i;
					if (jCheckAccumulate.isSelected()) {
						ydata[i] += data[i];
					} else {
						ydata[i] = data[i];
					}
					jTextChannel[i].setText("" + ydata[i]);
				}
			}
			if (counter2 != null) {
				data = (double[]) counter2.readout();

				for (int i = numChannels1; i < numChannels1 + numChannels2; i++) {
					xdata[i] = i;
					if (jCheckAccumulate.isSelected()) {
						ydata[i] += data[i - numChannels1];
					} else {
						ydata[i] = data[i - numChannels1];
					}
					jTextChannel[i].setText("" + ydata[i]);
				}
			}
			// ... and display
			simplePlot.setLinePoints(1, xdata, ydata);
		} catch (DeviceException de) {
			jTextSystem.setText(de.getMessage());
		}
	}

	private JPanel getJPlotPanel() {
		if (jPlotPanel == null) {
			jPlotPanel = new JPanel();
			jPlotPanel.setLayout(new BorderLayout());
			// Initialise plot
			simplePlot.setLegendVisible(false);
			simplePlot.initializeLine(1);
			simplePlot.setXAxisLabel("Channel");
			simplePlot.setYAxisLabel("Counts");
			simplePlot.setXAxisLimits(-0.5, (this.totalChannels - 0.5));

			simplePlot.setYAxisAutoScaling(true);
			simplePlot.setTitleVisible(false);
			jPlotPanel.add(simplePlot, BorderLayout.CENTER);
		}
		jPlotPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Counter Snapshot",
				TitledBorder.LEFT, TitledBorder.TOP, null, Color.black));

		jPlotPanel.add(getJTextSystem(), BorderLayout.SOUTH);
		return jPlotPanel;
	}

	private JPanel getJButtonPanel() {
		jButtonPanel = new JPanel();
		jButtonPanel.setLayout(new FlowLayout());

		jButtonPanel.add(new JLabel("Time (ms)"));
		jButtonPanel.add(getJTextTime());
		jButtonPanel.add(getJCheckAccumulate());
		jButtonPanel.add(getJButtonStart());
		jButtonPanel.add(getJButtonStop());

		jButtonPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createRaisedBevelBorder(),
				BorderFactory.createEmptyBorder(0, 0, 0, 0)));
		return jButtonPanel;
	}

	private JButton getJButtonStart() {
		jButtonStart = new JButton();
		jButtonStart.setText("Start");
		jButtonStart.setEnabled(true);
		jButtonStart.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				jButtonStop.setEnabled(true);
				jButtonStart.setEnabled(false);
				for (int i = 0; i < totalChannels; i++) {
					xdata[i] = 0;
					ydata[i] = 0;
				}
				// Create new acquisition thread
				runner = uk.ac.gda.util.ThreadManager.getThread(CounterTimerPanel.this, getClass().getName());
				running = true;
				runner.start();
			}
		});
		return jButtonStart;
	}

	private JButton getJButtonStop() {
		if (jButtonStop == null) {
			jButtonStop = new JButton();
			jButtonStop.setText("Stop");
			jButtonStop.setEnabled(false);
			jButtonStop.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						counter1.stop();
					} catch (DeviceException de) {
						jTextSystem.setText(de.getMessage());
					}
					running = false;
					jButtonStop.setEnabled(false);
				}
			});
		}
		return jButtonStop;
	}

	private JPanel getJDataPanel() {
		jTextChannel = new JTextField[totalChannels];
		jDataPanel = new JPanel();
		jDataPanel.setLayout(new BorderLayout());
		JPanel jInnerDataPanel = new JPanel();
		jInnerDataPanel.setLayout(new GridLayout(totalChannels + 1, 2));
		jDataPanel.add(jInnerDataPanel, BorderLayout.NORTH);
		for (int i = 0; i < totalChannels; i++) {

			jInnerDataPanel.add(getJLabelChannel(i));
			jInnerDataPanel.add(getJTextChannel(i));

		}
		jDataPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Channel Counts",
				TitledBorder.LEFT, TitledBorder.TOP, null, Color.black));
		return jDataPanel;
	}

	private JLabel getJLabelChannel(int i) {
		JLabel temp = new JLabel(labels[i]);
		return temp;
	}

	private JTextField getJTextChannel(int i) {
		jTextChannel[i] = new JTextField("0");
		jTextChannel[i].setHorizontalAlignment(SwingConstants.CENTER);
		return jTextChannel[i];
	}

	private JTextField getJTextTime() {
		jTextTime = new JTextField(10);
		jTextTime.setHorizontalAlignment(SwingConstants.CENTER);
		jTextTime.setText("1000");
		return jTextTime;
	}

	private JTextField getJTextSystem() {
		jTextSystem = new JTextField();
		jTextSystem.setHorizontalAlignment(SwingConstants.LEFT);
		return jTextSystem;
	}

	private JCheckBox getJCheckAccumulate() {
		jCheckAccumulate = new JCheckBox();
		jCheckAccumulate.setText("Accumulate");
		jCheckAccumulate.setHorizontalAlignment(SwingConstants.RIGHT);
		jCheckAccumulate.setHorizontalTextPosition(SwingConstants.LEADING);
		jCheckAccumulate.setVerticalAlignment(SwingConstants.CENTER);
		return jCheckAccumulate;
	}
}
