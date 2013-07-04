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

import java.awt.Graphics;
import java.awt.Point;

/**
 * Paints a crosshair onto an image showing the location of the beam.
 */
public class BeamCentrePainterImpl implements BeamCentrePainter {

	private boolean displayCrossHair;
	
	@Override
	public void setDisplayCrossHair(boolean display) {
		this.displayCrossHair = display;
	}
	
	private Point centre = new Point(0, 0);
	
	@Override
	public void setCentre(Point centre) {
		if (centre == null) {
			setDisplayCrossHair(false);
		}
		this.centre = centre;
	}
	
	@Override
	public void paint(Graphics g) {
		if (displayCrossHair && centre != null) {
			
			// Now we can compute the corner points...
			int xPoints[] = new int[4];
			int yPoints[] = new int[4];
			int dx = 1; // line thickness
			int crosshairLength = 50;
			
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
			
			g.fillPolygon(xPoints, yPoints, 4);
			
			// the horizontal cross hair
			xPoints[0] = centre.x - crosshairLength;
			yPoints[0] = centre.y + dx;
			xPoints[1] = centre.x - crosshairLength;
			yPoints[1] = centre.y - dx;
			xPoints[2] = centre.x + crosshairLength;
			yPoints[2] = centre.y - dx;
			xPoints[3] = centre.x + crosshairLength;
			yPoints[3] = centre.y + dx;
			
			g.fillPolygon(xPoints, yPoints, 4);
		}
	}
	
}
