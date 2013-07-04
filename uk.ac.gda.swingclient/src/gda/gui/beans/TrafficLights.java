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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JPanel;

/**
 *
 */
public class TrafficLights extends JPanel  {
	final int diameter, myBorder, myWidth,myHeight;

	/**
	 *
	 */
	public enum State { /**
	 * 
	 */
	RED, /**
	 * 
	 */
	AMBER, /**
	 * 
	 */
	GREEN,
	/**
	 * 
	 */
	UNKNOWN}
	/**
	 *
	 */
	public enum Monitor {/**
	 * 
	 */
	ON, /**
	 * 
	 */
	OFF}
	
	State state;
	Monitor monitor;
	private int numLights;
	
	/**
	 * @param diameter 
	 * 
	 */
	public TrafficLights(int diameter) {
		this(diameter, false);
	}

	/**
	 * @param diameter 
	 * @param singleLight 
	 * 
	 */
	public TrafficLights(int diameter, boolean singleLight) {
		this.diameter = diameter;
		numLights = singleLight ? 1 : 3;
		myBorder = (int) (diameter * 0.2);
		myWidth = 2*myBorder + diameter;
		myHeight = (1+numLights)*myBorder + numLights*diameter;
		this.setPreferredSize(new Dimension(myWidth, myHeight));
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		drawTrafficLight(g);
	}
	
	private void drawTrafficLight(Graphics g) {
		int x0, y0;
		x0 = myWidth/2;
		((Graphics2D)g).setRenderingHint
		  (RenderingHints.KEY_ANTIALIASING,
		   RenderingHints.VALUE_ANTIALIAS_ON); 
		((Graphics2D)g).setStroke(new BasicStroke());
		for (int i=0; i< numLights; i++) {
			y0 = (int) ((i+1)*myBorder + (i+0.5)*diameter);
			if (i == 0) {
				if(numLights==1){
					g.setColor(getState() == State.RED ? Color.RED : 
						getState() == State.AMBER ? Color.yellow :
							getState() == State.GREEN ? Color.GREEN :Color.DARK_GRAY);
				} else {
					g.setColor(getState() == State.RED ? Color.RED : Color.DARK_GRAY);
				}
			} else if (i == 1) {
				g.setColor(getState() == State.AMBER ? Color.YELLOW : Color.DARK_GRAY);
			} else if (i == 2) {
				g.setColor(getState() == State.GREEN ? Color.GREEN : Color.DARK_GRAY);
			}
			g.fillOval(x0-diameter/2, y0-diameter/2, diameter, diameter);
			g.setColor(Color.BLACK);
			g.drawOval(x0-diameter/2, y0-diameter/2, diameter, diameter);
		}
		g.setColor(getMonitor() == Monitor.ON ? Color.WHITE :Color.BLACK); 
		g.drawRoundRect(x0 - diameter/2 - myBorder/2, myBorder/2, diameter + myBorder, 
				numLights*diameter + numLights*myBorder, (1+numLights)*myBorder, (1+numLights)*myBorder);
	}
	
	/**
	 * @return Returns the monitor.
	 */
	public Monitor getMonitor() {
		return monitor;
	}

	/**
	 * @param monitor The monitor to set.
	 */
	public void setMonitor(Monitor monitor) {
		this.monitor = monitor;
		repaint();
	}

	/**
	 * @param state The state to set.
	 */
	public void setState(State state) {
		this.state = state;
		repaint();
	}

	/**
	 * @return Returns the state.
	 */
	public State getState() {
		return state;
	}

}
