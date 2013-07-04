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

import gda.factory.Finder;
import gda.oe.MoveableException;
import gda.oe.OE;
import gda.util.QuantityFactory;
import gda.util.StandardJTable;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.table.TableColumn;

import org.jscience.physics.quantities.Quantity;
import org.jscience.physics.units.SI;
import org.jscience.physics.units.Unit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * General Scan to display and allow editing of scan regions
 */
public class GeneralScan extends Scan {
	
	private static final Logger logger = LoggerFactory.getLogger(GeneralScan.class);
	
	protected StandardJTable jTable;

	protected JPanel innerPanel = new JPanel();

	private JButton addButton;

	private JButton deleteButton;

	protected JComboBox unitsCombo = new JComboBox();

	protected JComponent unitsComponent = unitsCombo;

	protected JComboBox oeNameCombo = new JComboBox();

	protected JComponent oeNameComponent = oeNameCombo;

	private JComboBox dofNameCombo = new JComboBox();

	protected JComponent dofNameComponent = dofNameCombo;

	protected TableColumn column4;

	private Hashtable<String, ScanModel> dofModels = new Hashtable<String, ScanModel>();

	// protected ScanModel scanModel;
	protected String items[];

	protected ArrayList<String> selectedDofNames = new ArrayList<String>();

	protected String oeName;

	protected int originalRowHeight;

	// FIXME this should be an array of comparators for multi-dimensional
	// scans
	private Comparator<String> oENamesComparator = new Comparator<String>() {
		@Override
		public int compare(String o1, String o2) {
			// Comparator which always returns 0 leaves things unsorted
			return 0;
		}
	};

	private Comparator<String> dofNamesComparator = oENamesComparator;

	private boolean addRegionEnabled = true;

	private boolean fixedDof = false;

	/** */
	public static String TIMESCAN = "Detectors";
	/** */
	public static String ASYNC_TIMESCAN = "Async Detector";
	/** */
	public static String SELECTED = "Selected";

	/**
	 * Constructor
	 */
	public GeneralScan() {
		this("Scan Dimension");
	}

	/**
	 * construct a general scan panel
	 * 
	 * @param name
	 *            String name of the scan dimension panel eg. "Monochromator Scan" or "Scan Dimension 1"
	 */
	public GeneralScan(String name) {
		this(name, true);
	}

	/**
	 * construct a general scan panel
	 * 
	 * @param name
	 *            String name of the scan dimension panel eg. "Monochromator Scan" or "Scan Dimension 1"
	 * @param addRegionEnabled
	 *            boolean true if users are allowed to add regions to the scan
	 */
	public GeneralScan(String name, boolean addRegionEnabled) {
		this.addRegionEnabled = addRegionEnabled;
		setLayout(new BorderLayout());

		jTable = new StandardJTable(getModel());
		jTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		originalRowHeight = jTable.getRowHeight();
		add(new JScrollPane(jTable), BorderLayout.CENTER);

		oeNameCombo.addActionListener(oeNameComboActionListener);
		dofNameCombo.addActionListener(dofNameComboActionListener);
		unitsCombo.addActionListener(unitsComboActionListener);

		addButton = new JButton("Add Region");
		addButton.setToolTipText("Click to add another region");
		addButton.setEnabled(false);
		addButton.addActionListener(addButtonActionListener);

		deleteButton = new JButton("Delete Region");
		deleteButton.setEnabled(false);
		deleteButton.setToolTipText("Click to delete the last region");
		deleteButton.addActionListener(deleteButtonActionListener);

		constructDefaultControlPanel();

		add(innerPanel, BorderLayout.SOUTH);
		setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), name, TitledBorder.LEFT,
				TitledBorder.TOP, null, Color.black));
	}

	/**
	 * contruct a panel for users to select scan component details (OE, DOF, units, extra scan regions)
	 */
	private void constructDefaultControlPanel() {
		if (innerPanel.getComponentCount() > 0)
			innerPanel.removeAll();

		innerPanel.add(new JLabel("Scan "));
		innerPanel.add(oeNameComponent);
		innerPanel.add(dofNameComponent);
		innerPanel.add(new JLabel(" in units of "));
		innerPanel.add(unitsComponent);
		if (addRegionEnabled) {
			innerPanel.add(addButton);
			innerPanel.add(deleteButton);
		}
	}

	/**
	 * modify control panel according to scan components available changing JComboBox to JLabel if only one item
	 * available
	 */
	private void modifyDefaultControlPanel() {
		if (oeNameCombo.getItemCount() == 1)
			oeNameComponent = new JLabel(oeNameCombo.getItemAt(0).toString());
		else
			oeNameComponent = oeNameCombo;

		if (dofNameCombo.getItemCount() == 1)
			dofNameComponent = new JLabel(dofNameCombo.getItemAt(0).toString());
		else
			dofNameComponent = dofNameCombo;

		if (unitsCombo.getItemCount() == 1)
			unitsComponent = new JLabel(unitsCombo.getItemAt(0).toString());
		else
			unitsComponent = unitsCombo;

		constructDefaultControlPanel();
	}

	/**
	 * use the scan model to check that the scan is valid and pop up error message if not valid
	 */
	public void check() {
		if (oeName.equals(TIMESCAN)) {
			((TimeScanModel) getModel()).getValid();
		} else {
			Object o = Finder.getInstance().find(oeName);
			if (o instanceof OE)
				((GeneralScanModel) getModel()).check((OE) o);
		}

		notifyIObservers(GeneralScan.this, null);
	}

	/**
	 * Set the names of scannables available for users to scan
	 * 
	 * @param oeNames
	 *            an ArrayList of String oe names available for user scanning and make these available for selection via
	 *            the defaultControlPanel
	 */
	public void setScannableNames(ArrayList<String> oeNames) {
		Collections.sort(oeNames, oENamesComparator);
		for (String s : oeNames)
			oeNameCombo.addItem(s);

		modifyDefaultControlPanel();
	}

	/**
	 * Set the names of scannables available for users to scan. Expects a single oe and a single dof
	 * 
	 * @param oeNames
	 *            an ArrayList of String oe names available for user scanning and make these available for selection via
	 *            the defaultControlPanel
	 * @param dofNames
	 *            the selected dof names for this dimension (set in client XML)
	 */
	public void setScannableNames(ArrayList<String> oeNames, ArrayList<String> dofNames) {
		fixedDof = true;

		selectedDofNames.add(dofNames.get(0));
		oeNameCombo.addItem(oeNames.get(0));

		modifyDefaultControlPanel();
	}

	/**
	 * disable time column of all scans except one since only one detector count time can be used during a nested OE
	 * scan
	 */
	public void disableTimeColumn() {
		if (jTable.getColumnModel().getColumnCount() == 6) {
			column4 = jTable.getColumnModel().getColumn(4);
			jTable.removeColumn(column4);
			((GeneralScanModel) getModel()).setAddTime(false);
		}
	}

	/**
	 * enable the time column of a scan, used when the scan containing the time column has been deleted
	 */
	public void enableTimeColumn() {
		if (jTable.getColumnModel().getColumnCount() == 5) {
			jTable.addColumn(column4);
			jTable.moveColumn(jTable.getColumnCount() - 1, 4);
			((GeneralScanModel) getModel()).setAddTime(true);
		}
	}

	/**
	 * Create a GeneralScanModel
	 * 
	 * @return the scan model
	 */
	@Override
	protected ScanModel createModel() {
		ScanModel scanModel = new GeneralScanModel("", "t");
		scanModel.setDefaultScan(8.0);
		return scanModel;
	}

	/**
	 * Enable add and delete buttons as appropriate and required
	 */
	@Override
	protected void reDisplay() {
		if (getModel().getRowCount() == 1)
			deleteButton.setEnabled(false);
		else
			deleteButton.setEnabled(true);
	}

	/**
	 * Get the name of the DOF selected to be scanned
	 * 
	 * @return the dof name
	 */
	public String getDofName() {
		return ((GeneralScanModel) getModel()).getDofName();
	}

	/**
	 * Get the name of the OE selected to be scanned
	 * 
	 * @return the dof name
	 */
	public String getOEName() {
		return oeName;
	}

	/**
	 * @see gda.gui.generalscan.Scan#init()
	 */
	@Override
	protected void init() {
		// do nothing at the moment
	}

	/**
	 * Get the scan units
	 * 
	 * @return the scan units as a Unit
	 */
	public Unit<? extends Quantity> getScanUnits() {
		return QuantityFactory.createUnitFromString((String) unitsCombo.getSelectedItem());
	}

	/**
	 * Get the total scan time
	 * 
	 * @return the total time
	 */
	public double getTotalTime() {
		return getModel().getTotalTime();
	}

	/**
	 * Get the total scan time
	 * 
	 * @return the total time
	 */
	public double getTimePerPoint() {
		return getTotalTime() / getModel().getTotalPoints();
	}

	/**
	 * set the oENamesComparator by which OE names will be checked against eachother
	 * 
	 * @param comparator
	 */
	public void setOENamesComparator(Comparator<String> comparator) {
		this.oENamesComparator = comparator;
	}

	/**
	 * Get the comparator function which checks oe names against each other
	 * 
	 * @return the comparator
	 */
	public Comparator<String> getOENamesComparator() {
		return oENamesComparator;
	}

	/**
	 * Get the comparator function which checks dof names against each other
	 * 
	 * @return the comparator
	 */
	public Comparator<String> getDofNamesComparator() {
		return dofNamesComparator;
	}

	/**
	 * set the dofNamesComparator by which DOF names will be checked against eachother
	 * 
	 * @param dofNamesComparator
	 *            the comparator
	 */
	public void setDofNamesComparator(Comparator<String> dofNamesComparator) {
		this.dofNamesComparator = dofNamesComparator;
	}

	/**
	 * Increase row height of JTable
	 * 
	 * @param byPixels
	 *            num pixrels to increase row height by
	 */
	public void increaseRowHeight(int byPixels) {
		jTable.setRowHeight(originalRowHeight + byPixels);
	}

	/**
	 * ActionListener for addButton
	 */
	private ActionListener addButtonActionListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent ev) {
			if (!oeName.equals(TIMESCAN))
				((GeneralScanModel) getModel()).addRow();
			// else
			// ((TimeScanModel) getModel()).addRow();
		}
	};

	/**
	 * ActionListener for deleteButton
	 */
	private ActionListener deleteButtonActionListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent ev) {
			((GeneralScanModel) getModel()).deleteLastRow();
		}
	};

	/**
	 * ActionListener for dofNameCombo
	 */
	private ActionListener dofNameComboActionListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent ev) {
			if (dofNameCombo.getItemCount() > 0) {
				String oeName = (String) oeNameCombo.getSelectedItem();
				try {
					String dofName = (String) dofNameCombo.getSelectedItem();
					ScanModel newModel = dofModels.get(dofName);
					if (newModel == null) {
						if (oeName.equals(TIMESCAN)) {
							newModel = new TimeScanModel();
							newModel.setDefaultScan(0.0);
						} else if (Finder.getInstance().find(oeName) instanceof OE) {
							newModel = new GeneralScanModel(dofName, "t");
							((GeneralScanModel) newModel).setDefaultScan(((OE) Finder.getInstance().find(oeName))
									.getPosition(dofName));
						}
						dofModels.put(dofName, newModel);
					}
					// Tell the superclass AND the jTable to use this model
					jTable.setModel(newModel);
					setModel(newModel);
					reDisplay();

					if (!oeName.equals(TIMESCAN))
						addButton.setEnabled(true);

					reDisplay();
					notifyIObservers(GeneralScan.this, null);

					// Set the unitsCombo to show the acceptable units of
					// this DOF
					unitsCombo.removeAllItems();
					if (oeName.equals(TIMESCAN)) {
						unitsCombo.addItem(SI.MILLI(SI.SECOND).toString());
					} else {
						OE oe = ((OE) Finder.getInstance().find(oeName));
						ArrayList<Unit<? extends Quantity>> unitsArray = oe.getAcceptableUnits(dofName);
						for (Unit<? extends Quantity> unit : unitsArray)
							unitsCombo.addItem(unit.toString());
					}
				} catch (MoveableException e) {
					logger.error(e.getMessage());
				}
			}
			modifyDefaultControlPanel();
		}
	};

	/**
	 * ActionListener for oeNameCombo
	 */
	private ActionListener oeNameComboActionListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent ev) {
			constructDefaultControlPanel();
			oeName = (String) oeNameCombo.getSelectedItem();

			if (oeName.equals(TIMESCAN)) {
				dofNameCombo.removeAllItems();
				dofNameCombo.addItem(SELECTED);
			} else {
				OE oe = ((OE) Finder.getInstance().find(oeName));
				if (!fixedDof) {
					items = oe.getDOFNames();
					Arrays.sort(items, dofNamesComparator);
					selectedDofNames = new ArrayList<String>();
					for (int i = 0; i < items.length; i++)
						selectedDofNames.add(items[i]);
				}
				dofNameCombo.removeAllItems();
				for (String dofName : selectedDofNames) {
					try {
						if (oe.isScannable(dofName))
							dofNameCombo.addItem(dofName);
					} catch (MoveableException e) {
						logger.debug(e.getStackTrace().toString());
					}
				}
				// setSelectedDof(dofNameCombo.getItemAt(0).toString());
			}
			modifyDefaultControlPanel();
			// updateUI();
			// car added for detectors check box setting & disabling
			// AddDimension
			// for TimeScan
			notifyIObservers(GeneralScan.this, null);
		}
	};

	/**
	 * ActionListener for unitsCombo
	 */
	protected ActionListener unitsComboActionListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent ev) {
			String dofName = (String) dofNameCombo.getSelectedItem();
			if (dofName != null && !oeName.equals(TIMESCAN)) {
				Unit<? extends Quantity> unit = getScanUnits();
				if (unit != null)
					((GeneralScanModel) getModel()).setDisplayUnits(unit);
			}
		}
	};

	/**
	 * remove named scannable from list available for selection
	 * 
	 * @param scannableName
	 *            name of oe or detector to be removed
	 */
	public void removeOeName(String scannableName) {
		for (int i = 0; i < oeNameCombo.getItemCount(); i++)
			if (oeNameCombo.getItemAt(i).equals(scannableName))
				oeNameCombo.removeItemAt(i);
	}

	/**
	 * add named scannable to list available for selection
	 * 
	 * @param scannableName
	 *            name of oe or detector to be added
	 */
	public void addOeName(String scannableName) {
		oeNameCombo.addItem(scannableName);
	}

	/**
	 * remove named dof from list available for selection
	 * 
	 * @param scannableName
	 *            name of dof to be removed
	 */
	public void removeDofName(String scannableName) {
		for (int i = 0; i < dofNameCombo.getItemCount(); i++)
			if (dofNameCombo.getItemAt(i).equals(scannableName))
				dofNameCombo.removeItemAt(i);
	}

	/**
	 * add named dof to list available for selection
	 * 
	 * @param scannableName
	 *            name of dof to be added
	 */
	public void addDofName(String scannableName) {
		dofNameCombo.addItem(scannableName);
	}

	/**
	 * determine if this scan is a timescan (detector scan)
	 * 
	 * @return boolean true for timeScan and false for OE scan
	 */
	public boolean thisIsATimeScan() {
		return oeName.equals(TIMESCAN);
	}

	/**
	 * get name of scan in Jython String as returned by Server
	 * 
	 * @return name of scan in Jython String
	 */
	public String getScanName() {
		return thisIsATimeScan() ? "timeScan" : "stepScan";
	}

}