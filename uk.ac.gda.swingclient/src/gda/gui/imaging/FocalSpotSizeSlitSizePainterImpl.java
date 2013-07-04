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

import gda.configuration.properties.LocalProperties;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

/**
 * Paints a box onto an image showing the size of the beam.
 */
public class FocalSpotSizeSlitSizePainterImpl implements FocalSpotSizePainter, SlitSizePainter, BeamSizePainter, AperturePainter {
	
	private Point[] focalSpotSizeCorners;
	
	private Point[] slitSizeCorners;
	
	private Point focalSpotSize = new Point();
	
	private Point slitSize = new Point();
	
	private Point centre;
	
	private int aperture;
	
	private boolean displayFocalSpotSize;
	
	private boolean representFocalSpotSizeAsEllipse;
	
	private boolean displaySlitSize;
	
	private boolean displayBeamSize;
	
	private boolean displayAperture = false;
	
	@Override
	public void setDisplayFocalSpotSize(boolean display) {
		this.displayFocalSpotSize = display;
	}
	
	@Override
	public void setDisplayFocalSpotSizeAsEllipse(boolean display) {
		this.representFocalSpotSizeAsEllipse = display;
	}

	@Override
	public void setDisplaySlitSize(boolean display) {
		this.displaySlitSize = display;
	}
	
	@Override
	public void setDisplayBeamSize(boolean display) {
		this.displayBeamSize = display;
	}
	
	@Override
	public void setDisplayAperture(boolean display) {
		this.displayAperture = display;
	}

	/**
	 * Sets the focal spot position and size.
	 * 
	 * @param topLeft top left corner of the focal spot
	 * @param bottomRight bottom right corner of the focal spot
	 */
	@Override
	public void setFocalSpotSize(Point topLeft, Point bottomRight) {
		if (topLeft == null || bottomRight == null) {
			focalSpotSizeCorners = null;
			setDisplayFocalSpotSize(false);
		} else {
			focalSpotSizeCorners = new Point[5];
			focalSpotSizeCorners[0] = topLeft;
			focalSpotSizeCorners[1] = new Point(topLeft.x, bottomRight.y);
			focalSpotSizeCorners[2] = bottomRight;
			focalSpotSizeCorners[3] = new Point(bottomRight.x, topLeft.y);
			focalSpotSizeCorners[4] = topLeft; // repeat of first point
			focalSpotSize = new Point(bottomRight.x - topLeft.x, bottomRight.y - topLeft.y);
		}
	}
	

	@Override
	public void setSlitSize(Point topLeft, Point bottomRight) {
		if (topLeft == null || bottomRight == null) {
			slitSizeCorners = null;
			setDisplaySlitSize(false);
		} else {
			slitSizeCorners = new Point[5];
			slitSizeCorners[0] = topLeft;
			slitSizeCorners[1] = new Point(topLeft.x, bottomRight.y);
			slitSizeCorners[2] = bottomRight;
			slitSizeCorners[3] = new Point(bottomRight.x, topLeft.y);
			slitSizeCorners[4] = topLeft; // repeat of first point
			slitSize = new Point(bottomRight.x - topLeft.x, bottomRight.y - topLeft.y);
		}
	}

	@Override
	public void setApertureSize(int apertureSize) {
		this.aperture = apertureSize;
	}

	@Override
	public void paint(Graphics g) {
		final Graphics2D g2 = (Graphics2D) g;
		Stroke originalStroke = g2.getStroke();
		RenderingHints originalRenderingHints = g2.getRenderingHints();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setStroke(new BasicStroke(1.5f));

		if (displayFocalSpotSize && focalSpotSizeCorners != null && focalSpotSizeCorners[0] != null) {
			if (representFocalSpotSizeAsEllipse==false) {
				// loop over each line
				for (int i = 0; i < 4; i++) {
					g.drawLine(focalSpotSizeCorners[i].x, focalSpotSizeCorners[i].y,
							focalSpotSizeCorners[i + 1].x, focalSpotSizeCorners[i + 1].y);
				}
			}
			else {
				g.drawOval(focalSpotSizeCorners[0].x,focalSpotSizeCorners[0].y, focalSpotSize.x, focalSpotSize.y);
			}
		}
		Color initialColor = g.getColor();

		if (displaySlitSize && slitSizeCorners != null) {
			g.setColor(Color.BLUE);
			//first, draw the normal slit box
			for (int i = 0; i < 4; i++) {
				g.drawLine(slitSizeCorners[i].x, slitSizeCorners[i].y,
						slitSizeCorners[i + 1].x, slitSizeCorners[i + 1].y);
			}
			g.setColor(initialColor);
		}
		
		Dimension apertureDimension = new Dimension(aperture, aperture);
		Point corner = new Point(centre.x - aperture / 2, centre.y - aperture / 2); //drawn from upper left corner
		Ellipse2D.Double circleAperture = new Ellipse2D.Double();
		circleAperture.setFrame(corner, apertureDimension);

		if (displayAperture) {
			g2.setColor(Color.GREEN);
			g2.draw(circleAperture);
		}

		if (displayBeamSize && slitSizeCorners != null && focalSpotSizeCorners != null) {
			//then draw the dotted intersection of the slits with beam size box/ellipse
			float[] dashes = {5.0F, 2.0F};
			BasicStroke wideAndDottedStroke = new BasicStroke(1.5F, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0F, dashes, 2.0F);
			g2.setStroke(wideAndDottedStroke);
			Rectangle2D.Double rectSlits = new Rectangle2D.Double();
			Rectangle2D.Double rectBeam = new Rectangle2D.Double();
			Ellipse2D.Double ellipseBeam = new Ellipse2D.Double();
			rectSlits.setFrame(slitSizeCorners[0].x, slitSizeCorners[0].y, slitSize.x, slitSize.y);
			ellipseBeam.setFrame(focalSpotSizeCorners[0].x, focalSpotSizeCorners[0].y, focalSpotSize.x, focalSpotSize.y);
			rectBeam.setFrame(focalSpotSizeCorners[0].x, focalSpotSizeCorners[0].y, focalSpotSize.x, focalSpotSize.y);
			Area rectSlitsArea = new Area(rectSlits);
			Area rectBeamArea = new Area (rectBeam);
			Area ellipseBeamArea = new Area(ellipseBeam);
			if (!LocalProperties.check("gda.mx.showSlitSize")) {
				if (representFocalSpotSizeAsEllipse==true) {
					rectSlitsArea = ellipseBeamArea;
				}
				else if (representFocalSpotSizeAsEllipse==false) {
					rectSlitsArea = rectBeamArea;
				}
			}
			else {
				if (representFocalSpotSizeAsEllipse==true) { //(beamSizeCorners != null && beamSizeCorners[0] != null && representBeamSizeAsEllipse==true) {
					rectSlitsArea.intersect(ellipseBeamArea);
				}
				else if (representFocalSpotSizeAsEllipse==false) {//(beamSizeCorners != null && beamSizeCorners[0] != null && representBeamSizeAsEllipse==false) {
					rectSlitsArea.intersect(rectBeamArea);
				}
			}
			
			if (aperture > 0) {
				Area apertureArea = new Area(circleAperture);
				rectSlitsArea.intersect(apertureArea);
			}
			
			g2.setColor(Color.RED);
			g2.draw(rectSlitsArea);
		}
		g2.setColor(initialColor);
		g2.setStroke(originalStroke);
		g2.setRenderingHints(originalRenderingHints);
	}

	public void setCentre(Point centre) {
		this.centre = centre;
	}
}
