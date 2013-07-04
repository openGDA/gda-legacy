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

package gda.gui.imaging;

import gda.images.camera.ImageListener;
import gda.images.camera.RTPStreamReceiver;
import gda.observable.IObserver;
import gda.observable.ObservableComponent;
import gda.util.LogPanel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A JPanel which displays a stream of images from an RTP source, but overlays the image with a cross-hair and rectangle
 * to display beam size/position.
 * <p>
 * This probably runs much slower than the RTPReceiver class!
 */
public class RTPCameraImageROISelector extends JLayeredPane implements ImageListener<Image> /*implements ImageROISelector*/ {

	protected static final Logger logger = LoggerFactory.getLogger(RTPCameraImageROISelector.class);
	
	Vector<String> key = null;
	boolean displayCrossHair = false;
	boolean displayCrosshairAtImageCentre = false;
	private Color crosshairColour = Color.red;
	/**
	 * @return displayCrossHairAtImageCentre
	 */
	public boolean isDisplayCrosshairAtImageCentre() {
		return displayCrosshairAtImageCentre;
	}

	/**
	 * @param displayCrosshairAtImageCentre
	 */
	public void setDisplayCrosshairAtImageCentre(
			boolean displayCrosshairAtImageCentre) {
		this.displayCrosshairAtImageCentre = displayCrosshairAtImageCentre;
		if(this.displayCrosshairAtImageCentre)
			setDisplayCrossHair(true);
	}

	/**
	 * @return displayCrosshair
	 */
	public boolean isDisplayCrossHair() {
		return displayCrossHair;
	}

	/**
	 * @param displayCrossHair
	 */
	public void setDisplayCrossHair(boolean displayCrossHair) {
		this.displayCrossHair = displayCrossHair;
	}
	/**
	 * @param colour
	 */
	public void setCrossHairColour(Color colour)
	{
		this.crosshairColour = colour;
	}

	/**
	 * @return centre
	 */
	public Point getCentre() {
		return centre;
	}

	/**
	 * @param centre
	 */
	public void setCentre(Point centre) {
		this.centre = centre;
	}

	boolean displayBeamSize = false;
	boolean displayScale = false;
	Point centre = new Point(0, 0);
	Point[] beamSizeCorners = new Point[5];
	private Image defaultImage = null;
	private double xScale = 1.0, yScale = 1.0; // microns per pixel
	Rectangle clip;
	Line2D line;
	Point pt;
	private int shape;
	/**
	 * 
	 */
	public static int RECTANGLE = 1;
	/**
	 * 
	 */
	public static int LINE = 2;
	/**
	 * 
	 */
	public static int POINT = 3;
	
	private ObservableComponent observableComponent = new ObservableComponent();
	private Dimension imageSize = new Dimension();
	private int crosshairLength = 50;
	
	/**
	 * @return crosshair length
	 */
	public int getCrosshairLength() {
		return crosshairLength;
	}

	/**
	 * @param crosshairLength
	 */
	public void setCrosshairLength(int crosshairLength) {
		this.crosshairLength = crosshairLength;
	}

	/**
	 * Constructor
	 */
	public RTPCameraImageROISelector() {
		super();

		URL url = LogPanel.class.getResource("splashImage.gif");
		if (url != null) {
			defaultImage = Toolkit.getDefaultToolkit().getImage(url);
		}
		clip = new Rectangle();
		line = new Line2D.Double();
		pt = new Point();
		Selector selector = new Selector(this);
		addMouseListener(selector);
		addMouseMotionListener(selector);
	}
	
	/** Are we currently displaying images? */
	protected volatile boolean videoActive;
	
	/** Latest image received from the video received */
	protected volatile Image latestImage;
	
	/** Task that repaints the display */
	protected Runnable repaintTask = new Runnable() {
		@Override
		public void run() {
			repaint();
		}
	};
	
	@Override
	public void processImage(Image image) {
		if (videoActive) {
			latestImage = image;
			SwingUtilities.invokeLater(repaintTask);
		}
	}
	
	public void start() {
		videoActive = true;
	}
	
	public void stop() {
		videoActive = false;
	}
	
	@Override
	public void paintComponent(Graphics g) {
		refreshGraphics(g);
		super.paintComponent(g);
	}
	
	public void refreshGraphics(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		//logger.info("refresh garphics calle");
		int height =0;
		int width =0;
		if (latestImage == null) {
			if (defaultImage != null) {
				height = defaultImage.getWidth(null);
				width = defaultImage.getHeight(null);
				g2.drawImage(defaultImage, 0, 0, getSize().width, getSize().height, 0, 0, width,
						width, null);
				
				//logger.info("setting Image size from default Image " + width + " " + height);
				
			}
		} else {
			g2.drawImage(latestImage, 0, 0, null);
			height = latestImage.getWidth(null);
			width = latestImage.getHeight(null);
			//logger.info("setting Image size from dlatest Image " + width + " " + height);			
		}
		imageSize.setSize(width,height);
		if(displayCrosshairAtImageCentre)
		{
			centre.y = imageSize.width/2;
			centre.x = imageSize.height/2;
			//logger.info("the centre is " + centre.x + " " + centre.y);
		}
		g2.setPaint(this.crosshairColour);
		// paint the cross-hair
		if (displayCrossHair && centre != null) {

			// Now we can compute the corner points...
			int xPoints[] = new int[4];
			int yPoints[] = new int[4];
			int dx = 1; // line thickness
			
			

			if (centre.x < dx) {
				centre.x = dx;
			}
			if (centre.y < dx) {
				centre.y = dx;
			}

			// the vertical cross hair
			xPoints[0] = centre.x + dx;
			yPoints[0] = centre.y - crosshairLength;
			xPoints[1] = centre.x - dx;
			yPoints[1] = centre.y - crosshairLength;
			xPoints[2] = centre.x - dx;
			yPoints[2] = centre.y + crosshairLength;
			xPoints[3] = centre.x + dx;
			yPoints[3] = centre.y + crosshairLength;

			g2.fillPolygon(xPoints, yPoints, 4);
			
			// the horizontal cross hair
			xPoints[0] = centre.x - crosshairLength;
			yPoints[0] = centre.y + dx;
			xPoints[1] = centre.x - crosshairLength;
			yPoints[1] = centre.y - dx;
			xPoints[2] = centre.x + crosshairLength;
			yPoints[2] = centre.y - dx;
			xPoints[3] = centre.x + crosshairLength;
			yPoints[3] = centre.y + dx;

			g2.fillPolygon(xPoints, yPoints, 4);
		}
		g2.setPaint(Color.red);
		if (null != clip && shape == RECTANGLE)
		{
			g2.drawRect(clip.x, clip.y, clip.width, clip.height);
			g2.drawString("("+clip.x+","+clip.y+")", clip.x, clip.y);
			g2.drawString("("+(clip.x+ clip.width)+","+(clip.y+clip.height)+")", (clip.x+ clip.width), (clip.y+clip.height));
		}
		
		// draw(Shape) seems to be verrrrrrrrrrrrrrrry slow
		// g2.draw(clip);

		else if (null != line && shape == LINE){
			// g2.draw(line);
			int x1 = (int) line.getX1();
			int x2 = (int) line.getX2();
			int y1 = (int)line.getY1();
			int y2 = (int) line.getY2();
			g2.drawLine(x1,y1,x2,y2 );
			g2.drawString("("+x1+","+y1+")", x1, y1);
			g2.drawString("("+x2+","+y2+")", x2, y2);
		}
		else if(null != pt && shape == POINT)
		{
			g2.drawString("("+pt.x+","+pt.y+")", pt.x, pt.y);
		}
		g2.dispose();
	}

	/*
	 * @Override public void refreshGraphics(Graphics g) { if (latestImage == null) { if (defaultImage != null) {
	 * g.drawImage(defaultImage, 0, 0, getSize().width, getSize().height, 0, 0, defaultImage.getWidth(null),
	 * defaultImage.getHeight(null), null); } } else { g.drawImage(latestImage, 0, 0, null); }
	 * //g.setXORMode(this.getBackground()); if(startPoint != null && endPoint != null) { if(shape == RECTANGLE)
	 * g.drawRect( Math.min(startPoint.x, endPoint.x), Math.min(startPoint.y, endPoint.y), Math.abs(startPoint.x -
	 * endPoint.x), Math.abs(startPoint.y - endPoint.y)); if(shape == LINE) g.drawLine(Math.min(startPoint.x,
	 * endPoint.x), Math.min(startPoint.y, endPoint.y),Math.max(startPoint.x, endPoint.x),Math.max(startPoint.y,
	 * endPoint.y)); } g.dispose(); }
	 */
	/**
	 * @return Returns the scale.
	 */
	public double getXScale() {
		return xScale;
	}

	/*
	 * public void paintComponent(Graphics g) { super.paintComponent(g); Graphics2D g2 = (Graphics2D)g;
	 * g2.setPaint(Color.red); if(null != clip && shape == RECTANGLE) g2.draw(clip); else if(null != line && shape==
	 * LINE) g2.draw(line); }
	 */
	/**
	 * @param scale
	 *            The scale to set.
	 */
	public void setXScale(double scale) {
		this.xScale = scale;
	}

	/**
	 * @return Returns the scale.
	 */
	public double getYScale() {
		return yScale;
	}

	/**
	 * @param scale
	 *            The scale to set.
	 */
	public void setYScale(double scale) {
		this.yScale = scale;
	}

	/**
	 * @param displayScale
	 *            The displayScale to set.
	 */
	public void setDisplayScale(boolean displayScale) {
		this.displayScale = displayScale;
	}

	/**
	 * @param start
	 * @param end
	 */
	public void setClipFrame(Point start, Point end) {
		if (this.shape == RECTANGLE){
			clip.setFrameFromDiagonal(start, end);
		}
		else if(this.shape == LINE){
			line.setLine(start.x, start.y, end.x, end.y);
		}
		else{
			pt.x = start.x;
			pt.y = start.y;
		}
		repaint();
	}

	/**
	 * @param shape
	 */
	public void setShape(int shape) {
		this.shape = shape;
	}
	
	/**
	 * @return image size
	 */
	public Dimension getImageSize()
	{
		return imageSize;
	}
	
	/**
	 * @param file
	 * @param format
	 */
	public void saveImage(File file, String format)
	{
		try {
			logger.info("Trying to save file " + file.getAbsolutePath() + " of the format " + format );
			if(latestImage != null)
				ImageIO.write((BufferedImage)latestImage, format, file);
			else
				logger.error("No image available");
		} catch (IOException e) {
			logger.error("problem saving the image as " + format + "in file " + file.getAbsolutePath());
		}
	}

	/**
	 * Test harness for this code.
	 * 
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		RTPStreamReceiver receiver = new RTPStreamReceiver();
		receiver.setHost("224.120.120.120");
		receiver.setPort(22224);
		receiver.configure();
		
		RTPCameraImageROISelector imagePanel = new RTPCameraImageROISelector();
		JFrame frame = new JFrame("RTPCameraImageROISelector");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(1024, 768);
		frame.add(imagePanel);
		frame.setVisible(true);
		imagePanel.setXScale(0.9);
		imagePanel.setYScale(1.1);
		imagePanel.setDisplayScale(true);
		
		receiver.addImageListener(imagePanel);

		imagePanel.start();
	}

	public Object getSelectedRegion() {
		Point[] data = null;
		if (shape == RECTANGLE) {
			data = new Point[2];
			//data[0] = new Point((int) clip.getX(), (int) clip.getY());
			data[0] = new Point((int) clip.getMinX(), (int) clip.getMinY());
			data[1] = new Point((int) clip.getMaxX(), (int) clip.getMaxY());
		} else if (shape == LINE) {
			data = new Point[2];
			data[0] = new Point((int) line.getX1(), (int) line.getY1());
			data[1] = new Point((int) line.getX2(), (int) line.getY2());
		}
		return data;

	}

	public void addIObserver(IObserver anIObserver) {
		observableComponent.addIObserver(anIObserver);
	}

	public void deleteIObserver(IObserver anIObserver) {
		observableComponent.deleteIObserver(anIObserver);
	}

	public void deleteIObservers() {
		observableComponent.deleteIObservers();
	}

	/**
	 * Notify all observers on the list of the requested change.
	 * 
	 * @param theObserved
	 *            the observed component
	 * @param theArgument
	 *            the data to be sent to the observer.
	 */
	public void notifyIObservers(Object theObserved, Object theArgument) {
		observableComponent.notifyIObservers(theObserved, theArgument);
	}
}

class Selector extends MouseInputAdapter {
	RTPCameraImageROISelector selectionPanel;
	Point start;
	boolean dragging, isClipSet;

	// private Point end;

	/**
	 * @param isp
	 */
	public Selector(RTPCameraImageROISelector isp) {
		selectionPanel = isp;
		dragging = false;
		isClipSet = false;
	}

	/**
	 * @see java.awt.event.MouseAdapter#mousePressed(java.awt.event.MouseEvent)
	 */
	@Override
	public void mousePressed(MouseEvent e) {

		start = e.getPoint();
		dragging = true;
		if (e.isControlDown())
			selectionPanel.setShape(RTPCameraImageROISelector.LINE);
		else
			selectionPanel.setShape(RTPCameraImageROISelector.RECTANGLE);

	}

	/**
	 * @see java.awt.event.MouseAdapter#mouseReleased(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseReleased(MouseEvent e) {
		dragging = false;
		selectionPanel.notifyIObservers(selectionPanel, selectionPanel.getSelectedRegion());

	}

	/**
	 * @see java.awt.event.MouseAdapter#mouseDragged(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseDragged(MouseEvent e) {
		if (dragging)
			// end = e.getPoint();
			selectionPanel.setClipFrame(start, e.getPoint());

	}
	
	/**
	 * @see java.awt.event.MouseAdapter#mouseDragged(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseClicked(MouseEvent e)
	{
		Point p = e.getLocationOnScreen();
		selectionPanel.setShape(RTPCameraImageROISelector.POINT);
		selectionPanel.setClipFrame(p ,p);
		RTPCameraImageROISelector.logger.info("mouse clicked at " + p.x + " " + p.y);
	}
}
