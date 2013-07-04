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

import gda.configuration.properties.LocalProperties;
import gda.device.Adc;
import gda.device.Analyser;
import gda.device.DeviceException;
import gda.device.MCAStatus;
import gda.device.adc.EpicsADC;
import gda.device.detector.analyser.EpicsMCA;
import gda.device.detector.analyser.EpicsMCAPresets;
import gda.device.detector.analyser.EpicsMCARegionOfInterest;
import gda.factory.Finder;
import gda.gui.AcquisitionPanel;
import gda.observable.IObserver;
import gda.plots.SimpleDataCoordinate;
import gda.plots.SimplePlot;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.text.JTextComponent;

import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.ui.Layer;
import org.jscience.physics.quantities.Quantity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * McaGUI Class
 */
public class McaGUI extends AcquisitionPanel implements IObserver {
	
	private static final Logger logger = LoggerFactory.getLogger(McaGUI.class);
	
	/**
	 * color
	 */
	public static Color[] color = { Color.BLUE, Color.RED, Color.CYAN, Color.DARK_GRAY, Color.GRAY, Color.GREEN,
			Color.MAGENTA, Color.ORANGE, Color.PINK, Color.YELLOW, Color.RED, Color.RED, Color.RED, Color.RED,
			Color.RED, Color.RED, Color.RED, Color.RED, Color.RED, Color.RED, Color.RED, Color.RED, Color.RED,
			Color.RED, Color.RED, Color.RED, Color.RED, Color.RED, Color.RED, Color.RED, Color.RED, Color.RED };

	private static final long serialVersionUID = 1L;

	private static final String LABEL_DEFAULT = " ";

	private static final double COUNT_DEFAULT = 0.0;

	private static final double REGION_DEFAULT = -1;

	private static final int BACKGROUND_DEFAULT = -1;

	private static final int CHANNEL_PLOT = 0;

	private static final int ENERGY_PLOT = 1;

	private JPanel namePanel;

	private JPanel timePanel;

	private JPanel roiPanel;

	private JPanel adcPanel;

	private JLabel nameLabel;

	private JButton startButton;

	private JButton stopButton;

	private JButton eraseButton;

	private JLabel statusLabel;

	private JLabel liveTimeLabel;

	private JLabel realTimeLabel;

	private JTextField liveTimeField;

	private JTextField realTimeField;

	private SimplePlot simplePlot;

	private JLabel gainLabel;

	private JLabel offsetLabel;

	private JLabel lldLabel;

	private JTextField gainField;

	private JTextField offsetField;

	private JTextField lldField;

	private String mcaName;

	private Analyser analyser;

	private int regionClickCount = -1;

	private double[] regionLow;

	private double[] regionHigh;

	private int lastSetRegion = -1;

	private McaCalibrationPanel energyCalibrationDialog;

	private HashMap<Integer, double[]> regionMap = new HashMap<Integer, double[]>();

	private double offset;

	private double slope;

	private double quadratic;

	private boolean calibrationAvailable;

	private boolean configured;

	private JPanel plotPanel;

	// private JComboBox xaxisCombo;
	private JLabel configLabel;

	private int selectedPlot = CHANNEL_PLOT;

	private JButton adcControl;

	private JDialog adcDialog;

	private AdcPanel adcControlPanel;

	private JButton tcaControl;

	private JDialog tcaDialog;

	private TcaPanel tcaControlPanel;

	private boolean usePolyConverter = false;

	/**
	 * Constructor
	 */
	public McaGUI()

	{
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		Box vertBox = Box.createVerticalBox();
		vertBox.add(getTimePanel());
		vertBox.add(getAdcPanel());
		Box horiBox = Box.createHorizontalBox();
		horiBox.add(vertBox);
		horiBox.add(getRoiPanel());
		this.add(getNamePanel());

		this.add(horiBox);
		makePlotPanel();
		this.add(plotPanel);

		Thread plotThread = uk.ac.gda.util.ThreadManager.getThread(new PlotUpdateWorker(), "MCA Plot Update thread");
		plotThread.start();
		
		setLabel("Fluorescence Detector Control");
	}

	private JPanel getAdcPanel() {
		gainLabel = new JLabel("Gain");
		offsetLabel = new JLabel("Offset");
		lldLabel = new JLabel("LLD");
		gainField = new JTextField(10);
		gainField.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				setAdcValues();

			}

		});
		offsetField = new JTextField(10);
		offsetField.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				setAdcValues();

			}

		});
		lldField = new JTextField(10);
		lldField.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				setAdcValues();

			}

		});
		adcPanel = new JPanel();
		adcPanel.setLayout(new GridBagLayout());
		GridBagConstraints gc = new GridBagConstraints();
		gc.gridx = GridBagConstraints.RELATIVE;
		gc.gridy = 0;
		adcPanel.add(gainLabel, gc);
		adcPanel.add(gainField, gc);
		gc.gridx = 0;
		gc.gridy = GridBagConstraints.RELATIVE;
		adcPanel.add(offsetLabel, gc);
		gc.gridx = GridBagConstraints.RELATIVE;
		gc.gridy = 1;
		adcPanel.add(offsetField, gc);
		gc.gridx = 0;
		gc.gridy = 2;
		adcPanel.add(lldLabel, gc);
		gc.gridx = GridBagConstraints.RELATIVE;
		adcPanel.add(lldField, gc);
		adcPanel.setBorder(BorderFactory.createTitledBorder(new EtchedBorder(), "ADC"));
		return adcPanel;
	}

	private SimplePlot getSimplePlot() {
		if (simplePlot == null) {
			simplePlot = new SimplePlot();
			simplePlot.setYAxisLabel("Values");
			simplePlot.setTitle("MCA");
			/*
			 * do not attempt to get calibration until the analyser is available getEnergyCalibration();
			 */

			simplePlot.setXAxisLabel("Channel Number");

			simplePlot.setTrackPointer(true);
			JPopupMenu menu = simplePlot.getPopupMenu();
			JMenuItem item = new JMenuItem("Add Region Of Interest");
			menu.add(item);
			item.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					JOptionPane.showMessageDialog(null, "To add a region of interest, please click on region low"
							+ " and region high channels\n" + "in the graph and set the index\n");
					regionClickCount = 0;
				}
			});

			JMenuItem calibitem = new JMenuItem("Calibrate Energy");
			/*
			 * Comment out as calibration is to come from the analyser directly menu.add(calibitem);
			 */
			calibitem.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						// regionClickCount = 0;
						int[] data = (int[]) analyser.getData();
						double[] dData = new double[data.length];
						for (int i = 0; i < dData.length; i++) {
							dData[i] = data[i];
						}
						if (energyCalibrationDialog == null) {
							energyCalibrationDialog = new McaCalibrationPanel((EpicsMCARegionOfInterest[]) analyser
									.getRegionsOfInterest(), dData, mcaName);
							energyCalibrationDialog.addIObserver(McaGUI.this);
						}
						energyCalibrationDialog.setVisible(true);
					} catch (DeviceException e1) {
						logger.error("Exception: " + e1.getMessage());
					} catch (Exception ex) {
						JOptionPane.showMessageDialog(null, ex.getMessage());
						ex.printStackTrace();
					}
				}

			});

			simplePlot.addMouseListener(new MouseAdapter() {

				@Override
				public void mouseClicked(MouseEvent me) {
					// /////////System.out.println("Mouse clicked " +
					// me.getX() + " "
					// /////// + me.getY());
					SimpleDataCoordinate coordinates = simplePlot.convertMouseEvent(me);
					if (simplePlot.getScreenDataArea().contains(me.getX(), me.getY()) && regionClickCount == 0) {
						regionLow = coordinates.toArray();
						regionClickCount++;
					} else if (simplePlot.getScreenDataArea().contains(me.getX(), me.getY()) && regionClickCount == 1) {
						regionHigh = coordinates.toArray();
						regionClickCount++;

						if (regionValid(regionLow[0], regionHigh[0])) {

							final String s = (String) JOptionPane.showInputDialog(null,
									"Please select the Region Index:\n", "Region Of Interest",
									JOptionPane.PLAIN_MESSAGE, null, null, String.valueOf(getNextRegion()));
							Thread t1 = uk.ac.gda.util.ThreadManager.getThread(new Runnable() {
								@Override
								public void run() {

									try {

										if (s != null) {
											int rIndex = Integer.parseInt(s);
											EpicsMCARegionOfInterest[] epc = { new EpicsMCARegionOfInterest() };
											epc[0].setRegionLow(regionLow[0]);
											epc[0].setRegionHigh(regionHigh[0]);
											epc[0].setRegionIndex(rIndex);
											epc[0].setRegionName("region " + rIndex);
											analyser.setRegionsOfInterest(epc);

											addRegionMarkers(rIndex, regionLow[0], regionHigh[0]);
										}

									} catch (DeviceException e) {
										logger.error("Unable to set the table values");
									}

								}

							});
							t1.start();
						}
					}
				}

			});

			// TODO note that selectePlot cannot be changed runtime
			simplePlot.initializeLine(selectedPlot);
			simplePlot.setLineName(selectedPlot, getSelectedPlotString());
			simplePlot.setLineColor(selectedPlot, getSelectedPlotColor());
			simplePlot.setLineType(selectedPlot, "LineOnly");

		}
		return simplePlot;
	}

	private void makePlotPanel() {
		plotPanel = new JPanel();
		// String[] v = {"Plot Channel Number", "Plot Energy"};
		// xaxisCombo = new JComboBox(v);
		configLabel = new JLabel("         ");
		JPanel pane = new JPanel();
		adcControl = new JButton("Adc Controls");
		adcControl.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (adcControlPanel != null)
					adcDialog.setVisible(true);
			}

		});
		tcaControl = new JButton("Tca Controls");
		tcaControl.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (tcaControlPanel != null)
					tcaDialog.setVisible(true);
			}

		});
		makeAdcControlDialog();
		makeTcaControlDialog();
		pane.setLayout(new GridBagLayout());
		GridBagConstraints gcs = new GridBagConstraints();
		gcs.gridx = 0;
		gcs.gridy = 0;
		gcs.gridwidth = GridBagConstraints.RELATIVE;
		// pane.add(xaxisCombo, gcs);
		gcs.gridx++;
		// gcs.gridwidth = GridBagConstraints.REMAINDER;
		pane.add(configLabel, gcs);
		gcs.gridx = GridBagConstraints.RELATIVE;
		pane.add(Box.createHorizontalStrut(500), gcs);
		pane.add(adcControl, gcs);
		pane.add(tcaControl, gcs);
		plotPanel.setLayout(new BorderLayout());
		plotPanel.add(getSimplePlot(), BorderLayout.CENTER);
		plotPanel.add(pane, BorderLayout.SOUTH);

	}

	private void makeTcaControlDialog() {
		if (tcaControlPanel == null) {
			tcaControlPanel = new TcaPanel();
			tcaDialog = new JDialog();
			Object[] options = { "OK" };
			Object[] array = { tcaControlPanel };

			// Create the JOptionPane.
			final JOptionPane optionPane = new JOptionPane(array, JOptionPane.PLAIN_MESSAGE, JOptionPane.YES_OPTION,
					null, options, options[0]);
			optionPane.addPropertyChangeListener(new PropertyChangeListener() {

				@Override
				public void propertyChange(PropertyChangeEvent e) {
					String prop = e.getPropertyName();

					if (isVisible()
							&& (e.getSource() == optionPane)
							&& (JOptionPane.VALUE_PROPERTY.equals(prop) || JOptionPane.INPUT_VALUE_PROPERTY
									.equals(prop))) {
						Object value = optionPane.getValue();

						if (value == JOptionPane.UNINITIALIZED_VALUE) {
							// ignore reset
							return;
						}

						// Reset the JOptionPane's value.
						// If you don't do this, then if the user
						// presses the same button next time, no
						// property change event will be fired.
						optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);

						if ("OK".equals(value)) {

							tcaDialog.setVisible(false);
						}
					}

				}

			});
			tcaDialog.setContentPane(optionPane);
			tcaDialog.pack();
			tcaDialog.setTitle("TCA Controls");
			tcaDialog.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);

		}

	}

	private void makeAdcControlDialog() {
		if (adcControlPanel == null) {
			adcControlPanel = new AdcPanel();
			adcDialog = new JDialog();
			Object[] options = { "OK" };
			Object[] array = { adcControlPanel };

			// Create the JOptionPane.
			final JOptionPane optionPane = new JOptionPane(array, JOptionPane.PLAIN_MESSAGE, JOptionPane.YES_OPTION,
					null, options, options[0]);
			optionPane.addPropertyChangeListener(new PropertyChangeListener() {

				@Override
				public void propertyChange(PropertyChangeEvent e) {
					String prop = e.getPropertyName();

					if (isVisible()
							&& (e.getSource() == optionPane)
							&& (JOptionPane.VALUE_PROPERTY.equals(prop) || JOptionPane.INPUT_VALUE_PROPERTY
									.equals(prop))) {
						Object value = optionPane.getValue();

						if (value == JOptionPane.UNINITIALIZED_VALUE) {
							// ignore reset
							return;
						}

						// Reset the JOptionPane's value.
						// If you don't do this, then if the user
						// presses the same button next time, no
						// property change event will be fired.
						optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);

						if ("OK".equals(value)) {

							adcDialog.setVisible(false);
						}
					}

				}

			});
			adcDialog.setContentPane(optionPane);
			adcDialog.pack();
			adcDialog.setTitle("ADC Controls");
			adcDialog.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);

		}

	}

	private double getMCAChannelToEnergy(int channel) {
		double res = 0;
		try {
			res = Quantity.valueOf((String) analyser.getAttribute(EpicsMCA.channelToEnergyPrefix + channel))
					.getAmount();
		} catch (Exception e) {
			logger.error("Exception: " + e.getMessage());
		}
		return res;
	}

	private boolean getEnergyCalibration() {
		usePolyConverter = getEnergyPolyConverter();
		if (!usePolyConverter) {
			// get slope and offset
			try {
				double valAtZero = getMCAChannelToEnergy(0);
				int numberOfChannels = (Integer) analyser.getAttribute(EpicsMCA.numberOfChannelsAttr);
				double valAtEnd = getMCAChannelToEnergy(numberOfChannels);
				slope = (valAtEnd - valAtZero) / numberOfChannels;
				offset = valAtZero;
				quadratic = 0.;
				usePolyConverter = true;
			} catch (Exception e) {
				logger.error("Exception: " + e.getMessage());
			}
		}
		return usePolyConverter;
	}

	private boolean getEnergyPolyConverter() {
		boolean foundOffset = false, foundSlope = false, foundQuadratic = false;
		String fileName = LocalProperties.get("mca.calibration.dir", ".");
		File file = new File(fileName + File.separator + mcaName);
		if (file.exists()) {
			try {
				BufferedReader reader = new BufferedReader(new FileReader(file));
				while (true) {
					String line = reader.readLine();
					if (line == null)
						break;

					if (line.startsWith("offset")) {
						this.offset = Double.parseDouble(line.substring(line.indexOf("=") + 1));
						foundOffset = true;
					}
					if (line.startsWith("slope")) {
						this.slope = Double.parseDouble(line.substring(line.indexOf("=") + 1));
						foundSlope = true;
					}
					if (line.startsWith("quadratic")) {
						this.quadratic = Double.parseDouble(line.substring(line.indexOf("=") + 1));
						foundQuadratic = true;
					}

				}
				reader.close();
			} catch (NumberFormatException e) {
				logger.error("Exception: " + e.getMessage());
			} catch (FileNotFoundException e) {
				logger.error("Exception: " + e.getMessage());
			} catch (IOException e) {
				logger.error("Exception: " + e.getMessage());
			}
		}
		return foundOffset && foundSlope && foundQuadratic;
	}

	protected void addRegionMarkers(int region, double d, double e) {
		if (regionMap.containsKey(region)) {
			double f[] = regionMap.get(region);
			if (d == f[0] && e == f[1])
				return;

			removeRegionMarkers(region, f[0], f[1]);
		}
		XYPlot xy = simplePlot.getChart().getXYPlot();
		xy.addDomainMarker(new ValueMarker(d, color[region], new BasicStroke(1), color[region], null, 180),
				Layer.FOREGROUND);
		xy.addDomainMarker(new ValueMarker(e, color[region], new BasicStroke(1), color[region], null, 180),
				Layer.FOREGROUND);
		regionMap.put(region, new double[] { d, e });

	}

	@SuppressWarnings("unchecked")
	protected void removeRegionMarkers(int region, double d, double e) {
		XYPlot xy = simplePlot.getChart().getXYPlot();
		int found = 0;
		Collection<ValueMarker> c = xy.getDomainMarkers(Layer.FOREGROUND);
		if (c != null) {
			Iterator<ValueMarker> i = c.iterator();
			xy.clearDomainMarkers();
			while (i.hasNext()) {
				ValueMarker m = i.next();
				if (found < 2 && (m.getValue() == d || m.getValue() == e)) {
					found++;
				}

				else
					xy.addDomainMarker(m, Layer.FOREGROUND);
			}
			if (regionMap.containsKey(region))
				regionMap.remove(region);
		}
	}

	protected boolean regionValid(double low, double high) {
		return (low >= 0 && high >= 0 && high >= low) ? true : false;
	}

	protected int getNextRegion() {
		if (++lastSetRegion == 32)
			lastSetRegion = 0;
		return lastSetRegion;
	}

	private MCATableModel model;

	private JTable roiTable;

	private JScrollPane roiScrollPane;

	private JLabel roiLabel;

	private JButton updateButton;

	private JButton cancelButton;

	private JButton refreshButton;

	private boolean tableDataSet;

	private Adc adc;

	private String adcName;

	private String tcaName;

	// private JLabel deadTimeLabel;
	// private JTextField deadTimeField;
	private JTextField presetRealTimeField;

	private JTextField presetLiveTimeField;

	private JTextField numberChannelsField;

	private static boolean SELECTION_DEFAULT = false;

	private JTable getRoiTable() {
		if (roiTable == null) {
			roiTable = new JTable() {
				// Place cell in edit mode when it 'gains focus'

				/**
				 * 
				 */
				private static final long serialVersionUID = 1L;

				@Override
				public void changeSelection(final int row, final int column, boolean toggle, boolean extend) {
					super.changeSelection(row, column, toggle, extend);

					if (editCellAt(row, column))
						getEditorComponent().requestFocusInWindow();
				}

				//
				// Select the text when the cell starts editing

				@Override
				public boolean editCellAt(int row, int column, EventObject e) {
					boolean result = super.editCellAt(row, column, e);
					final Component editor = getEditorComponent();

					if (editor != null && editor instanceof JTextComponent) {
						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								((JTextComponent) editor).selectAll();
							}
						});
					}

					return result;
				}
			};

			// roiTable.setSize(new Dimension(620, 70));
			roiTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
			String[] column = { "Select", "Index", "Label", "Sum", "Net", "Low", "High", "Background", "Preset Values" };
			Class<?>[] columnTypes = { Boolean.class, Integer.class, String.class, Double.class, Double.class,
					Double.class, Double.class, Integer.class, Double.class };

			model = new MCATableModel(column, columnTypes, 0);
			boolean[] editable = { true, false, true, false, false, true, true, true, true }; // 32);
			roiTable.setModel(model);
			model.setEditValues(editable);
			roiTable.setRowSelectionAllowed(false);
			roiTable.setColumnSelectionAllowed(false);
		}
		return roiTable;
	}

	private JScrollPane getRoiScrollPane() {
		if (roiScrollPane == null) {
			roiScrollPane = new JScrollPane();
			roiScrollPane.setViewportView(getRoiTable());
			roiTable.setPreferredScrollableViewportSize(new Dimension(500, 70));
		}
		return roiScrollPane;
	}

	private JPanel getRoiPanel() {
		roiLabel = new JLabel("Regions Of Interest");
		refreshButton = new JButton("Refresh");
		refreshButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Thread t1 = uk.ac.gda.util.ThreadManager.getThread(new Runnable() {

					@Override
					public void run() {
						try {

							// System.out.println("set table data called
							// from refresh
							// method");
							setTableData((EpicsMCARegionOfInterest[]) analyser.getRegionsOfInterest(), analyser
									.getRegionsOfInterestCount());
							updateAdcValues();
							// System.out.println("after setting the table
							// data " +
							// analyser);
						} catch (DeviceException de) {
							logger.error("Unable to get MCA regions of Interest Information " + de.getMessage());
						}
					}
				});
				t1.start();

			}

		});
		updateButton = new JButton("Set");
		updateButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Thread t = uk.ac.gda.util.ThreadManager.getThread(new Runnable() {
					@Override
					public void run() {
						int rows = roiTable.getRowCount();
						Vector<EpicsMCARegionOfInterest> roi = new Vector<EpicsMCARegionOfInterest>();
						for (int i = 0; i < rows; i++) {
							if (((Boolean) model.getValueAt(i, 0)).booleanValue()
									&& regionValid(((Double) model.getValueAt(i, 5)).doubleValue(), ((Double) model
											.getValueAt(i, 6)).doubleValue())) {
								roi.add(new EpicsMCARegionOfInterest(((Integer) model.getValueAt(i, 1)).intValue(),
										((Double) model.getValueAt(i, 5)).doubleValue(), ((Double) model.getValueAt(i,
												6)).doubleValue(), ((Integer) model.getValueAt(i, 7)).intValue(),
										((Double) model.getValueAt(i, 8)).doubleValue(), (String) model
												.getValueAt(i, 2)));
							}

						}
						if (roi.size() > 0) {
							Object[] obj = roi.toArray();
							EpicsMCARegionOfInterest eroi[] = new EpicsMCARegionOfInterest[obj.length];
							for (int j = 0; j < eroi.length; j++) {
								eroi[j] = (EpicsMCARegionOfInterest) obj[j];
							}

							try {
								tableDataSet = true;
								analyser.setRegionsOfInterest(eroi);
								tableDataSet = false;
							} catch (DeviceException e) {
								logger.error("Unable to set the table values");
								tableDataSet = false;
							}
						}
					}
				});
				t.start();

			}

		});
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Thread t2 = uk.ac.gda.util.ThreadManager.getThread(new Runnable() {
					@Override
					public void run() {
						int rows = roiTable.getRowCount();
						Vector<EpicsMCARegionOfInterest> roi = new Vector<EpicsMCARegionOfInterest>();
						for (int i = 0; i < rows; i++) {
							if (((Boolean) model.getValueAt(i, 0)).booleanValue()) {
								// add
								removeRegionMarkers(((Integer) model.getValueAt(i, 1)).intValue(), ((Double) model
										.getValueAt(i, 5)).doubleValue(), ((Double) model.getValueAt(i, 6))
										.doubleValue());
								roi.add(new EpicsMCARegionOfInterest(((Integer) model.getValueAt(i, 1)).intValue(), -1,
										-1, -1, -1, ""));
							}

						}
						if (roi.size() > 0) {
							Object[] obj = roi.toArray();
							EpicsMCARegionOfInterest eroi[] = new EpicsMCARegionOfInterest[obj.length];
							for (int j = 0; j < eroi.length; j++) {
								eroi[j] = (EpicsMCARegionOfInterest) obj[j];
							}

							try {
								tableDataSet = true;
								analyser.setRegionsOfInterest(eroi);
								tableDataSet = false;
							} catch (DeviceException e) {
								logger.error("Unable to set the table values");
								tableDataSet = false;
							}
						}
					}
				});
				t2.start();

			}

		});
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(refreshButton);
		buttonPanel.add(updateButton);
		buttonPanel.add(cancelButton);
		roiPanel = new JPanel();
		roiPanel.setLayout(new BorderLayout());
		roiPanel.add(roiLabel, BorderLayout.NORTH);
		roiPanel.add(getRoiScrollPane(), BorderLayout.CENTER);
		roiPanel.add(buttonPanel, BorderLayout.SOUTH);
		return roiPanel;
	}

	private JPanel getTimePanel() {
		liveTimeLabel = new JLabel("Live Time");
		realTimeLabel = new JLabel("Real Time");
		JLabel presetLiveTimeLabel = new JLabel("Preset Live Time");
		JLabel presetRealTimeLabel = new JLabel("Preset Real Time");
		JLabel numberChannelsLabel = new JLabel("Number Of Channels");
		// deadTimeLabel = new JLabel("Dead Time");
		liveTimeField = new JTextField(10);
		liveTimeField.setEditable(false);
		realTimeField = new JTextField(10);
		realTimeField.setEditable(false);
		presetLiveTimeField = new JTextField(10);
		presetLiveTimeField.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				setPresetValues();

			}

		});
		presetRealTimeField = new JTextField(10);
		presetRealTimeField.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				setPresetValues();

			}

		});
		numberChannelsField = new JTextField(10);
		numberChannelsField.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				setNumberChannels();

			}

		});
		// deadTimeField = new JTextField(10);
		timePanel = new JPanel();
		timePanel.setLayout(new GridBagLayout());
		GridBagConstraints gc = new GridBagConstraints();
		gc.gridx = GridBagConstraints.RELATIVE;
		gc.gridy = 0;
		timePanel.add(liveTimeLabel, gc);
		timePanel.add(liveTimeField, gc);
		gc.gridx = 0;
		gc.gridy = GridBagConstraints.RELATIVE;
		timePanel.add(presetLiveTimeLabel, gc);
		gc.gridx = GridBagConstraints.RELATIVE;
		gc.gridy = 1;
		timePanel.add(presetLiveTimeField, gc);
		gc.gridx = 0;
		gc.gridy = GridBagConstraints.RELATIVE;
		timePanel.add(realTimeLabel, gc);
		gc.gridx = GridBagConstraints.RELATIVE;
		gc.gridy = 2;
		timePanel.add(realTimeField, gc);
		gc.gridx = 0;
		gc.gridy = GridBagConstraints.RELATIVE;
		timePanel.add(presetRealTimeLabel, gc);
		gc.gridx = GridBagConstraints.RELATIVE;
		gc.gridy = 3;
		timePanel.add(presetRealTimeField, gc);
		gc.gridx = 0;
		gc.gridy = GridBagConstraints.RELATIVE;
		timePanel.add(numberChannelsLabel, gc);
		gc.gridx = GridBagConstraints.RELATIVE;
		gc.gridy = 4;
		timePanel.add(numberChannelsField, gc);
		gc.gridx = 0;
		gc.gridy = 2;
		return timePanel;
	}

	protected void setNumberChannels() {
		long numberChannels = 0;

		try {
			numberChannels = analyser.getNumberOfChannels();
			analyser.setNumberOfChannels(Long.parseLong(numberChannelsField.getText()));
		} catch (DeviceException e) {
			logger.error("Unable to set Number of channels to use" + e.getMessage());

		} catch (NumberFormatException nme) {
			JOptionPane.showMessageDialog(this, "Invalid channel number : " + nme.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
			numberChannelsField.setText(String.valueOf(numberChannels));
		}
	}

	protected void setPresetValues() {
		// System.out.println("inside preset set values");

		double preliveTime = 0.0;
		double preRealTime = 0.0;
		try {
			EpicsMCAPresets preset = (EpicsMCAPresets) analyser.getPresets();
			preliveTime = preset.getPresetLiveTime();
			preRealTime = preset.getPresetRealTime();
			preset.setPresetLiveTime(Float.parseFloat(presetLiveTimeField.getText()));
			preset.setPresetRealTime(Float.parseFloat(presetRealTimeField.getText()));
			analyser.setPresets(preset);

		} catch (DeviceException e) {
			logger.error("Unable to set Preset time values");

		} catch (NumberFormatException nme) {
			JOptionPane.showMessageDialog(this, "Invalid Analyser Preset live/real times values : " + nme.getMessage(),
					"Error", JOptionPane.ERROR_MESSAGE);
			presetRealTimeField.setText(String.valueOf(preRealTime));
			presetLiveTimeField.setText(String.valueOf(preliveTime));
		}

	}

	private JPanel getNamePanel() {
		nameLabel = new JLabel("MultiChannelAnalyser");
		startButton = new JButton("Start");
		startButton.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent e) {
				try {
					analyser.startAcquisition();
					// if(xaxisCombo.getSelectedIndex() == ENERGY_PLOT)
					// selectedPlot = ENERGY_PLOT;
					// else
					// selectedPlot = CHANNEL_PLOT;
					// xaxisCombo.setEnabled(false);

				} catch (DeviceException e1) {
					logger.error(e1.getMessage());

				}
			}
		});
		stopButton = new JButton("Stop");
		stopButton.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent e) {
				try {
					analyser.stopAcquisition();
					// if(calibrationAvailable)
					// xaxisCombo.setEnabled(true);

				} catch (DeviceException e1) {
					logger.error(e1.getMessage());
				}
			}
		});
		eraseButton = new JButton("Erase");
		eraseButton.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent e) {
				try {
					analyser.clear();
				} catch (DeviceException e1) {
					logger.error(e1.getMessage());
				}
			}
		});
		statusLabel = new JLabel("Status");
		namePanel = new JPanel();
		namePanel.setLayout(new BoxLayout(namePanel, BoxLayout.X_AXIS));
		namePanel.add(nameLabel);
		namePanel.add(Box.createHorizontalStrut(10));
		namePanel.add(startButton);
		namePanel.add(Box.createHorizontalStrut(10));
		namePanel.add(stopButton);
		namePanel.add(Box.createHorizontalStrut(10));
		namePanel.add(eraseButton);
		namePanel.add(Box.createHorizontalStrut(10));
		namePanel.add(statusLabel);
		namePanel.add(Box.createHorizontalStrut(10));
		return namePanel;
	}

	/**
	 * @return mca name
	 */
	public String getMcaName() {
		return mcaName;
	}

	/**
	 * @param mcaName
	 */
	public void setMcaName(String mcaName) {
		// ///System.out.println("the mca name is set to "+ mcaName);
		this.mcaName = mcaName;
	}

	@Override
	public void configure() {
		if (!configured) {
			try {
				logger.debug("confiuring mca");
				analyser = (Analyser) Finder.getInstance().find(this.mcaName);
				// /System.out.println("MCA FOUND " + analyser);
				adc = (Adc) Finder.getInstance().find(this.adcName);
				if (analyser != null) {
					analyser.addIObserver(this);
					// new timeFieldUpdater().start();
					final float[] li = (float[]) analyser.getElapsedParameters();
					final long channels = analyser.getNumberOfChannels();
					final EpicsMCAPresets preset = (EpicsMCAPresets) analyser.getPresets();
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							liveTimeField.setText(String.valueOf(li[1]));
							realTimeField.setText(String.valueOf(li[0]));
							numberChannelsField.setText(String.valueOf(channels));
							presetRealTimeField.setText(String.valueOf(preset.getPresetRealTime()));
							presetLiveTimeField.setText(String.valueOf(preset.getPresetLiveTime()));
							updateAdcValues();

							try {
								// //////System.out.println("before setting plot
								// " +
								// analyser);
								calibrationAvailable = getEnergyCalibration();
								if (calibrationAvailable) {
									configLabel.setText("       ");
									setupEnergyPlot();
								} else {
									configLabel.setText("MCA Energy calibration coefficients not found");
									// xaxisCombo.setEnabled(false);
								}
								updatePlot((int[]) analyser.getData());
								// System.out.println("after setting plot " +
								// analyser);

							} catch (DeviceException e) {
								logger.error("device exception in  Simpleplot get data");
								e.printStackTrace();
							}

							try {
								// ////// System.out.println("set table data
								// called from
								// configure method");
								initialiseTable(analyser.getNumberOfRegions());
								setTableData((EpicsMCARegionOfInterest[]) analyser.getRegionsOfInterest(), analyser
										.getRegionsOfInterestCount());
							} catch (DeviceException e) {
								logger
										.error("Unable to get MCA regions of" + " Interest Information "
												+ e.getMessage());
							}

						}
					});
				}

				if (adcControlPanel != null) {
					adcControlPanel.setAdcName(adcName);
					adcControlPanel.configure();
				}
				if (tcaControlPanel != null) {
					tcaControlPanel.setTcaName(tcaName);
					tcaControlPanel.configure();
				}
				// super.configure();
			}
			/*
			 * catch (FactoryException e) { Message.alarm("Factory exception in "); e.printStackTrace(); }
			 */
			catch (DeviceException e) {
				logger.error("Exception: " + e.getMessage());
			}
		}
		configured = true;
	}

	@Override
	public void update(Object theObserved, final Object changeCode) {
		// System.out.println("inside update" + configured + " " + changeCode);
		if (configured) {
			if (theObserved instanceof Analyser) {
				if (changeCode instanceof MCAStatus) {
					String statusString = null;
					// System.out.println("Status Update");//,
					// Message.Level.FOUR);
					MCAStatus s = (MCAStatus) changeCode;
					if (s.value() == MCAStatus._BUSY)
						statusString = "Acquiring";
					else if (s.value() == MCAStatus._READY)
						statusString = "Done";
					updateStatus(statusString);

				} else if (changeCode instanceof int[]) {
					// c//, Message.Level.FOUR);
					updatePlot((int[]) changeCode);

				} else if (changeCode instanceof EpicsMCARegionOfInterest) {
					// System.out.println("roi Update");
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							EpicsMCARegionOfInterest roi = (EpicsMCARegionOfInterest) changeCode;
							// System.out.println("set table data called
							// from update" +
							// "method");
							setRoiData(roi);

						}
					});
				} else if (changeCode instanceof float[]) {
					// System.out.println("inside float[] 0");
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							float roc[] = (float[]) changeCode;
							realTimeField.setText(String.valueOf(roc[0]));
							liveTimeField.setText(String.valueOf(roc[1]));
						}
					});

				}

				else if (changeCode instanceof double[]) {
					// System.out.println("inside double[] 0");
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							double roc[] = (double[]) changeCode;
							setRoiCountData(roc);
						}
					});

				}

			} else if (theObserved instanceof McaCalibrationPanel) {
				calibrationAvailable = getEnergyCalibration();
				if (calibrationAvailable) {
					configLabel.setText("  ");
					setupEnergyPlot();
				}
			}
		}

	}

	private void initialiseTable(int rows) {
		model.setRowCount(rows);
		for (int index = 0; index < rows; index++) {

			model.setValueAt(McaGUI.SELECTION_DEFAULT, index, 0);
			model.setValueAt(index, index, 1);
			model.setValueAt(LABEL_DEFAULT, index, 2);
			model.setValueAt(COUNT_DEFAULT, index, 3);
			model.setValueAt(McaGUI.COUNT_DEFAULT, index, 4);
			model.setValueAt(McaGUI.REGION_DEFAULT, index, 6);
			model.setValueAt(McaGUI.REGION_DEFAULT, index, 5);
			model.setValueAt(McaGUI.BACKGROUND_DEFAULT, index, 7);
			model.setValueAt(McaGUI.REGION_DEFAULT, index, 8);
		}

	}

	private void setTableData(EpicsMCARegionOfInterest[] roi, double[][] roiCounts) {
		// /////////////System.out.println("the data set boolean is " +
		// tableDataSet);
		if (!tableDataSet && roi != null) {
			for (int i = 0; i < roi.length; i++) {
				// System.out.println("setting table datda " + i);
				int index = roi[i].getRegionIndex();

				model.setValueAt(false, index, 0);
				model.setValueAt(index, index, 1);
				model.setValueAt(roi[i].getRegionName(), index, 2);
				model.setValueAt(roiCounts[index][0], index, 3);
				model.setValueAt(roiCounts[index][1], index, 4);
				double low = roi[i].getRegionLow();
				double high = roi[i].getRegionHigh();
				model.setValueAt(high, index, 6);
				model.setValueAt(low, index, 5);
				model.setValueAt(roi[i].getRegionBackground(), index, 7);
				model.setValueAt(roi[i].getRegionPreset(), index, 8);
				if (regionValid(low, high)) {
					lastSetRegion = index;
					this.addRegionMarkers(index, low, high);
				}

			}
		}
		// System.out.println("The latest r index is" + lastSetRegion);

	}

	private void setRoiData(EpicsMCARegionOfInterest roi) {
		// /////////////System.out.println("the data set boolean is " +
		// tableDataSet);
		if (!tableDataSet && roi != null) {

			int index = roi.getRegionIndex();

			model.setValueAt(false, index, 0);
			model.setValueAt(index, index, 1);
			model.setValueAt(roi.getRegionName(), index, 2);
			// model.setValueAt(roiCounts[index][0], index, 3);
			// model.setValueAt(roiCounts[index][1], index, 4);
			double low = roi.getRegionLow();
			double high = roi.getRegionHigh();
			model.setValueAt(high, index, 6);
			model.setValueAt(low, index, 5);
			model.setValueAt(roi.getRegionBackground(), index, 7);
			model.setValueAt(roi.getRegionPreset(), index, 8);

		}
		// System.out.println("The latest r index is" + lastSetRegion);

	}

	private void setRoiCountData(double[] roiCounts) {
		// /////////////System.out.println("the data set boolean is " +
		// tableDataSet);
		if (!tableDataSet && roiCounts != null) {

			int index = (int) roiCounts[0];

			model.setValueAt(roiCounts[1], index, 3);
			model.setValueAt(roiCounts[2], index, 4);

		}
		// System.out.println("The latest r index is" + lastSetRegion);

	}

	private void updateStatus(final String statusString2) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				statusLabel.setText(statusString2);
			}

		});
	}

	volatile int[] lastPlotData;

	private void redrawPlot(final int[] data) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				double datax[] = new double[data.length];
				double datay[] = new double[data.length];
				for (int i = 0; i < datax.length; i++) {
					if (selectedPlot == ENERGY_PLOT)
						datax[i] = channelToEnergy(i);
					else
						datax[i] = i;
					datay[i] = data[i];
				}

				simplePlot.setLinePoints(selectedPlot, datax, datay);

				lastPlotData = data;
			}
		});
	}

	volatile int[] dataToPlot;

	class PlotUpdateWorker implements Runnable {
		@Override
		public void run() {
			// TODO cancel somehow
			while (true) {
				final int[] data = dataToPlot;
				if (data != null && lastPlotData != data) {
					int i = 0;
					if (lastPlotData != null && lastPlotData.length == data.length)
						for (; i < data.length; i++)
							if (data[i] != lastPlotData[i])
								break;

					// equal do not plot
					if (i == data.length)
						lastPlotData = data;
					else
						redrawPlot(dataToPlot);
				}

				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
			}
		}
	}

	private void updatePlot(final int[] data) {
		dataToPlot = data;
	}

	private Color getSelectedPlotColor() {

		switch (selectedPlot) {
		case ENERGY_PLOT:
			return Color.PINK;
		default:
			return Color.GREEN;
		}
	}

	protected String getSelectedPlotString() {
		switch (selectedPlot) {
		case ENERGY_PLOT:
			return "Energy Vs Values";
		default:
			return "Channel vs Values";
		}

	}

	protected double channelToEnergy(int i) // in eV
	{
		double energy = 0;
		if (calibrationAvailable) {
			if (usePolyConverter) {
				energy = offset + i * slope + (i * i) * quadratic;
			} else {
				energy = getMCAChannelToEnergy(i);
			}
		}
		return energy;
	}

	private void updateAdcValues() {
		try {
			gainField.setText(String.valueOf(adc.getAttribute(EpicsADC.GAIN)));
			offsetField.setText(String.valueOf(adc.getAttribute(EpicsADC.OFFSET)));
			lldField.setText(String.valueOf(adc.getAttribute(EpicsADC.LLD)));
		} catch (DeviceException e) {
			logger.error("Exception: " + e.getMessage());
		}
	}

	private void setAdcValues() {

		try {
			adc.setAttribute(EpicsADC.GAIN, Double.parseDouble(gainField.getText()));
			adc.setAttribute(EpicsADC.OFFSET, Double.parseDouble(offsetField.getText()));
			adc.setAttribute(EpicsADC.LLD, Double.parseDouble(lldField.getText()));

		} catch (DeviceException e) {
			logger.error("Unable to set adc values");

		} catch (NumberFormatException nme) {
			JOptionPane.showMessageDialog(this, "Invalid Adc parameters : " + nme.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		JFrame f = new JFrame("MCA TEST");
		f.add(new McaGUI());
		f.setSize(800, 500);
		f.setVisible(true);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	/**
	 * @return adc name
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

	private void setupEnergyPlot() {
		simplePlot.addDependentXAxis(slope, offset);
		simplePlot.setDependentXAxisLabel("Energy");
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
