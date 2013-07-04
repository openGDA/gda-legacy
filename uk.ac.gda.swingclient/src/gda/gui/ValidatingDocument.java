/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package gda.gui;

import gda.gui.tables.CharacterValidator;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

/**
 * Subclass of {@link PlainDocument} that imposes a maximum length on the text
 * entered into a text field, and can validate characters.
 */
public class ValidatingDocument extends PlainDocument {
	
	private boolean hasMaximumLength;
	
	private int maximumLength;

	public void setMaximumLength(int length) {
		this.maximumLength = length;
		hasMaximumLength = true;
	}
	
	private CharacterValidator characterValidator;
	
	public void setCharacterValidator(CharacterValidator validator) {
		this.characterValidator = validator;
	}
	
	@Override
	public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
		
		// check max length won't be exceeded
		if (hasMaximumLength) {
			final int newLength = getLength() + str.length();
			if (newLength > maximumLength) {
				return;
			}
		}
		
		// check new characters are valid
		if (characterValidator != null && !characterValidator.areCharactersValid(str)) {
			return;
		}
		
		super.insertString(offs, str, a);
	}
	
}
