/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package gda.images.GUI;

import gda.images.ImageOperator;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

/**
 * Generic Image operations menu see ImageMenuObserver
 */

@SuppressWarnings("serial")
public class ImageMenu extends JMenu implements ActionListener {
	protected JMenuItem update;

	protected JMenuItem original;

	protected JMenu scaleMenu;

	protected JMenuItem autoScale;

	protected JMenuItem percentSize100;

	protected JMenuItem percentSize50;

	// private boolean toolTips;
	private ImageOperator operator = null;

	private ImageDisplayPanel displayPanel = null;

	/**
	 * Constructor
	 */
	public ImageMenu() {
		this("Image", true);
	}

	/**
	 * Constructor
	 * 
	 * @param title
	 */
	public ImageMenu(String title) {
		this(title, true);
	}

	/**
	 * Constructor
	 * 
	 * @param toolTips
	 */
	public ImageMenu(boolean toolTips) {
		this("Image", toolTips);
	}

	/**
	 * Constructor
	 * 
	 * @param title
	 * @param toolTips
	 */
	public ImageMenu(String title, boolean toolTips) {
		super(title);
		// this.toolTips = toolTips;

		update = new JMenuItem("Update");
		update.addActionListener(this);

		update.setActionCommand("updateImage");
		this.add(update);

		original = new JMenuItem("Original");
		original.addActionListener(this);
		original.setActionCommand("original");
		this.add(original);

		scaleMenu = new JMenu("Scale");

		autoScale = new JMenuItem("Auto Scale");
		autoScale.addActionListener(this);
		autoScale.setActionCommand("autoScale");
		scaleMenu.add(autoScale);

		percentSize100 = new JMenuItem("100 %");
		percentSize100.addActionListener(this);
		percentSize100.setActionCommand("scaleImage 1.0");
		scaleMenu.add(percentSize100);

		percentSize50 = new JMenuItem("50 %");
		percentSize50.addActionListener(this);
		percentSize50.setActionCommand("scaleImage 0.5");
		scaleMenu.add(percentSize50);

		this.add(scaleMenu);

		if (toolTips) {
			this.setToolTipText("display image menu options");
			update.setToolTipText("update image");
			original.setToolTipText("display original image");
			autoScale.setToolTipText("automatically scale displayed image to fit frame");
			percentSize100.setToolTipText("scale displayed image to full size (100%)");
			percentSize50.setToolTipText("scale displayed image to half size (50%)");
		}
	}

	/**
	 * @param operator
	 *            object
	 * @param displayPanel
	 */
	public void initialise(ImageOperator operator, ImageDisplayPanel displayPanel) {
		this.operator = operator;
		this.displayPanel = displayPanel;
	}

	/**
	 * @param sensitive
	 */
	public void setMenusSensitive(boolean sensitive) {
		setGenericMenusSensitive(sensitive);
	}

	/**
	 * @param sensitive
	 */
	public void setGenericMenusSensitive(boolean sensitive) {
		update.setEnabled(sensitive);
		original.setEnabled(sensitive);
		scaleMenu.setEnabled(sensitive);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("updateImage")) {
			operator.updateImage();
		} else if (e.getActionCommand().equals("original")) {
			displayPanel.original();
		} else if (e.getActionCommand().equals("autoScale")) {
			displayPanel.autoScale();
		} else if (e.getActionCommand().equals("scaleImage 1.0")) {
			displayPanel.scaleImage(1.0);
		} else if (e.getActionCommand().equals("scaleImage 0.5")) {
			displayPanel.scaleImage(0.5);
		}
	}
}