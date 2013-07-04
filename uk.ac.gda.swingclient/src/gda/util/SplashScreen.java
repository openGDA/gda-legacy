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

package gda.util;

import gda.configuration.properties.LocalProperties;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An application class which displays a splash screen logo image, which is displayed in the centre of the screen.
 */
public class SplashScreen extends JWindow {
	private static final Logger logger = LoggerFactory.getLogger(SplashScreen.class);

	private JPanel mainPanel;
	
	private JLabel splashVersionNumber;

	private JLabel splashImage;

	private boolean foundImage = false;

	/**
	 * Build a splash screen and display it in sensible location on screen.
	 */
	public SplashScreen() {
		foundImage = false;
		URL url = getClass().getResource("GDALogomed.png");
		if (url == null) {
			logger.debug("SplashScreen: SplashScreen(), Logo image file does not exist");
		} else {
			foundImage = true;
			createMainPanel();
			addSplashImage(url);
			addVersionString();
			pack();
			arrangeSplashOnScreen();
		}
	}

	private void createMainPanel() {
		mainPanel = new JPanel();
		mainPanel.setOpaque(true);
		mainPanel.setBackground(Color.WHITE);
		mainPanel.setBorder(new LineBorder(Color.GRAY));
		mainPanel.setLayout(new BorderLayout());
		add(mainPanel);
	}
	
	/**
	 * Add logo graphic image to window
	 * 
	 * @param url
	 *            image location
	 */
	private void addSplashImage(URL url) {
		ImageIcon image = new ImageIcon(url);
		splashImage = new JLabel(image);

		splashImage.setSize(splashImage.getIcon().getIconWidth(), splashImage.getIcon().getIconHeight());

		mainPanel.add(splashImage, BorderLayout.CENTER);
	}

	/**
	 * Add current version string to splash screen window
	 */
	private void addVersionString() {
		splashVersionNumber = new JLabel(Version.getRelease(), SwingConstants.CENTER);
		mainPanel.add(splashVersionNumber, BorderLayout.SOUTH);
	}

	/**
	 * Place splash in centre of screen at its correct size
	 */
	private void arrangeSplashOnScreen() {
		// Make window same size as logo image with room for version string
		int width = Math.max(splashImage.getWidth(), splashVersionNumber.getWidth()) + 70;
		int height = splashImage.getHeight() + splashVersionNumber.getHeight() + 70;

		setSize(width, height);
		MultiScreenSupport mss = new MultiScreenSupport();
		int xLocation = 0;
		int yLocation = 0;
		int x = 0;
		int y = 0;
		int index;
		if ((LocalProperties.get("gda.screen.primary")) != null) {
			// screen index for the primary screen starting from 0.
			index = LocalProperties.getInt("gda.screen.primary", 0);
			// mss.setPrimaryScreen(index);
			x = mss.getScreenXoffset(index);
			y = mss.getScreenYoffset(index);
			xLocation = x + mss.getScreenWidth(index) / 2;
			yLocation = y + mss.getScreenHeight(index) / 2;
		} else {
			int nosOfScreens = LocalProperties.getInt("gda.gui.nosOfScreens", 1);
			String displayingScreen = LocalProperties.get("gda.gui.displayingScreen", "top");
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			xLocation = screenSize.width / 2;
			yLocation = screenSize.height / 2;
			if (nosOfScreens == 2) {
				if ("bottom".equalsIgnoreCase(displayingScreen)) {
					xLocation = screenSize.width / 2;
					yLocation = (screenSize.height * 3) / 4;
				} else if ("top".equalsIgnoreCase(displayingScreen)) {
					xLocation = screenSize.width / 2;
					yLocation = screenSize.height / 4;
				} else if ("right".equalsIgnoreCase(displayingScreen)) {
					xLocation = (screenSize.width * 3) / 4;
					yLocation = screenSize.height / 2;
				} else if ("left".equalsIgnoreCase(displayingScreen)) {
					xLocation = screenSize.width / 4;
					yLocation = screenSize.height / 2;
				}
			} else if (nosOfScreens == 4) {
				if ("bottomleft".equalsIgnoreCase(displayingScreen)) {
					xLocation = screenSize.width / 4;
					yLocation = (screenSize.height * 3) / 4;
				} else if ("topleft".equalsIgnoreCase(displayingScreen)) {
					xLocation = screenSize.width / 4;
					yLocation = screenSize.height / 4;
				} else if ("bottomright".equalsIgnoreCase(displayingScreen)) {
					xLocation = (screenSize.width * 3) / 4;
					yLocation = (screenSize.height * 3) / 4;
				} else if ("topright".equalsIgnoreCase(displayingScreen)) {
					xLocation = (screenSize.width * 3) / 4;
					yLocation = screenSize.height / 4;
				}
			}
		}
		// Place logo in centre of displaying screen
		int posx = xLocation - (width / 2);
		int posy = yLocation - (height / 2);

		setLocation(posx, posy);
	}

	/**
	 * Display splash screen logo window and move window to front
	 */
	public void showSplash() {
		if (foundImage) {
			setVisible(true);
			toFront();
		}
	}

	/**
	 * Hide splash screen logo window
	 */
	public void hideSplash() {
		if (foundImage) {
			setVisible(false);
		}
	}

	/**
	 * Force an immediate paint. The SplashScreen itself is a JWindow and so has no paintImmediately. However since it
	 * has no decorations it should be safe just to paintImmediately its contents. These methods should only be called
	 * from within the EventDispatchThread hence the 'if' although it is not really necessary since we know that this
	 * method is only called once, from an invokeAndWait, in AcquisitionGUI's main.
	 */
	public void paintImmediately() {
		if (SwingUtilities.isEventDispatchThread()) {
			splashImage.paintImmediately(splashImage.getBounds());
			splashVersionNumber.paintImmediately(splashVersionNumber.getBounds());
		}
	}
}
