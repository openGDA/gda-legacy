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

package gda.device.detector.odccd;

import gda.device.ODCCD;
import gda.factory.FactoryException;
import gda.factory.Finder;
import gda.gui.AcquisitionPanel;

import javax.swing.JTabbedPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * <b>Title: </b>GDA GUI Panel for Oxford Diffraction CCDs.
 * </p>
 * <p>
 * <b>Description: </b>This class extends <code>gda.gui.AcquisitionPanel</code> and contains both the control and
 * monitor panel for the Oxford Diffraction CCDs. This class is designed to be created in the client GDA GUI.
 * </p>
 */

public class ODCCDPanel extends AcquisitionPanel {
	
	private static final Logger logger = LoggerFactory.getLogger(ODCCDPanel.class);

	/** Private reference to the CCD control panel. */
	private ODCCDControlPanel mControlPanel = null;

	/** Private reference to a concrete instance of ODCCD. */
	private ODCCD mCCD = null;

	/** Private reference to the name of the CCD control object. */
	private String _ccdName = null;

	/**
	 * This is called to set up the GDA GUI panel.
	 * 
	 * @throws FactoryException
	 */
	@Override
	public void configure() throws FactoryException {

		if ((mCCD = (ODCCD) Finder.getInstance().find(this.getCcdName())) == null) {
			logger.error("ERROR: Could not find " + this.getCcdName() + " using Finder.");
			throw new FactoryException("Could not find instance of remote ODCCD class.");
		}

		// Set the reference to the CCD in the panels
		mControlPanel = new ODCCDControlPanel(mCCD);
		// Set the refernece to the CCD object in the panels

		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.addTab("Control", mControlPanel);
		// Add tabbed panel
		this.add(tabbedPane);
	}

	/**
	 * Get the name of the CCD control object.
	 * 
	 * @return the name of the CCD control object
	 */
	public String getCcdName() {
		return _ccdName;
	}

	/**
	 * Set the name of the CCD control object.
	 * 
	 * @param ccdName
	 */
	public void setCcdName(String ccdName) {
		_ccdName = ccdName;
	}

}
