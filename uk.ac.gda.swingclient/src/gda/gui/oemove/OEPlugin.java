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

import java.util.ArrayList;

import javax.swing.JComponent;

/**
 * A class to allow representations other than OE's to be displayed in the OEInternalFrame class
 */
public class OEPlugin implements Representation {
	private String name;

	private String thumbNail;

	private String pluginName;

	private int frameWidth = 0;

	private int frameHeight = 0;

	private int xPosition = 0;

	private int yPosition = 0;

	private boolean showAtStartup = false;

	private Pluggable plugin = null;

	@Override
	public ArrayList<Representation> getRepresentationList() {
		return null;
	}

	@Override
	public void addRepresentation(Representation representation) {
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public int getXPosition() {
		return xPosition;
	}

	@Override
	public void setXPosition(int x) {
		xPosition = x;
	}

	@Override
	public int getYPosition() {
		return yPosition;
	}

	@Override
	public void setYPosition(int y) {
		yPosition = y;
	}

	@Override
	public void setFrameHeight(int frameHeight) {
		this.frameHeight = frameHeight;
	}

	@Override
	public int getFrameHeight() {
		int height = 0;
		if (frameHeight > 0)
			height = frameHeight;
		else if (plugin != null)
			height = getDisplayComponent().getPreferredSize().height;
		return height;
	}

	@Override
	public void setFrameWidth(int frameWidth) {
		this.frameWidth = frameWidth;
	}

	@Override
	public int getFrameWidth() {
		int width = 0;
		if (frameWidth > 0)
			width = frameWidth;
		else if (plugin != null)
			width = getDisplayComponent().getPreferredSize().width;

		return width;
	}

	@Override
	public JComponent getDisplayComponent() {
		// NB since no configuring is done in OEMove, and this finding cannot be
		// done
		// when the name is set must have it here.
		if (plugin == null) {
			plugin = (Pluggable) Finder.getInstance().find(pluginName);
		}
		return plugin.getDisplayComponent();
	}

	@Override
	public JComponent getControlComponent() {
		// NB since no configuring is done in OEMove, and this finding cannot be
		// done
		// when the name is set must have it here.
		if (plugin == null) {
			plugin = (Pluggable) Finder.getInstance().find(pluginName);
		}
		return plugin.getControlComponent();
	}

	/**
	 * @param showAtStartup
	 *            The showAtStartup to set.
	 */
	public void setShowAtStartup(boolean showAtStartup) {
		this.showAtStartup = showAtStartup;
	}

	@Override
	public boolean isShowAtStartup() {
		return showAtStartup;
	}

	/**
	 * @return Returns the pluginName.
	 */
	public String getPluginName() {
		return pluginName;
	}

	/**
	 * @param pluginName
	 *            The pluginName to set.
	 */
	public void setPluginName(String pluginName) {
		this.pluginName = pluginName;
	}

	/**
	 * @return Returns the thumbNail.
	 */
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

	@Override
	public void setParent(Representation representation) {
		// do nothing this is the parent
	}

	@Override
	public void setResizeable(boolean resizeable) {
	}

	@Override
	public boolean isResizeable() {
		return true;
	}

	@Override
	public void setEditable(boolean editable) {
	}

	@Override
	public boolean isEditable() {
		return false;
	}

	@Override
	public int getProtectionLevel() {
		return 0;
	}

	@Override
	public void setProtectionLevel(int protectionLevel) {
		// not used at present time
	}
}
