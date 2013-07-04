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

import gda.util.exafs.Element;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.Border;

/**
 * An ElementRenderer which allows selection of AbsorptionEdges by adding a panel of buttons to DefaultElementRenderer.
 */
public class AbsorptionEdgeElementRenderer extends DefaultElementRenderer implements ActionListener {
	private static Cursor handCursor = new Cursor(Cursor.HAND_CURSOR);

	private static Border emptyBorder = BorderFactory.createEmptyBorder();

	/**
	 * JPanel
	 */
	public final JPanel jPanel;

	/**
	 * Constructor
	 * 
	 * @param element
	 *            the element to be represented
	 */
	public AbsorptionEdgeElementRenderer(Element element) {
		/* Create a DefaultElementRenderer */
		super(element);

		/* Add a panel of buttons representing the edges. This is */
		/* added to the overall BorderLayout as the WEST part - it */
		/* will be given its preferred width if possible. */
		jPanel = createEdgeButtonPanel(element);
		if (jPanel == null) {
			setEnabled(false);
		} else
			add(jPanel, BorderLayout.WEST);
	}

	/**
	 * Creates the panel of buttons representing the edges.
	 * 
	 * @param element
	 *            the element to be represented
	 * @return the panel
	 */
	private JPanel createEdgeButtonPanel(Element element) {
		/* This method of Element returns an iterator which goes */
		/* through the names of the edges in the range */
		Iterator<String> i = element.getEdgesInEnergyRange(PropertyHandler.getMinimumEdgeEnergy(), PropertyHandler
				.getMaximumEdgeEnergy());

		JPanel jPanel = null;

		if (i != null) {
			/* Create a single column grid of buttons representing the */
			/* edges available for this element. */
			JPanel buttonGrid = new JPanel(new GridLayout(0, 1));
			JButton edgeButton;
			for (; i.hasNext();) {
				String edge = i.next();
				if( PropertyHandler.isEdgeListed(edge)){
					edgeButton = createEdgeButton(edge);
					edgeButton.addActionListener(this);
					buttonGrid.add(edgeButton);
				}
			}

			/* Instead of just returning the grid of buttons put it */
			/* inside another BorderLayout as the NORTH part. This */
			/* means that it will be given its preferred height. */
			jPanel = new JPanel(new BorderLayout());
			jPanel.setBackground(colors[element.getType()]);
			jPanel.add(buttonGrid, BorderLayout.NORTH);
		}

		return jPanel;
	}

	/**
	 * To implement the ActionListener interface - will be called when one of the edge buttons is clicked.
	 * 
	 * @param ae
	 *            the ActionEvent caused by the click
	 */

	@Override
	public void actionPerformed(ActionEvent ae) {
		/* Get the actual edge corresponding to the button from */
		/* the element and send it to IObservers. */
		notifyIObservers(this, getElement().getEdge(ae.getActionCommand()));
	}

	/**
	 * Creates a JButton to use to represent an edge. Sets some of the features.
	 * 
	 * @param edgeName
	 * @return the button
	 */
	private JButton createEdgeButton(String edgeName) {
		/* No actionCommand is set so it will default to the */
		/* label which is the edgeName */

		JButton jButton = new JButton(edgeName);

		jButton.setFont(PropertyHandler.getButtonFont());
		jButton.setBackground(colors[getElement().getType()]);
		jButton.setBorder(emptyBorder);
		jButton.setToolTipText("Click to select " + edgeName + " edge for " + getElement().getName());
		jButton.setCursor(handCursor);

		return jButton;
	}
}