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

import gda.beamline.BeamInfo;
import gda.factory.Configurable;
import gda.factory.FactoryException;
import gda.factory.Findable;
import gda.factory.Finder;
import gda.observable.IObserver;

import java.awt.Color;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

/**
 * Generates a JPanel to monitor a PV or Scannable.
 * <p>
 * The JPanel will have a border with the Scannable's name and its current value JLabel which will show RED coloured
 * digits when and if its value below certain threshold set by users. To use just add to an existing panel and be sure
 * to configure the scannableName via setScannableName and call configure() after that.
 */

public class ValueDisplayPanel extends JPanel implements IObserver, Configurable, Findable {
	private String findableName;
	private JLabel valueLabel;
	private String valueTitle;
	private BeamInfo beam;

	private boolean configured = false;

	/**
	 * Constructor.
	 */
	public ValueDisplayPanel() {
	}

	@Override
	public void configure() throws FactoryException {
		if (beam == null) {
			if (findableName == null)
				return;
		}
		if (!configured) {
			if (beam == null) {
				if ((beam = (BeamInfo) Finder.getInstance().find(findableName)) == null) {
					throw new FactoryException("Cannot find object: " + findableName);
				}
			}
			setLayout(new FlowLayout());
			double curval = 0.0;
			if (!(((Double) beam.getWavelength()).isNaN())) {
				curval = beam.getWavelength();
				valueLabel = new JLabel(String.format("%7.5f", curval));
			} else {
				valueLabel = new JLabel("Not Set");
			}
			valueLabel.setOpaque(true);
			valueLabel.setBackground(new Color(239, 239, 239));
			if (beam.isCalibrated()) {
				valueLabel.setForeground(Color.blue);
				valueLabel.setToolTipText("Calibrated wavelength value.");
			} else {
				valueLabel.setForeground(Color.red);
				valueLabel.setToolTipText("Uncalibrated wavelength value.");
			}
			setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), valueTitle,
					TitledBorder.LEFT, TitledBorder.TOP, null, Color.black));

			add(valueLabel);

			beam.addIObserver(this);
			configured = true;
		}
	}


	@Override
	public void update(Object theObserved, Object changeCode) {
		double value = -999;
		String valueStr = "";
		BeamInfo bi;
		if (theObserved instanceof BeamInfo) {
			bi = (BeamInfo) theObserved;
			value = ((Double) changeCode).doubleValue();
			valueStr = String.format("%7.5f", value);
			if (bi.isCalibrated()) {
				valueLabel.setForeground(Color.blue);
				valueLabel.setToolTipText("Calibrated wavelength value.");
			} else {
				valueLabel.setForeground(Color.red);
				valueLabel.setToolTipText("Uncalibrated wavelength value.");
			}
		}
		valueLabel.setText(valueStr);
	}

	/**
	 * gets the findable name
	 * 
	 * @return name
	 */
	public String getFindableName() {
		return findableName;
	}

	/**
	 * sets the findable name.
	 * 
	 * @param findableName
	 */
	public void setFindableName(String findableName) {
		this.findableName = findableName;
	}
	/**
	 * gets the beam properties object
	 * @return the beam object
	 */
	public BeamInfo getBeam() {
		return beam;
	}
	/**
	 * sets the beam properties object
	 * @param beam
	 */
	public void setBeam(BeamInfo beam) {
		this.beam = beam;
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
}
