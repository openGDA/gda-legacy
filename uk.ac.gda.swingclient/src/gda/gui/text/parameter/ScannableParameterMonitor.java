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

package gda.gui.text.parameter;

import gda.device.Scannable;
import gda.device.ScannableMotion;
import gda.device.ScannableMotionUnits;
import gda.device.scannable.ScannablePositionChangeEvent;
import gda.device.scannable.ScannableStatus;
import gda.observable.IObserver;

import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * class to update the parameter value field of a ParametersPanelBuilder with dof value
 */
public class ScannableParameterMonitor implements IObserver {
	
	private static final Logger logger = LoggerFactory.getLogger(ScannableParameterMonitor.class);
	
	final private Scannable scannable;

	final private ParametersPanelBuilder builder;

	final private long limitedId;

	private volatile boolean monitorThreadRunning = false;

	private boolean isScannableMotion = false;
	/**
	 * @param scannable
	 * @param builder
	 * @param limitedId
	 * @param isScannableMotion 
	 */
	private ScannableParameterMonitor(Scannable scannable, ParametersPanelBuilder builder, long limitedId, boolean isScannableMotion, boolean autoStart) {
		this.builder = builder;
		this.limitedId = limitedId;
		this.scannable = scannable;
		this.isScannableMotion = isScannableMotion;
		
		if (autoStart) {
			start();
		}
	}
	
	public void start() {
		builder.setLoseFocusAfterValueChange(true);
		
		scannable.addIObserver(this);
		updateDisplay();
	}
	
	public ScannableParameterMonitor(Scannable scannable, ParametersPanelBuilder builder, long limitedId, boolean isScannableMotion) {
		this(scannable, builder, limitedId, isScannableMotion, true);
	}
	
	/**
	 * Creates a {@link ScannableParameterMonitor} but does not automatically start it; calling code must do this by calling {@link #start()}.
	 */
	public static ScannableParameterMonitor create(Scannable scannable, ParametersPanelBuilder builder, long limitedId, boolean isScannableMotion) {
		return new ScannableParameterMonitor(scannable, builder, limitedId, isScannableMotion, false);
	}
	
	ScannableData getScannableData() throws Exception{
		ScannableData data = new ScannableData();
		data.pos = scannable.getPosition();
		data.isBusy = scannable.isBusy();
		if( isScannableMotion ){
			data.units = scannable.getAttribute(ScannableMotionUnits.USERUNITS).toString();
			Double [] limits = (Double[]) scannable.getAttribute(ScannableMotion.FIRSTINPUTLIMITS);
			data.min = limits != null &&  limits[0] != null ? limits[0] : -Double.MAX_VALUE;
			data.max = limits != null &&  limits[1] != null ? limits[1] : Double.MAX_VALUE;
		}
		return data;
	}
	
	void updateDisplay() {
		final ScannableData data;
		try {
			data = getScannableData();
			SwingUtilities.invokeLater(new Runnable(){
				@Override
				public void run(){
					ScannableParameterMonitor.this.updateDisplayInSwingThread(data);
				}
			});		
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	@Override
	public void update(Object theObserved, final Object changeCode) {
		if( changeCode instanceof ScannableStatus){
			updateDisplay();
		} else if( changeCode instanceof ScannablePositionChangeEvent){
			try {
				SwingUtilities.invokeLater(new Runnable(){
					@Override
					public void run(){
						builder.setParameterConnectedState(limitedId, true);
						builder.setParameterFromMonitor(limitedId, 
								((ScannablePositionChangeEvent)changeCode).newPosition);
					}
				});		
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
	}
	
	void updateDisplayInSwingThread(ScannableData data) {
		builder.setParameterConnectedState(limitedId, true);
		builder.setParameterFromMonitor(limitedId, data.pos);
		builder.setForeground(limitedId, data.isBusy ? java.awt.Color.MAGENTA : java.awt.Color.BLACK);
		builder.setEnabled(limitedId, !data.isBusy);
		if( isScannableMotion){
			builder.setUnits(limitedId, data.units);
			builder.setLimits(limitedId, data.min, data.max);
		}
		if (data.isBusy && !monitorThreadRunning ) {
			monitorThreadRunning = true;
			uk.ac.gda.util.ThreadManager.getThread(new Runnable() {
				@Override
				public void run() {
					try {
						Thread.sleep(1000);
						updateDisplay();
					} catch (Exception e) {
						logger.error(e.getMessage(),e);
					}
					monitorThreadRunning = false;
				}
			}).start();
		}
	}
}

class ScannableData{
	boolean isBusy;
	Object pos;
	String units;
	double min;
	double max;
	
	@Override
	public String toString() {
		return String.format("%s(isBusy=%s, pos=%s, units=%s, min=%.2f, max=%.2f)", getClass().getSimpleName(), isBusy, pos, units, min, max);
	}

}