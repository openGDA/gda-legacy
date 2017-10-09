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
import gda.gui.AcquisitionPanel;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;

import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;

/**
 * Create the OEMove interface panel, comprising menubar, desktop and control sub-panels
 */
public class OEMovePanel extends AcquisitionPanel {

	private ControlPanel controlPanel;
	private DesktopPanel desktopPanel;
	private RepresentationMenuBar representationMenuBar;
	private RepresentationFactory factory;
	private boolean loaded = false;

	/**
	 * Constructor
	 */
	public OEMovePanel() {
		String xmlFile = LocalProperties.get("gda.gui.oemove.xmlFile");
		factory = new Loader(xmlFile).getRepresentationFactory();

		controlPanel = new ControlPanel();
		controlPanel.setBorder(new BevelBorder(BevelBorder.RAISED));
		desktopPanel = new DesktopPanel(controlPanel);
		representationMenuBar = new RepresentationMenuBar(desktopPanel, factory);

		GridBagConstraints c = new GridBagConstraints();
		setLayout(new GridBagLayout());
		c.gridx = 0;
		c.gridy = GridBagConstraints.RELATIVE;
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1.0;
		c.weighty = 0.0;

		add(representationMenuBar, c);
		c.weighty = 1.0;
		add(desktopPanel, c);
		c.weighty = 0.0;
		add(controlPanel, c);
	}

	@Override
	public void configure() {
		// Setting the Frame visible should invoke the OEMovePanel's
		// ancestorListener's ancestorAdded() method which would then load the
		// default OE representations. This doesn't work under JDK 6 where, for
		// some reason, the ancestorListener fails to get the "ancestor
		// visibility
		// changed" message. AncestorListener now removed and load OE reps
		// directly in configure() method.
		if (!loaded) {
			loadDefaultRepresentations();
			loaded = true;
		}
	}

	private void loadDefaultRepresentations() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				if (factory != null) {
					for (Representation representation : factory.getRepresentationList()) {
						if (LocalProperties.check("gda.gui.oemove.pictorialView", false)) {
							if (representation.isShowAtStartup()) {
								desktopPanel.display(representation);
							}
						} else {
							ArrayList<Representation> children = representation.getRepresentationList();
							if (children != null) {
								for (Representation childRepresentation : children) {
									if (representation.isShowAtStartup() || childRepresentation.isShowAtStartup()) {
										desktopPanel.display(representation, childRepresentation);
									}
								}
							}
						}
					}
				}

			}
		});
	}
}
