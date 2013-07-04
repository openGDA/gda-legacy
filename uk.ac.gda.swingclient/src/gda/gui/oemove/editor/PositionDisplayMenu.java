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

package gda.gui.oemove.editor;

import gda.gui.oemove.DOFImageView;

import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

/**
 * PositionDisplayMenu Class
 */
public class PositionDisplayMenu extends JPopupMenu {
	private String[] compassPoints = { "None", "North", "NorthEast", "East", "SouthEast", "South", "SouthWest", "West",
			"NorthWest" };

	private String[] integers = { "1", "2", "3", "4", "5", "6", "7", "8", "9" };

	private DOFImageView dv;

	/**
	 * @param dofView
	 */
	public PositionDisplayMenu(DOFImageView dofView) {
		this.dv = dofView;
		JMenu compassMenu = new JMenu("PositionDisplay");
		JMenu integerMenu = new JMenu("PositionDisplaySize");
		add(compassMenu);
		add(integerMenu);

		for (int i = 0; i < compassPoints.length; i++) {
			JMenuItem menuItem = new JMenuItem(compassPoints[i]);
			menuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent ev) {
					dv.setLabelPosition(ev.getActionCommand());
					dv.displayLabel();
				}
			});
			compassMenu.add(menuItem);
		}

		for (int i = 0; i < integers.length; i++) {
			JMenuItem menuItem = new JMenuItem(integers[i]);
			menuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent ev) {
					dv.setLabelSize(Integer.valueOf(ev.getActionCommand()));
					dv.displayLabel();
				}
			});
			integerMenu.add(menuItem);
		}
	}

	/**
	 * @param position
	 * @return GridBagConstraints
	 */
	public static GridBagConstraints convertCompassPoint(String position) {
		GridBagConstraints c = new GridBagConstraints();

		c.gridx = 1;
		c.gridy = 1;

		if (position.startsWith("Nor")) // north
			c.gridy = 0;
		else if (position.startsWith("S")) // south
			c.gridy = 2;

		if (position.indexOf("E") != -1) // east
			c.gridx = 2;
		else if (position.indexOf("W") != -1) // west
			c.gridx = 0;

		return c;
	}
}
