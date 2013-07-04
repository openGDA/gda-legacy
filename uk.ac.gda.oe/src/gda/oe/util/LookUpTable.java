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

package gda.oe.util;

import gda.jscience.physics.units.NonSIext;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.jscience.physics.quantities.Angle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This contains the information needed to calculate required positions of DOFs for a give energy and polarization. (It
 * is assumed that there is one lookup table per harmonic) Should be generalized to be useful for any list of DOFs.
 * 
 * @see gda.oe.util.UndulatorMoveCalculator
 */
public class LookUpTable {
	private static final Logger logger = LoggerFactory.getLogger(LookUpTable.class);

	// private static int ENERGY = 0;
	// private static int GAP = 1;
	// private static int LOWER = 2;
	// private static int UPPER = 3;

	private ArrayList<PolarizationEntry> perPolarization = new ArrayList<PolarizationEntry>();

	private VariablePolarizationList variablePolarization = new VariablePolarizationList();

	/**
	 * VariablePolarizationList is an ArrayList<VariablePolarizationEntry> of VariablePolarizationEntries arranged by
	 * their AngleValue.
	 */
	private class VariablePolarizationList extends ArrayList<VariablePolarizationEntry> implements
			Comparator<VariablePolarizationEntry> {
		private double[] calculate(Angle polarization, double energy) {
			double[] rtrn = { 45.0, -10.0 };
			double angle = polarization.to(NonSIext.DEG_ANGLE).getAmount();

			// search the list of polarization entries for the two which
			// bracket angle
			Iterator<VariablePolarizationEntry> i = listIterator();
			Iterator<VariablePolarizationEntry> j = listIterator(1);

			VariablePolarizationEntry before = null;
			VariablePolarizationEntry after = null;

			if (angle <= get(0).getAngle()) {
				// Use the first two entries
				before = get(0);
				after = get(1);
			} else if (angle >= get(size() - 1).getAngle()) {
				// Use the last two entries
				before = get(size() - 2);
				after = get(size() - 1);
			} else {
				for (; i.hasNext() && j.hasNext();) {
					before = i.next();
					after = j.next();

					if (angle > before.getAngle() && angle < after.getAngle())
						break;
				}
			}

			if (before != null && after != null) {
				double[] beforeSet = before.getPolarizationEntry().calculate(energy);
				double[] afterSet = after.getPolarizationEntry().calculate(energy);

				logger.debug("angle, before.getAngle(), after.getAngle() " + angle + " " + before.getAngle() + " "
						+ after.getAngle());

				double factor = (angle - before.getAngle()) / (after.getAngle() - before.getAngle());

				for (int k = 0; k < 2; k++) {
					rtrn[k] = beforeSet[k] + (afterSet[k] - beforeSet[k]) * factor;
				}
			}
			return rtrn;
		}

		/**
		 * Gets the DOFNames connected with this table.
		 * 
		 * @return an array of DOFNames
		 */
		public String[] getDOFNames() {
			return get(0).getPolarizationEntry().getDOFNames();
		}

		/**
		 * Implements the Comparator interface, the return values are calculated so that the list gets sorted into
		 * increasing angle order.
		 * 
		 * @param o1
		 *            the first VariablePolarizationEntry
		 * @param o2
		 *            the second VariablePolarizationEntry to compare with the first
		 * @return the result of the compare. Zero if equal.
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(VariablePolarizationEntry o1, VariablePolarizationEntry o2) {
			int rtrn;
			if (o1.getAngle() > o2.getAngle())
				rtrn = 1;
			else if (o1.getAngle() < o2.getAngle())
				rtrn = -1;
			else
				rtrn = 0;
			return rtrn;
		}

		/**
		 * Sorts the list, VariablePolarizationEntry implements Comparator so appears both as the List and the
		 * Comparator
		 */
		public void order() {
			Collections.sort(this, this);
		}
	}

	/**
	 * A VariablePolarizationEntry is a PolarizationEntry with a specifying Angle
	 */
	private class VariablePolarizationEntry {
		private double angle;

		private PolarizationEntry pe;

		private VariablePolarizationEntry(double angle, PolarizationEntry pe) {
			this.angle = angle;
			this.pe = pe;
		}

		private double getAngle() {
			return angle;
		}

		private PolarizationEntry getPolarizationEntry() {
			return pe;
		}
	}

	/**
	 * A PolarizationEntry is an ArrayList<TableEntry> in which each item is a TableEntry
	 */
	private class PolarizationEntry extends ArrayList<TableEntry> implements Comparator<TableEntry> {
		private String dofOne;

		private String dofTwo;

		private PolarizationEntry(String dofOne, String dofTwo) {
			logger.debug("Creating a PolarizationEntry with dofs: " + dofOne + " and " + dofTwo);
			this.dofOne = dofOne;
			this.dofTwo = dofTwo;
		}

		/**
		 * Calculates the DOF positions for a given energy
		 * 
		 * @param energy
		 *            the required energy (assumed to be in the same units as in the table)
		 * @return double array containing the positions
		 */
		private double[] calculate(double energy) {
			double[] rtrn = new double[2];

			TableEntry before = null;
			TableEntry after = null;

			if (energy <= get(0).getValue(0)) {
				// Use the first two entries
				before = get(0);
				after = get(1);
			} else if (energy >= get(size() - 1).getValue(0)) {
				// Use the last two entries
				before = get(size() - 2);
				after = get(size() - 1);
			} else {
				// Search the list of table entries for the two which
				// bracket energy
				Iterator<TableEntry> i = listIterator();
				Iterator<TableEntry> j = listIterator(1);

				for (; i.hasNext() && j.hasNext();) {
					before = i.next();
					after = j.next();

					if (energy >= before.getValue(0) && energy < after.getValue(0))
						break;
				}
			}

			if (before != null && after != null) {

				double factor = (energy - before.getValue(0)) / (after.getValue(0) - before.getValue(0));

				for (int k = 0; k < 2; k++) {
					rtrn[k] = before.getValue(k + 1) + (after.getValue(k + 1) - before.getValue(k + 1)) * factor;
					logger.debug("rtrn[" + k + "] is " + rtrn[k]);
				}
			}
			return rtrn;
		}

		private String[] getDOFNames() {
			String[] rtrn = { dofOne, dofTwo };
			return rtrn;
		}

		/**
		 * Sorts the list, PolarizationEntry implements Comparator so appears both as the List and the Comparator
		 */
		public void order() {
			Collections.sort(this, this);
		}

		/**
		 * Implements the Comparator interface, the return values are calculated so that the list gets sorted into
		 * increasing energy order.
		 * 
		 * @param o1
		 *            the first TableEntry
		 * @param o2
		 *            the second TableEntry to compare with the first
		 * @return the result of the compare. Zero if equal.
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(TableEntry o1, TableEntry o2) {
			int rtrn;
			if (o1.getValue(0) > o2.getValue(0))
				rtrn = 1;
			else if (o1.getValue(0) < o2.getValue(0))
				rtrn = -1;
			else
				rtrn = 0;
			return rtrn;
		}

		/**
		 * @param gap
		 * @return double[]
		 */
		public double[] reverseCalculate(double gap) {
			double[] rtrn = new double[2];

			TableEntry before = null;
			TableEntry after = null;

			if (gap <= get(0).getValue(1)) {
				// Use the first two entries
				before = get(0);
				after = get(1);
			} else if (gap >= get(size() - 1).getValue(1)) {
				// Use the last two entries
				before = get(size() - 2);
				after = get(size() - 1);
			} else {
				// Search the list of table entries for the two which
				// bracket energy
				Iterator<TableEntry> i = listIterator();
				Iterator<TableEntry> j = listIterator(1);

				for (; i.hasNext() && j.hasNext();) {
					before = i.next();
					after = j.next();

					if (gap > before.getValue(1) && gap < after.getValue(1))
						break;
				}
			}
			if (before != null && after != null) {
				double factor = (gap - before.getValue(1)) / (after.getValue(1) - before.getValue(1));

				for (int k = 0; k <= 2; k += 2) {
					int index = (k + 1) / 2;
					rtrn[index] = before.getValue(k) + (after.getValue(k) - before.getValue(k)) * factor;
					logger.debug("rtrn[" + index + "] is " + rtrn[index]);
				}
			}
			return rtrn;
		}
	}

	/**
	 * TableEntry is really just a way to create an array of 3 doubles from a String and so its existence as a class is
	 * possibly a bit OTT.
	 */
	private class TableEntry {
		private double[] values = new double[3];

		private TableEntry(String readFromFile) {
			logger.debug("creating TableEntry from:" + readFromFile);

			StringTokenizer strtok = new StringTokenizer(readFromFile);

			for (int i = 0; i < 3; i++)
				values[i] = Double.valueOf(strtok.nextToken()).doubleValue();
		}

		/**
		 * Returns one of the values
		 * 
		 * @param index
		 *            the index
		 * @return the value at that index
		 */
		private double getValue(int index) {
			return values[index];
		}
	}

	/**
	 * @param fileName
	 */
	public LookUpTable(String fileName) {
		String inputString;
		double angle;
		StringTokenizer strtok;

		try {

			BufferedReader in = new BufferedReader(new FileReader(fileName));

			int counter = 0;
			PolarizationEntry pe;

			do {
				do {
					inputString = in.readLine();
				} while (inputString.startsWith("#"));

				logger.debug("Polarization type would be: " + inputString);
				inputString = in.readLine();
				strtok = new StringTokenizer(inputString);
				strtok.nextToken();

				pe = new PolarizationEntry(strtok.nextToken(), strtok.nextToken());

				inputString = in.readLine();
				inputString = in.readLine();
				do {
					// The last line read in the previous loop should be the
					// first
					// line of data for this loop
					pe.add(new TableEntry(inputString));
					inputString = in.readLine();
				} while (!inputString.startsWith("#"));

				pe.order();
				logger.debug("adding a polarization entry to the list " + counter);
				perPolarization.add(pe);

				counter++;

			} while (counter < 7);

			// Now should be the stuff for general angles
			do {
				inputString = in.readLine();
			} while (inputString.startsWith("#"));

			logger.debug("Polarization type would be: " + inputString);
			inputString = in.readLine();
			strtok = new StringTokenizer(inputString);
			strtok.nextToken();
			String dofOne = strtok.nextToken();
			String dofTwo = strtok.nextToken();
			pe = new PolarizationEntry(dofOne, dofTwo);

			inputString = in.readLine();
			inputString = in.readLine();
			do {
				strtok = new StringTokenizer(inputString);
				angle = Double.valueOf(strtok.nextToken()).doubleValue();

				do {
					inputString = in.readLine();
				} while (inputString.startsWith("#"));

				pe = new PolarizationEntry(dofOne, dofTwo);
				do {
					// The last line read in the previous loop should be the
					// first line of data for this loop
					pe.add(new TableEntry(inputString));
					inputString = in.readLine();
				} while (!(inputString == null || inputString.startsWith("#")));
				pe.order();
				variablePolarization.add(new VariablePolarizationEntry(angle, pe));
				if (inputString == null) {
					logger.debug("added entry for " + angle + " with no inputString");
				} else {
					logger.debug("added entry for " + angle + " inputString is " + inputString);
				}
				if (inputString == null)
					break;
				do {
					inputString = in.readLine();
				} while (inputString.startsWith("#"));
			} while (true);

			variablePolarization.order();
		} catch (FileNotFoundException fnfe) {
			logger.error("LookupTable: FileNotFoundException " + fnfe.getMessage());
		} catch (IOException ioe) {
			logger.error("LookupTable: IOException " + ioe.getMessage());
		}

	}

	/**
	 * Calculates the required DOF positions for the given energy and polarization
	 * 
	 * @param energy
	 *            the energy
	 * @param polarization
	 *            the polarization
	 * @return double array containing the required positions
	 */
	public double[] calculateValues(double energy, Angle polarization) {

		// find correct polarization entry

		int index = convertToPolarizationType(polarization);

		// Polarization types go from 1 (variable) to 8, the index in
		// the perPolarization arrayList has type 2 at index 0 so...
		index = index - 2;
		logger.debug("LookUpTable calculateValues choosing polarization index " + index);

		if (index == -1) {
			return variablePolarization.calculate(polarization, energy);
		}
		return perPolarization.get(index).calculate(energy);
	}

	/**
	 * @param gap
	 * @param polarization
	 * @return double[]
	 */
	public double[] reverseCalculateValues(double gap, Angle polarization) {
		// find correct polarization entry

		int index = convertToPolarizationType(polarization);

		// Polarization types go from 1 (variable) to 8, the index in
		// the perPolarization arrayList has type 2 at index 0 so...
		index = index - 2;
		logger.debug("LookUpTable calculateValues choosing polarization index " + index);

		if (index == -1) {
			// FIXME: return something calculated by variablePolarization
			return null;
		}
		return perPolarization.get(index).reverseCalculate(gap);
	}

	/**
	 * Returns the names of the DOFs which must be positioned to get a particular polarization
	 * 
	 * @param polarization
	 *            the specified polarization
	 * @return String array containing the DOF names
	 */
	public String[] getDofNames(Angle polarization) {

		// find correct polarization entry

		int index = convertToPolarizationType(polarization);

		// Polarization types go from 1 (variable) to 8, the index in
		// the perPolarization arrayList has type 2 at index 0 so...
		index = index - 2;
		logger.debug("LookUpTable calculateValues choosing polarization index " + index);

		if (index == -1) {
			return variablePolarization.getDOFNames();
		}
		return perPolarization.get(index).getDOFNames();
	}

	/**
	 * Converts the currentQuantity into one of the fixed types
	 * 
	 * @param toConvert
	 *            an angle representing a Polarization type
	 * @return the integer equivalent
	 */
	public static int convertToPolarizationType(Angle toConvert) {
		int rtrn = -1;
		double value = toConvert.to(NonSIext.DEG_ANGLE).getAmount();

		rtrn = ((int) Math.round(value)) / 180;
		if (rtrn == 0)
			rtrn = 1;

		logger.debug("convertToPolarizationType returning " + rtrn);

		return rtrn;

	}
}