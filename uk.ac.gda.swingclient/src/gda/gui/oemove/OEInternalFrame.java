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

import java.awt.BorderLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.WindowConstants;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class performs a container role as it encompasses the OE and all its associated DOFs.
 * <p>
 * All OEInternalFrames are managed by OEInternalFrameListener.
 * <p>
 * OEControl and DOFControlPanel are responsible for displaying all relevant data and information of the currently
 * selected degree of freedom of the activated OEInternalFrame.
 */
public class OEInternalFrame extends JInternalFrame {
	private static final Logger logger = LoggerFactory.getLogger(OEInternalFrame.class);

	private JComponent displayComponent;

	private Representation representation;

	private ControlPanel controlPanel;

	private String name;

	/**
	 * Display a child of an OERepresentation on the desktop panel in a single frame. The frame title is the name of the
	 * child representation (OEImageView) prefixed with the representation name, which is the name of the OE.
	 * 
	 * @param representation
	 * @param childRepresentation
	 * @param controlPanel
	 */
	public OEInternalFrame(Representation representation, Representation childRepresentation, ControlPanel controlPanel) {
		super(representation.getName() + ":" + childRepresentation.getName(), representation.isResizeable(), true,
				true, true);
		this.representation = representation;
		this.controlPanel = controlPanel;
		name = representation.getName() + ":" + childRepresentation.getName();
		displayComponent = childRepresentation.getDisplayComponent();
		if (controlPanel != null)
			controlPanel.addControlComponent(name, representation);
		initialise();
	}

	/**
	 * Display an OERepresentation on the desktop panel in a single frame. If there is more than one child of the
	 * OERepresentation then each will be displayed in a separate tabbed pane. The frame title is the name of the
	 * representation which is the name of the OE.
	 * 
	 * @param representation
	 * @param controlPanel
	 */
	public OEInternalFrame(Representation representation, ControlPanel controlPanel) {
		super(representation.getName(), representation.isResizeable(), true, true, true);
		this.representation = representation;
		this.controlPanel = controlPanel;
		name = representation.getName();
		displayComponent = representation.getDisplayComponent();
		if (controlPanel != null)
			controlPanel.addControlComponent(name, representation);
		initialise();
	}

	/**
	 * 
	 */
	public void initialise() {
		int height;
		int width;
		int xPos = 0;
		int yPos = 0;
		height = representation.getFrameHeight();
		width = representation.getFrameWidth();
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		getContentPane().setLayout(new BorderLayout());
		setBounds(xPos, yPos, width, height);
		if (controlPanel != null) {
			addInternalFrameListener(new InternalFrameAdapter() {
				/**
				 * This method will be called (indirectly by the DesktopManager) when a frame becomes the active frame.
				 * This will be because the user has clicked on it, or it has been de-iconified, or it has just had
				 * setSelected(true) called on it.
				 * 
				 * @param e
				 *            the InternalFrameEvent
				 */
				@Override
				public void internalFrameActivated(InternalFrameEvent e) {
					logger.debug("Internal frame is activated for " + name);
					controlPanel.updateControlPanel(name);
				}

				/**
				 * This method will be called (indirectly by the UI) when the user clicks on the close frame glyph.
				 * 
				 * @param e
				 *            the InternalFrameEvent
				 */
				@Override
				public void internalFrameClosing(InternalFrameEvent e) {
					logger.debug("Internal frame is closing " + name);
					controlPanel.removeControlComponent(name);
				}
			});
		}

		// used by OEEditor to resize the internal frame
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				JInternalFrame iframe = (JInternalFrame) e.getComponent();
				representation.setFrameWidth(iframe.getWidth());
				representation.setFrameHeight(iframe.getHeight());
				representation.setXPosition(iframe.getX());
				representation.setYPosition(iframe.getY());
			}

			@Override
			public void componentMoved(ComponentEvent e) {
				JInternalFrame iframe = (JInternalFrame) e.getComponent();
				representation.setXPosition(iframe.getX());
				representation.setYPosition(iframe.getY());
			}
		});

		getContentPane().add(displayComponent, BorderLayout.CENTER);
		if (representation.isResizeable() && !representation.isEditable())
			pack();
		setVisible(true);
	}

	/**
	 * Get the representation displayed by this class.
	 * 
	 * @return the representation displayed..
	 */
	public Representation getRepresentation() {
		return representation;
	}

	/**
	 * 
	 */
	public void setDisplayComponent() {
		displayComponent = representation.getDisplayComponent();
		representation.setEditable(true);
		getContentPane().removeAll();
		getContentPane().add(displayComponent, BorderLayout.CENTER);
	}
}
