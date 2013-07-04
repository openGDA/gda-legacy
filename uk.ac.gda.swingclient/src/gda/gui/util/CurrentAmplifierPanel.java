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

package gda.gui.util;

import gda.device.CurrentAmplifier;
import gda.device.DeviceException;
import gda.device.CurrentAmplifier.Status;
import gda.factory.Configurable;
import gda.factory.FactoryException;
import gda.factory.Findable;
import gda.factory.Finder;
import gda.observable.IObserver;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jidesoft.icons.IconSet;

/**
 * Generates a JPanel to monitor a PV or Scannable.
 * <p>
 * The JPanel will have a border with the Scannable's name and its current value JLabel which will show RED coloured
 * digits when and if its value below certain threshold set by users. To use just add to an existing panel and be sure
 * to configure the scannableName via setScannableName and call configure() after that.
 */

public class CurrentAmplifierPanel extends JPanel implements IObserver, Configurable, Findable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3714529010406127788L;

	private static final Logger logger = LoggerFactory.getLogger(CurrentAmplifierPanel.class);

	private String scannableName;
	private JLabel valueLabel;
	private CurrentAmplifier curramp;

	private double threshold;
	// private Timer timer;
	private boolean configured = false;
	private BeamStatus beamStatus = BeamStatus.ON;
	private ImageIcon beamicon;

	private ImageIcon nobeamicon;

	private ImageIcon overloadicon; 
	/**
	 * defines the overload status of the amplifier
	 * 
	 * @author fy65
	 */
	public enum BeamStatus {
		/**
		 * Ion Chamber Amplifier indicates Beam on
		 */
		ON,
		/**
		 * Ion Chamber Amplifier indicates Beam off
		 */
		OFF,
		/**
		 * Ion Chamber Amplifier indicates measurement overload
		 */
		OVERLOADED;
		/**
		 * converts value to Status
		 * 
		 * @param value
		 * @return status
		 */
		public static BeamStatus from_int(int value) {
			BeamStatus s = BeamStatus.ON;
			switch (value) {
			case 0:
				s = BeamStatus.ON;
				break;
			case 1:
				s = BeamStatus.OFF;
				break;
			case 2:
				s = BeamStatus.OVERLOADED;
				break;
			default:
				s = BeamStatus.ON;
			}
			return s;

		}
	}

	/**
	 * Constructor.
	 */
	public CurrentAmplifierPanel() {
		// timer = new Timer(1000, null);
		beamicon= createImageIcon("beam.png", "   on   ");
		nobeamicon=createImageIcon("nobeam.png", "   off  ");
		overloadicon=createImageIcon("Overload.jpg", "overload");
	}

	@Override
	public void configure() throws FactoryException {
		if (curramp == null) {
			if (scannableName == null)
				return;
		}
		if (!configured) {
			if (curramp == null) {
				if ((curramp = (CurrentAmplifier) Finder.getInstance().find(scannableName)) == null) {
					throw new FactoryException("Cannot find object: " + scannableName);
				}
			}
			setLayout(new FlowLayout());
			double curval = 0.0;
			Status status = Status.NORMAL;
			try {
				curval = curramp.getCurrent();
				status = curramp.getStatus();
			} catch (DeviceException e) {
				logger.warn("Cannot get current value from apmlifier {}", curramp.getName());
			}
			if (curval > getThreshold()) {
				beamStatus = BeamStatus.ON;
				valueLabel = new JLabel("   on   ", beamicon, SwingConstants.CENTER);
				valueLabel.setForeground(Color.green);
			} else {
				beamStatus = BeamStatus.OFF;
				valueLabel = new JLabel("   off  ", nobeamicon, SwingConstants.CENTER);
				valueLabel.setForeground(Color.red);
			}
			if (status == Status.OVERLOAD) {
				beamStatus = BeamStatus.OVERLOADED;
				valueLabel = new JLabel("overload", overloadicon, SwingConstants.CENTER);
				valueLabel.setForeground(Color.orange);
			}
			valueLabel.setOpaque(true);
			valueLabel.setBackground(new Color(239, 239, 239));
			setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "X-Ray", TitledBorder.LEFT,
					TitledBorder.TOP, null, Color.black));
			valueLabel.setPreferredSize(new Dimension(80, 24));
			add(valueLabel);

			curramp.addIObserver(this);
			// timer.addActionListener(new TimerListener(valueLabel));
			configured = true;
		}

	}

	/** Returns an ImageIcon, or null if the path was invalid. */
	protected ImageIcon createImageIcon(String path, String description) {
		java.net.URL imgURL = getClass().getResource(path);
		if (imgURL != null) {
			return new ImageIcon(imgURL, description);
		}
		logger.error("Couldn't find file: {} ", path);
		return null;

	}

	@Override
	public void update(Object theObserved, Object changeCode) {
		double value = -999;
		if (theObserved instanceof CurrentAmplifier) {
			if (changeCode instanceof Double) {
				value = Math.abs(((Double) changeCode).doubleValue());
				if (value <= threshold) {
					if (beamStatus != BeamStatus.OFF) {
						beamStatus = BeamStatus.OFF;
						valueLabel.setText("  off   ");
						valueLabel.setIcon(nobeamicon);
						valueLabel.setForeground(Color.RED);
					}
				} else {
					if (beamStatus != BeamStatus.ON) {
						beamStatus = BeamStatus.ON;
						valueLabel.setText("   on   ");
						valueLabel.setIcon(beamicon);
						valueLabel.setForeground(Color.green);
					}

				}
			} else if (changeCode instanceof CurrentAmplifier.Status) {
				CurrentAmplifier.Status status = (CurrentAmplifier.Status) changeCode;
				if (status == CurrentAmplifier.Status.OVERLOAD) {
					if (beamStatus != BeamStatus.OVERLOADED) {
						beamStatus = BeamStatus.OVERLOADED;
						valueLabel.setText("overload");
						valueLabel.setIcon(overloadicon);
						valueLabel.setForeground(Color.orange);
					}
				}
			}
		}
	}

	/**
	 * @return shutter name
	 */
	public String getScannableName() {
		return scannableName;
	}

	/**
	 * @param scannableName
	 */
	public void setScannableName(String scannableName) {
		this.scannableName = scannableName;
	}

	/**
	 * Gets the Current Amplifier object
	 * 
	 * @return the current amplifier object
	 */
	public CurrentAmplifier getCurramp() {
		return curramp;
	}

	/**
	 * sets the current amplifier object
	 * 
	 * @param curramp
	 */
	public void setCurramp(CurrentAmplifier curramp) {
		this.curramp = curramp;
	}

	/**
	 * gets the threshold
	 * 
	 * @return the threshold
	 */
	public double getThreshold() {
		return threshold;
	}

	/**
	 * sets the threshold
	 * 
	 * @param threshold
	 */
	public void setThreshold(double threshold) {
		this.threshold = threshold;
	}
}
