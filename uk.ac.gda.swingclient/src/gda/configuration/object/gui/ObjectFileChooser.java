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

package gda.configuration.object.gui;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * dialog to pick a file name accepts array of extension strings eg ".xml" and description
 */
@SuppressWarnings("serial")
public class ObjectFileChooser extends JFileChooser {
	private static final Logger logger = LoggerFactory.getLogger(ObjectFileChooser.class);

	JFrame frame = null;

	ObjectFileChooser(JFrame frame, final String[] _extensions, final String _extensionDescription) {
		this.frame = frame;

		setFileFilter(new FileFilter() {

			String[] extensions = _extensions;

			String extensionDescription = _extensionDescription;

			@Override
			public boolean accept(File f) {
				if (f.isDirectory()) {
					return true;
				}

				for (int i = 0; i < extensions.length; i++) {
					// see if filename ends with extension - compare in
					// lower case
					if (f.getName().toLowerCase().endsWith(extensions[i].toLowerCase())) {
						return true;
					}
				}

				return false;
			}

			@Override
			public String getDescription() {
				return extensionDescription;
			}
		});
	}

	/**
	 * @return the absolute path of filename
	 */
	public String chooseFileName() {
		int returnVal = showOpenDialog(frame);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = getSelectedFile();

			logger.info("Opening file " + file.getAbsolutePath());
			return file.getAbsolutePath();
		}
		logger.info("Open command cancelled by user.");
		return null;
	}

}
