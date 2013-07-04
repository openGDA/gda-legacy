/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council
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

package gda.gui.dv.panels;

import java.util.LinkedList;
import java.util.ListIterator;

import javax.swing.event.ChangeListener;

/**
 * All stack based playback are covered here so no interframe movements
 */
public class PlayBackStack implements IPlayBack {

	private int frameNr = 0;
	private int oldFrameNr = 0;
	private int totalFrames = 0;
	private boolean pause = false;
	private boolean terminate = false;
	private LinkedList<ChangeListener> listeners;

	/**
	 * Constructor for Stack based playback
	 * 
	 * @param numFrames
	 *            maximum number of frames to playback
	 */
	public PlayBackStack(int numFrames) {
		totalFrames = numFrames;
		listeners = new LinkedList<ChangeListener>();
	}

	@Override
	public synchronized void addChangeListener(ChangeListener listener) {
		listeners.add(listener);
	}

	@Override
	public void cleanup() {
		listeners.clear();
	}

	@Override
	public synchronized Direction getDirection() {
		if (oldFrameNr < frameNr)
			return Direction.FORWARD;
		else if (oldFrameNr > frameNr)
			return Direction.BACKWARD;
		else
			return Direction.NONE;
	}

	@Override
	public synchronized int getFrameNr() {
		return frameNr;
	}

	@Override
	public synchronized boolean isPaused() {
		return pause;
	}

	@Override
	public synchronized void pause() {
		pause = true;
	}

	@Override
	public synchronized void resume() {
		pause = false;
		notify();
	}

	@Override
	public void run() {
		while (frameNr < totalFrames && !terminate) {

			if (pause) {
				System.out.println("PAUSING....");
				try {
					synchronized (this) {
						while (pause) {
							wait();
						}
					}
				} catch (InterruptedException ex) {
				}
			}
			oldFrameNr = frameNr;
			frameNr++;
			updateDisplay();
			try {
				Thread.sleep(500);
			} catch (InterruptedException ex) {
			}
			Thread.yield();
		}
	}

	@Override
	public synchronized void setFrameNr(int newFrameNr, boolean executeChange) {
		oldFrameNr = frameNr;
		frameNr = newFrameNr;
		if (executeChange) {
			for (int i = 0; i < Math.abs(frameNr - oldFrameNr); i++) {
				updateDisplay();
			}
		}
	}

	@Override
	public synchronized void stop() {
		terminate = true;
	}

	private void updateDisplay() {
		if (frameNr != oldFrameNr && !terminate) {
			ListIterator<ChangeListener> iter = listeners.listIterator();
			while (iter.hasNext()) {
				ChangeListener listener = iter.next();
				listener.stateChanged(new javax.swing.event.ChangeEvent(this));
			}
		}
	}
}
