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

package gda.gui.generalscan;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;

/**
 * General Scan to display and allow editing of scan regions
 */
public class TimeScanPanel extends Scan {
	private JTable jTable;

	/**
	 * Constructor
	 */
	public TimeScanPanel() {
		setLayout(new BorderLayout());

		jTable = new JTable(getModel());
		jTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		add(new JScrollPane(jTable), BorderLayout.CENTER);

		setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Scan Dimension",
				TitledBorder.LEFT, TitledBorder.TOP, null, Color.black));
	}

	/**
	 * Creates an ScanModel
	 * 
	 * @return the scan model
	 */
	@Override
	protected ScanModel createModel() {
		ScanModel scanModel = new TimeScanModel();
		scanModel.setDefaultScan(0.0);
		return scanModel;
	}

	/**
	 * @see gda.gui.generalscan.Scan#init()
	 */
	@Override
	protected void init() {
		// do nothing at the moment
	}

	/**
	 * @see gda.gui.generalscan.Scan#reDisplay()
	 */
	@Override
	protected void reDisplay() {
		// does nothing
	}
}