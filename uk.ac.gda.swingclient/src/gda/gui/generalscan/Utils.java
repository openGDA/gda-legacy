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

import java.io.File;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * utilities
 */
public class Utils {

	private static final Logger logger = LoggerFactory.getLogger(Utils.class);

	/**
	 * Check whether the specified file does not exist and report an error if it does
	 * 
	 * @param component
	 *            JComponent where to display pop-up from
	 * @param name
	 *            String name the file to check
	 * @return boolean true = file exists and error reported; false = file does not exist
	 */
	public static boolean fileExistsAndErrorReported(JComponent component, String name) {
		boolean exists = new File(name).exists();
		if (exists) {
			JOptionPane.showConfirmDialog(component, "File " + name + " exists", "File Error ",
					JOptionPane.WARNING_MESSAGE);
		}
		return exists;
	}

	/**
	 * Check whether the specified file exists and report an error if it doesn't
	 * 
	 * @param name
	 *            String name the file to check
	 * @return boolean true = file doesn't exists and error reported; false = file exists
	 */
	static boolean fileDoesNotExistAndErrorReported(JComponent component, String name) {
		Boolean exists = new File(name).exists();
		if (!exists) {
			JOptionPane.showConfirmDialog(component, "Can't find File " + name, "File Error ",
					JOptionPane.WARNING_MESSAGE);
		}
		return !exists;
	}

	/**
	 * Check the integer in str is a valid integer falling between min & max
	 * 
	 * @param component
	 *            the component from which error message pop-ups can display
	 * @param str
	 *            String representation of the number to validate
	 * @param min
	 *            int the minimum allowed number
	 * @param max
	 *            int the maximum allowed number
	 * @return boolean true if str contains valid integer else false
	 */
	public static boolean isValidInteger(JComponent component, String str, int min, int max) {
		boolean valid = false;

		String numEventsString = str;
		StreamTokenizer t = new StreamTokenizer(new StringReader(numEventsString));
		t.parseNumbers();

		try {
			t.nextToken();
		} catch (IOException e) {
			logger.error("Error number entered is not a valid integer ");
		}

		if (t.ttype == StreamTokenizer.TT_NUMBER) {
			int num = new Integer(str).intValue();
			if (num >= min && num <= max)
				valid = true;
		}

		if (!valid) {
			JOptionPane.showConfirmDialog(component, "Invalid Integer Number " + str
					+ ".\n Please enter a value between " + min + " and " + max, "Number Error ",
					JOptionPane.WARNING_MESSAGE);
		}

		return valid;
	}

	/**
	 * @param str
	 * @return a vector of doubles
	 */
	public static Vector<Double> parseDoubles(String str) {
		Vector<Double> doubleArray = new Vector<Double>();

		StreamTokenizer t = new StreamTokenizer(new StringReader(str));
		t.parseNumbers();

		try {
			while (t.nextToken() != StreamTokenizer.TT_EOF) {
				if (t.ttype == StreamTokenizer.TT_NUMBER) {
					doubleArray.add(new Double(t.nval));
				}
			}
		} catch (IOException e) {
			logger.error("Error parsing number!");
		}

		return doubleArray;
	}
}
