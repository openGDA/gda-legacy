/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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
import gda.device.NamedEnumPositioner;
import gda.factory.Findable;
import gda.factory.Finder;
import gda.observable.IObserver;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnumPositionerComboBean extends javax.swing.JPanel implements IObserver, Runnable {
	
	private static final Logger logger = LoggerFactory.getLogger(EnumPositionerComboBean.class);

	protected JComboBox cmbChoices;
	protected String label;
	protected String enumPositionerName = "";
	protected String[] commandPositions = null;
	protected EnumPositioner enumPositioner = null;
	// lock to prevent a backlog of threads trying to update this Bean.
	protected volatile boolean updateLock = false;

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		cmbChoices.setEnabled(enabled);
	}	
	/**
	 * Constructor.
	 */
	public EnumPositionerComboBean() {
		super();
	}

	private java.awt.Dimension initialPreferredSize = new java.awt.Dimension(150, 67);
	
	public java.awt.Dimension getInitialPreferredSize() {
		return initialPreferredSize;
	}
	public void setInitialPreferredSize(java.awt.Dimension initialPreferredSize) {
		this.initialPreferredSize = initialPreferredSize;
	}
	
	private String cmbLabel;
	
	public String getCmbLabel() {
		return cmbLabel;
	}
	public void setCmbLabel(String cmbLabel) {
		this.cmbLabel = cmbLabel;
	}
	private void initGUI() {
		if( initialPreferredSize != null)
			this.setPreferredSize(initialPreferredSize);
		if( label != null && !label.isEmpty())
			this.setBorder(BorderFactory.createTitledBorder(label));
		if( cmbLabel != null && !cmbLabel.isEmpty())
			this.add(new JLabel(cmbLabel));
		this.add(getCmbChoices());
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
					if (obj instanceof NamedEnumPositioner) {
						String[] positionNameList= ((NamedEnumPositioner)enumPositioner).getPositions();
						commandPositions = positionNameList;
					}
					else {
						commandPositions = enumPositioner.getPositions();
					}
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
					uk.ac.gda.util.ThreadManager.getThread(new Runnable() {
						@Override
						public void run() {
							cmbChoicesActionPerformed();
						}
					}).start();
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
				if (enumPositioner instanceof NamedEnumPositioner) {
					currentPosition = ((NamedEnumPositioner)enumPositioner).getPositionName();
				}
				int location = ArrayUtils.indexOf(commandPositions, currentPosition);
				cmbChoices.setSelectedIndex(location);
			} catch (DeviceException e) {
				logger.error("Exception while trying to observe " + enumPositionerName + ": " + e.getMessage());
			} finally {
				updateLock = false;
			}
		}
	}

}
