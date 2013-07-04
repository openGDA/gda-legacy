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

package gda.gui.beans;

import gda.factory.Finder;
import gda.oe.MoveableException;
import gda.oe.MoveableStatus;
import gda.oe.OE;
import gda.util.QuantityFactory;

import org.jscience.physics.quantities.Quantity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This code was edited or generated using CloudGarden's Jigloo SWT/Swing GUI Builder, which is free for non-commercial
 * use. If Jigloo is being used commercially (ie, by a corporation, company or business for any purpose whatever) then
 * you should purchase a license for each developer using Jigloo. Please visit www.cloudgarden.com for details. Use of
 * Jigloo implies acceptance of these licensing terms. A COMMERCIAL LICENSE HAS NOT BEEN PURCHASED FOR THIS MACHINE, SO
 * JIGLOO OR THIS CODE CANNOT BE USED LEGALLY FOR ANY CORPORATE OR COMMERCIAL PURPOSE.
 */
/**
 * This bean displays and allows users to operate a DOF. It observes the DOF and updates its fields whenever they are
 * changed.
 * <p>
 * Once setup, connectToOE should be called to create the connection to the OE.
 */
public class DOFBean extends BeanBase {
	
	private static final Logger logger = LoggerFactory.getLogger(DOFBean.class);

	private OE theOE;

	private String oeName;

	private String dofName;

	/**
	 * Constructor.
	 */
	public DOFBean() {
		super();
		initGUI();
	}

	/**
	 * @return Returns the oeName.
	 */
	public String getOeName() {
		return oeName;
	}

	/**
	 * @param oeName
	 *            The oeName to set.
	 */
	public void setOeName(String oeName) {
		this.oeName = oeName;
	}

	/**
	 * @return Returns the dofName.
	 */
	public String getDofName() {
		return dofName;
	}

	/**
	 * @param dofName
	 *            The dofName to set.
	 */
	public void setDofName(String dofName) {
		this.dofName = dofName;
	}

	/**
	 * This should be called after OE and DOF strings setup. This connects to the OE and adds itself as an IObserver. It
	 * also changes the unit string and tooltiptext.
	 */
	@Override
	public void startDisplay() {

		if (oeName != null && dofName != null) {

			this.theOE = (OE) Finder.getInstance().find(oeName);
			if (theOE != null) {
				theOE.addIObserver(this);
				manualUpdate = true;
			}
			super.configure();
		} else {
			logger.error("DOFBean cannot connect to DOF as information missing!");
		}
	}

	@Override
	public void update(Object theObserved, Object changeCode) {
		if (theObserved instanceof OE) {
			if (changeCode instanceof MoveableStatus) {
				MoveableStatus ms = (MoveableStatus) changeCode;
				if (ms.getMoveableName().equals(dofName)) {
					super.update(theObserved, changeCode);
				}
			} else {
				super.update(theObserved, changeCode);
			}
		}
	}

	@Override
	protected synchronized void refreshValues() {

		try {
			// set the label
			this.getLblLabel().setText(getLabel());

			// set units
			unitsString = theOE.getReportingUnits(dofName).toString();

			// set value
			Double currentPosition = theOE.getPosition(dofName).getAmount();
			valueString = String.format(getDisplayFormat(), currentPosition).trim();

			// set tooltip
			String toolTip = theOE.getName() + " " + dofName + " ";
			toolTip += "(" + String.format(getDisplayFormat(), theOE.getSoftLimitLower(dofName).getAmount()) + ", ";
			toolTip += String.format(getDisplayFormat(), theOE.getSoftLimitUpper(dofName).getAmount()) + ")";
			tooltipString = toolTip;
		} catch (Exception e) {
			logger.error("Exception while trying to get position of  " + oeName + ": " + e.getMessage());
		}

	}

	@Override
	protected void txtValueActionPerformed() {
		// move the dof to whatever the current value is displayed.
		try {
			Quantity position = QuantityFactory.createFromTwoStrings(getTxtNoCommas(), getLblUnits().getText()
					.trim());
			theOE.moveTo(dofName, position);
		} catch (Exception e) {
			logger.error("Exception while trying to move " + oeName + ": " + e.getMessage());
		}
	}

	@Override
	protected boolean theObservableIsChanging() {
		try {
			return theOE.isMoving(dofName);
		} catch (MoveableException e) {
			return false;
		}
	}
}
