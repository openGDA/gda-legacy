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

package gda.oe.positioners;

import javax.swing.JFrame;

/**
 * A stand alone GUI program for writing objects to a binary file. Currently for 3 double values only and for manual
 * editing of filePath, fileName and said values.
 * 
 * @see PositionalValues The Object to be stored
 * @see FileWriterGuiPanel The work horse panel
 */
public class FileWriterGui extends JFrame {
	/**
	 * Constructor.
	 */
	public FileWriterGui() {
		super("File Writer Gui");
		FileWriterGuiPanel fileWriterGuiPanel = new FileWriterGuiPanel();
		getContentPane().add(fileWriterGuiPanel);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(500, 300);
		setVisible(true);
	}

	/**
	 * @param args
	 */
	@SuppressWarnings("unused")
	public static void main(String[] args) {
		new FileWriterGui();
	}

}
