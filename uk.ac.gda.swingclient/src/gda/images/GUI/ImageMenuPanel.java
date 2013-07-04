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

// working version ...

package gda.images.GUI;

import gda.configuration.properties.LocalProperties;
import gda.images.ImageOperator;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * Generic Image menu panel
 */

@SuppressWarnings("serial")
public class ImageMenuPanel extends JPanel implements ActionListener {
	protected GridBagConstraints c = new GridBagConstraints();

	protected JMenuBar menuBar = new JMenuBar();

	protected FileMenu fileMenu;

	protected ImageMenu imageMenu;

	protected JMenuItem quitMenuItem = new JMenuItem("Quit");

	protected JMenuItem nullMenuItem;

	protected ImageOperator operator;

	protected boolean toolTips;

	protected boolean enableQuit;

	/**
	 * @param userFile
	 * @param toolTips
	 */
	public ImageMenuPanel(String userFile, boolean toolTips) {
		this.toolTips = toolTips;
		configure();

		setLayout(new BorderLayout());
		menuBar.setLayout(new GridBagLayout());

		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 0.2;

		fileMenu = new FileMenu(userFile, toolTips);
		menuBar.add(fileMenu);

		c.gridx++;
		imageMenu = new ImageMenu(toolTips);
		menuBar.add(imageMenu, c);

		c.gridx++;
		quitMenuItem.setActionCommand("exit");
		quitMenuItem.addActionListener(this);
		quitMenuItem.setEnabled(false);
		menuBar.add(quitMenuItem, c);

		nullMenuItem = new JMenuItem(" ");
		menuBar.add(nullMenuItem, c);

		this.add(menuBar, BorderLayout.CENTER);

		final boolean state = false;
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				fileMenu.setEnabled(state);
				quitMenuItem.setEnabled(state);
				imageMenu.setEnabled(state);
			}
		});
	}

	protected void configure() {
		// userFile = LocalProperties.get("gda.images.userFile", null);
		enableQuit = (LocalProperties.get("gda.images.enableQUIT", "false")).equalsIgnoreCase("true");
		if (toolTips && enableQuit)
			quitMenuItem.setToolTipText("exit application");
	}

	/**
	 * @param operator
	 * @param displayPanel
	 */
	public void initialise(ImageOperator operator, ImageDisplayPanel displayPanel) {
		this.operator = operator;
		fileMenu.initialise(operator);
		imageMenu.initialise(operator, displayPanel);
		setActive(true);
	}

	/**
	 * @param active
	 */
	public void setActive(boolean active) {
		final boolean state = active;
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				fileMenu.setEnabled(state);
				if (enableQuit)
					quitMenuItem.setEnabled(state);
				imageMenu.setEnabled(state);
			}
		});
	}

	// ActionListener interface

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("exit")) {
			System.exit(0);
		}
	}
}
