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

package gda.gui;

import java.awt.Color;
import javax.swing.JLabel;
import java.text.DecimalFormat;

/**
 * GUILabelBase Class
 */
public class GUILabelBase extends JLabel implements GUILabel {

	private double threshold = 0, value = 0, thresholdWidth = 0;

	private boolean aboveThresholdIsGood = true, belowThresholdIsGood = false, atThresholdIsGood = true;

	private Color goodColor = new Color(0, 200, 0), badColor = Color.RED, otherColor = Color.RED; // Green has been
	// modified from
	// 0,255,0

	// because this was too bright

	private String borderTitle = "Title";

	private DecimalFormat form = new DecimalFormat("00.00");

	private SpecialText specialText;

	// case of selector, use the known values of those. for int, use a different
	// number format... and so on...
	class SpecialText { // TODO allow the programmer to include other ways to
		// interpret data here. How to do this exactly, I'm not
		// sure
		String getText(double value) {
			int intValue = (int) value;
			switch (intValue) {
			case 0:
				return "Open";
			case 1:
				return "Closed";
			case 2:
				return "Reset";
			default:
				return "Unknown";
			}
		}
	}

	/**
	 * Constructor
	 */
	public GUILabelBase() {
		super();
		this.setBorder(javax.swing.BorderFactory.createTitledBorder(borderTitle));
		this.setText("Unknown");
		this.setForeground(otherColor);
		this.specialText = new SpecialText();
	}

	@Override
	public Color getBadColor() {
		return badColor;
	}

	@Override
	public String getBorderTitle() {
		return borderTitle;
	}

	@Override
	public DecimalFormat getFormat() {
		return form;
	}

	@Override
	public Color getGoodColor() {
		return goodColor;
	}

	@Override
	public Color getOtherColor() {
		return otherColor;
	}

	@Override
	public boolean isGood() {
		if ((value > threshold) & aboveThresholdIsGood) {
			return true;
		} else if ((value == threshold) & atThresholdIsGood) {
			return true;
		} else if ((value < threshold) & belowThresholdIsGood) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void aboveThresholdIsGood(boolean value) {
		aboveThresholdIsGood = value;
	}

	@Override
	public void atThresholdIsGood(boolean value) {
		atThresholdIsGood = value;
	}

	@Override
	public void setBadColor(Color newBadColor) {
		badColor = newBadColor;

	}

	@Override
	public void belowThresholdIsGood(boolean value) {
		belowThresholdIsGood = value;

	}

	@Override
	public void setBorderTitle(String newBorderTitle) {
		borderTitle = newBorderTitle;
		this.setBorder(javax.swing.BorderFactory.createTitledBorder(borderTitle));

	}

	/**
	 * @param defaultValue
	 */
	public void setDefaultValue(double defaultValue) {
		value = defaultValue;

	}

	@Override
	public void setFormat(DecimalFormat newForm) {
		form = newForm;

	}

	@Override
	public void setGoodColor(Color newGoodColor) {
		goodColor = newGoodColor;

	}

	@Override
	public void setOtherColor(Color newOtherColor) {
		otherColor = newOtherColor;

	}

	@Override
	public void setThreshold(double newThreshold) {
		threshold = newThreshold;

	}

	@Override
	public double getThreshold() {
		return threshold;
	}

	@Override
	public void setThresholdWidth(double newThresholdWidth) {
		thresholdWidth = newThresholdWidth;
	}

	@Override
	public double getThresholdWidth() {
		return thresholdWidth;
	}

	@Override
	public void setValue(double newValue) {
		value = newValue;
		this.setText(form.format(value));
		this.recolor();
	}

	/**
	 * @param newValue
	 */
	public void setSpecialValue(double newValue) {
		value = newValue;
		this.setText(this.specialText.getText(value));
		this.recolor();
	}

	@Override
	public double getValue() {
		return value;
	}

	/**
	 * 
	 */
	public void recolor() {
		if (isGood()) {
			this.setForeground(goodColor);
		} else {
			this.setForeground(badColor);
		}
	}
}