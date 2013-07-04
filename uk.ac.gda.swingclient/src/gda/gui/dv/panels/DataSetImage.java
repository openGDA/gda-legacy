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

package gda.gui.dv.panels;

import gda.gui.dv.DoubleBufferedImageData;
import gda.gui.dv.ImageData;
import gda.plots.SimplePlot;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.WritableRaster;
import java.text.DecimalFormat;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSplitPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;

/**
 * 
 *
 */
public class DataSetImage extends JPanel implements MouseListener, MouseMotionListener, MouseWheelListener {
	private static final Logger logger = LoggerFactory.getLogger(DataSetImage.class);

	int pos = 0;

	/**
	 * The buffered image used by the Image Panel
	 */
	public BufferedImage buffImage = null;

	/**
	 * This is the raw data that is passed in when the information is passed from the server to the client, hence why it
	 * is public.
	 */
	// this has been converted to a dataset
	// public double[] pix = null;
	public DoubleDataset pix = null;

	/**
	 * This is the RGB values for the actual drawn image., now with a new datatype
	 */
	private ImageData image = null;

	/**
	 * The overlay pointer.
	 */
	public DoubleBufferedImageData overlay = null;

	private IMainPlotVisualiser visualiser = null;

	private IMainPlotManipulator manipulator = null;

	private int xOffset = 0;
	private int yOffset = 0;

	int pixHeight = 0;

	int pixWidth = 0;

	int colourCast = 1;

	/**
	 * image height
	 */
	public int imageHeight = 0;
	/**
	 * image width
	 */
	public int imageWidth = 0;

	/**
	 * 
	 */
	public double zoom = 1.0;

	int StartDragx = 0;
	int StartDragy = 0;
	boolean Dragging = false;

	JPopupMenu popup = null;

	JPanel bottomPanel = null;

	JPanel imagePanel = null;

	ImagePanel imagepan = null;

	JLabel posLabel = null;
	JLabel zoomLabel = null;

	JSplitPane rightPanel = null;

	SimplePlot topPlot = null;
	SimplePlot botPlot = null;

	JSplitPane splitter = null;

	Point lineStart = null;
	Point lineEnd = null;

	Point realStart = null;
	Point realEnd = null;

	boolean drawingLine = false;
	boolean drawingBox = false;

	JRadioButtonMenuItem rbLine = null;
	JRadioButtonMenuItem rbArea = null;

	ButtonGroup groupSelectStyle = null;

	MainPlot overlayManager = null;

	DecimalFormat twoPlaces = new DecimalFormat("0.00");

	/**
	 * Constructor
	 * 
	 * @param overlayMaster
	 */
	public DataSetImage(MainPlot overlayMaster) {
		super();

		overlayManager = overlayMaster;

		setLayout(new BorderLayout());

		bottomPanel = new JPanel(new BorderLayout());

		posLabel = new JLabel("Position");
		zoomLabel = new JLabel("Zoom = 100%");
		bottomPanel.add(posLabel, BorderLayout.WEST);
		bottomPanel.add(zoomLabel, BorderLayout.EAST);

		imagepan = new ImagePanel(this);

		imagepan.addMouseListener(this);
		imagepan.addMouseMotionListener(this);
		imagepan.addMouseWheelListener(this);

		imagepan
		.setToolTipText("Use the middle mouse button to move the image, and the scrollwheel to zoom in and out");

		this.add(imagepan, BorderLayout.CENTER);

		this.add(bottomPanel, BorderLayout.PAGE_END);

	}

	/**
	 * 
	 */
	public void dispose() {
		removeAll();
		imagepan.removeMouseListener(this);
		imagepan.removeMouseMotionListener(this);
		imagepan.removeMouseWheelListener(this);
		imagepan = null;
		bottomPanel.removeAll();
		bottomPanel = null;
	}

	/**
	 * This function applies the colour cast which is specified by the IMainPlotVisualiser which can be accessed by set
	 * and get Visualiser
	 */
	public void applyColorCast() {

		try {
			// this should now all be replaced with the new functionality
			if (pix != null) {
				image = visualiser.cast(pix);
			}

			// it would also be useful to suggest a GC at this point
			System.gc();

		} catch (Throwable e) {
			// This should not happen, be we suspect it does
			logger.error("Something wrong in the colour cast "+visualiser.toString()+": ", e);
		}
	}

	/**
	 * Basically this function takes a mouse event and changes the coordinates to the real ones.
	 * 
	 * @param e
	 *            the mouse event to sanitise
	 */
	private void sanitiseMouseEvent(MouseEvent e) {

		int realX = (int) ((e.getX() - xOffset) / zoom + pixWidth / 2.0);
		int realY = (int) ((e.getY() - yOffset) / zoom + pixHeight / 2.0);

		if (realX < 0) {
			realX = 0;
		}
		if (realY < 0) {
			realY = 0;
		}
		if (realX >= pixWidth) {
			realX = pixWidth - 1;
		}
		if (realY >= pixHeight) {
			realY = pixHeight - 1;
		}

		e.translatePoint(-e.getX(), -e.getY());
		e.translatePoint(realX, realY);

	}

	@Override
	public void mouseClicked(MouseEvent e) {

		// perform all the normal movement first

		// then sanitise the event and push it on to the manipulator if it exists.
		if (manipulator != null) {
			sanitiseMouseEvent(e);
			manipulator.mouseClicked(e);
			imagepan.repaint();
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {

		// perform all the normal movement first

		// then sanitise the event and push it on to the manipulator if it exists.
		if (manipulator != null) {
			sanitiseMouseEvent(e);
			manipulator.mouseEntered(e);
			imagepan.repaint();
		}
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// perform all the normal movement first

		// then sanitise the event and push it on to the manipulator if it exists.
		if (manipulator != null) {
			sanitiseMouseEvent(e);
			manipulator.mouseExited(e);
			imagepan.repaint();
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {

		// perform all the normal movement first
		if (e.getButton() == 2) {
			Dragging = true;
			StartDragx = e.getX();
			StartDragy = e.getY();
		}

		// then sanitise the event and push it on to the manipulator if it exists.
		if (manipulator != null) {
			sanitiseMouseEvent(e);
			manipulator.mousePressed(e);
			imagepan.repaint();
		}

	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// perform all the normal movement first
		if (e.getButton() == 2 && Dragging) {
			Dragging = false;

			xOffset += e.getX() - StartDragx;
			yOffset += e.getY() - StartDragy;

			imagepan.repaint();
		}

		// then sanitise the event and push it on to the manipulator if it exists.
		if (manipulator != null) {
			sanitiseMouseEvent(e);
			manipulator.mouseReleased(e);
			imagepan.repaint();
		}

	}

	@Override
	public void mouseDragged(MouseEvent e) {

		// perform all the normal movement first
		if (Dragging) {
			xOffset += e.getX() - StartDragx;
			yOffset += e.getY() - StartDragy;
			StartDragx = e.getX();
			StartDragy = e.getY();

			imagepan.repaint();
		}

		// then sanitise the event and push it on to the manipulator if it exists.
		if (manipulator != null) {
			sanitiseMouseEvent(e);
			manipulator.mouseDragged(e);
			imagepan.repaint();
		}

	}

	@Override
	public void mouseMoved(MouseEvent e) {

		// perform all the normal movement first

		int realX = (int) ((e.getX() - xOffset) / zoom + pixWidth / 2.0);
		int realY = (int) ((e.getY() - yOffset) / zoom + pixHeight / 2.0);

		if (realX >= 0 && realX < pixWidth && realY >= 0 && realY < pixHeight) {
			double val = pix.get(realY, realX);
			posLabel.setText("Position = (" + realX + "," + realY + ")  Intensity = " + String.format("%5.5g",val)+" ");
		} else {
			posLabel.setText("Position ");
		}

		// then sanitise the event and push it on to the manipulator if it exists.
		if (manipulator != null) {
			sanitiseMouseEvent(e);
			manipulator.mouseMoved(e);
			imagepan.repaint();
		}

	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {

		// perform all the normal movement first

		double oldZoom = zoom;

		// set the zoom part of the process.
		if (e.getWheelRotation() <= 0) {
			zoom *= 1.1;
		} else {
			zoom /= 1.1;
		}

		int mouseX = e.getX();
		int mouseY = e.getY();

		int Ax = mouseX - xOffset;
		int Ay = mouseY - yOffset;

		double Bx = Ax / oldZoom;
		double By = Ay / oldZoom;

		int Cx = (int) (Bx * zoom);
		int Cy = (int) (By * zoom);

		int Dx = xOffset + Cx;
		int Dy = yOffset + Cy;

		int Ex = mouseX - Dx;
		int Ey = mouseY - Dy;

		xOffset += Ex;
		yOffset += Ey;

		imagepan.repaint();

		// now update the information on the bottom bar
		zoomLabel.setText("Zoom = " + twoPlaces.format(zoom * 100) + "%");

	}

	class ImagePanel extends JPanel {

		JPanel owner = null;

		/**
		 * @param Owner
		 */
		public ImagePanel(JPanel Owner) {
			owner = Owner;
		}

		@Override
		protected void paintComponent(Graphics g) {

			super.paintComponent(g);

			Graphics2D graphic = (Graphics2D) g;

			int zoomWidth = (int) (pixWidth * zoom);
			int zoomHeight = (int) (pixHeight * zoom);

			int clippedWidth = zoomWidth;
			int clippedHeight = zoomHeight;

			int clippedXOffset = 0;
			int clippedYOffset = 0;

			if (xOffset - (int) (zoomWidth / 2.0) < 0) {
				clippedXOffset = -(xOffset - (int) (zoomWidth / 2.0));
				clippedWidth = zoomWidth - clippedXOffset;
			}

			if (xOffset + (int) (zoomWidth / 2.0) > this.getBounds().width) {
				clippedWidth -= xOffset + (int) (zoomWidth / 2.0) - this.getBounds().width;
			}

			if (yOffset - (int) (zoomHeight / 2.0) < 0) {
				clippedYOffset = -(yOffset - (int) (zoomHeight / 2.0));
				clippedHeight = zoomHeight - clippedYOffset;
			}

			if (yOffset + (int) (zoomHeight / 2.0) > this.getBounds().height) {
				clippedHeight -= yOffset + (int) (zoomHeight / 2.0) - this.getBounds().height;
			}

			// System.out.println("ClippedWidth = " + clippedWidth + " ClippedHeight = " + clippedHeight +"\n");

			if (clippedWidth * clippedHeight > 0) {

				BufferedImage bi = new BufferedImage(clippedWidth, clippedHeight, BufferedImage.TYPE_INT_RGB);

				WritableRaster raster = bi.getRaster();

				// this should mean that the dataset pointer which is held here
				// is now updated with the latest information about the overlay
				overlayManager.getOverlay();

				int[] pixels = ((DataBufferInt) raster.getDataBuffer()).getData();
				for (int j = 0; j < clippedHeight; j++) {
					int realy = (int) ((j + clippedYOffset) / zoom);
					for (int i = 0; i < clippedWidth; i++) {
						int realx = (int) ((i + clippedXOffset) / zoom);

						// this simply states if there is an overlay to apply it
						// over the top of the underlying image data
						if(image != null) {
							if (overlay == null) {


								pixels[i + j * clippedWidth] = image.get(realx, realy);


							} else {
								if (overlay.get(realx, realy) == 0) {
									pixels[i + j * clippedWidth] = image.get(realx, realy);
								} else {
									int[] pos = { realx, realy };
									// pixels[i+j*clippedWidth] =
									// blend(overlay.get(realx+realy*pixWidth),image.get(realx+realy*pixWidth),0.5);
									pixels[i + j * clippedWidth] = RGBBlend(image, overlay, pos);
								}
							}
						}
					}
				}

				graphic.drawImage(bi, xOffset - (int) (zoomWidth / 2.0) + clippedXOffset, yOffset
						- (int) (zoomHeight / 2.0) + clippedYOffset, null);

			}

		}

	}

	private int RGBBlend(ImageData image, DoubleBufferedImageData overlay, int[] position) {
		int[] irgba = image.getRGBA(position);
		int[] orgba = overlay.getRGBA(position);

		double opacity = (orgba[3] & 0xff) / 256.0;

		int r = (int) ((irgba[0] & 0xff) * (1-opacity) + (orgba[0] & 0xff) * opacity);
		int g = (int) ((irgba[1] & 0xff) * (1-opacity) + (orgba[1] & 0xff) * opacity);
		int b = (int) ((irgba[2] & 0xff) * (1-opacity) + (orgba[2] & 0xff) * opacity);

		return r + (g << 8) + (b << 16);
	}

	/**
	 * @return pix height
	 */
	public int getPixHeight() {
		return pixHeight;
	}

	/**
	 * @param pixHeight
	 */
	public void setPixHeight(int pixHeight) {
		this.pixHeight = pixHeight;
	}

	/**
	 * @return pix width
	 */
	public int getPixWidth() {
		return pixWidth;
	}

	/**
	 * @param pixWidth
	 */
	public void setPixWidth(int pixWidth) {
		this.pixWidth = pixWidth;
	}

	/**
	 * Gets the visualiser which is to be used for colour-casting the image
	 * 
	 * @return the visualiser which is in use.
	 */
	public IMainPlotVisualiser getVisualiser() {
		return visualiser;
	}

	/**
	 * Sets the visualiser to use, and refreshes the image to reflect the changes.
	 * 
	 * @param visualiser
	 */
	public void setVisualiser(IMainPlotVisualiser visualiser) {
		this.visualiser = visualiser;
		// now repaint the imagepanel
		imagepan.repaint();
	}

	/**
	 * Gets the current manipulator which is being used by the image panel
	 * 
	 * @return The manipulator panel being used
	 */
	public IMainPlotManipulator getManipulator() {
		return manipulator;
	}

	/**
	 * Sets the manipulator to be used by the image panel
	 * 
	 * @param manipulator
	 *            the new manipulator panel
	 */
	public void setManipulator(IMainPlotManipulator manipulator) {
		this.manipulator = manipulator;
	}

	/**
	 * Getter
	 * 
	 * @return value
	 */
	public int getXOffset() {
		return xOffset;
	}

	/**
	 * Setter
	 * 
	 * @param offset
	 */
	public void setXOffset(int offset) {
		xOffset = offset;
	}

	/**
	 * Getter
	 * 
	 * @return value
	 */
	public int getYOffset() {
		return yOffset;
	}

	/**
	 * Setter
	 * 
	 * @param offset
	 */
	public void setYOffset(int offset) {
		yOffset = offset;
	}

}
