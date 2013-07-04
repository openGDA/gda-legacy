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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;

/**
 * A class to display an individual view of an OE with degrees of freedom indicated as arrows on the view.
 */
public class OEImageView implements Viewable, Representation {
	private String name;

	private int xImagePosition = 0;

	private int yImagePosition = 0;

	private String oeGifName;

	private Map<String, Viewable> views = new LinkedHashMap<String, Viewable>();

	private boolean showAtStartup = false;

	private static boolean first = true;

	private Representation parent;

	private DOFImageComponent currentComponent = null;

	private DirectionToggle directionToggle;

	private JPanel oeImagePanel;

	private ImageIcon icon;

	private JLayeredPane pane;

	private boolean editable = false;

	private int protectionLevel = 0;

	/**
	 * Null constructor for Castor
	 */
	public OEImageView() {
	}

	@Override
	public ArrayList<Viewable> getViewableList() {
		return new ArrayList<Viewable>(views.values());
	}

	@Override
	public void addViewable(Viewable viewable) {
		views.put(viewable.getName(), viewable);
	}

	/**
	 * @return Returns the name.
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            The name to set.
	 */
	@Override
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return Returns the showAtStartup.
	 */
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

	/**
	 * @return Returns the oeGifName.
	 */
	public String getOeGifName() {
		return oeGifName;
	}

	/**
	 * @param oeGifName
	 *            The oeGifName to set.
	 */
	public void setOeGifName(String oeGifName) {
		this.oeGifName = oeGifName;
	}

	@Override
	public int getProtectionLevel() {
		return protectionLevel;
	}

	@Override
	public void setProtectionLevel(int protectionLevel) {
		this.protectionLevel = protectionLevel;
	}

	/**
	 * @return Returns the xImagePosition.
	 */
	public int getXImagePosition() {
		return xImagePosition;
	}

	/**
	 * @param imagePosition
	 *            The xImagePosition to set.
	 */
	public void setXImagePosition(int imagePosition) {
		xImagePosition = imagePosition;
	}

	/**
	 * @return Returns the yImagePosition.
	 */
	public int getYImagePosition() {
		return yImagePosition;
	}

	/**
	 * @param imagePosition
	 *            The yImagePosition to set.
	 */
	public void setYImagePosition(int imagePosition) {
		yImagePosition = imagePosition;
	}

	@Override
	public ArrayList<Representation> getRepresentationList() {
		// Do nothing for leaf implementation of composite pattern
		return null;
	}

	@Override
	public void addRepresentation(Representation representation) {
		// Do nothing for leaf implementation of composite pattern
	}

	@Override
	public JComponent getDisplayComponent() {
		pane = new JLayeredPane();
		JComponent component;

		// DirectionToggle to be used by the user when the selected mode of
		// movement is Continuous
		directionToggle = new DirectionToggle();

		oeImagePanel = new JPanel();
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 1;

		icon = new ImageIcon(getClass().getResource("OEImages/" + oeGifName));
		JLabel label = new JLabel(icon);

		if (editable) {
			oeImagePanel.addMouseMotionListener(new MouseMotionAdapter() {

				@Override
				public void mouseDragged(MouseEvent me) {
					Rectangle r = oeImagePanel.getBounds();
					int width = icon.getIconWidth();
					int height = icon.getIconHeight();
					xImagePosition = r.x + me.getX() - width / 2;
					yImagePosition = r.y + me.getY() - height / 2;
					oeImagePanel.setBounds(xImagePosition, yImagePosition, width, height);
				}
			});
		}
		oeImagePanel.setLayout(new GridBagLayout());
		oeImagePanel.add(label, c);
		oeImagePanel.setToolTipText(name);
		oeImagePanel.setBounds(xImagePosition, yImagePosition, icon.getIconWidth(), icon.getIconHeight());
		oeImagePanel.setOpaque(false);
		pane.add(oeImagePanel, JLayeredPane.DEFAULT_LAYER);

		((OERepresentation) parent).setCurrentRepresentation(this);
		// Now add DOFs

		for (Viewable viewable : views.values()) {
			component = viewable.getDisplayComponent();
			pane.add(component, JLayeredPane.PALETTE_LAYER);
			if (first) {
				first = false;
				currentComponent = (DOFImageComponent) component;
				currentComponent.setSelected(true);
			}
			((DOFImageView) viewable).setOE(((OERepresentation) parent).getOE());
			((DOFImageView) viewable).displayLabel();
		}

		JPanel displayPanel = new JPanel();
		displayPanel.setLayout(new BorderLayout());
		displayPanel.add(pane, BorderLayout.CENTER);
		displayPanel.add(directionToggle, BorderLayout.EAST);
		return displayPanel;
	}

	/**
	 * 
	 */
	public void setSelectedDOFComponent() {
		if (currentComponent != null)
			currentComponent.doSelect();
	}

	@Override
	public JComponent getControlComponent() {
		// not required for leaf implementation
		return null;
	}

	@Override
	public int getXPosition() {
		// not required for leaf implementation
		return 0;
	}

	@Override
	public void setXPosition(int x) {
		// not required for leaf implementation
	}

	@Override
	public int getYPosition() {
		// not required for leaf implementation
		return 0;
	}

	@Override
	public void setYPosition(int y) {
		// not required for leaf implementation
	}

	@Override
	public String getThumbNail() {
		// not required for leaf implementation
		return null;
	}

	@Override
	public int getFrameHeight() {
		// not required for leaf implementation
		return 0;
	}

	@Override
	public int getFrameWidth() {
		// not required for leaf implementation
		return 0;
	}

	@Override
	public void setParent(Representation parent) {
		this.parent = parent;
	}

	@Override
	public void setParent(Viewable viewable) {
		// do nothing this is the parent
	}

	/**
	 * @return the direction toggle
	 */
	public DirectionToggle getDirectionToggle() {
		return directionToggle;
	}

	@Override
	public void setResizeable(boolean resizeable) {
	}

	@Override
	public boolean isResizeable() {
		return false;
	}

	@Override
	public void setEditable(boolean editable) {
		this.editable = editable;
	}

	@Override
	public boolean isEditable() {
		return editable;
	}

	@Override
	public void setFrameHeight(int frameHeight) {
	}

	@Override
	public void setFrameWidth(int frameWidth) {
	}
}
