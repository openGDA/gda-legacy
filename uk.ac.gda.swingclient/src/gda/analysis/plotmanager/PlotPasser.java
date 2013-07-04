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

package gda.analysis.plotmanager;

import java.io.Serializable;

/**
 *
 */
public class PlotPasser implements Serializable {

	private String key;
	private String panel;

	/**
	 * This plot passer object is designed to allow quick communication across corba, and the ability for the client to
	 * fetch data from the server as required
	 * 
	 * @param plotKey
	 * @param plotPanelName
	 */
	public PlotPasser(String plotKey, String plotPanelName) {
		key = plotKey;
		panel = plotPanelName;
	}

	/**
	 * getter
	 * 
	 * @return the key
	 */
	public String getKey() {
		return key;
	}

	/**
	 * setter
	 * 
	 * @param key
	 */
	public void setKey(String key) {
		this.key = key;
	}

	/**
	 * getter
	 * 
	 * @return the panel
	 */
	public String getPanel() {
		return panel;
	}

	/**
	 * setter
	 * 
	 * @param panel
	 */
	public void setPanel(String panel) {
		this.panel = panel;
	}

}
