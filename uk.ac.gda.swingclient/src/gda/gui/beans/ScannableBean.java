/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council
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

import gda.device.Scannable;
import gda.device.scannable.ScannableUtils;
import gda.factory.Finder;
import gda.util.exceptionUtils;

import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class ScannableBean extends BeanBase{

	private static final Logger logger = LoggerFactory.getLogger(ScannableBean.class);

	private Scannable theScannable;
	private String scannableName;
	private int arrayLength;
	
	/**
	 * Constructor.
	 */
	public ScannableBean() {
		super();
		initGUI();
	}
	
	/**
	 * @return name of the scannable
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

	@Override
	public void startDisplay() {
		if (scannableName != null) {
			this.theScannable = (Scannable) Finder.getInstance().find(scannableName);
			if (theScannable != null) {
				arrayLength = theScannable.getInputNames().length + theScannable.getExtraNames().length;
				theScannable.addIObserver(this);
				if (getLabel().equals("Name")) {
					setLabel(scannableName);
					this.getLblLabel().setText(getLabel());
				}
				update(theScannable, null);
			}
		} 
	}

	@Override
	protected boolean theObservableIsChanging() {
		return false;
	}

	@Override
	protected void txtValueActionPerformed() {
		try {
			
			theScannable.moveTo(getTxtNoCommas()); 
		} catch (Exception e) {
			logger.error("Exception while trying to move " + scannableName + ": " + e.getMessage(),e);
			JOptionPane.showMessageDialog(getTopLevelAncestor(), scannableName + ": " + 
					exceptionUtils.getFullStackMsg(e),
					"Error Message", JOptionPane.ERROR_MESSAGE);
		}
	}

	@Override
	protected void refreshValues() {
		try{
			String[] posAsString = ScannableUtils.getFormattedCurrentPositionArray(
					theScannable.getPosition(), 
					arrayLength, 
					theScannable.getOutputFormat());
			valueString = posAsString[0];
		} catch (Throwable e) {
			logger.error("Exception while trying to move " + scannableName + ": " + e.getMessage(),e);
		}
	}
	
	@Override
	protected void configure() {
		//do nothing
	}

	/**
	 * This should hold the logic about whether to update or now based on the gievn information
	 * 
	 * @see gda.observable.IObserver#update(java.lang.Object, java.lang.Object)
	 */
	@Override
	public void update(Object theObserved, Object changeCode) {
		updateDisplay();	
	}	
}
