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

package gda.gui.oemove.plugins;

import gda.factory.Configurable;
import gda.factory.FactoryException;
import gda.factory.Findable;
import gda.gui.oemove.Pluggable;
import gda.gui.oemove.plugins.webcam.Connection;
import gda.gui.oemove.plugins.webcam.Show;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * WebCamViewer Class
 */
public class WebCamViewer implements Pluggable, Findable, Configurable {
	private static final Logger logger = LoggerFactory.getLogger(WebCamViewer.class);

	String video = "http://172.23.5.58/axis-cgi/mjpg/video.cgi?camera=&resolution=704x576";

	private boolean started = false;

	private JButton startButton = new JButton();

	private Connection connection = null;

	private Show show;

	private String name;

	private JLabel label = new JLabel();

	private String url;

	private JPanel displayComponent;

	private JPanel controlComponent;

	/**
	 * Constructor
	 */
	public WebCamViewer() {
	}

	@Override
	public void configure() throws FactoryException {
		createDisplayComponent();
		createControlComponent();
	}

	@Override
	public JComponent getDisplayComponent() {
		return displayComponent;
	}

	private void createDisplayComponent() {
		try {
			displayComponent = new JPanel();
			connection = new Connection(video);
			displayComponent.add(label);

			show = new Show();
			connection.addPropertyChangeListener("image", show);
			displayComponent.setLayout(new BorderLayout());
			displayComponent.add(show);
		} catch (Exception e) {
			logger.error("WebCamViewer exception " + e);
		}
	}

	@Override
	public JComponent getControlComponent() {
		return controlComponent;
	}

	private void createControlComponent() {
		controlComponent = new JPanel();
		controlComponent.setLayout(new FlowLayout());

		setButtonText();
		startButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				doAction();
			}
		});
		controlComponent.add(startButton);
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	/**
	 * @return Returns the url.
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * @param url
	 *            The url to set.
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	private void doAction() {
		if (started) {
			started = false;
		} else {
			started = !started;
			connection.go();
			// show.join();
		}
		setButtonText();
	}

	private void setButtonText() {
		String text = (started) ? "Stop" : "Start";
		startButton.setText(text);
	}

}
