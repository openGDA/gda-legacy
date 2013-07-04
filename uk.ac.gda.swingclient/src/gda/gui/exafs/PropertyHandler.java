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

package gda.gui.exafs;

import gda.configuration.properties.LocalProperties;


import java.awt.Font;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Deals with all the properties which ExafsGUI expects to be able to get from java.properties file.
 */

public class PropertyHandler {
	/**
	 * Min energy in keV
	 */
	public static final String GDA_EXAFS_MIN_ENERGY = "gda.exafs.minEnergy";
	
	/**
	 * Max energy in keV
	 */
	public static final String GDA_EXAFS_MAX_ENERGY = "gda.exafs.maxEnergy";
	
	/**
	 * list of edges to be considered. If not set then use all. e.g. "L1 L2" DO NOT USE commas
	 */
	public static final String GDA_EXAFS_EDGE_LIST = "gda.exafs.edgeList";
	
	private static final Logger logger = LoggerFactory.getLogger(PropertyHandler.class);

	private static double minimumEdgeEnergy;

	private static double maximumEdgeEnergy;
	
	private static String edgeList=null;

	private static Font buttonFont;

	private static Font symbolFont;

	private static Font numberFont;

	public static boolean isEdgeListed(String edge){
		return edgeList==null ? true : edgeList.contains(edge);
	}
	static {
		String s;

		try {
			s = LocalProperties.get(GDA_EXAFS_MIN_ENERGY);
			if (s == null) {
				throw new IllegalArgumentException("Property " + GDA_EXAFS_MIN_ENERGY + " not found");
			}
			Double d = new Double(s);
			minimumEdgeEnergy = d.doubleValue();
			logger.debug("minimumEdgeEnergy is " + minimumEdgeEnergy);

			s = LocalProperties.get(GDA_EXAFS_MAX_ENERGY);
			if (s == null) {
				throw new IllegalArgumentException("Property " + GDA_EXAFS_MAX_ENERGY + " not found");
			}
			d = new Double(s);
			maximumEdgeEnergy = d.doubleValue();
			logger.debug("maximumEdgeEnergy is " + maximumEdgeEnergy);

			// The asbolute minimum energy has wavelength equal to 2d, the
			// entirely
			// arbitrary 0.25 is the amount
			// that the default kspace scan subtracts from the edge. This is
			// meant
			// to protect from incorrect
			// setting of the minimumEdgeEnergy causing NaNs
			/*
			 * double absoluteMinEnergy = Converter.convert(twoD, Converter.ANGSTROM, Converter.KEV); if
			 * (minimumEdgeEnergy - 0.25 < absoluteMinEnergy) { logger.debug("WARNING minimumEdgeEnergy reset from " +
			 * minimumEdgeEnergy + " to " + absoluteMinEnergy); minimumEdgeEnergy = absoluteMinEnergy + 0.25; }
			 */
			s = LocalProperties.get("gda.exafs.buttonFontSize");
			buttonFont = new Font("Helvetica", Font.BOLD, (new Integer(s)).intValue());

			symbolFont = buttonFont;
			numberFont = buttonFont;
			
			
			edgeList = LocalProperties.get(GDA_EXAFS_EDGE_LIST,null);
		} catch (NumberFormatException nfex) {
			logger.error("NumberFormatException in PropertyHandler", nfex);
		} catch (Throwable e) {
			logger.error("Exception", e);
		}
		
	}

	/**
	 * @return minimumEdgeEnergy
	 */
	public static double getMinimumEdgeEnergy() {
		return minimumEdgeEnergy;
	}

	/**
	 * @return maximumEdgeEnergy
	 */
	public static double getMaximumEdgeEnergy() {
		return maximumEdgeEnergy;
	}

	/**
	 * @return buttonFont
	 */
	public static Font getButtonFont() {
		return buttonFont;
	}

	/**
	 * @return symbolFont
	 */
	public static Font getSymbolFont() {
		return symbolFont;
	}

	/**
	 * @return numberFont
	 */
	public static Font getNumberFont() {
		return numberFont;
	}
}
