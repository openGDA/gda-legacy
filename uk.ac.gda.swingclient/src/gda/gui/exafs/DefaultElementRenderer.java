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
import java.awt.Color;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;

/**
 * DefaultElementRenderer makes a panel containing the name and atomic number of the element.
 */
public class DefaultElementRenderer extends ElementRenderer {

	/**
	 * @param bg sets the background color of the sybol pane
	 */
	public void setSymboPaneBackground(Color bg){
		symbolPanel.setBackground(bg);
	}
	private JPanel symbolPanel;

	protected static Color[] colors = { Color.yellow, Color.orange, Color.green, Color.magenta, Color.pink, Color.cyan,
			Color.red };

	/**
	 * Constructor
	 * 
	 * @param element
	 *            the element to be represented
	 */

	public DefaultElementRenderer(Element element) {
		super(element);

		setBackground(colors[element.getType()]);
		setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));

		/* The symbol panel is the CENTER part of a BorderLayout */
		/* this means that it will take up any spare space. */
		/* Any additional parts which subclasses add should be */
		/* put into the other parts of the BorderLayout. */
		setLayout(new BorderLayout());
		symbolPanel = createSymbolPanel(element);
		add(symbolPanel, BorderLayout.CENTER);
		setToolTipText(element.getName());
	}

	/**
	 * Creates a JPanel containing the elements symbol and atomic number
	 * 
	 * @param element
	 *            the element to be rendered
	 * @return the panel
	 */
	private JPanel createSymbolPanel(Element element) {
		JPanel jPanel = new JPanel();
		JLabel nLabel = new JLabel(String.valueOf(element.getAtomicNumber()));
		JLabel sLabel = new JLabel(element.getSymbol());

		jPanel.setLayout(new GridLayout(0, 1));
		jPanel.setBackground(colors[element.getType()]);

		nLabel.setFont(PropertyHandler.getNumberFont());
		nLabel.setHorizontalAlignment(SwingConstants.CENTER);

		jPanel.add(nLabel);

		sLabel.setFont(PropertyHandler.getSymbolFont());
		sLabel.setHorizontalAlignment(SwingConstants.CENTER);

		jPanel.add(sLabel);

		return jPanel;
	}

}