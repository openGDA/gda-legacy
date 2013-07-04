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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A subpanel used inside JythonEditor displaying a single file for editing.
 */
public class JythonEditorScriptEditor extends JPanel {
	private static final Logger logger = LoggerFactory.getLogger(JythonEditorScriptEditor.class);

	/**
	 * If the document has been edited since its last save
	 */
	public boolean isEdited = false;

	/**
	 * If the document has ever been saved
	 */
	public boolean isSaved = true;

	private LineNumberedPaper textEdit = new LineNumberedPaper();

	private JScrollPane jScrollPane1 = new JScrollPane();

	private BorderLayout borderLayout1 = new BorderLayout();

	private String fileName = "";

	private File file = null;

	private JythonEditor editor = null;

	private UndoManager manager = new UndoManager();

	/**
	 * @param editor
	 *            the JythonEditor panel which this object is displayed inside.
	 */
	public JythonEditorScriptEditor(JythonEditor editor) {
		try {
			this.editor = editor;
			jbInit();
			textEdit.setDocument(new CodeDocument());
			textEdit.getDocument().addDocumentListener(new JythonEditor_textEdit_documentAdapter(this));

			// Registering the undo manager as Listener
			textEdit.getDocument().addUndoableEditListener(new JythonEditorUndoableListener(manager));

			// make control-s save the current file
			this.getActionMap().put("ctrl_s", new AbstractAction("ctrl_s") {
				@Override
				public void actionPerformed(ActionEvent evt) {
					if (isEdited()) {
						saveFile();
					} else {
						saveAs();
					}
					getEditor().setButtons();
				}
			});
			this.getInputMap().put(KeyStroke.getKeyStroke("control S"), "ctrl_s");
			textEdit.getActionMap().put("ctrl_s", new AbstractAction("ctrl_s") {
				@Override
				public void actionPerformed(ActionEvent evt) {
					if (file == null) {
						saveAs();
					} else {
						saveFile();
					}
					getEditor().setButtons();
				}
			});
			textEdit.getInputMap().put(KeyStroke.getKeyStroke("control S"), "ctrl_s");

			// make control-o open a file
			this.getActionMap().put("ctrl_o", new AbstractAction("ctrl_o") {
				@Override
				public void actionPerformed(ActionEvent evt) {
					openNew();
				}
			});
			this.getInputMap().put(KeyStroke.getKeyStroke("control O"), "ctrl_o");
			textEdit.getActionMap().put("ctrl_o", new AbstractAction("ctrl_o") {
				@Override
				public void actionPerformed(ActionEvent evt) {
					openNew();
				}
			});
			textEdit.getInputMap().put(KeyStroke.getKeyStroke("control O"), "ctrl_o");

			textEdit.setCaretPosition(0);

			// accelerator keys for undo and redo#
			textEdit.getInputMap().put(KeyStroke.getKeyStroke("control Z"), "ctrl_z");
			textEdit.getActionMap().put("ctrl_z", new AbstractAction("ctrl_z") {

				@Override
				public void actionPerformed(ActionEvent e) {
					undo();

				}

			});

			textEdit.getInputMap().put(KeyStroke.getKeyStroke("control Y"), "ctrl_y");
			textEdit.getActionMap().put("ctrl_y", new AbstractAction("ctrl_y") {

				@Override
				public void actionPerformed(ActionEvent e) {
					redo();
				}

			});

			textEdit.getInputMap().put(KeyStroke.getKeyStroke("control shift Z"), "ctrl_shift_z");
			textEdit.getActionMap().put("ctrl_shift_z", new AbstractAction("ctrl_shift_z") {

				@Override
				public void actionPerformed(ActionEvent e) {
					redo();
				}

			});

		} catch (Exception e) {
			logger.debug(e.getStackTrace().toString());
		}

	}

	private void jbInit() {
		this.setLayout(borderLayout1);
		textEdit.setText("");
		this.add(jScrollPane1, java.awt.BorderLayout.CENTER);
		jScrollPane1.getViewport().add(textEdit);
	}

	/**
	 * @return the file this object is displaying.
	 */
	public File getFile() {
		return file;
	}

	/**
	 * @return the name of the file this object is displaying.
	 */
	public String getFileName() {
		return fileName;
	}
	
	public void setFileName(String filename) {
		this.fileName = filename;
	}

	/**
	 * @param file
	 *            the file this object should open and display
	 */
	public void setFile(File file) {
		this.file = file;
		setFileName(file.getName());
		isSaved = true;
		isEdited = false;
	}

	/**
	 * @return returns true if the file has been altered since its last save.
	 */
	public boolean isEdited() {
		return this.isEdited;
	}

	/**
	 * Opens the file defined by the setFile method.
	 */
	public void openFile() {
		try {
			textEdit.setText(readFile(file));
			textEdit.setCaretPosition(0);
			isSaved = true;
			isEdited = false;
		} catch (IOException ex) {
			// if get here, then act like vi and a blank file would be
			// opened
			// no need to throw an error
		}
	}

	/**
	 * @return the JythonEditor this object is inside.
	 */
	public JythonEditor getEditor() {
		return editor;
	}

	/**
	 * Closes the file, but if the file has been edited since its last save then prompts the user if the file should be
	 * saved before being closed.
	 * 
	 * @return close the file this object has open.
	 */
	public boolean close() {
		// if this has been edited, ask user if they want to save first
		boolean savedSuccessfully = true;
		if (isEdited) {
			if (JOptionPane.showConfirmDialog(this.getParent().getParent(),
					"Do you want to save changes to the current file?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
				savedSuccessfully = this.saveFile();
			}
		}
		if (savedSuccessfully) {
			setFileName("");
			file = null;
			textEdit.setText("");
			isSaved = true;
			isEdited = false;
			return true;
		}
		return false;
	}

	private boolean saveAs() {
		return this.editor.btnSaveAs_actionPerformed();
	}

	private void openNew() {
		this.editor.btnOpen_actionPerformed();
	}

	/*
	 * a BufferedReader wrapped around a FileReader to read the text data from the given file. @param file File @return
	 * String @throws IOException
	 */
	private String readFile(File file) throws IOException {

		StringBuffer fileBuffer;
		String fileString = null;
		String line;

		try {
			FileReader in = new FileReader(file);
			BufferedReader dis = new BufferedReader(in);
			fileBuffer = new StringBuffer();
			while ((line = dis.readLine()) != null) {
				fileBuffer.append(line + "\n");
			}
			in.close();
			fileString = fileBuffer.toString();
		} catch (IOException e) {
			throw e;
		}
		return fileString;
	}

	/**
	 * Writes a string into a newly created file.
	 * 
	 * @return boolean
	 */
	public boolean saveFile() {
		try {
			if (file != null) {
				PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file)));
				out.print(textEdit.getText());
				out.flush();
				out.close();
				isSaved = true;
				isEdited = false;
				return true;
			}
			return saveAs();
		} catch (IOException e) {
			logger.debug(e.getStackTrace().toString());
			return false;
		}
	}

	void textEdit_editPerformed() {
		isEdited = true;
		isSaved = false;
		editor.setButtons();
	}

	/**
	 * Undo the last edit made.
	 */
	public void undo() {
		try {
			manager.undo();
		} catch (CannotUndoException e) {
			// Deliberately left blank
		}

	}

	/**
	 * Redo the last edit made.
	 */
	public void redo() {
		try {
			manager.redo();
		} catch (CannotRedoException e) {
			// Deliberately left blank
		}

	}

}

class JythonEditor_textEdit_documentAdapter implements DocumentListener {
	JythonEditorScriptEditor adaptee;

	/**
	 * @param adaptee -
	 *            the enclosing JythonEditorScriptEditor
	 */
	public JythonEditor_textEdit_documentAdapter(JythonEditorScriptEditor adaptee) {
		this.adaptee = adaptee;
	}

	@Override
	public void insertUpdate(DocumentEvent evt) {
		adaptee.textEdit_editPerformed();
	}

	@Override
	public void removeUpdate(DocumentEvent evt) {
		adaptee.textEdit_editPerformed();
	}

	@Override
	public void changedUpdate(DocumentEvent evt) {
		adaptee.textEdit_editPerformed();
	}
}
