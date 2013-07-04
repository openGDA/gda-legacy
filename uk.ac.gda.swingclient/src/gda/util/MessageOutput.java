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

package gda.util;

import gda.gui.GUIMessagePanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

/**
 * A class to create a message output window
 */
public class MessageOutput extends GUIMessagePanel implements MouseListener {
	private static JTextArea textOutput = null;

	private static String text = null;

	private static Color color = null;

	private JPopupMenu popup;

	private JScrollPane pane;

	private String name;

	/**
	 * Constructor to create a message output window
	 */
	public MessageOutput() {
	}

	/**
	 * Constructor to create a message output window
	 * 
	 * @param name
	 */
	public MessageOutput(String name) {
		this.name = name;
		configure();
	}

	@Override
	public void configure() {
		pane = new JScrollPane();
		textOutput = new JTextArea(5, 20);
		textOutput.setEditable(false);
		pane.setViewportView(textOutput);

		setLayout(new BorderLayout());
		/*
		 * setBorder( BorderFactory.createTitledBorder( BorderFactory.createEtchedBorder(), name, TitledBorder.LEFT,
		 * TitledBorder.TOP, null, Color.black));
		 */
		add(pane, BorderLayout.CENTER);
		popup = createPopup();
		textOutput.addMouseListener(this);
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	private JPopupMenu createPopup() {
		JMenuItem mi;
		popup = new JPopupMenu("Output");

		mi = popup.add(new JMenuItem("Clear"));
		mi.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				textOutput.setText("");
				notifyIObservers(MessageOutput.this, "Clear");
			}
		});

		return popup;
	}

	// MouseListener interface implementation.

	@Override
	public void mousePressed(MouseEvent me) {
		if (me.isPopupTrigger())
			popup.show(textOutput, me.getPoint().x, me.getPoint().y);
	}

	@Override
	public void mouseReleased(MouseEvent me) {
		if (me.isPopupTrigger())
			popup.show(textOutput, me.getPoint().x, me.getPoint().y);
	}

	@Override
	public void mouseClicked(MouseEvent me) {
		if (me.isPopupTrigger())
			popup.show(textOutput, me.getPoint().x, me.getPoint().y);
	}

	@Override
	public void mouseEntered(MouseEvent me) {
	}

	@Override
	public void mouseExited(MouseEvent me) {
	}

	/**
	 * Append text to the text area
	 * 
	 * @param text
	 *            the text to append to the text area
	 */
	public static void appendText(String text) {
		appendAndScrollToEnd(text);
	}

	/**
	 * @param text
	 * @param color
	 */
	public static void appendText(String text, Color color) {
		if (textOutput != null) {
			Color temp = textOutput.getForeground();
			textOutput.setForeground(color);
			appendAndScrollToEnd(text);
			textOutput.setForeground(temp);
		}
	}

	private static void appendAndScrollToEnd(String text) {
		if (textOutput != null) {
			textOutput.append(text + "\n");
			textOutput.setCaretPosition(textOutput.getDocument().getLength());
		}
	}

	/**
	 * @param txt
	 */
	public static void appendTextLater(String txt) {
		text = txt;
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				appendText(text);
			}
		});
	}

	/**
	 * @param txt
	 * @param colour
	 */
	public static void appendTextLater(String txt, Color colour) {
		text = txt;
		color = colour;
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				appendText(text, color);
			}
		});
	}

}
