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

package gda.gui.oemove;

import gda.configuration.properties.LocalProperties;
import gda.util.TitleBorder;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.SwingConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RepresentationMenuBar is responsible for populating a JMenu. It displays the menu in various forms; either as a
 * single drop down menu with submenu for the differing views; or as a series of thumbnail pictures; or a series of
 * buttons. On selection the desktopPanel displays the appropriate view.
 */
public class RepresentationMenuBar extends JMenuBar {
	private static final Logger logger = LoggerFactory.getLogger(RepresentationMenuBar.class);

	private JMenu menu = new JMenu("Configured representations");

	private JMenu subMenu;

	private JMenuItem menuItem;

	private JButton button;

	private ImageIcon icon = null;

	private DesktopPanel desktopPanel;

	/**
	 * @param desktopPanel
	 * @param factory
	 */
	public RepresentationMenuBar(DesktopPanel desktopPanel, RepresentationFactory factory) {
		this.desktopPanel = desktopPanel;

		boolean tabbedView = LocalProperties.check("gda.gui.oemove.pictorialView", false);
		boolean singleMenu = LocalProperties.check("gda.gui.oemove.singleMenu", false);

		if (!singleMenu) {
			setLayout(new ModifiedFlowLayout(FlowLayout.CENTER, 0, 5));
		}

		if (factory.getRepresentationList().isEmpty()) {
			logger.error("No representations available");
		} else {
			for (Representation representation : factory.getRepresentationList()) {
				// a single drop down menu with submenus
				if (singleMenu) {
					subMenu = new JMenu(representation.getName());
					menu.add(subMenu);
					menu.addSeparator();
					for (Representation childRepresentation : representation.getRepresentationList()) {
						menuItem = new JMenuItem(childRepresentation.getName());
						menuItem.addActionListener(new MenuItemActionAdapter(representation, childRepresentation));
						subMenu.add(menuItem);
					}
					add(menu);
				}
				// a pictorial representation of the beamline with a tabbed view
				// of
				// representations
				else if (tabbedView) {
					URL url;
					String thumbNail = representation.getThumbNail();
					if (thumbNail != null && !thumbNail.equals("") && (url = getClass().getResource(thumbNail)) != null) {
						icon = new ImageIcon(url);
						button = new JButton(icon);
						Insets insets = new Insets(0, 0, 0, 0);
						button.setMargin(insets);
					} else {
						button = new JButton(representation.getName());
					}
					button.addActionListener(new ButtonActionAdapter(representation));
					add(button);

				}
				// Construct a regular menu with submenu items. Each
				// representation
				// in an individual frame.
				else {
					subMenu = new JMenu(representation.getName());
					subMenu.setHorizontalTextPosition(SwingConstants.LEFT);
					subMenu.setBorder(BorderFactory.createRaisedBevelBorder());
					ArrayList<Representation> children = representation.getRepresentationList();
					if (children != null) {
						for (Representation childRepresentation : children) {
							menuItem = new JMenuItem(childRepresentation.getName());
							menuItem.addActionListener(new MenuItemActionAdapter(representation, childRepresentation));
							subMenu.add(menuItem);
						}
					} else {
						menuItem = new JMenuItem(representation.getName());
						menuItem.addActionListener(new ButtonActionAdapter(representation));
						subMenu.add(menuItem);
					}
					add(subMenu);

				}
			}

			setBorder(new TitleBorder("Representation of Beamline Elements"));

		}
	}

	private class ButtonActionAdapter implements ActionListener {
		private Representation representation;

		/**
		 * @param representation
		 */
		public ButtonActionAdapter(Representation representation) {
			this.representation = representation;
		}

		@Override
		public void actionPerformed(ActionEvent ev) {
			desktopPanel.display(representation);
		}

	}

	private class MenuItemActionAdapter implements ActionListener {
		private Representation representation;

		private Representation childRepresentation;

		/**
		 * @param representation
		 * @param childRepresentation
		 */
		public MenuItemActionAdapter(Representation representation, Representation childRepresentation) {
			this.representation = representation;
			this.childRepresentation = childRepresentation;
		}

		@Override
		public void actionPerformed(ActionEvent ev) {
			desktopPanel.display(representation, childRepresentation);
		}
	}

	/**
	 * A modified version of FlowLayout that allows containers using this Layout to behave in a reasonable manner when
	 * placed inside a JScrollPane Slightly modified from a version found in a news group.
	 */

	private class ModifiedFlowLayout extends FlowLayout {

		public ModifiedFlowLayout(int align, int hgap, int vgap) {
			super(align, hgap, vgap);
		}

		@Override
		public Dimension minimumLayoutSize(Container target) {
			return computeSize(target, false);
		}

		@Override
		public Dimension preferredLayoutSize(Container target) {
			return computeSize(target, true);
		}

		private Dimension computeSize(Container target, boolean minimum) {
			synchronized (target.getTreeLock()) {
				int hgap = getHgap();
				int vgap = getVgap();
				int w = target.getWidth();

				// If no width assigned yet then use the width of the screen
				if (w == 0)
					w = (int) Toolkit.getDefaultToolkit().getScreenSize().getWidth();

				Insets insets = target.getInsets();
				if (insets == null)
					insets = new Insets(0, 0, 0, 0);
				int reqdWidth = 0;

				int maxwidth = w - (insets.left + insets.right + hgap * 2);
				int n = target.getComponentCount();
				int x = 0;
				int y = insets.top;
				int rowHeight = 0;

				for (int i = 0; i < n; i++) {
					Component c = target.getComponent(i);
					if (c.isVisible()) {
						Dimension d = minimum ? c.getMinimumSize() : c.getPreferredSize();
						// If adding this component to this row will not exceed
						// the
						// maximum width then do that.
						if ((x == 0) || ((x + hgap + d.width) <= maxwidth)) {
							if (x > 0) {
								x += hgap;
							}
							x += d.width;
							rowHeight = Math.max(rowHeight, d.height);
						}
						// Otherwise start a new row.
						else {
							x = d.width;
							y += vgap + rowHeight;
							rowHeight = d.height;
						}
						reqdWidth = Math.max(reqdWidth, x);
					}
				}
				y += rowHeight;
				return new Dimension(reqdWidth + insets.left + insets.right, y + insets.top + insets.bottom);
			}
		}
	}

}
