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

package gda.gui;

import gda.factory.Configurable;
import gda.factory.FactoryException;
import gda.factory.Findable;

import javax.swing.JPanel;

/**
 * A class to provide a uniform interface for all panels that will be configured into the tabbed pane environment of
 * {@link gda.gui.AcquisitionFrame}
 */
public class MessagePanel extends JPanel implements Findable, Configurable, Tidyable {
	private transient int tabIndex;

	/**
	 * @return Returns the tabIndex.
	 */
	public int getTabIndex() {
		return tabIndex;
	}

	/**
	 * @param tabIndex
	 *            The tabIndex to set.
	 */
	public void setTabIndex(int tabIndex) {
		this.tabIndex = tabIndex;
	}

	/**
	 * Subclasses to override this method.
	 * 
	 * @see gda.gui.Tidyable#tidyup()
	 */
	@Override
	public void tidyup() {
	}

	/**
	 * Subclasses to override this method.
	 * 
	 * @see gda.factory.Configurable#configure()
	 */
	@Override
	public void configure() throws FactoryException {
	}
}
