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

package gda.gui.oemove;

import java.awt.BorderLayout;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

/**
 * ControlPanel Class
 */
public class ControlPanel extends JPanel {
	private Map<String, JComponent> controlComponents = new LinkedHashMap<String, JComponent>();

	private JComponent currentControlComponent;

	/**
	 * Constructor
	 */
	public ControlPanel() {
		setLayout(new BorderLayout());
		setBorder(new BevelBorder(BevelBorder.RAISED));
	}

	/**
	 * @param name
	 * @param representation
	 */
	public void addControlComponent(String name, Representation representation) {
		controlComponents.put(name, representation.getControlComponent());
	}

	/**
	 * @param name
	 */
	public void removeControlComponent(String name) {
		JComponent component = controlComponents.get(name);
		controlComponents.remove(name);
		remove(component);
		revalidate();
		repaint();
	}

	/**
	 * This method is called from within the event thread when an internal frame is selected.
	 * 
	 * @param name
	 *            the name associated with the internal frame
	 */
	public void updateControlPanel(String name) {
		if (currentControlComponent != null)
			remove(currentControlComponent);
		currentControlComponent = controlComponents.get(name);
		add(currentControlComponent);
		currentControlComponent.repaint();
		revalidate();
		repaint();
	}
}
