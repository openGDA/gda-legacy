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

package gda.gui.xuv;

import gda.data.PathConstructor;
import gda.gui.util.SimpleFileFilter;
import gda.observable.IObservableJPanel;
import gda.plots.SimplePlot;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.PageFormat;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.StringTokenizer;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reads in and plots SRS data file. Note that though this class was originally developed for 5U it is in fact almost
 * completely general and will read any SRS data file. The only problem area is the array plotSelected which remembers
 * what to plot from one set of data to the next. This only works so long as all data files have the same detectors.
 * This should be fixed and then the panel should be promoted out of the gda.gui.xuv package.
 */
public class DataPlotter extends IObservableJPanel {
	
	private static final Logger logger = LoggerFactory.getLogger(DataPlotter.class);
	
	private static final int NO_NORMALISATION = -1;

	private static final int DEFAULT_PLOT = 1;

	private SimplePlot simplePlot;

	private JCheckBox whatToPlotButtons[] = null;

	private JPanel whatToPlotPanel;

	private JRadioButton radioButtons[] = null;

	private JPanel normalizePanel;

	private int whichToNormalizeTo = NO_NORMALISATION;

	private int howManyYValues = 0;

	private boolean[] plotSelected = { false, false };

	private String fileName;

	private boolean is2DPlot;

	private ActionListener whatToPlotSetter;

	private ActionListener normalizer;

	/**
	 * Constructor
	 */
	public DataPlotter() {
		whatToPlotSetter = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setWhatToPlot();
			}
		};

		normalizer = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Normalizing is done by the BFI method of reading in the data
				// again with a new value of whatToNormalizeTo. This may be slow
				// if large numbers of points are involved.
				setWhichToNormalizeTo();
				loadAndPlotData();
			}
		};

		// Create and display panels
		setLayout(new BorderLayout());
		JPanel jpanel = new JPanel(new BorderLayout());

		jpanel.add(createButtonPanel(), BorderLayout.NORTH);
		createWhatToPlotPanel();
		jpanel.add(whatToPlotPanel, BorderLayout.WEST);
		createNormalizePanel();
		jpanel.add(normalizePanel, BorderLayout.EAST);
		add(jpanel, BorderLayout.EAST);

		// Create and display plot
		simplePlot = new SimplePlot();
		simplePlot.setTrackPointer(true);
		simplePlot.setYAxisLabel("various units");
		simplePlot.setXAxisAutoScaling(true);
		simplePlot.setYAxisAutoScaling(true);
		add(simplePlot, BorderLayout.CENTER);
	}

	private JPanel createButtonPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(2, 2));
		JButton openButton = new JButton("Open...");
		openButton.setEnabled(true);
		openButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				JFileChooser jf = new JFileChooser(PathConstructor.createFromDefaultProperty());
				String filter[] = { "dat" };
				SimpleFileFilter dataFilter = new SimpleFileFilter(filter, "Run-number (*.dat)");

				jf.resetChoosableFileFilters();
				jf.addChoosableFileFilter(dataFilter);

				if (jf.showDialog(getRootPane(), "Load") == JFileChooser.APPROVE_OPTION) {
					if (jf.getSelectedFile() != null) {
						logger.debug("adding Data line " + " @ " + new Date());
						fileName = jf.getSelectedFile().getAbsolutePath();
						loadAndPlotData();
						logger.debug("adding Data line " + " @ " + new Date());
					}
				}
			}
		});

		JButton printButton = new JButton("Print...");
		printButton.setEnabled(true);
		printButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				PrinterJob printerJob = PrinterJob.getPrinterJob();
				PageFormat pageFormat = printerJob.defaultPage();
				pageFormat.setOrientation(PageFormat.LANDSCAPE);
				printerJob.setPrintable(simplePlot, pageFormat);
				try {
					if (printerJob.printDialog()) {
						printerJob.print();
					}
				} catch (PrinterException pe) {
					logger.error("Caught PrinterException: " + pe.getMessage());
				}
			}
		});

		final JCheckBox twoDDataCheckBox = new JCheckBox("2D data file");
		twoDDataCheckBox.setSelected(false);
		twoDDataCheckBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				is2DPlot = twoDDataCheckBox.isSelected();
			}
		});

		panel.add(openButton);
		panel.add(printButton);
		panel.add(twoDDataCheckBox);

		Border b = BorderFactory.createEtchedBorder();
		panel.setBorder(BorderFactory.createTitledBorder(b, "Load", TitledBorder.TOP, TitledBorder.CENTER));

		return panel;
	}

	private void createWhatToPlotPanel() {
		// The whatToPlotPanel panel contains a set of check boxes
		whatToPlotPanel = new JPanel(new GridLayout(0, 1));
		Border b = BorderFactory.createEtchedBorder();
		whatToPlotPanel.setBorder(BorderFactory.createTitledBorder(b, "Plot Shows", TitledBorder.TOP,
				TitledBorder.CENTER));
		whatToPlotPanel.setVisible(false);
	}

	private void createNormalizePanel() {
		// The normalizePanel panel contains a set of radio buttons
		normalizePanel = new JPanel(new GridLayout(0, 1));
		Border b = BorderFactory.createEtchedBorder();
		normalizePanel.setBorder(BorderFactory.createTitledBorder(b, "Normalize To", TitledBorder.TOP,
				TitledBorder.CENTER));
		normalizePanel.setVisible(false);
	}

	private void loadAndPlotData() {
		BufferedReader bufferedReader;
		String line;
		StringTokenizer strTok;
		String lastCommentLine = null;
		boolean firstPoint = true;
		double[] values = null;
		double xValue = 0.0;
		double yValue = 0.0;
		int skip = is2DPlot ? 2 : 1;

		try {
			bufferedReader = new BufferedReader(new FileReader(fileName));
			while ((line = bufferedReader.readLine()) != null) {
				try {
					strTok = new StringTokenizer(line, " ,\t");
					if (firstPoint) {
						// Only consider the y values (x is the first or second)
						if (strTok.countTokens() > 1) {
							howManyYValues = strTok.countTokens() - skip;
							values = new double[howManyYValues];
							plotSelected = new boolean[howManyYValues];
						}
					}
					if (values != null) {
						int j = 0;
						while (strTok.hasMoreTokens()) {
							if (j == skip - 1) {
								xValue = Double.valueOf(strTok.nextToken()).doubleValue();
							} else if (j >= skip) {
								yValue = Double.valueOf(strTok.nextToken()).doubleValue();
								values[j - skip] = yValue;
							} else {
								// kludge cos of these 5u ers who want 2d plot
								// on a 1d
								// grid
								// we need to ignore the outer x value and we
								// need it to
								// have a
								// number format exception for comment records.
								xValue = Double.valueOf(strTok.nextToken()).doubleValue();
							}
							j++;
						}
					}
					if (firstPoint) {
						String[] channelNames = null;
						int numberOfChannels;
						if (lastCommentLine != null) {
							StringTokenizer strtok = new StringTokenizer(lastCommentLine, "\t\n");
							numberOfChannels = strtok.countTokens() - 1;
							// If it is a 2D plot then reduce
							// numberOfChannels by 1 and
							// skip the first token.
							if (is2DPlot) {
								numberOfChannels -= 1;
								strtok.nextToken();
							}
							simplePlot.setXAxisLabel(strtok.nextToken() + " position");
							channelNames = new String[numberOfChannels];
							for (int i = 0; i < numberOfChannels; i++) {
								channelNames[i] = strtok.nextToken();
							}
						}
						setPlotHeader(fileName, channelNames);
					}

					addData(xValue, values, 0);
					firstPoint = false;
				} catch (NumberFormatException nfe) {
					logger.error("Comment:" + line);
					lastCommentLine = line;
				}
			}
			bufferedReader.close();
		} catch (IOException ioe) {
			logger.error("IOException in loadAndPlotData: " + ioe.getMessage());
		}

	}

	/**
	 * Adds new data to the lines.
	 * 
	 * @param xVal
	 *            the x xvalue
	 * @param yVals
	 *            array of y values
	 * @param start
	 *            value for data addition
	 */
	private void addData(double xVal, double[] yVals, int start) {
		for (int j = 0; j < yVals.length; j++) {
			int i = j + start;
			double yVal = 0;
			// Normalise if required
			if (whichToNormalizeTo != NO_NORMALISATION) {
				if (yVals[whichToNormalizeTo] != 0) {
					yVal = yVals[j] / yVals[whichToNormalizeTo];
				} else {
					yVal = 0;
				}
			} else {
				yVal = yVals[j];
			}
			simplePlot.addPointToLine(i, xVal, yVal);
		}
	}

	private void setPlotHeader(String title, String[] channelNames) {

		if (whatToPlotButtons != null) {
			for (int i = 0; i < whatToPlotButtons.length; i++) {
				if (i < howManyYValues) {
					plotSelected[i] = whatToPlotButtons[i].isSelected();
				}
				simplePlot.deleteLine(i);
			}
		}

		whatToPlotPanel.removeAll();
		normalizePanel.removeAll();
		updateUI();

		createPlotSelectButtons(channelNames);
		createNormalizeButtons(channelNames);
		whatToPlotPanel.setVisible(true);
		normalizePanel.setVisible(true);

		// Set up plot
		for (int i = 0; i < howManyYValues; i++) {
			simplePlot.initializeLine(i);
			simplePlot.linesOnly(i);
			simplePlot.setLineName(i, channelNames[i]);
		}
		simplePlot.setTitle(title);

		setWhatToPlot();
		setWhichToNormalizeTo();
	}

	private void createPlotSelectButtons(String[] names) {
		boolean atLeastOneSelected = false;
		if (howManyYValues > 0) {
			whatToPlotButtons = new JCheckBox[howManyYValues];
			for (int i = 0; i < howManyYValues; i++) {
				whatToPlotButtons[i] = new JCheckBox(names[i]);
				whatToPlotButtons[i].setSelected(plotSelected[i]);
				atLeastOneSelected = atLeastOneSelected || plotSelected[i];
				whatToPlotPanel.add(whatToPlotButtons[i]);
				whatToPlotButtons[i].addActionListener(whatToPlotSetter);
			}
			if (!atLeastOneSelected) {
				whatToPlotButtons[DEFAULT_PLOT].setSelected(true);
			}
		}
	}

	private void setWhatToPlot() {
		// Uses BFI method, lines are always present, those not wanted are
		// made invisible.
		for (int i = 0; i < howManyYValues; i++) {
			simplePlot.setLineVisibility(i, whatToPlotButtons[i].isSelected());
			plotSelected[i] = whatToPlotButtons[i].isSelected();
		}
		updateUI();
	}

	private void createNormalizeButtons(String[] names) {
		ButtonGroup normaliseButtonGroup = new ButtonGroup();
		radioButtons = new JRadioButton[howManyYValues + 1];

		for (int i = 0; i < howManyYValues + 1; i++) {
			if (i == 0) {
				radioButtons[i] = new JRadioButton("None");
			} else {
				radioButtons[i] = new JRadioButton(names[i - 1]);
			}
			normalizePanel.add(radioButtons[i]);
			radioButtons[i].addActionListener(normalizer);
			normaliseButtonGroup.add(radioButtons[i]);
		}
		radioButtons[whichToNormalizeTo + 1].setSelected(true);
	}

	private void setWhichToNormalizeTo() {
		for (int i = 0; i < howManyYValues + 1; i++) {
			if (radioButtons[i].isSelected()) {
				whichToNormalizeTo = i - 1;
				break;
			}
		}
		logger.debug("whichToNormalizeTo is: " + whichToNormalizeTo);
	}
}