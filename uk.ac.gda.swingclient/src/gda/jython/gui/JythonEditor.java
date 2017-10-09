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

import gda.gui.AcquisitionPanel;
import gda.jython.Jython;
import gda.jython.JythonServerFacade;
import gda.jython.JythonServerStatus;
import gda.observable.IObserver;
import gda.util.exceptionUtils;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A visual component to allow the user to open, edit and run Jython scripts.
 */
public class JythonEditor extends AcquisitionPanel implements IObserver {
	
	private static final Logger logger = LoggerFactory.getLogger(JythonEditor.class);

	/**
	 * The name this object should always be known as within the GDA Finder
	 */
	public static final String NAME = "JythonEditor";

	private static final String TERMINALNAME = "JythonTerminal";

	/** Number of last new document created */
	private AtomicInteger lastNewFileNumber = new AtomicInteger();
	
	BorderLayout borderLayout1 = new BorderLayout();

	JPanel editorButtonsPanel = new JPanel();

	JButton btnSave = new JButton();

	JButton btnRun = new JButton();

	JButton btnSaveAs = new JButton();

	boolean scriptOrQueueRunning = false;

	boolean isConfigured = false;

	// the terminal this bean works with (this holds a reference to a Jython
	// interpreter and acts as a console for this Bean - cannot have
	// multiple Jython consoles)
	JythonServerFacade jythonFacade;

	// the list of filehandles which match the list of scripts to run
	// this must be kept uptodate with the list of scripts 'scriptList'
	// Vector<File> fileList = new Vector<File>();

	JButton btnNew = new JButton();

	JButton btnOpen = new JButton();

	JFileChooser fc = new JFileChooser();

	TitledBorder titledBorder1;

	BorderLayout borderLayout3 = new BorderLayout();

	BorderLayout borderLayout4 = new BorderLayout();

	JScrollPane jScrollPane1 = new JScrollPane();

	JTabbedPane editTab = new JTabbedPane();

	JButton btnClose = new JButton();

	// undo and redo buttons
	JButton undoButton = new JButton("undo");

	JButton redoButton = new JButton("redo");

	@Override
	public void configure() {
		try {
			if (!isConfigured) {
				jythonFacade = JythonServerFacade.getInstance();
				jythonFacade.addIObserver(this);

				fc.setCurrentDirectory(new File(jythonFacade.getDefaultScriptProjectFolder()));
				fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				fc.setFileFilter(new PythonFileFilter());
				createBlankEditor();
				isConfigured = true;
			}
		} catch (Exception ex) {
			exceptionUtils.logException(logger, "JythonEditor: error while configuring.", ex);
		}
	}

	/**
	 * 
	 */
	public JythonEditor() {
		try {
			jbInit();
		} catch (Exception ex) {
			exceptionUtils.logException(logger, "JythonEditor exception.", ex);
		}
		setLabel("Jython Editor");
	}

	@Override
	public void setName(String name) {
		// intentionally does nothing
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public void update(Object observable, Object changeCode) {
		/*
		 * To be called when the interpreter has finished running any script
		 */
		if (observable instanceof JythonServerFacade && changeCode instanceof JythonServerStatus) {
			JythonServerStatus newStatus = (JythonServerStatus) changeCode;

			// if script and queue stopped
			if (newStatus.scanStatus == Jython.IDLE && newStatus.scriptStatus == Jython.IDLE) {
				scriptOrQueueRunning = false;
				setButtons();
			}

			// if a script or a queue running
			if (newStatus.scriptStatus == Jython.RUNNING || newStatus.scanStatus == Jython.RUNNING) {
				scriptOrQueueRunning = true;
				setButtons();
			}
		}

	}

	/**
	 * Open a new script for editing
	 */
	protected void btnNew_actionPerformed() {
		createBlankEditor();
		int tabCount = editTab.getTabCount();
		editTab.setSelectedIndex(tabCount - 1);
		fc.setSelectedFile(new File(""));
	}

	/**
	 * Open a script for editing. If empty, it shouldn't open a blank one.
	 */
	protected void btnOpen_actionPerformed() {
		JythonEditorScriptEditor editor = getCurrentEditor();

		// open a file
		int returnVal = fc.showOpenDialog(this);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			// if the current tab is not seeing anything
			if (editor.getFileName().compareTo("") == 0) {
				editor.setFile(fc.getSelectedFile());
				editor.openFile();
			} else {
				addTab();
			}

			setButtons();
		}
	}

	/**
	 * Ensure that the enabled status of the buttons on this object are up to date.
	 */
	public void setButtons() {
		JythonEditorScriptEditor editor = getCurrentEditor();

		if (editor != null && editor.isEdited) {
			this.btnRun.setEnabled(false);
			this.btnSave.setEnabled(true);
			this.btnSaveAs.setEnabled(true);
			editTab.setTitleAt(editTab.getSelectedIndex(), editor.getFileName() + "*");
			fc.setSelectedFile(editor.getFile());
			if (editor.getFile() != null) {
				this.btnSave.setEnabled(true);
			} else {
				this.btnSave.setEnabled(false);
			}
		} else if (editor != null) {
			this.btnRun.setEnabled(true);
			this.btnSave.setEnabled(false);
			this.btnSaveAs.setEnabled(false);

			if (editor.getFileName().compareTo("") == 0) {
				editTab.setTitleAt(editTab.getSelectedIndex(), "");
			} else {
				editTab.setTitleAt(editTab.getSelectedIndex(), editor.getFileName());
			}
			fc.setSelectedFile(editor.getFile());
			// disable run button if a script or queue is running
			this.btnRun.setEnabled(!scriptOrQueueRunning);
		}
	}

	private void addTab() {
		JythonEditorScriptEditor sel = new JythonEditorScriptEditor(this);
		sel.setFile(fc.getSelectedFile());
		sel.openFile();

		editTab.insertTab(sel.getFileName(), null, sel, sel.getFileName(), editTab.getTabCount());
		editTab.setSelectedIndex(editTab.getTabCount() - 1);
	}

	private JythonEditorScriptEditor getCurrentEditor() {
		JythonEditorScriptEditor current = null;
		int tabNum = editTab.getSelectedIndex();
		if (tabNum >= 0) {
			current = ((JythonEditorScriptEditor) editTab.getComponentAt(tabNum));
		}
		return current;
	}

	private void createBlankEditor() {
		JythonEditorScriptEditor nextEditor = new JythonEditorScriptEditor(this);
		final String filename = "unsaved file " + lastNewFileNumber.incrementAndGet();
		nextEditor.setFileName(filename);
		editTab.add(nextEditor);
	}

	void btnRun_actionPerformed() {
		JythonEditorScriptEditor editor = getCurrentEditor();
		// button should only be enabled if the file has been saved
		if (editor != null && !editor.isEdited && editor.getFile() != null) {
			// disable this button to avoid multiple scripts running at the
			// same time.
			this.btnRun.setEnabled(false);
			this.btnSave.setEnabled(false);
			this.btnSaveAs.setEnabled(false);

			// run the file through the GDAJythonInterpreter
			jythonFacade.runScript(editor.getFile(), TERMINALNAME);
		}
	}

	boolean btnSave_actionPerformed() {
		JythonEditorScriptEditor editor = getCurrentEditor();
		if (editor != null) {
			boolean result = editor.saveFile();
			if (result == true) {
				setButtons();
			} else {
				JOptionPane.showMessageDialog(this, "File could not be saved in the chosen location.",
						"Error saving file", 0);
			}
			return result;
		}
		return false;
	}

	/**
	 * Save the current script in a new file
	 * 
	 * @return true if saved
	 */
	protected boolean btnSaveAs_actionPerformed() {
		JythonEditorScriptEditor editor = getCurrentEditor();

		if (editor != null) {

			// Set to a default name for save.
			if (editor.getFile() != null) {
				fc.setSelectedFile(editor.getFile());
			} else {
				fc.setSelectedFile(new File(""));
			}
			// Open chooser dialog
			int result = fc.showSaveDialog(this);

			if (result == JFileChooser.CANCEL_OPTION) {
				return false;
			} else if (result == JFileChooser.APPROVE_OPTION) {
				// if file selected has no suffix, add one
				String filename = fc.getSelectedFile().getName();
				if (filename.lastIndexOf('.') == -1) {
					// make a new one with the suffix
					editor.setFile(new File(fc.getSelectedFile().getPath() + ".py"));
				} else {
					editor.setFile(fc.getSelectedFile());
				}
				if (editor.getFile().exists()) {
					int response = JOptionPane.showConfirmDialog(null, "Overwrite existing file?", "Confirm Overwrite",
							JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
					if (response == JOptionPane.CANCEL_OPTION) {
						return false;
					}
				}

				return btnSave_actionPerformed();
			}
		}
		return false;
	}

	void btnClose_actionPerformed() {
		if (getCurrentEditor() != null && getCurrentEditor().close()) {
			if (editTab.getTabCount() > 1) {
				editTab.remove(getCurrentEditor());
			} else if (editTab.getTabCount() < 1) {
				createBlankEditor();
			}
		}
		setButtons();
	}

	void editTab_stateChanged() {
		setButtons();
	}

	/*
	 * UI initialisation. This code is created by JBuilder. To be able to edit the UI with JBuilder all design time
	 * edits to visual components must be made here. @throws Exception
	 */
	private void jbInit() {
		titledBorder1 = new TitledBorder(BorderFactory.createEtchedBorder(Color.white, new Color(165, 163, 151)),
				"Script");
		this.setLayout(borderLayout1);
		btnNew.setToolTipText("create a new blank file");
		btnNew.setText("New");
		btnNew.addActionListener(new JythonEditor_btnNew_actionAdapter(this));
		btnSave.setEnabled(false);
		btnSave.setToolTipText("saves changes");
		btnSave.setText("Save");
		btnSave.addActionListener(new JythonEditor_btnSave_actionAdapter(this));
		btnRun.setEnabled(false);
		btnRun.setToolTipText("runs the saved script");
		btnRun.setText("Run");
		btnRun.addActionListener(new JythonEditor_btnRun_actionAdapter(this));
		btnSaveAs.setEnabled(false);
		btnSaveAs.setToolTipText("saves changes");
		btnSaveAs.setText("Save As...");
		btnSaveAs.addActionListener(new JythonEditor_btnSaveAs_actionAdapter(this));
		btnOpen.setToolTipText("opens script for editing");
		btnOpen.setText("Open");
		btnOpen.addActionListener(new JythonEditor_btnOpen_actionAdapter(this));
		jScrollPane1.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		jScrollPane1.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		editTab.setTabPlacement(SwingConstants.BOTTOM);
		editTab.addChangeListener(new JythonEditor_editTab_changeAdapter(this));
		btnClose.setText("Close");
		btnClose.addActionListener(new JythonEditor_btnClose_actionAdapter(this));
		// Actionlistener for the buttons
		undoButton.addActionListener(new JythonEditor_undo_actionAdapter(this));
		redoButton.addActionListener(new JythonEditor_redo_actionAdapter(this));

		editorButtonsPanel.add(btnNew, null);
		editorButtonsPanel.add(btnOpen, null);
		editorButtonsPanel.add(btnSave, null);
		editorButtonsPanel.add(btnSaveAs, null);
		editorButtonsPanel.add(btnClose);
		editorButtonsPanel.add(btnRun, null);
		editorButtonsPanel.add(undoButton, null);
		editorButtonsPanel.add(redoButton, null);

		this.add(editorButtonsPanel, BorderLayout.SOUTH);
		this.add(editTab, BorderLayout.CENTER);
	}

	/**
	 * Undo last edit action in editor.
	 */
	public void undo_actionPerformed() {
		JythonEditorScriptEditor editor = getCurrentEditor();
		if (editor != null)
			editor.undo();

	}

	/**
	 * Redo last undone action in editor.
	 */
	public void redo_actionPerformed() {
		JythonEditorScriptEditor editor = getCurrentEditor();
		if (editor != null)
			editor.redo();

	}
}

// inner adapter classes to handle events

class JythonEditor_editTab_changeAdapter implements ChangeListener {
	private JythonEditor adaptee;

	JythonEditor_editTab_changeAdapter(JythonEditor adaptee) {
		this.adaptee = adaptee;
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		adaptee.editTab_stateChanged();
	}
}

class JythonEditor_btnClose_actionAdapter implements ActionListener {
	private JythonEditor adaptee;

	JythonEditor_btnClose_actionAdapter(JythonEditor adaptee) {
		this.adaptee = adaptee;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		adaptee.btnClose_actionPerformed();
	}
}

class JythonEditor_btnSave_actionAdapter implements java.awt.event.ActionListener {
	JythonEditor adaptee;

	JythonEditor_btnSave_actionAdapter(JythonEditor adaptee) {
		this.adaptee = adaptee;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		adaptee.btnSave_actionPerformed();
	}
}

class JythonEditor_btnNew_actionAdapter implements java.awt.event.ActionListener {
	JythonEditor adaptee;

	JythonEditor_btnNew_actionAdapter(JythonEditor adaptee) {
		this.adaptee = adaptee;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		adaptee.btnNew_actionPerformed();
	}
}

class JythonEditor_btnOpen_actionAdapter implements java.awt.event.ActionListener {
	JythonEditor adaptee;

	JythonEditor_btnOpen_actionAdapter(JythonEditor adaptee) {
		this.adaptee = adaptee;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		adaptee.btnOpen_actionPerformed();
	}
}

class JythonEditor_btnSaveAs_actionAdapter implements java.awt.event.ActionListener {
	JythonEditor adaptee;

	JythonEditor_btnSaveAs_actionAdapter(JythonEditor adaptee) {
		this.adaptee = adaptee;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		adaptee.btnSaveAs_actionPerformed();
	}
}

class JythonEditor_btnRun_actionAdapter implements java.awt.event.ActionListener {
	JythonEditor adaptee;

	JythonEditor_btnRun_actionAdapter(JythonEditor adaptee) {
		this.adaptee = adaptee;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		adaptee.btnRun_actionPerformed();
	}
}

class PythonFileFilter extends FileFilter {
	// Accept all directories and all python files.
	@Override
	public boolean accept(File f) {
		boolean found = false;
		if (f.isDirectory()) {
			found = true;
		}

		String extension = PythonFileFilter.getExtension(f);
		if (extension != null) {
			if (extension.equals("py")) {
				found = true;
			}
		}
		return found;
	}

	// The description of this filter
	@Override
	public String getDescription() {
		return "Python scripts (.py)";
	}

	private static String getExtension(File f) {
		String ext = null;
		String s = f.getName();
		int i = s.lastIndexOf('.');

		if (i > 0 && i < s.length() - 1) {
			ext = s.substring(i + 1).toLowerCase();
		}
		return ext;
	}

}

class JythonEditor_undo_actionAdapter implements java.awt.event.ActionListener {
	JythonEditor adaptee;

	JythonEditor_undo_actionAdapter(JythonEditor adaptee) {
		this.adaptee = adaptee;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		adaptee.undo_actionPerformed();
	}
}

class JythonEditor_redo_actionAdapter implements java.awt.event.ActionListener {
	JythonEditor adaptee;

	JythonEditor_redo_actionAdapter(JythonEditor adaptee) {
		this.adaptee = adaptee;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		adaptee.redo_actionPerformed();
	}
}
