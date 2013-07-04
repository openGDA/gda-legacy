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

package gda.gui.util;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.factory.Configurable;
import gda.factory.FactoryException;
import gda.factory.Findable;
import gda.factory.Finder;
import gda.observable.IObserver;

import java.awt.Color;
import java.awt.FlowLayout;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generates a JPanel to monitor a PV or Scannable.
 * <p>
 * The JPanel will have a border with the Scannable's name and its current value JLabel which will show RED coloured
 * digits when and if its value below certain threshold set by users. To use just add to an existing panel and be sure
 * to configure the scannableName via setScannableName and call configure() after that.
 */

public class StateDisplayPanel extends JPanel implements IObserver, Configurable, Findable {
	private static final Logger logger = LoggerFactory.getLogger(StateDisplayPanel.class);
	private String scannableName;
	private JLabel valueLabel;
	private String valueTitle = null;
	private Scannable scannable;

	private boolean configured = false;
	private Map<String, Color> states = new LinkedHashMap<String, Color>();

	/**
	 * Constructor.
	 */
	public StateDisplayPanel() {
	}

	@Override
	public void configure() throws FactoryException {
		if (scannable == null) {
			if (scannableName == null)
				return;
		}
		if (!configured) {
			if (scannable == null) {
				if ((scannable = (Scannable) Finder.getInstance().find(scannableName)) == null) {
					throw new FactoryException("Cannot find Scannable: " + scannableName);
				}
			}
			setLayout(new FlowLayout());
			String curval = "";
			try {
				curval = scannable.getPosition().toString();
			} catch (DeviceException e) {
				throw new FactoryException("Cannot get current position from Scannable: " + scannable.getName());
			}
			valueLabel = new JLabel(curval);
			valueLabel.setOpaque(true);
			valueLabel.setBackground(new Color(239, 239, 239));
			for (Entry<String, Color> each : states.entrySet()) {
				if (curval.equalsIgnoreCase(each.getKey())) {
					valueLabel.setForeground(each.getValue());
					valueLabel.setToolTipText(scannable.getName()+" "+each.getKey());
				}
			}
			if (getValueTitle() == null) {
				valueTitle = scannable.getName();
			}
			setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), valueTitle.trim(),
					TitledBorder.LEFT, TitledBorder.TOP, null, Color.black));

			add(valueLabel);

			scannable.addIObserver(this);
			configured = true;
		}
	}

	@Override
	public void update(Object theObserved, Object changeCode) {
		String valueStr = (String)changeCode;
		logger.warn("GUI panel update to {}", changeCode.toString());
		Scannable scannable;
		if (theObserved instanceof Scannable) {
			scannable = (Scannable) theObserved;
			for (Entry<String, Color> each : states.entrySet()) {
				if (valueStr.equalsIgnoreCase(each.getKey())) {
					valueLabel.setForeground(each.getValue());
					valueLabel.setToolTipText(scannable.getName()+" "+each.getKey());
				}
			}
		}
		valueLabel.setText(valueStr);
	}

	/**
	 * gets the findable name
	 * 
	 * @return name
	 */
	public String getScannableName() {
		return scannableName;
	}

	/**
	 * sets the findable name.
	 * 
	 * @param findableName
	 */
	public void setScannableName(String findableName) {
		this.scannableName = findableName;
	}
	/**
	 * gets the scanable object
	 * @return the beam object
	 */
	public Scannable getScannable() {
		return scannable;
	}
	/**
	 * sets the scannable object
	 * @param scannable
	 */
	public void setScannable(Scannable scannable) {
		this.scannable = scannable;
	}

	/**
	 * get value title for the panel border
	 * 
	 * @return title
	 */
	public String getValueTitle() {
		return valueTitle;
	}

	/**
	 * sets the value title for the panel border
	 * 
	 * @param valueTitle
	 */
	public void setValueTitle(String valueTitle) {
		this.valueTitle = valueTitle;
	}

	public Map<String, Color> getStates() {
		return states;
	}
	public void setStates(Map<String, Color> states) {
		this.states = states;
	}
}
