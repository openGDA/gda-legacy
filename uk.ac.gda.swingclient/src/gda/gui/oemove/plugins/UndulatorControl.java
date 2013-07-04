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

import gda.factory.Configurable;
import gda.factory.Findable;
import gda.factory.Finder;
import gda.gui.oemove.Pluggable;
import gda.gui.oemove.control.DefaultDOFPositionDisplay;
import gda.gui.oemove.control.HarmonicDOFInputDisplay;
import gda.gui.oemove.control.HarmonicDOFPositionDisplay;
import gda.gui.oemove.control.PolarizationDOFInputDisplay;
import gda.gui.oemove.control.PolarizationDOFPositionDisplay;
import gda.jython.JythonServerFacade;
import gda.observable.IObserver;
import gda.oe.MoveableStatus;
import gda.oe.OE;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.jfree.chart.annotations.XYTextAnnotation;

/**
 * UndulatorControl Class
 */
public class UndulatorControl implements Configurable, Findable, Pluggable, IObserver {
	private String name;

	private String undulatorName;

	private String undulatorMediatorName;

	private JButton moveButton;

	private OE undulator;

	private JythonServerFacade jsf;

	private JTextField energyField;

	private PolarizationDOFInputDisplay polarizationCombo;

	private HarmonicDOFInputDisplay harmonicCombo;

	private JTextField currentEnergyField;

	private JTextField currentPolarizationField;

	private JTextField currentHarmonicField;

	private JPanel displayComponent;

	private JPanel buttonPanel;

	XYTextAnnotation ta;

	@Override
	public void configure() {
		undulator = (OE) Finder.getInstance().find(undulatorName);
		jsf = JythonServerFacade.getInstance();

		createDisplayComponent();
		undulator.addIObserver(this);
	}

	@Override
	public JComponent getDisplayComponent() {
		return displayComponent;
	}

	@Override
	public JComponent getControlComponent() {
		return new JPanel();
	}

	/**
	 * @return undulatorMediatorName
	 */
	public String getUndulatorMediatorName() {
		return undulatorMediatorName;
	}

	/**
	 * @param undulatorMediatorName
	 */
	public void setUndulatorMediatorName(String undulatorMediatorName) {
		this.undulatorMediatorName = undulatorMediatorName;
	}

	/**
	 * @return undulatorName
	 */
	public String getUndulatorName() {
		return undulatorName;
	}

	/**
	 * @param undulatorName
	 */
	public void setUndulatorName(String undulatorName) {
		this.undulatorName = undulatorName;
	}

	private void createDisplayComponent() {
		displayComponent = new JPanel();
		// JTabbedPane jtp = new JTabbedPane();
		JPanel innerDisplayComponent = new JPanel(new BorderLayout());
		JPanel topPanel = new JPanel(new BorderLayout());
		JPanel changes = new JPanel(new GridLayout(4, 0));
		JPanel current = new JPanel(new BorderLayout());
		JPanel currentLabels = new JPanel(new GridLayout(4, 0));
		JPanel currentFields = new JPanel(new GridLayout(4, 0));

		currentEnergyField = new DefaultDOFPositionDisplay(undulator, "UndulatorEnergy", 15, false);
		currentPolarizationField = new PolarizationDOFPositionDisplay(undulator, "UndulatorPolarization", 15, false);
		currentHarmonicField = new HarmonicDOFPositionDisplay(undulator, "UndulatorHarmonic", 15, false);

		currentLabels.add(new JLabel(""));
		currentLabels.add(new JLabel("Energy (eV"));
		currentLabels.add(new JLabel("Polarization"));
		currentLabels.add(new JLabel("Harmonic"));
		current.add(currentLabels, BorderLayout.WEST);

		currentFields.add(new JLabel("Current"));
		currentFields.add(currentEnergyField);
		currentFields.add(currentPolarizationField);
		currentFields.add(currentHarmonicField);
		current.add(currentFields, BorderLayout.EAST);
		topPanel.add(current, BorderLayout.WEST);

		energyField = new JTextField(currentEnergyField.getText(), 10);
		polarizationCombo = new PolarizationDOFInputDisplay();
		polarizationCombo.setBorder(null);
		harmonicCombo = new HarmonicDOFInputDisplay();
		harmonicCombo.setBorder(null);
		changes.add(new JLabel("Move to"));
		changes.add(energyField);
		changes.add(polarizationCombo);
		changes.add(harmonicCombo);
		topPanel.add(changes, BorderLayout.EAST);
		topPanel.setBorder(BorderFactory.createEtchedBorder());

		createButtonPanel();
		innerDisplayComponent.add(topPanel, BorderLayout.NORTH);
		innerDisplayComponent.add(buttonPanel, BorderLayout.SOUTH);

		// jtp.addTab("Move", innerDisplayComponent);

		// createViewPanel();
		// DO NOT REMOVE THIS, it is commented out because the viewPanel is
		// still
		// under test
		// jtp.addTab("View", viewPanel);
		// displayComponent.add(jtp);
		displayComponent.add(innerDisplayComponent);
	}

	private void move() {
		try {
			moveButton.setEnabled(false);
			double energy = Double.valueOf(energyField.getText());
			String command = "import org.jscience; import javax.swing.JOptionPane;umc = gda.factory.Finder.getInstance().find(\""
					+ undulatorMediatorName + "\");";
			command = command + "umc.setRequestedHarmonic(org.jscience.physics.quantities.Quantity.valueOf("
					+ harmonicCombo.getValue() + ", org.jscience.physics.units.Unit.ONE));";
			command = command + "umc.setRequestedPolarization(org.jscience.physics.quantities.Quantity.valueOf("
					+ polarizationCombo.getValue() + ", org.jscience.physics.units.NonSI.DEGREE_ANGLE));";

			command = command + "\ntry:\n\tUndulatorEnergy.moveTo(gda.util.QuantityFactory.createFromString(\""
					+ energy + " eV\"))";
			command = command + ";FixedFocus.moveTo(gda.util.QuantityFactory.createFromString(\"" + energy + " eV\"))"
					+ "\n";
			command = command + "except gda.jython.scannable.ScannableException, se:";
			// Really should display the message from the Exception at this
			// point
			// but it is complicated because it has been added to by
			// UndulatorEnergy and Scannable and does not indicate the
			// likely
			// cause of the problem - that the requested move would move the
			// gap
			// or phase outside soft limits.
			command = command + "\n\tjavax.swing.JOptionPane.showMessageDialog(None,";
			command = command + " \"Move incomplete.\\nCheck requested move is within limits.\");";
			jsf.runCommand(command);
		} catch (NumberFormatException nfe) {
			JOptionPane.showMessageDialog(null, "Invalid energy specified", "", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void createButtonPanel() {
		buttonPanel = new JPanel();
		moveButton = new JButton("Move");
		moveButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				move();
			}
		});
		buttonPanel.add(moveButton);
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void update(Object theObserved, Object changeCode) {
		if (changeCode instanceof MoveableStatus) {
			MoveableStatus ms = (MoveableStatus) changeCode;
			if (ms.getMoveableName().equals("UndulatorEnergy")) {
				moveButton.setEnabled(!(ms.value() == MoveableStatus.BUSY));
			}
		}
	}
}
