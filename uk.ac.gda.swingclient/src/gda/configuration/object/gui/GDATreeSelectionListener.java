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
import gda.configuration.object.ObjectAttributeMetaData;
import gda.configuration.object.ObjectConfig;

import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * GDATreeSelectionListener Class
 */
public class GDATreeSelectionListener implements TreeSelectionListener {
	private JTree tree = null;

	// private JTable rightPane = null;
	private JPanel rightPane = null;

	private ObjectConfig objectConfigurator = null;

	GDATreeSelectionListener(JTree _tree, JPanel _rightPane, ObjectConfig _objectConfigurator) {
		tree = _tree;
		rightPane = _rightPane;
		objectConfigurator = _objectConfigurator;
	}

	class TextFieldListener implements ActionListener, FocusListener {
		JTextField j;

		GenericObjectConfigDataElement node = null;

		TextFieldListener(JTextField j, GenericObjectConfigDataElement node) {
			this.j = j;
			this.node = node;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			node.setText(j.getText());
		}

		@Override
		public void focusGained(FocusEvent e) {
			// README - FUTURE WORK - Do something here?
		}

		@Override
		public void focusLost(FocusEvent e) {
			node.setText(j.getText());
		}
	}

	private void addAttributeToPane(GenericObjectConfigDataElement node, int textFieldWidth, String value,
			GridBagConstraints c) {
		JLabel j = new JLabel(node.getName());
		JTextField t = new JTextField(textFieldWidth);
		t.addActionListener(new TextFieldListener(t, node));
		t.addFocusListener(new TextFieldListener(t, node));
		t.setText(value);

		// t.setHorizontalAlignment(JTextField.CENTER);
		// t.setEditable(false);
		// t.setBackground(Color.WHITE);

		rightPane.add(j, c);
		rightPane.add(t, c);
	}

	// N.B. takes dot-separated path name string to fetch metadata from
	// schema
	private void addAttributeMetaData(String pathName, GridBagConstraints c) {
		// fetch type info etc for this element from schema
		ObjectAttributeMetaData metaData = objectConfigurator.getObjectAttributeMetaData(pathName);
		String s = null;
		JLabel mds = null;

		if (metaData != null) {
			// README - FUTURE WORK - in GDATreeSelectionListener, build up
			// popup
			// textpanel, containing
			// metadata for all fields fetched for current object
			// README - FUTURE WORK - use metadata to validate value - and
			// display
			// metadata in right pane?
			// s = (String) metaData;
			s = // metaData.getName() +
			metaData.getType() + "," + metaData.getMinOccurs() + "," + metaData.getMaxOccurs() + ","
					+ metaData.getDefaultValue();
			mds = new JLabel(s);
			rightPane.add(mds, c);
		}
	}

	// Display config data for selected node in right-hand panel
	@Override
	public void valueChanged(TreeSelectionEvent e) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();

		if (node == null)
			return;

		Object nodeInfo = node.getUserObject();

		// ditch any previous components
		rightPane.removeAll();

		// use this to layout components properly
		GridBagConstraints c = new GridBagConstraints();

		// c.fill = GridBagConstraints.BOTH;
		// c.gridx = 20;
		// c.gridy = 0;
		// c.weightx = 0.5;
		// c.gridx = GridBagConstraints.RELATIVE;
		// c.gridwidth = 2;
		c.anchor = GridBagConstraints.WEST;

		// -------------------------------------------------------------------
		// README - FUTURE WORK - store node name+value in Tree nodes, instead
		// of
		// GenericObjectConfigDataElement
		// => need to make GenericObjectConfigDataElement tree global
		// and fetch data from that tree, instead of GUI tree
		GenericObjectConfigDataElement el = (GenericObjectConfigDataElement) nodeInfo;

		addAttributeToPane(el, 5, "", c);

		// README - FUTURE WORK - CLICK ON LEAF SIMPLE OR COMPLEX NODES DOESNT
		// DISPLAY METADATA TYPE PROPERLY
		// README - FUTURE WORK - CLICK ON DOFS POSITIONERS DOESNT FETCH
		// BASE-TYPE
		// METADATA YET
		// README - FUTURE WORK - select ObjectFactory in tree by default

		// fetch type info etc for this element from schema
		addAttributeMetaData(el.getName(), c);

		c.gridy++;
		c.gridy++;

		// handle GenericOE's especially
		// README - FUTURE WORK - get rid of specialization for GenericOE??
		if (el.getName().equalsIgnoreCase("GenericOE")) {
			GenericObjectConfigDataElement child = el.getChildren().get(0);

			addAttributeToPane(child, 5, child.getText(), c);

			c.gridy++;

			for (int i = 1; i < el.getChildren().size(); i++) {
				GenericObjectConfigDataElement childi = el.getChildren().get(i);

				String value = "";
				if (childi.getChildren() != null && childi.getChildren().size() > 0) {
					GenericObjectConfigDataElement c0 = childi.getChildren().get(0);

					value = c0.getText();
				}

				addAttributeToPane(childi, 5, value, c);

				// fetch type info etc for this element from schema
				addAttributeMetaData(el.getName() + "." + childi.getName(), c);

				c.gridy++;
			}
		} else {
			// display all info for children
			for (int i = 0; i < el.getChildren().size(); i++) {
				GenericObjectConfigDataElement childi = el.getChildren().get(i);

				addAttributeToPane(childi, 5, childi.getText(), c);

				// fetch type info etc for this element from schema
				addAttributeMetaData(el.getName() + "." + childi.getName(), c);

				c.gridy++;
			}
		}
		// -------------------------------------------------------------------

		rightPane.revalidate();
		rightPane.repaint();
	}
}
