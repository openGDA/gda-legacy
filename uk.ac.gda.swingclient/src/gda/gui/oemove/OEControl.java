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

package gda.gui.oemove;

import gda.gui.oemove.control.DOFMode;
import gda.observable.IObservable;
import gda.observable.IObserver;
import gda.observable.UpdateDelayer;
import gda.oe.MoveableException;
import gda.oe.OE;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import javax.swing.border.TitledBorder;

import org.jscience.physics.quantities.Quantity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class has the responsibility of display a set of control buttons that enable all modes of movement of OE
 * possible.
 * <p>
 * It also has to display the DOFControl DOFControlPanel which displays all information of the currently selected Degree
 * of Freedom.
 * <p>
 * A scrollable JTextarea which reports messages from the system is displayed by this class.
 */
public class OEControl extends JPanel implements IObserver {
	
	private static final Logger logger = LoggerFactory.getLogger(OEControl.class);
	
	private JPanel buttonPanel;

	private JPanel dofPanel;

	private JPanel dofChooserPanel;

	private JButton start;

	private JButton stop;

	private JButton set;

	private JButton help;

	private JButton refresh;

	private JComboBox dofComboBox;

	private String dofNameSelected;

	private OE oe;

	private Map<String, JComponent> dofControlPanels = new LinkedHashMap<String, JComponent>();

	private DOFControlPanel currentDOFControlPanel = null;

	private Map<String, Viewable> views = new LinkedHashMap<String, Viewable>();

	private DirectionToggle directionToggle;

	private String helpMessage = "Use this window to drive beam line Optical Elements (OEs).\n\n"
			+ "An OE is some item of beam line equipment, usually used to\n"
			+ "condition the beam but not exclusively so.\n" + "Graphical representations of OEs can be "
			+ "displayed using the menus,\n" + "or menu, at the top of the window. Internal windows are created,\n"
			+ "each showing one representation of an OE.\n\n" + "Each OE has a number of Degrees Of Freedom (DOFs).\n"
			+ "Select the particular DOF you want to drive by either clicking\n"
			+ "on the graphical representation or using the \"DOF Name\" menu.\n"
			+ "The selected DOF for each OE representation should be in red.\n\n"
			+ "Parameters for the selected DOF on the selected OE representation\n"
			+ "will be displayed alongside the \"DOF Name\" menu.\n" + "Select the operation using the \"Mode\" menu. "
			+ "Enter an increment or\n" + "position and start the operation. The + and - buttons in each\n"
			+ "internal window can be used in by or continuous mode";

	/**
	 * The constructor creates a display area for a view of the OE model
	 * 
	 * @param representation
	 */
	public OEControl(Representation representation) {
		oe = ((OERepresentation) representation).getOE();
		setLayout(new BorderLayout());
		setBorder(new BevelBorder(BevelBorder.RAISED));

		start = new JButton("Start Move");
		start.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doStart();
			}
		});
		start.setEnabled(true);

		stop = new JButton("Stop Move");
		stop.setForeground(Color.black);
		stop.setBackground(Color.red);
		stop.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doStop();
			}
		});
		stop.setEnabled(true);

		set = new JButton("Set Position");
		set.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doSet();
			}
		});
		set.setEnabled(false);

		refresh = new JButton("Refresh");
		refresh.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doRefresh();
			}
		});

		help = new JButton("Help");
		help.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				JOptionPane.showMessageDialog(getTopLevelAncestor(), helpMessage, "Help Message",
						JOptionPane.INFORMATION_MESSAGE);
			}
		});

		buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		buttonPanel.add(start);
		buttonPanel.add(stop);
		buttonPanel.add(set);
		buttonPanel.add(refresh);
		buttonPanel.add(help);

		dofChooserPanel = getDOFChooserPanel(representation);
		dofPanel = new JPanel();
		dofPanel.setLayout(new FlowLayout());
		dofPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(), BorderFactory
				.createEmptyBorder(0, 0, 0, 0)));

		add(dofPanel, BorderLayout.NORTH);
		add(buttonPanel, BorderLayout.SOUTH);
		// the first dof in the list is selected. This causes an action event and the updateDOFChoice method to be
		// called to add the correct dofControlpanel with the dofcombobox.
		dofComboBox.setSelectedIndex(0);
	}

	@SuppressWarnings("unused")
	private JPanel getDOFChooserPanel(Representation representation) {
		JPanel panel = new JPanel();
		dofComboBox = new JComboBox();
		dofComboBox.setVisible(true);
		dofComboBox.setEnabled(true);

		// items are added to the dofComboBox before the actionListener is added
		// otherwise it would fire for every item added
		Representation child = ((OERepresentation) representation).getCurrentRepresentation();
		directionToggle = ((OEImageView) child).getDirectionToggle();
		directionToggle.setOe(oe);

		for (Viewable view : ((OEImageView) child).getViewableList()) {
			views.put(view.getName(), view);
			DOFImageView dofImageView = (DOFImageView) view;
			dofComboBox.addItem(view.getName());
			JComponent dofControlPanel = dofImageView.getControlPanel(oe, view.getName());
			dofControlPanels.put(view.getName(), dofControlPanel);
			ArrayList<String> modeList = dofImageView.getModeNameList();
			if (!modeList.isEmpty()) {
				((DOFControlPanel) dofControlPanel).setModeNames(modeList);
			}
			ArrayList<String> speedList = dofImageView.getSpeedNameList();
			if (!speedList.isEmpty()) {
				((DOFControlPanel) dofControlPanel).setSpeedNames(speedList);
			}
			((DOFControlPanel) dofControlPanel).setDefaultInputValue(dofImageView.getDefaultInputValue());
			new UpdateDelayer(this, (IObservable) dofControlPanel);
			new UpdateDelayer(this, (IObservable) view);
			DOFInputDisplay dofInputDisplay = ((DOFControlPanel) dofControlPanel).getDOFInputDisplay();
			setButtonState(((DOFControlPanel) dofControlPanel));
			dofInputDisplay.addActionListener(new ActionListener() {
				// this action listener is added to capture <cr> from the input field.
				@Override
				public void actionPerformed(ActionEvent e) {
					// This 'if' allows DOFInputDisplays to stop the <CR> action from working by setting their
					// ActionCommands to something else - see for example HarmonicDOFInputDisplay

					if (e.getActionCommand().equals("Start")) {
						uk.ac.gda.util.ThreadManager.getThread(new Runnable() {
							@Override
							public void run() {
								doStart();
							}
						}).start();
					}
				}
			});

		}
		dofComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				updateDOFChoice();
			}
		});

		if (dofComboBox.getItemCount() > 1) {
			dofComboBox.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "DOF Name",
					TitledBorder.CENTER, TitledBorder.TOP, null, Color.black));

			panel.add(dofComboBox);
		} else {
			JLabel label = new JLabel(dofComboBox.getSelectedItem().toString());
			label.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "DOF Name",
					TitledBorder.CENTER, TitledBorder.TOP, null, Color.black));
			panel.add(label);
		}
		return panel;
	}

	/**
	 * Update the dof control panel
	 */
	public void updateDOFChoice() {
		dofNameSelected = (String) dofComboBox.getSelectedItem();
		if (currentDOFControlPanel != null)
			dofPanel.removeAll();
		currentDOFControlPanel = (DOFControlPanel) dofControlPanels.get(dofNameSelected);
		dofPanel.add(dofChooserPanel);
		dofPanel.add(currentDOFControlPanel);
		directionToggle.setDOFControlPanel(dofNameSelected, currentDOFControlPanel);
		int driveMode = currentDOFControlPanel.getMode();
		directionToggle.setEnabled(driveMode == DOFMode.RELATIVE || driveMode == DOFMode.CONTINUOUS);
		revalidate();
		updateDOFView();
		super.repaint();
	}

	/**
	 * Update the dof view
	 */
	public void updateDOFView() {
		DOFImageView dofImageView = (DOFImageView) views.get(dofNameSelected);
		if (dofImageView != null) {
			dofImageView.setSelectedDOF();
			// bug #564 selecting a DOF on the same OE with a different mode
			// requires the OEcontrol panel to reset its button state.
			setButtonState(currentDOFControlPanel);
		}
	}

	/**
	 * Sets up the buttonPanel for the selected mode of operation.
	 * 
	 * @param iObservable
	 *            the observed class
	 * @param arg
	 *            the change code
	 */
	@Override
	public void update(Object iObservable, Object arg) {
		if (iObservable instanceof DOFControlPanel) {
			setButtonState((DOFControlPanel) iObservable);
		} else if (iObservable instanceof DOFImageView) {
			DOFImageView dofImageView = (DOFImageView) iObservable;
			dofComboBox.setSelectedItem(dofImageView.getName());
		}
	}

	/**
	 * Start movement
	 */
	public void doStart() {
		Quantity inputQuantity = null;
		DOFControlPanel dofControlPanel = currentDOFControlPanel;

		int driveMode = dofControlPanel.getMode();
		try {
			if ((inputQuantity = dofControlPanel.getInputQuantity()) != null) {
				if (driveMode == DOFMode.HOME_SET || driveMode == DOFMode.HOME) {
					oe.home(dofNameSelected);
				} else if ((driveMode == DOFMode.RELATIVE || driveMode == DOFMode.ABSOLUTE)) {
					oe.setSpeedLevel(dofNameSelected, dofControlPanel.getSpeedLevel());
					switch (driveMode) {
					case DOFMode.RELATIVE:
						oe.moveBy(dofNameSelected, inputQuantity);
						break;
					case DOFMode.ABSOLUTE:
						oe.moveTo(dofNameSelected, inputQuantity);
						break;
					}
				}
			}
		} catch (Exception me) {
			String msg = me.getMessage();
			logger.error("OEControl.doStart() caught exception with message: " + msg);
			JOptionPane.showMessageDialog(getTopLevelAncestor(), msg, "Error occurred", JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Stop movement
	 */
	public void doStop() {
		try {
			oe.stop(dofNameSelected);
		} catch (MoveableException me) {
			String msg = me.getMessage();
			logger.error("OEControl.doStop() caught exception with message: " + msg);
			JOptionPane.showMessageDialog(getTopLevelAncestor(), msg, "Error Message", JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Update current positions
	 */
	public void doRefresh() {
		for (JComponent component : dofControlPanels.values()) {
			DOFControlPanel dofControlPanel = (DOFControlPanel) component;
			dofControlPanel.refresh();
		}
	}

	/**
	 * Set position to required value
	 */
	public void doSet() {
		Quantity inputQuantity = null;
		DOFControlPanel dofControlPanel = currentDOFControlPanel;
		int driveMode = dofControlPanel.getMode();

		try {
			if ((inputQuantity = dofControlPanel.getInputQuantity()) != null) {
				if (driveMode == DOFMode.HOME_SET || driveMode == DOFMode.HOME) {
					oe.setHomeOffset(dofNameSelected, inputQuantity);
				} else {
					oe.setPosition(dofNameSelected, inputQuantity);
				}
			}
		} catch (MoveableException me) {
			String msg = me.getMoveableStatus().getMessage();
			logger.error("OEControl.doSet() caught exception with message: " + msg);
			JOptionPane.showMessageDialog(getTopLevelAncestor(), msg, "Error Message", JOptionPane.ERROR_MESSAGE);
		} catch (Exception me) {
			String msg = me.getMessage();
			logger.error("OEControl.doSet() caught exception with message: " + msg);
			JOptionPane.showMessageDialog(getTopLevelAncestor(), msg, "Error Message", JOptionPane.ERROR_MESSAGE);
		}
	}

	@Override
	public void repaint() {
		super.repaint();
		if (dofComboBox != null) {
			updateDOFChoice();
		}
	}

	/**
	 * Set the state of the buttons on the OEControl panel
	 * 
	 * @param dofControlPanel
	 */
	public void setButtonState(DOFControlPanel dofControlPanel) {
		DOFInputDisplay inputData = dofControlPanel.getDOFInputDisplay();
		int mode = dofControlPanel.getMode();
		inputData.setMode(mode);
		// bug #645 the refresh button causes all DOFS to update and
		// consequently this method is called. We therefore require only
		// the currentSelected DOF to change the state of the OE control panel
		if (dofControlPanel.equals(currentDOFControlPanel)) {
			switch (mode) {
			case DOFMode.RELATIVE:
				directionToggle.setEnabled(true);
				inputData.setActionCommand("Start");
				start.setEnabled(true);
				stop.setEnabled(true);
				set.setEnabled(false);
				break;
			case DOFMode.ABSOLUTE:
				directionToggle.setEnabled(false);
				inputData.setActionCommand("Start");
				start.setEnabled(true);
				stop.setEnabled(true);
				set.setEnabled(false);
				break;
			case DOFMode.SET:
			case DOFMode.HOME_SET:
				directionToggle.setEnabled(false);
				inputData.setActionCommand("Set");
				set.setEnabled(true);
				start.setEnabled(false);
				stop.setEnabled(false);
				break;
			case DOFMode.CONTINUOUS:
				inputData.setActionCommand("");
				start.setEnabled(false);
				stop.setEnabled(false);
				directionToggle.setEnabled(true);
				break;
			}
			buttonPanel.revalidate();
			buttonPanel.repaint();
		}
	}
}