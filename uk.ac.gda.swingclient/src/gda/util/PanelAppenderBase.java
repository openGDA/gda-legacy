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

package gda.util;

import java.awt.Color;

import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.Layout;

/**
 * Base class for panels that display log messages.
 * 
 * @param <E>
 */
public abstract class PanelAppenderBase<E> extends AppenderBase<E> implements PanelAppender {
	
	static SimpleAttributeSet RED = new SimpleAttributeSet();

	static SimpleAttributeSet ORANGE = new SimpleAttributeSet();

	static SimpleAttributeSet BLACK = new SimpleAttributeSet();

	static SimpleAttributeSet DARK_GRAY = new SimpleAttributeSet();

	static {
		StyleConstants.setForeground(RED, Color.red);
		StyleConstants.setFontFamily(RED, "Helvetica");
		StyleConstants.setFontSize(RED, 12);

		StyleConstants.setForeground(ORANGE, Color.orange);
		StyleConstants.setBold(ORANGE, true);
		StyleConstants.setFontFamily(ORANGE, "Helvetica");
		StyleConstants.setFontSize(ORANGE, 12);

		StyleConstants.setForeground(BLACK, Color.black);
		StyleConstants.setFontFamily(BLACK, "Helvetica");
		StyleConstants.setFontSize(BLACK, 12);

		StyleConstants.setForeground(DARK_GRAY, Color.darkGray);
		StyleConstants.setFontFamily(DARK_GRAY, "Helvetica");
		StyleConstants.setFontSize(DARK_GRAY, 12);
	}
	
	@Override
	public Layout<E> getLayout() {
		return layout;
	}

	@Override
	public void setLayout(Layout<E> layout) {
		this.layout = layout;
	}
	
	@Override
	public void start() {
		if (layout == null) {
			addError("No layout set for the appender named [" + name + "].");
			return;
		}
		super.start();
	}

	protected int maximumLength = 100000;

	protected abstract void setEndSelection();
	
	protected abstract void setTopSelection();
	
	protected abstract void insertTextAtEnd(String text, AttributeSet set);
	
	protected abstract void insertTextAtTop(String text, AttributeSet set);
	
	protected void appendText(JTextPane textOutput, String text, Color color) {
		if (textOutput != null) {
			// set the view focus
			// textOutput.setCaretPosition(textOutput.getDocument().getLength());
			setEndSelection();
			if (color == Color.red) {
				insertTextAtEnd(text, RED);
			} else if (color == Color.orange) {
				insertTextAtEnd(text, ORANGE);
			} else if (color == Color.black) {
				insertTextAtEnd(text, BLACK);
			} else if (color == Color.darkGray) {
				insertTextAtEnd(text, DARK_GRAY);
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void append(Object arg0) {
		LoggingEvent event = (LoggingEvent) arg0;
		Color color = Color.magenta;
		Level level = event.getLevel();
		if (level == Level.ERROR) {
			color = Color.red;
		} else if (level == Level.WARN) {
			color = Color.orange;
		} else if (level == Level.INFO) {
			color = Color.black;
		} else if (level == Level.DEBUG) {
			color = Color.darkGray;
		}

		appendTextLater(layout.doLayout((E) event), color);
	}

	protected void prependText(JTextPane textOutput, String text, Color color) {
		if (textOutput != null) {
			// set the view focus
			// textOutput.setCaretPosition(0);
			setTopSelection();
			if (color == Color.red) {
				insertTextAtTop(text, RED);
			} else if (color == Color.orange) {
				insertTextAtTop(text, ORANGE);
			} else if (color == Color.black) {
				insertTextAtTop(text, BLACK);
			} else if (color == Color.darkGray) {
				insertTextAtTop(text, DARK_GRAY);
			}
		}
	}
	
	protected void insertTextAtTop(JTextPane textOutput, String text, AttributeSet set) {
		try {
			DefaultStyledDocument d = (DefaultStyledDocument) textOutput.getDocument();
			int offset = d.getLength();
			if (offset > maximumLength) {
				d.remove(offset - maximumLength / 10, maximumLength / 10);
				// d.replace(0, offset, null, null);
				// offset = 0;
			}
			d.insertString(0, text, set);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}
	
	protected void insertTextAtEnd(JTextPane textOutput, String text, AttributeSet set) {
		try {
			DefaultStyledDocument d = (DefaultStyledDocument) textOutput.getDocument();
			int offset = d.getLength();
			if (offset > maximumLength) {
				d.remove(0, maximumLength / 10);
				// d.replace(0, offset, null, null);
				offset = d.getLength();
			}
			d.insertString(offset, text, set);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}

	/**
	 * A {@link Runnable} task that appends text to a panel.
	 */
	public class AppendRunner implements Runnable {
		String text;

		Color color;

		/**
		 * @param text
		 * @param color
		 */
		public AppendRunner(String text, Color color) {
			this.text = text;
			this.color = color;
		}

		@Override
		public void run() {
			appendText(text, color);
		}
	}
}
