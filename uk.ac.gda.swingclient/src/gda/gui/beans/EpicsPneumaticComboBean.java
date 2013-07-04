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

import gda.device.DeviceException;
import gda.device.EnumPositioner;
import gda.factory.Findable;
import gda.factory.Finder;
import gda.observable.IObserver;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Specific versions of the EnumPositionerComboBean which deals with EpicsPneumatic objects which have separate lists of
 * reported positions and command positions.
 */
public class EpicsPneumaticComboBean extends javax.swing.JPanel implements IObserver, Runnable {
	
	private static final Logger logger = LoggerFactory.getLogger(EpicsPneumaticComboBean.class);

	protected JComboBox cmbChoices;
	protected String label;
	protected String enumPositionerName = "";
	protected String[] commandPositions = null;
	protected EnumPositioner enumPositioner = null;
	// lock to prevent a backlog of threads trying to update this Bean.
	protected volatile boolean updateLock = false;
	private JLabel lblStatus = new JLabel("");
	
	/**
	 * Constructor.
	 */
	public EpicsPneumaticComboBean() {
		super();
	}
	
	private void initGUI() {
		this.setMinimumSize(new java.awt.Dimension(200, 19));
		this.setBorder(BorderFactory.createTitledBorder(label));
		this.setLayout(new FlowLayout());
		this.add(getCmbChoices());
		this.add(lblStatus);
	}

	
	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		if( cmbChoices != null)
			cmbChoices.setEnabled(enabled);
	}

	/**
	 * Call this to connect to the object and build the UI.
	 */
	public void configure() {
		
		if (enumPositionerName != null) {
			try {
				Findable obj = Finder.getInstance().find(enumPositionerName);
				if (obj != null && obj instanceof EnumPositioner) {
					this.enumPositioner = (EnumPositioner) obj;
					enumPositioner.addIObserver(this);
					// what to send
					commandPositions = enumPositioner.getPositions();
					initGUI();
					updateDisplay();
				}
			} catch (DeviceException e) {
				logger.error("deviceexception while retrieving positions from positioner: " + e.getMessage());
			}
		} else {
			logger.error("EnumPositionerComboBean cannot connect to object as information missing!");
		}
	}

	/**
	 * @see gda.observable.IObserver#update(java.lang.Object, java.lang.Object)
	 */
	@Override
	public void update(Object theObserved, Object changeCode) {
		updateDisplay();
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
	 * @return the enumPositionerName
	 */
	public String getEnumPositionerName() {
		return enumPositionerName;
	}

	/**
	 * @param enumPositionerName
	 *            the enumPositionerName to set
	 */
	public void setEnumPositionerName(String enumPositionerName) {
		this.enumPositionerName = enumPositionerName;
	}

	/**
	 * Must be called after the positions array has been populated.
	 * 
	 * @return JComboBox
	 */
	private JComboBox getCmbChoices() {
		if (cmbChoices == null && commandPositions != null) {
			ComboBoxModel cmbChoicesModel = new DefaultComboBoxModel(commandPositions);
			cmbChoices = new JComboBox();
			cmbChoices.setModel(cmbChoicesModel);
			cmbChoices.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent evt) {
					cmbChoicesActionPerformed();
				}
			});
		}
		return cmbChoices;
	}

	protected void cmbChoicesActionPerformed() {
		// if the change was not caused by an update
		if (!updateLock) {
			try {
				enumPositioner.moveTo(cmbChoices.getSelectedItem());
			} catch (DeviceException e) {
				logger.error("device exception while trying to move " + enumPositionerName + " to "
						+ (String) cmbChoices.getSelectedItem() + ": " + e.getMessage());
			}
		}
	}
	
	private void updateDisplay() {
		if (!updateLock) {
			updateLock = true;
			SwingUtilities.invokeLater(this);
		}
	}

	/**
	 * {@inheritDoc} Updates the displayed information. This MUST only be called from the Swing Thread via the
	 * updateDisplay method.
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		synchronized (this) {
			try {
				String currentPosition = (String) enumPositioner.getPosition();
				lblStatus.setText(currentPosition);
			} catch (DeviceException e) {
				logger.error("Exception while trying to observe " + enumPositionerName + ": " + e.getMessage());
			} finally {
				updateLock = false;
			}
		}
	}




}
