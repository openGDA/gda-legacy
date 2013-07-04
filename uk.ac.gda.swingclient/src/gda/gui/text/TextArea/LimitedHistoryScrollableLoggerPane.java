/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

import java.awt.GridLayout;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Like {@link SimpleScrollableLoggerPane}, but limits the number of lines
 * displayed in the panel.
 */
public class LimitedHistoryScrollableLoggerPane extends LoggerPane {
	
	private static final Logger logger = LoggerFactory.getLogger(LimitedHistoryScrollableLoggerPane.class);
	
	private static final String lineSeparator = System.getProperty("line.separator");
	
	private final JTextArea textArea;
	private final JScrollPane scrollPane;
	
	private ThreadSafeRingBuffer<String> lines;
	
	public LimitedHistoryScrollableLoggerPane(String title, int linesToKeep) {
		
		textArea = new JTextArea();
		textArea.setLineWrap(true);
		
		scrollPane = new JScrollPane(textArea);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		
		setLayout(new GridLayout());
		add(scrollPane);
		
		if (title != null) {
			setBorder(BorderFactory.createTitledBorder(title));
		}
		
		this.lines = new ThreadSafeRingBuffer<String>(linesToKeep);
	}
	
	@Override
	public JScrollPane getScrollPane() {
		return scrollPane;
	}
	
	private AtomicBoolean dirty = new AtomicBoolean();
	
	@Override
	public void updateStatus(String line) {
		lines.add(line);
		dirty.set(true);
	}
	
	@Override
	public void clearStatus() {
		lines.clear();
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				textArea.setText("");
			}
		});
	}
	
	public void startUpdateThread() {
		Timer timer = new Timer();
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				if (dirty.getAndSet(false)) {
					updateDisplay();
				}
			}
		};
		timer.schedule(task, 0, 500);
	}
	
	private void updateDisplay() {
		List<String> latestLines = lines.getContent();
		
		final StringBuffer newTextAreaContent = new StringBuffer();
		for (String line : latestLines) {
			newTextAreaContent.append(line + lineSeparator);
		}
		
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					textArea.setText(newTextAreaContent.toString());
					resizeContent();
				}
			});
		} catch (Exception e) {
			logger.error("Couldn't update text area content", e);
		}
	}
	
	private void resizeContent() {
		textArea.getCaret().setDot(textArea.getText().length());
		scrollPane.scrollRectToVisible(textArea.getVisibleRect());
	}
}
