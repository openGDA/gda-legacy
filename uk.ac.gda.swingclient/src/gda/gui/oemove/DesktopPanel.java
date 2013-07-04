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

import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.DefaultDesktopManager;
import javax.swing.JDesktopPane;
import javax.swing.JPopupMenu;
import javax.swing.border.BevelBorder;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Displays the representations in internal frames. Representations are displayed either as a single view in an internal
 * frame; or as a series of tabbed views in the internal frame.
 */
public class DesktopPanel extends JDesktopPane {
	private static final Logger logger = LoggerFactory.getLogger(DesktopPanel.class);

	private JPopupMenu popup = new JPopupMenu();

	private static String currentState = "default";

	private ControlPanel controlPanel;

	private PropertyChangeSupport p = new PropertyChangeSupport(this);

	// private String stateName;

	// This is used to keep a list of OEInternalFrames indexed by what they
	// represent (which may be a child representation if pictorial view is
	// false). This enables us to prevent creation of several frames for
	// the same Representation.
	private Map<Representation, OEInternalFrame> internalFrames = new LinkedHashMap<Representation, OEInternalFrame>();

	// This is set as an InternalFrameListener on each OEInternalFrame and
	// removes an OEInternalFrame from the map when it closes.
	private InternalFrameAdapter ifa = new InternalFrameAdapter() {
		@Override
		public void internalFrameClosing(InternalFrameEvent e) {
			OEInternalFrame oeif = (OEInternalFrame) e.getSource();
			String name = oeif.getRepresentation().getName();
			// fire property change event to inform the OEEditor panel that
			// the
			// internal frame has been disposed of.
			p.firePropertyChange("representation", null, name);
			// Removing an Object from the values() Collection of a Map does
			// actually remove its key as well.
			internalFrames.values().remove(oeif);
		}
	};

	/**
	 * Constructor
	 */
	public DesktopPanel() {
		setDesktopManager(new DefaultDesktopManager());
		setBorder(new BevelBorder(BevelBorder.LOWERED));
	}

	/**
	 * @param controlPanel
	 */
	public DesktopPanel(ControlPanel controlPanel) {
		this.controlPanel = controlPanel;
		setDesktopManager(new DefaultDesktopManager());
		setBorder(new BevelBorder(BevelBorder.LOWERED));
//		createPopupMenu();
		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent me) {
				if ((me.getModifiers() & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK) {
					popup.show(me.getComponent(), me.getX(), me.getY());
				}
			}
		});
	}

	/**
	 * Use this method do display a representation with all of its childRepresentations
	 * 
	 * @param representation
	 */
	public void display(Representation representation) {
		OEInternalFrame oeInternalFrame;
		int index = 1;

		try {
			if ((oeInternalFrame = internalFrames.get(representation)) == null) {
				oeInternalFrame = new OEInternalFrame(representation, controlPanel);
				oeInternalFrame.setLocation(representation.getXPosition(), representation.getYPosition());
				add(oeInternalFrame, index);
				internalFrames.put(representation, oeInternalFrame);
				oeInternalFrame.addInternalFrameListener(ifa);
			}

			oeInternalFrame.setSelected(true);
			oeInternalFrame.setIcon(false);
			// kludge added: When the loadDefaultRepresentations was removed
			// from the AncestorListener bug #956 and added to the
			// configure()
			// method the setSelected(true) no longer generated an
			// internalFrameActivated event as the desktop was not showing.
			// So
			// we will forcibly update the control panel!
			if (controlPanel != null && !isShowing())
				controlPanel.updateControlPanel(representation.getName());
		} catch (Exception ex) {
			logger.error("Unable to select frame " + representation.getName(), ex);
		}

	}

	/**
	 * Use this method to display a single child representation.
	 * 
	 * @param representation
	 * @param childRepresentation
	 */
	public void display(Representation representation, Representation childRepresentation) {
		OEInternalFrame oeInternalFrame;
		int index = 1;

		try {
			if ((oeInternalFrame = internalFrames.get(childRepresentation)) == null) {
				oeInternalFrame = new OEInternalFrame(representation, childRepresentation, controlPanel);
				oeInternalFrame.setLocation(representation.getXPosition(), representation.getYPosition());
				add(oeInternalFrame, index);
				internalFrames.put(childRepresentation, oeInternalFrame);
				oeInternalFrame.addInternalFrameListener(ifa);
			}
			oeInternalFrame.setSelected(true);
			oeInternalFrame.setIcon(false);
		} catch (java.beans.PropertyVetoException pe) {
			logger.error("Unable to select frame " + representation.getName());
		}
	}

	/**
	 * @param representation
	 */
	public void reDisplay(Representation representation) {
		OEInternalFrame oeInternalFrame;
		if ((oeInternalFrame = internalFrames.get(representation)) != null) {
			oeInternalFrame.setDisplayComponent();
			revalidate();
		}
	}

	/**
	 * @return current state
	 */
	public static String getCurrentState() {
		return currentState;
	}

	@Override
	public void addPropertyChangeListener(String name, PropertyChangeListener listener) {
		p.addPropertyChangeListener(name, listener);
	}
}
