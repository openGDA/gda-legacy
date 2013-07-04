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

package gda.gui.text.TextArea;

import java.awt.GridBagConstraints;
import java.io.PrintWriter;

import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;

/**
 * A JPanel that contains a SimpleLogger JTextArea into which output can be written. The whole object is scrollable
 */
public class SimpleScrollableLoggerPane extends LoggerPane {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public final JScrollPane scrollPane;

	private final SimpleLogger simpleLogger;

	private final PrintWriter printWriter;

	private final JTextArea simpleLoggerTextArea;

	/**
	 * Constructor
	 * 
	 * @param title
	 */
	public SimpleScrollableLoggerPane(String title) {
		super();

		simpleLoggerTextArea = new JTextArea();
		simpleLoggerTextArea.setLineWrap(true);
		simpleLogger = new SimpleLogger(simpleLoggerTextArea);
		printWriter = new PrintWriter(simpleLogger);
		scrollPane = new JScrollPane(simpleLoggerTextArea);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		setLayout(new java.awt.GridLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = c.gridy = 0;
		c.fill = GridBagConstraints.BOTH;
		add(scrollPane, c);
		if (title != null) {
			setBorder(BorderFactory.createTitledBorder(title));
		}

	}

	@Override
	public JScrollPane getScrollPane() {
		return scrollPane;
	}
	
	/**
	 * @return A PrintWriter interface to the pane
	 */
	public PrintWriter getPrintWriter() {
		return printWriter;
	}

	/**
	 * @return A SimpleLogger interface to the pane
	 */
	public SimpleLogger getSimpeLogger() {
		return simpleLogger;
	}

	/**
	 * Scrolls the pane down to the last line of output
	 */
	public void ResizeContent() {
		simpleLoggerTextArea.getCaret().setDot(simpleLoggerTextArea.getText().length());
		scrollPane.scrollRectToVisible(simpleLoggerTextArea.getVisibleRect());
	}

	@Override
	public void clearStatus() {
		getSimpeLogger().clear();
		ResizeContent();
	}

	@Override
	public void updateStatus(String status) {
		getPrintWriter().println(status);
		ResizeContent();
	}	
}
