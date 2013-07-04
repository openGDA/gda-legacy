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

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

/**
 * CheckBoxPanel
 */
public class CheckBoxPanel extends JPanel implements ActionListener {
	private ArrayList<String> names;

	private JCheckBox[] checkBoxes;

	private boolean[] isSelectedArray;

	private ArrayList<CheckBoxPanelObserver> observers = new ArrayList<CheckBoxPanelObserver>();

	/**
	 * @param names
	 */
	public CheckBoxPanel(ArrayList<String> names) {
		this.names = names;
		JPanel checkBoxesPanel = new JPanel(new GridLayout(0, 1));
		Border b = BorderFactory.createEtchedBorder();
		checkBoxesPanel.setBorder(BorderFactory.createTitledBorder(b, "Plot Shows", TitledBorder.TOP,
				TitledBorder.CENTER));

		checkBoxes = new JCheckBox[names.size()];
		isSelectedArray = new boolean[names.size()];

		for (int i = 0; i < names.size(); i++) {
			checkBoxes[i] = new JCheckBox(names.get(i));
			checkBoxesPanel.add(checkBoxes[i]);
			checkBoxes[i].addActionListener(this);
			isSelectedArray[i] = false;
		}

		add(checkBoxesPanel);
	}

	/**
	 * Implements the ActionListener interface.
	 * 
	 * @param ae
	 *            the action event
	 */
	@Override
	public void actionPerformed(ActionEvent ae) {
		for (int i = 0; i < names.size(); i++) {
			isSelectedArray[i] = checkBoxes[i].isSelected();
		}

		for (CheckBoxPanelObserver cbdo : observers)
			cbdo.checkBoxPanelChanged(this, isSelectedArray);
	}

	/**
	 * @param cbdo
	 */
	public void addObserver(CheckBoxPanelObserver cbdo) {
		observers.add(cbdo);
	}

	/**
	 * @param i
	 * @param value
	 */
	public void setSelected(int i, boolean value) {
		if (!(checkBoxes[i].isSelected() == value))
			checkBoxes[i].doClick();
	}

	/**
	 * @return boolean isSelectedArray
	 */
	public boolean[] getSelected() {

		return isSelectedArray;
	}

}
