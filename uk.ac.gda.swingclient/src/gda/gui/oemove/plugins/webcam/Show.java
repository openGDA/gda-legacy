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

package gda.gui.oemove.plugins.webcam;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Show Class
 */
public class Show extends JPanel implements PropertyChangeListener {
	private static final Logger logger = LoggerFactory.getLogger(Show.class);

	// this is too slow - must use JMF or custom component for full-rate
	// video

	private JLabel label = new JLabel();

	private JFrame frame = new JFrame();

	private Object lock = new Object();

	/**
	 * 
	 */
	public Show() {
		add(label);
	}

	/**
	 * 
	 */
	public void join() {
		synchronized (lock) {
			try {
				lock.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent e) {
		// too slow for more than one-shot
		byte[] buffer = (byte[]) e.getNewValue();
		logger.debug("got " + buffer.length + " bytes");
		// if (!done)
		// {
		ImageIcon icon = new ImageIcon(buffer);
		setImage(icon);
		// done = true;
		// }
	}

	/**
	 * @param image
	 */
	public void setImage(final ImageIcon image) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				label.setIcon(image);
				frame.pack();
			}
		});
	}

}
