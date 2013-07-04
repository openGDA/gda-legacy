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
import gda.factory.Findable;
import gda.factory.Finder;
import gda.factory.ObjectFactory;
import gda.factory.XmlObjectCreator;

import java.util.List;

import javax.swing.JFrame;

import junit.framework.TestCase;
import org.junit.Ignore;

/**
 * SampleImagePanelTest Class
 */
@SuppressWarnings("deprecation")
@Ignore("2010/01/20 Test ignored since not passing in Hudson")
public class SampleImagePanelTest extends TestCase {
	JFrame frame;

	String xmlFile = LocalProperties.get("gda.image.xml");

	List<Findable> findables = null;

	ObjectFactory factory = null;

	Finder finder = null;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		xmlFile = LocalProperties.get("gda.objectserver.xml");
		xmlFile = xmlFile.replace('\\', '/');

		XmlObjectCreator oc = new XmlObjectCreator();
		oc.setXmlFile(xmlFile);
		factory = oc.getFactory();
		findables = factory.getFindables();
		// add the object factory the the finder
		finder = Finder.getInstance();
		finder.addFactory(factory);

		frame = new JFrame();
		frame.setSize(800, 800);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	/*
	 * @see TestCase#tearDown()
	 */
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
	 * Class under test for void SampleImagePanel(boolean)
	 */
	public void testSampleImagePanelboolean() {
		// SampleImagePanel imagePanel = new SampleImagePanel();
		// assertNotNull(imagePanel);
	}
}
