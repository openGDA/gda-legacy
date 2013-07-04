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

package gda.gui.oemove.plugins;

import gda.epics.connection.EpicsChannelManager;
import gda.epics.connection.EpicsController;
import gda.epics.connection.InitializationListener;
import gda.factory.FactoryException;
import gda.factory.Findable;
import gda.gui.oemove.Pluggable;
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.TimeoutException;
import gov.aps.jca.event.MonitorEvent;
import gov.aps.jca.event.MonitorListener;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This code was edited or generated using CloudGarden's Jigloo SWT/Swing GUI Builder, which is free for non-commercial
 * use. If Jigloo is being used commercially (ie, by a corporation, company or business for any purpose whatever) then
 * you should purchase a license for each developer using Jigloo. Please visit www.cloudgarden.com for details. Use of
 * Jigloo implies acceptance of these licensing terms. A COMMERCIAL LICENSE HAS NOT BEEN PURCHASED FOR THIS MACHINE, SO
 * JIGLOO OR THIS CODE CANNOT BE USED LEGALLY FOR ANY CORPORATE OR COMMERCIAL PURPOSE.
 */
/**
 * A plugin for OEMove which observes an Epics pv and displays/switches its state between two possible values.
 */
public class EpicsTwoStateControl implements Pluggable, Findable, InitializationListener, MonitorListener {
	private static final Logger logger = LoggerFactory.getLogger(EpicsTwoStateControl.class);
	private String name;
	private String state1Label = "state1";
	private String state2Label = "state2";
	private String state1Cmd = "ON";
	private String state2Cmd = "OFF";
	private String observedPV;

	private JLabel lblCurrentState;
	private JButton btnOperate;
	private JPanel pnlControl;
	private JPanel displayComponent;
	private JPanel controlComponent;

	private EpicsController controller = null;
	private EpicsChannelManager channelManager;
	private Channel channel;

	/**
	 * Constructor.
	 */
	public EpicsTwoStateControl() {
	}

	@Override
	public void configure() throws FactoryException {
		// connections made during the create?Component methods.
	}

	@Override
	public void initializationCompleted() throws TimeoutException, CAException, InterruptedException {
		String initialState = this.controller.cagetStringArray(channel)[0];
		getLblCurrentState().setText(initialState);
	}

	@Override
	public void monitorChanged(MonitorEvent arg0) {
		try {
			String currentState = this.controller.cagetStringArray(channel)[0];
			getLblCurrentState().setText(currentState);
		} catch (Exception e) {
			if( e instanceof RuntimeException)
				throw (RuntimeException)e;
			logger.error(getName() + " exception in monitorChanged",e);
		}
	}

	@Override
	public JComponent getControlComponent() {
		if (controlComponent == null) {
			createControlComponent();
		}
		return controlComponent;
	}

	@Override
	public JComponent getDisplayComponent() {
		if (displayComponent == null) {
			createDisplayComponent();
		}
		return displayComponent;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return Returns the state1Label.
	 */
	public String getState1Label() {
		return state1Label;
	}

	/**
	 * @param state1Label
	 *            The state1Label to set.
	 */
	public void setState1Label(String state1Label) {
		this.state1Label = state1Label;
	}

	/**
	 * @return Returns the state2Label.
	 */
	public String getState2Label() {
		return state2Label;
	}

	/**
	 * @param state2Label
	 *            The state2Label to set.
	 */
	public void setState2Label(String state2Label) {
		this.state2Label = state2Label;
	}

	/**
	 * @return Returns the state1Cmd.
	 */
	public String getState1Cmd() {
		return state1Cmd;
	}

	/**
	 * @param state1Cmd
	 *            The state1Cmd to set.
	 */
	public void setState1Cmd(String state1Cmd) {
		this.state1Cmd = state1Cmd;
	}

	/**
	 * @return Returns the state2Cmd.
	 */
	public String getState2Cmd() {
		return state2Cmd;
	}

	/**
	 * @param state2Cmd
	 *            The state2Cmd to set.
	 */
	public void setState2Cmd(String state2Cmd) {
		this.state2Cmd = state2Cmd;
	}

	/**
	 * @return Returns the observedPV.
	 */
	public String getObservedPV() {
		return observedPV;
	}

	/**
	 * @param observedPV
	 *            The observedPV to set.
	 */
	public void setObservedPV(String observedPV) {
		this.observedPV = observedPV;
	}

	private void createConnection() {
		controller = EpicsController.getInstance();
		channelManager = new EpicsChannelManager(this);
		try {
			channel = channelManager.createChannel(getObservedPV(), false);
			controller.setMonitor(channel, this);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void createControlComponent() {
		if (channel == null) {
			createConnection();
		}
		controlComponent = new JPanel();
		controlComponent.add(getPnlControl());
	}

	private void createDisplayComponent() {
		if (channel == null) {
			createConnection();
		}
		displayComponent = new JPanel();
		displayComponent.add(getLblCurrentState());
	}

	private JButton getBtnOperate() {
		if (btnOperate == null) {
			btnOperate = new JButton();
			btnOperate.setText(getState1Label() + "/" + getState2Label());
			btnOperate.setToolTipText("Changes the state");
			btnOperate.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent evt) {
					btnOperateMouseClicked();
				}
			});
		}
		return btnOperate;
	}

	private JLabel getLblCurrentState() {
		if (lblCurrentState == null) {
			lblCurrentState = new JLabel();
			lblCurrentState.setBorder(BorderFactory.createTitledBorder("Current state"));
			lblCurrentState.setPreferredSize(new java.awt.Dimension(114, 40));
			lblCurrentState.setAlignmentY(0.0f);
			lblCurrentState.setText(getState1Label());
		}
		return lblCurrentState;
	}

	private void btnOperateMouseClicked() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					if (lblCurrentState.getText().equals(getState1Label())) {
						switchUIToState2();
						controller.caput(channel, state2Cmd);
					} else {
						switchUIToState1();
						controller.caput(channel, state1Cmd);
					}
				} catch (Exception e) {
					logger.error(getName() + " exception in btnOperateMouseClicked",e);
				}
			}
		});
	}

	private void switchUIToState1() {
		lblCurrentState.setText(getState1Label());
		lblCurrentState.setForeground(Color.GREEN);
	}

	private void switchUIToState2() {
		lblCurrentState.setText(getState2Label());
		lblCurrentState.setForeground(Color.RED);
	}

	private JPanel getPnlControl() {
		if (pnlControl == null) {
			pnlControl = new JPanel();
			pnlControl.setBorder(BorderFactory.createTitledBorder("Change the state"));
			pnlControl.setPreferredSize(new java.awt.Dimension(152, 57));
			pnlControl.setMinimumSize(new java.awt.Dimension(152, 57));
			pnlControl.add(getBtnOperate());
		}
		return pnlControl;
	}

}
