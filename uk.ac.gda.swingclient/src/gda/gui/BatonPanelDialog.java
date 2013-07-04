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

package gda.gui;

import gda.jython.JythonServerFacade;
import gda.jython.authenticator.UserAuthentication;

import java.awt.Frame;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

class BatonPanelDialog extends JDialog implements PropertyChangeListener {
	private JTextField textField;
	private JPasswordField textField2;
	private JOptionPane optionPane;

	private String btnString1 = "Enter";
	private String btnString2 = "Cancel";
	private boolean authenticated = false;
	private String username = "";

	public boolean getAuthenticated() {
		return authenticated;
	}

	/**
	 * @param aFrame
	 */
	public BatonPanelDialog(Frame aFrame) {
		super(aFrame, true);

		setTitle("Switch User");

		textField = new JTextField(10);
		textField2 = new JPasswordField(10);
		// textField2.setEchoChar('*');

		// Create an array of the text and components to be displayed.
		String msgString1 = "UserName";
		String msgString2 = "Password";
		Object[] array = { msgString1, textField, msgString2, textField2 };

		// Create an array specifying the number of dialog buttons
		// and their text.
		Object[] options = { btnString1, btnString2 };

		// Create the JOptionPane.
		optionPane = new JOptionPane(array, JOptionPane.INFORMATION_MESSAGE, JOptionPane.OK_CANCEL_OPTION, null,
				options, options[0]);

		// Make this dialog display it.
		setContentPane(optionPane);

		// Handle window closing correctly.
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent we) {
				optionPane.setValue(new Integer(JOptionPane.CLOSED_OPTION));
			}
		});

		// Ensure the text field always gets the first focus.
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentShown(ComponentEvent ce) {
				textField.requestFocusInWindow();
			}
		});

		optionPane.addPropertyChangeListener(this);
	}

	/**
	 * This method reacts to state changes in the option pane.
	 * 
	 * @param e
	 */
	@Override
	public void propertyChange(PropertyChangeEvent e) {
		String prop = e.getPropertyName();

		if (isVisible() && (e.getSource() == optionPane)
				&& (JOptionPane.VALUE_PROPERTY.equals(prop) || JOptionPane.INPUT_VALUE_PROPERTY.equals(prop))) {
			Object value = optionPane.getValue();

			if (value == JOptionPane.UNINITIALIZED_VALUE) {
				// ignore reset
				return;
			}

			// Reset the JOptionPane's value. If you don't do this, then if the user presses the same button next time,
			// no property change event will be fired.
			optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);

			// if pressed OK and the username entered is different to the current username
			if (btnString1.equals(value) && !textField.getText().equals(UserAuthentication.getUsername())) {
				authenticated = JythonServerFacade.getInstance().switchUser(textField.getText(),
						String.copyValueOf(textField2.getPassword()));
				if (authenticated) {
					username = textField.getText();
				}
				clearAndHide();
			} else { // user closed dialog or clicked cancel
				clearAndHide();
			}
		}
	}

	/** This method clears the dialog and hides it. */
	public void clearAndHide() {
		textField.setText(null);
		textField2.setText(null);
		setVisible(false);
	}

	public String getUserName() {
		return username;
	}
	
	@Override
	public void setVisible(boolean visible){
		if (visible){
			username = "";
			authenticated = false;
		}
		super.setVisible(visible);
	}
}
