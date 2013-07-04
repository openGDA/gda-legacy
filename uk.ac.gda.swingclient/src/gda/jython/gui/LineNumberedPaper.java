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

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Component;
import javax.swing.plaf.ComponentUI;
import javax.swing.JTextPane;

/**
 * Draws line numbers next to each line, in the same font as the text. * Based upon the comment in
 * {@link #getInsets(Insets) getInsets} maybe the * "line numbering" could be a border?
 */
public class LineNumberedPaper extends JTextPane {

	/** * The line number should be right justified. */
	public static int RIGHT_JUSTIFY = 0;

	/** * The line number should be left justified. */
	public static int LEFT_JUSTIFY = 1;

	/** * Indicates the justification of the text of the line number. */
	private int lineNumberJustification = RIGHT_JUSTIFY;

	/**
	 * Constructor.
	 */
	public LineNumberedPaper() {
		super();
		setOpaque(false);
		// for the JythonEditor monospaced is very useful
		this.setFont(new Font("monospaced", Font.PLAIN, 12));
	}

	// if this is NOT opaque...then painting is a problem...
	// basically...this draws the line numbers...
	// but...super.paintComponent()...erases the background...and the
	// line numbers...what to do?
	// 
	// "workaround": paint the background in
	// this class... }
	@Override
	public Insets getInsets() {
		return getInsets(new Insets(0, 0, 0, 0));
	}

	/**
	 * * This modifies the insets, by adding space for the line number on the * left. Should be modified to add space on
	 * the right, depending upon * Locale.
	 * 
	 * @param insets
	 *            Insets
	 * @return Insets
	 */
	@Override
	public Insets getInsets(Insets insets) {
		insets = super.getInsets(insets);
		insets.left += lineNumberWidth();
		return insets;
	}

	/**
	 * @return int
	 */
	public int getLineNumberJustification() {
		return lineNumberJustification;
	}

	/**
	 * @param justify
	 *            int
	 */
	public void setLineNumberJustification(int justify) {
		if (justify == RIGHT_JUSTIFY || justify == LEFT_JUSTIFY) {
			lineNumberJustification = justify;
		}
	}

	/*
	 * Returns the width, in pixels, of the maximum line number, plus a trailing space. @return int
	 */
	private int lineNumberWidth() {
		//      
		// note: should this be changed to use all nines for the lineCount?
		// for example, if the number of rows is 111...999 could be wider
		// (in pixels) in a proportionally spaced font...
		//      
		// int lineCount = Math.max(getRows(), getLineCount() + 1);
		int lineCount = getLineCount();
		return getFontMetrics(getFont()).stringWidth(lineCount + " ");
	}

	// // NOTE: This method is called every time the cursor blinks...
	// so...optimize (later and if possible) for speed...
	//   
	@Override
	public void paintComponent(Graphics g) {
		Insets insets = getInsets();
		Rectangle clip = g.getClipBounds();
		g.setColor(getBackground()); // see
		// note
		// in
		// constructor
		// about
		// this...
		g.fillRect(clip.x, clip.y, clip.width, clip.height); // do the line
		// numbers need
		// redrawn?
		if (clip.x < insets.left) {
			FontMetrics fm = g.getFontMetrics();
			int fontHeight = fm.getHeight();
			// starting location at the "top" of the page...
			// y is the starting baseline for the font...
			// should "font leading" be applied?
			int y = fm.getAscent() + insets.top;
			// // now determine if it is the "top" of the page...or
			// somewhere else
			// //
			int startingLineNumber = ((clip.y + insets.top) / fontHeight) + 1; //         
			// use any one of the following if's: // //
			if (startingLineNumber != 1)
				if (y < clip.y) { //            
					// not within the clip rectangle...move it...
					// determine how many fontHeight's there are between
					// y and clip.y...then add that many fontHeights //
					y = startingLineNumber * fontHeight;
					// - (fontHeight - fm.getAscent());
				}
			// // options: // . write the number rows in the document
			// (current)
			// . write the number of existing lines in the document (to do)
			// // see
			// getLineCount()
			// // determine which the "drawing" should end...
			// add fontHeight: make sure...part of the line number is drawn
			// //
			// could also do this by determining what the last line //
			// number to
			// draw.
			// then the "while" loop whould change accordingly. //
			int yend = y + clip.height + fontHeight; // base x position of
			// the
			// line number
			int lnxstart = insets.left;
			if (lineNumberJustification == LEFT_JUSTIFY) {
				// actual starting location of the string of a left
				// justified string...it's constant...
				// the right justified string "moves"...
				lnxstart -= lineNumberWidth();
			}
			g.setColor(getForeground()); //         
			// loop until out of the "visible" region... //
			// int length = ("" + Math.max(getRows(), getLineCount() +
			// 1)).length();
			int length = ("" + getLineCount()).length();
			while (y < yend) { // // options: // . left justify the line
				// numbers (current) // . right justify the
				// line number (to do) //
				if (lineNumberJustification == LEFT_JUSTIFY) {
					g.drawString(startingLineNumber + " ", lnxstart, y);
				} else { // right justify
					String label = padLabel(startingLineNumber, length, true);
					g.drawString(label, insets.left - fm.stringWidth(label), y);
				}
				y += fontHeight;
				startingLineNumber++;
			}
		} // draw
		// line
		// numbers?
		super.paintComponent(g);
	} // paintComponent

	/*
	 * Create the string for the line number. NOTE: The <tt>length</tt> param does not include the <em>optional</em>
	 * space added after the line number. @param lineNumber to stringize @param length the length desired of the string
	 * @param addSpace @return the line number for drawing
	 */
	private String padLabel(int lineNumber, int length, boolean addSpace) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(lineNumber);
		for (int count = (length - buffer.length()); count > 0; count--) {
			buffer.insert(0, ' ');
		}
		if (addSpace) {
			buffer.append(' ');
		}
		return buffer.toString();
	}

	private int getLineCount() {
		String text = this.getText();
		int charCt = text.length(); // The number of characters in the
		// text is just its length.

		int lineCt = 1;
		for (int i = 0; i < charCt; i++) {
			if (text.charAt(i) == '\n')
				lineCt++;
		}
		return Math.max(lineCt, 10);
	}

	/**
	 * overwritten to disallow line folding which would spoil the line numbering {@inheritDoc}
	 * 
	 * @see javax.swing.JEditorPane#getScrollableTracksViewportWidth()
	 */
	@Override
	public boolean getScrollableTracksViewportWidth() {
		Component parent = getParent();
		ComponentUI ui = getUI();

		return parent != null ? (ui.getPreferredSize(this).width <= parent.getSize().width) : true;
	}

} // LineNumberedPaper
