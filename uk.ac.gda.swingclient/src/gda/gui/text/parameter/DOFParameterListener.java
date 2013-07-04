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

package gda.gui.text.parameter;

import gda.factory.Finder;
import gda.oe.MoveableException;
import gda.oe.OE;
import gda.oe.util.OEHelpers;
import gda.util.exceptionUtils;

import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.util.ArrayList;

import javax.swing.JPanel;

import org.jscience.physics.quantities.Quantity;
import org.jscience.physics.units.Unit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DOFParameterListener Class
 */
public class DOFParameterListener implements VetoableChangeListener {
	
	private static final Logger logger = LoggerFactory.getLogger(DOFParameterListener.class);
	
	final private String oeName, dofName;

	private final OE oe;

	/**
	 * @param oeName
	 * @param dofName
	 */
	public DOFParameterListener(String oeName, String dofName) {
		this.oeName = oeName;
		this.dofName = dofName;
		if (oeName == null || dofName == null) {
			oe = null;
			throw new IllegalArgumentException("ObservableParameterMonitor. oeName or dofName is null");

		}
		Object o = Finder.getInstance().find(oeName);
		if (o == null) {
			oe = null;
			throw new IllegalArgumentException("ObservableParameterMonitor. unable to find " + oeName);
		}
		if (!(o instanceof OE)) {
			oe = null;
			throw new IllegalArgumentException("ObservableParameterMonitor. " + oeName + " is not an OE");
		}
		oe = (OE) o;
		String names[] = oe.getDOFNames();
		boolean found = false;
		for (String name : names) {
			if (name.equals(dofName)) {
				found = true;
				break;
			}
		}
		if (!found) {
			throw new IllegalArgumentException("ObservableParameterMonitor. OE:" + oeName + " does not contain dof "
					+ dofName);
		}
	}

	@Override
	public void vetoableChange(PropertyChangeEvent e) throws PropertyVetoException {
		Object source = e.getSource();
		if (source == null)
			throw new IllegalArgumentException("DOFParameterListener.propertyChange - source == null ");
		if (source instanceof ParametersPanelBuilder.ParameterChangeEventSource) {

			Object newObject = e.getNewValue();
			if ((newObject == null) || !(newObject instanceof Double)) {
				throw new IllegalArgumentException(
						"DOFParameterListener.propertyChange -  (newObject == null ) || !(newObject instanceof Limited) ");
			}
			try {
				Unit<? extends Quantity> units = oe.getReportingUnits(dofName);
				Quantity val = Quantity.valueOf((Double) newObject, units);
				oe.moveTo(dofName, val);
			} catch (MoveableException ex) {
				exceptionUtils.logException(logger, "Error in vetoableChange :" + oeName + "." + dofName, ex);
				throw new PropertyVetoException(ex.getMessage(), e);
			}

		}
	}
	
	/**
	 * @param id
	 * @param oeName
	 * @param dofName
	 * @param format
	 * @param label
	 * @param tooltip
	 * @param displayedMnemonic
	 * @param alignmentX
	 * @param editable
	 * @return Limited<Double>
	 * @throws gda.oe.MoveableException
	 */
	public static Limited createDOFLimited(int id, String oeName, String dofName, String format, String label,
			String tooltip, char displayedMnemonic, float alignmentX, boolean editable) throws gda.oe.MoveableException {
		if (oeName == null || dofName == null) {
			throw new IllegalArgumentException("createDOFLimited. oeName or dofName is null");
		}
		OE oe = OEHelpers.getOEForDOF(oeName, dofName);
		double val = oe.getPosition(dofName).getAmount();
		Unit<? extends Quantity> units = oe.getReportingUnits(dofName);
		Quantity lower = oe.getSoftLimitLower(dofName);
		Quantity upper = oe.getSoftLimitUpper(dofName);
		
		return new Limited(id, val, lower.getAmount(), upper.getAmount(), format, label, tooltip, units
				.toString(), displayedMnemonic, alignmentX, editable);
	}

	/**
	 * @param phiOE
	 * @param phiDOFName
	 * @param format
	 * @param label
	 * @param tooltip
	 * @param displayedMnemonic
	 * @param alignmentX
	 * @param editable
	 * @param title
	 * @return ParameterBuilderPanel
	 * @throws gda.oe.MoveableException
	 */
	@SuppressWarnings("unused")
	public static ParameterBuilderPanel createDOFPanel(String phiOE, String phiDOFName, String format, String label,
			String tooltip, char displayedMnemonic, float alignmentX, boolean editable, String title)
			throws gda.oe.MoveableException {
		ArrayList<Limited> parametersLimited = new ArrayList<Limited>();
		parametersLimited.add(createDOFLimited(1, phiOE, phiDOFName, format, label, tooltip, displayedMnemonic,
				alignmentX, editable));

		GridLayout subLayout = new GridLayout(1, 0);
		subLayout.setHgap(10);

		ParametersPanelBuilder panelBuilder = new ParametersPanelBuilder( parametersLimited, 1, "", title, null,
				null, null, 2, 80, 5, 10, 10, null, null);
		if (editable) {
			panelBuilder.addVetoableChangeListener(ParametersPanelBuilder.ValuePropertyName + "1",
					new DOFParameterListener(phiOE, phiDOFName));
		}
		new ObservableParameterMonitor(phiOE, phiDOFName, panelBuilder, 1);

		JPanel panel1 = new JPanel();
		panel1.setLayout(new GridBagLayout());
		panel1.add(panelBuilder);
		return new ParameterBuilderPanel(panelBuilder, panel1);
	}	
}
