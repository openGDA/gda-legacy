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

import gda.images.ImageOperator;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;

/**
 * A generic pull down file menu which is added to a menu bar and does file type operations on an image This class calls
 * methods in interface FileMenuInterface and needs a command operator class (currently ImageFileHandler) which
 * implements this interface.
 */
@SuppressWarnings("serial")
public class FileMenu extends JMenu implements ActionListener {
	private JMenuItem readMenu;

	private JMenuItem saveMenu;

	private JMenuItem saveAsMenu;

	private JMenuItem saveAndExitMenu;

	private JMenuItem exitMenu;

	// private Object commandHandler = null;

	private static final String SAVE_AS = "saveAs";

	private static final String SELECT_AND_READ = "selectAndRead";

	private static final String SAVE = "save";

	private static final String SAVE_AND_EXIT = "saveAndExit";

	private static final String EXIT = "exit";

	// private boolean toolTips;
	private ImageOperator operator;

	private String userFile;

	/**
	 * @param userFile
	 */
	public FileMenu(String userFile) {
		this(userFile, true);
	}

	/**
	 * @param userFile
	 * @param toolTips
	 */
	public FileMenu(String userFile, boolean toolTips) {
		super("File");
		this.userFile = userFile;
		// this.toolTips = toolTips;

		readMenu = new JMenuItem("Read...");
		readMenu.addActionListener(this);
		readMenu.setActionCommand(SELECT_AND_READ);
		this.add(readMenu);

		saveMenu = new JMenuItem("Save");
		saveMenu.addActionListener(this);
		saveMenu.setActionCommand(SAVE);
		this.add(saveMenu);

		saveAsMenu = new JMenuItem("Save As...");
		saveAsMenu.addActionListener(this);
		saveAsMenu.setActionCommand(SAVE_AS);
		this.add(saveAsMenu);

		saveAndExitMenu = new JMenuItem("Save & Exit");
		saveAndExitMenu.addActionListener(this);
		saveAndExitMenu.setActionCommand(SAVE_AND_EXIT);
		this.add(saveAndExitMenu);

		exitMenu = new JMenuItem("Exit");
		exitMenu.setActionCommand(EXIT);
		exitMenu.addActionListener(this);
		this.add(exitMenu);

		if (toolTips) {
			this.setToolTipText("display file menu options");
			readMenu.setToolTipText("read image from selected file");
			saveMenu.setToolTipText("save image to file");
			saveAsMenu.setToolTipText("save image to selected file");
			saveAndExitMenu.setToolTipText("save image to file and exit application");
			exitMenu.setToolTipText("exit application");
		}
	}

	/**
	 * @param operator
	 */
	public void initialise(ImageOperator operator) {
		this.operator = operator;
	}

	/**
	 * set all File menu items to be enabled or disabled according tp value of param b
	 * 
	 * @param b
	 *            determines whether menu items enabled (b = true) or disabled (b = false)
	 */
	public void setActive(boolean b) {
		final boolean sensitive = b;
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				readMenu.setEnabled(sensitive);
				saveMenu.setEnabled(sensitive);
				saveAsMenu.setEnabled(sensitive);
				saveAndExitMenu.setEnabled(sensitive);
				exitMenu.setEnabled(sensitive);
			}
		});
	}

	// ActionListener interface
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals(SELECT_AND_READ)) {
			String selection = operator.selectAndReadAndDisplay(userFile);
			if (selection != null) {
				userFile = selection;
			}
		} else if (e.getActionCommand().equals(SAVE)) {
			operator.save(userFile);
		} else if (e.getActionCommand().equals(SAVE_AS)) {
			String selection = operator.selectAndSave(userFile);
			if (selection != null)
				userFile = selection;
		} else if (e.getActionCommand().equals(SAVE_AND_EXIT)) {
			operator.save(userFile);
			operator.exit(this);
		} else if (e.getActionCommand().equals(EXIT)) {
			operator.exit(this);
		}
	}
}
