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
import java.text.DecimalFormat;

interface GUILabel {
	/**
	 * @return boolean
	 */
	public boolean isGood();

	// public void setDefault();
	/**
	 * @param value
	 */
	public void aboveThresholdIsGood(boolean value);

	/**
	 * @param value
	 */
	public void atThresholdIsGood(boolean value);

	/**
	 * @param value
	 */
	public void belowThresholdIsGood(boolean value);

	/**
	 * @param threshold
	 */
	public void setThreshold(double threshold);

	/**
	 * @return threshold
	 */
	public double getThreshold();

	/**
	 * @param thresholdWidth
	 */
	public void setThresholdWidth(double thresholdWidth);

	/**
	 * @return threshold width
	 */
	public double getThresholdWidth();

	/**
	 * @param goodColor
	 */
	public void setGoodColor(Color goodColor);

	/**
	 * @return good colour
	 */
	public Color getGoodColor();

	/**
	 * @param badColor
	 */
	public void setBadColor(Color badColor);

	/**
	 * @return bad colour
	 */
	public Color getBadColor();

	/**
	 * @param otherColor
	 */
	public void setOtherColor(Color otherColor);

	/**
	 * @return other colour
	 */
	public Color getOtherColor();

	/**
	 * @param form
	 */
	public void setFormat(DecimalFormat form);

	/**
	 * @return format
	 */
	public DecimalFormat getFormat();

	/**
	 * @param borderTitle
	 */
	public void setBorderTitle(String borderTitle);

	/**
	 * @return border title
	 */
	public String getBorderTitle();

	/**
	 * @param value
	 */
	public void setValue(double value);

	/**
	 * @return value
	 */
	public double getValue();
}