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

package gda.gui.exafs;

import gda.jython.JythonServerFacade;
import gda.plots.SimpleCoordinateFormatter;
import gda.plots.SimpleDataCoordinate;
import gda.plots.SimplePlot;
import gda.util.SRSToChooch;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Map;
import java.util.StringTokenizer;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ChoochRunner class
 */
public class ChoochRunner extends JPanel implements Runnable {
	private static final Logger logger = LoggerFactory.getLogger(ChoochRunner.class);

	private class ChoochCoordinateFormatter extends SimpleCoordinateFormatter {

		@Override
		public String formatCoordinates(SimpleDataCoordinate sdc) {
			return "mouse position " + getXNumberFormat().format(sdc.getX()) + " (eV)";
		}
	}

	private ProcessBuilder pb;

	private Process p;

	private double inflectionPoint;

	private double peak;

	private double remotePoint;

	private Thread thread;

	private JButton runButton;

	private JTextField inflectionField;

	private JTextField peakField;

	private JTextField remoteField;

	private JTextField fileNameField;

	private JTextField elementField;

	private JTextField edgeTypeField;

	private JButton inflectionButton;

	private JButton peakButton;

	private JButton remoteButton;

	private SimplePlot choochPlot;

	private String moveMonoDofName = null;

	private String fileToWorkOn = null;

	private String elementName = null;

	private String edgeType = null;

	private String choochFailureMessage = "Benny or Chooch failed while working on data from ";

	/**
	 * @param moveMonoDofName
	 */
	public ChoochRunner(String moveMonoDofName) {
		super(new BorderLayout());
		this.moveMonoDofName = moveMonoDofName;

		choochPlot = new SimplePlot();
		choochPlot.setTrackPointer(true);
		choochPlot.setCoordinateFormatter(new ChoochCoordinateFormatter());
		choochPlot.setTitle("f' and f''");
		add(choochPlot, BorderLayout.CENTER);
		runButton = new JButton("Run");
		runButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				start();
			}
		});

		runButton.setToolTipText("Click to run CHOOCH on the last XAFS data");

		inflectionButton = new JButton("Go There");
		inflectionButton.setToolTipText("Click to move mono to inflection point energy");
		inflectionButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				((JButton) e.getSource()).setEnabled(false);
				moveMonoTo(inflectionField.getText());
			}
		});
		inflectionButton.setEnabled(false);

		peakButton = new JButton("Go There");
		peakButton.setToolTipText("Click to move mono to peak energy");
		peakButton.setEnabled(false);
		peakButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				((JButton) e.getSource()).setEnabled(false);
				moveMonoTo(peakField.getText());

			}
		});

		remoteButton = new JButton("Go There");
		remoteButton.setToolTipText("Click to move mono to remote energy");
		remoteButton.setEnabled(false);
		remoteButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				((JButton) e.getSource()).setEnabled(false);
				moveMonoTo(remoteField.getText());
			}
		});

		JPanel eastPanel = new JPanel(new BorderLayout());
		JPanel controlPanel = new JPanel(new BorderLayout());
		JPanel leftColumn = new JPanel(new GridLayout(6, 0));
		leftColumn.add(new JLabel(""));
		leftColumn.add(runButton);
		leftColumn.add(new JLabel(""));
		leftColumn.add(new JLabel("Inflection "));
		leftColumn.add(new JLabel("Peak "));
		leftColumn.add(new JLabel("Remote "));
		controlPanel.add(leftColumn, BorderLayout.WEST);

		JPanel centerColumn = new JPanel(new GridLayout(6, 0));
		centerColumn.add(new JLabel(""));
		centerColumn.add(new JLabel(""));
		centerColumn.add(new JLabel(""));
		inflectionField = new JTextField("0.00000 eV");
		centerColumn.add(inflectionField);
		peakField = new JTextField("0.00000 eV");
		centerColumn.add(peakField);
		remoteField = new JTextField("0.00000 eV");
		centerColumn.add(remoteField);

		controlPanel.add(centerColumn, BorderLayout.CENTER);

		JPanel rightColumn = new JPanel(new GridLayout(6, 0));
		rightColumn.add(new JLabel(""));
		rightColumn.add(new JLabel(""));
		rightColumn.add(new JLabel(""));
		rightColumn.add(inflectionButton);
		rightColumn.add(peakButton);
		rightColumn.add(remoteButton);
		controlPanel.add(rightColumn, BorderLayout.EAST);

		JPanel yap = new JPanel(new BorderLayout());
		JPanel yapRight = new JPanel(new GridLayout(4, 0));
		JPanel yapLeft = new JPanel(new GridLayout(4, 0));

		fileNameField = new JTextField();
		fileNameField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				fileToWorkOn = fileNameField.getText();
			}
		});

		// Padding
		yapLeft.add(new JLabel(""));
		yapRight.add(new JLabel(""));

		yapLeft.add(new JLabel("Exafs file name "));
		yapRight.add(fileNameField);

		elementField = new JTextField();
		elementField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				elementName = elementField.getText();
			}
		});

		yapLeft.add(new JLabel("Element "));
		yapRight.add(elementField);

		edgeTypeField = new JTextField();
		edgeTypeField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				edgeType = edgeTypeField.getText();
			}
		});
		yapLeft.add(new JLabel("Edge type "));
		yapRight.add(edgeTypeField);

		yap.add(yapLeft, BorderLayout.WEST);
		yap.add(yapRight, BorderLayout.CENTER);
		controlPanel.add(yap, BorderLayout.SOUTH);

		eastPanel.add(controlPanel, BorderLayout.NORTH);
		add(eastPanel, BorderLayout.EAST);
	}

	private void moveMonoTo(String position) {
		String command;
		// This moves the DynamicFocus DOF to the given position

		command = moveMonoDofName + ".moveTo(\"" + position + "\")";
		logger.debug("jythonCommand is " + command);
		JythonServerFacade.getInstance().runCommand(command);
	}

	@Override
	public synchronized void run() {
		boolean choochSucceeded = false;
		try {
			p = pb.start();
			BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
			bw.write("This is the title\n");
			bw.flush();
			String line;
			StringTokenizer strtok;
			while ((line = br.readLine()) != null) {
				logger.debug("!!!!! " + line);
				if (line.contains("Aborted")) {
					break;
				}
				if (line.contains("Inflection point")) {
					strtok = new StringTokenizer(line, "|");
					strtok.nextToken();
					strtok.nextToken();
					inflectionPoint = Double.valueOf(strtok.nextToken());
				}
				if (line.contains("Peak")) {
					strtok = new StringTokenizer(line, "|");
					strtok.nextToken();
					strtok.nextToken();
					peak = Double.valueOf(strtok.nextToken());
					remotePoint = peak + 100;
					choochSucceeded = true;
					break;
				}
			}

			if (choochSucceeded) {
				// Must wait for the sub process to terminate correctly or the
				// files
				// will not be closed
				try {
					p.waitFor();
				} catch (InterruptedException ie) {
					logger.error("InterruptedException while waiting for Chooch process to terminate");
				}

				// Now it should be safe to read the data (and the exitValue of
				// the sub-process).
				logger.debug("!!!!!! " + p.exitValue());
				logger.debug("!!!!!! " + inflectionPoint + " " + peak);
				runButton.setEnabled(true);
				inflectionField.setText(String.valueOf(inflectionPoint) + " eV");
				inflectionButton.setEnabled(true);
				peakField.setText(String.valueOf(peak) + " eV");
				peakButton.setEnabled(true);
				remoteField.setText(String.valueOf(remotePoint) + " eV");
				remoteButton.setEnabled(true);

				readEfsFile(fileToWorkOn.replaceFirst(".dat", ".cuc") + ".efs");
			} else {
				JOptionPane.showMessageDialog(this, choochFailureMessage + fileToWorkOn, "Chooch Failure",
						JOptionPane.ERROR_MESSAGE);
			}

			runButton.setEnabled(true);
		} catch (IOException e) {
			logger.debug(e.getStackTrace().toString());
		}

	}

	private void readEfsFile(String filename) {
		BufferedReader br;
		String line;
		int numberOfPoints;
		double[] energy;
		double[] fPrimed;
		double[] fDoublePrimed;
		StringTokenizer strtok;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(filename))));

			line = br.readLine();
			line = br.readLine();
			strtok = new StringTokenizer(line);
			numberOfPoints = Integer.valueOf(strtok.nextToken());
			energy = new double[numberOfPoints];
			fPrimed = new double[numberOfPoints];
			fDoublePrimed = new double[numberOfPoints];
			for (int i = 0; i < numberOfPoints; i++) {
				line = br.readLine();
				strtok = new StringTokenizer(line);
				energy[i] = Double.valueOf(strtok.nextToken());
				// NB chooch produces f'' first for some reason
				fDoublePrimed[i] = Double.valueOf(strtok.nextToken());
				fPrimed[i] = Double.valueOf(strtok.nextToken());
			}
			br.close();
			choochPlot.initializeLine(0);
			choochPlot.setLineName(0, "f'");
			choochPlot.setLinePoints(0, energy, fPrimed);
			choochPlot.initializeLine(1);
			choochPlot.setLineName(1, "f''");
			choochPlot.setLinePoints(1, energy, fDoublePrimed);
			choochPlot.setTitle("f' and f'' from " + filename.replaceAll("cuc.efs", "dat"));

		} catch (Exception e) {
			logger.debug(e.getStackTrace().toString());
		}

	}

	/**
	 * @return inflectionPoint
	 */
	public double getInflectionPoint() {
		return inflectionPoint;
	}

	/**
	 * @return peak
	 */
	public double getPeak() {
		return peak;
	}

	/**
	 * 
	 */
	@SuppressWarnings("unused")
	public void start() {
		if (fileToWorkOn == null || elementName == null || edgeType == null) {

		} else {
			String choochName = fileToWorkOn.replaceFirst(".dat", ".cuc");
			new SRSToChooch(fileToWorkOn, choochName, 6.271);
			logger.debug("trying to convert " + fileToWorkOn + " to " + choochName);

			// String directory =
			// PathConstructor.createFromDefaultProperty();
			String directory = (new File(fileToWorkOn)).getParent();
			pb = new ProcessBuilder("/pxbin/runchooch", elementField.getText(), choochName, edgeTypeField.getText());
			Map<String, String> env = pb.environment();
			String path = env.get("PATH");
			logger.debug("PATH is " + path);
			path = "/pxbin:" + path;
			env.put("PATH", path);
			pb.directory(new File(directory));

			pb.redirectErrorStream(true);
			thread = uk.ac.gda.util.ThreadManager.getThread(this);
			thread.start();
		}
	}

	/**
	 * @param fileToWorkOn
	 */
	public void setFileToWorkOn(String fileToWorkOn) {
		this.fileToWorkOn = fileToWorkOn;
		fileNameField.setText(fileToWorkOn);
	}

	/**
	 * @param elementName
	 */
	public void setElementName(String elementName) {
		this.elementName = elementName;
		elementField.setText(elementName);
	}

	/**
	 * @param edgeType
	 */
	public void setEdgeType(String edgeType) {
		this.edgeType = edgeType;
		edgeTypeField.setText(edgeType);
	}
}
