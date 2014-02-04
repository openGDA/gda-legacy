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

import gda.gui.JViewportWithScrollControl;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.text.AttributeSet;

import ch.qos.logback.classic.spi.LoggingEvent;

/**
 * This class picks up all gda logging message from logback. The instances are created by LogBack on processing the
 * configuration files. However the panel is added to the LogPanel object which expects this to act as a singleton. For
 * this reason the panel
 * 
 * @param <E>
 */
public class LogPanelAppender<E extends LoggingEvent> extends PanelAppenderBase<E> {
	private static JPanel panel = null;

	private static JTextPane textOutput;

	/**
	 * Constructor
	 */
	public LogPanelAppender() {
		panel = SingleLogPanelAppender.getPanel();
		SingleLogPanelAppender.getPane();
		textOutput = SingleLogPanelAppender.getTextOutput();
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
	
	public static void clearPanel() {
		textOutput.setText("");
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
		SingleLogPanelAppender.getTextOutput().setSelectionStart(textOutput.getDocument().getLength());
		textOutput.setSelectionEnd(textOutput.getDocument().getLength());
	}

}

class SingleLogPanelAppender {
	private static SingleLogPanelAppender singleton = null;

	private static JPanel panel = null;

	private static JScrollPane pane;

	private static JTextPane textOutput;

	private static SingleLogPanelAppender getInstance() {
		return singleton != null ? singleton : (singleton = new SingleLogPanelAppender());
	}

	private SingleLogPanelAppender() {
		panel = new JPanel(new BorderLayout());
		
		pane = new JScrollPane() {
			@Override
			protected JViewport createViewport() {
				return new JViewportWithScrollControl();
			}
		};
		
		textOutput = new JTextPane();
		textOutput.setEditable(false);
		pane.setViewportView(textOutput);
		panel.add(pane, BorderLayout.CENTER);
	}
	
	public static void setScrollingEnabled(boolean enabled) {
		JViewportWithScrollControl viewport = (JViewportWithScrollControl) pane.getViewport();
		viewport.setScrollingEnabled(enabled);
	}
	
	/**
	 * @return JPanel
	 */
	public static JPanel getPanel() {
		getInstance();
		return panel;
	}

	/**
	 * @return JScrollPane
	 */
	public static JScrollPane getPane() {
		getInstance();
		return pane;
	}

	/**
	 * @return JTextPane
	 */
	public static JTextPane getTextOutput() {
		getInstance();
		return textOutput;
	}
}
