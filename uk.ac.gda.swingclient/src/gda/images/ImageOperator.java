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

import gda.images.GUI.ImageDisplayPanel;
import gda.images.GUI.ImagePanel;

import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ImageOperator Class
 */
public class ImageOperator implements ImageOperations {
	
	private static final Logger logger = LoggerFactory.getLogger(ImageOperator.class);
	
	protected BufferedImage image = null;

	protected ImagePanel imagePanel;

	protected ImageDisplayPanel displayPanel;

	protected String userFile = null;

	/**
	 * @param imagePanel
	 * @param imageFile
	 */
	public ImageOperator(ImagePanel imagePanel, String imageFile) {
		this.userFile = imageFile;
		this.imagePanel = imagePanel;
		displayPanel = imagePanel.getDisplayPanel();
		configure();
	}

	protected void configure() {
		// intentionally empty, override in children
	}

	@Override
	public void display(BufferedImage image) {
		displayPanel.displayImage(image);
	}

	/**
	 * @return user file
	 */
	public String getUserFile() {
		return userFile;
	}

	@Override
	public String selectAndReadAndDisplay(String file) {
		String selection = null;

		selection = ImageFileHandler.getOpenFileSelection(displayPanel, file);
		if (selection != null) {
			readAndDisplay(selection);
			userFile = selection;
		}

		return selection;
	}

	@Override
	public void readAndDisplay(String file) {
		try {
			BufferedImage image = ImageFileHandler.read(file);
			if (image != null)
				displayPanel.displayImage(image);
		} catch (FileNotFoundException e) {
			logger.debug(e.getStackTrace().toString());
		} catch (IOException e) {
			logger.debug(e.getStackTrace().toString());
		}
	}

	@Override
	public void save(String userFile) {
		BufferedImage image = displayPanel.getDisplayedImage();
		if (image != null) {
			ImageFileHandler.save(image, userFile);
		}
	}

	@Override
	public String selectAndSave(String userFile) {
		String selection = null;
		selection = ImageFileHandler.getSaveFileSelection(displayPanel, userFile);
		if (selection != null) {
			save(selection);
			userFile = selection;
		}
		return selection;
	}

	@Override
	public void saveAndExit(String file) {
		save(file);
		exit(this);
	}

	// this method is needed to do generic update of image from file, it is
	// overridden in
	// children requiring update from camera
	@Override
	public void updateImage() {
		readAndDisplay(userFile);
	}

	@Override
	public void exit(Object requestingObject) {
		System.exit(0);
	}

	/**
	 * @param file
	 */
	public void setUserFile(String file) {
		userFile = file;
	}
}
