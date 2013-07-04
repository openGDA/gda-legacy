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

/**
 * Inteface to be implemented by the various types of scan displayer.
 */
public interface CommandSupplier {
	/**
	 * Should supply a command to run the currently displayed scan
	 * 
	 * @return supplied command to run the currently displayed scan
	 */
	public String getCommand();

	/**
	 * Should return whether or not the currently displayed scan is valid
	 * 
	 * @return true if currently displayed scan is valid
	 */
	public boolean getValid();

	/**
	 * Should set the scan mode (continuous or step).
	 * 
	 * @param scanMode
	 */
	public void setScanMode(int scanMode);

	/**
	 * Should set the scan mode (continuous or step).
	 * 
	 * @param scanMode
	 *            "Step" or "Continuous"
	 */
	public void setScanMode(String scanMode);
}
