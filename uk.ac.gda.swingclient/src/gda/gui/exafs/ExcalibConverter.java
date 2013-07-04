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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Converts modern 'one-line' SRS files to the format required by EXCALIB
 */
public class ExcalibConverter {
	private static final Logger logger = LoggerFactory.getLogger(ExcalibConverter.class);

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ExcalibConverter ec = new ExcalibConverter();
		if (args.length == 2) {
			ec.convert(args[0], args[1]);
		} else {
			logger.debug("There should be two arguments: input_file_name output_file_name");

		}
	}

	/**
	 * Constructor
	 */
	public ExcalibConverter() {

	}

	/**
	 * The actual converter. Uses the traditional BFI method. Only works for 9.3 data in its current form.
	 * 
	 * @param fileName
	 * @param outputFileName
	 */
	public void convert(String fileName, String outputFileName) {
		BufferedReader bufferedReader;
		BufferedWriter bufferedWriter;
		String line;
		StringTokenizer strTok;
		boolean firstPoint = true;
		double[] values = new double[1];
		double xValue = 0.0;
		double yValue = 0.0;
		boolean inSRSHeader = false;
		// double previousXValue = Double.POSITIVE_INFINITY;
		int howManyYValues = -1;
		NumberFormat nf = NumberFormat.getInstance();

		nf.setMaximumFractionDigits(2);
		nf.setGroupingUsed(false);
		try {
			bufferedReader = new BufferedReader(new FileReader(fileName));
			bufferedWriter = new BufferedWriter(new FileWriter(outputFileName));
			while ((line = bufferedReader.readLine()) != null) {
				try {
					strTok = new StringTokenizer(line, " ,\t");
					// Station 9.3 data should be in the form:
					// bragg time I0 It Im nine_fluorescence_detectors
					// encoder
					if (firstPoint) {
						if (strTok.countTokens() > 1) {
							howManyYValues = strTok.countTokens() - 1;
							values = new double[howManyYValues];
						}
					}
					xValue = Double.valueOf(strTok.nextToken()).doubleValue();

					int j = 1;
					while (strTok.hasMoreTokens()) {
						yValue = Double.valueOf(strTok.nextToken()).doubleValue();
						values[j - 1] = yValue;
						j++;
					}
					// Fluorescence
					if (howManyYValues == 14) {
						double iftot = 0.0;

						for (int k = 0; k < 9; k++)
							iftot += values[k];

						// xValue sorting commented out because it doesn't work
						// for reverse scans.
						// Needs further thought.
						// if (xValue < previousXValue)
						{
							// Write out in Excalib type 6 format
							bufferedWriter.write("" + xValue + " " + values[10] + " " + values[11] + " " + values[12]
									+ " " + nf.format(iftot) + " " + values[13] + "\n");
							bufferedWriter.write("               " + values[0] + " " + values[1] + " " + values[2]
									+ " " + values[3] + "\n");
							bufferedWriter.write("               " + values[4] + " " + values[5] + " " + values[6]
									+ " " + values[7] + "\n");
							bufferedWriter.write("               " + values[8] + "\n");
							// previousXValue = xValue;
						}
						/*
						 * else { logger.debug("DUPLICATE or NON-DECREASING xvalue " + xValue + " " + previousXValue); }
						 */
					}
					// Transmission
					else {
						// xValue sorting commented out because it doesn't work
						// for reverse scans.
						// Needs further thought.
						// if (xValue < previousXValue)
						{
							// Write out in Excalib type 6 format
							bufferedWriter.write("" + xValue + " " + values[1] + " " + values[2] + " " + values[3]
									+ " " + " " + values[4] + "\n");
							// previousXValue = xValue;
						}
						/*
						 * else { logger.debug("DUPLICATE or NON-DECREASING xvalue " + xValue + " " + previousXValue); }
						 */
					}
				}
				// A NumberFormatException indicates that the line is a comment
				// line
				catch (NumberFormatException nfe) {
					// Message.alarm("Comment:" + line);
					if (line.contains("&SRS")) {
						inSRSHeader = true;
					}
					if (inSRSHeader) {
						bufferedWriter.write(line + "\n");
						if (line.contains("&END")) {
							inSRSHeader = false;
						}
					}
				}
			}
			bufferedReader.close();
			bufferedWriter.close();
		} catch (IOException ioe) {
			logger.error("IOException in loadAndPlotData: " + ioe.getMessage());
		}

	}
}
