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
import gda.factory.Findable;
import gda.factory.Finder;
import gda.observable.IObserver;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GUI bean which observes an EnumPositioner object which looks at the Epics SimpleBinary template.
 */
public class SimpleBinaryBean extends javax.swing.JPanel implements IObserver, Runnable {
	
	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		btnOperate.setEnabled(enabled);
	}

	private static final Logger logger = LoggerFactory.getLogger(SimpleBinaryBean.class);

	private JButton btnOperate;
	private String label = "Binary Control";
	private String simpleBinaryName = "";
	private EnumPositioner simpleBinary;
	protected String dialogMessage;
	protected String dialogTitle;
	protected boolean hasConfirmationDialog = false;
	
	// lock to prevent a backlog of threads trying to update this Bean.
	private volatile boolean updateLock = false;

	private JLabel lblState;

	/**
	 * Constructor.
	 */
	public SimpleBinaryBean() {
		super();
	}

	/**
	 * Call this to connect to the object and build the UI.
	 */
	public void configure() {
		if (simpleBinary == null && simpleBinaryName != null) {
			Findable obj = Finder.getInstance().find(simpleBinaryName);
			if (obj != null && obj instanceof EnumPositioner) {
				this.simpleBinary = (EnumPositioner) obj;
			}
		}
		if( simpleBinary != null){
			simpleBinary.addIObserver(this);
			initGUI();
			updateDisplay();
		} else {
			logger.error("SimpleBinaryBean cannot connect to object as information missing!");
		}
	}
	
	public void configureConfirmationDialog(String message, String title) {
		dialogMessage = message;
		dialogTitle = title;
		hasConfirmationDialog = true;
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
	 * @return the simpleBinaryName
	 */
	public String getSimpleBinaryName() {
		return simpleBinaryName;
	}

	/**
	 * @param simpleBinaryName
	 *            the simpleBinaryName to set
	 */
	public void setSimpleBinaryName(String simpleBinaryName) {
		this.simpleBinaryName = simpleBinaryName;
	}

	private void initGUI() {
//		this.setPreferredSize(new java.awt.Dimension(150, 67));
//		this.setBorder(BorderFactory.createTitledBorder(""));
		this.add(getBtnOperate());
		this.add(lblState = new JLabel("State"));
	}

	private JButton getBtnOperate() {
		if (btnOperate == null) {
			btnOperate = new JButton();
			btnOperate.setText(label);
			btnOperate.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent evt) {
					btnOperateActionPerformed();
				}
			});
		}
		return btnOperate;
	}

	protected boolean executeCommand() {
		if (hasConfirmationDialog) {
			int dialogSelection = JOptionPane.showConfirmDialog(SimpleBinaryBean.this, 
				dialogMessage,
				dialogTitle,
				JOptionPane.YES_NO_OPTION,
				JOptionPane.WARNING_MESSAGE);
			if (dialogSelection == JOptionPane.NO_OPTION)
				return false;
		}
		return true;
	}
	
	protected void btnOperateActionPerformed() {
		if (!executeCommand()) {
			return;
		}
		
		try {
			String[] positions = simpleBinary.getPositions();
			int index = ArrayUtils.indexOf(positions, this.lblState.getText());
			if (index == 0) {
				simpleBinary.moveTo(positions[1]);
			} else {
				simpleBinary.moveTo(positions[0]);
			}
		} catch (DeviceException e) {
			logger.error("exception while GUI bean trying to operate " + simpleBinaryName + ": " + e.getMessage());
		}
	}

	private void updateDisplay() {
		if (!updateLock) {
			updateLock = true;
			SwingUtilities.invokeLater(this);
		}
	}

	@Override
	public void run() {
		synchronized (this) {
			try {
				String currentPosition = (String) simpleBinary.getPosition();
				String[] positions = simpleBinary.getPositions();
				if (positions.length > 0 && currentPosition.equals(positions[0])) {
					this.lblState.setForeground(Color.RED);
				} else {
					this.lblState.setForeground(Color.decode("#228B22")); // nicegreen
				}
				this.lblState.setText(currentPosition);
			} catch (DeviceException e) {
				logger.error("Exception while trying to observe " + simpleBinaryName + ": " + e.getMessage());
			} finally {
				updateLock = false;
			}
		}
	}

	/**
	 * @return Returns the simpleBinary.
	 */
	public EnumPositioner getSimpleBinary() {
		return simpleBinary;
	}

	/**
	 * @param simpleBinary The simpleBinary to set.
	 */
	public void setSimpleBinary(EnumPositioner simpleBinary) {
		this.simpleBinary = simpleBinary;
	}
}
