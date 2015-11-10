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

import gda.observable.IObservableJPanel;
import gda.observable.IObserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for various types of Scan.
 */
public abstract class Scan extends IObservableJPanel implements IObserver, CommandSupplier {
	
	private static final Logger logger = LoggerFactory.getLogger(Scan.class);
	
	/**
	 * 
	 */
	public static final int MODE_STEP = 0;

	/**
	 * 
	 */
	public static final int MODE_CONTINUOUS = 1;

	private ScanModel model;

	/* createModel is a FactoryMethod */
	/* init and reDisplay are TemplateMethods */

	/* This method should create a ScanModel of suitable type */
	protected abstract ScanModel createModel();

	/* This method should create the GUI elements needed */
	protected abstract void init();

	/* This method should display values from the model in the GUI elements */
	protected abstract void reDisplay();

	/**
	 * Constructor
	 */
	public Scan() {
		model = createModel();
		model.addIObserver(this); //FIXME: potential race condition
		init();
	}

	/**
	 * Returns the model.
	 * 
	 * @return the model
	 */
	public ScanModel getModel() {
		return model;
	}

	/**
	 * @param model
	 */
	public void setModel(ScanModel model) {
		this.model.deleteIObserver(this);
		this.model = model;
		this.model.addIObserver(this);
		reDisplay();
		notifyIObservers(this, null);
	}

	/**
	 * Called by IObservables
	 * 
	 * @param observed
	 * @param argument
	 */
	@Override
	public void update(Object observed, Object argument) {
		/* All Scans observe their model */
		if (observed instanceof ScanModel) {
			logger.debug("Scan update called by ScanModel");
			reDisplay();
			notifyIObservers(this, null);
		}
	}

	/**
	 * @return total number of points
	 */
	public int getTotalNumberOfPoints() {
		return model.getTotalPoints();
	}

	/**
	 * Implementation of CommandSupplier interface is by delegating to the model.
	 * 
	 * @return the command
	 */
	@Override
	public String getCommand() {
		return model.getCommand();
	}

	@Override
	public boolean getValid() {
		return model.getValid();
	}

	@Override
	public void setScanMode(int scanMode) {
		model.setScanMode(scanMode);
	}

	@Override
	public void setScanMode(String scanMode) {
		int mode = MODE_STEP;
		if (scanMode.equals("Continuous")) {
			mode = MODE_CONTINUOUS;
		}
		setScanMode(mode);
	}
}
