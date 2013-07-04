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

import gda.data.srs.SrsFile;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extends gda.data.srs.SrsFile to provide the ability to read the data as double[] instead of Vector<String> to help
 * with speed problems when plotting.
 */

public class SrsDoubleFile extends SrsFile {
	private static final Logger logger = LoggerFactory.getLogger(SrsDoubleFile.class);

	/**
	 * Read a SRS file.
	 * 
	 * @param filename
	 *            The filename to read.
	 * @return The SRS buffer.
	 * @throws IOException
	 */
	public SrsDoubleBuffer readDoubleFile(String filename) throws IOException {
		BufferedReader in = null;
		SrsDoubleBuffer buf = null;
		try {
			in = new BufferedReader(new FileReader(filename));
			buf = this.readDoubleStream(in);
		} catch (FileNotFoundException e) {
			logger.error("ERROR: Could not open file to read in SrsDoubleFile#readFile");
		} finally {
			if (in != null) {
				in.close();
			}
		}

		return buf;
	}

	/**
	 * @param in
	 *            The input stream BufferedReader
	 * @return The SRS buffer
	 */
	public SrsDoubleBuffer readDoubleStream(BufferedReader in) {
		// Read the header.
		String line = null;
		String headerString = new String();
		int limit = 10000;
		int count = 0;
		do {
			try {
				line = in.readLine();
				// Message.debug("header: " + line);
			} catch (IOException e) {
				logger.error("ERROR: Could not read from SRS stream in SrsDoubleFile.");
				break;
			}
			if ((!line.contains("&SRS")) && (count == 0)) {
				break;
			}
			// System.err.println("header: " + line);
			headerString = headerString.concat(line + '\n');
			count++;
		} while ((!line.contains("&END")) && (count < limit));

		// Add the header to the SrsBuffer.
		SrsDoubleBuffer buf = new SrsDoubleBuffer();
		// System.err.println("header: " + headerString);
		buf.setHeader(headerString);

		// Read the data variables
		Vector<String> vars = null;
		// If the first line read was the variable line, then we don't need to
		// read another line.
		if (line != null) {
			if ((!line.contains("&SRS")) && (count == 0)) {
				vars = this.searchForTabbedElements(line);
			} else {
				try {
					line = in.readLine();
				} catch (IOException e) {
					logger.error(("ERROR: Could not read variable line from SRS stream in SrsDoubleFile."));
				}
				vars = this.searchForTabbedElements(line);
			}
		}

		// Now read the data lines
		Vector<double[]> data = new Vector<double[]>();
		while (true) {
			try {
				line = in.readLine();
				// Message.debug("line: " + line);
			} catch (IOException e) {
				logger.error(("ERROR: Could not read data line from SRS stream in SrsDoubleFile."));
			}
			if (line == null) {
				break;
			}
			data.add(this.searchForTabbedDoubleElements(line));
		}

		double[] values;
		if (vars != null) {
			for (int i = 0; i < vars.size(); i++) {
				values = new double[data.size()];
				for (int datasize = 0; datasize < data.size(); datasize++) {
					values[datasize] = data.elementAt(datasize)[i];
				}
				buf.setData(vars.elementAt(i), values);
			}
		}

		// Now read any trailer.
		try {
			line = in.readLine();
		} catch (IOException e) {
			logger.debug(("ERROR: Could not read trailer line from SRS stream in SrsDoubleFile."));
		}
		if (line != null) {
			buf.setTrailer(line);
		}

		return buf;
	}

	/**
	 * Method which reads all the tabbed separated values from a String.
	 * 
	 * @param line
	 * @return The found eleemnts in a Vector<String>
	 */
	private Vector<String> searchForTabbedElements(String line) {
		Vector<String> vars = new Vector<String>();

		// Check the line
		if ((line == null) || (line.length() == 0)) {
			return null;
		}
		// Add an end-of-line
		String newLine = line + '\n';

		char ch = '0';
		int index = 0;
		StringBuffer var = new StringBuffer(newLine.length());
		// Get the first character
		ch = newLine.charAt(index);
		var.append(ch);
		// Loop to get the next characters, and test to find tabs and
		// end-of-lines.
		while (true) {
			index++;
			if ((ch == '\t') || (ch == '\n')) {
				vars.add(var.substring(0, var.length() - 1).toString());
				var.delete(0, var.length());
			}
			// System.err.println("var: " + var);
			if (index == line.length()) {
				vars.add(var.substring(0, var.length()).toString());
				break;
			}
			ch = newLine.charAt(index);
			var.append(ch);
		}

		// Get rid of any excess tabs.
		for (int i = 0; i < vars.size(); i++) {
			if (vars.elementAt(i).trim().length() == 0) {
				vars.remove(i);
			}
		}

		// System.err.println("vars: " + vars);

		return vars;
	}

	/**
	 * Method which reads all the tabbed separated values from a String.
	 * 
	 * @param line
	 * @return The found eleemnts in a Vector<String>
	 */
	private double[] searchForTabbedDoubleElements(String line) {
		double[] values = null;

		if ((line != null) && (line.length() != 0)) {
			String[] stringValues = line.split("\t");
			values = new double[stringValues.length];
			for (int i = 0; i < values.length; i++) {
				values[i] = Double.valueOf(stringValues[i]);
			}
		}
		return values;
	}

}
