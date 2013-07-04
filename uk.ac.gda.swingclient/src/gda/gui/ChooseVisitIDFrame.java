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

package gda.gui;

import gda.configuration.properties.LocalProperties;
import gda.data.metadata.GDAMetadataProvider;
import gda.data.metadata.Metadata;
import gda.data.metadata.VisitEntry;
import gda.data.metadata.icat.IcatProvider;
import gda.jython.JythonServerFacade;
import gda.jython.authenticator.UserAuthentication;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Popup panel used during client startup when there are multiple valid visit IDs for the user on the beamline for this
 * moment in time. The user should use this popup to choose which visit ID to collect data under.
 * <p>
 * If there is only one choice then this value will be stored and the panel will not show itself. If there is a problem
 * then the defVisit value from the metadata system (or, failing that, the java properties) will be used.
 * <p>
 * If the user is a member of facility staff (listed in the beamlinestaff.xml file) then the user will always have the
 * option of the defVisit and the current visit as well as any relevant visit IDs of their own.
 */
public class ChooseVisitIDFrame extends JFrame {

	private static final Logger logger = LoggerFactory.getLogger(ChooseVisitIDFrame.class);
	private static ChooseVisitIDFrame theInstance;
	private static String chosenVisitID = "";

	private JLabel jLabel1;
	private JPanel jPanel2;
	private JPanel jPanel1;
	private JButton btnCancel;
	private JButton btnDone;
	private JPanel pnlButtons;
	private JTable tblVisits;

	/**
	 * @return the singleton instance of this class
	 */
	public static ChooseVisitIDFrame getInstance() {
		if (theInstance == null) {
			theInstance = new ChooseVisitIDFrame();
			theInstance.setVisible(false);
		}
		return theInstance;
	}

	/**
	 * @return the visit ID chosen by the user. This should be the visit which the beamline works under whenever this
	 *         user's client has the beamline baton.
	 */
	public static String getChosenVisitID() {
		return chosenVisitID;
	}

	@Override
	public void setVisible(boolean visible) {
		if (visible) {
			chosenVisitID = null;
			populateTable();
			// only show if populateTable couldn't work out for itself what the visitID should be but did populate 
			// the table
			if (chosenVisitID == null && tblVisits.getModel().getRowCount() > 0) {
				super.setVisible(true);
			}
		} else {
			super.setVisible(false);
		}
	}

	private ChooseVisitIDFrame() {
		super("Choose Visit ID");
		initialize();
	}

	private void initialize() {
		getContentPane().add(getJPanel2(), BorderLayout.NORTH);
		getContentPane().add(getPnlButtons(), BorderLayout.SOUTH);

		// explicitly add header as table not in a scrollable panel
		getContentPane().add(getJPanel1(), BorderLayout.CENTER);

		{
			this.setSize(630, 336);
		}
	}

	private JTable getTblVisits() {
		if (tblVisits == null) {
			VisitsTableModel jTable1Model = new VisitsTableModel();
			tblVisits = new JTable();
			tblVisits.setModel(jTable1Model);
			tblVisits.setAutoCreateRowSorter(true);
			tblVisits.setColumnSelectionAllowed(false);
			tblVisits.setRowSelectionAllowed(true);

			tblVisits.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
			tblVisits.getColumnModel().getColumn(0).setPreferredWidth(100);
			tblVisits.getColumnModel().getColumn(0).setMaxWidth(100);
			tblVisits.getColumnModel().getColumn(0).setMinWidth(100);
			tblVisits.getColumnModel().getColumn(1).setPreferredWidth(120);
			tblVisits.getColumnModel().getColumn(1).setMaxWidth(120);
			tblVisits.getColumnModel().getColumn(1).setMinWidth(120);
		}
		return tblVisits;
	}

	private void populateTable() {

		// if no metadata then use default in java properties or none
		Metadata metadata = GDAMetadataProvider.getInstance();
		if (metadata == null) {
			storeVisitIDInServer(LocalProperties.get("gda.defVisit", ""));
			btnCancelActionPerformed();
			return;
		}

		// if no icat then use default in java properties or none
		try {
			if (!IcatProvider.getInstance().icatInUse()) {
				storeVisitIDInServer(LocalProperties.get("gda.defVisit", ""));
				btnCancelActionPerformed();
			} else {

				// test if the result has multiple entries
				String user = UserAuthentication.getUsername();
				VisitEntry[] visits = IcatProvider.getInstance().getMyValidVisits(user);

				// if no valid visit ID then do same as the cancel button
				if (visits == null || visits.length == 0) {
					btnCancelActionPerformed();
				} else if (visits.length == 1){
					storeVisitIDInServer(visits[0].getVisitID());
					btnCancelActionPerformed();
				} else {
					for (int i = 0; i < visits.length; i++) {
						((VisitsTableModel) tblVisits.getModel()).addRow();
						tblVisits.getModel().setValueAt(visits[i].getVisitID(), i, 0);
						tblVisits.getModel().setValueAt(visits[i].getTitle(), i, 1);
					}
				}
			}
		} catch (Exception e) {
			storeVisitIDInServer(LocalProperties.get("gda.defVisit", ""));
			logger.error("Exception while populating ChooseVisitID popup panel. Error was: " + e.getMessage()
					+ ". Will use visit ID: " + chosenVisitID);
			btnCancelActionPerformed();
		}

	}

	private JPanel getPnlButtons() {
		if (pnlButtons == null) {
			pnlButtons = new JPanel();
			pnlButtons.add(getBtnDone());
			pnlButtons.add(getBtnCancel());
		}
		return pnlButtons;
	}

	private JButton getBtnDone() {
		if (btnDone == null) {
			btnDone = new JButton();
			btnDone.setText("Use visit");
			btnDone.setToolTipText("Select the highlighted visit ID to collect data");
			btnDone.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent evt) {
					btnDoneActionPerformed();
				}
			});
		}
		return btnDone;
	}

	private JButton getBtnCancel() {
		if (btnCancel == null) {
			btnCancel = new JButton();
			btnCancel.setText("Cancel");
			btnCancel.setToolTipText("Make no choice and do not start GDA");
			btnCancel.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent evt) {
					btnCancelActionPerformed();
				}
			});
		}
		return btnCancel;
	}

	private void btnDoneActionPerformed() {

		if (getTblVisits().getSelectedRowCount() != 1) {
			return;
		}

		int row = getTblVisits().getSelectedRow();
		storeVisitIDInServer(getTblVisits().getModel().getValueAt(row, 0).toString());
		this.setVisible(false);
	}

	private void btnCancelActionPerformed() {
		this.setVisible(false);
	}
	
	private static void storeVisitIDInServer(String chosenVisitID){
		ChooseVisitIDFrame.chosenVisitID = chosenVisitID;
		JythonServerFacade.getInstance().changeVisitID(chosenVisitID);
	}

	private JPanel getJPanel1() {
		if (jPanel1 == null) {
			jPanel1 = new JPanel();
			BorderLayout jPanel1Layout = new BorderLayout();
			jPanel1.setLayout(jPanel1Layout);
			{
				JTableHeader header = getTblVisits().getTableHeader();
				jPanel1.add(getTblVisits(), BorderLayout.CENTER);
				jPanel1.add(header, BorderLayout.NORTH);
			}
		}
		return jPanel1;
	}

	private JPanel getJPanel2() {
		if (jPanel2 == null) {
			jPanel2 = new JPanel();
			BorderLayout jPanel2Layout = new BorderLayout();
			jPanel2.setLayout(jPanel2Layout);
			jPanel2.add(getJLabel1(), BorderLayout.CENTER);
		}
		return jPanel2;
	}

	private JLabel getJLabel1() {
		if (jLabel1 == null) {
			jLabel1 = new JLabel();
			jLabel1.setText("You have multiple valid visit IDs. Select which one to collect data under:");
		}
		return jLabel1;
	}

	private class VisitsTableModel extends AbstractTableModel {

		private Vector<VisitEntry> data = new Vector<VisitEntry>(1);

		/**
		 * Constructor.
		 */
		public VisitsTableModel() {
			super();
		}

		@Override
		public boolean isCellEditable(int row, int col) {
			return false;
		}

		@Override
		public String getColumnName(int i) {
			if (i == 0) {
				return "Visit ID";
			}
			if (i == 1) {
				return "Proposal Number";
			}
			return "Title";

		}

		@Override
		public int getColumnCount() {
			return 3;
		}

		@Override
		public int getRowCount() {
			if (data == null) {
				return 0;
			}
			return data.size();
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			if (columnIndex > 2 || rowIndex > data.size()) {
				return null;
			}

			VisitEntry row = data.get(rowIndex);

			if (columnIndex == 0) {
				return row.getVisitID();
			}
			return row.getTitle();
		}

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {

			if (rowIndex < getRowCount()) {

				VisitEntry row = data.get(rowIndex);

				try {
					if (columnIndex == 0 && aValue instanceof String) {
						row.setVisitID(((String) aValue).trim());
					} else if (columnIndex == 1 && aValue instanceof String) {
						row.setTitle(((String) aValue));
					}
					data.set(rowIndex, row);
				} catch (Exception e) {
					final String msg = "Invalid proposal string";
					logger.error(msg, e);
					JOptionPane.showMessageDialog(ChooseVisitIDFrame.this, msg);
				}
			}

		}

		/**
		 * Add an extra row
		 */
		public void addRow() {
			data.add(new VisitEntry("",""));
		}
	}
}
