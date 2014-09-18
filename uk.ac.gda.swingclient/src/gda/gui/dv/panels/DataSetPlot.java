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

package gda.gui.dv.panels;


import gda.jython.JythonServerFacade;
import gda.plots.SimplePlot;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

import org.eclipse.dawnsci.analysis.dataset.impl.DoubleDataset;

/**
 * DataSetPlot Class
 */
public class DataSetPlot extends SimplePlot {

	private JMenuItem testButton;
	private String xAxisMotor = null;
	private double xLocation = 0.0;
	
	// this should also include links to the datasets used so they can be referenced by 
	// other panels requiring the data
	private DoubleDataset xAxis;
	private Vector<DoubleDataset> yAxis = new Vector<DoubleDataset>();
	private JCheckBoxMenuItem liveButton;
	private boolean freezePlot = false;
	
	/**
	 * Constructor
	 * 
	 * @param xAxisName
	 */
	public DataSetPlot(String xAxisName) {
		super();
		setTurboMode(true);
		setFreezePlot(false);

		xAxisMotor = xAxisName;
		
		setTitle("");

		// Please do not remove this as it is required to make the right mouse button work properly.
		setTrackPointer(true);
	}

	/**
	 * @param xAxisName
	 */
	public void setXAxisMotor(String xAxisName) {
		xAxisMotor = xAxisName;
	}

	@Override
	protected void displayPopupMenu(int arg0, int arg1) {
		xLocation = getCoordinates()[0];
		String display = "Move " + xAxisMotor + " to " + xLocation;
		testButton.setText(display);
		super.displayPopupMenu(arg0, arg1);
	}

	@Override
	protected JPopupMenu createPopupMenu(boolean properties, boolean save, boolean print, boolean zoom) {
		JPopupMenu jpm = super.createPopupMenu(properties, save, print, zoom);

		jpm.add(new JSeparator());

		// This button toggles the image-type magnification
		testButton = new JMenuItem("Move Motor to here");
		testButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String command = "pos " + xAxisMotor + " " + xLocation;
				Object[] options = { "Yes", "Cancel" };
				int returnValue = JOptionPane.showOptionDialog(null, "run the command \"" + command + "\"",
						"Move Motor?", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, options,
						options[1]);
				if (returnValue == 0) {
					JythonServerFacade.getInstance().print(command);
					JythonServerFacade.getInstance().runCommand(command);
				}
			}
		});
		jpm.add(testButton);
		
		// The live update plot toggles the value of liveUpdatePlot.
		liveButton = new JCheckBoxMenuItem("Freeze Plot");
		liveButton.setHorizontalTextPosition(SwingConstants.LEFT);
		liveButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setFreezePlot(!isFreezePlot());
			}
		});
		jpm.add(liveButton);

		return jpm;
	}
	public boolean isFreezePlot() {
		return freezePlot ;
	}

	/**
	 * Sets the value of liveUpdatePlot.
	 * 
	 * @param freezePlot
	 *            The new value.
	 */
	public void setFreezePlot(boolean freezePlot) {
		this.freezePlot = freezePlot;
		liveButton.setSelected(freezePlot);
	}

	/**
	 * @return the xAxis
	 */

	public DoubleDataset getXAxis() {
		return xAxis;
	}

	/**
	 * @param data
	 */
	public void setXAxis(DoubleDataset data) {
		xAxis = data;
	}

	/**
	 * @return the yAxis
	 */

	public Vector<DoubleDataset> getYAxis() {
		return yAxis;
	}

	/**
	 * @param axis
	 */
	public void setYAxis(Vector<DoubleDataset> axis) {
		yAxis = axis;
	}

	/**
	 * Special Setter with takes an array of datasets as its input.
	 * @param ydata
	 */
	public void setYAxis(DoubleDataset[] ydata) {
		// clear out the vector
		yAxis.clear();
		
		for(int i = 0; i < ydata.length; i++) {
			yAxis.add(ydata[i]);
		}
		
	}
	
	/**
	 * Special Setter with takes an array of datasets as its input, and dosn't reset the vector.
	 * @param ydata
	 */
	public void addYAxis(DoubleDataset[] ydata) {
		
		for(int i = 0; i < ydata.length; i++) {
			yAxis.add(ydata[i]);
		}
		
	}
	
}
