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
import gda.factory.Findable;
import gda.factory.corba.util.EventService;
import gda.jython.InterfaceProvider;
import gda.jython.Jython;
import gda.jython.JythonServerFacade;
import gda.util.About;
import gda.util.MultiScreenSupport;
import gda.util.ObjectServer;
import gda.util.PleaseWaitWindow;
import gda.util.WindowSize;
import gda.util.exceptionUtils;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.LayoutFocusTraversalPolicy;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jidesoft.docking.DefaultDockingManager;
import com.jidesoft.docking.DockContext;
import com.jidesoft.docking.DockableFrame;
import com.jidesoft.docking.DockableHolder;
import com.jidesoft.docking.DockingManager;
import com.jidesoft.icons.JideIconsFactory;
import com.jidesoft.plaf.LookAndFeelFactory;
import com.jidesoft.swing.JideTabbedPane;

/**
 * A class which implements a tabbed pane environment so that individual experiments can be configured within.
 */
public class AcquisitionFrame extends JFrame implements DockableHolder {

	private static final Logger logger = LoggerFactory.getLogger(AcquisitionFrame.class);
	private JPanel displayPanel;
	private JSplitPane splitPane = null;

	// this is where the panels go in docking mode
	private JPanel pane = new JPanel();

	// other [people use these two
	private JTabbedPane tabbedPane = new JTabbedPane();
	private JTabbedPane messagesPane = new JTabbedPane();
	private JMenuBar menuBar = new JMenuBar();
	private ObjectServer objectServer;
	JythonServerFacade jsf;

	private boolean dock = false;
	private static DockingManager _dockingManager;
	private static Vector<String> viewerObjects;
	private About aboutBox;
	private static MultiScreenSupport mss = new MultiScreenSupport();
	private int screenIndex;
	public static AcquisitionFrame instance;

	/**
	 * @param proportionalLocation
	 */
	public void setDividerLocation(double proportionalLocation) {
		if (splitPane != null)
			splitPane.setDividerLocation(proportionalLocation);
	}

	/**
	 * Inner class implementing Runnable to make the exit confirmation code neater.
	 */
	private class Terminator implements Runnable {
		// Terminate scans and scripts, and clear queues then wait for the
		// JythonServerStatus to go to idle. Finally do the normal
		// tidying up and exiting.
		@Override
		public void run() {
			InterfaceProvider.getCommandAborter().abortCommands();
			PleaseWaitWindow pww = new PleaseWaitWindow("Stopping scans and scripts. Please wait...");
			pww.setVisible(true);
			while (scriptingIsActive())
				try {
					synchronized (this) {
						wait(100);
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				pww.setVisible(false);
				exit();
		}

	}

	/**
	 * @param name
	 *            window title
	 * @param objectServer 
	 */
	public AcquisitionFrame(String name, ObjectServer objectServer) {
		super(name);

		this.objectServer = objectServer;
		// screen index for the primary screen starting from 0.
		screenIndex = LocalProperties.getInt("gda.screen.primary", 0);

		initDocking();

		String landf = LocalProperties.get("gda.lookandfeel.class");
		if (landf != null && !landf.equals("")) {
			setLookandFeel(landf);
		} else {
			setLookandFeel("javax.swing.plaf.metal.MetalLookAndFeel");
		}

		displayPanel = new JPanel(new BorderLayout());
		if (LocalProperties.check("gda.gui.border", true)) {
			displayPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLoweredBevelBorder(),
					BorderFactory.createEmptyBorder(5, 3, 5, 3)));
		}
		instance = this;
	}

	private void initDocking() {
		// check docking licensed
		dock = LocalProperties.check("gda.gui.dock", false);
		if (!dock)
			return;

		String company = LocalProperties.get("jdock.license.company.name");
		String project = LocalProperties.get("jdock.license.project.name");
		String license = LocalProperties.get("jdock.license.key");
		if (company != null && project != null && license != null)
			com.jidesoft.utils.Lm.verifyLicense(company, project, license);
		else {
			logger.error("Licensing information for the docking is not found");
			dock = false;
			return;
		}

		_dockingManager = new DefaultDockingManager(this, pane);
		_dockingManager.setProfileKey("gda"); // where layouts are saved, in the user preferences tree
		_dockingManager.setOutlineMode(1);
		_dockingManager.setDragAllTabs(true);
		_dockingManager.setEasyTabDock(true);
		_dockingManager.setAutohidable(false);
		_dockingManager.setDoubleClickAction(DockingManager.DOUBLE_CLICK_TO_FLOAT);
		_dockingManager.setShowTitleBar(false);
		_dockingManager.setTabbedPaneCustomizer(new DefaultDockingManager.TabbedPaneCustomizer() {
			@Override
			public void customize(JideTabbedPane tabPane) {
				tabPane.setTabPlacement(SwingConstants.TOP);
				tabPane.setHideOneTab(false);
				tabPane.setShowCloseButtonOnTab(true);
			}
		});
	}

	/**
	 * Configure all gui panels
	 * 
	 * @param batonPanelOnly set to true if only the baton/messaging panel is to be displayed
	 */
	public void configure(boolean batonPanelOnly) {
		viewerObjects = new Vector<String>();
		if (dock) {
			_dockingManager.beginLoadLayoutData();
			_dockingManager.setShowWorkspace(false);
		}

		List<String> names = objectServer.getFindableNames();
		if (names != null) {
			for (String name : names) {
				try {
					Findable findable = objectServer.getFindable(name);
					
					if (!batonPanelOnly){
						addPanel(findable);
					} else if (findable instanceof BatonPanel){
						addPanel(findable);
					}
				} catch (Exception ex) {
					exceptionUtils.logException(logger, "AcquisitionFrame: configure() name " + name, ex);
				}
			}

			if (dock){
				_dockingManager.loadLayoutData();
			}

			jsf = JythonServerFacade.getInstance();
			createMenuBar();

			if (dock) {
				displayPanel.add(pane, BorderLayout.CENTER);
			} else {
				if (messagesPane.getTabCount() != 0) {
					splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tabbedPane, messagesPane);
					displayPanel.add(splitPane, BorderLayout.CENTER);
				} else {
					displayPanel.add(tabbedPane, BorderLayout.CENTER);
				}
			}
		}
		getContentPane().add(displayPanel, BorderLayout.CENTER);
		SwingUtilities.updateComponentTreeUI(this);
	}

	private void addPanel(Findable findable) {
		if (dock) {
			if (findable instanceof AcquisitionPanel) {
				AcquisitionPanel pnl = (AcquisitionPanel) findable;
				String label = pnl.getLabel();
				_dockingManager.addFrame(createDockableFrame(label, pnl));
				viewerObjects.addElement(label);
			} else if (findable instanceof GUIMessagePanel) {
				// messagesPane.addTab(name, (GUIMessagePanel)
				// findable);
				GUIMessagePanel pnl = (GUIMessagePanel)findable;
				String label = pnl.getLabel();
				_dockingManager.addFrame(createDockableFrame(label, pnl));
				viewerObjects.addElement(label);
			}
		} else {
			if (findable instanceof AcquisitionPanel) {
				AcquisitionPanel pnl = (AcquisitionPanel) findable;
				tabbedPane.addTab(pnl.getLabel(), pnl);
				((AcquisitionPanel) findable).setTabIndex(tabbedPane.getTabCount() - 1);
			} else if (findable instanceof GUIMessagePanel) {
				GUIMessagePanel pnl = (GUIMessagePanel)findable;
				messagesPane.addTab(pnl.getLabel(), pnl);
			}
		}
	}

	/**
	 * Show dialog to confirm exit from program
	 */
	public void confirmExit() {
		Toolkit.getDefaultToolkit().beep();

		// If the JythonServerFacade is not idle then offer three choices
		if (scriptingIsActive()) {
			String[] options = { "Stop scans then exit", "Exit without stopping scans", "Cancel exit" };
			int answer = JOptionPane.showOptionDialog(getContentPane(),
					"There are scans or scripts running or queued. What would you like to do?", "Confirm Exit",
					JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

			// Any running scans may take some time to stop (or indeed may
			// not stop at all) so the terminating and waiting must be done in
			// another thread (because here we are in the Java event thread and
			// if
			// we do it here the window will not get repainted properly).
			if (answer == 0) {
				uk.ac.gda.util.ThreadManager.getThread(new Terminator()).start();
			}
			// Just do the old clearing up exit and leave the ObjectServer
			// carrying on regardless
			else if (answer == 1) {
				exit();
			}
		}
		// exit support for multi-screen display
		else if (mss.getNumberOfScreens() > 1) {
			final JOptionPane optionPane = new JOptionPane("Do you really want to exit?", JOptionPane.QUESTION_MESSAGE,
					JOptionPane.YES_NO_OPTION);
			final PopupDialog pd = new PopupDialog(this, "Confirm Exit", true, optionPane);

			pd.addPropertyChangeListener(new PropertyChangeListener() {

				@Override
				public void propertyChange(PropertyChangeEvent e) {
					String prop = e.getPropertyName();

					if (isVisible() && (e.getSource() == optionPane) && (JOptionPane.VALUE_PROPERTY.equals(prop))) {
						Object value = optionPane.getValue();

						if (value == JOptionPane.UNINITIALIZED_VALUE) {
							// ignore reset
							return;
						}
						// Reset the JOptionPane's value.
						// If you don't do this, then if the user
						// presses the same button next time, no
						// property change event will be fired.
						optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);

						if (value.equals(JOptionPane.YES_OPTION)) {
							exit();
						} else {
							pd.setVisible(false);
						}
					}
				}
			});
			pd.setVisible(true);
		}
		// If the JythonServerFacade is idle then ask for confirmation of exit.
		else if (JOptionPane.showConfirmDialog(this, "Do you really want to exit?", "Confirm Exit",
				JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
			exit();
		}

	}

	private boolean scriptingIsActive() {
		return jsf.getScanStatus() != Jython.IDLE || jsf.getScriptStatus() != Jython.IDLE;
	}

	public void exit() {
		for (int i = 0; i < tabbedPane.getTabCount(); i++) {
			((Tidyable) tabbedPane.getComponentAt(i)).tidyup();
		}

		// NOTE: This shutdown code is also in gda.rcp.Application
		//
		// The RCP client calls similar shutdown code in the stop() method.
		// The Java client calls it in this method per frame.
		//
		// Note that the swing client should probably do this in a shutdown
		// hook Runtime.getRuntime().addShutdownHook(...)
		
		// if running GDA in distributed mode then unsubscribe from
		// eventservice
		if (!ObjectServer.isLocal()) {
			EventService eventService = EventService.getInstance();
			if (eventService != null) {
				eventService.unsubscribe();
			}
		}

		//tell JythonServerFacade to disconnect from JythonServer
		JythonServerFacade.disconnect();

		System.exit(0);
	}

	/**
	 * @param name
	 */
	public static void showFrame(String name) {
		showFrame(name, true);
	}
	/**
	 * @param name
	 * @param show  - true if frame is to be shown, else frame is hidden
	 */
	public static void showFrame(String name, boolean show) {
		if(_dockingManager != null){
			if( show){
				_dockingManager.showFrame(name);
			} else {
				_dockingManager.hideFrame(name);
			}
		}
	}

	private void createMenuBar() {
		if (dock) {
			JMenu quitMenu = new JMenu("File");

			// see BeamLostPopup class for what is going on
			// It is not pretty to do it here, but with RCP menu contributions will be much cleaner soon anyway
			if (LocalProperties.get("gda.gui.beammodemonitor")!=null) {

				final BeamLostPopup blp = new BeamLostPopup(this, LocalProperties.get("gda.gui.beammodemonitor"));
				final JMenuItem blpEntry = new JCheckBoxMenuItem("Monitor machine status", blp.isLive());
				blpEntry.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						blp.setLive(((AbstractButton)  e.getSource()).isSelected());
					}
				});
				// this is required to reflect the state of the blp even when silenced through the popup checkbox
				blpEntry.addPropertyChangeListener(new PropertyChangeListener() {
					@Override
					public void propertyChange(PropertyChangeEvent evt) {
						blpEntry.setSelected(blp.isLive());
					}
				});
				quitMenu.add(blpEntry);
				quitMenu.addSeparator();
			}

			JMenuItem quitItem = new JMenuItem("Quit");
			quitItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent ev) {
					confirmExit();
				}
			});

			quitMenu.add(quitItem);
			JMenu viewMenu = createViewMenu();
			menuBar.add(quitMenu);
			menuBar.add(viewMenu);
			menuBar.add(createLookandFeelMenu());
			menuBar.add(createSaveMenu());
			// only create webcam menu when there are entries
			JMenu wcm = createWebCamMenu();
			if (wcm != null)
				menuBar.add(wcm);
			menuBar.add(createHelpMenu());
			setJMenuBar(menuBar);
		}
	}

	private void setLookandFeel(String landfclassname) {
		try {
			UIManager.setLookAndFeel(landfclassname);
			if (dock)
				LookAndFeelFactory.installJideExtension(LookAndFeelFactory.VSNET_STYLE);
			SwingUtilities.updateComponentTreeUI(this);
		} catch (ClassNotFoundException e) {
			logger.error("Unable to set the Look and Feel" + e);
		} catch (InstantiationException e) {
			logger.error("Unable to set the Look and Feel" + e);
		} catch (IllegalAccessException e) {
			logger.error("Unable to set the Look and Feel" + e);
		} catch (UnsupportedLookAndFeelException e) {
			logger.error("Unable to set the Look and Feel" + e);
		}
	}

	private JMenu createLookandFeelMenu() {
		JMenuItem item;
		JMenu menu = new JMenu("Look and Feel");
		menu.setMnemonic('L');

		// for testing different look and feel setting
		final Map<String,String> landflist = new LinkedHashMap<String,String>();
		for (LookAndFeelInfo laf : UIManager.getInstalledLookAndFeels()) {
			landflist.put(laf.getName(), laf.getClassName());
		}

		for (String landf : landflist.keySet()) {
			item = new JMenuItem(landf);
			item.addActionListener(new AbstractAction() {
				@Override
				public void actionPerformed(ActionEvent e) {
					setLookandFeel(landflist.get(((JMenuItem) e.getSource()).getText()));
				}
			});
			menu.add(item);
		}
		return menu;
	}

	private JMenu createViewMenu() {
		JMenuItem item;
		JMenu menu = new JMenu("View");
		menu.setMnemonic('V');

		Collections.sort(viewerObjects);
		for (Enumeration<String> e = viewerObjects.elements(); e.hasMoreElements();) {
			final String s = e.nextElement();
			item = new JMenuItem(s, JideIconsFactory.getImageIcon(JideIconsFactory.DockableFrame.BLANK));
			item.addActionListener(new AbstractAction() {

				@Override
				public void actionPerformed(ActionEvent e) {
					_dockingManager.showFrame(s);
				}
			});
			menu.add(item);
		}
		item = new JMenuItem("Select Previous View");
		item.setMnemonic('P');
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, InputEvent.SHIFT_MASK));
		item.addActionListener(new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (_dockingManager.getActiveFrame() == null)
					return;
				String activeFrameName = _dockingManager.getActiveFrame().getName();
				List<String> dockableFramesNames = _dockingManager.getAllFrameNames();
				Object[] frameNames = dockableFramesNames.toArray();
				int iframe = 0;
				{
					Vector<String> visibleDockableFrameNames = new Vector<String>();
					for (iframe = 0; iframe < frameNames.length; iframe++) {
						if (!_dockingManager.getFrame((String) frameNames[iframe]).isHidden())
							visibleDockableFrameNames.add((String) frameNames[iframe]);
					}
					frameNames = visibleDockableFrameNames.toArray();
				}
				for (iframe = 0; iframe < frameNames.length; iframe++) {
					try {
						if (((String) frameNames[iframe]).equals(activeFrameName))
							break;
					} catch (Exception exception) {
						logger.warn(exception.getMessage());
					}
				}
				iframe = (iframe == 0) ? frameNames.length - 1 : iframe - 1;
				_dockingManager.showFrame((String) frameNames[iframe]);
			}
		});
		menu.add(item);

		item = new JMenuItem("Select Next View");
		item.setMnemonic('N');
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F6, InputEvent.SHIFT_MASK));
		item.addActionListener(new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (_dockingManager.getActiveFrame() == null)
					return;
				String activeFrameName = _dockingManager.getActiveFrame().getName();
				List<String> dockableFramesNames = _dockingManager.getAllFrameNames();
				Object[] frameNames = dockableFramesNames.toArray();
				int iframe = 0;
				{
					Vector<String> visibleDockableFrameNames = new Vector<String>();
					for (iframe = 0; iframe < frameNames.length; iframe++) {
						if (!_dockingManager.getFrame((String) frameNames[iframe]).isHidden())
							visibleDockableFrameNames.add((String) frameNames[iframe]);
					}
					frameNames = visibleDockableFrameNames.toArray();
				}
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

		return menu;

	}

	private JMenu createSaveMenu() {
		JMenuItem item;
		JMenu menu = new JMenu("Layout");

		item = new JMenuItem("Save");
		item.addActionListener(new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent e) {
				_dockingManager.saveLayoutDataAs("saved");
			}
		});
		menu.add(item);
		item = new JMenuItem("Load saved");
		item.addActionListener(new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent e) {
				_dockingManager.loadLayoutDataFrom("saved");

			}
		});
		menu.add(item);

		item = new JMenuItem("Load default");
		item.addActionListener(new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent e) {
				_dockingManager.loadLayoutDataFrom("default");
			}
		});
		menu.add(item);
		return menu;

	}

	private JMenu createWebCamMenu() {
		JMenuItem item;
		JMenu menu = new JMenu("Webcams");
		String name;
		boolean empty = true;

		for (Integer i = 0; i < 10; i++) {
			if ((name = LocalProperties.get("gda.webcam.n" + i + ".name")) != null) {
				item = new JMenuItem(name);
				if (LocalProperties.get("gda.webcam.n" + i + ".url") != null) {
					item.addActionListener(new AbstractAction(LocalProperties.get("gda.webcam.n" + i + ".url")) {
						@Override
						public void actionPerformed(ActionEvent e) {
							if (Desktop.isDesktopSupported()) {
								Desktop desktop = Desktop.getDesktop();
								if (desktop.isSupported(Desktop.Action.BROWSE)) {
									try {
										desktop.browse(new URI((String) getValue(javax.swing.Action.NAME)));
									} catch (IOException ioe) {
										ioe.printStackTrace();
									} catch (URISyntaxException use) {
										use.printStackTrace();
									}
								}
							}
						}
					});
					menu.add(item);
					empty = false;
				}
			}
		}
		if (empty)
			return null;
		return menu;
	}

	private JMenu createHelpMenu() {
		JMenuItem item;
		JMenu menu = new JMenu("Help");

		item = new JMenuItem("GDA Manual");
		if (LocalProperties.get("gda.help.manual") != null) {
			item.addActionListener(new AbstractAction() {
				@Override
				public void actionPerformed(ActionEvent e) {

					if (Desktop.isDesktopSupported()) {
						Desktop desktop = Desktop.getDesktop();
						if (desktop.isSupported(Desktop.Action.BROWSE)) {
							try {
								desktop.browse(new URI(LocalProperties.get("gda.help.manual")));
							} catch (IOException ioe) {
								ioe.printStackTrace();
							} catch (URISyntaxException use) {
								use.printStackTrace();
							}
						}
					}
				}
			});
		} else {
			item.setEnabled(false);
		}
		menu.add(item);

		item = new JMenuItem("Beamline Manual");
		if (LocalProperties.get("gda.beamline.manual") != null) {
			item.addActionListener(new AbstractAction() {
				@Override
				public void actionPerformed(ActionEvent e) {

					if (Desktop.isDesktopSupported()) {
						Desktop desktop = Desktop.getDesktop();
						if (desktop.isSupported(Desktop.Action.BROWSE)) {
							try {
								desktop.browse(new URI(LocalProperties.get("gda.beamline.manual")));
							} catch (IOException ioe) {
								ioe.printStackTrace();
							} catch (URISyntaxException use) {
								use.printStackTrace();
							}
						}
					}
				}
			});
		} else {
			item.setEnabled(false);
		}
		menu.add(item);

		item = new JMenuItem("Report a Bug");
		if (LocalProperties.get("gda.bugReport.site") != null) {
			item.addActionListener(new AbstractAction() {
				@Override
				public void actionPerformed(ActionEvent e) {

					if (Desktop.isDesktopSupported()) {
						Desktop desktop = Desktop.getDesktop();
						if (desktop.isSupported(Desktop.Action.BROWSE)) {
							try {
								desktop.browse(new URI(LocalProperties.get("gda.bugReport.site")));
							} catch (IOException ioe) {
								ioe.printStackTrace();
							} catch (URISyntaxException use) {
								use.printStackTrace();
							}
						}
					}
				}
			});
		} else {
			item.setVisible(false);
		}
		menu.add(item);

		menu.addSeparator();

		item = new JMenuItem("About GDA");
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showAboutBox();
			}
		});

		menu.add(item);

		return menu;

	}

	/**
	 * Display the about box.
	 */
	private void showAboutBox() {
		if (aboutBox == null) {
			aboutBox = new About(this);
		}
		aboutBox.setVisible(true);
	}

	/**
	 * Set location of gui on screen
	 */
	public void setPosition() {
		int xLocation = 0;
		int yLocation = 0;
		int x = 0;
		int y = 0;

		if (LocalProperties.get("gda.screen.primary") != null) {
			x = mss.getScreenXoffset(screenIndex);
			y = mss.getScreenYoffset(screenIndex);
		} else {
			x = LocalProperties.getInt("gda.gui.x", 0);
			y = LocalProperties.getInt("gda.gui.y", 0);
			int nosOfScreens = LocalProperties.getInt("gda.gui.nosOfScreens", 1);
			String displayingScreen = LocalProperties.get("gda.gui.displayingScreen", "top");
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

			if (nosOfScreens == 2) {
				if ("bottom".equalsIgnoreCase(displayingScreen)) {
					yLocation = screenSize.height / 2;
				} else if ("right".equalsIgnoreCase(displayingScreen)) {
					xLocation = screenSize.width / 2;
				}
			} else if (nosOfScreens == 4) {
				if ("bottomleft".equalsIgnoreCase(displayingScreen)) {
					yLocation = screenSize.height / 2;
				} else if ("bottomright".equalsIgnoreCase(displayingScreen)) {
					xLocation = screenSize.width / 2;
					yLocation = screenSize.height / 2;
				} else if ("topright".equalsIgnoreCase(displayingScreen)) {
					xLocation = screenSize.width / 2;
				}
			}
		}
		int posx = xLocation + x;
		int posy = yLocation + y;
		setLocation(posx, posy);
	}

	/**
	 * Set starting size of gui
	 */
	public void setSize() {
		double fraction = 0.9;
		int width = 1040, height = 920;

		if (LocalProperties.get("gda.screen.primary") != null) {
			width = mss.getScreenWidth(screenIndex);
			height = mss.getScreenHeight(screenIndex);
			setSize(width, height);
		} else {
			String property1, property2;

			// If either preferred dimension is smaller than its (arbitrary)
			// default we use the default value instead. This is because many
			// panels do not seem to have sensible preferred heights
			// (look into this someone).
			Dimension d = getPreferredSize();
			setPreferredSize(new Dimension(Math.max(d.width, width), Math.max(d.height, height)));

			if ((property1 = LocalProperties.get("gda.gui.fractionalSize")) != null) {
				try {
					fraction = Double.parseDouble(property1);
				} catch (NumberFormatException nfex) {
					logger.error("Error parsing window size for GDA AcquisitionGUI.\n" + "Default assumed " + width
							+ " " + height);
				} finally {
					setSize(new WindowSize(fraction));
				}
			} else {
				if ((property1 = LocalProperties.get("gda.gui.width")) != null
						&& (property2 = LocalProperties.get("gda.gui.height")) != null) {
					try {
						width = Integer.parseInt(property1);
						height = Integer.parseInt(property2);
					} catch (NumberFormatException nfex) {
						logger.error("Error parsing window size for GDA AcquisitionGUI.\n" + "Default assumed " + width
								+ " " + height);
					} finally {
						setSize(width, height);
					}
				} else {
					// If no properties have been set then call the pack
					// method, this
					// should set the AcquisitionFrame to its preferred
					// size.
					pack();
				}
			}
		}
	}

	@Override
	public DockingManager getDockingManager() {
		return _dockingManager;
	}

	/**
	 * @param name
	 * @param container
	 * @return the dockable frame
	 */
	static protected DockableFrame createDockableFrame(String name, Container container) {
		DockableFrame f = new DockableFrame(name, JideIconsFactory.getImageIcon(JideIconsFactory.DockableFrame.BLANK));
		int dc = DockContext.STATE_FRAMEDOCKED;
		int horizontalScrollBarPolicy = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER;
		int verticalScrollBarPolicy = ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER;

		f.getContext().setInitSide(DockContext.DOCK_SIDE_WEST);

		if (container instanceof AcquisitionPanel) {
			AcquisitionPanel acquisitionPanel = (AcquisitionPanel) container;
			dc = acquisitionPanel.getInitModeAsInt();
			horizontalScrollBarPolicy = acquisitionPanel.getHorizontalScrollBarPolicyAsInt();
			verticalScrollBarPolicy = acquisitionPanel.getVerticalScrollBarPolicyAsInt();

		} else if (container instanceof GUIMessagePanel) {
			f.getContext().setInitSide(DockContext.DOCK_SIDE_SOUTH);
		}
		if( container instanceof DockContextProvider){
			f.getContext().setInitSide(((DockContextProvider)container).getInitSide());
		}
		f.getContext().setCurrentMode(dc);
		f.getContext().setInitMode(dc);

		if (horizontalScrollBarPolicy != ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
				&& verticalScrollBarPolicy != ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER) {
			JScrollPane panel = new JScrollPane(container);
			panel.setHorizontalScrollBarPolicy(horizontalScrollBarPolicy);
			panel.setVerticalScrollBarPolicy(verticalScrollBarPolicy);
			f.getContentPane().add(panel);
		} else {
			f.getContentPane().add(container);
		}
		f.setKey(name);
		f.setName(name);
		f.setAvailableButtons(DockableFrame.BUTTON_CLOSE | DockableFrame.BUTTON_MAXIMIZE);
		f.setUndockedBounds(new Rectangle(500, 400));
		f.setFocusTraversalPolicy(new LayoutFocusTraversalPolicy());
		return f;
	}

	/**
	 * Allow other panels to set the focus of the main tabbedPane
	 * 
	 * @param panelName
	 *            String the name of the panel to set focus to
	 */
	public void setFocus(String panelName) {
		for (int i = 0; i < tabbedPane.getTabCount(); i++) {
			if (tabbedPane.getComponentAt(i).getName().equals(panelName))
				tabbedPane.setSelectedIndex(i);
		}
	}

	/**
	 * Get the name of the tab which has the focus
	 * 
	 * @return the tab name
	 */
	public String getFocus() {
		int i = tabbedPane.getSelectedIndex();
		return tabbedPane.getComponentAt(i).getName();
	}
}
