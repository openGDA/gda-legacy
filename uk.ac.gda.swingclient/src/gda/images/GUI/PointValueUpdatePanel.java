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

import gda.configuration.properties.LocalProperties;
import gda.observable.IObserver;
import gda.px.pxgen.MultiLineToolTip;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Point2D;
import java.text.NumberFormat;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Generic class to update and display a point value (x,y) in a JPanel Java Properties: gda.images.mouseClickLabel:
 * String; optional; default "mouse-click posn" label to be displayed next to position of user mouse-click
 */

@SuppressWarnings("serial")
public class PointValueUpdatePanel extends JPanel implements IObserver, MouseListener {
	JTextField xTextField = new JTextField("            ");

	JTextField yTextField = new JTextField("            ");

	double x, y;

	private String panelName;

	protected MultiLineToolTip xTextFieldToolTip;

	protected MultiLineToolTip yTextFieldToolTip;

	protected int toolTipOffsetX = 40;

	protected int toolTipOffsetY = 5;

	// private boolean toolTips;

	/**
	 * Constructor
	 */
	public PointValueUpdatePanel() {
		this(null, true);
	}

	/**
	 * Constructor
	 * 
	 * @param title
	 */
	public PointValueUpdatePanel(String title) {
		this(title, true);
	}

	/**
	 * Constructor
	 * 
	 * @param title
	 * @param toolTips
	 */
	public PointValueUpdatePanel(String title, boolean toolTips) {
		super(new GridLayout(1, 3));
		configure();
		if (title != null)
			this.panelName = title;

		// this.toolTips = toolTips;

		JPanel xPanel = new JPanel(new BorderLayout());
		JPanel yPanel = new JPanel(new BorderLayout());

		JLabel xLabel = new JLabel(" X = ");
		xPanel.add(xLabel, BorderLayout.WEST);
		xPanel.add(xTextField, BorderLayout.CENTER);

		JLabel yLabel = new JLabel(" Y = ");
		yPanel.add(yLabel, BorderLayout.WEST);
		yPanel.add(yTextField, BorderLayout.CENTER);

		JLabel panelLabel = new JLabel(panelName);
		this.add(panelLabel);
		this.add(xPanel);
		this.add(yPanel);

		if (toolTips)
			setToolTips();
	}

	/**
	 * configure method
	 */
	public void configure() {
		panelName = LocalProperties.get("gda.images.mouseClickLabel", "mouse-click posn ");
	}

	protected void setToolTips() {
		xTextFieldToolTip = new MultiLineToolTip("Position of user mouse-click \n" + "in Pixels, in x direction\n"
				+ "(displayed with yellow cross-hairs)");
		xTextField.addMouseListener(this);
		yTextFieldToolTip = new MultiLineToolTip("Position of user mouse-click \n" + "in Pixels, in y direction\n"
				+ "(displayed with yellow cross-hairs)");
		yTextField.addMouseListener(this);
	}

	/**
	 * @param point
	 */
	public void updateValues(Point2D.Double point) {
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(1);

		xTextField.setText(nf.format((new Double(point.getX()))).toString());
		yTextField.setText(nf.format((new Double(point.getY()))).toString());
	}

	// IObserver Interface

	@Override
	public void update(Object observed, Object arg) {
		if (arg instanceof Point2D.Double) {
			updateValues((Point2D.Double) arg);
		}
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {

	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		if (arg0.getComponent() == xTextField) {
			xTextFieldToolTip.setLocation(xTextField.getLocationOnScreen().x + toolTipOffsetX, xTextField
					.getLocationOnScreen().y
					+ xTextField.getHeight() + toolTipOffsetY);
			xTextFieldToolTip.setVisible(true);
		} else if (arg0.getComponent() == yTextField) {
			yTextFieldToolTip.setLocation(yTextField.getLocationOnScreen().x + toolTipOffsetX, yTextField
					.getLocationOnScreen().y
					+ yTextField.getHeight() + toolTipOffsetY);
			yTextFieldToolTip.setVisible(true);
		}
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		if (arg0.getComponent() == xTextField) {
			xTextFieldToolTip.setVisible(false);
		} else if (arg0.getComponent() == yTextField) {
			yTextFieldToolTip.setVisible(false);
		}
	}

	@Override
	public void mousePressed(MouseEvent arg0) {

	}

	@Override
	public void mouseReleased(MouseEvent arg0) {

	}
}