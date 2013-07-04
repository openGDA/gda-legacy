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

package gda.util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A utility class which repeatedly reads a file searching for a required string in that file. The file is checked once
 * every second, until a timeout expires. If the string is found at any point up till then, the program exits with a
 * status code of 0. Otherwise the program exits with a non zero status code. Run as a main program or via a public
 * static method. args[0] - String to search for. args[1] - File to search in. args[2] - Timeout in mSeconds.
 */

public class StringInFile {
	private static final Logger logger = LoggerFactory.getLogger(StringInFile.class);

	/**
	 * 
	 */
	public static boolean timeoutExpired = false;

	/**
	 * Main Method.s
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		int timeout = 0;
		boolean found = false;
		boolean showWaitWindow = true;

		try {
			if (args.length < 3) {
				logger.debug("Usage: java gda.unit.StringInFile string file timeout showWaitWindow(default is true)");
			}

			else {
				if (args.length == 4) {
					showWaitWindow = Boolean.parseBoolean(args[3]);
				}
				if (showWaitWindow) {
					PleaseWaitWindow plw = new PleaseWaitWindow("Please wait...");
					plw.setVisible(true);
				}
				timeout = Integer.parseInt(args[2]);
				found = searchFile(args[0], args[1], timeout);
			}
		} catch (NumberFormatException nfex) {
			logger.debug("Error in timeout value " + args[2]);
		}

		if (found)
			System.exit(0);
		else
			System.exit(1);
	}

	/**
	 * @param string
	 * @param file
	 * @param timeout
	 * @return boolean
	 */
	public static boolean searchFile(String string, String file, int timeout) {
		boolean found = false;
		String line;

		Date timeToRun = new Date(System.currentTimeMillis() + timeout);
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				timeoutExpired = true;
			}
		}, timeToRun);

		try {
			while (!timeoutExpired && !found) {
				BufferedReader in = new BufferedReader(new FileReader(file));
				while ((line = in.readLine()) != null && !found) {
					if (line.indexOf(string) != -1)
						found = true;
				}
				in.close();
				if (!found)
					Sleep.sleep(1000);
			}
		} catch (FileNotFoundException e) {
			logger.warn("File " + file + " not found by StringInFile.");
		} catch (IOException e1) {
			logger.warn("Error reading file " + file);
		}

		return found;
	}
}