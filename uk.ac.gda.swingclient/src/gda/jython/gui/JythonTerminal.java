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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.text.BadLocationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import gda.factory.Finder;
import gda.gui.util.WatchPanel;
import gda.jython.IScanDataPointObserver;
import gda.jython.InterfaceProvider;
import gda.jython.Jython;
import gda.jython.JythonServerFacade;
import gda.jython.Terminal;
import gda.observable.IObserver;
import gda.scan.IScanDataPoint;
import gda.util.PropertyUtils;

/**
 * A visual component to hold a GDAJythonInterpreter. It is designed to be similar to a command terminal.
 */
public class JythonTerminal extends JPanel implements Runnable, IObserver, Terminal, IScanDataPointObserver {

	private static final Logger logger = LoggerFactory.getLogger(JythonTerminal.class);

	private static final String NORMALPROMPT = ">>>";

	private static final String ADDITONALINPUTPROMPT = "...";

	private static final String RAWINPUTPROMPT = "-->";

	// to make sure configure is only run once
	private boolean configured = false;

	static private final int maxCommandsToSave = 100;

	// the instance of the scripting mediator
	JythonServerFacade commandserver = null;

	// command history
	Vector<String> cmdHistory = new Vector<String>(0);

	int cmdHistory_index = 0;

	boolean runFromHistory = false;

	String commandFileName = null;

	// where output currently printing to in txtOutput (for \r characters)
	int caretPosition = 0;

	// the current command
	String currentCmd = new String();

	// print terminal output
	boolean printOutput = false;

	String printOutputFileName = "";

	FileWriter printOutputFile;

	Vector<String> terminalLineSources = new Vector<String>();


	//private ArrayList<String> shutterList = new ArrayList<String>();
	//private ArrayList<String> amplifierList = new ArrayList<String>();
	//private ArrayList<String> valueList = new ArrayList<String>();

	// GUI components
	Finder finder = Finder.getInstance();

	JPanel pnlOutput = new JPanel();

	JPanel pnlInput = new JPanel();

	BorderLayout borderLayout1 = new BorderLayout();

	BorderLayout borderLayout2 = new BorderLayout();

	BorderLayout borderLayout3 = new BorderLayout();

	JPanel pnlTerminal = new JPanel();

	BorderLayout borderLayout5 = new BorderLayout();

	TitledBorder titledBorder1;

	FlowLayout flowLayout1 = new FlowLayout();

	TitledBorder titledBorder2;

	JTextField txtPrompt = new JTextField();

	JScrollPane jScrollPane1 = new JScrollPane();


	/**
	 * The JTextArea which holds the user's latest command
	 */
	private JTextArea txtOutput = new JTextArea();

	JTextField txtInput = new TabTextField();
	AutoCompleter autoCompleter = new AutoCompleter(txtInput, this);

	JPanel pnlLeft = new JPanel();

	/**
	 * The JPanel which holds the watches
	 */
	public WatchPanel watches = new WatchPanel();

	BorderLayout borderLayout6 = new BorderLayout();

	BorderLayout borderLayout4 = new BorderLayout();

	private String userScriptDir = null;

	/**
	 * configures the class - needed as this can only be run after JythonServerFacade.getInstance will not fail
	 */
	public void configure() {
		if (!configured) {
			commandserver = JythonServerFacade.getInstance();
			commandserver.addIObserver(this);
			userScriptDir = commandserver.getDefaultScriptProjectFolder();
			autoCompleter.setJythonServerFacade(commandserver);
			configured = true;

			// start watches
			watches.configure();

			// (re)open the file containing the old command history
			try {
				commandFileName = LocalProperties.get("gda.jythonTerminal.commandHistory.path", userScriptDir);
				if (!(commandFileName.endsWith("\\") || commandFileName.endsWith("/"))) {
					commandFileName += System.getProperty("file.separator");
				}
				commandFileName += ".cmdHistory.txt";
				File commandFile = new File(commandFileName);

				// if the file exists, read its contents
				if (commandFile.exists()) {
					BufferedReader in = new BufferedReader(new FileReader(commandFile));
					String str = "";
					while ((str = in.readLine()) != null) {
						if (!(str.compareTo("") == 0)) {
							cmdHistory.add(str);
						}
					}
					in.close();

					// if we have read in more than 500 commands, then
					// reduce file
					// and array size down to 500. This is done here as
					// during
					// running
					// we want to write to file every command as quickly as
					// possible
					int numberToRemove = cmdHistory.size() - maxCommandsToSave;
					if (numberToRemove > 0) {
						for (int i = 0; i < numberToRemove; i++) {
							cmdHistory.removeElementAt(0);
						}

						// then rebuild file
						BufferedWriter out = new BufferedWriter(new FileWriter(commandFile));

						for (int i = 0; i < cmdHistory.size(); i++) {
							out.write(cmdHistory.get(i) + "\n");
						}
						out.close();
					}
					this.cmdHistory_index = cmdHistory.size();
				}

				// else make a new file
				else {
					commandFile.createNewFile();
				}
			} catch (Exception e) {
				logger.error("JythonTerminal: error configuring cmdHistory in " + commandFileName, e);
				commandFileName = null;
			}

			// print out the messages generated during startup
			appendOutput(commandserver.getStartupOutput());
		}
	}

	/**
	 * Remove this terminal from the command server
	 * observer list.
	 *
	 * This method should be called to ensure new terminals
	 * can be registered with the command server.
	 */
	public void dispose(){
		if (commandserver != null){
			commandserver.deleteIObserver(this);
			commandserver = null;
		}
	}

	@Override
	public String getName() {
		return JythonGuiConstants.TERMINALNAME;
	}

	/**
	 * Constructor.
	 */
	public JythonTerminal() {
		try {
			setName(JythonGuiConstants.TERMINALNAME);
			jbInit();
		} catch (Exception e) {
			logger.debug(e.getStackTrace().toString());
		}
	}

	@Override
	public void write(String output) {
		appendOutput(output);
	}

	/**
	 * This is intended to be called by hitting return while user is in the txtInput JTextField. This run method should
	 * not be called directly. It is only public to fulfil the Runnable interface requirements. {@inheritDoc}
	 *
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		// print out what was typed
		appendOutput(txtPrompt.getText() + txtInput.getText() + "\n");
		// if this is the start of a new command
		if (txtPrompt.getText().compareTo(NORMALPROMPT) == 0) {
			String typedCmd = txtInput.getText();
			// add the command to cmdHistory
			if (cmdHistory.size() == 0) {
				addCommandToHistory(typedCmd);
			} else if ((typedCmd.compareTo("") != 0)
					&& (typedCmd.compareTo(cmdHistory.get(cmdHistory.size() - 1)) != 0)) {
				addCommandToHistory(typedCmd);
			}
			if (cmdHistory_index != cmdHistory.size() - 2) {
				runFromHistory = true;
			}
			// run the command
			boolean needMore = commandserver.runsource(typedCmd, getName());
			// if not a complete Jython command
			if (needMore) {
				// change the prompt
				txtPrompt.setText(ADDITONALINPUTPROMPT);
				// save the command so far
				currentCmd = typedCmd;
				// clear the command-line
				txtInput.setText("");
				txtInput.setCaret(txtInput.getCaret());
			} else {
				currentCmd = "";
				// reset the cmdHistory pointer if we just added a new line
				cmdHistory_index = cmdHistory.size();
				runFromHistory = false;
				// clear the command-line
				txtInput.setText("");
				txtInput.setCaret(txtInput.getCaret());
			}
		}
		// if we are part way through a multi-line command
		else if (txtPrompt.getText().compareTo(ADDITONALINPUTPROMPT) == 0) {
			// add to history if something was entered
			String typedCmd = txtInput.getText();
			if (typedCmd.compareTo("") != 0) {
				if (cmdHistory.size() == 0) {
					addCommandToHistory(typedCmd);
				} else if ((typedCmd.compareTo("") != 0)
						&& (typedCmd.compareTo(cmdHistory.get(cmdHistory.size() - 1)) != 0)) {
					addCommandToHistory(typedCmd);
				}
				if (cmdHistory_index != cmdHistory.size() - 2) {
					runFromHistory = true;
				}
			}
			// append to whole command
			currentCmd += "\n" + typedCmd;
			// run the command
			txtInput.setEnabled(false);
			boolean needMore = commandserver.runsource(currentCmd, getName());
			txtInput.setEnabled(true);
			txtInput.requestFocus();
			// if not a complete Jython command
			if (needMore) {
				// change the prompt
				txtPrompt.setText(ADDITONALINPUTPROMPT);
				// clear the command-line
				txtInput.setText("");
				txtInput.setCaret(txtInput.getCaret());
			} else {
				// change the prompt
				txtPrompt.setText(NORMALPROMPT);
				currentCmd = "";
				// reset the cmdHistory pointer
				cmdHistory_index = cmdHistory.size();
				runFromHistory = false;
				// clear the command-line
				txtInput.setText("");
				txtInput.setCaret(txtInput.getCaret());
			}
		}
		// else a script has asked for input
		else if (txtPrompt.getText().compareTo(RAWINPUTPROMPT) == 0) {
			// get the next input from the user
			commandserver.setRawInput(txtInput.getText());
			// clear the command-line
			txtInput.setText("");
			txtInput.setCaret(txtInput.getCaret());
		}
	}


	/**
	 * From the IObservers interface. Used when a scan has been initiated from the terminal or jython editor. This
	 * terminal is registered as an observer, so if the graphics option has been selected for this object then the data
	 * will be shown on a graph.
	 *
	 * @param dataSource
	 *            Object
	 * @param dataPoint
	 *            Object
	 */
	@Override
	public void update(Object dataSource, Object dataPoint) {

		// if from scans then objects are in the format String, ScanDataPoint
		if (dataPoint instanceof IScanDataPoint && dataSource instanceof JythonServerFacade) {
			// always print the point to the terminal
			printScanPoint((IScanDataPoint) dataPoint);
		} else if (dataPoint instanceof String) {
			String message = (String) dataPoint;

			if (message.compareTo(Jython.RAW_INPUT_REQUESTED) == 0) {
				// change prompt and next input will go through a different
				// method call
				txtPrompt.setText(RAWINPUTPROMPT);
				txtInput.setBackground(Color.LIGHT_GRAY);
				// clear the command-line
				txtInput.setText("");
				txtInput.setCaret(txtInput.getCaret());
			} else if (message.compareTo(Jython.RAW_INPUT_RECEIVED) == 0) {
				// change prompt back to usual
				txtPrompt.setText(NORMALPROMPT);
				txtInput.setBackground(Color.WHITE);
			}
		}
	}


	void handleKeyEventInTextInputBox(KeyEvent e) {
		// when up or down arrows pressed, scroll through vector of commands
		// down arrow
		if (e.getKeyCode() == 40) {
			runFromHistory = false;
			if (cmdHistory_index < cmdHistory.size() - 1) {
				cmdHistory_index++;
				txtInput.setText(cmdHistory.get(cmdHistory_index));
			}
			// if at end of array then dont move index pointer but add a
			// blank
			// string
			else if (cmdHistory_index == cmdHistory.size() - 1) {
				cmdHistory_index++;
				txtInput.setText("");
			}
		}
		// up arrow
		else if (e.getKeyCode() == 38) {
			if (runFromHistory) {
				runFromHistory = false;
			} else if (cmdHistory_index > 0) {
				cmdHistory_index--;
			}
			if (cmdHistory.size() != 0) {
				txtInput.setText(cmdHistory.get(cmdHistory_index));
			}
		}

		// Ctrl-U clears the text box
		else if (e.getModifiersEx() == InputEvent.CTRL_DOWN_MASK && e.getKeyCode() == KeyEvent.VK_U) {
			txtInput.setText("");
		}
	}

	/*
	 * Takes the command entered by the user in the txtInput JTextField and gives it to the GDAJythonInterpreter
	 * runsource command. This returns a boolean which is true if the command is not a complete Jython command. This
	 * method then changes the command prompt to inform the user that more input is required. <P> This runs in its own
	 * thread (by calling the run() method) so that commands which tkae a long time to complete do not freeze the GUI.
	 * @param e ActionEvent
	 */
	void txtInput_actionPerformed() {
		// first intercept to see if there's any command which this panel is
		// interested in rather than passing to the interpreter.
		String[] parts = txtInput.getText().split(" ");
		if (parts.length < 1) {
			return;
		}

		// if want a watch
		if (parts[0].toLowerCase().compareTo("watch") == 0) {
			if (parts.length > 1) {
				for (int i = 1; i < parts.length; ++i) {
					watches.addWatch(parts[i]);
					addCommandToHistory(txtInput.getText());
				}
			} else {
				watches.setVisible(true);
			}
			txtInput.setText("");
		}
		// if want to look at the command history
		else if (parts[0].toLowerCase().compareTo("history") == 0) {
			// print out what was typed
			appendOutput(this.txtPrompt.getText() + parts[0] + "\n");

			// print out the last 100 commands
			int i = 0;
			i = cmdHistory.size() > 100 ? cmdHistory.size() - 100 : 0;

			for (; i < cmdHistory.size(); i++) {
				appendOutput(i + "\t" + cmdHistory.get(i) + "\n");
			}
			txtInput.setText("");
		}
		// record terminal output
		else if (parts[0].toLowerCase().compareTo("record") == 0) {
			if (parts[1].toLowerCase().compareTo("on") == 0) {
				printOutput = true;
				determineOutputFileName();
				// print out what was typed
				appendOutput(this.txtPrompt.getText() + txtInput.getText() + "\n");
				addCommandToHistory(txtInput.getText());

				logger.info("Recording terminal output to: " + this.printOutputFileName);
			}

			else if (parts[1].toLowerCase().compareTo("off") == 0) {
				try {
					// print out what was typed
					appendOutput(this.txtPrompt.getText() + txtInput.getText() + "\n");
					addCommandToHistory(txtInput.getText());
					printOutput = false;
					printOutputFile.close();
					printOutputFile = null;
					logger.info("Stopped recording terminal output");
				} catch (IOException e1) {
					printOutputFile = null;
				}
			}
			txtInput.setText("");
		}
		// repeat old commands
		else if (parts[0].startsWith("!")) {
			String stringToMatch = txtInput.getText().substring(1);

			// if stringToMatch is a number, then use that command
			if (stringIsAnInteger(stringToMatch)) {
				txtInput.setText(cmdHistory.get(Integer.parseInt(stringToMatch)));
				uk.ac.gda.util.ThreadManager.getThread(this, getClass().getName()).start();
				return;
			}
			// else search backwards through the history to find a match
			int i = cmdHistory.size() - 1;
			boolean foundOne = false;
			for (; i >= 0; i--) {
				String oldCommand = cmdHistory.get(i);

				if (oldCommand.length() >= stringToMatch.length()) {
					String oldCmd = cmdHistory.get(i).substring(0, stringToMatch.length());
					if (oldCmd.compareTo(stringToMatch) == 0) {
						txtInput.setText(cmdHistory.get(i));
						i = 0;
						foundOne = true;
					}
				}
			}
			if (foundOne) {
				uk.ac.gda.util.ThreadManager.getThread(this, getClass().getName()).start();
			} else {
				appendOutput("" + "\n");
				txtInput.setText("");
			}

		}
		// everything else, pass to the Command Server in a separate thread to
		// stop the GUI freezing.
		else {
			uk.ac.gda.util.ThreadManager.getThread(this, getClass().getName()).start();
		}
	}

	private boolean stringIsAnInteger(String stringToCheck) {
		// a bit of a hack, but works!
		try {
			Integer.parseInt(stringToCheck);
			return true; // Did not throw, must be a number
		} catch (NumberFormatException err) {
			return false; // Threw, So is not a number
		}
	}

	private void addCommandToHistory(String newCommand) {
		// add command to the history
		cmdHistory.add(newCommand);

		// also save command to a file
		try {
			if (commandFileName != null) {
				BufferedWriter out = new BufferedWriter(new FileWriter(commandFileName, true));
				out.write(newCommand + "\n");
				out.close();
			}
		} catch (IOException e) {
		}
	}

	private void determineOutputFileName() {
		// filter to use when looking in the user script directory
		FilenameFilter filter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.startsWith("terminal_output") && name.endsWith(".txt");
			}
		};

		// get the terminal output directory from java properties. If no property defined, use the user script directory
		String terminalOutputDir = null;
		if ( LocalProperties.get("gda.jython.terminalOutputDir") != null ){
			terminalOutputDir = PropertyUtils.getExistingDirFromLocalProperties("gda.jython.terminalOutputDir");
			}
		else{
			terminalOutputDir = userScriptDir;
			}

		if (!(terminalOutputDir.endsWith("\\") || terminalOutputDir.endsWith("/"))) {
			terminalOutputDir += System.getProperty("file.separator");
		}

		// look through this directory for existing files
		File userScriptsDirectory = new File(terminalOutputDir);
		String[] files = userScriptsDirectory.list(filter);
		int maxValue = 0;

		// determine the highest existing file of the format:
		// terminal_output_XX.txt
		for (String fileName : files) {
			String whatsLeft = fileName.substring(15);
			whatsLeft = whatsLeft.substring(0, whatsLeft.lastIndexOf("."));

			if (whatsLeft.length() > 0) {
				int value = Integer.parseInt(whatsLeft.substring(1));

				if (value > maxValue) {
					maxValue = value;
				}
			}
		}

		// determine the file name to use
		String filename;
		if (maxValue == 0) {
			filename = terminalOutputDir + "terminal_output_1.txt";
		} else {
			filename = terminalOutputDir + "terminal_output_" + (maxValue + 1) + ".txt";
		}

		// open the file
		try {
			File out = new File(filename);
			printOutputFile = new FileWriter(out);
			printOutputFileName = filename;
		} catch (IOException e) {
			printOutputFileName = "";
			logger.warn("JythonTerminal could not create the output file: " + filename);
		}
	}

	/*
	 * Stops cleanly current scan and script, if running. @param e ActionEvent
	 */
	void btnHaltScriptScan_actionPerformed() {
		if (this.commandserver.getScanStatus() == Jython.RUNNING || this.commandserver.getScanStatus() == Jython.PAUSED) {
			this.commandserver.requestFinishEarly();
		} else {
			InterfaceProvider.getCommandAborter().abortCommands();
		}
	}

	/*
	 * Prints out the latest data to the terminal. @param dataPoint ScanDataPoint
	 */
	private void printScanPoint(IScanDataPoint dataPoint) {
		// either way, always print out data from all scans to terminal
		// check if we have seen the source before.
		if (!terminalLineSources.contains(dataPoint.getUniqueName())) {
			appendOutput(dataPoint.getHeaderString() + "\n");
			terminalLineSources.add(dataPoint.getUniqueName());
		}
		appendOutput(dataPoint.toFormattedString() + "\n");
	}

	/**
	 * Used by write and other methods to append output to the JTextArea terminal output.
	 *
	 * @param newOutput
	 */
	private void appendOutput(String newOutput) {
		SwingUtilities.invokeLater(new SimpleOutputUpdater(newOutput));
	}

	/**
	 * Used by appendOutpupt
	 */
	private final class SimpleOutputUpdater implements Runnable {

		String newOutput = "";

		/**
		 * Constructor
		 *
		 * @param newOutput
		 */
		SimpleOutputUpdater(String newOutput) {
			this.newOutput = newOutput;
		}

		/**
		 * Overwrites the text in the JTextArea with the supplied string starting at the location defined by
		 * caretPosition.
		 *
		 * @param theString
		 */
		private void addToOutput(String theString) {

			// find location of end of last line
			int finalCharacter = txtOutput.getText().length();

			// if caret at the end of the last line
			if (caretPosition >= finalCharacter) {
				txtOutput.append(theString);
			}
			// else if the output would only overwrite existing text
			else if (theString.length() + caretPosition < finalCharacter) {
				txtOutput.replaceRange(theString, caretPosition, caretPosition + theString.length());
			}
			// else a mixture of overwriting and appending
			else {
				int firstPartLength = finalCharacter - caretPosition;
				String firstPart = theString.substring(0, firstPartLength);
				String lastPart = theString.substring(firstPartLength);
				txtOutput.replaceRange(firstPart, caretPosition, caretPosition + firstPartLength);
				txtOutput.append(lastPart);
			}
		}

		/*
		 * Sets viewable part of the scrollable component to its bottom.
		 */
		private void scrollToBottom() {

			try {
				txtOutput.setCaretPosition(txtOutput.getText().length());
			} catch (Exception e) {
				// ignore these - these occur when too much information
				// being written to, so ignore the exception as this
				// method
				// will owrk again once the input has quietned down.
			}

		}

		@Override
		public void run() {

			newOutput = newOutput.replaceAll("\\r\\n", "\n");

			// if a repeat of the command prompt
			if (newOutput.startsWith(NORMALPROMPT)) {
				// make sure that the print out starts on a new line
				if (!txtOutput.getText().endsWith("\n")) {
					txtOutput.append("\r\n");
					// update where new print out should start
					caretPosition = txtOutput.getText().length();
				}
				// print
				addToOutput(newOutput);
				// update where new print out should start
				caretPosition = txtOutput.getText().length();
				scrollToBottom();
			}
			// if just regular output simply append
			else if (!newOutput.contains("\r") && !newOutput.startsWith(RAWINPUTPROMPT)) {
				// If text field has grown too long, trim off 10% from the
				// beginning.
				// Note: This is only performed for this "regular output" case
				// because
				// changing the text field length for the other cases messes up
				// the
				// caretPosition value.
				int currentLength = txtOutput.getText().length();
				if (currentLength > LocalProperties.getInt("gda.jython.jythonTerminal.textFieldCharactersCap", 100000)) {
					try {
						txtOutput.setText(txtOutput.getText(currentLength / 10, currentLength - currentLength / 10));
					} catch (BadLocationException e) {
						logger.warn("Could not truncate text field");
					}
				}
				// print
				addToOutput(newOutput);
				// update where new print out should start
				caretPosition = txtOutput.getText().length();
				scrollToBottom();
				// if output starts with '-->' when user requested input
				// mid-script
			} else if (newOutput.startsWith(RAWINPUTPROMPT)) {
				// add this output to the end of the previous line
				caretPosition = txtOutput.getText().length() - 1;
				// print
				addToOutput(newOutput);
				// update where new print out should start
				caretPosition = txtOutput.getText().length();
				scrollToBottom();
			}
			// Otherwise must contain a \r or start with '-->'.
			// This should be handled properly so
			// the caret is returned to the start of the last line rather than
			// \r
			// being treated as a new line marker.
			else {
				try {

					// find out where the \r is
					int locOfCR = newOutput.indexOf("\r");

					// remove any final \n
					if (newOutput.endsWith("\n")) {
						newOutput = newOutput.substring(0, newOutput.length() - 1);
					}

					// if \r at start of string, move caret to start of previous
					// line, unless that line started with '>>>'
					if (locOfCR == 0) {
						int startRange = txtOutput.getLineStartOffset(txtOutput.getLineCount() - 1);

						caretPosition = startRange;
					}
					// else add first part of text and then move the caret of
					// that line
					else {
						String substring = newOutput.substring(0, locOfCR);
						addToOutput(substring);

						int startRange = txtOutput.getLineStartOffset(txtOutput.getLineCount() - 1);
						caretPosition = startRange;
					}

					// if anything after the /r in the text, append that
					if (newOutput.length() > locOfCR + 1) {
						String stringToAppend = newOutput.substring(locOfCR + 1);
						// print
						addToOutput(stringToAppend);
						// update where new print out should start
						caretPosition += stringToAppend.length();
					}

				}
				// any error, simply output everything and treat \r as a \n
				catch (Exception e) {
					txtOutput.setText(txtOutput.getText() + newOutput);
					caretPosition = txtOutput.getText().length();
					scrollToBottom();
				}
			}
			// if output being saved to a file (record command)
			if (printOutputFile != null) {
				try {
					printOutputFile.append(newOutput);
					printOutputFile.flush();
				} catch (IOException e) {
					printOutputFile = null;
				}
			}
		}
	}

	private void jbInit() {

		titledBorder1 = new TitledBorder(BorderFactory.createEtchedBorder(Color.white, new Color(165, 163, 151)),
				"Terminal");
		titledBorder2 = new TitledBorder(BorderFactory.createEtchedBorder(Color.white, new Color(165, 163, 151)),
				"Graphics");

		this.setLayout(borderLayout1);
		txtPrompt.setBackground(Color.white);
		txtPrompt.setFont(new java.awt.Font("Monospaced", 0, 12));
		txtPrompt.setBorder(null);
		txtPrompt.setMaximumSize(new Dimension(35, 29));
		txtPrompt.setMinimumSize(new Dimension(35, 29));
		txtPrompt.setOpaque(true);
		txtPrompt.setPreferredSize(new Dimension(35, 29));
		txtPrompt.setCaretColor(Color.black);
		txtPrompt.setEditable(false);
		txtPrompt.setMargin(new Insets(0, 0, 0, 0));
		txtPrompt.setText(">>>");
		txtPrompt.setFocusable(false);
		txtInput.setFont(new java.awt.Font("Monospaced", 0, 12));
		txtInput.setBorder(null);
		txtInput.setMaximumSize(new Dimension(561, 29));
		txtInput.setMinimumSize(new Dimension(0, 29));
		txtInput.setPreferredSize(new Dimension(500, 29));
		txtInput.setMargin(new Insets(0, 0, 0, 0));
		txtInput.setText("");
		txtInput.setFocusable(true);
		txtInput.addActionListener(new JythonTerminal_txtInput_actionAdapter(this));
		txtInput.addKeyListener(new JythonTerminal_txtInput_keyAdapter(this));
		this.setEnabled(true);
		this.setDebugGraphicsOptions(0);
		jScrollPane1.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		jScrollPane1.setAutoscrolls(true);
		jScrollPane1.setBorder(null);
		jScrollPane1.setMaximumSize(new Dimension(2147483647, 2147483647));
		jScrollPane1.setMinimumSize(new Dimension(592, 23));
		jScrollPane1.setPreferredSize(new Dimension(592, 22));
		txtOutput.setFont(new java.awt.Font("Monospaced", 0, 12));
		txtOutput.setBorder(null);
		txtOutput.setMaximumSize(new Dimension(2147483647, 2147483647));
		txtOutput.setEditable(false);
		txtOutput.setTabSize(3);

		pnlOutput.setLayout(borderLayout2);
		pnlInput.setLayout(borderLayout3);
		pnlTerminal.setLayout(borderLayout5);
		pnlInput.setBorder(null);
		pnlTerminal.setBorder(titledBorder1);
		pnlTerminal.setMinimumSize(new Dimension(500, 79));
		pnlTerminal.setPreferredSize(new Dimension(500, 78));
		pnlLeft.setLayout(borderLayout6);


		pnlInput.add(txtPrompt, BorderLayout.WEST);
		pnlInput.add(txtInput, BorderLayout.CENTER);
		pnlLeft.add(watches, BorderLayout.NORTH);
		pnlLeft.add(pnlTerminal, BorderLayout.CENTER);

		this.add(pnlLeft, BorderLayout.CENTER);
		pnlTerminal.add(pnlOutput, BorderLayout.CENTER);
		pnlOutput.add(jScrollPane1, BorderLayout.CENTER);
		pnlTerminal.add(pnlInput, BorderLayout.SOUTH);
		jScrollPane1.getViewport().add(txtOutput, null);

		// make control-B stop the current scans and scripts
		this.getActionMap().put("ctrl_b", new AbstractAction("ctrl_b") {
			@Override
			public void actionPerformed(ActionEvent evt) {
				btnHaltScriptScan_actionPerformed();
			}
		});
		this.getInputMap().put(KeyStroke.getKeyStroke("control B"), "ctrl_b");
		txtInput.getActionMap().put("ctrl_b", new AbstractAction("ctrl_b") {
			@Override
			public void actionPerformed(ActionEvent evt) {
				btnHaltScriptScan_actionPerformed();
			}
		});
		txtInput.getInputMap().put(KeyStroke.getKeyStroke("control B"), "ctrl_b");
		txtOutput.getActionMap().put("ctrl_b", new AbstractAction("ctrl_b") {
			@Override
			public void actionPerformed(ActionEvent evt) {
				btnHaltScriptScan_actionPerformed();
			}
		});
		txtOutput.getInputMap().put(KeyStroke.getKeyStroke("control B"), "ctrl_b");
	}
}

/*
 * inner classes automatically created by JBuilder to capture user actions
 */

class JythonTerminal_txtInput_actionAdapter implements java.awt.event.ActionListener {
	JythonTerminal adaptee;

	JythonTerminal_txtInput_actionAdapter(JythonTerminal adaptee) {
		this.adaptee = adaptee;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		adaptee.txtInput_actionPerformed();
	}
}

class JythonTerminal_txtInput_keyAdapter implements java.awt.event.KeyListener {
	JythonTerminal adaptee;

	JythonTerminal_txtInput_keyAdapter(JythonTerminal adaptee) {
		this.adaptee = adaptee;
	}

	@Override
	public void keyPressed(KeyEvent e) {
		adaptee.handleKeyEventInTextInputBox(e);
	}

	@Override
	public void keyReleased(KeyEvent e) {
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}
}

class JythonTerminal_btnHaltScriptScan_actionAdapter implements ActionListener {
	private JythonTerminal adaptee;

	JythonTerminal_btnHaltScriptScan_actionAdapter(JythonTerminal adaptee) {
		this.adaptee = adaptee;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		adaptee.btnHaltScriptScan_actionPerformed();
	}
}
