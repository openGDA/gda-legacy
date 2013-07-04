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

package gda.device.attenuator;

import gda.device.Attenuator;
import gda.device.DeviceException;
import gda.factory.FactoryException;
import gda.factory.Finder;
import gda.gui.AcquisitionPanel;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;


/**
 *
 */
public class AttenuationPanel extends AcquisitionPanel implements Runnable {
	private FilterDisplay[] filters = null;

	private Attenuator attenuator;
	private volatile boolean valuesChanged = false;
	private volatile Double currentDesiredEnergy;
	private volatile Double currentDesiredTransmission;
	private volatile Double currentClosestMatchEnergy;
	private volatile Double currentClosestMatchTransmission;
	private volatile boolean[] currentActualFilters;
	private volatile boolean[] currentCalculatedFilters;

	protected JTextField currentDesiredEnergyTxt;

	protected JLabel currentClosestMatchEnergyLbl;

	protected JTextField currentDesiredTransmissionTxt;

	protected JLabel currentClosestMatchTransmissionLbl;

	protected Container pnlFilters;
	
	
	/**
	 * 
	 */
	public AttenuationPanel(){
		initGUI();
	}

	@Override
	public void configure() throws FactoryException {
		attenuator = Finder.getInstance().find("xia");

		try {
			int numberFilters = attenuator.getNumberFilters();
			filters = new FilterDisplay[numberFilters];
			final String[] filterNames = attenuator.getFilterNames();
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					for (int i = 0; i < filters.length; i++) {
						filters[i].setName(filterNames[i]);
					}
				}
			});
		} catch (DeviceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		refreshDisplay();
		uk.ac.gda.util.ThreadManager.getThread(this, this.getClass().getName()).start();
	}

	@Override
	public void update(Object theObserved, Object changeCode) {
		refreshDisplay();
	}

	@Override
	public void run() {
		while (true) {
			if (valuesChanged)
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						currentDesiredEnergyTxt.setText(currentDesiredEnergy.toString());
						currentClosestMatchEnergyLbl.setText(currentClosestMatchEnergy.toString());
						currentDesiredTransmissionTxt.setText(currentDesiredTransmission.toString());
						currentClosestMatchTransmissionLbl.setText(currentClosestMatchTransmission.toString());
						valuesChanged = false;

						if (filters != null && filters.length == currentActualFilters.length) {
							for (int i = 0; i < filters.length; i++) {
								filters[i].setCalculated(currentCalculatedFilters[i]);
								filters[i].setActual(currentActualFilters[i]);
								pnlFilters.add(filters[i], new GridBagConstraints(0, i + 1, 1, 1, 0.0, 0.0,
										GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
										new Insets(0, 0, 0, 0), 0, 0));
							}
						}

					}
				});
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void refreshDisplay() {
		try {
			currentDesiredEnergy = attenuator.getDesiredEnergy();
			currentDesiredTransmission = attenuator.getDesiredTransmission();
			currentClosestMatchEnergy = attenuator.getClosestMatchEnergy();
			currentClosestMatchTransmission = attenuator.getTransmission();
			currentActualFilters = attenuator.getFilterPositions();
			currentCalculatedFilters = attenuator.getDesiredFilterPositions();
			valuesChanged = true;
		} catch (DeviceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void initGUI() {
		try {
			BorderLayout thisLayout = new BorderLayout();
			this.setLayout(thisLayout);
			{
				JPanel pnlControl = new JPanel();
				GridBagLayout pnlControlLayout = new GridBagLayout();
				pnlControlLayout.rowWeights = new double[] { 0.1, 0.1, 0.1, 0.1 };
				pnlControlLayout.rowHeights = new int[] { 7, 7, 7, 7 };
				pnlControlLayout.columnWeights = new double[] { 0.1 };
				pnlControlLayout.columnWidths = new int[] { 7 };
				this.add(pnlControl, BorderLayout.NORTH);
				pnlControl.setBorder(BorderFactory.createTitledBorder("Parameters"));
				pnlControl.setPreferredSize(new java.awt.Dimension(336, 297));
				pnlControl.setLayout(pnlControlLayout);
				{
					JPanel pnlEnergy = new JPanel();
					GridBagLayout pnlEnergyLayout = new GridBagLayout();
					pnlControl.add(pnlEnergy, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
							GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
					pnlEnergy.setBorder(BorderFactory.createTitledBorder("Energy"));
					pnlEnergyLayout.rowWeights = new double[] { 0.1, 0.1, 0.1, 0.1 };
					pnlEnergyLayout.rowHeights = new int[] { 7, 7, 7, 7 };
					pnlEnergyLayout.columnWeights = new double[] { 0.1, 0.1, 0.1, 0.1 };
					pnlEnergyLayout.columnWidths = new int[] { 7, 7, 7, 7 };
					pnlEnergy.setLayout(pnlEnergyLayout);
					{
						JLabel jLabel1 = new JLabel();
						pnlEnergy.add(jLabel1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST,
								GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
						jLabel1.setText("Desired:");
					}
					{
						JLabel jLabel2 = new JLabel();
						pnlEnergy.add(jLabel2, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.EAST,
								GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
						jLabel2.setText("Closest Match:");
					}
					{
						currentClosestMatchEnergyLbl = new JLabel();
						pnlEnergy.add(currentClosestMatchEnergyLbl, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
								GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
						currentClosestMatchEnergyLbl.setText("jLabel3");
					}
					{
						currentDesiredEnergyTxt = new JTextField("");
						pnlEnergy.add(currentDesiredEnergyTxt, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
								GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
						currentDesiredEnergyTxt.setText("jTextField1");
					}
				}
				{
					JPanel pnlTransmission = new JPanel();
					GridBagLayout pnlTransmissionLayout = new GridBagLayout();
					pnlControl.add(pnlTransmission, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
					pnlTransmission.setBorder(BorderFactory.createTitledBorder("Transmission"));
					pnlTransmissionLayout.rowWeights = new double[] { 0.1, 0.1, 0.1, 0.1 };
					pnlTransmissionLayout.rowHeights = new int[] { 7, 7, 7, 7 };
					pnlTransmissionLayout.columnWeights = new double[] { 0.1, 0.1, 0.1, 0.1 };
					pnlTransmissionLayout.columnWidths = new int[] { 7, 7, 7, 7 };
					pnlTransmission.setLayout(pnlTransmissionLayout);
					pnlTransmission.add(new JLabel("Desired:"), new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
							GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
					pnlTransmission.add(new JLabel("Closest Match:"), new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
								GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
					{
						currentClosestMatchTransmissionLbl = new JLabel("");
						pnlTransmission.add(currentClosestMatchTransmissionLbl, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
								GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
					}
					{
						currentDesiredTransmissionTxt = new JTextField("");
						pnlTransmission.add(currentDesiredTransmissionTxt, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
								GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
					}
				}
				{
					JPanel pnlButtons = new JPanel();
					pnlControl.add(pnlButtons, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
					{
						JButton btnCurrentEnergy = new JButton("Calculate Closest Values");
						pnlButtons.add(btnCurrentEnergy);
					}
					{
						JButton btnChangeFilters = new JButton("Change Filters");
						pnlButtons.add(btnChangeFilters);
					}
				}
			}
			{
				JPanel pnlFilters = new JPanel();
				GridBagLayout pnlFiltersLayout = new GridBagLayout();
				this.add(pnlFilters, BorderLayout.CENTER);
				pnlFilters.setBorder(BorderFactory.createTitledBorder("Filter Positions"));
				pnlFiltersLayout.rowWeights = new double[] { 0.1, 0.1, 0.1, 0.1 };
				pnlFiltersLayout.rowHeights = new int[] { 7, 7, 7, 7 };
				pnlFiltersLayout.columnWeights = new double[] { 0.1 };
				pnlFiltersLayout.columnWidths = new int[] { 7 };
				pnlFilters.setLayout(pnlFiltersLayout);

				JPanel pnlTitles = new JPanel();
				pnlFilters.add(pnlTitles, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
						GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
				pnlTitles.add(new JLabel("Filter"));
				pnlTitles.add(new JLabel("Calculated"));
				pnlTitles.add(new JLabel("Actual"));
				pnlTitles.add(new JLabel("Move"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
