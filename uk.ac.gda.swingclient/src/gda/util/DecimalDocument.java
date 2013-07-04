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

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

/**
 * A class extending java.swing.PlainDocument which will allow only decimal floating-point numbers (+ or -), and numbers
 * of the form 123e45 to be entered. TextField components which use <b>setDocument</b> to use a DecimalDocument
 * instance should be prepared to handle some situations where the contents of the document are only a partly- formed
 * number (like '-' or '123e'). Such contents are allowed in order to make number input more user friendly. For
 * instance, if the number at any time contains only a '-' character, this is allowed. If it were not allowed, the user
 * would have to enter at least one digit of a negative number before going back and entering the negation character,
 * since '-' is obviously not a number on it's own.
 */
public class DecimalDocument extends PlainDocument {
	/**
	 * Constructor
	 */
	public DecimalDocument() {
		super();
	}

	/**
	 * Returns the value of the number in this document.
	 * 
	 * @return The value of the contents as a double.
	 */
	public double getValue() {
		Double value = new Double(0.0);

		try {
			String text = getText(0, getLength());
			if (text.equals(""))
				text = "0";

			if (text.endsWith("e") || text.endsWith("E")) {
				value = new Double(text.substring(0, (text.length() - 1)));
			} else if (text.endsWith("e-") || text.endsWith("E-")) {
				value = new Double(text.substring(0, (text.length() - 2)));
			} else if (text.startsWith(".")) {
				value = new Double("0" + text);
			} else {
				value = new Double(text);
			}
		} catch (BadLocationException ex) {
		}

		return (value.doubleValue());
	}

	/**
	 * Overrides <b>AbstractDocument.insertString</b>
	 * 
	 * @param offs
	 *            see <b>PlainDocument</b>
	 * @param str
	 *            see <b>PlainDocument</b>
	 * @param a
	 *            see <b>PlainDocument</b>
	 * @exception BadLocationException
	 *                Thrown if the substring is off the edge of the array.
	 * @see javax.swing.text.PlainDocument
	 */
	@Override
	public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
		String current_text = getText(0, getLength());
		String before_offset = current_text.substring(0, offs);
		String after_offset = current_text.substring(offs, current_text.length());
		String proposed_result = before_offset + str + after_offset;

		try {
			if (!isPartNumber(proposed_result))
				Double.valueOf(proposed_result);
			super.insertString(offs, str, a);
		} catch (NumberFormatException e) {
		}
	}

	/**
	 * Overrides <b>AbstractDocument.remove</b>
	 * 
	 * @param offs
	 *            see <b>PlainDocument</b>
	 * @param len
	 *            see <b>PlainDocument</b>
	 * @exception BadLocationException
	 *                Thrown if the substring is off the edge of the array.
	 * @see javax.swing.text.PlainDocument
	 */
	@Override
	public void remove(int offs, int len) throws BadLocationException {
		String current_text = getText(0, getLength());
		String before_offset = current_text.substring(0, offs);
		String after_offset = current_text.substring(len + offs, current_text.length());
		String proposed_result = before_offset + after_offset;

		try {
			if ((proposed_result.length() != 0) && !isPartNumber(proposed_result))
				Double.valueOf(proposed_result);
			super.remove(offs, len);
		} catch (NumberFormatException e) {
		}
	}

	/**
	 * private method used internally to ensure that partly-formed numbers are not chucked out.
	 * 
	 * @param num
	 *            the number to test
	 * @return true if part number
	 */
	private boolean isPartNumber(String num) {
		boolean partNumber = false;

		if ((num.compareTo("-") == 0)) {
			partNumber = true;
		} else if (num.startsWith(".") && (num.lastIndexOf(".") == 0)) {
			partNumber = true;
		} else if ((num.toUpperCase().endsWith("E")) && (num.toUpperCase().indexOf("E") == (num.length() - 1))) {
			partNumber = true;
		} else if ((num.toUpperCase().endsWith("E-")) && (num.toUpperCase().indexOf("E") == (num.length() - 2))) {
			partNumber = true;
		}

		return partNumber;
	}
}
