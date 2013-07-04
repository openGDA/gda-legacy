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

/*
 * This is the top level panel which creates and initialises all panels and all command operator objects. It acts as a
 * Mediator (Java pattern) between GUI objects and objects which control the effect of GUI, delegating control to
 * controller classes (colleagues) where possible. It is only really mediating at initialisation, after that delegated
 * control classes take over.
 */

package gda.images.GUI;

import gda.configuration.properties.LocalProperties;
import gda.gui.AcquisitionPanel;
import gda.images.ImageOperator;

import java.awt.BorderLayout;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

/**
 * A generic panel to display and manipulate an image from file. Configuarble options allow monitoring and display of
 * user mouse-clicks and reading from and saving to file. Java Properties : gda.images.menuStyle: boolean; optional;
 * default true; if true then image operations available via menus, if false image operations available via button
 * panel. gda.images.monitorMouseClicks: boolean, optional; default true; if true then PointValueUpdatePanel will
 * monitor user mouse clicks and display it's position value in pixels; position also marked on image as yellow
 * cross-hairs and it's gda.images.toolTips: boolean; optional; default true; if true toolTips are displayed via
 * MouseListener interface gda.images.userFile: String; optional; default ${user.home}/tmp.bmp may be over-ridden by
 * parameter in constructor
 */
@SuppressWarnings("serial")
public class ImagePanel extends AcquisitionPanel {
	protected String title;

	protected boolean menuStyle;

	protected boolean monitorMouseClicks;

	protected ImageDisplayPanel displayPanel;

	protected PointValueUpdatePanel positionPanel;

	protected ImageMenuPanel menuPanel;

	protected ImageButtonPanel imageButtonPanel;

	protected ImageOperator imageOperator;

	protected boolean toolTips;

	protected boolean userOptions = true;

	protected String mouseClickPositionTitle = null;

	protected String userFile = null;

	/**
	 * @param mouseClickPositionTitle
	 * @param imageFile
	 * @param userOptions
	 */
	public ImagePanel(String mouseClickPositionTitle, String imageFile, boolean userOptions) {
		this.userFile = imageFile;
		this.userOptions = userOptions;
		this.mouseClickPositionTitle = mouseClickPositionTitle;
		configure();
	}

	/**
	 * @param imageFile
	 * @param userOptions
	 */
	public ImagePanel(String imageFile, boolean userOptions) {
		this.userFile = imageFile;
		this.userOptions = userOptions;
		configure();
	}

	/**
	 * @param configure
	 */
	public ImagePanel(@SuppressWarnings("unused") boolean configure) {
		configure();
	}

	/**
	 * empty contructor need to maintain AquisitionPanel inheritance
	 */
	public ImagePanel() {
		// empty contructor need to maintain AquisitionPanel inheritance
	}

	@Override
	public void configure() {
		setLayout(new BorderLayout());
		setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLoweredBevelBorder(), BorderFactory
				.createEmptyBorder(5, 3, 5, 3)));

		if (userFile == null)
			userFile = LocalProperties.get("gda.images.userFile", System.getProperty("user.home") + File.separator
					+ "tmp.bmp");
		menuStyle = (LocalProperties.get("gda.images.menuStyle", "false")).equalsIgnoreCase("true");
		monitorMouseClicks = (LocalProperties.get("gda.images.monitorMouseClicks", "true")).equalsIgnoreCase("true");
		toolTips = (LocalProperties.get("gda.images.toolTips", "true")).equalsIgnoreCase("true");

		// README must create panels before operator
		createPanels();
		imageOperator = createImageOperator();

		initialise();
	}

	protected ImageOperator createImageOperator() {
		imageOperator = new ImageOperator(this, userFile);
		return imageOperator;
	}

	protected void addObservability() {
		displayPanel.addIObserver(positionPanel);
	}

	protected void createPanels() {
		displayPanel = new ImageDisplayPanel(monitorMouseClicks);
		if (monitorMouseClicks) {
			positionPanel = new PointValueUpdatePanel(mouseClickPositionTitle);
		}
		if (menuStyle) {
			menuPanel = new ImageMenuPanel(userFile, toolTips);
		} else {
			imageButtonPanel = new ImageButtonPanel(userFile, toolTips);
		}

		if (userOptions) {
			add(menuStyle ? (JPanel) menuPanel : (JPanel) imageButtonPanel, BorderLayout.NORTH);
		}
		add(displayPanel, BorderLayout.CENTER);

		if (positionPanel != null) {
			add(positionPanel, BorderLayout.SOUTH);
		}
	}

	/**
	 * @return displayPanel
	 */
	public ImageDisplayPanel getDisplayPanel() {
		return displayPanel;
	}

	// public String getUserFile()
	// {
	// return menuStyle ? menuPanel.getUserFile()
	// : imageButtonPanel.getUserFile();
	// }

	/**
	 * 
	 */
	public void initialise() {
		// MessageOutput.appendTextLater(" initializing image panels...");

		displayPanel.initialise(imageOperator);

		if (menuStyle) {
			menuPanel.initialise(imageOperator, displayPanel);
		} else {
			imageButtonPanel.initialise(imageOperator);
		}

		addObservability();
	}

}