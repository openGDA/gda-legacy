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

package gda.jython.gui;

import gda.gui.AcquisitionPanel;
import gda.gui.scanplot.ScanPlotPanel;
import gda.gui.util.CurrentAmplifierPanel;
import gda.gui.util.ShutterPanel;
import gda.gui.util.StateDisplayPanel;
import gda.gui.util.ValueDisplayPanel;
import gda.observable.IObserver;

import java.awt.Component;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JSplitPane;

/**
 * Simple Acq panel to bring the 3 individual panels - terminal, controller and plot together as one
 */
public class JythonTerminalAcqPanel extends AcquisitionPanel implements IObserver  {
	private boolean configured = false;
	JythonTerminal term;
	JythonController controller;
	ScanPlotPanel scan;
	
	public JythonTerminalAcqPanel(){
		term = new JythonTerminal();
		scan = new ScanPlotPanel();
		scan.setName("ScanPlot");
		JSplitPane jSplitPane1 = new JSplitPane();
		jSplitPane1.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
		jSplitPane1.setOneTouchExpandable(true);			
		jSplitPane1.add(term, JSplitPane.TOP);
		jSplitPane1.add(scan, JSplitPane.BOTTOM);
		controller = new JythonController();

		this.setEnabled(true);
		this.setDebugGraphicsOptions(0);
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		jSplitPane1.setAlignmentX(Component.CENTER_ALIGNMENT);
		this.add(jSplitPane1);

		controller.setAlignmentX(Component.CENTER_ALIGNMENT);
		this.add(controller);
		
		setLabel("Terminal");
	}
	
	@Override
	public void configure() {
		if (!configured) {
			term.configure(); //to get print updates
			scan.configure();
			controller.configure();
			configured = true;
		}
	}	

	public ArrayList<String> getShutterList() {
		return controller.getShutterList();
	}

	public void setShutterList(ArrayList<String> shutterList) {
		controller.setShutterList(shutterList);
	}

	public void addShutter(String sn) {
		controller.addShutter(sn);
	}

	public ArrayList<ShutterPanel> getShutterPanels() {
		return controller.getShutterPanels();
	}

	public void setShutterPanels(ArrayList<ShutterPanel> panels){
		controller.setShutterPanels(panels);
	}

	public ArrayList<String> getAmplifierList() {
		return controller.getAmplifierList();
	}

	public void setAmplifierList(ArrayList<String> amplifierList) {
		controller.setAmplifierList(amplifierList);
	}

	public void addAmplifier(String cam) {
		controller.addAmplifier(cam);
	}
	
	public ArrayList<CurrentAmplifierPanel> getAmplifierPanels() {
		return controller.getAmplifierPanels();
	}

	public void setAmplifierPanels(ArrayList<CurrentAmplifierPanel> panels) {
		controller.setAmplifierPanels(panels);
	}

	public ArrayList<String> getValueList() {
		return controller.getValueList();
	}

	public void setValueList(ArrayList<String> valueList) {
		controller.setValueList(valueList);
	}

	public void addValue(String cam) {
		controller.addValue(cam);
	}

	public ArrayList<ValueDisplayPanel> getValueDisplayPanels(){
		return controller.getValueDisplayPanels();
	}

	public void setValueDisplayPanel(ArrayList<ValueDisplayPanel> panels){
		controller.setValueDisplayPanels(panels);
	}

	public void setStateDisplayPanels(ArrayList<StateDisplayPanel> panels) {
		controller.setStateDisplayPanels(panels);
	}	

	public ArrayList<StateDisplayPanel> getStateDisplayPanels() {
		return controller.getStateDisplayPanels();
	}	

	/**
	 * Returns whether new data sets would be placed in new graphs or as extra lines to current graphs.
	 */
	public boolean isExtraGraphs() {
		return scan.isExtraGraphs();
	}

	/**
	 * Get the number of initial graphs displayed in the panel.
	 */
	public int getNumberInitialGraphs() {
		return scan.getNumberInitialGraphs();
	}

	/**
	 * Sets whether new data sets would be placed in new graphs or as extra lines to current graphs.
	 */
	public void setExtraGraphs(boolean extraGraphs) {
		scan.setExtraGraphs(extraGraphs);
	}

	/**
	 * Sets the number of initial graphs created by the configure method.
	 */
	public void setNumberInitialGraphs(int numberInitialGraphs) {
		scan.setNumberInitialGraphs(numberInitialGraphs);
	}

	/**
	 * disposes of the child objects
	 */
	public void dispose(){
		term.dispose();
		scan.dispose();
		controller.dispose();
	}

	@Override
	public void update(Object theObserved, Object changeCode) {
	}
	
}
