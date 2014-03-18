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
import gda.gui.ClientSideLogService;
import gda.gui.GUIMessagePanel;
import gda.icons.GdaIcons;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;

/**
 * LogPanel Class
 * 
 * <p>This is the main entry point when launching a standalone log panel (for example when using {@code gda logpanel}).
 */
public class LogPanel extends GUIMessagePanel {
	private static final Logger logger = LoggerFactory.getLogger(LogPanel.class);
	
	public LogPanel() {
		//setLabel("Messages");
	}
	
	private JPanel panel;

	@Override
	public void configure() {
		setLayout(new BorderLayout());

		panel = LogPanelAppender.getPanel();

		if (panel != null) {
			add(panel, BorderLayout.CENTER);
		}

	}

	private static int clientSideLogServicePort = 6001;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {

				String logConfig;
				LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();

				JoranConfigurator configurator = new JoranConfigurator();
				lc.reset();
				configurator.setContext(lc);

				try {
					if ((logConfig = LocalProperties.get("gda.logPanel.logging.xml")) != null) {
						configurator.doConfigure(logConfig);
					}
				} catch (JoranException e) {
					e.printStackTrace();
				}
				String port = null;
				if ((port = System.getProperty("gda.logPanel.logging.port")) != null) {
					logger.info("Using system properties port number {}", port);
					clientSideLogServicePort = Integer.parseInt(port);
				} else {
					clientSideLogServicePort = LocalProperties.getInt(
							"gda.logPanel.logging.port",
							clientSideLogServicePort);
					logger.warn("Please wait until 'Server initialisation complete' before you start GDA client");
					logger.info("Using local properties port number {}",
							clientSideLogServicePort);
				}
				Thread logServer = uk.ac.gda.util.ThreadManager.getThread(new ClientSideLogService(
						clientSideLogServicePort, lc), "LogServer");
				logServer.start();

				// Turn off metal's use of bold fonts
				UIManager.put("swing.boldMetal", Boolean.FALSE);
				
				LogPanel panel = new LogPanel();
				panel.configure();
				panel.setBorder(new EmptyBorder(5, 0, 0, 0));
				
				JPanel panel1 = new JPanel();
				panel1.setLayout(new java.awt.GridLayout());
				GridBagConstraints c = new GridBagConstraints();
				c.gridx = c.gridy = 0;
				c.fill = GridBagConstraints.BOTH;
				panel1.add(panel, c);
				
				final JCheckBox scrollLockButton = new JCheckBox("Scroll Lock");
				scrollLockButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						final boolean scrollLockOn = scrollLockButton.isSelected();
						SingleLogPanelAppender.setScrollingEnabled(!scrollLockOn);
					}
				});
				
				JButton clearButton = new JButton("Clear");
				clearButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						LogPanelAppender.clearPanel();
					}
				});
				
				JPanel buttonPanel = new JPanel();
				buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
				buttonPanel.add(scrollLockButton);
				buttonPanel.add(clearButton);
				
				JFrame frame = new JFrame();
				frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
				frame.add(panel1, BorderLayout.CENTER);
				frame.add(buttonPanel, BorderLayout.SOUTH);
				frame.setSize(1000, 500);
				frame.setTitle("GDA Log Panel");
				frame.setIconImage(GdaIcons.getWindowIcon());
				frame.setVisible(true);
			}
		});
	}
}
