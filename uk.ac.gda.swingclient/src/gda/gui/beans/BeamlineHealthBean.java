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

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.scannable.ScannablePositionChangeEvent;
import gda.gui.beans.TrafficLights.State;
import gda.observable.IObserver;

import java.awt.Desktop;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.BorderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to display the beamline health using a traffic light
 * Monitors two scannables:
 * healthScannable - value is either "good", "bad" or "poor" - otherwise the TrafficLight is set to UNKNOWN
 * heartBeatScannable
 */
public class BeamlineHealthBean extends javax.swing.JPanel implements IObserver, MouseListener {
	
	private static final Logger logger = LoggerFactory.getLogger(BeamlineHealthBean.class);
	
	/**
	 * heartBeatScannable switches between on and something else
	 */
	public static final String ON = "on";
	/**
	 * value of healthScannable when the state is bad - RED light
	 */
	public static final String BAD = "bad";
	/**
	 * value of healthScannable when the state is poor - AMBER light
	 */
	public static final String POOR = "poor";
	/**
	 * value of healthScannable when the state is good - GREEN light
	 */
	public static final String GOOD = "good";
	Scannable healthScannable, heartBeatScannable;
	String title;
	TrafficLights lights;
	Integer diameter=0;
	URI uri;


	/**
	 * @return URI attribute of the healthScannable to be displayed in a browser
	 * @throws URISyntaxException
	 * @throws DeviceException
	 */
	public URI getUri() throws URISyntaxException, DeviceException {
		URI uri = new URI((String) healthScannable.getAttribute("uri"));
		return uri;
	}
	/**
	 * @return diameter of the TrafficLight in pixels
	 */
	public Integer getDiameter() {
		return diameter;
	}
	/**
	 * @param diameter of the TrafficLight in pixels
	 */
	public void setDiameter(Integer diameter) {
		this.diameter = diameter;
	}
	/**
	 * @return Border Title 
	 */
	public String getTitle() {
		return title;
	}
	/**
	 * @param title - Border Title 
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * @return healthScannable
	 */
	public Scannable getHealthScannable() {
		return healthScannable;
	}
	/**
	 * @param healthScannable - scannable whose value indicates the health of the beamline
	 */
	public void setHealthScannable(Scannable healthScannable) {
		this.healthScannable = healthScannable;
	}
	/**
	 * @return  scannable that changes back and forth to "on" everytime the health is checked
	 */
	public Scannable getHeartBeatScannable() {
		return heartBeatScannable;
	}
	/**
	 * @param heartBeatScannable scannable that changes back and forth to "on" everytime the health is checked
	 */
	public void setHeartBeatScannable(Scannable heartBeatScannable) {
		this.heartBeatScannable = heartBeatScannable;
	}
	/**
	 * Simple constructor
	 */
	public BeamlineHealthBean(){
	}
	/**
	 * Call this method to create the panel and start monitoring the scannables
	 */
	public void configure(){
		setBorder(BorderFactory.createTitledBorder(getTitle()));
		add(lights = new TrafficLights(getDiameter(), true));
		lights.addMouseListener(this);
		healthScannable.addIObserver(this);
		heartBeatScannable.addIObserver(this);
		try {
			updateHealthIndicator((String) healthScannable.getPosition());
			updateHealthEcgIndicator((String) heartBeatScannable.getPosition());
		} catch (DeviceException ex) {
			logger.error(ex.getMessage(),ex);
		}
	}
	@Override
	public void update(Object theObserved, Object changeCode) {
		//with access control the changeCode will come from the CGLIB wrapped object - we need to check its name
		if(((Scannable)theObserved).getName().equals(healthScannable.getName())
			&& changeCode instanceof ScannablePositionChangeEvent){
			String s = (String)((ScannablePositionChangeEvent)changeCode).newPosition;
			updateHealthIndicator(s);
		}
		if(((Scannable)theObserved).getName().equals(heartBeatScannable.getName())
				&& changeCode instanceof ScannablePositionChangeEvent){
			String s = (String)((ScannablePositionChangeEvent)changeCode).newPosition;
			updateHealthEcgIndicator(s);
		}
	}
	
	private void updateHealthIndicator(String newState) {
		TrafficLights.State state = State.UNKNOWN;
		if (newState.equals(GOOD)) {
			state = State.GREEN;
		} else if (newState.equals(POOR)) {
			state = State.AMBER;
		} else if (newState.equals(BAD)) {
			state = State.RED;
		}
		
		lights.setState(state);
		lights.setToolTipText("Beamline health is " + newState + ". Click to open report from last check");
	}
	
	private void updateHealthEcgIndicator(String newState) {
		lights.setMonitor(newState.equals(ON) ? TrafficLights.Monitor.ON : TrafficLights.Monitor.OFF);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (Desktop.isDesktopSupported() ) {
			Desktop desktop = Desktop.getDesktop();
			if (desktop.isSupported(Desktop.Action.BROWSE)) {
				try {
					desktop.browse(getUri());
				} catch (Exception ex) {
					logger.error(ex.getMessage(),ex);
				}
			}
		}
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
