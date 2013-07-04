/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

package gda.gui.beans;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.scannable.ScannablePositionChangeEvent;
import gda.observable.IObserver;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * ScannableSlider derives from JSlider to control a Scannable and update the slider as the scannable updates
 * backLightBrightnessSlider = new ScannableSlider(backlightBrightness, SwingConstants.HORIZONTAL, 0, 100, 100);
 * backLightBrightnessSlider.setToolTipText("Adjust brightness");
 * backLightBrightnessSlider.setBorder(BorderFactory.createTitledBorder("Brightness (%)"));
 * backLightBrightnessSlider.setMajorTickSpacing(25); 
 * backLightBrightnessSlider.setMinorTickSpacing(5);
 * backLightBrightnessSlider.setPaintTicks(true); 
 * backLightBrightnessSlider.setPaintLabels(true);
 */
public class ScannableSlider extends JSlider {
	private static final Logger logger = LoggerFactory.getLogger(ScannableSlider.class);

	private int lastPerCentVal;
	private volatile Boolean valBeingChanged = false;

	public ScannableSlider(final Scannable scannable, int orientation, int min, int max, int val) {
		super(orientation, min, max, val);
		try {
			setValue(((Double) scannable.getPosition()).intValue());
		} catch (DeviceException e1) {
			logger.error("Error reading position from " + scannable.getName(), e1);
		}
		addChangeListener(new ChangeListener() {
			ExecutorService newFixedThread = Executors.newFixedThreadPool(1);

			@Override
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider) e.getSource();
				final int percentToSet = source.getValue();
				synchronized (valBeingChanged) {
					if (lastPerCentVal == percentToSet || valBeingChanged)
						return;
				}

				newFixedThread.execute(new Runnable() {

					@Override
					public void run() {
						try {
							synchronized (valBeingChanged) {
								// we want to reduce the number of calls to asynchronousMoveTo
								valBeingChanged = true;
								int valFromSlider, valSent = -1;
								while (valSent != (valFromSlider = getValue())) {
									// logger.info("Set value to {}", valFromSlider);
									scannable.asynchronousMoveTo(valFromSlider);
									valSent = valFromSlider;
									try {
										Thread.sleep(100);
									} catch (InterruptedException e) {
										// do nothing
									}
								}
								valBeingChanged = false;
							}
							lastPerCentVal = percentToSet;
						} catch (DeviceException e) {
							logger.error("Error adjusting brightness", e);
						}
					}

				});
			}
		});
		scannable.addIObserver(new IObserver() {

			@Override
			public void update(Object source, Object arg) {
				if (arg instanceof ScannablePositionChangeEvent) {
					if (valBeingChanged || getValueIsAdjusting())
						return;
					final Double newPos = (Double) ((ScannablePositionChangeEvent) arg).newPosition;
					lastPerCentVal = newPos.intValue();
					SwingUtilities.invokeLater(new Runnable() {

						@Override
						public void run() {
							// should only update the GUI
							setValue(lastPerCentVal);
						}
					});
				}

			}
		});
	}
}
