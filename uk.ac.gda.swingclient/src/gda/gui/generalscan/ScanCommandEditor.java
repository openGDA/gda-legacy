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

import javax.swing.JComponent;

/**
 * Interface which allows a class to be made available to edit scan scripts on a station or dof basis just before they
 * are run
 */
public interface ScanCommandEditor {
	/**
	 * Returns a JComponent which can be displayed within a GeneralScanPanel to allow editing of values and so on.
	 * 
	 * @return the JComponent
	 */
	public JComponent getComponent();

	/**
	 * Tells the ScanCommandEditor which dof is currently selected so that it can arrange itself accordingly.
	 * 
	 * @param dofName
	 *            the name of the dof currently selected.
	 * @return true if it will do something for this dof, false if not.
	 */
	public boolean enableDofName(String dofName);

	/**
	 * Edits the given scan command as appropriate for the current settings.
	 * 
	 * @param commmand
	 *            the unedited command.
	 * @return the edited command.
	 */
	public String editCommand(String commmand);
}