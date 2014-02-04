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

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.AttributeSet;

import ch.qos.logback.classic.spi.LoggingEvent;

/**
 * This class display gda logging message on DebugPanel from the gda servers and clients.
 * 
 * @param <E>
 */
public class DebugPanelAppender<E extends LoggingEvent> extends PanelAppenderBase<E> {
	private static JPanel panel;
	private JScrollPane pane;
	private JTextPane textOutput;

	/**
	 * Constructor
	 */
	public DebugPanelAppender() {
		panel = new JPanel(new BorderLayout());
		pane = new JScrollPane();

		textOutput = new JTextPane();
		// insertTextAtEnd("GDA output here: \n", BLACK);

		textOutput.setEditable(false);
		pane.setViewportView(textOutput);

		panel.add(pane, BorderLayout.CENTER);
	}

	@Override
	public void appendText(String text, Color color) {
		appendText(textOutput, text, color);
	}

	@Override
	public void prependText(String text, Color color) {
		prependText(textOutput, text, color);
	}

	@Override
	public void appendTextLater(String text, Color color) {
		SwingUtilities.invokeLater(new AppendRunner(text, color));
	}

	/**
	 * @return panel
	 */
	public static JPanel getPanel() {
		return panel;
	}

	@Override
	protected void insertTextAtTop(String text, AttributeSet set) {
		insertTextAtTop(textOutput, text, set);
	}

	@Override
	protected void insertTextAtEnd(String text, AttributeSet set) {
		insertTextAtEnd(textOutput, text, set);
	}

	@Override
	protected void setTopSelection() {
		textOutput.setSelectionStart(0);
		textOutput.setSelectionEnd(0);
	}

	// Needed for inserting text/icons in the right places
	@Override
	protected void setEndSelection() {
		textOutput.setSelectionStart(textOutput.getDocument().getLength());
		textOutput.setSelectionEnd(textOutput.getDocument().getLength());
	}

}
