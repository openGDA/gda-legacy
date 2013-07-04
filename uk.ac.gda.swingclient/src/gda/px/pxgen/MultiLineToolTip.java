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

package gda.px.pxgen;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.StringTokenizer;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.UIManager;

/**
 * MultiLineToolTip Class
 */
public class MultiLineToolTip extends JWindow {
	private String line;

	private JLabel label;

	private int width = 0, height = 0;

	/**
	 * @param text
	 */
	public MultiLineToolTip(String text) {
		StringTokenizer st = new StringTokenizer(text, "\n");
		JPanel panel = new JPanel();
		Color toolTipBackground = UIManager.getColor("ToolTip.background");
		GridBagConstraints c = new GridBagConstraints();
		int lineWidth = 0;
		int lineHeight = 0;
		Dimension d;

		panel.setLayout(new GridBagLayout());
		c.gridx = 0;
		c.gridy = GridBagConstraints.RELATIVE;
		c.fill = GridBagConstraints.BOTH;
		c.weighty = 1.0;

		while (st.hasMoreTokens()) {
			line = st.nextToken();
			label = new JLabel(line);
			d = label.getPreferredSize();
			lineHeight = d.height;
			lineWidth = d.width;
			height += lineHeight;
			if (lineWidth > width)
				width = lineWidth;
			panel.add(label, c);
		}

		panel.setBorder(BorderFactory.createLineBorder(Color.black));
		if (toolTipBackground != null)
			panel.setBackground(toolTipBackground);
		width += 15;
		height += 10;
		setSize(width, height);

		getContentPane().add(panel, BorderLayout.CENTER);
	}

	@Override
	public void setLocation(int x, int y) {
		super.setLocation(x, y);
	}
}
