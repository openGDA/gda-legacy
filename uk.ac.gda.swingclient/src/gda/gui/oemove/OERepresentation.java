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

import gda.factory.Finder;
import gda.oe.OE;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * A class to manage the display of representations
 */
public class OERepresentation implements Representation {

	private String name;
	private String thumbNail;
	private int xPosition = 0;
	private int yPosition = 0;
	private int frameWidth;
	private int frameHeight;
	private boolean showAtStartup = false;
	private Map<String, Representation> representations = new LinkedHashMap<String, Representation>();
	private Representation currentRepresentation = null;
	private OE oe;
	private Map<String, OEControl> oeControls = new LinkedHashMap<String, OEControl>();
	private JTabbedPane tabbedPane;
	private OEControlComponent oeControlPanel = null;
	private boolean resizeable = false;
	private boolean editable = false;
	private int protectionLevel = 0;

	/**
	 * Null constructor for Castor
	 */
	public OERepresentation() {
	}

	@Override
	public ArrayList<Representation> getRepresentationList() {
		return new ArrayList<Representation>(representations.values());
	}

	@Override
	public void addRepresentation(Representation representation) {
		representation.setParent(this);
		representations.put(representation.getName(), representation);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @return Returns the name.
	 * @see gda.gui.oemove.Representation#getName()
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @param name
	 *            The name to set.
	 * @see gda.gui.oemove.Representation#setName(java.lang.String)
	 */
	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public int getFrameHeight() {
		return frameHeight;
	}

	@Override
	public void setFrameHeight(int frameHeight) {
		this.frameHeight = frameHeight;
	}

	@Override
	public int getFrameWidth() {
		return frameWidth;
	}

	@Override
	public void setFrameWidth(int frameWidth) {
		this.frameWidth = frameWidth;
	}

	@Override
	public int getProtectionLevel() {
		return protectionLevel;
	}

	@Override
	public void setProtectionLevel(int protectionLevel) {
		this.protectionLevel = protectionLevel;
	}

	@Override
	public JComponent getDisplayComponent() {
		JComponent displayComponent = null;
		tabbedPane = new JTabbedPane();
		int size = representations.size();
		for (Representation representation : representations.values()) {
			if (currentRepresentation == null)
				currentRepresentation = representation;

			if (editable)
				representation.setEditable(true);
			displayComponent = representation.getDisplayComponent();
			if (!editable) {
				OEControl oeControl = new OEControl(this);
				oeControls.put(representation.getName(), oeControl);
			}
			if (size == 1) {
				return displayComponent;
			}
			tabbedPane.add(representation.getName(), displayComponent);
		}
		tabbedPane.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent ev) {
				JTabbedPane tabbedPane = (JTabbedPane) ev.getSource();
				if (tabbedPane.getTabCount() > 0) {
					String tabName = tabbedPane.getTitleAt(tabbedPane.getSelectedIndex());
					for (Representation representation : representations.values()) {
						if (representation.getName().equals(tabName)) {
							currentRepresentation = representation;
							break;
						}
					}
					oeControlPanel.removeAll();
					OEControl oeControl = oeControls.get(tabbedPane.getTitleAt(tabbedPane.getSelectedIndex()));
					oeControlPanel.addControl(oeControl);
					oeControl.repaint();
				}
			}
		});
		return tabbedPane;
	}

	@Override
	public void setParent(Representation representation) {
		// do nothing this is the parent
	}

	/**
	 * @return the OE
	 */
	public OE getOE() {
		if (oe == null)
			oe = (OE) Finder.getInstance().find(name);
		return oe;
	}

	@Override
	public JComponent getControlComponent() {
		oeControlPanel = new OEControlComponent();
		oeControlPanel.setLayout(new BorderLayout());
		if (tabbedPane != null && tabbedPane.getTabCount() > 0) {
			oeControlPanel.addControl(oeControls.get(tabbedPane.getTitleAt(tabbedPane.getSelectedIndex())));
		} else {
			oeControlPanel.addControl(new OEControl(this));
		}
		return oeControlPanel;
	}

	@Override
	public int getXPosition() {
		return xPosition;
	}

	@Override
	public void setXPosition(int xPosition) {
		this.xPosition = xPosition;
	}

	@Override
	public int getYPosition() {
		return yPosition;
	}

	@Override
	public void setYPosition(int yPosition) {
		this.yPosition = yPosition;
	}

	@Override
	public boolean isShowAtStartup() {
		return showAtStartup;
	}

	/**
	 * @param showAtStartup
	 *            The showAtStartup to set.
	 */
	public void setShowAtStartup(boolean showAtStartup) {
		this.showAtStartup = showAtStartup;
	}

	@Override
	public String getThumbNail() {
		return thumbNail;
	}

	/**
	 * @param thumbNail
	 *            The thumbNail to set.
	 */
	public void setThumbNail(String thumbNail) {
		this.thumbNail = thumbNail;
	}

	/**
	 * @return Returns the currentRepresentation.
	 */
	public Representation getCurrentRepresentation() {
		return currentRepresentation;
	}

	/**
	 * @param currentRepresentation
	 *            The currentRepresentation to set.
	 */
	public void setCurrentRepresentation(Representation currentRepresentation) {
		this.currentRepresentation = currentRepresentation;
	}

	class OEControlComponent extends JPanel {
		private OEControl oeControl;

		/**
		 * @param oeControl
		 */
		public void addControl(OEControl oeControl) {
			this.oeControl = oeControl;
			add(oeControl, BorderLayout.CENTER);
		}

		@Override
		public void repaint() {
			if (oeControl != null)
				oeControl.repaint();
			else
				super.repaint();
		}
	}

	@Override
	public void setResizeable(boolean resizeable) {
		this.resizeable = resizeable;
	}

	@Override
	public boolean isResizeable() {
		return resizeable;
	}

	@Override
	public void setEditable(boolean editable) {
		this.editable = editable;
	}

	@Override
	public boolean isEditable() {
		return editable;
	}
}
