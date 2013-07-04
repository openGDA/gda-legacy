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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;

/**
 * A JMenu which implements IObservable.
 */
public class IObservableJMenu extends JMenu implements IObservable, ActionListener {
	private ObservableComponent oc;

	/**
	 * Create an IObservable JMenu with a specific title
	 * 
	 * @param title
	 *            the title to display on the JMenu
	 */
	public IObservableJMenu(String title) {
		super(title);
		oc = new ObservableComponent();
	}

	/**
	 * 
	 */
	public IObservableJMenu() {
		super();
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

	/**
	 * @param theObserved
	 * @param theArgument
	 */
	public void notifyIObservers(Object theObserved, Object theArgument) {
		oc.notifyIObservers(theObserved, theArgument);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		notifyIObservers(this, command);
	}
}
