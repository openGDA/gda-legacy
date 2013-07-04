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

enum Direction {
	FORWARD, BACKWARD, NONE
}

/**
 * Interface for a PlayBack of image sequences in the DataSetImages panel
 */

public interface IPlayBack extends Runnable {

	/**
	 * Add a change listener to the playback to get notified when a change occurs
	 * 
	 * @param listener
	 *            change listener to be added to the playback
	 */
	public void addChangeListener(ChangeListener listener);

	/**
	 * Pause the playing
	 */

	public void pause();

	/**
	 * Is the playback currently paused?
	 * 
	 * @return true if it is paused otherwise false
	 */
	public boolean isPaused();

	/**
	 * Resume playback
	 */
	public void resume();

	/**
	 * Get the current frame number the playbay back is at
	 * 
	 * @return current frame number
	 */
	public int getFrameNr();

	/**
	 * Get the current playing direction (see Direction enum)
	 * 
	 * @return the current playing direction
	 */
	public Direction getDirection();

	/**
	 * Set the current frame number in playback
	 * 
	 * @param newFrameNr
	 *            new frame position
	 * @param executeChange
	 *            should this change be notified to the changelistener
	 */
	public void setFrameNr(int newFrameNr, boolean executeChange);

	/**
	 * Stop the playback
	 */
	public void stop();

	/**
	 * clean up the playback remove unnecessary references
	 */

	public void cleanup();

	@Override
	public void run();

}
