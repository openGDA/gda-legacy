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

package gda.gui.eventnotification;

import gda.configuration.properties.LocalProperties;
import gda.factory.FactoryException;
import gda.factory.Finder;
import gda.gui.AcquisitionFrame;
import gda.gui.AcquisitionPanel;
import gda.observable.IObservable;
import gda.observable.IObserver;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.File;
import java.util.HashMap;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Event notification panel provides XML-specified information, for instance on beam availability.
 * User is alerted to changes in this information by the panel automatically being brought to the front.
 * 
 */
public class EventNotificationPanel extends AcquisitionPanel implements IObserver {

	private static final Logger logger = LoggerFactory.getLogger(EventNotificationPanel.class);

	private String xmlConfigName;
	private EventNotificationParams params;
	
	private JCheckBox ignoreAllFutureUpdatesCheckBox;

	/**
	 * Contains each monitor as an IObservable and its related notification panel
	 */
	private HashMap<IObservable, NotificationPanel> panels = new HashMap<IObservable, NotificationPanel>();
	
	@Override
	public void configure() throws FactoryException {
		
		try {
			String configName = LocalProperties.get(LocalProperties.GDA_CONFIG);
			if (configName!= null) {
				configName += File.separator + "xml" + File.separator + getXmlConfigName();
			} else {
				configName = getXmlConfigName();
			}
			this.params = EventNotificationParams.createFromXML(configName);
		} catch (Exception e) {
			logger.error("Problem creating notification parameters:" + e.getMessage());
		}

		// create vertical box layout of panels, each containing a grid layout of horizontal notification panels
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		JPanel horizontalPanel = new JPanel(new GridBagLayout());
		int col = 0;
		
		for (NotificationPanel panel: this.params.getPanels()) {
			if (panel.isPlacePanelOnNewLine() && horizontalPanel.getComponentCount() > 0) {

				// add current horizontal panel to main panel 
				add(horizontalPanel);
				horizontalPanel = new JPanel(new GridBagLayout());
				col = 0;
			}
			horizontalPanel.add(panel, getNewConstraints(col, panel.getTextPositioning()));
			col ++;
			
			panel.configure();
			addIObervableToPanel(panel);
		}		
		add(horizontalPanel);		// final row
		
		// add final button and check box 
		JPanel bottomPanel = new JPanel(new FlowLayout());
/*		bottomPanel.add(this.okayButton = new JButton("Close window"));
		this.okayButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
					AcquisitionFrame.hideFrame("EventNotificationPanel");
				}
			});*/
		bottomPanel.add(this.ignoreAllFutureUpdatesCheckBox = new JCheckBox("Ignore future updates on all", false));
		this.ignoreAllFutureUpdatesCheckBox.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent e) {
					for (NotificationPanel panel: params.getPanels()) {
						panel.setIgnoreCheckBox(ignoreAllFutureUpdatesCheckBox.isSelected());
					}
				}
			});
		add(bottomPanel);
	}
	/**
	 * Adds an IObservable for given panel
	 * 
	 * @param panel the notification panel
	 */
	private void addIObervableToPanel(NotificationPanel panel) {
		Object instance = Finder.getInstance().find(panel.getName());
		if (instance instanceof IObservable) {					
			this.panels.put((IObservable)instance, panel);
			((IObservable)instance).addIObserver(this);
			
			if (panel instanceof PvPanel) {			// only PvPanel will have 'ignore-updates' checkbox
				JCheckBox ignoreCheckBox = panel.getIgnoreCheckBox();
				ignoreCheckBox.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
						if (!((JCheckBox)e.getSource()).isSelected()) {
							ignoreAllFutureUpdatesCheckBox.setSelected(false);
						}
					}
				});
			}
		}
	}
	/**
	 * Returns new grid bag constraints
	 * 
	 * @param cellX the column position
	 * @param anchor value for the anchor
	 * @return new grid bag constraints
	 */
	private GridBagConstraints getNewConstraints(int cellX, String anchor) {
		GridBagConstraints c = new GridBagConstraints();
		c.weightx = 0.5;
		c.weighty = 0.5;
		c.fill = GridBagConstraints.BOTH;
		c.gridx = cellX;
		if (anchor != null && anchor.toLowerCase().equals("east")) {
			c.anchor = GridBagConstraints.EAST;
		} else {
			c.anchor = GridBagConstraints.WEST;
		}
		return c;
	}

	@Override
	public void update(Object theObserved, Object changeCode) {
		NotificationPanel panel = this.panels.get(theObserved);
		panel.update(theObserved, changeCode);
		if (!panel.igoreFutureUpdates()) {
			try {
				if (panel instanceof PvPanel) {			// only updated PvPanel will bring event panel to front
					AcquisitionFrame.showFrame("EventNotificationPanel");
				}
			}
			catch (Exception e) {
				logger.error("Problem updating notification panel: " + e.getMessage());
			}
		}
	}
	/**
	 * @return XML configuration file name
	 */
	public String getXmlConfigName() {
		return xmlConfigName;
	}
	/**
	 * @param xmlConfigName XML configuration file name
	 */
	public void setXmlConfigName(String xmlConfigName) {
		this.xmlConfigName = xmlConfigName;
	}
}