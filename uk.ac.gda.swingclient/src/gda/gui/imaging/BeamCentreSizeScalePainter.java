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
 * A {@link SampleImageModifier} that can paint the beam centre crosshair, beam
 * size box, beam scale box, and distance between two points onto an image.
 */
public class BeamCentreSizeScalePainter implements BeamSizePainter, BeamCentrePainter, FocalSpotSizePainter, BeamScalePainter, DistancePainter, SlitSizePainter, AperturePainter {

	private BeamCentrePainterImpl beamCentrePainter;
	
	private FocalSpotSizeSlitSizePainterImpl beamSizeSlitSizePainter;
	
	private BeamScalePainterImpl beamScalePainter;
	
	private DistancePainterImpl distancePainter;
	/**
	 * Creates a sample display object.
	 */
	public BeamCentreSizeScalePainter() {
		beamCentrePainter = new BeamCentrePainterImpl();
		beamSizeSlitSizePainter = new FocalSpotSizeSlitSizePainterImpl();
		beamScalePainter = new BeamScalePainterImpl();
		distancePainter = new DistancePainterImpl();
	}
	
	@Override
	public void setDisplayCrossHair(boolean display) {
		beamCentrePainter.setDisplayCrossHair(display);
	}
	
	@Override
	public void setCentre(Point centre) {
		beamCentrePainter.setCentre(centre);
		beamSizeSlitSizePainter.setCentre(centre);
	}
	
	@Override
	public void setDisplayFocalSpotSize(boolean display) {
		beamSizeSlitSizePainter.setDisplayFocalSpotSize(display);
	}
	
	@Override
	public void setDisplayFocalSpotSizeAsEllipse(boolean display) {
		beamSizeSlitSizePainter.setDisplayFocalSpotSizeAsEllipse(display);
		
	}
	
	@Override
	public void setFocalSpotSize(Point topLeft, Point bottomRight) {
		beamSizeSlitSizePainter.setFocalSpotSize(topLeft, bottomRight);
	}
	
	@Override
	public void setDisplayScale(boolean displayScale) {
		beamScalePainter.setDisplayScale(displayScale);
	}
	
	@Override
	public double getXScale() {
		return beamScalePainter.getXScale();
	}
	
	@Override
	public void setXScale(double scale) {
		beamScalePainter.setXScale(scale);
		distancePainter.setXScale(scale);
	}
	
	@Override
	public double getYScale() {
		return beamScalePainter.getYScale();
	}
	
	@Override
	public void setYScale(double scale) {
		beamScalePainter.setYScale(scale);
		distancePainter.setYScale(scale);
	}

	@Override
	public void setPointsForDistanceCalculation(Point startPoint, Point endPoint) {
		distancePainter.setPointsForDistanceCalculation(startPoint, endPoint);
	}

	@Override
	public void setDisplayDistance(boolean displayDistance) {
		distancePainter.setDisplayDistance(displayDistance);
	}
	
	@Override
	public void paint(Graphics g) {
		beamSizeSlitSizePainter.paint(g);
		beamCentrePainter.paint(g);
		beamScalePainter.paint(g);
		distancePainter.paint(g);
	}

	@Override
	public void setDisplaySlitSize(boolean display) {
		beamSizeSlitSizePainter.setDisplaySlitSize(display);
		
	}

	@Override
	public void setSlitSize(Point topLeft, Point bottomRight) {
		beamSizeSlitSizePainter.setSlitSize(topLeft, bottomRight);
	}
	
	@Override
	public void setDisplayBeamSize(boolean display) {
		beamSizeSlitSizePainter.setDisplayBeamSize(display);
		
	}

	@Override
	public void setApertureSize(int size) {
		beamSizeSlitSizePainter.setApertureSize(size);
	}
	
	@Override
	public void setDisplayAperture(boolean display) {
		beamSizeSlitSizePainter.setDisplayAperture(display);
		
	}
}
