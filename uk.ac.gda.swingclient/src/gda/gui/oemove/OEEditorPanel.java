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
import gda.factory.Finder;
import gda.gui.AcquisitionPanel;
import gda.gui.util.SimpleFileFilter;
import gda.oe.OE;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OEEditorPanel Class
 */
public class OEEditorPanel extends AcquisitionPanel {
	
	private static final Logger logger = LoggerFactory.getLogger(OEEditorPanel.class);
	
	private JMenuBar menuBar;
	private JMenu fileMenu;
	private JMenu oeMenu;
	private JMenu oeImageMenu;
	private JMenu dofMenu;
	private JMenu arrowMenu;
	private DesktopPanel desktopPanel;
	private JFileChooser jf;

	private ArrayList<String> oeNames;
	private OERepresentation currentRepresentation;
	private DOFImageView dofView;
	private RepresentationFactory factory = new RepresentationFactory();
	private String currentDOFName;

	/**
	 * 
	 */
	public OEEditorPanel() {
	}

	@Override
	public void configure() {
		menuBar = new JMenuBar();
		fileMenu = new JMenu("File");
		oeMenu = new JMenu("OE");
		oeImageMenu = new JMenu("OEImages");
		dofMenu = new JMenu("DOFs");
		arrowMenu = new JMenu("Arrows");

		menuBar.add(fileMenu);
		menuBar.add(oeMenu);
		menuBar.add(oeImageMenu);
		menuBar.add(dofMenu);
		menuBar.add(arrowMenu);

		setLayout(new BorderLayout());
		add(menuBar, BorderLayout.NORTH);
		desktopPanel = new DesktopPanel();
		add(desktopPanel, BorderLayout.CENTER);
		desktopPanel.addPropertyChangeListener("representation", factory);
		factory.setName("OEViewFactory");

		JMenuItem load = new JMenuItem("Load");
		load.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				loadRepresentations();
			}
		});
		fileMenu.add(load);

		JMenuItem save = new JMenuItem("Save");
		save.addActionListener(new ActionListener() {
			@SuppressWarnings("unused")
			@Override
			public void actionPerformed(ActionEvent ev) {
				String xmlFile = LocalProperties.get("gda.gui.oemove.xmlFile");
				new Saver(xmlFile, factory);
			}
		});
		fileMenu.add(save);

		String filter[] = { "xml" };
		jf = new JFileChooser(System.getProperty("user.dir"));
		jf.addChoosableFileFilter(new SimpleFileFilter(filter, "XML (*.xml)"));

		JMenuItem saveAs = new JMenuItem("Save As...");
		saveAs.addActionListener(new ActionListener() {
			@SuppressWarnings("unused")
			@Override
			public void actionPerformed(ActionEvent ev) {
				if (jf.showSaveDialog(getRootPane()) == JFileChooser.APPROVE_OPTION) {
					if (jf.getSelectedFile() != null) {
						String fileName = jf.getSelectedFile().getAbsolutePath();
						logger.debug("ParameterFileController: Save file " + fileName);
						new Saver(fileName, factory);
					}
				}
			}
		});
		fileMenu.add(saveAs);

		Finder finder = Finder.getInstance();
		oeNames = finder.listAllNames("OE");
		JMenuItem[] item = new JMenuItem[oeNames.size()];
		for (int i = 0; i < oeNames.size(); i++) {
			item[i] = new JMenuItem(oeNames.get(i));
			item[i].addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent ev) {
					String oeName = ev.getActionCommand();
					OE oe = ((OE) Finder.getInstance().find(oeName));
					String[] items = oe.getDOFNames();
					JMenuItem[] dofItems = new JMenuItem[items.length];
					dofMenu.removeAll();
					for (int i = 0; i < items.length; i++) {
						dofItems[i] = new JMenuItem(items[i]);
						dofMenu.add(dofItems[i]);
						dofItems[i].addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent ev) {
								currentDOFName = ev.getActionCommand();
							}
						});
					}
					currentRepresentation = new OERepresentation();
					currentRepresentation.setName(oeName);
					currentRepresentation.setFrameHeight(200);
					currentRepresentation.setFrameWidth(200);
					currentRepresentation.setResizeable(true);
					currentRepresentation.setEditable(true);
					desktopPanel.display(currentRepresentation);
					factory.addRepresentation(currentRepresentation);
				}
			});
			oeMenu.add(item[i]);
		}

		addDOFImage();
		addArrowImages("Images/", arrowMenu);
	}

	@Override
	public void tidyup() {
	}

	private void addDOFImage() {
		JMenu leftSubMenu = new JMenu("Left");
		JMenu rightSubMenu = new JMenu("Right");
		try {
			ArrayList<JMenuItem> menuItems = new ArrayList<JMenuItem>();
			String line;
			URL url = getClass().getResource("OEImages/");
			String urlFile = url.getFile();
			if (urlFile.contains("!")) {
				String s = urlFile.substring(5, urlFile.indexOf('!'));
				JarFile jf = new JarFile(s);
				for (Enumeration<JarEntry> e = jf.entries(); e.hasMoreElements();) {
					JarEntry jarEntry = e.nextElement();
					if (jarEntry.toString().contains("/OEImages")) {
						line = jarEntry.toString().substring(jarEntry.toString().lastIndexOf('/') + 1);
						JMenuItem menuItem = getOEImageMenuItem(line);
						menuItems.add(menuItem);
						if (line.startsWith("L"))
							leftSubMenu.add(menuItem);
						else if (line.startsWith("R"))
							rightSubMenu.add(menuItem);
					}
				}
			} else {
				InputStream is = url.openStream();
				BufferedReader reader = new BufferedReader(new InputStreamReader(is));
				while ((line = reader.readLine()) != null) {
					JMenuItem menuItem = getOEImageMenuItem(line);
					menuItems.add(menuItem);
					if (line.startsWith("L"))
						leftSubMenu.add(menuItem);
					else if (line.startsWith("R"))
						rightSubMenu.add(menuItem);
				}
			}
			oeImageMenu.add(leftSubMenu);
			oeImageMenu.add(rightSubMenu);
		} catch (IOException e) {
			logger.error("OEEditor: " + e);
		}
	}

	private JMenuItem getOEImageMenuItem(String line) {
		JMenuItem menuItem = new JMenuItem(line);
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				OEImageView imageView = new OEImageView();
				String oeGifName = ev.getActionCommand();
				imageView.setOeGifName(oeGifName);
				imageView.setName(oeGifName.substring(0, oeGifName.lastIndexOf(".")));
				currentRepresentation.addRepresentation(imageView);
				desktopPanel.reDisplay(currentRepresentation);
			}
		});
		return menuItem;
	}

	private void addArrowImages(String directory, JMenu currentMenu) {
		try {
			String line;
			String command;
			String preString;
			URL url = getClass().getResource(directory);
			String urlFile = url.getFile();
			/*
			 * Are the arrow images being read from a jar file or a directory structure.
			 */
			if (urlFile.contains("!")) {
				preString = url.toString().substring(0, url.toString().indexOf('!') + 2);
				String s = urlFile.substring(5, urlFile.indexOf('!'));
				JarFile jf = new JarFile(s);
				/*
				 * Parse each line of the jar file to establish whether it contains information about the correct
				 * directory. Then create the menus checking that each sub menu has not already been created. Finally
				 * add the icon contains in the file.
				 */
				for (Enumeration<JarEntry> e = jf.entries(); e.hasMoreElements();) {
					JarEntry jarEntry = e.nextElement();
					line = jarEntry.toString();
					if (line.contains("/" + directory) && !line.endsWith("/")) {
						String imageFile = line;
						line = line.substring(line.indexOf("/" + directory) + directory.length() + 1);
						command = line;
						JMenu menu = currentMenu;
						StringTokenizer st = new StringTokenizer(line, "/");
						while (st.countTokens() != 1) {
							String token = st.nextToken();
							JMenu subMenu = new JMenu(token);
							Component[] components = menu.getMenuComponents();
							boolean exists = false;
							for (int i = 0; i < components.length; i++) {
								JMenu jmenu = (JMenu) components[i];
								if (token.equals(jmenu.getText())) {
									exists = true;
									menu = jmenu;
								}
							}
							if (!exists) {
								menu.add(subMenu);
								menu = subMenu;
							}
						}
						URL urlurl = new URL(preString + imageFile);
						ImageIcon icon = new ImageIcon(urlurl);
						JMenuItem menuItem = new JMenuItem(icon);
						menu.add(menuItem);
						setArrowMenuItem(menuItem, command);
					}
				}
			} else {
				BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
				while ((line = reader.readLine()) != null) {
					if (!line.startsWith(".")) {
						if (line.endsWith(".gif") || line.endsWith(".png")) {
							URL urlForFile = getClass().getResource(directory + line);
							ImageIcon icon = new ImageIcon(urlForFile);
							String filename = urlForFile.toString();
							command = filename.substring(filename.lastIndexOf("Images/") + 7, filename.length());

							JMenuItem menuItem = new JMenuItem(icon);
							currentMenu.add(menuItem);
							setArrowMenuItem(menuItem, command);
						} else {
							JMenu jm = new JMenu(line);
							currentMenu.add(jm);
							addArrowImages(directory + line + "/", jm);
						}
					}
				}
			}
		} catch (IOException e) {
			logger.error("OEEditor: " + e);
		}
	}

	private void setArrowMenuItem(JMenuItem menuItem, String command) {
		menuItem.setActionCommand(command);
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				dofView = new DOFImageView();
				dofView.setName(currentDOFName);
				dofView.setArrowGifName(ev.getActionCommand());
				dofView.setEditable(true);
				OEImageView oeImageView = (OEImageView) currentRepresentation.getCurrentRepresentation();
				oeImageView.addViewable(dofView);
				desktopPanel.reDisplay(currentRepresentation);
			}
		});

	}

	private void loadRepresentations() {
		String xmlFile = LocalProperties.get("gda.gui.oemove.xmlFile");
		factory = new Loader(xmlFile).getRepresentationFactory();
		for (Representation representation : factory.getRepresentationList()) {
			representation.setResizeable(true);
			representation.setEditable(true);
			ArrayList<Representation> children = representation.getRepresentationList();
			if (children != null) {
				for (Representation childRepresentation : children) {
					ArrayList<Viewable> views = ((OEImageView) childRepresentation).getViewableList();
					for (Viewable view : views) {
						((DOFImageView) view).setEditable(true);
					}
				}
			}
			desktopPanel.display(representation);
		}
	}
}
