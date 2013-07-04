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

import gda.configuration.properties.LocalProperties;
import gda.images.GUI.ImagePanel;

import javax.swing.JFrame;

import junit.framework.TestCase;

/**
 * ImagePanelTest Class
 */
public class ImagePanelTest extends TestCase {
	JFrame frame;

	String mappingFile = LocalProperties.get("gda.objectserver.mapping");

	String xmlFile = LocalProperties.get("gda.image.xml");

	/**
	 * Constructor
	 */
	public ImagePanelTest() {
		frame = new JFrame();
		frame.setSize(800, 800);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		frame = new JFrame();
		frame.setSize(800, 800);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		frame.dispose();
	}

	/**
	 * 
	 */
	public void testSetUp() {

	}

	/**
	 * Class under test for void ImagePanel()
	 */
	public void testImagePanel() {
		ImagePanel imagePanel = new ImagePanel();
		assertNotNull(imagePanel);
	}

	/**
	 * Class under test for void ImagePanel(boolean)
	 */
	public void testImagePanelboolean() {
	}

	/**
	 * Class under test for void ImagePanel(String, boolean)
	 */
	public void testImagePanelStringbooleanTrue() {
		// ImagePanel imagePanel = new ImagePanel("someString", true);
		// assertNotNull(imagePanel);
		//
		// imagePanel = new ImagePanel("someOtherString", false);
		// assertNotNull(imagePanel);
		//
		// imagePanel = new ImagePanel("", true);
		// assertNotNull(imagePanel);
		//
		// imagePanel = new ImagePanel("", false);
		// assertNotNull(imagePanel);
	}

	/**
	 * 
	 */
	public void testUpdateImage() {
		// ImagePanel imagePanel = new ImagePanel(true, true);
		// frame.getContentPane().add(imagePanel);
		// imagePanel.initialise(false);
		// frame.setVisible(true);
		// imagePanel.updateImage();
		// Sleep.sleep(2000);
	}

}