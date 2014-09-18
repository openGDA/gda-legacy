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

package gda.gui.dv;

import gda.analysis.ScanFileHolder;
import gda.data.PathConstructor;
import gda.gui.AcquisitionPanel;
import gda.observable.IObserver;

import java.awt.BorderLayout;
import java.awt.ScrollPane;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;

import org.eclipse.dawnsci.analysis.api.io.ScanFileHolderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.io.SRSLoader;

/**
 * This panel is designed to give the users and beamline scientists a full view of all the data collected on the
 * beamline
 */
public class DataViewPanel extends AcquisitionPanel implements TreeSelectionListener, KeyListener, MouseListener,
		ActionListener, IObserver {
	
	private static final Logger logger = LoggerFactory.getLogger(DataViewPanel.class);

	// Items in the Panel
	JPanel leftPanel;

	JTree tree;

	JPanel rightPanel;

	JSplitPane splitter;

	JPopupMenu popup;

	JTable table;

	DefaultTableModel model;

	JButton but;

	DataVectorPlot dvp;

	/**
	 * The constructor for the class
	 */
	public DataViewPanel() {
		super();

		createLayout();

	}

	/**
	 * Function to create the layout of the panel, to be used in general by the constructor
	 */
	private void createLayout() {

		this.setLayout(new BorderLayout());

		leftPanel = new JPanel();

		DefaultMutableTreeNode top = new DefaultMutableTreeNode("data");
		DefaultMutableTreeNode mid;

		model = new DefaultTableModel(1, 4);

		table = new JTable(model) {
			@Override
			public Class<?> getColumnClass(int column) {
				if ((column == 3) || (column == 2)) {
					Boolean bool = new Boolean(true);
					return bool.getClass();
				}
				String s = "";
				return s.getClass();
			}
		};

		File dir = new File(PathConstructor.createFromDefaultProperty());

		String[] list = dir.list();

		for (String s : list) {
			mid = new DefaultMutableTreeNode(s);
			top.add(mid);

			// Object[] addin = new Object[4];

			// addin[0] = s;
			// addin[1] = "";
			// addin[2] = null;
			// addin[3] = null;

			// model.addRow(addin);

		}

		table.addMouseListener(this);

		ScrollPane scroll = new ScrollPane();

		tree = new JTree(top);

		tree.addTreeSelectionListener(this);

		scroll.add(tree);

		// leftPanel.add(scroll);

		rightPanel = new JPanel();

		// leftPanel.setBackground(Color.BLACK);
		// rightPanel.setBackground(Color.GREEN);

		but = new JButton("press me");
		but.addActionListener(this);

		// rightPanel.add(but);

		dvp = new DataVectorPlot();
		rightPanel.add(dvp);

		splitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scroll, rightPanel);

		// this.add(scroll, BorderLayout.LINE_START);

		this.add(splitter, BorderLayout.CENTER);

	}

	@Override
	public void valueChanged(TreeSelectionEvent e) {

//		DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();


		/*
		 * if(node.toString().endsWith(".dat") && (node.getChildCount() == 0)) { // load in the SRS File ScanFileHolder
		 * data = new ScanFileHolder(); data.loadSRS(PathConstructor.createFromDefaultProperty()+"/"+node.toString());
		 * DefaultMutableTreeNode leaf; String[] headers = data.getHeadings(); for(int i = 0; i < headers.length; i++) {
		 * leaf = new DefaultMutableTreeNode(headers[i]); node.add(leaf); } } else { }
		 */

	}

	@Override
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub

		if (e.getKeyChar() == 'x') {

			DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();

			System.out.println(node.getParent().toString() + "/" + node + " is set as x");

		}

		if (e.getKeyChar() == 'y') {

			DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();

			System.out.println(node.getParent().toString() + "/" + node + " is set to y");

		}

	}

	@Override
	public void mouseClicked(MouseEvent e) {

		int column = table.getSelectedColumn();
		int row = table.getSelectedRow();

		Object obj = table.getValueAt(row, column);

		try {
			String s = (String) obj;

			// DefaultMutableTreeNode node = (DefaultMutableTreeNode)
			// tree.getLastSelectedPathComponent();

			if (s.endsWith(".dat")) {

				// load in the SRS File
				ScanFileHolder data = new ScanFileHolder();
				data.load(new SRSLoader(PathConstructor.createFromDefaultProperty() + "/" + s));

				String[] headers = data.getHeadings();
				for (int i = headers.length - 1; i >= 0; i--) {

					Object[] addin = new Object[4];

					addin[0] = "";
					addin[1] = headers[i];
					addin[2] = true;
					addin[3] = true;

					model.insertRow(row + 1, addin);
				}
			}

		} catch (ScanFileHolderException err) {
			// do nothing
		} catch (Exception err) {
		// do nothing
		}

	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void update(Object dataSource, Object dataPoint) {
		// // TODO Auto-generated method stub
		// if (dataPoint instanceof PlotPackage) {
		// if (((PlotPackage) dataPoint).getPlotPanelName().equals(
		// this.getName())) {
		// ((PlotPackage) dataPoint).plot(dvp);
		// }
		// }
	}

}
