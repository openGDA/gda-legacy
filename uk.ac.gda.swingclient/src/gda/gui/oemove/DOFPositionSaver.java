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

package gda.gui.oemove;

import gda.factory.Finder;
import gda.oe.MoveableException;
import gda.oe.OE;

import java.io.PrintStream;
import java.util.ArrayList;

/**
 * To change the template for this generated type comment go to Window - Preferences - Java - Code Generation - Code and
 * Comments
 */
public class DOFPositionSaver {

	/**
	 * Saves the names and positions of all DOFs to a PrintStream
	 */

	public static void save() {
		save(System.out);
	}

	/**
	 * Saves the names and positions of all DOFs to a PrintStream
	 * 
	 * @param out
	 *            PrintStream to write to
	 */
	public static void save(PrintStream out) {
		ArrayList<String> oeNames = null;

		Finder finder = Finder.getInstance();
		oeNames = finder.listAllNames("OE");
		// for each OE name find tha actual OE from the Finder:
		// get its dofNames
		// for each dofName get the position of that DOF and write it out
		for (String oeName : oeNames) {
			String[] dofNames;
			int j;
			OE oe = (OE) finder.find(oeName);
			if (oe != null) {
				out.println("OE:" + oeName);
				dofNames = oe.getDOFNames();
				for (j = 0; j < dofNames.length; j++) {
					try {
						out.println("  DOF: " + dofNames[j] + " position: " + oe.getPosition(dofNames[j]));
					} catch (MoveableException e) {
						out.println("  DOF: " + dofNames[j] + " position: " + " ERROR ");
					}
				}
			}
		}
		out.close();
	}

}
