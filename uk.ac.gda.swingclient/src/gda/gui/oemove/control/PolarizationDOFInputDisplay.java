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

package gda.gui.oemove.control;

import gda.gui.oemove.DOFInputDisplay;
import gda.oe.dofs.PolarizationValue;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.ComboBoxEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a simple extension of JComboBox which allows fixed polarization types to be displayed and changed.
 */
public class PolarizationDOFInputDisplay extends JComboBox implements DOFInputDisplay {
	
	private static final Logger logger = LoggerFactory.getLogger(PolarizationDOFInputDisplay.class);
	
	private class Editor extends JTextField implements ComboBoxEditor {

		private Editor() {
			// FIXME: this value seems to give sensible behaviour when
			// the menu appears in OEMove. Without it when the
			// textField is used the menu width changes to be too
			// small. However 8 does not seem to correspond to
			// the actual number of columns that appear and any
			// value greater makes the menu too wide.
			super(8);
		}

		@Override
		public Component getEditorComponent() {
			return this;
		}

		@Override
		public Object getItem() {
			return getText();
		}

		@Override
		public void setItem(Object anObject) {
			setText((String) anObject);
		}

	}

	ComboBoxEditor g = getEditor();

	/**
	 * 
	 */
	public PolarizationDOFInputDisplay() {
		addItem("LeftCircular");
		addItem("RightCircular");
		addItem("Horizontal");
		addItem("Vertical (M+)");
		addItem("Vertical (M-)");
		addItem("Vertical (O+)");
		addItem("Vertical (O-)");
		addItem("0.0");
		setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Change To",
				TitledBorder.CENTER, TitledBorder.TOP, null, Color.black));

		// This stops the selecting action from actually starting a move
		// (see OEControl).
		setActionCommand("Do-nothing");

		editor = new Editor();
		setEditor(editor);
		/*
		 * editor.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent ae) {
		 * Message.out("Editor actionPerformed about to super.fireActionEvent with " + getValue());
		 * PolarizationDOFInputDisplay.super.fireActionEvent(); } });
		 */
		addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (((String) e.getItem()).equals(getItemAt(7))) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						setEditable(true);
					} else {
						String newItem = ((JTextField) editor).getText();
						if (getSelectedIndex() == -1) {
							removeItemAt(7);
							insertItemAt(newItem, 7);
						}
						setEditable(false);
					}
				}
			}
		});
	}

	/**
	 * Overrides the super class method to ensure that "Start" cannot be set as the actionCommand. (See
	 * OEControlPanel.update()).
	 * 
	 * @param command
	 *            the command
	 */
	@Override
	public void setActionCommand(String command) {
		String toSet;

		if (command.equals("Start")) {
			toSet = "Do-nothing";
		} else {
			toSet = command;
		}
		super.setActionCommand(toSet);
	}

	@Override
	public void fireActionEvent() {
		// FIXME: ActionEvents are not sent if selecting the ArbitraryAngle
		// item (getSelectedIndex = 7) or if it has been changed
		// (getSelectedIndex = -1). This means that in OEMove you
		// always have to press the start button for ArbitraryAngle.
		// This was the only way to avoid having TWO ActionEvents if
		// you edited the value then pressed the start button.
		if (getSelectedIndex() != 7 && getSelectedIndex() != -1) {
			super.fireActionEvent();
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		final JFrame frame = new JFrame();

		JPanel jPanel = new JPanel();

		final PolarizationDOFInputDisplay pde = new PolarizationDOFInputDisplay();
		jPanel.add(pde);

		pde.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				logger.debug("pde actionListener the value is " + pde.getValue());
			}
		});

		JButton jButton = new JButton("Press");
		jPanel.add(jButton);
		jButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				logger.debug("pde actionListener the value is " + pde.getValue());
			}
		});
		frame.getContentPane().add(jPanel);

		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent ev) {
				System.exit(0);
			}
		});
		frame.pack();
		frame.setVisible(true);
	}

	/**
	 * Returns an angle, the fixed values used are the same as those in UndulatorPolarizationDOF.
	 * 
	 * @return angle of selected item.
	 */
	@Override
	public Double getValue() {
		return PolarizationValue.stringToDouble((String) getSelectedItem());
	}

	@Override
	public void setValue(String value) {
		setSelectedItem(value);
	}

	@Override
	public void setMode(int newMode) {
		// Does nothing deliberately
	}

	@Override
	public String toString() {
		return NOTSAVEABLE;
	}

}