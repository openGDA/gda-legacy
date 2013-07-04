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

package gda.configuration.object.gui;

import gda.configuration.object.GenericObjectConfigDataElement;
import gda.configuration.object.ObjectConfig;
import gda.configuration.object.xml.XMLObjectConfig;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

// README - FUTURE WORK - Store object type + name in tree instead of reference
// to GenericObjectConfigDataElement's
/**
 * GDAObjectConfigTool Class
 */
public class GDAObjectConfigTool {
	// README - variables used by inner classes made package, instead of
	// private
	// for so dont need to use synthetic accessor methods
	private static GDAObjectConfigTool thisInstance = null;

	/* private */static DefaultTreeModel treeModel = null;

	/* private */static ObjectConfig objectConfigurator = null;

	/* private */static String[] xmlExt = { ".xml" };

	/* private */static String xmlExtDesc = "XML instance files";

	/* private */JFrame frame = null;

	/* private */JTree tree = null;

	private JButton buttonLoad = null;

	private JButton buttonSave = null;

	private JButton buttonCreateObject = null;

	private JButton buttonDeleteObject = null;

	// private JTable rightPane = null;
	private JPanel rightPane = null;

	// load button lets user select an XML instance file
	// which is loaded in (via Castor) and displayed in the object tree pane
	private void addLoadButtonListener() {
		buttonLoad.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				ObjectFileChooser ofc = new ObjectFileChooser(frame, xmlExt, xmlExtDesc);
				String instanceFileName = ofc.chooseFileName();

				if (instanceFileName != null) {
					objectConfigurator.loadObjectModel(instanceFileName);

					if (objectConfigurator.getObjectModelRoot() != null) {
						treeModel = objectConfigurator.buildGUITreeModel();
						tree.setModel(treeModel);
					}
				}
			}
		});
	}

	// save button lets user select an XML instance file name.
	// object model (currently displayed in tree pane) is saved out (via
	// Castor).
	private void addSaveButtonListener() {
		// README - FUTURE WORK - implement save button functionality
		/*
		 * buttonSave.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent ae) { } });
		 */
	}

	// README - FUTURE WORK - add move object up/down functionality (within
	// object type group) - right click or gui button
	// README - FUTURE WORK - add/delete field functionality - right click
	// or gui
	// button

	private void addCreateButtonListener() {
		buttonCreateObject.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				// README - FUTURE WORK - add only available objects below
				// selected
				// item in tree
				String[] objectTypes = objectConfigurator.getAvailableObjectTypesList();
				String name = "";

				name = (String) JOptionPane.showInputDialog(frame, "Please select object type", "Create Object",
						JOptionPane.QUESTION_MESSAGE, null, objectTypes, objectTypes[0]);

				if (name == null) {
					return;
				}

				// add new object under root of datamodel
				// GenericObjectConfigDataElement e =
				objectConfigurator.createObject(objectConfigurator.getObjectModelRoot(), name);

				// regenerate tree model
				treeModel = objectConfigurator.buildGUITreeModel();
				tree.setModel(treeModel);
			}
		});
	}

	private void addDeleteButtonListener() {
		// README - FUTURE WORK - disallow delete on ObjectFactory
		buttonDeleteObject.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();

				if (node == null)
					return;

				Object nodeInfo = node.getUserObject();

				GenericObjectConfigDataElement el = (GenericObjectConfigDataElement) nodeInfo;

				// only allow deletion of root level objects
				// so start at root, and if not found at root level, wont get
				// deleted
				objectConfigurator.deleteObject(objectConfigurator.getObjectModelRoot(), el);

				// regenerate tree model
				treeModel = objectConfigurator.buildGUITreeModel();
				tree.setModel(treeModel);
			}
		});
	}

	// when user clicks on node, display its info in right hand pane
	private void addObjectTreeSelectionListener() {
		tree.addTreeSelectionListener(new GDATreeSelectionListener(tree, rightPane, objectConfigurator));
	}

	private void initGUI() {
		frame = new JFrame("GDA Configuration Tool");
		frame.setSize(800, 600);
		frame.setLocation(100, 100);

		frame.getContentPane().setLayout(new FlowLayout());

		// close application on closing frame
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent ev) {
				System.exit(0);
			}
		});

		// Load and Save buttons
		buttonLoad = new JButton("Load Instance File");
		buttonSave = new JButton("Save Instance File");
		// README - FUTURE WORK - add validate button/functionality?
		// JButton buttonValidate = new JButton("Validate Instance File");

		// handlers for loading & saving instance file
		addLoadButtonListener();
		addSaveButtonListener();

		// Create and Delete Object buttons
		buttonCreateObject = new JButton("Create Object");
		buttonDeleteObject = new JButton("Delete Object");

		// handlers for create & delete objects
		addCreateButtonListener();
		addDeleteButtonListener();

		// Quit button exits application
		JButton buttonQuit = new JButton("Quit");

		buttonQuit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				System.exit(0);
			}
		});

		// Left Hand pane Castor object tree
		tree = new JTree(treeModel);

		// Create scroll pane to put tree into
		JScrollPane sp = new JScrollPane();
		sp.setPreferredSize(new Dimension(300, 500));
		sp.getViewport().add(tree);

		// Right hand object-attributes editing pane
		rightPane = new JPanel();
		rightPane.setLayout(new GridBagLayout());
		rightPane.setPreferredSize(new Dimension(450, 500));

		// Put left and right panes into splitter
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sp, rightPane);
		// splitPane.setMinimumSize(new Dimension(300, 200));
		// splitPane.setDividerLocation(0.2);

		// put everything into frame
		frame.getContentPane().add(splitPane);
		frame.getContentPane().add(buttonLoad);
		frame.getContentPane().add(buttonSave);
		// frame.getContentPane().add(buttonValidate);
		frame.getContentPane().add(buttonCreateObject);
		frame.getContentPane().add(buttonDeleteObject);
		frame.getContentPane().add(buttonQuit);

		// when user clicks on tree object,
		// its attribute data appears in right-hand pane
		addObjectTreeSelectionListener();

		// display frame
		frame.setVisible(true);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		thisInstance = new GDAObjectConfigTool();
		/*
		 * MappingToSchemaUpdater.updateMappingElementsIntoExistingSchema(
		 * "D:\\GDA\\gda-trunk\\src\\gda\\factory\\mapping.xml", "C:\\Documents and
		 * Settings\\msd43.DL\\Desktop\\gda_schema4.xsd" );
		 */
		/*
		 * MappingFileToCSV.dumpMappingFileDataToCSV( "D:\\GDA\\gda-trunk\\src\\gda\\factory\\mapping.xml",
		 * "C:\\Documents and Settings\\msd43.DL\\Desktop\\mapping.csv"
		 * //"D:\\GDA\\dev\\src\\java\\gda\\factory\\mapping.csv" );
		 */

		objectConfigurator = new XMLObjectConfig();

		objectConfigurator.loadSchema(
		// "C:\\Documents and Settings\\msd43.DL\\Desktop\\gda_schema4.xsd"
				"D:\\GDA\\gda-trunk\\src\\gda\\configuration\\object\\GDASchema.xsd");

		// auto-load a test instance file - for quicker debugging
		objectConfigurator.loadObjectModel(
		// "C:\\Documents and Settings\\msd43.DL\\Desktop\\testfactory.xml"
				// "D:\\GDA\\dev\\params\\xml\\stn7_6_Factory.xml"
				"C:\\Documents and Settings\\msd43.DL\\Desktop\\test3.xml");

		if (objectConfigurator.getObjectModelRoot() != null) {
			treeModel = objectConfigurator.buildGUITreeModel();
		}

		thisInstance.initGUI();

		// README - FUTURE WORK - force user to pick an instance XML file to
		// load
		/*
		 * ObjectFileChooser ofc = new ObjectFileChooser(frame, xmlExt, xmlExtDesc); String instanceFileName =
		 * ofc.chooseFileName(); if(instanceFileName != null) { objectConfigurator.loadObjectModel(instanceFileName);
		 * if(objectConfigurator.getObjectModelRoot() != null) { treeModel = objectConfigurator.buildGUITreeModel();
		 * thisInstance.tree.setModel(treeModel); } }
		 */

	}
}
