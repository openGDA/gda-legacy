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

package gda.images.GUI;

import gda.configuration.properties.LocalProperties;
import gda.images.ImageOperator;
import gda.observable.IObservableJPanel;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;

/**
 * ImageButtonPanel class, a panel of button to allow user operations on displayed image Java Properties:
 * gda.images.enableQuit : boolean; optional; default false; flag to enable a QUIT button to exit application
 */
@SuppressWarnings("serial")
public class ImageButtonPanel extends IObservableJPanel implements ActionListener {
	/**
	 * update button
	 */
	public JButton updateButton;

	/**
	 * read button
	 */
	public JButton readButton;

	/**
	 * save button
	 */
	public JButton saveButton;

	/**
	 * quit button
	 */
	public JButton quitButton;

	protected ImageOperator operator;

	protected String userFile;

	protected final static String TMP_FILE = System.getProperty("user.dir") + File.separator + "tmp.bmp";

	protected boolean toolTips;

	protected boolean enableQuit;

	/**
	 * Constructor
	 * 
	 * @param userFile
	 * @param toolTips
	 */
	public ImageButtonPanel(String userFile, boolean toolTips) {
		this.userFile = userFile;
		this.toolTips = toolTips;

		configure();
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Image ", TitledBorder.LEFT,
				TitledBorder.TOP, null, Color.black));

		c.weightx = 0.2;
		c.gridy = 0;
		c.gridx = GridBagConstraints.RELATIVE;
		c.insets = new Insets(2, 5, 2, 5);
		c.ipadx = 10;
		c.anchor = GridBagConstraints.WEST;

		createButtons();
		add(readButton, c);
		add(saveButton, c);
		add(updateButton, c);
		add(quitButton, c);

		setActive(false);
	}

	protected void configure() {
		enableQuit = (LocalProperties.get("gda.images.enableQUIT", "false")).equalsIgnoreCase("true");
	}

	protected void createButtons() {
		updateButton = new JButton("Update");
		updateButton.setActionCommand("update");
		updateButton.addActionListener(this);

		readButton = new JButton("Read");
		readButton.setActionCommand("read");
		readButton.addActionListener(this);

		saveButton = new JButton("Save");
		saveButton.setActionCommand("save");
		saveButton.addActionListener(this);

		quitButton = new JButton("Quit");
		quitButton.setActionCommand("exit");
		quitButton.addActionListener(this);
	}

	/**
	 * @param operator
	 */
	public void initialise(ImageOperator operator) {
		this.operator = operator;
		if (toolTips) {
			setToolTips();
		}

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
				readButton.setEnabled(state);
				updateButton.setEnabled(state);
				saveButton.setEnabled(state);
				if (enableQuit)
					quitButton.setEnabled(state);
			}
		});
	}

	protected void setToolTips() {
		readButton.setToolTipText("Read image from user selected file");
		saveButton.setToolTipText("Save image to user selected file ");
		updateButton.setToolTipText("update image from file " + operator.getUserFile());
		if (enableQuit)
			quitButton.setToolTipText("exit application");
	}

	// ActionListener

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("read")) {
			String selection = operator.selectAndReadAndDisplay(userFile);
			if (selection != null)
				userFile = selection;
		} else if (e.getActionCommand().equals("update")) {
			operator.readAndDisplay(userFile);
			updateButton.setEnabled(true);
		} else if (e.getActionCommand().equals("save")) {
			String selection = operator.selectAndSave(userFile);
			if (selection != null)
				userFile = selection;
		} else if (e.getActionCommand().equals("exit")) {
			operator.exit(this);
		}
	}
}