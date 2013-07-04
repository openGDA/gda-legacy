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

package gda.gui.epics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ActionEventRunner Class
 */
public class ActionEventRunner implements java.awt.event.ActionListener {
	
	private static final Logger logger = LoggerFactory.getLogger(ActionEventRunner.class);
	
	/**
	 *
	 */
	public class EventDispatcher implements Runnable {
		private final java.awt.event.ActionEvent e;

		private EventDispatcher(java.awt.event.ActionEvent e) {
			this.e = e;
		}

		@Override
		public void run() {
			try {
				observer.run(this, e);
			} catch (Exception expt) {
				logger.error(toString() + " " + expt.getMessage());
			}
		}
	}

	private final ActionEventRunnerObserver observer;

	/**
	 * @param observer
	 */
	public ActionEventRunner(ActionEventRunnerObserver observer) {
		this.observer = observer;
	}

	@Override
	public void actionPerformed(java.awt.event.ActionEvent e) {
		uk.ac.gda.util.ThreadManager.getThread(new EventDispatcher(e)).start();
	}
}
