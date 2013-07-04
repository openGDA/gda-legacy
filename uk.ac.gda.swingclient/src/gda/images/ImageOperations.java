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

package gda.images;

import java.awt.image.BufferedImage;

/**
 * ImageOperations interface
 */
public interface ImageOperations {
	/**
	 * @param image
	 */
	public void display(BufferedImage image);

	/**
	 * @param file
	 */
	public void readAndDisplay(String file);

	/**
	 * @param userFile
	 */
	public void save(String userFile);

	/**
	 * @param file
	 */
	public void saveAndExit(String file);

	/**
	 * @param file
	 * @return String
	 */
	public String selectAndReadAndDisplay(String file);

	/**
	 * @param userFile
	 * @return String
	 */
	public String selectAndSave(String userFile);

	/**
	 * 
	 */
	public void updateImage();

	/**
	 * @param requestingObject
	 */
	public void exit(Object requestingObject);
}
