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
 * 
 *
 */
public class ImageDataLoader implements Runnable {

	
	private LinkedList<IJob> jobList = null;
	private LinkedList<ChangeListener> listeners = null;
	
	private boolean shutdown = false;
	
	/**
	 * Constructor for an ImageDataLoader
	 */
	
	public ImageDataLoader()
	{
		jobList = new LinkedList<IJob>();
		listeners = new LinkedList<ChangeListener>();
	}	
	
	
	/**
	 * Add another job to the joblist
	 * @param newJob
	 */
	public synchronized void addJob(IJob newJob)
	{
		jobList.add(newJob);
		this.notify();
	}
	
	/**
	 * Add a ChangeListener to the loader
	 * @param listener the ChangeListener to be added 
	 */
	public void addChangeListener(ChangeListener listener)
	{
	   listeners.add(listener);
	}
	
	private synchronized void doJob()
	{
		IJob currentJob = jobList.remove();
		currentJob.executeJob();
		ListIterator<ChangeListener> iter = listeners.listIterator();
		while (iter.hasNext()) {
			ChangeListener listener = iter.next();
			listener.stateChanged(new javax.swing.event.ChangeEvent(this));
		}		
	}
	
	/**
	 * Stop the ImageDataLoader it should terminate the thread 
	 */
	
	public void stop()
	{
		shutdown = true;
	}	

	@Override
	public void run() {
		while (!shutdown)
		{
			if (jobList.size() == 0)
			{
				try {
					synchronized(this)
					{
						while (jobList.size() == 0)
							wait();
					}
				} catch (InterruptedException ex) {}
			} else {
				doJob();
			}	
		}
	}

}

	