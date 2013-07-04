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

package gda.jython.scriptcontroller;

import gda.factory.Finder;
import gda.gui.AcquisitionPanel;
import gda.jython.JythonServerFacade;
import gda.observable.IObserver;

import javax.swing.JButton;
import javax.swing.JTextField;

/**
 * A GUI panel to demonstrate the ScriptControllers. These are used to enable communication between a GUI panel and a
 * script. The GUI panel can run the script and receive messages back from the script for display. This class was built
 * using VisualEditor.
 * <p>
 * To use this demo add this to the server.xml: <ScriptController> <name>ScriptControllerDemo</name>
 * <command>reload(ScriptControllerDemo);ScriptControllerDemo.runDemo()</command> <parametersName></parametersName>
 * <importCommand>import ScriptControllerDemo</importCommand> </ScriptController>
 * <p>
 * Add this to the gui.xml: <ScriptControllerDemoPanel> <name>Script controller demo</name> </ScriptControllerDemoPanel>
 * <p>
 * And create a file called ScriptControllerDemo.py in your scripts folder with the contents: from gda.factory import
 * Finder def runDemo(): #get the ScriptController object from the Finder controller =
 * Finder.getInstance().find("ScriptControllerDemo") #do the work of the script here #send messages back to any
 * observers controller.update(None,"Script complete.");
 */
public class ScriptControllerDemoPanel extends AcquisitionPanel implements IObserver {

	private JButton jButton = null;

	private JTextField jTextField = null;

	private Scriptcontroller demoController = null;

	/**
	 * 
	 */
	public ScriptControllerDemoPanel() {
		super();
		initialize();
	}

	@Override
	public void configure() {
		// get the controller from the finder
		demoController = (Scriptcontroller) Finder.getInstance().find("ScriptControllerDemo");
		// register this panle as an observer of the controller object
		demoController.addIObserver(this);

	}

	/*
	 * This method initializes this
	 */
	private void initialize() {
		this.add(getJButton(), null);
		this.add(getJTextField(), null);

	}

	@Override
	public void update(Object theObserved, Object changeCode) {
		if (theObserved instanceof Scriptcontroller && changeCode instanceof String) {
			// change the contents of the textfiled with the message from the script
			jTextField.setText((String) changeCode);
		}
	}

	/**
	 * This method initializes jButton
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getJButton() {
		if (jButton == null) {
			jButton = new JButton();
			jButton.setText("Run script");
			jButton.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					// run the command supplied by the ScriptController through the Commandserver. Doing things this way
					// ensures that any access control implemented in the GDA would still apply to this script.
					JythonServerFacade.getInstance().runCommand(demoController.getCommand(), getName());
				}
			});
		}
		return jButton;
	}

	/**
	 * This method initializes jTextField
	 * 
	 * @return javax.swing.JTextField
	 */
	private JTextField getJTextField() {
		if (jTextField == null) {
			jTextField = new JTextField();
			jTextField.setPreferredSize(new java.awt.Dimension(150, 20));
		}
		return jTextField;
	}

}
