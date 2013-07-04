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

import gda.device.DeviceException;
import gda.gui.mx.samplechanger.SampleDisplay;
import gda.icons.GdaIcons;
import gda.images.camera.ImageListener;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.media.Buffer;
import javax.media.Player;
import javax.media.control.FrameGrabbingControl;
import javax.media.format.VideoFormat;
import javax.media.util.BufferToImage;
import javax.swing.JLayeredPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A JPanel which displays a stream of images from an RTP source, but overlays the image with a cross-hair and rectangle
 * to display beam size/position.
 * <p>
 * This probably runs much slower than the RTPReceiver class!
 */
public class RTPCameraClient extends JLayeredPane implements SampleDisplay, ImageListener<Image> {

	private static final Logger logger = LoggerFactory.getLogger(RTPCameraClient.class);
	
	protected volatile Image latestImage;
	
	Vector<String> key = null;
	
	private BeamCentreSizeScalePainter imageModifier;
	
	private Image defaultImage = null;
	
	private boolean flipImageHorizontally;
	
	/**
	 * Constructor
	 */
	public RTPCameraClient() {
		super();
		defaultImage = GdaIcons.getLogo(128);
		imageModifier = new BeamCentreSizeScalePainter();
	}
	
	protected int desiredFrameRate = 25;
	
	@Override
	public BeamCentreSizeScalePainter getImageModifier() {
		return imageModifier;
	}
	
	/**
	 * Sets whether the image should be flipped horizontally.
	 * 
	 * @param flipImageHorizontally {@code true} to flip the image
	 */
	public void setFlipImageHorizontally(boolean flipImageHorizontally) {
		this.flipImageHorizontally = flipImageHorizontally;
	}

	/**
	 * Grabs an image from the specified player.
	 * 
	 * @param player the player
	 * 
	 * @return the grabbed image
	 * 
	 * @throws DeviceException
	 * @throws IOException
	 */
	public static BufferedImage getImage(Player player) throws DeviceException, IOException {
		FrameGrabbingControl fgc = (FrameGrabbingControl) player.getControl(FrameGrabbingControl.class.getName());
		
		if (fgc == null) {
			throw new DeviceException("Couldn't capture image - RTP source not ready");
		}
		
		Buffer buf = fgc.grabFrame();
		
		if (buf == null || buf.getData() == null) {
			throw new DeviceException("Couldn't capture image - frame contains no data");
		}
		
		if (System.getProperty("os.name").startsWith("Windows") || buf.getData() instanceof int[]) {
			VideoFormat vf = (VideoFormat)buf.getFormat();
			BufferToImage btoi = new BufferToImage(vf);
			return (BufferedImage) btoi.createImage(buf);
		}
		
		return ImageIO.read(new ByteArrayInputStream((byte[]) buf.getData()));
	}
	
	@Override
	public void run() {
		// do nothing
	}

	/**
	 * Sets the list of keys to be displayed on the image.
	 * 
	 * @param key list of keys
	 */
	public void setKey(Vector<String> key) {
		this.key = new Vector<String>();
		for (String s : key) {
			this.key.add(s);
		}
	}

	@Override
	public void processImage(Image image) {
		if (updateTimer != null) {
			this.latestImage = image;
		}
	}
	
	@Override
	public void paintComponent(Graphics g) {
		refreshGraphics(g);
		super.paintComponent(g);
	}

	public void refreshGraphics(Graphics g) {
		
		final Image imageToBeDisplayed = latestImage != null ? latestImage : defaultImage;
		
		final Point topLeftOfImageRegion = new Point(0, 0);
		final Dimension sizeOfImageRegion = getDimensionOfImage(imageToBeDisplayed);
		
		paintRegionOfImage(imageToBeDisplayed, topLeftOfImageRegion, sizeOfImageRegion, g);
		
		imageModifier.paint(g);
		
		if (key != null) {
			java.awt.FontMetrics metrics = g.getFontMetrics();
			int fontHeight = metrics.getHeight();
			int x = 0;
			for (String s : key) {
				g.drawString(s, x, 0);
				x += fontHeight * 1.5;
			}
		}
	}
	
	protected void paintRegionOfImage(Image image, Point topLeftOfImageRegion, Dimension sizeOfImageRegion, Graphics g) {
		final Point bottomRightOfImageRegion = addPointAndDimension(topLeftOfImageRegion, sizeOfImageRegion);
		
		final Point topLeftOfDisplayArea = new Point (0, 0);
		final Dimension sizeOfDisplayArea = getImagePane().getPreferredSize();
		final Point bottomRightOfDisplayArea = addPointAndDimension(topLeftOfDisplayArea, sizeOfDisplayArea);
		
		if (flipImageHorizontally) {
			topLeftOfDisplayArea.x = bottomRightOfDisplayArea.x;
			bottomRightOfDisplayArea.x = 0;
		}
		
		g.drawImage(image,
			topLeftOfDisplayArea.x, topLeftOfDisplayArea.y,
			bottomRightOfDisplayArea.x, bottomRightOfDisplayArea.y,
			topLeftOfImageRegion.x, topLeftOfImageRegion.y,
			bottomRightOfImageRegion.x, bottomRightOfImageRegion.y,
			null);
	}
	
	private Dimension getDimensionOfImage(Image image) {
		return new Dimension(image.getWidth(null), image.getHeight(null));
	}
	
	private Point addPointAndDimension(Point p, Dimension d) {
		return new Point(p.x + d.width, p.y + d.height);
	}

	@Override
	public JLayeredPane getImagePane() {
		return this;
	}

	protected Timer updateTimer;
	
	private static final Object START_STOP_LOCK = new Object();
	
	@Override
	public void start() {
		synchronized (START_STOP_LOCK) {
			if (updateTimer == null) {
				logger.info("Starting capture");
				int period = 1000 / desiredFrameRate;
				String timerName = String.format("RTPCameraClient(period=%dms)", period);
				updateTimer = new Timer(timerName);
				TimerTask task = createImageUpdateTask();
				updateTimer.scheduleAtFixedRate(task, 0, period);
			}
		}
	}
	
	protected TimerTask createImageUpdateTask() {
		return new TimerTask() {
			@Override
			public void run() {
				repaint();
			}
		};
	}

	@Override
	public void stop() {
		synchronized (START_STOP_LOCK) {
			if (updateTimer != null) {
				logger.info("Stopping capture");
				updateTimer.cancel();
				updateTimer = null;
			}
		}
	}

}
