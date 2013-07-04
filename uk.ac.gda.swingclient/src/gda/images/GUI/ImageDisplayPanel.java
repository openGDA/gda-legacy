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
import gda.observable.IObservableJPanel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generic Image display panel (understands *.gif, *.jpg and *.bmp files) Optional java properties : boolean String
 * gda.images.monitorMouseClicks Flag set to "true" or "false". If "true", user mouse clicks will be monitored on the
 * image display panel, this position is displayed and the point marked with cross-hairs. Default "true"
 */

@SuppressWarnings("serial")
public class ImageDisplayPanel extends IObservableJPanel implements ImageMenuObserver, MouseListener {
	
	private static final Logger logger = LoggerFactory.getLogger(ImageDisplayPanel.class);
	
	protected BufferedImage originalImage;

	protected BufferedImage displayedImage;

	protected double xscale = 1.0;

	protected double yscale = 1.0;

	protected boolean fitToFrame = true;

	protected Rectangle clear;

	protected int imageWidth, imageHeight;

	protected AffineTransform transform;

	protected JPanel saveChooserPanel;

	protected double x = 0;

	protected double y = 0;

	protected boolean xDefined = false;

	protected boolean monitorMouseClicks;

	protected ImageOperator operator;

	/**
	 * @param monitorMouseClicks
	 *            monitor user mouse clicks and display as cross-hairs on image if true
	 */
	public ImageDisplayPanel(boolean monitorMouseClicks) {
		this.monitorMouseClicks = monitorMouseClicks;
		setBackground(Color.white);
		transform = new AffineTransform();
		if (monitorMouseClicks)
			addMouseListener(this);
	}

	/**
	 * @param operator
	 */
	public void initialise(ImageOperator operator) {
		this.operator = operator;
		fitToFrame = true;
		if (operator != null) {
			operator.updateImage();
		}
	}

	/*
	 * fit image to display panel by using scale factors
	 */
	@Override
	public void autoScale() {
		fitToFrame = true;
		repaint();
	}

	/**
	 * @return the displayed BufferedImage
	 */
	public BufferedImage getDisplayedImage() {
		return displayedImage;
	}

	/**
	 * get image scale factor in x direction
	 * 
	 * @return xscale scale factor in x direction
	 */
	public double getXscale() {
		return xscale;
	}

	/**
	 * get image scale factor in y direction
	 * 
	 * @return yscale scale factor in y direction
	 */
	public double getYscale() {
		return yscale;
	}

	/*
	 * re-display first displayed image and scale it to fill the display panel
	 */
	@Override
	public void original() {
		// displayedImage = Utilities.makeBufferedImage(originalImage);
		displayedImage = originalImage;
		setAutoScale(true);
		repaint();
	}

	@Override
	protected void paintComponent(final Graphics g) {
		if (displayedImage == null) {
			logger.debug("imageDisplayPanel, error NULL image !");
		} else {
			final BufferedImage image = displayedImage;
			final Graphics2D g2 = (Graphics2D) g;
			g2.setPaint(getBackground());
			imageWidth = getWidth();
			imageHeight = getHeight();

			g2.fill(getBounds());
			g2.transform(transform);

			if (fitToFrame) {
				xscale = getWidth() / ((double) image.getWidth());
				yscale = getHeight() / ((double) image.getHeight());
			}
			g2.scale(xscale, yscale);
			final int length = 6;

			g2.drawImage(displayedImage, 0, 0, null);
			if (xDefined) {
				g2.setColor(Color.yellow);
				g2.drawLine((int) x - length, (int) y, (int) x + length, (int) y);
				g2.drawLine((int) x, (int) y - length, (int) x, (int) y + length);
			}

			// FIXME - this doesn't work
			// SwingUtilities.invokeLater(new Runnable()
			// {
			// public void run()
			// {
			// g2.drawImage(image, 0, 0, null);
			// if (xDefined)
			// {
			// g2.setColor(Color.yellow);
			// g2.drawLine((int) x - length, (int) y, (int) x + length,
			// (int) y);
			// g2.drawLine((int) x, (int) y - length, (int) x, (int) y
			// + length);
			// }
			// }
			// });
		}
	}

	/*
	 * scale image w.r.t. the size of the original image recieved / read from file @param scale value to multiply image
	 * size by in both X & Y directions
	 */
	@Override
	public void scaleImage(double scale) {
		xscale = yscale = scale;
		fitToFrame = false;
		repaint();
	}

	/**
	 * @param fitToFrame
	 */
	public void setAutoScale(boolean fitToFrame) {
		this.fitToFrame = fitToFrame;
	}

	/**
	 * display image in display panel
	 * 
	 * @param image
	 *            BufferedImage to be displayed
	 */
	public void displayImage(BufferedImage image) {
		if (image != null) {
			if (originalImage == null)
				originalImage = image;

			displayedImage = image;
			setPreferredSize(new Dimension(displayedImage.getWidth(), displayedImage.getHeight()));
			clear = new Rectangle(0, 0, displayedImage.getWidth(), displayedImage.getHeight());
			repaint();
		}
	}

	@Override
	public void updateImage() {
		// For simple image display, this may involove reading the image from
		// file
		// For more complicated systems it may involve getting an image from a
		// camera.
		// Implementation of ImageOperator will do whatever is necessary and
		// request
		// the captured image to be displayed in here.
		logger.debug(" ImageDisplayPanel.updateimage()");
		if (operator != null) {
			operator.updateImage();
		}
	}

	// MouseListener methods
	@Override
	public void mouseClicked(MouseEvent me) {
		if (monitorMouseClicks) {
			xDefined = true;
			x = me.getX() / xscale;
			y = me.getY() / yscale;
			Point2D.Double point = new Point2D.Double(x, y);
			// double pointArray[] = { x, y };

			notifyIObservers(this, point);

			// display cross-hairs at point where mouse clicked
			repaint();
		}
	}

	@Override
	public void mouseEntered(MouseEvent me) {
		// not implemented
	}

	@Override
	public void mouseExited(MouseEvent me) {
		// not implemented
	}

	@Override
	public void mousePressed(MouseEvent me) {
		// not implemented
	}

	@Override
	public void mouseReleased(MouseEvent me) {
		// not implemented
	}
}
