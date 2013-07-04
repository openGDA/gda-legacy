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

package gda.gui.mi;

import gda.gui.AcquisitionPanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagLayout;

import javax.swing.JPanel;
import javax.swing.JSplitPane;

/**
 * ImageCollectionPanel Class
 */
public class ImageCollectionPanel extends AcquisitionPanel
// implements IObserver, Scannable, Findable
{

	private JSplitPane splitPane;

	private JPanel inputDataPanel;

	private JPanel imageDisplayPanel;

	/**
	 * This is the default constructor
	 */
	public ImageCollectionPanel() {
	}

	@Override
	public void configure() {
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true);
		splitPane.setLeftComponent(createInputDataPanel());
		splitPane.setResizeWeight(0.0);
		splitPane.setOneTouchExpandable(true);
		splitPane.setContinuousLayout(true);

		setLayout(new BorderLayout());
		add(splitPane, BorderLayout.CENTER);
		// Dimension size = new Dimension(150,150);
		// imageDisplayPanel.setMinimumSize(size);
		splitPane.setRightComponent(createImageDisplayPanel());
	}

	private Component createImageDisplayPanel() {
		imageDisplayPanel = new JPanel();
		imageDisplayPanel.setLayout(new GridBagLayout());
		// GridBagConstraints c = new GridBagConstraints();
		imageDisplayPanel.setBackground(Color.YELLOW);

		return imageDisplayPanel;
	}

	private JPanel createInputDataPanel() {
		inputDataPanel = new JPanel();
		inputDataPanel.setLayout(new GridBagLayout());
		// GridBagConstraints c = new GridBagConstraints();
		inputDataPanel.setBackground(Color.RED);

		return inputDataPanel;
	}

}
