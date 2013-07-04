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

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

/**
 * Based on example 975 in Java Almanac but removes 10% from the beginning of the the field if the limit is reached..
 */
public class TextComponentSizeLimiter extends DocumentFilter {
	int maxSize;

	// limit is the maximum number of characters allowed.
	/**
	 * @param limit
	 */
	public TextComponentSizeLimiter(int limit) {
		maxSize = limit;
	}

	// This method is called when characters are inserted into the document
	@Override
	public void insertString(DocumentFilter.FilterBypass fb, int offset, String str, AttributeSet attr)
			throws BadLocationException {
		replace(fb, offset, 0, str, attr);
	}

	// This method is called when characters in the document are replace with
	// other characters
	@Override
	public void replace(DocumentFilter.FilterBypass fb, int offset, int length, String str, AttributeSet attrs)
			throws BadLocationException {
		int newLength = fb.getDocument().getLength() - length + str.length();

		// If textfield size is not too long just append
		if (newLength <= maxSize) {
			fb.replace(offset, length, str, attrs);
		}
		// If textfield size is too long, then trim 10% off beginning and add
		else {
			// trim
			fb.replace(0, maxSize / 10, null, null);

			// append
			fb.replace(offset, length, str, attrs);
		}
	}
}
