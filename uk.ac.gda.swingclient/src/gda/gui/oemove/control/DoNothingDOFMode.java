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

package gda.gui.oemove.control;

import gda.gui.oemove.DOFModeDisplay;
import gda.observable.IObserver;
import gda.observable.ObservableComponent;

import java.util.ArrayList;

import javax.swing.JLabel;

/**
 * DoNothingDOFMode JLabel
 */
public class DoNothingDOFMode extends JLabel implements DOFModeDisplay {
	private ObservableComponent observableComponent = new ObservableComponent();

	int mode = -1;

	/**
	 * Constructor
	 */
	public DoNothingDOFMode() {
		super("");
	}

	@Override
	public void setMode(int mode) {
		this.mode = mode;
	}

	@Override
	public int getMode() {
		return mode;
	}

	@Override
	public void setModeNames(ArrayList<String> modeList) {
	}

	@Override
	public void addIObserver(IObserver anIObserver) {
		observableComponent.addIObserver(anIObserver);
	}

	@Override
	public void deleteIObserver(IObserver anIObserver) {
		observableComponent.deleteIObserver(anIObserver);
	}

	@Override
	public void deleteIObservers() {
		observableComponent.deleteIObservers();
	}

	@Override
	public String toString() {
		return (new Integer(mode)).toString();
	}
}
