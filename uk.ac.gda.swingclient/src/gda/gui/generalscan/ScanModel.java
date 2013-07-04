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

package gda.gui.generalscan;

import gda.observable.IObserver;
import gda.observable.ObservableComponent;

import javax.swing.table.AbstractTableModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for the data models used by the various types of scan.
 */
public abstract class ScanModel extends AbstractTableModel implements ValueModel, CommandSupplier {
	protected static final Logger logger = LoggerFactory.getLogger(ScanModel.class);

	private int scanMode;

	/**
	 * Should calculate a sensible scan for the given value
	 * 
	 * @param value
	 *            the value to calculate a sensible scan for.
	 */
	public abstract void setDefaultScan(double value);

	/**
	 * TemplateMethod which should do recalculating needed after set value
	 */
	protected abstract void reCalculate();

	/**
	 * TemplateMethod needed to fully implement setValue
	 * 
	 * @param name
	 * @param value
	 * @throws NoSuchFieldException
	 * @throws IllegalAccessException
	 */
	protected abstract void _setValue(String name, String value) throws NoSuchFieldException, IllegalAccessException;

	/**
	 * TemplateMethod needed to fully implement getValue
	 * 
	 * @param name
	 * @return the value
	 * @throws NoSuchFieldException
	 * @throws IllegalAccessException
	 */
	protected abstract String _getValue(String name) throws NoSuchFieldException, IllegalAccessException;

	/**
	 * Get the total no. points in the scan
	 * 
	 * @return the total no. points
	 */
	public abstract int getTotalPoints();

	/* methods to implement the ValueModel interface */

	/**
	 * Gets the value of the named property.
	 * 
	 * @param name
	 *            the name of the value to get
	 * @return the value
	 */
	@Override
	public String getValue(String name) {
		String value = null;

		try {
			/* subclasses must provide an implementation of this method */
			value = _getValue(name);
		} catch (NoSuchFieldException nsfe) {
			logger.error("NoSuchFieldException in XafsModel getValue: " + nsfe);
		} catch (IllegalAccessException iae) {
			logger.error("IllegalAccessException in XafsModel getValue: " + iae);
		}
		return value;
	}

	/**
	 * Sets the value of the named property.
	 * 
	 * @param name
	 *            the name of the value to be set
	 * @param value
	 *            the value to give it
	 */
	@Override
	public void setValue(String name, String value) {
		try {
			/* subclasses must provide an implementation of this method */
			_setValue(name, value);
		} catch (NoSuchFieldException nsfe) {
			logger.error("Exception in ScanModel setValue: " + nsfe);
		} catch (IllegalAccessException iae) {
			logger.error("Exception in ScanModel setValue: " + iae);
		}

		reCalculate();
		notifyIObservers(this, null);
	}

	/* Methods to implement the IObservable interface (which is part */
	/* of the ValueModel interface). */

	private ObservableComponent oc = new ObservableComponent();

	@Override
	public void addIObserver(IObserver anIObserver) {
		oc.addIObserver(anIObserver);
	}

	@Override
	public void deleteIObserver(IObserver anIObserver) {
		oc.deleteIObserver(anIObserver);
	}

	@Override
	public void deleteIObservers() {
		oc.deleteIObservers();
	}

	/**
	 * Notify observers of a change
	 * 
	 * @param theObserved
	 *            the object being observed (usually this)
	 * @param theArgument
	 *            the change to be communicated to observers of theObserved
	 */
	public void notifyIObservers(Object theObserved, Object theArgument) {
		oc.notifyIObservers(theObserved, theArgument);
	}

	@Override
	public void setScanMode(int scanMode) {
		this.scanMode = scanMode;
	}

	@Override
	public void setScanMode(String scanMode) {
		int mode = Scan.MODE_STEP;
		if (scanMode.equals("Continuous")) {
			mode = Scan.MODE_CONTINUOUS;
		}
		setScanMode(mode);
	}

	/**
	 * @return the int scan mode
	 */
	public int getScanMode() {
		return scanMode;
	}

	/**
	 * Get the total time for the scan
	 * 
	 * @return total time (units may depend on implementation)
	 */
	public abstract double getTotalTime();
}
