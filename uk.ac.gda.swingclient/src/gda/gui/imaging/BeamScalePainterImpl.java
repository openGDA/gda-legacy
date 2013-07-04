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

import java.awt.Dimension;
import java.awt.Graphics;
import java.text.DecimalFormat;

import javax.vecmath.Vector2d;

/**
 * Paints a beam scale box onto an image.
 */
public class BeamScalePainterImpl implements BeamScalePainter {
	
	private boolean displayScale = false;
	
	@Override
	public void setDisplayScale(boolean displayScale) {
		this.displayScale = displayScale;
	}
	
	/**
	 * Number of microns per pixel.
	 */
	private Vector2d micronsPerPixel = new Vector2d(1.0, 1.0);
	
	/**
	 * Dimensions of scale box.
	 */
	private Dimension scaleBoxSize = new Dimension(100, 100);
	
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
	public void paint(Graphics g) {
		if (displayScale) {
			int width = scaleBoxSize.width, height = scaleBoxSize.height;
			int x = 854, y = 618; // x and y of far upper left corner of the scale bar area
			int xPoints[] = new int[4];
			int yPoints[] = new int[4];
			xPoints[0] = x;
			yPoints[0] = y;
			xPoints[1] = x;
			yPoints[1] = y + height;
			xPoints[2] = x + width;
			yPoints[2] = y + height;
			xPoints[3] = x + width;
			yPoints[3] = y;
			g.fillPolygon(xPoints, yPoints, 4);
			DecimalFormat scaleFormat = new DecimalFormat("0.0"); // http://leepoint.net/nodes-java/data/strings/conversion/num2string.html
			g.drawString(scaleFormat.format(scaleBoxSize.width  * micronsPerPixel.x) + " μm", x + width / 2 - 10, y + height + 20);
			g.drawString(scaleFormat.format(scaleBoxSize.height * micronsPerPixel.y) + " μm", x + width + 10, y + height / 2);
		}
	}

}
