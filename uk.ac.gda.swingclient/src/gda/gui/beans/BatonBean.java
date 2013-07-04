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

package gda.gui.beans;

import gda.jython.IBatonStateProvider;
import gda.jython.batoncontrol.BatonChanged;
import gda.observable.IObserver;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.SwingUtilities;

/**
 * Traffic light indication of whether the user has the baton - Green if he has else RED
 * If you click on the panel the baton panel is shown.
 */
public class BatonBean extends javax.swing.JPanel implements IObserver, MouseListener {
	String title="Baton";
	TrafficLights lights;
	Integer diameter=30;
	IBatonStateProvider batonStateProvider;
	String nameOfFrameToShowOnClick="Baton Panel";

	/**
	 * @return name of Frame to show when the user clicks the lights
	 */
	public String getNameOfFrameToShowOnClick() {
		return nameOfFrameToShowOnClick;
	}
	/**
	 * @param nameOfFrameToShowOnClick
	 */
	public void setNameOfFrameToShowOnClick(String nameOfFrameToShowOnClick) {
		this.nameOfFrameToShowOnClick = nameOfFrameToShowOnClick;
	}
	/**
	 * @return batonStateProvider
	 */
	public IBatonStateProvider getBatonStateProvider() {
		return batonStateProvider;
	}
	/**
	 * @param batonStateProvider
	 */
	public void setBatonStateProvider(IBatonStateProvider batonStateProvider) {
		this.batonStateProvider = batonStateProvider;
	}
	/**
	 * @return diameter of the traffic light
	 */
	public Integer getDiameter() {
		return diameter;
	}
	/**
	 * @param diameter
	 */
	public void setDiameter(Integer diameter) {
		this.diameter = diameter;
	}
	/**
	 * @return border title
	 */
	public String getTitle() {
		return title;
	}
	/**
	 * @param title
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * 
	 */
	public BatonBean(){
	}
	
	/**
	 * 
	 */
	public void configure(){
		setBorder(BorderFactory.createTitledBorder(getTitle()));
		add(lights = new TrafficLights(getDiameter(),true));
		lights.addMouseListener(this);
		batonStateProvider.addBatonChangedObserver(this);
		updateInSwingThread();
	}
	private void updateInSwingThread(){
		
		lights.setState(batonStateProvider.amIBatonHolder()? TrafficLights.State.GREEN : TrafficLights.State.RED);
		lights.setToolTipText(batonStateProvider.amIBatonHolder()? "You hold the baton and have control of the beamline":
			"You do not hold the baton. Click to switch to the Baton Panel");

	}
	
	@Override
	public void update(Object theObserved, Object changeCode) {
		if(changeCode instanceof BatonChanged){
			SwingUtilities.invokeLater(new Runnable(){
				@Override
				public void run(){
					BatonBean.this.updateInSwingThread();
				}
			});
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		gda.gui.AcquisitionFrame.showFrame(nameOfFrameToShowOnClick);
	}
	@Override
	public void mouseEntered(MouseEvent e) {
	}
	@Override
	public void mouseExited(MouseEvent e) {
	}
	@Override
	public void mousePressed(MouseEvent e) {
	}
	@Override
	public void mouseReleased(MouseEvent e) {
	}
	
	
}
