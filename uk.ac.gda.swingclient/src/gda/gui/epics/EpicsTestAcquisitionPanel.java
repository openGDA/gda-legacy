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

package gda.gui.epics;

import gda.device.DeviceException;
import gda.device.epicsdevice.EpicsCTRL;
import gda.device.epicsdevice.EpicsDBR;
import gda.device.epicsdevice.EpicsMonitorEvent;
import gda.device.epicsdevice.EpicsRegistrationRequest;
import gda.device.epicsdevice.IEpicsChannel;
import gda.device.epicsdevice.IEpicsDevice;
import gda.device.epicsdevice.ReturnType;
import gda.factory.Finder;
import gda.gui.AcquisitionPanel;
import gda.gui.text.parameter.EpicsPanelParameterListener;
import gda.gui.text.parameter.EpicsParameterMonitor;
import gda.gui.text.parameter.Limited;
import gda.gui.text.parameter.ParametersPanelBuilder;
import gda.plots.Marker;
import gda.plots.SimplePlot;
import gda.plots.SimplePlotFrame;
import gda.plots.SimpleXYSeries;
import gda.plots.Type;

import java.awt.Component;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.jfree.data.xy.XYDataItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * EpicsParameterMonitor Class
 */
public class EpicsTestAcquisitionPanel extends AcquisitionPanel {

	/**
	 * Constructor
	 */
	public EpicsTestAcquisitionPanel() {
		super();
		initialize();
	}

	private void initialize() {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(new ButtonPanel(this, false));
	}

}

final class ButtonPanel extends JPanel {
	
	private static final Logger logger = LoggerFactory.getLogger(ButtonPanel.class);
	
	final private JTextField jDeviceField, jRecordField, jFieldField;

	final private JButton jTestButton;

	final private EpicsTestAcquisitionPanel topPanel;

	private SubPanel subPanel;

	private WaveFormPanel waveFormPanel;

	final boolean testLayout;

	/**
	 * @param topPanel1
	 * @param testLayout1
	 */
	public ButtonPanel(EpicsTestAcquisitionPanel topPanel1, boolean testLayout1) {
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		add(jDeviceField = new JTextField());
		add(jRecordField = new JTextField());
		add(jFieldField = new JTextField());
		add(jTestButton = new JButton("Run"));
		this.topPanel = topPanel1;
		this.testLayout = testLayout1;
		jTestButton.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent e) {
				if (subPanel != null) {
					try {
						subPanel.dispose();
					} catch (DeviceException ex) {
						logger.error(ex.getMessage());
					}
					topPanel.remove(subPanel);
					subPanel = null;
				}
				if (waveFormPanel != null) {
					try {
						waveFormPanel.dispose();
					} catch (DeviceException ex) {
						logger.error(ex.getMessage());
					}
					topPanel.remove(waveFormPanel);
					waveFormPanel = null;
				}
				topPanel.add(subPanel = new SubPanel(jDeviceField.getText(), jRecordField.getText(), jFieldField
						.getText(), testLayout));
				topPanel.add(waveFormPanel = new WaveFormPanel(jDeviceField.getText(), jRecordField.getText(),
						jFieldField.getText(), testLayout));
				topPanel.validate();
			}
		});
	}
}

final class WaveFormPanel extends JPanel implements EpicsMonitorListener {
	private final String deviceName;

	private SimplePlotFrame simplePlotFrame = null;

	private EpicsMonitor _monitor;

	/**
	 * @param deviceName
	 * @param recordName
	 * @param fieldName
	 * @param testLayout
	 */
	public WaveFormPanel(String deviceName, String recordName, String fieldName, boolean testLayout) {
		this.deviceName = deviceName;
		simplePlotFrame = new SimplePlotFrame();
		simplePlotFrame.pack();
		simplePlotFrame.setVisible(true);

		if (!testLayout) {
			_monitor = new EpicsMonitor(ReturnType.DBR_CTRL, deviceName, recordName, fieldName, this);
		}
	}

	/**
	 * @throws DeviceException
	 */
	public void dispose() throws DeviceException {
		if (_monitor != null) {
			_monitor.dispose();
		}
		IEpicsDevice epicsDevice = (IEpicsDevice) Finder.getInstance().find(deviceName);
		if (epicsDevice != null)
			epicsDevice.closeUnUsedChannels();
	}

	@Override
	public void update(EpicsMonitor monitor, EpicsRegistrationRequest request, EpicsMonitorEvent event) {
		if (event.epicsDbr instanceof EpicsDBR) {
			EpicsDBR epicsDBR = (EpicsDBR) event.epicsDbr;
			int _count = epicsDBR._count;
			if (_count > 0) {
				double[] xVals = new double[_count];
				for (int i = 0; i < _count; i++)
					xVals[i] = i;
				double[] yVals = new double[_count];
				Object _value = epicsDBR._value;
				if (_value instanceof double[]) {
					double[] data = (double[]) _value;
					int i = 0;
					for (double d : data) {
						yVals[i] = d;
						i++;
					}
				} else if (_value instanceof float[]) {
					float[] data = (float[]) _value;
					int i = 0;
					for (float d : data) {
						yVals[i] = d;
						i++;
					}
				} else if (_value instanceof int[]) {
					int[] data = (int[]) _value;
					int i = 0;
					for (int d : data) {
						yVals[i] = d;
						i++;
					}
				} else if (_value instanceof byte[]) {
					byte[] data = (byte[]) _value;
					int i = 0;
					for (byte d : data) {
						yVals[i] = d;
						i++;
					}
				} else if (_value instanceof short[]) {
					short[] data = (short[]) _value;
					int i = 0;
					for (short d : data) {
						yVals[i] = d;
						i++;
					}
				} else if (_value instanceof Object[]) {
					if (((Object[]) _value)[0] instanceof Integer) {
						Integer[] data = (Integer[]) _value;
						int i = 0;
						for (Integer d : data) {
							yVals[i] = d;
							i++;
						}
					} else if (((Object[]) _value)[0] instanceof Double) {
						Double[] data = (Double[]) _value;
						int i = 0;
						for (Double d : data) {
							yVals[i] = d;
							i++;
						}
					}
				}
				SimplePlot simplePlot = simplePlotFrame.simplePlot;
				SimpleXYSeries xySeries = new SimpleXYSeries("Test",0,0, xVals, yVals);
				xySeries.setType(Type.LINEANDPOINTS);
				xySeries.setMarker(Marker.fromCounter(0));
				simplePlot.initializeLine(xySeries);				
			}
		}
	}
}

/**
 * 
 */
final class MyComparator implements Comparable<Object> {

	@Override
	public int compareTo(Object o) {
		return 0;
	}

}

final class MyXYSeries extends SimpleXYSeries {
	private int numItems;

	double[] xVals;

	double[] yVals;

	MyXYSeries(int numItems, double[] xVals, double[] yVals) {
		super("Test", 0, SimplePlot.LEFTYAXIS);
		this.numItems = numItems;
		this.xVals = xVals;
		this.yVals = yVals;
	}

	@Override
	public XYDataItem getDataItem(int index) {
		return new XYDataItem(xVals[index], yVals[index] + 20.);
	}

	@Override
	public int getItemCount() {
		return numItems;
	}

	@Override
	public List<?> getItems() {
		return super.getItems();
	}

	@Override
	public int getMaximumItemCount() {
		return numItems;
	}

	@Override
	public Number getX(int index) {
		return xVals[index];
	}

	@Override
	public Number getY(int index) {
		return yVals[index];
	}

}

final class SubPanel extends JPanel {
	private static final Logger logger = LoggerFactory.getLogger(SubPanel.class);
	private static int numItems = 10;

	private final ArrayList<EpicsParameterMonitor> monitors;

	private final String deviceName;

	/**
	 * @param deviceName
	 * @param recordName
	 * @param fieldName
	 * @param testLayout
	 */
	public SubPanel(String deviceName, String recordName, String fieldName, boolean testLayout) {
		this.deviceName = deviceName;
		Double minVal = -1.0 * Double.MAX_VALUE;
		Double maxVal = Double.MAX_VALUE;
		{
			IEpicsDevice experimentEpicsDevice = (IEpicsDevice) Finder.getInstance().find(deviceName);
			if (experimentEpicsDevice == null)
				throw new IllegalArgumentException(" ParametersPanelListener. unable to find device" + deviceName);
			IEpicsChannel ctrlChan = experimentEpicsDevice.createEpicsChannel(ReturnType.DBR_CTRL, recordName,
					fieldName, -1.0);
			try {
				Object val = ctrlChan.getValue();
				if (val instanceof EpicsCTRL) {
					EpicsCTRL ctrl = (EpicsCTRL) val;
					if (ctrl._lcl.doubleValue() != ctrl._ucl.doubleValue()) {
						minVal = ctrl._lcl.doubleValue();
						maxVal = ctrl._ucl.doubleValue();
					}
				}
			} catch (Exception e) {
				logger.warn("Unable to get limits for " + deviceName + recordName + fieldName);
			}
		}

		ArrayList<Limited> parametersLimited = new ArrayList<Limited>();
		for (int i = 0; i < numItems; i++) {
			parametersLimited.add(new Limited(i, -1., minVal, maxVal, "%5.5g", "Label", "Tooltip", "units",
					'D', Component.RIGHT_ALIGNMENT, true));
		}
		GridLayout subLayout = new GridLayout(1, 0);
		subLayout.setHgap(10);
		ParametersPanelBuilder panelBuilder = new ParametersPanelBuilder( parametersLimited, 1, "", "Parameters",
				BorderFactory.createTitledBorder("Parameters"), null, null, 5, 70, 5, 10, 10, null, null);
		this.add(panelBuilder);
		if (!testLayout) {
			monitors = new ArrayList<EpicsParameterMonitor>();
			for (int i = 0; i < numItems; i++) {
				panelBuilder.addVetoableChangeListener(ParametersPanelBuilder.ValuePropertyName + Integer.toString(i),
						new EpicsPanelParameterListener(deviceName, recordName, fieldName, -1.0, 0.));
				monitors.add(new EpicsParameterMonitor(deviceName, recordName, fieldName, panelBuilder, i));
			}
		} else
			monitors = null;

	}

	/**
	 * @throws DeviceException
	 */
	public void dispose() throws DeviceException {
		if (monitors != null) {
			for (EpicsParameterMonitor monitor : monitors) {
				monitor.dispose();
			}
		}
		IEpicsDevice epicsDevice = (IEpicsDevice) Finder.getInstance().find(deviceName);
		if (epicsDevice != null)
			epicsDevice.closeUnUsedChannels();
	}
}