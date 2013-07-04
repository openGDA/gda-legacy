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

package gda.observable;

import javax.swing.JPanel;

/**
 * A JPanel which implements IObservable.
 */

public class IObservableJPanel extends JPanel implements IObservable {
	
	private ObservableComponent oc;

	public IObservableJPanel() {
		oc = new ObservableComponent();
	}

	@Override
	public void addIObserver(IObserver io) {
		oc.addIObserver(io);
	}

	@Override
	public void deleteIObserver(IObserver io) {
		oc.deleteIObserver(io);
	}

	@Override
	public void deleteIObservers() {
		oc.deleteIObservers();
	}

	public void notifyIObservers(Object theObserver, Object theArgument) {
		oc.notifyIObservers(theObserver, theArgument);
	}
}
