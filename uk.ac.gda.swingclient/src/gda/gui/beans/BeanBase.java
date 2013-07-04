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

package gda.gui.beans;

import gda.observable.IObserver;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * Base class for the GUI beans in this package
 */
public abstract class BeanBase extends JPanel implements IObserver, Runnable {

	private static final Logger logger = LoggerFactory.getLogger(BeanBase.class);

	// lock to prevent a backlog of threads trying to update this Bean.
	protected volatile boolean manualUpdate = false;

	// these are the values to update in the refreshValues method and to display in the updateDisplay method
	protected volatile String unitsString = "";
	protected volatile String valueString = "0.0";
	protected volatile String tooltipString = "";

	private JLabel lblUnits;
	private JLabel lblLabel;
	private JFormattedTextField txtValue;
	private String label = "Name";
	private String displayFormat = "%5.2g";
	private boolean zeroSmallNumbers = false;

	/**
	 * Collect the information to display, but do not display it.
	 */
	protected abstract void refreshValues();

	/**
	 * The action performed when return is pressed when the cursor is in the text box.
	 */
	protected abstract void txtValueActionPerformed();

	/**
	 * @return true if the obsvered object's value is changing (e.g. a motor is moving)
	 */
	protected abstract boolean theObservableIsChanging();

	/**
	 * The implementing class should build the GUI and connect to the object it is representing and begin to display the
	 * information.
	 * <p>
	 * The GUI is built by calling the initGUI() method.
	 * <p>
	 * This must be called after calling all the relevent accessor functions to set up the bean.
	 */
	public abstract void startDisplay();

	/**
	 * This should be called at the end of every implementation of the startDisplay() method.
	 */
	protected void configure() {
		uk.ac.gda.util.ThreadManager.getThread(this, label).start();
	}

	/**
	 * This should hold the logic about whether to update or now based on the gievn information
	 * 
	 * @see gda.observable.IObserver#update(java.lang.Object, java.lang.Object)
	 */
	@Override
	public void update(Object theObserved, Object changeCode) {
		manualUpdate = true;
	}

	@Override
	public void run() {
		// infinite loop
		while (true) {
			boolean updateDueToMove = false;
			try {
				// if move in progress or a manual update sent, call updateDisplay in swing thread
				updateDueToMove = theObservableIsChanging();
				while (theObservableIsChanging() || manualUpdate) {
					updateDisplay();
					Thread.sleep(1000);//100ms delay caused the gui to freeze on i02 when displaying the microglide
					manualUpdate = false;
				}
				// if the move just finished, then update one last time to pick up final value
				if (updateDueToMove) {
					updateDisplay();
				}
			} catch (Exception e) {
				final String displayedLabel = (label != null) ? StringUtils.quote(label) : "unlabelled";
				logger.error(String.format("Could not update %s bean", displayedLabel), e);
			} finally {
				try {
					Thread.sleep(2500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @param label
	 *            the label to set
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * @return the displayFormat
	 */
	public String getDisplayFormat() {
		return displayFormat;
	}

	/**
	 * @param displayFormat
	 *            the displayFormat to set
	 */
	public void setDisplayFormat(String displayFormat) {
		this.displayFormat = displayFormat;
		//the following line assumes something of the format "%5.0f". Only one digit allowed for the first number
		int minWidth = Integer.parseInt(displayFormat.substring(1, 2))+1;
		getTxtValue().setMinimumSize(new Dimension(minWidth * 10, 19));
		getTxtValue().setPreferredSize(new Dimension(minWidth * 10, 19));
	}

	/**
	 * @return the zeroSmallNumbers
	 */
	public boolean isZeroSmallNumbers() {
		return zeroSmallNumbers;
	}

	/**
	 * @param zeroSmallNumbers
	 *            the zeroSmallNumbers to set
	 */
	public void setZeroSmallNumbers(boolean zeroSmallNumbers) {
		this.zeroSmallNumbers = zeroSmallNumbers;
	}

	/**
	 * @return the editable
	 */
	public boolean isEditable() {
		return getTxtValue().isEditable();
	}

	/**
	 * @param editable
	 *            the editable to set
	 */
	public void setEditable(boolean editable) {
		getTxtValue().setEditable(editable);
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		getTxtValue().setEnabled(enabled);
	}

	protected void initGUI() {
		this.setLayout(new FlowLayout());
		this.add(getLblLabel());
		this.add(getTxtValue());
		this.add(getLblUnits());
		this.setMinimumSize(new java.awt.Dimension(150, 19));
		this.setMinimumSize(new java.awt.Dimension(200, 19));
	}

	protected JLabel getLblLabel() {
		if (lblLabel == null) {
			lblLabel = new JLabel();
			lblLabel.setText("Name");
			lblLabel.setHorizontalAlignment(SwingConstants.RIGHT);
			lblLabel.setHorizontalTextPosition(SwingConstants.RIGHT);
		}
		return lblLabel;
	}

	protected JTextField getTxtValue() {
		if (txtValue == null) {
			txtValue = new JFormattedTextField();
			// When the text box loses focus, keep the edited value. This will be reverted eventually by the
			// main update loop
			//TODO this will cause the text to revert from 1700 to 1,700 . We need to set the formatter to simple or do not use this box at all.
			//Currently we handle the comma in the txtValueActionPerformed
			txtValue.setFocusLostBehavior(JFormattedTextField.PERSIST);
			
			txtValue.setValue(new Double(100));
			txtValue.setText("position");
			txtValue.setHorizontalAlignment(SwingConstants.RIGHT);
			txtValue.setPreferredSize(new java.awt.Dimension(60, 19));
			txtValue.setMinimumSize(new java.awt.Dimension(60, 19));
			txtValue.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent evt) {
					
					// Remove focus from the text field, so it updates as the motor moves
					requestFocus();
					
					// do this in a new thread that's not the event thread.
					uk.ac.gda.util.ThreadManager.getThread(new Runnable() {
						@Override
						public void run() {
							manualUpdate = true;
							txtValueActionPerformed();
						}
					}).start();
				}
			});
		}
		return txtValue;
	}

	protected JLabel getLblUnits() {
		if (lblUnits == null) {
			lblUnits = new JLabel();
			lblUnits.setText("units");
			lblUnits.setHorizontalTextPosition(SwingConstants.LEFT);
			lblUnits.setHorizontalAlignment(SwingConstants.LEFT);
		}
		return lblUnits;
	}
	
	/**
	 * Refreshes the current values, and (on the AWT event dispatching thread) updates the GUI components.
	 * 
	 * <p>The GUI components are not updated if the text box has focus, to avoid overwriting a manually-entered value.
	 */
	protected void updateDisplay() {
		refreshValues();
		if (!getTxtValue().hasFocus()) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					getLblLabel().setText(label);
					getLblUnits().setText(unitsString);
					getTxtValue().setText(valueString);
					setToolTipText(tooltipString);
				}
			});
		}
	}
	
	//if text shows 1,700 then return 1700
	protected String getTxtNoCommas(){
		return getTxtValue().getText().trim().replace(",", "");		
	}

}
