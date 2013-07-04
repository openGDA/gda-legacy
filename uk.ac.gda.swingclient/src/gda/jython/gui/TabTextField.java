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

import java.awt.AWTKeyStroke;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Collections;
import java.util.Set;

import javax.swing.JTextField;

/**
 * An extension of the JTextField which adds a tab to the end of its text rather than losing focus when the tab button
 * is pressed. Used by the JythonTerminal class as the tab character is part of the Jython syntax and tabs need to be
 * entered.
 */
class TabTextField extends JTextField {

	// Constructors
	/**
	 * Constructor.
	 */
	public TabTextField() {
		super();
		init();
	}

	/**
	 * Constructor with initial text.
	 * 
	 * @param text
	 */
	public TabTextField(String text) {
		super(text);
		init();
	}

	private void addTab() {
		try {
			this.getDocument().insertString(this.getCaretPosition(), "\t", null);
		} catch (Exception ex) {
		}

	}

	private void init() {
		// remove key binding to traverse focus from this component
		Set<AWTKeyStroke> s = Collections.emptySet();
		this.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, s);
		// add a key event listener that will observe tab keys
		addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent event) {
				// look for tab keys
				if (event.getKeyCode() == KeyEvent.VK_TAB || event.getKeyChar() == '\t') {
					addTab();
					event.consume();
				}
			}
		});
	}
}
