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

package gda.gui.dv;

import org.eclipse.dawnsci.analysis.dataset.coords.RotatedCoords;
import org.eclipse.dawnsci.analysis.dataset.coords.SectorCoords;

/**
 * Class which is like a dataset but contains image data as integers
 * 
 * Make sure all public x,y access is translated to row-major order.
 * This makes access, i.e. position[], in screen coordinates.
 */
public class ImageData {

	int[] data = null;
	/*
	 * Note: x,y => horizontal and vertical (downwards) so on display (0,0) is top left and (w-1,h-1) is bottom right
	 * Also store image so (x,y) is mapped to (r,c), i.e. index = x + y*w = c + r*w
	 */

	int w = 0;
	int h = 0;

	/**
	 * Constructor which generates the basic system with the right size data.
	 * 
	 * @param width
	 * @param height
	 */
	public ImageData(int width, int height) {
		w = width;
		h = height;
		data = new int[w * h];
	}

	/**
	 * Constructor which creates the object with the right size and data intact
	 * 
	 * @param width
	 * @param height
	 * @param datain
	 */
	public ImageData(int width, int height, int[] datain) {
		w = width;
		h = height;
		data = datain;
	}

	/**
	 * getter for the data
	 * 
	 * @return the data integer array
	 */
	public int[] getData() {
		return data;
	}

	/**
	 * setter for all the data
	 * 
	 * @param data
	 */
	public void setData(int[] data) {
		this.data = data;
	}

	/**
	 * getter for the height
	 * 
	 * @return the height
	 */
	public int getH() {
		return h;
	}

	/**
	 * setter for the height
	 * 
	 * @param h
	 */
	public void setH(int h) {
		this.h = h;
	}

	/**
	 * getter for the width
	 * 
	 * @return the width
	 */
	public int getW() {
		return w;
	}

	/**
	 * setter for the width
	 * 
	 * @param w
	 */
	public void setW(int w) {
		this.w = w;
	}

	/**
	 * Sets the value at a position
	 * 
	 * @param value
	 * @param position
	 */
	public void set(int value, int position) {
		data[position] = value;
	}

	/**
	 * gets the value at a position
	 * 
	 * @param position
	 * @return the value at that point
	 */
	public int get(int position) {
		return data[position];
	}

	/**
	 * gets the value at x and y
	 * 
	 * @param x
	 * @param y
	 * @return the value at that point
	 */
	public int get(int x, int y) {
		return data[x + y * w];
	}

	/**
	 * gets an integer array of the [R,G,B,A] values at a point
	 * 
	 * @param position
	 *            the position of the data in the dataset
	 * @return the [R,G,B,A] integer array all from 0-255
	 */
	public int[] getRGBA(int[] position) {
		int[] result = new int[4];

		// Sanity check
		if (position[0] < 0)
			position[0] = 0;
		if (position[0] >= w)
			position[0] = w - 1;
		if (position[1] < 0)
			position[1] = 0;
		if (position[1] >= h)
			position[1] = h - 1;

		int image = data[position[0] + position[1] * w];
		result[0] = image & 0xff;
		result[1] = (image >>  8) & 0xff;
		result[2] = (image >> 16) & 0xff;
		result[3] = (image >> 24) & 0xff;

		return result;
	}

	/**
	 * sets an integer value [R,G,B,A] at a point
	 * 
	 * @param RGBA
	 * @param position
	 */
	private void setRGBA(int[] RGBA, int[] position) {
		if (position[0] >= 0 && position[0] < w && position[1] >= 0 && position[1] < h) {
			// first generate the full integer
			int val = (RGBA[0] & 0xff);
			val += (RGBA[1] & 0xff) << 8;
			val += (RGBA[2] & 0xff) << 16;
			val += (RGBA[3] & 0xff) << 24;
			data[position[0] + position[1] * w] = val;
		}
	}

	/**
	 * sets an integer value [R,G,B,A] at a point
	 * 
	 * @param RGBA
	 * @param position
	 */
	@SuppressWarnings("unused")
	private void setRGBA(int[] RGBA, int position) {
		if (position >= 0 && position < data.length) {
			// first generate the full integer
			int val = (RGBA[0] & 0xff);
			val += (RGBA[1] & 0xff) << 8;
			val += (RGBA[2] & 0xff) << 16;
			val += (RGBA[3] & 0xff) << 24;
			data[position] = val;
		}
	}

	/**
	 * Sets the whole matrix to zero
	 */
	public void clear() {
		for (int i = 0; i < data.length; i++) {
			data[i] = 0;
		}
	}

	/**
	 * Draws pixel
	 * 
	 * @param x
	 * @param y
	 * @param RGBA
	 */
	public void drawPixel(double x, double y, int[] RGBA) {
		int[] pos = { (int) Math.round(x), (int) Math.round(y) };
		setRGBA(RGBA, pos);
	}

	/**
	 * Function that draws line across a dataset
	 * 
	 * @param startX
	 * @param startY
	 * @param endX
	 * @param endY
	 * @param RGBA
	 */
	public void drawLine(int startX, int startY, int endX, int endY, int[] RGBA) {
		// use DDA

		double vx = endX - startX;
		double vy = endY - startY;

		drawPixel(startX, startY, RGBA);

		if (Math.abs(vx) >= Math.abs(vy)) {
			if (startX > endX) {
				int t = endX;
				endX = startX;
				startX = t;
				t = endY;
				endY = startY;
				startY = t;
			}

			double m = vy / vx;
			double ry = startY;
			for (int x = startX + 1; x < endX; x++) {
				ry += m;
				drawPixel(x, ry, RGBA);
			}
		} else {
			if (startY > endY) {
				int t = endX;
				endX = startX;
				startX = t;
				t = endY;
				endY = startY;
				startY = t;
			}

			double m = vx / vy;
			double rx = startX;

			for (int y = startY + 1; y < endY; y++) {
				rx += m;
				drawPixel(rx, y, RGBA);
			}
		}
		drawPixel(endX, endY, RGBA);
	}

	/**
	 * Draws interpolated pixels in X direction
	 * 
	 * @param x
	 * @param y
	 * @param right
	 * @param RGBA
	 */
	private void drawInterpolatedXPixels(double x, double y, boolean right, int[] RGBA) {
		int ix = (int) x;
		double r = x - ix;
		int[] pos = { ix, (int) Math.floor(y) };
		double a = r * RGBA[3];
		int[] aRGBA = { RGBA[0], RGBA[1], RGBA[2], (int) a };
		int[] bRGBA = { RGBA[0], RGBA[1], RGBA[2], (int) (RGBA[3] - a) };
	
		// System.out.printf("dIXP: (%d,%d) %g %d\n", ix, iy, r, (int) (r*A));
		if (right) {
			setRGBA(bRGBA, pos);
	
			pos[0]++;
			setRGBA(aRGBA, pos);
		} else {
			pos[0]++; // need an offset for leftward line
			setRGBA(aRGBA, pos);
	
			pos[0]--;
			setRGBA(bRGBA, pos);
		}
	}

	/**
	 * Draws interpolated pixels in Y direction
	 * 
	 * @param x
	 * @param y
	 * @param down
	 * @param RGBA
	 */
	private void drawInterpolatedYPixels(double x, double y, boolean down, int[] RGBA) {
		int iy = (int) Math.floor(y);
		int[] pos = { (int) Math.floor(x), iy };
		double r = y - iy;
		double a = r * RGBA[3];
		int[] aRGBA = { RGBA[0], RGBA[1], RGBA[2], (int) a };
		int[] bRGBA = { RGBA[0], RGBA[1], RGBA[2], (int) (RGBA[3] - a) };
	
		// System.out.printf("dIYP: (%d,%d) %g %g %d %d\n", ix, iy, r, a, (int) (a), (int) (A-a));
		if (down) {
			setRGBA(bRGBA, pos);
	
			pos[1]++;
			setRGBA(aRGBA, pos);
		} else {
			pos[1]++; // need an offset for upward line
			setRGBA(aRGBA, pos);
	
			pos[1]--;
			setRGBA(bRGBA, pos);
		}
	}

	/**
	 * Function that draws an anti-aliased line across a dataset
	 * 
	 * @param startX
	 * @param startY
	 * @param endX
	 * @param endY
	 * @param RGBA
	 */
	public void drawAALine(int startX, int startY, int endX, int endY, int[] RGBA) {
		// use DDA

		double vx = endX - startX;
		double vy = endY - startY;

		if (Math.abs(vx) >= Math.abs(vy)) {
			if (startX > endX) {
				int t = endX;
				endX = startX;
				startX = t;
				t = endY;
				endY = startY;
				startY = t;
			}
			drawPixel(startX, startY, RGBA);

			double m = vy / vx;
			if (m == 0) {
				for (int x = startX + 1; x < endX; x++) {
					drawPixel(x, startY, RGBA);
				}
			} else {
				double ry = startY;
				boolean down = (m > 0);
				for (int x = startX + 1; x < endX; x++) {
					ry += m;
					drawInterpolatedYPixels(x, ry, down, RGBA);
				}
			}
		} else {
			if (startY > endY) {
				int t = endX;
				endX = startX;
				startX = t;
				t = endY;
				endY = startY;
				startY = t;
			}
			drawPixel(startX, startY, RGBA);

			double m = vx / vy;
			if (m == 0) {
				for (int y = startY + 1; y < endY; y++) {
					drawPixel(startX, y, RGBA);
				}
			} else {
				double rx = startX;
				boolean right = (m > 0);

				for (int y = startY + 1; y < endY; y++) {
					rx += m;
					drawInterpolatedXPixels(rx, y, right, RGBA);
				}
			}
		}
		drawPixel(endX, endY, RGBA);
	}

	/**
	 * @param centreX
	 * @param centreY
	 * @param radius
	 * @param RGBA
	 */
	public void drawCircle(int centreX, int centreY, int radius, int[] RGBA) {
		drawPixel(centreX + radius, centreY, RGBA);
		drawPixel(centreX - radius, centreY, RGBA);
		drawPixel(centreX, centreY + radius, RGBA);
		drawPixel(centreX, centreY - radius, RGBA);
		int y = 0;
		double xs = radius * radius;
		double x = Math.sqrt(xs);
		while (y <= x) {
			y++;
			xs -= 2.0 * y + 1.0;
			x = Math.sqrt(xs);
			drawPixel(centreX + x, centreY + y, RGBA);
			drawPixel(centreX - x, centreY + y, RGBA);
			drawPixel(centreX + x, centreY - y, RGBA);
			drawPixel(centreX - x, centreY - y, RGBA);
			drawPixel(centreX + y, centreY + x, RGBA);
			drawPixel(centreX - y, centreY + x, RGBA);
			drawPixel(centreX + y, centreY - x, RGBA);
			drawPixel(centreX - y, centreY - x, RGBA);
		}
	}

	/**
	 * @param centreX
	 * @param centreY
	 * @param radius
	 * @param sAngle
	 * @param eAngle
	 * @param RGBA
	 */
	public void drawArc(int centreX, int centreY, int radius, double sAngle, double eAngle, int[] RGBA) {
		if (sAngle < 360.0) {
			if (eAngle > 360.0) {
				drawArc(centreX, centreY, radius, sAngle, 360.0, RGBA);
				drawArc(centreX, centreY, radius, 0, eAngle-360.0, RGBA);
				return;
			}
		} else {
			sAngle -= 360.0;
			eAngle -= 360.0;
		}
		if (sAngle <= 0.0 && 0.0 <= eAngle)
			drawPixel(centreX + radius - 1, centreY, RGBA);
		if (sAngle <= 180.0 && 180.0 <= eAngle)
			drawPixel(centreX - radius + 1, centreY, RGBA);
		if (sAngle <= 90.0 && 90.0 <= eAngle)
			drawPixel(centreX, centreY + radius - 1, RGBA);
		if (sAngle <= 270.0 && 270.0 <= eAngle)
			drawPixel(centreX, centreY - radius + 1, RGBA);

		int y = 0;
		double xs = radius * radius;
		double x = radius;
		double angle;
		while (y <= x) {
			y++;
			xs -= 2.0 * y + 1.0;
			x = Math.sqrt(xs);
			angle = (180.0 / Math.PI) * Math.atan2(y, x);
			if (angle < 0)
				angle += 360.0;

			if (sAngle <= angle && angle <= eAngle)
				drawPixel(centreX + x, centreY + y, RGBA);
			if (sAngle <= 90.0 - angle && 90.0 - angle <= eAngle)
				drawPixel(centreX + y, centreY + x, RGBA);
			if (sAngle <= 90.0 + angle && 90.0 + angle <= eAngle)
				drawPixel(centreX - y, centreY + x, RGBA);
			if (sAngle <= 180.0 - angle && 180.0 - angle <= eAngle)
				drawPixel(centreX - x, centreY + y, RGBA);
			if (sAngle <= 180.0 + angle && 180.0 + angle <= eAngle)
				drawPixel(centreX - x, centreY - y, RGBA);
			if (sAngle <= 270.0 - angle && 270.0 - angle <= eAngle)
				drawPixel(centreX - y, centreY - x, RGBA);
			if (sAngle <= 270.0 + angle && 270.0 + angle <= eAngle)
				drawPixel(centreX + y, centreY - x, RGBA);
			if (sAngle <= 360.0 - angle && 360.0 - angle <= eAngle)
				drawPixel(centreX + x, centreY - y, RGBA);
		}
	}

	/**
	 * Draws marker for centre of sector
	 * 
	 * @param x
	 * @param y
	 * @param radius
	 * @param RGBA
	 */
	public void drawCentreMarker(int x, int y, int radius, int[] RGBA) {
		drawCircle(x, y, radius, RGBA);
		drawLine(x - radius + 1, y, x + radius - 1, y, RGBA);
		drawLine(x, y - radius + 1, x, y + radius - 1, RGBA);
	}

	/**
	 * Function that draws an arrow
	 * @param x 
	 * @param y 
	 * @param dx 
	 * @param dy 
	 * @param len 
	 * @param width 
	 * @param RGBA 
	 */
	public void drawArrowhead(int x, int y, double dx, double dy, int len, int width, int[] RGBA) {
		double perpx = -0.5*width*dy; // coordinate offsets of tips from midpoint
		double perpy = 0.5*width*dx;
		double tipx = x - len*dx; // midpoint coordinates of tips 
		double tipy = y - len*dy;
		drawAALine(x, y, (int) (tipx+perpx), (int) (tipy+perpy), RGBA);
		drawAALine(x, y, (int) (tipx-perpx), (int) (tipy-perpy), RGBA);
	}
	
	
	/**
	 * Function that draws a box across the image
	 * 
	 * @param left
	 * @param top
	 * @param right
	 * @param bottom
	 * @param RGBA
	 */
	public void drawBox(int left, int top, int right, int bottom, int[] RGBA) {
		for (int y = top; y <= bottom; y++) {
			for (int x = left; x <= right; x++) {
				int[] pos = { x, y };
				setRGBA(RGBA, pos);
			}
		}

	}

	/**
	 * Function that draws outline of a box (of given thickness) around the image
	 * 
	 * @param left
	 * @param top
	 * @param right
	 * @param bottom
	 * @param thickness
	 * @param RGBA
	 */
	public void drawBoxOutline(int left, int top, int right, int bottom, int thickness, int[] RGBA) {
		int[] pos = { left, top };

		for (int dy = 0; dy < thickness; dy++) {
			pos[1] = top + dy;
			for (int x = left; x <= right; x++) {
				pos[0] = x;
				setRGBA(RGBA, pos);
			}
		}
		for (int dy = 0; dy < thickness; dy++) {
			pos[1] = bottom + 1 - thickness + dy;
			for (int x = left; x <= right; x++) {
				pos[0] = x;
				setRGBA(RGBA, pos);
			}
		}
		for (int y = top; y <= bottom; y++) {
			pos[1] = y;
			for (int dx = 0; dx < thickness; dx++) {
				pos[0] = left + dx;
				setRGBA(RGBA, pos);
			}
			for (int dx = 0; dx < thickness; dx++) {
				pos[0] = right + 1 - thickness + dx;
				setRGBA(RGBA, pos);
			}
		}
	}

	/**
	 * Function that checks whether current coordinates are in given box
	 * 
	 * @param x
	 * @param y
	 * @param left
	 * @param top
	 * @param right
	 * @param bottom
	 * @return boolean
	 */
	public boolean isInBox(int x, int y, int left, int top, int right, int bottom) {
		return (x >= left && x < right && y >= top && y < bottom);
	}

	/**
	 * Function that draws a box across the image
	 * @param angle 
	 * @param sx 
	 * @param sy 
	 * @param h 
	 * @param w 
	 * @param RGBA
	 */
	public void drawRotatedBox(double angle, int sx, int sy, int h, int w, int[] RGBA) {
		RotatedCoords rc = new RotatedCoords(angle);
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				double[] pos = rc.transformToOriginal(x, y);
				drawPixel(sx + pos[0], sy + pos[1], RGBA);
			}
		}

	}

	/**
	 * Function that draws outline of a box (of given thickness) around the image
	 * @param angle 
	 * @param sx 
	 * @param sy 
	 * @param h 
	 * @param w 
	 * @param thickness
	 * @param RGBA
	 */
	public void drawRotatedBoxOutline(double angle, int sx, int sy, int h, int w, int thickness, int[] RGBA) {
		RotatedCoords rc = new RotatedCoords(angle);
		for (int dy = 0; dy < thickness; dy++) {
			for (int x = 0; x < w; x++) {
				double[] sp = rc.transformToOriginal(x, dy);
				drawPixel(sx + sp[0], sy + sp[1], RGBA);
				sp = rc.transformToOriginal(x, h + 1 - thickness + dy);
				drawPixel(sx + sp[0], sy + sp[1], RGBA);
			}
		}
		for (int y = 0; y < h; y++) {
			for (int dx = 0; dx < thickness; dx++) {
				double[] sp = rc.transformToOriginal(dx, y);
				drawPixel(sx + sp[0], sy + sp[1], RGBA);
				sp = rc.transformToOriginal(w + 1 - thickness + dx, y);
				drawPixel(sx + sp[0], sy + sp[1], RGBA);
			}
		}
	}

	/**
	 * Function that draws a sector across the image
	 * 
	 * @param cx
	 * @param cy
	 * @param sr
	 * @param sp
	 * @param er
	 * @param ep
	 * @param RGBA
	 */
	public void drawSector(int cx, int cy, double sr, double sp, double er, double ep, int[] RGBA) {
		// need to work out delta phi from larger radius
		double dp = 0.5 * 180.0 / (Math.PI * er);

		for (double r = sr; r < er; r += 1.0) {
			for (double p = sp; p < ep; p += dp) {
				SectorCoords sc = new SectorCoords(r, p, false);
				double[] rc = sc.getCartesian();
				drawPixel(cx + rc[0], cy + rc[1], RGBA);
			}
		}

	}

	/**
	 * @param cx
	 * @param cy
	 * @param sr
	 * @param sp
	 * @param er
	 * @param ep
	 * @param thickness
	 * @param RGBA
	 */
	public void drawSectorOutline(int cx, int cy, double sr, double sp, double er, double ep, int thickness, int[] RGBA) {

		drawSector(cx, cy, sr, sp, sr + thickness, ep, RGBA);
		drawSector(cx, cy, er - thickness, sp, er, ep, RGBA);

		double dp = 180.0 * thickness / (Math.PI * er);
		drawSector(cx, cy, sr, sp, er, sp + dp, RGBA);
		drawSector(cx, cy, sr, ep - dp, er, ep, RGBA);

	}

	/**
	 * Check if a point is in a given sector
	 * 
	 * @param x
	 * @param y
	 * @param cx
	 * @param cy
	 * @param sr
	 * @param sp
	 * @param er
	 * @param ep
	 * @return boolean
	 */
	public boolean isInSector(int x, int y, int cx, int cy, double sr, double sp, double er, double ep) {
		SectorCoords sc = new SectorCoords(x - cx, y - cy, true);
		double[] psc = sc.getPolar();
		return (psc[0] >= sr && psc[0] < er && psc[1] >= sp && psc[1] < ep);
	}

}
