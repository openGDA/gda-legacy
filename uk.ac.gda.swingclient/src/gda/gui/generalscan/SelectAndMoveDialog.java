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

package gda.gui.generalscan;

import gda.jython.JythonServerFacade;
import gda.plots.SimplePlot;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import org.jscience.physics.quantities.Quantity;
import org.jscience.physics.units.Unit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dialog which allows users to select points from a graph and move something to the corresponding x value. The point
 * can be selected either by clicking on the plot or by specifying one of the lines and searching for its peak value.
 */
public class SelectAndMoveDialog extends JDialog {
	private static final Logger logger = LoggerFactory.getLogger(SelectAndMoveDialog.class);

	private ArrayList<String> peakingAlgorithms;

	private JythonServerFacade scriptingMediator = JythonServerFacade.getInstance();

	private JTextField scannedDOFField = new JTextField();

	private JTextField currentPositionField = new JTextField();

	private JTextField targetField = new JTextField();

	private JButton findPeakButton;

	private JButton moveButton;

	private JButton stopButton;

	private JButton selectPointButton;

	private CoordinateGetter cg;

	private DOFMover dm;

	private double[] coords;

	private SimplePlot simplePlot;

	private String scannedObjName;

	private Unit<? extends Quantity> scanUnits;

	private ArrayList<String> lineNames;

	private RadioButtonPanel peakingLinePanel = null;

	private double xOfPeak;

	RadioButtonPanel peakingAlgorithmPanel;

	private int selectedLine = 0;

	private int selectedAlgorithm = 0;

	private class CoordinateGetter implements Runnable {
		@Override
		public void run() {
			coords = simplePlot.getCursorCoordinates();
			setTargetPosition(coords[0]);
			selectPointButton.setEnabled(true);
		}
	}

	private class DOFMover implements Runnable {
		@Override
		public void run() {
			String dofName = scannedObjName.substring(scannedObjName.lastIndexOf('.') + 1);
			String command = dofName + ".asynchronousMoveTo(\"" + targetField.getText() + " " + scanUnits + "\")";
			scriptingMediator.evaluateCommand(command);

			while (scriptingMediator.evaluateCommand(dofName + ".isMoving()").equals("1")) {
				currentPositionField.setText(scriptingMediator.evaluateCommand(scannedDOFField.getText()
						+ ".getPosition()"));
			}
			stopButton.setEnabled(false);
			selectPointButton.setEnabled(true);
			findPeakButton.setEnabled(true);
		}
	}

	private JPanel createButtonPanel() {
		JPanel buttonPanel = new JPanel(new GridLayout(0, 1));
		findPeakButton = new JButton("FindPeak");
		findPeakButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				findPeak();
			}
		});
		findPeakButton.setToolTipText("Set target position by finding peak of specified line");
		buttonPanel.add(findPeakButton);

		selectPointButton = new JButton("SelectPoint");
		selectPointButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				selectPoint();
			}
		});
		selectPointButton.setToolTipText("Set target position by clicking on graph");
		buttonPanel.add(selectPointButton);

		moveButton = new JButton("Move");
		moveButton.setEnabled(false);
		moveButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				move();
			}
		});
		moveButton.setToolTipText("Move scanned dof to target position");
		buttonPanel.add(moveButton);

		stopButton = new JButton("Stop");
		stopButton.setEnabled(false);
		stopButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				stop();
			}
		});
		stopButton.setToolTipText("Stop the DOF");
		buttonPanel.add(stopButton);

		JButton doneButton = new JButton("Done");
		doneButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);

			}
		});
		doneButton.setToolTipText("Popdown this window");
		buttonPanel.add(doneButton);

		buttonPanel.setBorder(BorderFactory.createEtchedBorder());

		return buttonPanel;
	}

	/**
	 * @param simplePlot
	 * @param lineNames
	 */
	public SelectAndMoveDialog(SimplePlot simplePlot, ArrayList<String> lineNames) {
		// NB if you use null you must cast because there are
		// two JDialog constructors which would match
		super((Frame) null, "SelectAndMove", false);

		this.lineNames = lineNames;
		this.simplePlot = simplePlot;

		JPanel panel = new JPanel(new BorderLayout());
		JPanel movingDOFPanel = createMovingDOFPanel();

		JPanel yap = new JPanel(new BorderLayout());

		peakingLinePanel = new RadioButtonPanel(lineNames, "Line to Peak");
		peakingLinePanel.addObserver(new RadioButtonPanelObserver() {

			@Override
			public void radioButtonPanelChanged(RadioButtonPanel nd, int selectedButton) {

				setSelectedLine(selectedButton);
			}
		});

		peakingAlgorithms = new ArrayList<String>();
		peakingAlgorithms.add("BFI");
		peakingAlgorithms.add("Gaussian");
		peakingAlgorithms.add("Lorentzian");
		peakingAlgorithmPanel = new RadioButtonPanel(peakingAlgorithms, "Algorithm");
		peakingAlgorithmPanel.addObserver(new RadioButtonPanelObserver() {

			@Override
			public void radioButtonPanelChanged(RadioButtonPanel nd, int selectedButton) {
				setSelectedAlgorithm(selectedButton);
			}
		});

		panel.add(movingDOFPanel, BorderLayout.NORTH);
		panel.add(peakingLinePanel, BorderLayout.WEST);
		yap.add(peakingAlgorithmPanel, BorderLayout.NORTH);
		yap.add(createInformationPanel(), BorderLayout.CENTER);
		yap.add(createButtonPanel(), BorderLayout.SOUTH);
		panel.add(yap, BorderLayout.EAST);
		getContentPane().add(panel);
		pack();
		setMinimumSize(getSize());
	}

	private void move() {
		dm = new DOFMover();
		selectPointButton.setEnabled(false);
		stopButton.setEnabled(true);
		moveButton.setEnabled(false);
		uk.ac.gda.util.ThreadManager.getThread(dm, "DOFMover").start();
	}

	private void stop() {
		String dofName = scannedObjName.substring(scannedObjName.lastIndexOf('.') + 1);
		String command = dofName + ".stop()";
		scriptingMediator.evaluateCommand(command);
	}

	private void selectPoint() {
		selectPointButton.setEnabled(false);
		findPeakButton.setEnabled(false);
		cg = new CoordinateGetter();
		uk.ac.gda.util.ThreadManager.getThread(cg, "CoordinateGetter").start();
	}

	/**
	 * @param name
	 */
	public void setScannedDOFName(String name) {
		scannedObjName = name;
		scannedDOFField.setText(name);
		currentPositionField.setText(scriptingMediator.evaluateCommand(name + ".getPosition()"));
		pack();
	}

	private JPanel createInformationPanel() {
		JPanel panel = new JPanel(new BorderLayout());

		panel.add(new JLabel("<html> choose a line" + "<br>" + " click FindPeak" + "<br><br>" + "<bold> OR</bold>"
				+ "<br><br>" + " click SelectPoint" + "<br>" + " click on plot</html>"));

		panel.setBorder(BorderFactory.createEtchedBorder());
		return panel;
	}

	private JPanel createMovingDOFPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		JPanel leftPanel = new JPanel(new GridLayout(3, 0));
		JPanel rightPanel = new JPanel(new GridLayout(3, 0));

		leftPanel.add(new JLabel("Name"));
		leftPanel.add(new JLabel("Current Position"));
		leftPanel.add(new JLabel("Target Position"));

		rightPanel.add(scannedDOFField);
		rightPanel.add(currentPositionField);
		rightPanel.add(targetField);
		panel.add(leftPanel, BorderLayout.WEST);
		panel.add(rightPanel, BorderLayout.CENTER);

		Border b = BorderFactory.createEtchedBorder();
		panel.setBorder(BorderFactory.createTitledBorder(b, "Scanned DOF", TitledBorder.TOP, TitledBorder.CENTER));

		return panel;
	}

	/**
	 * @param scanUnits
	 *            The scanUnits to set.
	 */
	public void setScanUnits(Unit<? extends Quantity> scanUnits) {
		this.scanUnits = scanUnits;
	}

	/**
	 * @param target
	 */
	public void setTargetPosition(double target) {
		targetField.setText(String.valueOf(target));
		moveButton.setEnabled(true);
	}

	private void findPeak() {
		selectPointButton.setEnabled(false);
		findPeakButton.setEnabled(false);
		logger.debug("Would find peak in " + lineNames.get(selectedLine) + " using "
				+ peakingAlgorithms.get(selectedAlgorithm));

		logger.debug("!!!!! " + simplePlot.getLineXValueOfPeak(lineNames.get(selectedLine)));
		xOfPeak = simplePlot.getLineXValueOfPeak(lineNames.get(selectedLine));
		setTargetPosition(xOfPeak);
		moveButton.setEnabled(true);
	}

	private void setSelectedLine(int selectedButton) {
		if (selectedLine != selectedButton) {
			findPeakButton.setEnabled(true);
			selectPointButton.setEnabled(true);
			moveButton.setEnabled(false);
			targetField.setText("");
			selectedLine = selectedButton;
		}
	}

	private void setSelectedAlgorithm(int selectedButton) {
		selectedAlgorithm = selectedButton;
	}
}
