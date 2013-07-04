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

package gda.gui.beans;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.scannable.ScannablePositionChangeEvent;
import gda.factory.Configurable;
import gda.observable.IObserver;

import java.awt.FlowLayout;

import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.JSpinner.DefaultEditor;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A GUI component for controlling a {@link Scannable} that has a numeric position. Consists of a slider
 * plus a spinner. The scannable's position is only changed when the slider is released; the position is not changed
 * while the slider is being dragged.
 */
public class NumericScannableControl extends JPanel implements Configurable {
	
	private static final Logger logger = LoggerFactory.getLogger(NumericScannableControl.class);

	private JSlider slider;
	private JSpinner spinner;
	
	private Scannable scannable;
	
	/**
	 * Indicates whether {@link ChangeEvent}s from the slider/spinner will be acted upon. This is temporarily set to
	 * {@code false} when we receive an update from the scannable, so that the slider/spinner update does not result in
	 * a call to set the scannable's value.
	 */
	private volatile boolean setScannableValueOnGuiChange = false;
	
	public NumericScannableControl() {
		setLayout(new FlowLayout());
		
		slider = new JSlider(SwingConstants.VERTICAL);
		
		slider.setPaintTicks(true);
		slider.setPaintLabels(true);
		
		spinner = new JSpinner();
		
		add(slider);
		add(spinner);

		slider.setEnabled(false);
		spinner.setEnabled(false);
	}
	
	/**
	 * Sets the {@link Scannable} being controlled.
	 */
	public void setScannable(Scannable s) {
		this.scannable = s;
	}
	
	public void setMinimum(int minimum) {
		slider.setMinimum(minimum);
		SpinnerNumberModel model = (SpinnerNumberModel) spinner.getModel();
		model.setMinimum(minimum);
	}

	public void setMaximum(int maximum) {
		slider.setMaximum(maximum);
		SpinnerNumberModel model = (SpinnerNumberModel) spinner.getModel();
		model.setMaximum(maximum);
	}
	
	/**
	 * Sets the major tick spacing for the slider.
	 * 
	 * @see JSlider#setMajorTickSpacing(int)
	 */
	public void setSliderMajorTickSpacing(int spacing) {
		slider.setMajorTickSpacing(spacing);
	}

	/**
	 * Sets the minor tick spacing for the slider.
	 * 
	 * @see JSlider#setMinorTickSpacing(int)
	 */
	public void setSliderMinorTickSpacing(int spacing) {
		slider.setMinorTickSpacing(spacing);
	}
	
	/**
	 * Sets the number of columns for the spinner's text field.
	 * 
	 * @see JTextField#setColumns(int)
	 */
	public void setSpinnerTextFieldColumns(int columns) {
		DefaultEditor spinnerEditor = (DefaultEditor) spinner.getEditor();
		JTextField spinnerTextField = spinnerEditor.getTextField();
		spinnerTextField.setColumns(columns);
	}
	
	@Override
	public void configure() {
		
		// React to events from the scannable by updating the slider/spinner
		if (scannable != null) {
			scannable.addIObserver(new IObserver() {
				@Override
				public void update(Object theObserved, Object changeCode) {
					if (changeCode instanceof ScannablePositionChangeEvent) {
						ScannablePositionChangeEvent event = (ScannablePositionChangeEvent) changeCode;
						if (event.newPosition instanceof Number) {
							Number value = (Number) event.newPosition;
							updateToShowValue(value.intValue());
						}
					}
				}
			});
		}
		
		slider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				// Do not change the scannable's position while the slider is still being dragged
				if (setScannableValueOnGuiChange && !slider.getValueIsAdjusting()) {
					changeValue(slider.getValue());
				}
			}
		});
		
		spinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if (setScannableValueOnGuiChange) {
					int newValue = (Integer) spinner.getValue();
					changeValue(newValue);
				}
			}
		});
		
		showCurrentValue();
	}
	
	/**
	 * Updates the slider and spinner to show the scannable's current value.
	 */
	private void showCurrentValue() {
		if (scannable != null) {
			try {
				Number currentValue = (Number) scannable.getPosition();
				updateToShowValue(currentValue.intValue());
			} catch (DeviceException e) {
				logger.error("Unable to read initial value of scannable", e);
			}
		}
	}
	
	/**
	 * Enables this control.
	 */
	public void start() {
		spinner.setEnabled(true);
		slider.setEnabled(true);
		setScannableValueOnGuiChange = true;
	}
	
	/**
	 * Sets the value of the {@link Scannable}.
	 */
	private void changeValue(final int newValue) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				
				// Prevent changes to the slider/spinner while changing the scannable's value
				slider.setEnabled(false);
				spinner.setEnabled(false);
				
				new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							
							// Change the scannable's value
							changeScannableValue(newValue);
							
						} catch (DeviceException e) {
							logger.error("Unable to change value of scannable", e);
						} finally {
							SwingUtilities.invokeLater(new Runnable() {
								@Override
								public void run() {
									
									// Enable the slider/spinner again
									slider.setEnabled(true);
									spinner.setEnabled(true);
								}
							});
						}
					}
				}).start();
			}
		});
	}
	
	private void changeScannableValue(int newValue) throws DeviceException {
		// If a scannable has been set, then change its position
		if (scannable != null) {
			scannable.moveTo(newValue);
			// No need to update the GUI - an event will be received from the scannable
			// and the GUI will be updated when that event is received
		}
		
		// Otherwise, mimic the effect of a motor moving by pausing for a second
		else {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// ignore
			}
			// There is no scannable, so we need to update the GUI here as no event will be received
			updateToShowValue(newValue);
		}
	}
	
	/**
	 * Updates the slider and spinner to show a new value.
	 */
	private void updateToShowValue(final int newValue) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				setScannableValueOnGuiChange = false;
				spinner.setValue(newValue);
				slider.setValue(newValue);
				setScannableValueOnGuiChange = true;
			}
		});
	}

}
