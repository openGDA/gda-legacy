/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council
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

package gda.gui.dv.panels;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JColorChooser;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import de.jreality.ui.viewerapp.ViewerApp;

/**
 * Popup Tool Menu for different tools that operate on the DataSet3DPlot panel
 */

public class DataSet3DToolMenu extends JPopupMenu implements MouseListener, ActionListener {

	private JMenuItem mtmChangeColour = null;
	private Component viewerComp = null;
	private ViewerApp viewer = null;

	/**
	 * Constructor for the popup menu associated to the 3D viewer
	 * 
	 * @param ap
	 *            jReality Viewer Application
	 */

	public DataSet3DToolMenu(ViewerApp ap) {
		mtmChangeColour = new JMenuItem("Change background colour");
		mtmChangeColour.addActionListener(this);
		this.add(mtmChangeColour);
		viewer = ap;
		viewerComp = ap.getContent();
		viewerComp.addMouseListener(this);
	}

	private void checkOnPopup(MouseEvent evt) {
		if (evt.isPopupTrigger()) {
			this.show(evt.getComponent(), evt.getX(), evt.getY());
		}
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		checkOnPopup(arg0);
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// Nothing to do

	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// Nothing to do
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		checkOnPopup(arg0);
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		checkOnPopup(arg0);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		viewer.setBackgroundColor(JColorChooser.showDialog(viewerComp, "Background colour", java.awt.Color.black));
	}
}
