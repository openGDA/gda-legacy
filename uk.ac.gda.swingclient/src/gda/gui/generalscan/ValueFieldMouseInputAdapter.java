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

import java.awt.event.MouseEvent;

import javax.swing.event.MouseInputAdapter;

/**
 * Acts as the MouseListener for ALL ValueFields (since the mouse can only be in one ValueField at a time it should be
 * alright to do this).
 */
public class ValueFieldMouseInputAdapter extends MouseInputAdapter {
	private String value;

	private ValueField workingFor;

	private static ValueFieldMouseInputAdapter instance = new ValueFieldMouseInputAdapter();

	private ValueFieldMouseInputAdapter() {
	}

	/**
	 * @return instance
	 */
	public static ValueFieldMouseInputAdapter getInstance() {
		return instance;
	}

	/**
	 * Overrides the MouseInputAdapter's mouseEntered. Saves the current value when the mouse enters.
	 * 
	 * @param squeek
	 *            the mouse event
	 */
	@Override
	public void mouseEntered(MouseEvent squeek) {
		workingFor = (ValueField) squeek.getSource();
		value = workingFor.getText();
	}

	/**
	 * Overrides the MouseInputAdapter's mouseExited. If the value has changed sets the new value in the model (by
	 * calling the ValueField's actionPerformed method).
	 * 
	 * @param squeek
	 *            the mouse event
	 */
	@Override
	public void mouseExited(MouseEvent squeek) {
		String newValue = workingFor.getText();
		if (!newValue.equals(value)) {
			workingFor.actionPerformed(null);
		}
	}
}
