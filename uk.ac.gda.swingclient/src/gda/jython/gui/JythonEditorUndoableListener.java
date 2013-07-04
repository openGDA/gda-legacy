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

import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.AbstractDocument;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;

/**
 * Listens for undo events in JythonEditor.
 */
public class JythonEditorUndoableListener implements UndoableEditListener {
	private UndoManager manager;

	/**
	 * @param manager -
	 *            the undomanager used by the JythonEditor.
	 */
	public JythonEditorUndoableListener(UndoManager manager) {
		this.manager = manager;
	}

	@Override
	public void undoableEditHappened(UndoableEditEvent ev) {
		UndoableEdit edit = ev.getEdit();

		// Include this method to ignore syntax changes
		if (edit instanceof AbstractDocument.DefaultDocumentEvent
				&& ((AbstractDocument.DefaultDocumentEvent) edit).getType() == AbstractDocument.DefaultDocumentEvent.EventType.CHANGE) {
			return;
		}

		manager.addEdit(edit);

	}

}
