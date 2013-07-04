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

package gda.oe.positioners;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

/**
 * Used by stand alone GUI program for writing objects to a binary file. Currently for 3 double values only and for
 * manual editing of filePath, fileName and said values.
 * 
 * @see PositionalValues the Object to be stored and retrieved
 * @see FileWriterGui the main program
 */
public class FileWriterGuiPanel extends JPanel {
	private double lowerLimit = Double.NaN;

	private double upperLimit = Double.NaN;

	private double homeOffset = Double.NaN;

	private double positionOffset = Double.NaN;

	private JButton buttonCancel;

	private JButton buttonClear;

	private JButton buttonGet;

	private JButton buttonSave;

	private JLabel labelFilePath;

	private JLabel labelFileName;

	private JLabel labelNewLowerLimit;

	private JLabel labelNewUpperLimit;

	private JLabel labelNewHomeOffset;

	private JLabel labelNewPositionOffset;

	private JLabel labelStoredLowerLimit;

	private JLabel labelStoredUpperLimit;

	private JLabel labelStoredHomeOffset;

	private JLabel labelStoredPositionOffset;

	private JPanel panelNewValues;

	private JPanel panelStoredValues;

	private JPanel panelFileParameters;

	private JTextField fieldFilePath;

	private JTextField fieldFileName;

	private JTextField fieldNewLowerLimit;

	private JTextField fieldNewUpperLimit;

	private JTextField fieldNewHomeOffset;

	private JTextField fieldNewPositionOffset;

	private JTextField fieldStoredLowerLimit;

	private JTextField fieldStoredUpperLimit;

	private JTextField fieldStoredHomeOffset;

	private JTextField fieldStoredPositionOffset;

	private PositionalValues positionalValues;

	private String fileName = null;

	private String filePath = null;

	private String separator = System.getProperty("file.separator");

	/**
	 * Constructor.
	 */
	public FileWriterGuiPanel() {
		positionalValues = new PositionalValues();

		buttonCancel = new JButton("Cancel");
		buttonClear = new JButton("Clear");
		buttonGet = new JButton("Get");
		buttonSave = new JButton("Save");

		labelFileName = new JLabel("File Name:");
		labelFilePath = new JLabel("File Path:");
		labelNewLowerLimit = new JLabel("Lower Limit");
		labelNewUpperLimit = new JLabel("Upper Limit");
		labelNewHomeOffset = new JLabel("Home Offset");
		labelNewPositionOffset = new JLabel("Position Offset");
		labelStoredLowerLimit = new JLabel("Lower Limit");
		labelStoredUpperLimit = new JLabel("Upper Limit");
		labelStoredHomeOffset = new JLabel("Home Offset");
		labelStoredPositionOffset = new JLabel("Position Offset");

		fieldFileName = new JTextField(30);
		fieldFilePath = new JTextField(30);
		fieldNewLowerLimit = new JTextField(20);
		fieldNewUpperLimit = new JTextField(20);
		fieldNewHomeOffset = new JTextField(20);
		fieldNewPositionOffset = new JTextField(20);
		fieldStoredLowerLimit = new JTextField(20);
		fieldStoredLowerLimit.setEditable(false);
		fieldStoredUpperLimit = new JTextField(20);
		fieldStoredUpperLimit.setEditable(false);
		fieldStoredHomeOffset = new JTextField(20);
		fieldStoredHomeOffset.setEditable(false);
		fieldStoredPositionOffset = new JTextField(20);
		fieldStoredPositionOffset.setEditable(false);

		buttonCancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				cancel();
			}
		});

		buttonClear.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				clear();
			}
		});

		buttonGet.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				get();
			}
		});

		buttonSave.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				save();
			}
		});

		setLayout(new BorderLayout());
		add(createFileParametersPanel(), BorderLayout.NORTH);
		add(createNewValuesPanel(), BorderLayout.CENTER);
		add(createStoredValuesPanel(), BorderLayout.SOUTH);
		// setSize(500, 300);
		// setVisible(true);
	}

	/**
	 * This method clears the text boxes in the panel for new values.
	 */
	private void cancel() {
		fieldNewLowerLimit.setText("");
		fieldNewUpperLimit.setText("");
		fieldNewHomeOffset.setText("");
		fieldNewPositionOffset.setText("");
	}

	/**
	 * This method clears the text boxes in the panel for stored values.
	 */
	private void clear() {
		fieldStoredLowerLimit.setText("");
		fieldStoredUpperLimit.setText("");
		fieldStoredHomeOffset.setText("");
		fieldStoredPositionOffset.setText("");
	}

	/**
	 * Creates and sets layout for the file parameters panel
	 * 
	 * @return panelFileParameters
	 */
	private JPanel createFileParametersPanel() {
		// Set layout of file parameters panel
		panelFileParameters = new JPanel();
		panelFileParameters.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		int oldfill = c.fill;

		c.gridx = GridBagConstraints.RELATIVE;
		c.gridy = GridBagConstraints.RELATIVE;

		c.gridwidth = 1;
		panelFileParameters.add(labelFilePath, c);

		c.gridwidth = GridBagConstraints.REMAINDER;
		c.fill = GridBagConstraints.HORIZONTAL;
		panelFileParameters.add(fieldFilePath, c);
		c.fill = oldfill;

		c.gridwidth = 1;
		panelFileParameters.add(labelFileName, c);

		c.gridwidth = GridBagConstraints.REMAINDER;
		c.fill = GridBagConstraints.HORIZONTAL;
		panelFileParameters.add(fieldFileName, c);
		c.fill = oldfill;

		return panelFileParameters;
	}

	/**
	 * Creates and sets the layout for the panel where new values are entered to be saved to a file.
	 * 
	 * @return panelNewValues
	 */
	private JPanel createNewValuesPanel() {
		panelNewValues = new JPanel();
		panelNewValues.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
				"Set File Values", TitledBorder.LEFT, TitledBorder.TOP, null, Color.black));
		panelNewValues.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		int oldfill = c.fill;

		c.gridx = GridBagConstraints.RELATIVE;
		c.gridy = GridBagConstraints.RELATIVE;

		c.gridwidth = 1;
		panelNewValues.add(labelNewLowerLimit, c);

		c.gridwidth = GridBagConstraints.REMAINDER;
		c.fill = GridBagConstraints.HORIZONTAL;
		panelNewValues.add(fieldNewLowerLimit, c);
		c.fill = oldfill;

		c.gridwidth = 1;
		panelNewValues.add(labelNewUpperLimit, c);

		c.gridwidth = GridBagConstraints.REMAINDER;
		c.fill = GridBagConstraints.HORIZONTAL;
		panelNewValues.add(fieldNewUpperLimit, c);
		c.fill = oldfill;

		c.gridwidth = 1;
		panelNewValues.add(labelNewHomeOffset, c);

		c.gridwidth = GridBagConstraints.REMAINDER;
		c.fill = GridBagConstraints.HORIZONTAL;
		panelNewValues.add(fieldNewHomeOffset, c);
		c.fill = oldfill;

		c.gridwidth = 1;
		panelNewValues.add(labelNewPositionOffset, c);

		c.gridwidth = GridBagConstraints.REMAINDER;
		c.fill = GridBagConstraints.HORIZONTAL;
		panelNewValues.add(fieldNewPositionOffset, c);
		c.fill = oldfill;

		c.gridwidth = 1;
		panelNewValues.add(buttonSave, c);
		c.gridwidth = 1;
		panelNewValues.add(buttonCancel, c);

		return panelNewValues;
	}

	/**
	 * Creates and sets out the panel for the stored values to be displayed.
	 * 
	 * @return panelStoredValues
	 */
	private JPanel createStoredValuesPanel() {
		panelStoredValues = new JPanel();
		panelStoredValues.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
				"Get Stored Values", TitledBorder.LEFT, TitledBorder.TOP, null, Color.black));
		panelStoredValues.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		int oldfill = c.fill;

		c.gridx = GridBagConstraints.RELATIVE;
		c.gridy = GridBagConstraints.RELATIVE;

		c.gridwidth = 1;
		panelStoredValues.add(labelStoredLowerLimit, c);

		c.gridwidth = GridBagConstraints.REMAINDER;
		c.fill = GridBagConstraints.HORIZONTAL;
		panelStoredValues.add(fieldStoredLowerLimit, c);
		c.fill = oldfill;

		c.gridwidth = 1;
		panelStoredValues.add(labelStoredUpperLimit, c);

		c.gridwidth = GridBagConstraints.REMAINDER;
		c.fill = GridBagConstraints.HORIZONTAL;
		panelStoredValues.add(fieldStoredUpperLimit, c);
		c.fill = oldfill;

		c.gridwidth = 1;
		panelStoredValues.add(labelStoredHomeOffset, c);

		c.gridwidth = GridBagConstraints.REMAINDER;
		c.fill = GridBagConstraints.HORIZONTAL;
		panelStoredValues.add(fieldStoredHomeOffset, c);
		c.fill = oldfill;

		c.gridwidth = 1;
		panelStoredValues.add(labelStoredPositionOffset, c);

		c.gridwidth = GridBagConstraints.REMAINDER;
		c.fill = GridBagConstraints.HORIZONTAL;
		panelStoredValues.add(fieldStoredPositionOffset, c);
		c.fill = oldfill;

		c.gridwidth = 1;
		panelStoredValues.add(buttonGet, c);

		c.gridwidth = 1;
		panelStoredValues.add(buttonClear, c);

		return panelStoredValues;
	}

	/**
	 * Gets and checks values exist in the file and directory path fields. Opens a binary file for reading and reads the
	 * values as an object before casting them into a PositionalValues Object. These values are then displayed on the
	 * GUI
	 */
	protected void get() {
		if (validateFileFields()) {
			String limitStore = filePath + separator + fileName;

			ObjectInputStream in = null;
			try {
				in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(limitStore)));

				positionalValues = (PositionalValues) in.readObject();
				in.close();
			} catch (IOException ioe) {
				JOptionPane.showMessageDialog(panelFileParameters,
						"An exception occurred while trying to read the file. " + ioe.getMessage());
			} catch (ClassNotFoundException e) {
				JOptionPane.showMessageDialog(panelFileParameters, "Object not found within the file. "
						+ e.getMessage());
			}

			// display the values from the object.
			// FIXME If one of the values has not been set - will throw an
			// exception - does this need changing??
			try {
				if (positionalValues != null) {
					fieldStoredLowerLimit.setText(Double.toString(positionalValues.getLowerLimit()));
					fieldStoredUpperLimit.setText(Double.toString(positionalValues.getUpperLimit()));
					fieldStoredHomeOffset.setText(Double.toString(positionalValues.getHomeOffset()));
					fieldStoredPositionOffset.setText(Double.toString(positionalValues.getPositionOffset()));
				} else {
					JOptionPane.showMessageDialog(panelFileParameters, "Values Object was not found. ");
				}
			} catch (Exception e) {
				JOptionPane.showMessageDialog(panelFileParameters,
						"An exception occurred while setting the values to the Object. " + e.getMessage());
			}
		}
	}

	/**
	 * Gets and checks values exist in the file and directory path fields. Gets and checks values exist in the new
	 * values fields Opens a binary file for writing, sets the new values into a PositionalValues Object and saves the
	 * object to the file.
	 */
	private void save() {
		if (validateFileFields()) {
			if (validateValueFields()) {
				String limitStore = filePath + separator + fileName;

				ObjectOutputStream out = null;
				if (positionalValues != null) {
					positionalValues.setLowerLimit(lowerLimit);
					positionalValues.setUpperLimit(upperLimit);
					positionalValues.setHomeOffset(homeOffset);
					positionalValues.setPositionOffset(positionOffset);
					try {
						out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(limitStore)));

						out.writeObject(positionalValues);
						out.flush();
						out.close();
					} catch (IOException ioe) {
						JOptionPane.showMessageDialog(panelNewValues, "Cannot write to file. " + ioe.getMessage());
					}
				} else {
					JOptionPane.showMessageDialog(panelNewValues, "The Values Object was not found.");
				}
			}
		}
	}

	/**
	 * Checks to whether text fields have content and prompts for action if not.
	 * 
	 * @return valid if content is present in both fields
	 */
	private boolean validateFileFields() {
		boolean valid = false;
		fileName = fieldFileName.getText();
		filePath = fieldFilePath.getText();
		if (fileName == null || fileName == "") {
			JOptionPane.showMessageDialog(panelNewValues, "Please enter a file name.");
		} else if (filePath == null || filePath == "") {
			JOptionPane.showMessageDialog(panelNewValues, "Please enter a file path.");
		} else {
			valid = true;
		}
		return valid;
	}

	/**
	 * Checks if there is content in value text fields. If no content found - value set to Double.NaN If content found -
	 * parse to a double and prompt for action if not parsable
	 * 
	 * @return valid true if values have been set successfully.
	 */
	private boolean validateValueFields() {
		boolean valid = false;
		String valueString = "";
		try {
			if ((valueString = fieldNewLowerLimit.getText()) != "") {
				lowerLimit = Double.parseDouble(valueString);
			} else {
				lowerLimit = Double.NaN;
			}
			if ((valueString = fieldNewUpperLimit.getText()) != "") {
				upperLimit = Double.parseDouble(valueString);
			} else {
				upperLimit = Double.NaN;
			}
			if ((valueString = fieldNewHomeOffset.getText()) != "") {
				homeOffset = Double.parseDouble(valueString);
			} else {
				homeOffset = Double.NaN;
			}
			if ((valueString = fieldNewPositionOffset.getText()) != "") {
				positionOffset = Double.parseDouble(valueString);
			} else {
				positionOffset = Double.NaN;
			}
			valid = true;
		} catch (NumberFormatException nfe) {
			valid = false;
			JOptionPane.showMessageDialog(panelFileParameters, valueString + " is not a number " + nfe.getMessage());

		}
		return valid;
	}
}
