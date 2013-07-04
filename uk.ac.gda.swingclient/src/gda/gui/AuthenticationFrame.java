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

package gda.gui;

import gda.configuration.properties.LocalProperties;
import gda.icons.GdaIcons;
import gda.jython.authenticator.UserAuthentication;
import gda.util.MultiScreenSupport;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

/**
 * Construct an authentication panel for logging onto the GDA.
 */
public class AuthenticationFrame extends JFrame {

	private static AuthenticationFrame theInstance = null; // @jve:decl-index=0:visual-constraint="530,12"

	private JButton okButton = null;

	private JButton cancelButton = null;

	ButtonGroup buttonGroup = new ButtonGroup();

	private JRadioButton useRadioBtn = null;

	private JRadioButton elseRadioBtn = null;

	private JTextField usernameTxtField = null;

	private JTextField passwordTxtField = null;

	private int screenIndex;

	/**
	 * This method initializes
	 */
	private AuthenticationFrame() {
		super("Login to GDA");
		// screen index for the primary screen starting from 0.
		screenIndex = LocalProperties.getInt("gda.screen.primary", 0);
		initialize();
	}

	/**
	 * Get a singleton instance of this class.
	 * 
	 * @return a singleton instance of this class
	 */
	public static AuthenticationFrame getInstance() {
		if (theInstance == null) {
			theInstance = new AuthenticationFrame();
		}
		return theInstance;
	}

	@Override
	public void setVisible(boolean visible) {
		if (visible) {
			// quick method to place the frame to the (rough) centre of screen
			this.pack();
			setLocation();
			okButton.requestFocusInWindow();
		}
		super.setVisible(visible);
	}

	private void setLocation() {
		int xLocation = 0;
		int yLocation = 0;

		if (LocalProperties.get("gda.screen.primary") != null) {
			MultiScreenSupport mss = new MultiScreenSupport();
			int x = mss.getScreenXoffset(screenIndex);
			int y = mss.getScreenYoffset(screenIndex);
			int width = mss.getScreenWidth(screenIndex);
			int height = mss.getScreenHeight(screenIndex);
			xLocation = x + width / 2;
			yLocation = y + height / 2;
		} else {
			int nosOfScreens = LocalProperties.getInt("gda.gui.nosOfScreens", 1);
			String displayingScreen = LocalProperties.get("gda.gui.displayingScreen", "top");
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			xLocation = screenSize.width / 2;
			yLocation = screenSize.height / 2;
			if (nosOfScreens == 2) {
				if ("bottom".equalsIgnoreCase(displayingScreen)) {
					xLocation = screenSize.width / 2;
					yLocation = (screenSize.height * 3) / 4;
				} else if ("top".equalsIgnoreCase(displayingScreen)) {
					xLocation = screenSize.width / 2;
					yLocation = screenSize.height / 4;
				} else if ("right".equalsIgnoreCase(displayingScreen)) {
					xLocation = (screenSize.width * 3) / 4;
					yLocation = screenSize.height / 2;
				} else if ("left".equalsIgnoreCase(displayingScreen)) {
					xLocation = screenSize.width / 4;
					yLocation = screenSize.height / 2;
				}
			} else if (nosOfScreens == 4) {
				if ("bottomleft".equalsIgnoreCase(displayingScreen)) {
					xLocation = screenSize.width / 4;
					yLocation = (screenSize.height * 3) / 4;
				} else if ("topleft".equalsIgnoreCase(displayingScreen)) {
					xLocation = screenSize.width / 4;
					yLocation = screenSize.height / 4;
				} else if ("bottomright".equalsIgnoreCase(displayingScreen)) {
					xLocation = (screenSize.width * 3) / 4;
					yLocation = (screenSize.height * 3) / 4;
				} else if ("topright".equalsIgnoreCase(displayingScreen)) {
					xLocation = (screenSize.width * 3) / 4;
					yLocation = screenSize.height / 4;
				}
			}
		}
		// Place logo in centre of displaying screen
		Dimension d = getPreferredSize();
		int posx = xLocation - (d.width / 2);
		int posy = yLocation - (d.height / 2);

		setLocation(posx, posy);
	}

	/**
	 * This method initialises this
	 */
	private void initialize() {

		this.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
		this.setTitle("Login to the GDA");
		this.add(new JLabel("You are currently logged in as:"));
		this.add(new JLabel(System.getProperty("user.name")));

		buttonGroup = new ButtonGroup();
		useRadioBtn = new JRadioButton("log in to the GDA using this account", true);
		elseRadioBtn = new JRadioButton("use account:");
		elseRadioBtn.setFocusable(false);
		buttonGroup.add(useRadioBtn);
		buttonGroup.add(elseRadioBtn);
		this.add(useRadioBtn);
		this.add(elseRadioBtn);

		JPanel cred = new JPanel();
		cred.setLayout(new GridLayout(2, 2, 10, 10));
		cred.add(new JLabel("Username:"));
		cred.add(getUsernameTxtField());
		cred.add(new JLabel("Password:"));
		cred.add(getPasswordTxtField());
		this.add(cred);
		cred.setPreferredSize(new java.awt.Dimension(154, 50));
		getContentPane().add(getOkButton());

		this.add(getCancelButton());

		this.setSize(new Dimension(300, 200));
		this.setMinimumSize(new Dimension(300, 200));
		this.setPreferredSize(new Dimension(300, 200));

		setIconImage(GdaIcons.getWindowIcon());
	}

	private void userDeclined() {
		UserAuthentication.clearValues();
		this.setVisible(false);
	}

	private void setUserChoices() {
		if (useRadioBtn.isSelected()) {
			UserAuthentication.setToUseOSAuthentication();
		} else {
			UserAuthentication.setToNotUseOSAuthentication(usernameTxtField.getText().trim(), passwordTxtField
					.getText());
		}
	}

	/**
	 * This method initializes okButton
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getOkButton() {
		if (okButton == null) {
			okButton = new JButton();
			okButton.setText("OK");
			okButton.setMinimumSize(new Dimension(50, 30));
			okButton.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					okButtonPressed();
				}
			});
			okButton.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("ENTER"), "enter");
			okButton.getActionMap().put("enter", new AbstractAction() {
				@Override
				public void actionPerformed(ActionEvent e) {
					okButtonPressed();
				}
			});
		}
		return okButton;
	}

	private void okButtonPressed() {
		if (buttonGroup.isSelected(elseRadioBtn.getModel())
				&& (usernameTxtField.getText().equals("") || passwordTxtField.getText().equals(""))) {
			return;
		}
		setUserChoices();
		AuthenticationFrame.this.setVisible(false);
	}

	/**
	 * This method initializes cancelButton
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getCancelButton() {
		if (cancelButton == null) {
			cancelButton = new JButton();
			cancelButton.setText("Cancel");
			cancelButton.setMinimumSize(new Dimension(60, 30));
			cancelButton.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					userDeclined();
				}
			});
		}
		return cancelButton;
	}

	/**
	 * This method initializes usernameTxtField
	 * 
	 * @return javax.swing.JTextField
	 */
	private JTextField getUsernameTxtField() {
		if (usernameTxtField == null) {
			usernameTxtField = new JTextField();
			usernameTxtField.setPreferredSize(new java.awt.Dimension(30, 20));
			usernameTxtField.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent evt) {
					buttonGroup.setSelected(elseRadioBtn.getModel(), true);
				}
			});
			usernameTxtField.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							buttonGroup.setSelected(elseRadioBtn.getModel(), true);
							getPasswordTxtField().requestFocus();
						}
					});
				}
			});
		}
		return usernameTxtField;
	}

	/**
	 * This method initializes passwordTxtField
	 * 
	 * @return javax.swing.JTextField
	 */
	private JTextField getPasswordTxtField() {
		if (passwordTxtField == null) {
			passwordTxtField = new JPasswordField();
			passwordTxtField.setPreferredSize(new java.awt.Dimension(30, 20));
			passwordTxtField.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					setUserChoices();
					okButtonPressed();
				}
			});

		}
		return passwordTxtField;
	}
} 
