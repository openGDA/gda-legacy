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

package gda.util;

import gda.gui.GUIMessagePanel;

import java.awt.BorderLayout;

import javax.swing.JPanel;

/**
 * DebugPanel Class
 */
public class DebugPanel extends GUIMessagePanel {
	private JPanel panel;

	@Override
	public void configure() {
		setLayout(new BorderLayout());

		panel = DebugPanelAppender.getPanel();

		if (panel != null)
			add(panel, BorderLayout.CENTER);

	}
}
