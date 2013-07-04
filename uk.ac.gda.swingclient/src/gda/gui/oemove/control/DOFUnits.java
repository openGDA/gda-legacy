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

import gda.configuration.properties.LocalProperties;
import gda.gui.oemove.DOFUnitsDisplay;
import gda.observable.IObserver;
import gda.observable.ObservableComponent;
import gda.util.QuantityFactory;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.border.TitledBorder;

import org.jscience.physics.quantities.Quantity;
import org.jscience.physics.units.Unit;

/**
 * DOFUnits Class
 */
public class DOFUnits extends JComboBox implements ActionListener, DOFUnitsDisplay {
	private Unit<? extends Quantity> units;

	private ObservableComponent observableComponent = new ObservableComponent();

	/**
	 * Create a combobox to display acceptable units
	 * 
	 * @param acceptableUnits
	 *            the unit type
	 * @param reportingUnits
	 */
	public DOFUnits(ArrayList<Unit<? extends Quantity>> acceptableUnits, Unit<? extends Quantity> reportingUnits) {
		for (Unit<? extends Quantity> unit : acceptableUnits)
			if (unit != null && unit.toString() != null)
				addItem(unit.toString());

		if (reportingUnits == null)
			units = acceptableUnits.get(0);
		else
			units = reportingUnits;

		setSelectedItem(units.toString());

		addActionListener(this);
		setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Units", TitledBorder.CENTER,
				TitledBorder.TOP, null, Color.black));
	}

	@Override
	public void actionPerformed(ActionEvent ae) {
		JComboBox combo = (JComboBox) ae.getSource();
		units = QuantityFactory.createUnitFromString((String) combo.getSelectedItem());

		observableComponent.notifyIObservers(this, null);
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
	public Unit<? extends Quantity> getUnits() {
		return units;
	}

	/**
	 * @return selected units
	 */
	public int getSelectedUnits() {
		return getSelectedIndex();
	}

	@Override
	public void setUnits(int index) {
		if (!LocalProperties.check("gda.oemove.enableMM", true))
			index = 0;
		else if (!LocalProperties.check("gda.oemove.enableDEG", true))
			index = 0;

		setSelectedIndex(index);
	}

	@Override
	public String toString() {
		return "" + getSelectedIndex();
	}
}
