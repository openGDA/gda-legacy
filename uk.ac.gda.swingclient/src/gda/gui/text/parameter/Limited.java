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

import gda.gui.text.validator.Confirmer;
import gda.gui.text.validator.Validator;

import java.awt.Component;

import javax.swing.JFormattedTextField;

/**
 * Encapsulates all the information about a variable represented by a double to allow it to be added to JPanel in an
 * easy to use way. It contains limits units, formatString and other useful items.
 * 
 */

public class Limited {
	/**
	 * The value of the variable
	 */
	Object val;

	/**
	 * The minimum value of the variable
	 */
	Object min;

	/**
	 * The maximum value of the variable
	 */
	Object max;

	/**
	 * The text to be displayed as a label of the variable
	 */
	String label="";

	/**
	 * The prefix of the tool tip help
	 */
	String tooltip="";

	/**
	 * fullTooltip - this is used if not null
	 */
	String fullTooltip=null;
	
	/**
	 * The text to be displayed as the units
	 */
	String units="";

	/**
	 * The formatString used to create a SimpleFormatter used to convert the value to text and vice versa
	 */
	String formatString="";

	/**
	 * The char to be used as an accelerator key
	 */
	char displayedMnemonic=(char) 0;

	/**
	 * unique id amongst an array of Limiteds
	 */
	int id=0;

	/**
	 * 
	 */
	boolean editable=false;
	/**
	 * 
	 */
	float alignmentX=Component.RIGHT_ALIGNMENT;

	Validator validator = null;
	
	Confirmer confirmer;

	/**
	 * @return The initial value - type of val, min and max msut all be the same
	 */
	public Object getVal() {
		return val;
	}

	/**
	 * @return The min value - type of val, min and max msut all be the same
	 */
	public Object getMin() {
		return min;
	}

	/**
	 * @return The max value - type of val, min and max msut all be the same
	 */
	public Object getMax() {
		return max;
	}

	/**
	 * @return label to be displayed next to the value field
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @return  Initial part of tooltip - the limits and units are added later
	 */
	public String getTooltip() {
		return tooltip;
	}

	/**
	 * @return The units
	 */
	public String getUnits() {
		return units;
	}

	/**
	 * @return format string e.g. %10.5f
	 */
	public String getFormatString() {
		return formatString;
	}

	/**
	 * @return  default is 0 
	 */
	public char getDisplayedMnemonic() {
		return displayedMnemonic;
	}

	/**
	 * @return  used for future referencing of this field
	 */
	public int getId() {
		return id;
	}

	/**
	 * @return default is false
	 */
	public boolean isEditable() {
		return editable;
	}

	/**
	 * @return default is Component.RIGHT_ALIGNMENT;
	 */
	public float getAlignmentX() {
		return alignmentX;
	}

	/**
	 * @param min  only effective when first constructing the object
	 */
	public void setMin(Object min) {
		this.min = min;
	}

	/**
	 * @param max  only effective when first constructing the object
	 */
	public void setMax(Object max) {
		this.max = max;
	}

	/**
	 * @param label
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * @param tooltip
	 */
	public void setTooltip(String tooltip) {
		this.tooltip = tooltip;
	}

	public String getFullTooltip() {
		return fullTooltip;
	}

	public void setFullTooltip(String fullTooltip) {
		this.fullTooltip = fullTooltip;
	}

	public Validator getValidator() {
		return validator;
	}

	public void setValidator(Validator validator) {
		this.validator = validator;
	}
	
	public void setConfirmer(Confirmer confirmer) {
		this.confirmer = confirmer;
	}
	
	public Confirmer getConfirmer() {
		return confirmer;
	}

	/**
	 * @param units
	 */
	public void setUnits(String units) {
		this.units = units;
	}

	/**
	 * @param formatString
	 */
	public void setFormatString(String formatString) {
		this.formatString = formatString;
	}

	/**
	 * @param displayedMnemonic
	 */
	public void setDisplayedMnemonic(char displayedMnemonic) {
		this.displayedMnemonic = displayedMnemonic;
	}

	/**
	 * @param id
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * @param editable
	 */
	public void setEditable(boolean editable) {
		this.editable = editable;
	}

	/**
	 * @param alignmentX
	 */
	public void setAlignmentX(float alignmentX) {
		this.alignmentX = alignmentX;
	}

	/**
	 * @param initialValue  
	 */
	public Limited(Object initialValue){
		val = initialValue;
		if(val instanceof Double){
			min = -Double.MAX_VALUE;
			max = -Double.MAX_VALUE;
			formatString = "%10.5f";
		} else if( val instanceof Integer){
			min = Integer.MIN_VALUE;
			max = Integer.MAX_VALUE;
			formatString = "%d";
		} else if( val instanceof String){
			min = "";
			max = "";
			formatString = "%s";
		} else {
			min = val;
			max = val;
		}
		
	}


	/**
	 * @param id
	 * @param val
	 *            The value of the variable
	 * @param min
	 *            The minimum value of the variable
	 * @param max
	 *            The maximum value of the variable
	 * @param label
	 *            The text to be displayed as a label of the variable
	 * @param tooltip
	 *            The text to be displayed as a tooltip help
	 * @param units
	 *            The text to be displayed as the units
	 * @param formatString
	 *            The formatString used to create a SimpleFormatter used to convert the value to text and vice versa
	 * @param displayedMnemonic
	 *            The char to be used as an accelerator key
	 * @param alignmentX
	 * @param editable
	 */
	public Limited(int id, Object val, Object min, Object max, String formatString, String label, String tooltip, String units,
			char displayedMnemonic, float alignmentX, boolean editable) {
		this.id = id;
		this.val = val;
		this.min = min;
		this.max = max;
		this.formatString = formatString;
		this.label = label;
		this.tooltip = tooltip;
		this.units = units;
		this.displayedMnemonic = displayedMnemonic;
		this.alignmentX = alignmentX;
		this.editable = editable;
	}

	/**
	 * @param val
	 * @param min
	 * @param max
	 * @param formatString
	 * @param label
	 * @param tooltip
	 * @param units
	 * @param displayedMnemonic
	 * @param alignmentX
	 */
	public Limited(Object val, Object min, Object max, String formatString, String label, String tooltip, String units,
			char displayedMnemonic, float alignmentX) {
		this(0, val, min, max, formatString, label, tooltip, units, displayedMnemonic, alignmentX, true);
	}

	/**
	 * Creates a new Limited from an existing object and a new value
	 * 
	 * @param source
	 *            The existing object from which all but the value field will be extracted and used to create the new
	 *            object
	 * @param value
	 *            The value for the new object
	 * @return A new Limited object
	 */
	public static Limited setVal(Limited source, Object value) {
		if( !source.val.getClass().equals(value.getClass()))
			throw new IllegalArgumentException("classes are not the same");
		return new Limited(source.id, value, source.min, source.max, source.formatString, source.label,
				source.tooltip, source.units, source.displayedMnemonic, source.alignmentX, source.editable);
	
	}

	/**
	 * {@link JFormattedTextField#COMMIT_OR_REVERT} should be the default here - it's the default for a
	 * {@link JFormattedTextField} (see {@link JFormattedTextField#setFocusLostBehavior(int)}).
	 */
	private int focusLostBehaviour = JFormattedTextField.COMMIT_OR_REVERT;
	
	public void setFocusLostBehaviour(int behaviour) {
		this.focusLostBehaviour = behaviour;
	}
	
	public int getFocusLostBehaviour() {
		return focusLostBehaviour;
	}
	
	@Override
	public String toString() {
		return this.label + " = " + String.format(this.formatString, this.val) + this.units;
	}

}
