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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;

import javax.vecmath.Vector2d;
import java.lang.Math;
import java.text.DecimalFormat;
/**
 * Paints a box onto an image showing the size of the beam.
 */
public class DistancePainterImpl implements DistancePainter {

	private boolean showDistance = false;
	private Point[] beamSizeCorners;
	/**
	 * Number of microns per pixel.
	 */
	private Vector2d micronsPerPixel = new Vector2d(1.0, 1.0);
	
	@Override
	public void setPointsForDistanceCalculation(Point startPoint, Point endPoint) {
		beamSizeCorners= new Point[2];
		beamSizeCorners[0] = startPoint;
		beamSizeCorners[1] = endPoint;
	}
	@Override
	public double getXScale() {
		return micronsPerPixel.x;
	}
	
	@Override
	public void setXScale(double scale) {
		this.micronsPerPixel.x = scale;
	}
	
	@Override
	public double getYScale() {
		return micronsPerPixel.y;
	}
	
	@Override
	public void setYScale(double scale) {
		this.micronsPerPixel.y = scale;
	}
	
	@Override
	public void setDisplayDistance(boolean showDistance) {
		this.showDistance = showDistance;
	}
	
	/**
	 * Calculates the distance between two points using the known visual system distance scales in X and Y
	 * @param p1
	 * @param p2
	 * @return distance
	 */
	private double getDistance(Point p1, Point p2) {
		if (p1==null) {
			throw new NullPointerException("p1 is null");
		} else if (p2==null) {
			throw new NullPointerException("p2 is null");
		}
		return Math.sqrt(Math.pow(((p1.getX()-p2.getX())*getXScale()),2)
				+Math.pow(((p1.getY()-p2.getY())*getYScale()),2));
	}
	
	/**
	 * Find the coordinates of the middle point between two points. Integer coordinates.
	 * @param p1
	 * @param p2
	 * @return midlinePoint(integer coordinates)
	 */
	private Point findMidline(Point p1, Point p2) {
		if (p1==null) {
			throw new NullPointerException("p1 is null");
		} else if (p2==null) {
			throw new NullPointerException("p2 is null");
		}
		return new Point((int)(p1.getX() + p2.getX())/2, (int)(p1.getY() + p2.getY())/2);
	}
	@Override
	public void paint(Graphics g) {
		if (showDistance) {
			if (beamSizeCorners != null && beamSizeCorners[0] != null) {
				Color oldColor= g.getColor();
				g.setColor(Color.RED);
				g.drawLine(beamSizeCorners[0].x, beamSizeCorners[0].y,
						beamSizeCorners[1].x, beamSizeCorners[1].y);
				Point midline = findMidline(beamSizeCorners[0], beamSizeCorners[1]);
				DecimalFormat scaleFormat = new DecimalFormat("0.0"); // http://leepoint.net/nodes-java/data/strings/conversion/num2string.html
				g.drawString(scaleFormat.format(getDistance(beamSizeCorners[0], beamSizeCorners[1]))+" μm", (int) midline.getX(), (int) midline.getY());
				g.setColor(oldColor);
			}
			}
	}
}