/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package gda.testlib;

import gda.gui.AcquisitionFrame;
import gda.util.SplashScreen;
import gda.util.Version;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.WindowConstants;

/**
 * UI parts of GDASetup in core
 * 
 * @author rjw82
 * 
 */
public class GDASetupUI extends GDASetup {

	private static final GDASetupUI gdaSetupObj = new GDASetupUI();

	// for starting GUI
	private static AcquisitionFrame acquisitionFrame;

	private static SplashScreen splashScreen;

	private static Thread guiThread;

	protected GDASetupUI() {

	}

	/**
	 * Starts Simulator GUI on local machine running in own thread. Call tearDownAll() to halt it from tearDown() method
	 * 
	 * @throws Exception
	 */
	public void setUpGUI() throws Exception {
		// now start the gui using code from main in AcquisitionGUI
		guiThread = uk.ac.gda.util.ThreadManager.getThread(new Runnable() {
			@Override
			public void run() {
				// Display logo
				splashScreen = new SplashScreen();
				splashScreen.showSplash();

				String guiTitle = "GDA AcquisitionGUI Version " + Version.getRelease();

				acquisitionFrame = new AcquisitionFrame(guiTitle, null);
				acquisitionFrame.setSize();
				acquisitionFrame.configure(false);
				acquisitionFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
				acquisitionFrame.addWindowListener(new WindowAdapter() {
					@Override
					public void windowClosing(WindowEvent ev) {
						acquisitionFrame.confirmExit();
					}
				});

				// Hide logo
				splashScreen.hideSplash();

				acquisitionFrame.setVisible(true);
			}
		});

		guiThread.start();

		Thread.sleep(1000);
		if (guiThread == null) {
			throw new Exception("GDA GUI has not started (null)");
		}
	}

	/**
	 * method to deliver the single instance of this object to external classes.
	 * 
	 * @return the standard GDA test setup instance
	 */
	public static GDASetupUI getInstance() {
		return (gdaSetupObj);
	}

}
