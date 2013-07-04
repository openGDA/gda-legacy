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

import gda.gui.oemove.editor.PositionDisplayMenu;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

/**
 * This class presents DOFImage on screen in OEMove. It also behaves like a JButton, that is when clicked on the colour
 * of DOFImage changes colour from black to red. There will only ever be one degree of freedom selected.
 */
public class DOFImageComponent extends JPanel {
	private ImageIcon unSelectedImage;

	private ImageIcon selectedImage;

	private JLabel label;

	private static DOFImageComponent selectedDOFComponent = null;

	private DOFImageView dofImageView;

	private JPopupMenu popup;

	private JComponent lastDpd = null;

	/**
	 * @param dofView
	 */
	public DOFImageComponent(DOFImageView dofView) {
		this.dofImageView = dofView;
		popup = new PositionDisplayMenu(dofView);
		GridBagConstraints c = new GridBagConstraints();
		setLayout(new GridBagLayout());

		label = new JLabel();
		if (dofView.isEditable()) {
			label.addMouseMotionListener(new MouseMotionAdapter() {
				@Override
				public void mouseDragged(MouseEvent me) {
					if ((me.getModifiers() & InputEvent.BUTTON3_MASK) != InputEvent.BUTTON3_MASK) {
						Rectangle r = getBounds();
						int width = selectedImage.getIconWidth();
						int height = selectedImage.getIconHeight();
						int x = r.x + me.getX() - width / 2;
						int y = r.y + me.getY() - height / 2;
						setBounds(x, y, width, height);
						setXY(x, y);
						dofImageView.displayLabel();
						// repaint();
					}
				}
			});
			label.addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent me) {
					if ((me.getModifiers() & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK) {
						popup.show(me.getComponent(), me.getX(), me.getY());
					}
				}
			});
		} else {
			label.addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent e) {
					doSelect();
					notifyControl();
				}
			});
		}
		c.gridx = 1;
		c.gridy = 1;
		add(label, c);

		String arrowGifName = dofView.getArrowGifName();
		unSelectedImage = new ImageIcon(getClass().getResource("Images/" + arrowGifName));
		selectedImage = new ImageIcon(getClass().getResource("s_Images/" + arrowGifName));

		setSelected(false);
		setBounds(dofView.getXImagePosition(), dofView.getYImagePosition(), unSelectedImage.getIconWidth(),
				unSelectedImage.getIconHeight());
		setOpaque(false);
	}

	/**
	 * 
	 */
	public void doSelect() {
		if (selectedDOFComponent != null)
			selectedDOFComponent.setSelected(false);

		setSelected(true);
	}

	private void notifyControl() {
		dofImageView.notifyIObservers();
	}

	/**
	 * Set the image to its selected state.
	 * 
	 * @param selected
	 *            true to select component else false
	 */
	public void setSelected(boolean selected) {
		if (selected) {
			label.setIcon(selectedImage);
			selectedDOFComponent = this;
		} else {
			label.setIcon(unSelectedImage);
		}
		setSize(getPreferredSize());
	}

	/**
	 * @param compassPoint
	 * @param dpd
	 */
	public void addDisplay(String compassPoint, JComponent dpd) {
		if (lastDpd != null)
			remove(lastDpd);
		lastDpd = dpd;
		GridBagConstraints c = PositionDisplayMenu.convertCompassPoint(compassPoint);
		add(dpd, c);

		// Recalculate the bounds of the DOFImage and DOFPositionDisplay
		// label so that the DOFImage remains in a constant position when adding
		// the position display to compass points SouthWest thru' NorthEast.

		Dimension d = dpd.getPreferredSize();
		int x = getX();
		int y = getY();
		int width = getWidth();
		int height = getHeight();

		if (c.gridy == 0) {
			y = y - d.height;
		}
		if (c.gridx == 0) {
			x = x - d.width;
		}
		setBounds(x, y, width, height);

		setSize(getPreferredSize());
		revalidate();
		repaint();
	}

	private void setXY(int x, int y) {
		dofImageView.setXImagePosition(x);
		dofImageView.setYImagePosition(y);
	}
}
