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
 * A {@link SampleImageModifier} that can paint the beam centre crosshair and
 * beam size box onto an image.
 */
public class BeamCentreAndSizePainterImpl implements BeamCentrePainter, FocalSpotSizePainter, BeamSizePainter {

	private BeamCentrePainterImpl beamCentrePainter;
	
	private FocalSpotSizeSlitSizePainterImpl beamSizeSlitSizePainter;
	
	/**
	 * Creates a sample display object.
	 */
	public BeamCentreAndSizePainterImpl() {
		beamCentrePainter = new BeamCentrePainterImpl();
		beamSizeSlitSizePainter = new FocalSpotSizeSlitSizePainterImpl();
	}
	
	@Override
	public void setDisplayCrossHair(boolean display) {
		beamCentrePainter.setDisplayCrossHair(display);
	}
	
	@Override
	public void setCentre(Point centre) {
		beamCentrePainter.setCentre(centre);
	}
	
	@Override
	public void setDisplayBeamSize(boolean display) {
		beamSizeSlitSizePainter.setDisplayBeamSize(display);
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
	public void paint(Graphics g) {
		beamSizeSlitSizePainter.paint(g);
		beamCentrePainter.paint(g);
	}
	
}
