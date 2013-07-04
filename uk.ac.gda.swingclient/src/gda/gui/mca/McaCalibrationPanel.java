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
import gda.device.detector.analyser.EpicsMCARegionOfInterest;
import gda.observable.IObservable;
import gda.observable.IObserver;
import gda.observable.ObservableComponent;
import gda.util.fit.FitPoly;
import gda.util.fit.GaussianFunction;
import gda.util.fit.GaussianMultiModel;
import gda.util.fit.LMFittingParameters;
import gda.util.fit.MCACurveFit;
import gda.util.fit.MCAParameters;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * McaCalibrationPanel Class
 */
public class McaCalibrationPanel extends JDialog implements PropertyChangeListener, ActionListener, IObservable {
	
	private static final Logger logger = LoggerFactory.getLogger(McaCalibrationPanel.class);

	private ObservableComponent observableComponent = new ObservableComponent();

	private JComboBox typeCombo;

	private JButton computeButton;

	private JButton plotCalibrationButton;

	private JButton plotFWHMButton;

	private JTextField unitsField;

	private JTextField offsetField;

	private JTextField slopeField;

	private JTextField quadraticField;

	private JComboBox[] useCombo;

	private JTextField[] centroidField;

	private JTextField[] fwhmField;

	private JTextField[] energyField;

	private JTextField[] flourField;

	private JPanel calibrationControlPanel;

	private JPanel calibCoefficientsPanel;

	private JPanel roiPanel;

	private JPanel calibPanel;

	private String btnString1 = "Ok";

	private String btnString2 = "Cancel";

	private JOptionPane optionPane;

	private JButton[] fitButton;

	static McaCalibrationPanel dia = null;

	// should be changed to ageneric ROI rather than epics specific
	private EpicsMCARegionOfInterest rois[];

	private EpicsMCARegionOfInterest selectedrois[];

	private double[] data;

	private String mcaName;

	/**
	 * @param roi
	 * @param data
	 * @param name
	 * @throws Exception
	 */
	public McaCalibrationPanel(EpicsMCARegionOfInterest roi[], double[] data, String name) throws Exception {
		this.rois = roi;
		this.data = data;
		this.mcaName = name;
		calibPanel = new JPanel();
		calibPanel.setLayout(new GridBagLayout());
		GridBagConstraints gbc1 = new GridBagConstraints();
		gbc1.gridx = GridBagConstraints.REMAINDER;
		gbc1.gridy = GridBagConstraints.RELATIVE;
		gbc1.fill = GridBagConstraints.HORIZONTAL;
		// EpicsMCARegionOfInterest eroi[] = new EpicsMCARegionOfInterest[4];
		makeRoiPanel();

		makeCalibrationControl();
		makeCalibrationCoefficients();
		calibPanel.add(roiPanel, gbc1);
		calibPanel.add(calibrationControlPanel, gbc1);
		calibPanel.add(calibCoefficientsPanel, gbc1);
		/*
		 * okButton = new JButton("OK"); cancelButton = new JButton("Cancel"); JPanel buttonPanel = new JPanel();
		 * buttonPanel.add(okButton); buttonPanel.add(cancelButton);
		 * buttonPanel.setBorder(BorderFactory.createLineBorder(Color.black)); calibPanel.add(buttonPanel);
		 */
		Object[] options = { btnString1, btnString2 };
		Object[] array = { calibPanel };

		// Create the JOptionPane.
		optionPane = new JOptionPane(array, JOptionPane.PLAIN_MESSAGE, JOptionPane.YES_NO_OPTION, null, options,
				options[0]);
		setContentPane(optionPane);
		pack();
		setTitle("Energy Calibration");
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent we) {
				/*
				 * Instead of directly closing the window, we're going to change the JOptionPane's value property.
				 */
				optionPane.setValue(new Integer(JOptionPane.CLOSED_OPTION));
			}
		});

		/*
		 * //Ensure the text field always gets the first focus. addComponentListener(new ComponentAdapter() { public
		 * void componentShown(ComponentEvent ce) { textField.requestFocusInWindow(); } });
		 */

		// Register an event handler that puts the text into the option pane.
		// textField.addActionListener(this);
		// Register an event handler that reacts to option pane state changes.
		optionPane.addPropertyChangeListener(this);
	}

	/**
	 * This method reacts to state changes in the option pane.
	 * 
	 * @param e
	 *            the property change event
	 */
	@Override
	public void propertyChange(PropertyChangeEvent e) {
		String prop = e.getPropertyName();

		if (isVisible() && (e.getSource() == optionPane)
				&& (JOptionPane.VALUE_PROPERTY.equals(prop) || JOptionPane.INPUT_VALUE_PROPERTY.equals(prop))) {
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

			if (btnString1.equals(value)) {
				// analyser.setcalibrationValues();
				writeToFile();
				notifyIObservers(this, "Calibration Available");
				// Do something relevant to Ok
				// we're done; clear and dismiss the dialog
				clearAndHide();

			} else { // user closed dialog or clicked cancel

				clearAndHide();
			}
		}
	}

	private void writeToFile() {
		String fileName = LocalProperties.get("mca.calibration.dir", ".");
		File file = new File(fileName + File.separator + this.mcaName);
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			writer.write("offset=" + offsetField.getText() + "\n");
			writer.write("slope=" + slopeField.getText() + "\n");
			writer.write("quadratic=" + quadraticField.getText() + "\n");
			writer.close();
		} catch (IOException e) {
			logger.error("Exception: " + e.getMessage());
		}
	}

	/**
	 * @param mcaName
	 * @param offset
	 * @param slope
	 * @param quadratic
	 */
	public static void writeToFile(String mcaName, double offset, double slope, double quadratic) {
		String fileName = LocalProperties.get("mca.calibration.dir", ".");
		File file = new File(fileName + File.separator + mcaName);
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			writer.write("offset=" + offset + "\n");
			writer.write("slope=" + slope + "\n");
			writer.write("quadratic=" + quadratic + "\n");
			writer.close();
		} catch (IOException e) {
			logger.error("Exception: " + e.getMessage());
		}
	}

	private void makeCalibrationControl() {
		JLabel typeLabel = new JLabel("Calibration Type:");
		typeCombo = new JComboBox(new String[] { "Linear", "Quadratic" });
		computeButton = new JButton("Compute Calibration");
		computeButton.setActionCommand("calibrate");
		computeButton.addActionListener(this);
		plotCalibrationButton = new JButton("Plot Calibration");
		plotCalibrationButton.setEnabled(false);
		plotFWHMButton = new JButton("Plot FWHM");
		plotFWHMButton.setEnabled(false);
		calibrationControlPanel = new JPanel();
		calibrationControlPanel.add(typeLabel);
		calibrationControlPanel.add(typeCombo);
		calibrationControlPanel.add(computeButton);

		calibrationControlPanel.add(plotCalibrationButton);
		calibrationControlPanel.add(plotFWHMButton);
		calibrationControlPanel.setBorder(BorderFactory.createLineBorder(Color.black));

	}

	private void makeCalibrationCoefficients() {
		JLabel coeffLabel = new JLabel("Calibration Coefficients: ");
		JLabel unitsLabel = new JLabel("Units");
		JLabel offsetLabel = new JLabel("Offset");
		JLabel slopeLabel = new JLabel("Slope");
		JLabel quadLabel = new JLabel("Quadratic");
		unitsField = new JTextField(10);
		offsetField = new JTextField(10);
		offsetField.setEditable(false);
		slopeField = new JTextField(10);
		slopeField.setEditable(false);
		quadraticField = new JTextField(10);
		quadraticField.setEditable(false);
		calibCoefficientsPanel = new JPanel();
		calibCoefficientsPanel.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 0;
		calibCoefficientsPanel.add(unitsLabel, gbc);
		gbc.gridx++;
		calibCoefficientsPanel.add(offsetLabel, gbc);
		gbc.gridx++;
		calibCoefficientsPanel.add(slopeLabel, gbc);
		gbc.gridx++;
		calibCoefficientsPanel.add(quadLabel, gbc);
		gbc.gridx = 0;
		gbc.gridy = 1;
		calibCoefficientsPanel.add(coeffLabel, gbc);
		gbc.gridx++;
		calibCoefficientsPanel.add(unitsField, gbc);
		gbc.gridx++;
		calibCoefficientsPanel.add(offsetField, gbc);
		gbc.gridx++;
		calibCoefficientsPanel.add(slopeField, gbc);
		gbc.gridx++;
		calibCoefficientsPanel.add(quadraticField, gbc);
		calibCoefficientsPanel.setBorder(BorderFactory.createLineBorder(Color.black));

	}

	private void makeRoiPanel() throws Exception {
		JLabel roiLabel = new JLabel("ROI");
		JLabel useLabel = new JLabel("Use?");
		JLabel centroidLabel = new JLabel("Centroid");
		JLabel fwhmLabel = new JLabel("FWHM");
		JLabel energyLabel = new JLabel("Energy");
		JLabel flourLabel = new JLabel("Region Name");
		// JLabel diffLabel = new JLabel("Energy diff.");
		JLabel roiIndex[] = new JLabel[rois.length];
		roiPanel = new JPanel();
		roiPanel.setLayout(new GridBagLayout());
		GridBagConstraints gc = new GridBagConstraints();
		gc.gridx = GridBagConstraints.RELATIVE;
		// gc.gridx = 0;
		gc.gridy = 0;
		roiPanel.add(roiLabel, gc);
		gc.gridx = GridBagConstraints.RELATIVE;
		roiPanel.add(useLabel, gc);
		roiPanel.add(centroidLabel, gc);
		roiPanel.add(fwhmLabel, gc);
		roiPanel.add(energyLabel, gc);
		roiPanel.add(flourLabel, gc);
		// gc.gridx = GridBagConstraints.REMAINDER;
		// roiPanel.add(diffLabel, gc);
		// EpicsMCARegionOfInterest[] srois = selectRois();
		selectRois();
		if (selectedrois == null)
			throw new Exception("NO ROIS are selected");
		useCombo = new JComboBox[selectedrois.length];
		centroidField = new JTextField[selectedrois.length];
		fwhmField = new JTextField[selectedrois.length];
		energyField = new JTextField[selectedrois.length];
		flourField = new JTextField[selectedrois.length];
		// diffField = new JTextField[selectedrois.length];
		fitButton = new JButton[selectedrois.length];
		for (int i = 0; i < selectedrois.length; i++) {
			gc.gridx = 0;
			gc.gridy++;
			roiIndex[i] = new JLabel(String.valueOf(i));
			useCombo[i] = new JComboBox(new String[] { "yes", "No" });
			centroidField[i] = new JTextField(10);
			centroidField[i].setText(String.valueOf(getScaled(setCentre(i), 6)));
			fwhmField[i] = new JTextField(10);
			fwhmField[i].setEditable(false);
			energyField[i] = new JTextField("0.000");
			flourField[i] = new JTextField(selectedrois[i].getRegionName());
			// diffField[i] = new JTextField(10);
			// diffField[i].setEditable(false);
			fitButton[i] = new JButton("fit");
			fitButton[i].setActionCommand("fit" + i);
			fitButton[i].addActionListener(this);
			roiPanel.add(roiIndex[i], gc);
			gc.gridx = GridBagConstraints.RELATIVE;
			roiPanel.add(useCombo[i], gc);
			roiPanel.add(centroidField[i], gc);
			roiPanel.add(fwhmField[i], gc);
			roiPanel.add(energyField[i], gc);
			roiPanel.add(flourField[i], gc);
			// gc.gridx = GridBagConstraints.REMAINDER;
			// roiPanel.add(diffField[i], gc);
			roiPanel.add(fitButton[i], gc);

		}
		roiPanel.setBorder(BorderFactory.createLineBorder(Color.black));
	}

	private double setCentre(int rowIndex) {
		double centre = (rois[rowIndex].getRegionHigh() + rois[rowIndex].getRegionLow()) / 2;
		return centre;
	}

	// /This should probably in the Epics MCA getROI method
	private EpicsMCARegionOfInterest[] selectRois() {
		Vector<EpicsMCARegionOfInterest> vroi = new Vector<EpicsMCARegionOfInterest>();
		for (int i = 0; i < rois.length; i++) {
			double low = rois[i].getRegionLow();
			if (low >= 0 && rois[i].getRegionHigh() >= low)
				vroi.add(rois[i]);
		}
		if (vroi.size() != 0) {
			selectedrois = new EpicsMCARegionOfInterest[vroi.size()];
			for (int j = 0; j < selectedrois.length; j++) {
				selectedrois[j] = vroi.get(j);
			}
			return selectedrois;
		}
		selectedrois = null;
		return null;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		double d = 2.3456789E-7;// 1.1435150954101E7;
		logger.debug("The scaled value is " + McaCalibrationPanel.getScaled(d, 10));

		JFrame f = new JFrame("Calibration");
		JButton click = new JButton("click");
		int dataLength = 101;
		// dummy data values to use when mca hardware is not available
		double[] x = new double[dataLength];
		double[] y = new double[dataLength];
		x[0] = 0.0;
		y[0] = 1.31067031E8;
		x[1] = 1.0;
		y[1] = 9.348836E7;
		x[2] = 2.0;
		y[2] = 5873.0;
		x[3] = 3.0;
		y[3] = 191.0;
		x[4] = 4.0;
		y[4] = 166.0;
		x[5] = 5.0;
		y[5] = 173.0;
		x[6] = 6.0;
		y[6] = 148.0;
		x[7] = 7.0;
		y[7] = 162.0;
		x[8] = 8.0;
		y[8] = 125.0;
		x[9] = 9.0;
		y[9] = 122.0;
		x[10] = 10.0;
		y[10] = 114.0;
		x[11] = 11.0;
		y[11] = 114.0;
		x[12] = 12.0;
		y[12] = 118.0;
		x[13] = 13.0;
		y[13] = 112.0;
		x[14] = 14.0;
		y[14] = 95.0;
		x[15] = 15.0;
		y[15] = 77.0;
		x[16] = 16.0;
		y[16] = 97.0;
		x[17] = 17.0;
		y[17] = 96.0;
		x[18] = 18.0;
		y[18] = 93.0;
		x[19] = 19.0;
		y[19] = 68.0;
		x[20] = 20.0;
		y[20] = 66.0;
		x[21] = 21.0;
		y[21] = 56.0;
		x[22] = 22.0;
		y[22] = 61.0;
		x[23] = 23.0;
		y[23] = 39.0;
		x[24] = 24.0;
		y[24] = 39.0;
		x[25] = 25.0;
		y[25] = 27.0;
		x[26] = 26.0;
		y[26] = 18.0;
		x[27] = 27.0;
		y[27] = 12.0;
		x[28] = 28.0;
		y[28] = 9.0;
		x[29] = 29.0;
		y[29] = 0.0;
		x[30] = 30.0;
		y[30] = 1.0;
		x[31] = 31.0;
		y[31] = 0.0;
		x[32] = 32.0;
		y[32] = 0.0;
		x[33] = 33.0;
		y[33] = 0.0;
		x[34] = 34.0;
		y[34] = 0.0;
		x[35] = 35.0;
		y[35] = 0.0;
		x[36] = 36.0;
		y[36] = 0.0;
		x[37] = 37.0;
		y[37] = 0.0;
		x[38] = 38.0;
		y[38] = 0.0;
		x[39] = 39.0;
		y[39] = 0.0;
		x[40] = 40.0;
		y[40] = 0.0;
		x[41] = 41.0;
		y[41] = 0.0;
		x[42] = 42.0;
		y[42] = 0.0;
		x[43] = 43.0;
		y[43] = 0.0;
		x[44] = 44.0;
		y[44] = 0.0;
		x[45] = 45.0;
		y[45] = 0.0;
		x[46] = 46.0;
		y[46] = 0.0;
		x[47] = 47.0;
		y[47] = 0.0;
		x[48] = 48.0;
		y[48] = 0.0;
		x[49] = 49.0;
		y[49] = 0.0;
		x[50] = 50.0;
		y[50] = 0.0;
		x[51] = 51.0;
		y[51] = 0.0;
		x[52] = 52.0;
		y[52] = 0.0;
		x[53] = 53.0;
		y[53] = 0.0;
		x[54] = 54.0;
		y[54] = 0.0;
		x[55] = 55.0;
		y[55] = 0.0;
		x[56] = 56.0;
		y[56] = 0.0;
		x[57] = 57.0;
		y[57] = 0.0;
		x[58] = 58.0;
		y[58] = 0.0;
		x[59] = 59.0;
		y[59] = 0.0;
		x[60] = 60.0;
		y[60] = 0.0;
		x[61] = 61.0;
		y[61] = 0.0;
		x[62] = 62.0;
		y[62] = 0.0;
		x[63] = 63.0;
		y[63] = 0.0;
		x[64] = 64.0;
		y[64] = 0.0;
		x[65] = 65.0;
		y[65] = 0.0;
		x[66] = 66.0;
		y[66] = 0.0;
		x[67] = 67.0;
		y[67] = 0.0;
		x[68] = 68.0;
		y[68] = 0.0;
		x[69] = 69.0;
		y[69] = 0.0;
		x[70] = 70.0;
		y[70] = 0.0;
		x[71] = 71.0;
		y[71] = 0.0;
		x[72] = 72.0;
		y[72] = 0.0;
		x[73] = 73.0;
		y[73] = 0.0;
		x[74] = 74.0;
		y[74] = 0.0;
		x[75] = 75.0;
		y[75] = 0.0;
		x[76] = 76.0;
		y[76] = 0.0;
		x[77] = 77.0;
		y[77] = 0.0;
		x[78] = 78.0;
		y[78] = 0.0;
		x[79] = 79.0;
		y[79] = 0.0;
		x[80] = 80.0;
		y[80] = 0.0;
		x[81] = 81.0;
		y[81] = 0.0;
		x[82] = 82.0;
		y[82] = 0.0;
		x[83] = 83.0;
		y[83] = 0.0;
		x[84] = 84.0;
		y[84] = 0.0;
		x[85] = 85.0;
		y[85] = 0.0;
		x[86] = 86.0;
		y[86] = 0.0;
		x[87] = 87.0;
		y[87] = 0.0;
		x[88] = 88.0;
		y[88] = 0.0;
		x[89] = 89.0;
		y[89] = 0.0;
		x[90] = 90.0;
		y[90] = 0.0;
		x[91] = 91.0;
		y[91] = 0.0;
		x[92] = 92.0;
		y[92] = 0.0;
		x[93] = 93.0;
		y[93] = 1.0;
		x[94] = 94.0;
		y[94] = 0.0;
		x[95] = 95.0;
		y[95] = 0.0;
		x[96] = 96.0;
		y[96] = 1.0;
		x[97] = 97.0;
		y[97] = 10.0;
		x[98] = 98.0;
		y[98] = 352.0;
		x[99] = 99.0;
		y[99] = 1294000;
		x[100] = 100.0;
		y[100] = 638310.0;
		EpicsMCARegionOfInterest[] roi = new EpicsMCARegionOfInterest[3];
		roi[0] = new EpicsMCARegionOfInterest(0, 0, 20, 0, 0, "region0");
		roi[1] = new EpicsMCARegionOfInterest(1, 30, 50, 0, 0, "region1");
		roi[2] = new EpicsMCARegionOfInterest(2, 70, 100, 0, 0, "region2");
		try {
			dia = new McaCalibrationPanel(roi, y, "testmca");
		} catch (Exception e) {
			logger.error("Exception: " + e.getMessage());
		}
		f.add(click);
		click.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				dia.setVisible(true);

			}

		});

		// f.add(new McaCalibrationPanel());
		f.setSize(400, 600);
		f.setVisible(true);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	/** This method clears the dialog and hides it. */
	public void clearAndHide() {

		setVisible(false);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		String actionCommand = arg0.getActionCommand();
		if (actionCommand.startsWith("fit")) {
			int rowIndex = Integer.parseInt(actionCommand.substring(actionCommand.length() - 1));
			GaussianMultiModel myModel = new GaussianMultiModel();
			int dataLength = (int) (rois[rowIndex].getRegionHigh() - rois[rowIndex].getRegionLow()) + 1;
			double centre = Double.NaN;
			String centroidFieldValue = centroidField[rowIndex].getText();
			if (centroidFieldValue != null) {
				try {
					centre = Double.parseDouble(centroidFieldValue);
				} catch (NumberFormatException e) {
					logger.debug("Centroid should be a Number");
					centre = (rois[rowIndex].getRegionHigh() + rois[rowIndex].getRegionLow()) / 2;
				}
			} else {
				centre = (rois[rowIndex].getRegionHigh() + rois[rowIndex].getRegionLow()) / 2;
			}
			double width = rois[rowIndex].getRegionHigh() - rois[rowIndex].getRegionLow();
			myModel.addFunction(new GaussianFunction(data[(int) centre], centre, width));
			// myModel.addFunction(new GaussianFunction(1.8, 2.1, 4.5));
			LMFittingParameters fitParams = new LMFittingParameters();
			MCAParameters mcaParams = new MCAParameters();
			mcaParams.setEnergySlope(1.0);
			mcaParams.setEnergyOffset(0.0);
			// Create some test data to fit against
			logger.debug("Initial values to fit " + data[(int) centre] + " centre " + centre + " width " + width);
			double[] x = new double[dataLength];
			double[] y = new double[dataLength];
			for (int i = 0; i < dataLength; i++) {
				x[i] = i;
				y[i] = data[i];
				logger.debug("x = " + x[i] + " y= " + y[i]);
			}

			double[][] results = MCACurveFit.LMfit(myModel, fitParams, mcaParams, x, y);
			centroidField[rowIndex].setText(String.valueOf(getScaled(results[0][1], 6)));
			fwhmField[rowIndex].setText(String.valueOf(getScaled(results[0][2], 6)));
			// //
			// diffField[i].setText(String.valueOf(Double.parseDouble(energyFieeld[rowIndex])))
		} else if (actionCommand.startsWith("calibrate")) {
			String calibrationType = (String) typeCombo.getSelectedItem();
			if ((calibrationType).equalsIgnoreCase("linear")) {
				if (selectedrois.length < 2) {
					JOptionPane.showMessageDialog(this,
							"Linear energy calibration needs atleast two regions of interest");
					return;

				}
				Vector<Double> channelVector = new Vector<Double>();
				Vector<Double> energyVector = new Vector<Double>();
				for (int i = 0; i < selectedrois.length; i++) {
					if (((String) useCombo[i].getSelectedItem()).equalsIgnoreCase("yes")) {
						channelVector.add(new Double(centroidField[i].getText()));
						energyVector.add(new Double(energyField[i].getText()));
					}
				}
				if (channelVector.size() < 2 || energyVector.size() < 2) {
					channelVector.add(0.0);
					energyVector.add(0.0);
					// JOptionPane.showMessageDialog(this,
					// "Linear energy calibartion needs atleast two regions
					// of
					// interest");
					// return;

				}
				double fitParams[] = new double[4];
				double channel[] = new double[channelVector.size()];
				double energy[] = new double[energyVector.size()];
				for (int j = 0; j < channel.length; j++) {
					channel[j] = channelVector.get(j).doubleValue();
					energy[j] = energyVector.get(j).doubleValue();
				}
				FitPoly.fit(fitParams, channel, energy, null, channel.length);
				offsetField.setText(String.valueOf(getScaled(fitParams[0], 6)));
				slopeField.setText(String.valueOf(getScaled(fitParams[1], 6)));
				quadraticField.setText("0.0");
				// double[] saveParams = {fitParams[0], fitParams[1], 0.0};
				// saveCalibration(saveParams);

			} else if ((calibrationType).equalsIgnoreCase("quadratic")) {
				if (selectedrois.length < 3) {
					JOptionPane.showMessageDialog(this,
							"Quadratic energy calibartion needs atleast two regions of interest");
					return;

				}
				Vector<Double> channelVector = new Vector<Double>();
				Vector<Double> energyVector = new Vector<Double>();
				for (int i = 0; i < selectedrois.length; i++) {
					if (((String) useCombo[i].getSelectedItem()).equalsIgnoreCase("yes")) {
						channelVector.add(new Double(centroidField[i].getText()));
						energyVector.add(new Double(energyField[i].getText()));
					}
				}
				if (channelVector.size() < 3 || energyVector.size() < 3) {
					JOptionPane.showMessageDialog(this,
							"Quadratic energy calibration needs atleast two regions of interest");
					return;

				}
				double fitParams[] = new double[6];
				double channel[] = new double[channelVector.size()];
				double energy[] = new double[energyVector.size()];
				for (int j = 0; j < channel.length; j++) {
					channel[j] = channelVector.get(j).doubleValue();
					energy[j] = energyVector.get(j).doubleValue();
				}
				FitPoly.fit(fitParams, channel, energy, null, channel.length);
				// fitParams[0] + " " + fitParams[1]);
				offsetField.setText(String.valueOf(getScaled(fitParams[0], 6)));
				slopeField.setText(String.valueOf(getScaled(fitParams[1], 6)));

				quadraticField.setText(String.valueOf(getScaled(fitParams[2], 6)));
				// double[] saveParams1 = {fitParams[0], fitParams[1],
				// fitParams[2]};
				// saveCalibration(saveParams1);
			}

		}

	}

	/**
	 * @see gda.observable.IObservable#addIObserver(gda.observable.IObserver)
	 */
	@Override
	public void addIObserver(IObserver anIObserver) {
		observableComponent.addIObserver(anIObserver);
	}

	/**
	 * @see gda.observable.IObservable#deleteIObserver(gda.observable.IObserver)
	 */
	@Override
	public void deleteIObserver(IObserver anIObserver) {
		observableComponent.deleteIObserver(anIObserver);
	}

	/**
	 * @see gda.observable.IObservable#deleteIObservers()
	 */
	@Override
	public void deleteIObservers() {
		observableComponent.deleteIObservers();
	}

	/**
	 * @param theObserved
	 * @param theArgument
	 */
	public void notifyIObservers(Object theObserved, Object theArgument) {
		observableComponent.notifyIObservers(theObserved, theArgument);
	}

	/**
	 * @param value
	 * @param scale
	 * @return scaled
	 */
	static public double getScaled(double value, int scale) {
		double result = value; // default: unscaled

		// use BigDecimal String constructor as this is the only exact way for
		// double values
		result = new BigDecimal("" + value).setScale(scale, BigDecimal.ROUND_HALF_UP).doubleValue();

		return result;
	}// getScaled()1.1435150954101E7

}
