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

import javax.swing.event.ChangeListener;
import de.jreality.scene.Appearance;
import de.jreality.shader.CommonAttributes;
import java.util.LinkedList;
import java.util.ListIterator;

/**
 *
 *
 */

public class PlayBackSlideshow implements IPlayBack {

	private int frameNr = 0;
	private int oldFrameNr = 0;
	private int oldSliceNr = 0;
	private int totalFrames = 0;
	private Appearance[] currentApps;
	private boolean pause = false;
	private boolean terminate = false;
	private LinkedList<ChangeListener> listeners;

	/**
	 * Constructor for the PlayBackSlideshow
	 * 
	 * @param appearances
	 *            appearance nodes
	 * @param numFrames
	 *            number of total frames
	 */
	public PlayBackSlideshow(Appearance[] appearances, int numFrames) {
		totalFrames = numFrames;
		currentApps = appearances;
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
	public void resume() {
		pause = false;
		notify();
	}

	@Override
	public void run() {
		while (frameNr < totalFrames && !terminate) {

			if (pause) {
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
			updateAppearances();
			try {
				Thread.sleep(30);
			} catch (InterruptedException ex) {
			}
			Thread.yield();
		}
	}

	@Override
	public void setFrameNr(int newFrameNr, boolean executeChange) {
		oldFrameNr = frameNr;
		frameNr = newFrameNr;
		if (executeChange)
			updateAppearances();
	}

	@Override
	public synchronized void stop() {
		terminate = true;
	}

	private synchronized void updateAppearances() {
		if (!terminate) {
			int sliceNr = (frameNr / 100) % DataSetImages.MAXIMAGEBUFFER;
			if (sliceNr != oldSliceNr) {
				ListIterator<ChangeListener> iter = listeners.listIterator();
				while (iter.hasNext()) {
					ChangeListener listener = iter.next();
					listener.stateChanged(new javax.swing.event.ChangeEvent(this));
				}
				oldSliceNr = sliceNr;
			}

			int frameOfSlice = frameNr % 100;

			double transparency = (frameOfSlice + 1) / 100.0;
			currentApps[sliceNr].setAttribute(CommonAttributes.POLYGON_SHADER + "." + CommonAttributes.TRANSPARENCY,
					transparency);
		}
	}
}
