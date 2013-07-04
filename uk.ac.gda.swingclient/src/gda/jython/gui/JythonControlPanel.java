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

package gda.jython.gui;

import gda.factory.Finder;
import gda.gui.util.CurrentAmplifierPanel;
import gda.gui.util.ShutterPanel;
import gda.gui.util.ValueDisplayPanel;
import gda.jython.Jython;
import gda.jython.JythonServerFacade;
import gda.jython.JythonServerStatus;
import gda.observable.IObserver;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple JPanel with buttons to halt, pause, stop scripts and scans
 */
public class JythonControlPanel extends JPanel implements IObserver {

	private static final Logger logger = LoggerFactory.getLogger(JythonControlPanel.class);

	/**
	 * The name this object should always be known as within the GDA Finder
	 */
	public static final String NAME = "JythonScriptControlPanel";

	// to make sure configure is only run once
	private boolean configured = false;

	// the instance of the scripting mediator
	JythonServerFacade commandserver = null;

	// strings on buttons
	private static final String SCANPAUSED = "Pause scan";

	private static final String SCANRUNNING = "Resume scan";

	private static final String SCRIPTPAUSED = "Pause script";

	private static final String SCRIPTRUNNING = "Resume script";

	JButton btnPauseScan = new JButton();

	JButton btnPauseScript = new JButton();

	JButton btnHaltScriptScan = new JButton();

	private ArrayList<String> shutterList = new ArrayList<String>();
	private ArrayList<String> amplifierList = new ArrayList<String>();
	private ArrayList<String> valueList = new ArrayList<String>();
	private ArrayList<CurrentAmplifierPanel> amplifierPanels = new ArrayList<CurrentAmplifierPanel>();
	Finder finder = Finder.getInstance();

	private ArrayList<ValueDisplayPanel> valueDispalyPanels = new ArrayList<ValueDisplayPanel>();

	private ArrayList<ShutterPanel> shutterPanels = new ArrayList<ShutterPanel>();
	/**
	 * configures the class - needed as this can only be run after JythonServerFacade.getInstance will not fail
	 */
	public void configure() {
		if (!configured) {
			commandserver = JythonServerFacade.getInstance();
			commandserver.addIObserver(this);
			this.setLayout(new FlowLayout());
			
			// add the optional shutter buttons
			if (!(shutterList.isEmpty())) {
				for (String sn : shutterList) {
					ShutterPanel sp = new ShutterPanel();
					sp.setShutterName(sn);
					sp.configure();
					this.add(sp);
				}
			}
			if (!(shutterPanels.isEmpty())) {
				for (ShutterPanel panel : shutterPanels) {
					this.add(panel);
				}
			}
			// add the optional current amplifier panels
			if (!(amplifierList.isEmpty())) {
				for (String amp : amplifierList) {
					CurrentAmplifierPanel cam;
					if ((cam = (CurrentAmplifierPanel) finder.find(amp)) != null) {
						this.add(cam);
					} else {
						logger.warn("{} cannot find {}",getName(), amp);
					}
				}
			}
			if (!(amplifierPanels.isEmpty())) {
				for (CurrentAmplifierPanel panel : amplifierPanels) {
					this.add(panel);
				}
			}
			// add the optional beam value display panels
			if (!(valueList.isEmpty())) {
				for (String val : valueList) {
					ValueDisplayPanel vdp;
					if ((vdp = (ValueDisplayPanel) finder.find(val)) != null) {
						this.add(vdp);
					} else {
						logger.warn("{} cannot find {}",getName(), val);
					}
				}
			}
			if (!(valueDispalyPanels.isEmpty())) {
				for (ValueDisplayPanel panel : valueDispalyPanels) {
					this.add(panel);
				}
			}

			configured = true;
		}
	}
	
	/**
	 * Remove this terminal from the command server
	 * observer list. 
	 * 
	 * This method should be called to ensure new terminals
	 * can be registered with the command server.
	 */
	public void dispose(){
		if (commandserver != null){
			commandserver.deleteIObserver(this);
			commandserver = null;
		}
	}

	@Override
	public String getName() {
		return NAME;
	}

	/**
	 * Constructor.
	 */
	public JythonControlPanel() {
		try {
			setName(NAME);
			jbInit();
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		}
	}



	/**
	 * From the IObservers interface. Used when a scan has been initiated from the terminal or jython editor. This
	 * terminal is registered as an observer, so if the graphics option has been selected for this object then the data
	 * will be shown on a graph.
	 * 
	 * @param dataSource
	 *            Object
	 * @param dataPoint
	 *            Object
	 */
	@Override
	public void update(Object dataSource, Object dataPoint) {

		// if from scans then objects are in the format String, ScanDataPoint
		if (dataPoint instanceof JythonServerStatus) {
			JythonServerStatus status = (JythonServerStatus) dataPoint;

			if (status.scanStatus == Jython.PAUSED) {
				btnPauseScan.setText(SCANRUNNING);
			} else
			{
				btnPauseScan.setText(SCANPAUSED);
			}

			if (status.scriptStatus == Jython.PAUSED) {
				btnPauseScript.setText(SCRIPTRUNNING);
			} else
			{
				btnPauseScript.setText(SCRIPTPAUSED);
			}
		}
	}

	/**
	 * @return shutter list
	 */
	public ArrayList<String> getShutterList() {
		return shutterList;
	}

	/**
	 * @param shutterList
	 */
	public void setShutterList(ArrayList<String> shutterList) {
		this.shutterList = shutterList;
	}

	/**
	 * @param sn
	 */
	public void addShutter(String sn) {
		this.shutterList.add(sn);
	}

	/**
	 * @return shutter list
	 */
	public ArrayList<String> getAmplifierList() {
		return amplifierList;
	}

	/**
	 * @param amplifierList
	 */
	public void setAmplifierList(ArrayList<String> amplifierList) {
		this.amplifierList = amplifierList;
	}

	/**
	 * @param cam
	 */
	public void addAmplifier(String cam) {
		this.amplifierList.add(cam);
	}

	/**
	 * @return value list
	 */
	public ArrayList<String> getValueList() {
		return valueList;
	}

	/**
	 * @param valueList
	 */
	public void setValueList(ArrayList<String> valueList) {
		this.valueList = valueList;
	}

	/**
	 * @param cam
	 */
	public void addValue(String cam) {
		this.valueList.add(cam);
	}
	public ArrayList<CurrentAmplifierPanel> getAmplifierPanels() {
		return amplifierPanels;
	}

	public void setAmplifierPanels(ArrayList<CurrentAmplifierPanel> panels) {
		this.amplifierPanels = panels;
		
	}

	public ArrayList<ValueDisplayPanel> getValueDisplayPanels() {
		return valueDispalyPanels ;
		
	}

	public void setValueDisplayPanels(ArrayList<ValueDisplayPanel> panels) {
		this.valueDispalyPanels=panels;
		
	}

	public ArrayList<ShutterPanel> getShutterPanels() {
		return shutterPanels ;
	}

	public void setShutterPanels(ArrayList<ShutterPanel> panels) {
		this.shutterPanels=panels;
		
	}

	/*
	 * Pauses the currently running script if appropriate tests have been added to the script. @param e ActionEvent
	 */
	void btnPauseScript_actionPerformed() {
		if (btnPauseScript.getText().compareTo(SCRIPTPAUSED) == 0) {
			commandserver.pauseCurrentScript();
			btnPauseScript.setText(SCRIPTRUNNING);
		} else {
			commandserver.resumeCurrentScript();
			btnPauseScript.setText(SCRIPTPAUSED);
		}
	}

	/*
	 * Sends a panic stop to the command server. @param e ActionEvent
	 */
	void btnPanic_actionPerformed() {
		commandserver.panicStop();
	}


	/*
	 * Pause the currently running scan @param e ActionEvent
	 */
	void btnPauseScan_actionPerformed() {
		if (btnPauseScan.getText().compareTo(SCANPAUSED) == 0) {
			JythonServerFacade.getInstance().pauseCurrentScan();
			btnPauseScan.setText(SCANRUNNING);

			// then ask user if they wish to stop the current scan
			// altogether
		} else {
			JythonServerFacade.getInstance().resumeCurrentScan();
			btnPauseScan.setText(SCANPAUSED);
		}
	}

	/*
	 * Stops cleanly current scan and script, if running. @param e ActionEvent
	 */
	void btnHaltScriptScan_actionPerformed() {
		if (this.commandserver.getScanStatus() == Jython.RUNNING || this.commandserver.getScanStatus() == Jython.PAUSED) {
			this.commandserver.haltCurrentScan();
		} else {
			this.commandserver.haltCurrentScript();
		}
	}

	private void jbInit() {

		this.setEnabled(true);
		this.setDebugGraphicsOptions(0);
		btnPauseScan.setToolTipText("Click to pause\\resume running scans");
		btnPauseScan.setText(SCANPAUSED);
		btnPauseScan.addActionListener(new JythonTerminal_btnPauseScan_actionAdapter(this));
		btnPauseScript.setText(SCRIPTPAUSED);
		btnPauseScript.addActionListener(new JythonTerminal_btnPauseScript_actionAdapter(this));
		btnPauseScript.setToolTipText("pauses\\resumes the current script if possible");

		btnHaltScriptScan.setText("Halt current scans/scripts");
		btnHaltScriptScan.addActionListener(new JythonTerminal_btnHaltScriptScan_actionAdapter(this));
		
		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		btnPauseScan.setAlignmentX(Component.CENTER_ALIGNMENT);
		this.add(btnPauseScan);
		btnPauseScript.setAlignmentX(Component.CENTER_ALIGNMENT);
		this.add(btnPauseScript);
		btnHaltScriptScan.setAlignmentX(Component.CENTER_ALIGNMENT);
		this.add(btnHaltScriptScan);
		
	}

class JythonTerminal_btnPauseScan_actionAdapter implements java.awt.event.ActionListener {
	JythonControlPanel adaptee;

	JythonTerminal_btnPauseScan_actionAdapter(JythonControlPanel adaptee) {
		this.adaptee = adaptee;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		adaptee.btnPauseScan_actionPerformed();
	}
}

class JythonTerminal_btnPauseScript_actionAdapter implements java.awt.event.ActionListener {
	JythonControlPanel adaptee;

	JythonTerminal_btnPauseScript_actionAdapter(JythonControlPanel adaptee) {
		this.adaptee = adaptee;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		adaptee.btnPauseScript_actionPerformed();
	}
}

class JythonTerminal_btnPanic_actionAdapter implements java.awt.event.ActionListener {
	JythonControlPanel adaptee;

	JythonTerminal_btnPanic_actionAdapter(JythonControlPanel adaptee) {
		this.adaptee = adaptee;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		adaptee.btnPanic_actionPerformed();
	}
}

class JythonTerminal_btnHaltScriptScan_actionAdapter implements ActionListener {
	private JythonControlPanel adaptee;

	JythonTerminal_btnHaltScriptScan_actionAdapter(JythonControlPanel adaptee) {
		this.adaptee = adaptee;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		adaptee.btnHaltScriptScan_actionPerformed();
	}
}

}