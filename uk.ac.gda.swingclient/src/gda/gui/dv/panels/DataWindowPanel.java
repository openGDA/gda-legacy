/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council
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

package gda.gui.dv.panels;

import gda.gui.dv.ImageData;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.WritableRaster;
import java.util.LinkedList;
import java.util.ListIterator;

import javax.swing.JPanel;
import javax.swing.event.ChangeListener;

/**
 * Panel that provides interaction with the window on the data you can visible move around the window position in the
 * data set
 */

public class DataWindowPanel extends JPanel implements MouseListener, MouseMotionListener {

	private int xDataDim;
	private int yDataDim;
	private int xWindowDim;
	private int yWindowDim;
	private int xWindowPos;
	private int yWindowPos;
	@SuppressWarnings("unused")
	private boolean isDragging = false;
	private BufferedImage overViewImage = null;
	private LinkedList<ChangeListener> listeners = null;

	/**
	 * Default constructor
	 */
	public DataWindowPanel() {
		this.setPreferredSize(new java.awt.Dimension(64, 64));
		xDataDim = 256;
		yDataDim = 256;
		xWindowDim = 256;
		yWindowDim = 256;
		xWindowPos = 0;
		yWindowPos = 0;
		isDragging = false;
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		listeners = new LinkedList<ChangeListener>();
	}

	/**
	 * Set the overview image to this data window panel
	 * 
	 * @param image
	 *            the image data of the overview image
	 */

	public void setOverviewImage(ImageData image) {
		int height = this.getSize().height;
		int width = this.getSize().width;
		float yScale = (float) yDataDim / (float) height;
		float xScale = (float) xDataDim / (float) width;

		overViewImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		WritableRaster raster = overViewImage.getRaster();
		int[] pixels = ((DataBufferInt) raster.getDataBuffer()).getData();
		for (int y = 0; y < height; y++) {
			int realy = (int) (y * yScale);
			for (int x = 0; x < width; x++) {
				int realx = (int) (x * xScale);
				pixels[x + y * width] = image.get(realx, realy);
			}
		}
		System.gc();
		this.repaint();
	}

	/**
	 * Set the absolute data set size
	 * 
	 * @param xDim
	 *            x dimension in pixels
	 * @param yDim
	 *            y dimension in pixels
	 */
	public void setDataSetSize(int xDim, int yDim) {
		xDataDim = xDim;
		yDataDim = yDim;
	}

	/**
	 * Set the maximum window size on the data set
	 * 
	 * @param xWinDim
	 *            x window size in pixels
	 * @param yWinDim
	 *            y window size in pixels
	 */

	public void setWindowSize(int xWinDim, int yWinDim) {
		xWindowDim = xWinDim;
		yWindowDim = yWinDim;
	}

	/**
	 * @return the current window X position in the data set
	 */
	public int getWindowXPos() {
		int compWidth = this.getSize().width;
		int realPos = (int) (xWindowPos * ((float) xDataDim / (float) compWidth));
		return realPos;
	}

	/**
	 * @return the current window Y position in the data set
	 */
	public int getWindowYPos() {
		int compHeight = this.getSize().height;
		int realPos = (int) (yWindowPos * (float) yDataDim / compHeight);
		return realPos;
	}

	@Override
	protected void paintComponent(Graphics g) {

		super.paintComponent(g);
		// Graphics2D graphics = (Graphics2D) g;
		g.setColor(java.awt.Color.white);
		int compWidth = this.getSize().width - 1;
		int compHeight = this.getSize().height - 1;
		int windowXSize = (int) (compWidth * ((float) xWindowDim / (float) xDataDim));
		int windowYSize = (int) (compHeight * ((float) yWindowDim / (float) yDataDim));

		g.fillRect(0, 0, compWidth, compHeight);

		if (overViewImage != null) {
			Graphics2D graphic = (Graphics2D) g;
			graphic.drawImage(overViewImage, null, 0, 0);
		}
		g.setColor(java.awt.Color.red);

		g.drawRect(xWindowPos, yWindowPos, windowXSize, windowYSize);
	}

	@Override
	public void mouseClicked(MouseEvent event) {
		// Nothing to be done

	}

	@Override
	public void mouseEntered(MouseEvent event) {
		// Nothing to be done
	}

	@Override
	public void mouseExited(MouseEvent event) {
		// Nothing to be done

	}

	@Override
	public void mousePressed(MouseEvent event) {
		if (this.isEnabled())
			isDragging = true;
	}

	@Override
	public void mouseReleased(MouseEvent event) {
		if (this.isEnabled()) {
			isDragging = false;
			ListIterator<ChangeListener> iter = listeners.listIterator();
			while (iter.hasNext()) {
				ChangeListener listener = iter.next();
				listener.stateChanged(new javax.swing.event.ChangeEvent(this));
			}
		}
	}

	@Override
	public void mouseDragged(MouseEvent event) {
		if (this.isEnabled()) {
			xWindowPos = event.getX();
			yWindowPos = event.getY();
			int compWidth = this.getSize().width - 1;
			int compHeight = this.getSize().height - 1;
			int windowXSize = (int) (compWidth * ((float) xWindowDim / (float) xDataDim));
			int windowYSize = (int) (compHeight * ((float) yWindowDim / (float) yDataDim));
			if (xWindowPos > compWidth - windowXSize)
				xWindowPos = compWidth - windowXSize;
			if (xWindowPos < 0)
				xWindowPos = 0;
			if (yWindowPos > compHeight - windowYSize)
				yWindowPos = compHeight - windowYSize;
			if (yWindowPos < 0)
				yWindowPos = 0;
			this.repaint();
		}
	}

	@Override
	public void mouseMoved(MouseEvent event) {
		// Nothing to do
	}

	/**
	 * Adds a ChangeListener to the DataWindowPanel
	 * 
	 * @param listener
	 *            new listener that should be added
	 */

	public void addChangeListener(ChangeListener listener) {
		listeners.add(listener);
	}

}
