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

package gda.device.scannable;

import gda.device.DeviceException;
import gda.device.ScannableMotion;
import gda.device.scannable.ScannableUtils.ScannableValidationException;
import gda.oe.MoveableException;
import gda.oe.OE;

import org.jscience.physics.quantities.Quantity;

public class DOFAdapterWithStationaryRelativeLimits extends DOFAdapter {
	
	ScannableMotion relativeLimitReferenceScannable;

	Double minimumPosWrtReference;

	Double maximumPosWrtReference;

	public DOFAdapterWithStationaryRelativeLimits(OE oe, String dofname) throws ScannableValidationException {
		super(oe, dofname);
	}

	@Override
	public String toString() {
		try {
			String output = "";

			if (getReportingUnits() == null) {
				output = getName() + " : " + String.format(getOutputFormat()[0], getPosition());
			} else {
				output = getName() + " : "
						+ String.format(getOutputFormat()[0], Double.parseDouble(getPosition().toString())) + " "
						+ getReportingUnits().toString();
			}

			// Display the scannable limits
			// Print limits if they are set for this input
			// Check which are set being very careful of nulls!
			Double lowerLim = null;
			Double upperLim = null;
			{
				Double [] limits=getLowerGdaLimits();
				if (limits != null && limits[0] != null) {
					lowerLim = limits[0];
				}
			}
			{
				Double [] limits=getUpperGdaLimits();
				if (limits != null && limits[0] != null) {
					upperLim = limits[0];
				}
			}

			if (lowerLim != null | upperLim != null) {
				output += " gda(";
				if (lowerLim != null) {
					output += lowerLim;
				} else {
					output += " ";
				}
				output += " : ";
				if (upperLim != null) {
					output += upperLim;
				} else {
					output += " ";
				}
				output += ")";

			}			
			// Display the DOF limits
			if (oe.getSoftLimitLower(dofname) != null && oe.getSoftLimitUpper(dofname) != null) {
				output += " dof(" + String.format(getOutputFormat()[0], oe.getSoftLimitLower(dofname).getAmount())
						+ " : " + String.format(getOutputFormat()[0], oe.getSoftLimitUpper(dofname).getAmount()) + ")";
			}

			// Display the relative limit
			if (maximumPosWrtReference != null) {
				String referenceName = relativeLimitReferenceScannable.getName();
				output += " (" + getName() + "-" + referenceName + "<=" + this.maximumPosWrtReference.toString() + ")";
			}
			if (minimumPosWrtReference != null) {
				String referenceName = relativeLimitReferenceScannable.getName();
				output += " (" + getName() + "-" + referenceName + ">=" + this.minimumPosWrtReference.toString() + ")";
			}
			return output;
		} catch (Exception e) {
			return getName() + ". DOF.toString:exception = " + e.getMessage();
		}
	}

	/**
	 * {@inheritDoc} Overrides ScanabbleMotionBase, to also check DOF limits its(including EPICs soft limits).
	 * 
	 * @see gda.device.ScannableMotion#checkPositionValid(java.lang.Object)
	 */
	@Override
	public String checkPositionValid(Object position) {
		// Check against DOF limits. These track EPICs soft limits in most
		// cases.

		// Check against scannable limits
		String report = super.checkPositionValid(position);
		if (report != null) {
			return report;
		}
		
		try // TODO: Is this try/catch loop really needed (rdw-Jan2008)
		{
			Quantity newPosition = Quantity.valueOf(position.toString());
			Quantity upper = this.oe.getSoftLimitUpper(this.dofname);
			Quantity lower = this.oe.getSoftLimitLower(this.dofname);

			if (lower.getAmount() > upper.getAmount()) {
				Quantity temp = upper;
				upper = lower;
				lower = temp;
			}

			if (!(newPosition.getAmount() >= lower.getAmount() && newPosition.getAmount() <= upper.getAmount())) {
				return String.format("target position (%s) is outside of DOF/EPICs limits (%s, %s )", newPosition
						.toText(), lower.toText(), upper.toText());
			}

			

			// Check against relative limits

			report = this.checkRelativeLimits(ScannableUtils.objectToArray(position)[0]);
			if (report != null) {
				return "Moving " + this.getName() + " would break relative limits: " + report;
			}

			// Has passed all checks
			return null;

		}
		// Is this try/catch loop really needed (rdw-Jan2008)
		catch (MoveableException e) {
			throw new RuntimeException("error occurred during validation: " + e.getMessage());
		} catch (DeviceException e) {
			throw new RuntimeException("error occurred during validation: " + e.getMessage());
		}
	}

	public void setMinimumPosWrtReference(Integer value, ScannableMotion referenceScannable) {
		this.setMinimumPosWrtReference(value.doubleValue(), referenceScannable);
	}

	public void setMinimumPosWrtReference(Double value, ScannableMotion referenceScannable) {
		this.relativeLimitReferenceScannable = referenceScannable;
		this.maximumPosWrtReference = null;
		this.minimumPosWrtReference = value;
	}

	public void setMaximumPosWrtReference(Integer value, ScannableMotion referenceScannable) {
		this.setMaximumPosWrtReference(value.doubleValue(), referenceScannable);
	}

	public void setMaximumPosWrtReference(Double value, ScannableMotion referenceScannable) {
		this.relativeLimitReferenceScannable = referenceScannable;
		this.minimumPosWrtReference = null;
		this.maximumPosWrtReference = value;
	}

	/**
	 * Checks if the position hits a relative limits To use this:
	 * <p>
	 * example: make sure a-b>90 >>> aMinusBMinimum = 90 >>> a.setMinimumPosWrtReference(aMinusBMinimum, b) >>>
	 * b.setMaximumPosWrtReference(-aMinusBMinimum, a)
	 * 
	 * @param posObject
	 * @return null if okay else a description of the problem
	 * @throws DeviceException
	 */
	public String checkRelativeLimits(Object posObject) throws DeviceException {
		double pos = ScannableUtils.objectToArray(posObject)[0];
		if (this.minimumPosWrtReference != null) {
			double referencePos = new Double(relativeLimitReferenceScannable.getPosition().toString());

			if ((pos - referencePos) < this.minimumPosWrtReference) {
				String referenceName = relativeLimitReferenceScannable.getName();
				return "pos(" + getName() + ")[" + pos + "] - pos(" + referenceName + ")[" + referencePos
						+ "] must be more than " + this.minimumPosWrtReference.toString();
			}
		}
		if (this.maximumPosWrtReference != null) {
			double referencePos = new Double(relativeLimitReferenceScannable.getPosition().toString());

			if ((pos - referencePos) > this.maximumPosWrtReference) {
				String referenceName = relativeLimitReferenceScannable.getName();
				return "pos(" + getName() + ") - pos(" + referenceName + ") must be less than "
						+ this.maximumPosWrtReference.toString();
			}
		}
		return null;
	}

	
	public ScannableMotion getRelativeLimitReferenceScannable() {
		return relativeLimitReferenceScannable;
	}

	public void setRelativeLimitReferenceScannable(ScannableMotion relativeLimitReferenceScannable) {
		this.relativeLimitReferenceScannable = relativeLimitReferenceScannable;
	}

	public Double getMinimumPosWrtReference() {
		return minimumPosWrtReference;
	}

	public Double getMaximumPosWrtReference() {
		return maximumPosWrtReference;
	}

}
