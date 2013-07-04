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

package gda.gui.eventnotification;

import gda.factory.Configurable;
import gda.factory.FactoryException;
import gda.observable.IObserver;
import gda.util.TitleBorder;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

/**
 * Base class for a panel on the main EventNotificationPanel
 */
public class NotificationPanel extends JPanel implements Configurable, IObserver {
	private boolean placePanelOnNewLine = true;
	private String textPositioning = "West";
	
	private JCheckBox ignoreFutureUpdatesCheckBox;

	/**
	 * @return checkbox for ignoring future updates
	 */
	public JCheckBox getIgnoreCheckBox() {
		return ignoreFutureUpdatesCheckBox;
	}
	
	/**
	 * Returns whether future updates are ignored.
	 * 
	 * @return {@code true} if updates are ignored
	 */
	public boolean igoreFutureUpdates() {
		if (ignoreFutureUpdatesCheckBox == null) {
			return false;		// if box not set then it's false 
		}
		return ignoreFutureUpdatesCheckBox.isSelected();
	}
	/**
	 * Sets the checkbox for ignoring future updates
	 * @param selected if the checkbox is to be checked or not
	 */
	public void setIgnoreCheckBox(boolean selected) {
		if (ignoreFutureUpdatesCheckBox != null) {
			ignoreFutureUpdatesCheckBox.setSelected(selected);
		}
	}
	
	/**
	 * @param ignoreCheckBox
	 */
	public void setIgnoreCheckBox(JCheckBox ignoreCheckBox) {
		this.ignoreFutureUpdatesCheckBox = ignoreCheckBox;		
	}
	@Override
	public void configure() throws FactoryException {
		setBorder(new TitleBorder(this.getName()));
	
	}

	@Override
	public void update(Object theObserved, Object changeCode) {
		//setForeground(Color.RED);

	}
	/**
	 * @return true if this panel to be placed on a new line
	 */
	public boolean isPlacePanelOnNewLine() {
		return placePanelOnNewLine;
	}
	/**
	 * @param placePanelOnNewLine true if this panel to be placed on a new line
	 */
	public void setPlacePanelOnNewLine(boolean placePanelOnNewLine) {
		this.placePanelOnNewLine = placePanelOnNewLine;
	}
	/**
	 * @return positioning of text
	 */
	public String getTextPositioning() {
		return textPositioning;
	}
	/**
	 * @param textPositioning positioning of text
	 */
	public void setTextPositioning(final String textPositioning) {
		this.textPositioning = textPositioning;
	}
}
