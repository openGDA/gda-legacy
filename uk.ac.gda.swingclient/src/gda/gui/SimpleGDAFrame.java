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

package gda.gui;

import gda.configuration.properties.LocalProperties;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.LayoutFocusTraversalPolicy;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import com.jidesoft.docking.DefaultDockingManager;
import com.jidesoft.docking.DockableHolder;
import com.jidesoft.docking.DockingManager;
import com.jidesoft.icons.JideIconsFactory;
import com.jidesoft.swing.JideTabbedPane;

/**
 * SimpleGDAFrame Class
 */
public class SimpleGDAFrame extends JFrame implements DockableHolder {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3982064646693182714L;

	private final DockingManager _dockingManager;

	private final JPanel displayPanel;

	private final JPanel pane;

	@Override
	public DockingManager getDockingManager() {
		return _dockingManager;
	}

	/**
	 * @param name
	 * @param panels
	 */
	@SuppressWarnings("rawtypes")
	public SimpleGDAFrame(String name, java.util.Vector<JPanel> panels) {
		super(name);
		pane = new JPanel();
		pane.setFocusTraversalKeysEnabled(true);
		displayPanel = new JPanel(new BorderLayout());
		/*
		 * run App with VM arguments: -Djdock.license.company.name="Diamond Light Source Ltd. and CCLRC"
		 * -Djdock.license.project.name=GDA -Djdock.license.key=<correct key>
		 */
		String company = LocalProperties.get("jdock.license.company.name");
		String project = LocalProperties.get("jdock.license.project.name");
		String license = LocalProperties.get("jdock.license.key");
		com.jidesoft.utils.Lm.verifyLicense(company, project, license);
		_dockingManager = new DefaultDockingManager(this, pane);
		if (_dockingManager == null)
			return;
		_dockingManager.setShowTitleBar(false);
		_dockingManager.setTabbedPaneCustomizer(new DefaultDockingManager.TabbedPaneCustomizer() {
			@Override
			public void customize(JideTabbedPane tabbedPane) {
				// tabbedPane.setShrinkTabs(false);
				tabbedPane.setTabPlacement(SwingConstants.TOP);
				tabbedPane.setHideOneTab(false);
				tabbedPane.setShowCloseButtonOnTab(true);
			}
		});
		_dockingManager.setOutlineMode(1);
		_dockingManager.setDragAllTabs(false);
		_dockingManager.setDoubleClickAction(DockingManager.DOUBLE_CLICK_TO_FLOAT);
		_dockingManager.beginLoadLayoutData();
		_dockingManager.setShowWorkspace(false);
		for (JPanel panel : panels) {
			String name1 = panel.getName() == null ? "Unknown" : panel.getName();
			_dockingManager.addFrame(AcquisitionFrame.createDockableFrame(name1, panel));
		}
		_dockingManager.loadLayoutData();
		pane.setFocusTraversalPolicy(new LayoutFocusTraversalPolicy());
		displayPanel.setBackground(new Color(204, 204, 255));
		displayPanel.add(pane, BorderLayout.CENTER);
		getContentPane().add(displayPanel, BorderLayout.CENTER);
		JMenu quitMenu = new JMenu("File");
		JMenuItem quitItem = new JMenuItem("Quit");
		quitItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				System.exit(0);
			}
		});

		quitMenu.add(quitItem);
		JMenuBar menuBar = new JMenuBar();
		menuBar.add(quitMenu);

		JMenu menu = new JMenu("View");
		menu.setMnemonic('V');

		List dockableFramesNames = _dockingManager.getAllFrameNames();
		Object[] frameNames = dockableFramesNames.toArray();
		int iframe = 0;
		for (iframe = 0; iframe < frameNames.length; iframe++) {
			final String name1 = (String) frameNames[iframe];
			JMenuItem item = new JMenuItem(name1, JideIconsFactory.getImageIcon(JideIconsFactory.DockableFrame.BLANK));
			item.addActionListener(new AbstractAction() {
				/**
				 * 
				 */
				private static final long serialVersionUID = 656783864648323234L;

				@Override
				public void actionPerformed(ActionEvent e) {
					_dockingManager.showFrame(name1);
				}
			});
			menu.add(item);
		}

		JMenuItem item;
		item = new JMenuItem("Select Next View");
		item.setMnemonic('N');
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0));
		item.addActionListener(new AbstractAction() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1479619947410332116L;

			@Override
			public void actionPerformed(ActionEvent e) {
				if (_dockingManager.getActiveFrame() == null)
					return;
				String activeFrameName = _dockingManager.getActiveFrame().getName();
				List dockableFramesNames = _dockingManager.getAllFrameNames();
				Object[] frameNames = dockableFramesNames.toArray();
				int iframe = 0;
				for (iframe = 0; iframe < frameNames.length; iframe++) {
					try {
						if (((String) frameNames[iframe]).equals(activeFrameName))
							break;
					} catch (Exception exception) {
						exception.getMessage();//
					}
				}
				iframe = (iframe >= frameNames.length - 1) ? 0 : iframe + 1;
				_dockingManager.showFrame((String) frameNames[iframe]);
			}
		});
		menu.add(item);

		item = new JMenuItem("Select Previous View");
		item.setMnemonic('P');
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F6, InputEvent.SHIFT_MASK));
		item.addActionListener(new AbstractAction() {
			/**
			 * 
			 */
			private static final long serialVersionUID = -3658320840524583749L;

			@Override
			public void actionPerformed(ActionEvent e) {
				if (_dockingManager.getActiveFrame() == null)
					return;
				String activeFrameName = _dockingManager.getActiveFrame().getName();
				List dockableFramesNames = _dockingManager.getAllFrameNames();
				Object[] frameNames = dockableFramesNames.toArray();
				int iframe = 0;
				for (iframe = 0; iframe < frameNames.length; iframe++) {
					try {
						if (((String) frameNames[iframe]).equals(activeFrameName))
							break;
					} catch (Exception exception) {
						exception.getMessage();//
					}
				}
				iframe = (iframe == 0) ? frameNames.length - 1 : iframe - 1;
				_dockingManager.showFrame((String) frameNames[iframe]);
			}
		});
		menu.add(item);
		menuBar.add(menu);

		setJMenuBar(menuBar);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// Schedule a job for the event dispatch thread:
		// creating and showing this application's GUI.
		UIManager.put("swing.boldMetal", Boolean.FALSE);

		// Create a simple dialog
		JPanel panel1 = new JPanel();
		panel1.add(new JTextField("Field1", 10));
		panel1.add(new JTextField("Field2", 10));
		panel1.setName("Panel1");

		Vector<JPanel> panels = new Vector<JPanel>();
		panels.add(panel1);
		JPanel panel2 = new JPanel();
		panel2.add(new JTextField("Field1", 10));
		panel2.add(new JTextField("Field2", 10));
		panel2.setName("Panel2");
		panels.add(panel2);
		JPanel panel3 = new JPanel();
		panel3.add(new JTextField("Field3", 10));
		panel3.add(new JTextField("Field3", 10));
		panel3.setName("Panel3");
		panels.add(panel3);
		Frame frame = new SimpleGDAFrame("Test", panels);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent ev) {
				System.exit(0);
			}
		});
		frame.setVisible(true);
	}
}
