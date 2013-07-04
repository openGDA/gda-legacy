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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

/**
 * RadioButtonPanel Class
 */
public class RadioButtonPanel extends JPanel implements ItemListener {
	private ButtonGroup bg;

	private ArrayList<RadioButtonPanelObserver> observers = new ArrayList<RadioButtonPanelObserver>();

	private JRadioButton[] radioButtons;

	/**
	 * @param lineNames
	 */
	public RadioButtonPanel(ArrayList<String> lineNames) {
		this(lineNames, null);
	}

	/**
	 * @param lineNames
	 * @param title
	 */
	public RadioButtonPanel(ArrayList<String> lineNames, String title) {
		super();
		bg = new ButtonGroup();

		JPanel radioButtonsPanel = new JPanel(new GridLayout(0, 1));
		Border b = BorderFactory.createEtchedBorder();
		radioButtonsPanel.setBorder(BorderFactory.createTitledBorder(b, title, TitledBorder.TOP, TitledBorder.CENTER));

		radioButtons = new JRadioButton[lineNames.size()];

		for (int i = 0; i < lineNames.size(); i++) {
			radioButtons[i] = new JRadioButton(lineNames.get(i));
			radioButtonsPanel.add(radioButtons[i]);
			bg.add(radioButtons[i]);
			radioButtons[i].addItemListener(this);
		}

		if (lineNames.size() >= 1) {
			bg.setSelected(radioButtons[0].getModel(), true);
		}
		add(radioButtonsPanel);
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		if (e.getStateChange() == ItemEvent.SELECTED) {
			JRadioButton jrb = (JRadioButton) e.getSource();
			int selected = -1;
			for (int i = 0; i < radioButtons.length; i++)
				if (jrb == radioButtons[i]) {
					selected = i;
					break;
				}
			for (RadioButtonPanelObserver ndo : observers) {
				ndo.radioButtonPanelChanged(this, selected);
			}
		}
	}

	/**
	 * @param ndo
	 */
	public void addObserver(RadioButtonPanelObserver ndo) {
		observers.add(ndo);
	}

}
