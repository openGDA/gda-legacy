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

package gda.gui.oemove.plugins;

import gda.factory.Configurable;
import gda.factory.FactoryException;
import gda.factory.Findable;
import gda.gui.oemove.Pluggable;
import gda.jython.ICommandRunner;
import gda.jython.Jython;
import gda.jython.JythonServerFacade;
import gda.jython.JythonServerStatus;
import gda.observable.IObserver;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An OEMove plugin to control the opening and closing of a cryo stream shutter. Allows for a timed closure.
 */
public class CryoShutter implements IObserver, Pluggable, Findable, Configurable {
	
	private static final Logger logger = LoggerFactory.getLogger(CryoShutter.class);
	
	private String scriptName;

	private String openImageFileName = "OEImages/LCryoFlapOpen50.gif";

	private String closeImageFileName = "OEImages/LCryoFlapClosed50.gif";

	private static final boolean DEFAULT_STATE = true;

	private boolean open = DEFAULT_STATE;

	private JButton shutterButton;

	private JLabel label;

	private JythonServerFacade scriptingMediator = null;

	private ImageIcon openImage;

	private ImageIcon closedImage;

	private File scriptFile;

	private JTextField textField;

	private String name;

	private JPanel displayComponent;

	private JPanel controlComponent;

	private ICommandRunner commandRunnerForTesting;

	/**
	 * Create the plugin to control the cryo stream shutter
	 */
	public CryoShutter() {
		openImage = new ImageIcon(getResource(openImageFileName));
		closedImage = new ImageIcon(getResource(closeImageFileName));
	}

	/*
	 * This method returns the URL of the specified file. It should be used when the required file is at the same
	 * directory hierachy level as this class such that the file would have to be specified with ../somedirectory/file.
	 * This is only required when addressing resource in this way in a jar file. It assumes the presence of an interface
	 * class in one directroy level down.
	 */
	private URL getResource(String file) {
		URL url = null;
		Class<?>[] classes = getClass().getInterfaces();
		for (Class<?> cls : classes) {
			if ((url = cls.getResource(file)) != null) {
				break;
			}
		}
		return url;
	}

	@Override
	public JComponent getDisplayComponent() {
		return displayComponent;
	}

	private void createControlComponent() {
		controlComponent = new JPanel();
		controlComponent.setLayout(new FlowLayout());

		textField = new JTextField(10);
		textField.setText("1");
		textField.setHorizontalAlignment(SwingConstants.CENTER);
		textField.setFont(new Font("Monospaced", Font.BOLD, 14));
		textField.setToolTipText("Time for cryo flap shut");
		textField.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Time(secs)",
				TitledBorder.CENTER, TitledBorder.TOP, null, Color.black));

		shutterButton = new JButton();
		setButtonText();
		shutterButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				runShutterScript();
				setButtonText();
				setLabelIcon();
			}
		});
		controlComponent.add(textField);
		controlComponent.add(shutterButton);
	}

	@Override
	public JComponent getControlComponent() {
		return controlComponent;
	}

	private void setLabelIcon() {
		if (label != null) {
			if (open)
				label.setIcon(openImage);
			else
				label.setIcon(closedImage);
		}
	}

	private void setButtonText() {
		String text = (open) ? "Close" : "Open";
		if (shutterButton != null)
			shutterButton.setText(text);
	}

	private void runShutterScript() {
		if (scriptFile.exists()) {
			if (open) {
				scriptingMediator.runCommand("cryoShutterClose(" + textField.getText() + ")");
				open = false;
			} else {
				scriptingMediator.runCommand("cryoShutterOpen()");
				open = true;
			}
		}
	}

	@Override
	public void update(Object theObserved, Object changeCode) {
		if (changeCode instanceof JythonServerStatus) {
			JythonServerStatus newStatus = (JythonServerStatus) changeCode;

			// if script stopped
			if (newStatus.scriptStatus == Jython.IDLE) {
				open = DEFAULT_STATE;
				setButtonText();
				setLabelIcon();
			}
		}
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	private void createDisplayComponent() {
		displayComponent = new JPanel();
		label = new JLabel();
		setLabelIcon();
		displayComponent.add(label);
	}

	@Override
	public void configure() throws FactoryException {

		scriptingMediator = JythonServerFacade.getInstance();
		scriptingMediator.addIObserver(this);

		scriptFile = configureScriptFile(scriptName);

		if (scriptFile.exists()) {
			scriptingMediator.runScript(scriptFile, getName());
		} else {
			logger
					.error("CryoShutter: Specified script file " + scriptFile.getAbsolutePath()
							+ " could not be opened.");
		}

		createDisplayComponent();
		createControlComponent();
	}

	File configureScriptFile(String scriptName) {
		File scriptFile = new File(scriptName);
		if (!scriptFile.exists())
			scriptFile = new File(getCommandRunner().locateScript(scriptName));
		return scriptFile;
	}

	private ICommandRunner getCommandRunner() {
		return commandRunnerForTesting != null ? commandRunnerForTesting : scriptingMediator;
	}

	void setCommandRunnerForTesting(ICommandRunner runner) {
		commandRunnerForTesting = runner;
	}
	
	/**
	 * Get the script file name which controls the opening/closing of the shutter.
	 * 
	 * @return the script name
	 */
	public String getScriptName() {
		return scriptName;
	}

	/**
	 * Set the script file name which controls the opening/closing of the shutter.
	 * 
	 * @param scriptName
	 */
	public void setScriptName(String scriptName) {
		this.scriptName = scriptName;
	}
}
