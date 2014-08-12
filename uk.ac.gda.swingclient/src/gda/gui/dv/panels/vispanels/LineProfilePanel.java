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

package gda.gui.dv.panels.vispanels;

import gda.gui.dv.DoubleBufferedImageData;
import gda.gui.dv.panels.IMainPlotManipulator;
import gda.gui.dv.panels.MainPlot;
import gda.gui.dv.panels.VisPanel;
import gda.gui.util.HandleBox;
import gda.gui.util.HandleBoxes;
import gda.jython.JythonServerFacade;
import gda.plots.SimplePlot;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.image.MemoryImageSource;
import java.util.HashMap;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import uk.ac.diamond.scisoft.analysis.dataset.Dataset;
import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
import uk.ac.diamond.scisoft.analysis.dataset.function.LineSample;

/**
 * Basic class that provides a line profile through the data
 */
public class LineProfilePanel extends VisPanel implements IMainPlotManipulator {

	DoubleBufferedImageData overlay = null;
	DoubleDataset data = null;

	int sx = 0;
	int ex = 0;
	int sy = 0;
	int ey = 0;
	double angle = 0.0;
	int cx = 0; // current position for moving box
	int cy = 0;
	int mx = 0; // midpoint for rotation
	int my = 0;

	boolean dragging = false;
	Cursor mycursor = getCursor();
	Cursor circleCursor = null;
	private int[] lineRGBA = { 255, 255, 0, 180 };
	private int[] outLineRGBA = { 255, 255, 0, 80 };

	private final static int MINSIZEFACTOR = 2; // minimum size factor for determining when to perform profile

	// calculation

	/**
	 * possible handle states
	 */
	private enum HandleStatus {
		/**
		 * Specifies the handle does nothing
		 */
		NONE,
		/**
		 * Specifies the handle is for moving
		 */
		MOVE,
		/**
		 * Specifies the handle is for resizing
		 */
		RESIZE,
		/**
		 * Specifies the handle is for re-orienting
		 */
		REORIENT,
		/**
		 * Specifies the handle is for spinning
		 */
		ROTATE
	}

	HandleStatus hStatus = HandleStatus.NONE;
	HandleBoxes hBoxes = null;

	SimplePlot linePlot = null;
	private static double lineStep = 0.5;

	private JSpinner ssx = null; // start x
	private JSpinner ssy = null; // start y
	private JSpinner sex = null; // end x
	private JSpinner sey = null; // end y
	private JSpinner spa = null; // angle

	private ChangeListener spinnerChangeListener = new ChangeListener() {
		@Override
		public void stateChanged(ChangeEvent e) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					if (!dragging) { // don't update when dragging
						sx = (Integer) ssx.getModel().getValue();
						ex = (Integer) sex.getModel().getValue();
						sy = (Integer) ssy.getModel().getValue();
						ey = (Integer) sey.getModel().getValue();
						angle = (Double) spa.getModel().getValue();
						updatePlot();
						owner.getDataSetImage().repaint();
					}
				}
			});
		}
	};

	private ChangeListener spinnerChangeListenerAngle = new ChangeListener() {
		@Override
		public void stateChanged(ChangeEvent e) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					if (!dragging) { // don't update when dragging
						angle = (Double) spa.getModel().getValue();
						double hypot = Math.hypot(ex - sx, ey - sy);
						ex = (int) (sx + hypot * Math.cos(Math.PI * angle / 180.0));
						ey = (int) (sy + hypot * Math.sin(Math.PI * angle / 180.0));
						sex.getModel().setValue(ex);
						sey.getModel().setValue(ey);
						updatePlot();
						owner.getDataSetImage().repaint();
					}
				}
			});
		}
	};

	// Nasty hack that places the GUI information into the JythonTerminal instance
	HashMap<String, Double> roi = null;
	String roiJythonName = "plot_client_line_roi";

	@SuppressWarnings("unchecked")
	private void updateJythonTerminalNamespace() {
		JythonServerFacade jsf = JythonServerFacade.getInstance();

		if (jsf != null) {
			Object jObj = jsf.getFromJythonNamespace(roiJythonName);
			if (jObj instanceof HashMap) {
				roi = (HashMap<String, Double>) jObj;
			} else {
				roi = new HashMap<String, Double>();
				jsf.placeInJythonNamespace(roiJythonName, roi);
			}
			if (roi != null) {
				roi.put("start x", new Double(sx));
				roi.put("start y", new Double(sy));
				roi.put("end x", new Double(ex));
				roi.put("end y", new Double(ey));
				roi.put("length", new Double(Math.sqrt(Math.pow(ex - sx, 2) + Math.pow(ey - sy, 2))));
				roi.put("angle", new Double(angle));
				jsf.placeInJythonNamespace(roiJythonName, roi);
			}
		}
	}

	/**
	 * constructor which mainly sets out the GUI
	 * 
	 * @param main
	 *            The panel which this one is integrated with.
	 */
	public LineProfilePanel(MainPlot main) {
		super(main);

		setName("Line Profile");

		linePlot = new SimplePlot();
		linePlot.setTitle("Profile of directed line");
		linePlot.setXAxisLabel("Length along line");
		linePlot.setYAxisLabel("Intensity");

		FlowLayout flowlo = new FlowLayout();
		flowlo.setAlignment(FlowLayout.LEFT);

		JPanel pl41 = new JPanel();
		pl41.setLayout(flowlo);
		pl41.add(new JLabel("Start x: ", SwingConstants.RIGHT));
		SpinnerModel smsx = new SpinnerNumberModel(sx, -5000, 5000, 1);
		ssx = new JSpinner(smsx);
		ssx.addChangeListener(spinnerChangeListener);
		pl41.add(ssx);

		JPanel pl42 = new JPanel();
		pl42.setLayout(flowlo);
		pl42.add(new JLabel("End x: ", SwingConstants.RIGHT));
		SpinnerModel smex = new SpinnerNumberModel(ex, -5000, 5000, 1);
		sex = new JSpinner(smex);
		sex.addChangeListener(spinnerChangeListener);
		pl42.add(sex);

		JPanel pl43 = new JPanel();
		pl43.setLayout(flowlo);
		pl43.add(new JLabel("Start y: ", SwingConstants.RIGHT));
		SpinnerModel smsy = new SpinnerNumberModel(sy, -5000, 5000, 1);
		ssy = new JSpinner(smsy);
		ssy.addChangeListener(spinnerChangeListener);
		pl43.add(ssy);

		JPanel pl44 = new JPanel();
		pl44.setLayout(flowlo);
		pl44.add(new JLabel("End y: ", SwingConstants.RIGHT));
		SpinnerModel smey = new SpinnerNumberModel(ey, -5000, 5000, 1);
		sey = new JSpinner(smey);
		sey.addChangeListener(spinnerChangeListener);
		pl44.add(sey);

		JPanel pl45 = new JPanel();
		pl45.setLayout(flowlo);
		pl45.add(new JLabel("Angle: ", SwingConstants.RIGHT));
		SpinnerModel sma = new SpinnerNumberModel(angle, 0., 360., 1.);
		spa = new JSpinner(sma);
		spa.addChangeListener(spinnerChangeListenerAngle);
		pl45.add(spa);

		JPanel pl4 = new JPanel();
		pl4.setLayout(new GridBagLayout());
		pl4.add(pl41, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));
		pl4.add(pl42, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));
		pl4.add(pl43, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));
		pl4.add(pl44, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));
		pl4.add(pl45, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));

		this.setLayout(new BorderLayout());
		this.add(linePlot, BorderLayout.NORTH);
		this.add(pl4, BorderLayout.SOUTH);

		hBoxes = new HandleBoxes();
		circleCursor = createCircleCursor();
	}

	private Cursor createCircleCursor() {
		int curWidth = 32;
		int curHeight = 32;
		int x, y;

		int pix[] = new int[curWidth * curHeight];
		for (y = 0; y <= curHeight; y++)
			for (x = 0; x <= curWidth; x++)
				pix[y + x] = 0; // all points transparent

		// black circle - outside
		int curCol = Color.black.getRGB();
		int yscale = 10;
		int xscale = 10;
		for (x = 2; x <= 8; x++)
			pix[x] = curCol; // up
		for (x = 2; x <= 8; x++)
			pix[(yscale * curWidth) + x] = curCol; // bottom
		for (y = 2; y <= 8; y++)
			pix[curWidth * y] = curCol; // left
		for (y = 2; y <= 8; y++)
			pix[(curWidth * y) + yscale] = curCol; // right
		pix[1 + curWidth] = curCol;
		pix[yscale + curWidth - 1] = curCol;
		pix[1 + (curWidth * (yscale - 1))] = curCol;
		pix[(curWidth * (yscale - 1)) + yscale - 1] = curCol;

		// white circle - inside
		curCol = Color.white.getRGB();
		yscale = yscale - 1;
		xscale = xscale - 1;
		for (x = 3; x <= 7; x++)
			pix[x + curWidth] = curCol; // up
		for (x = 3; x <= 7; x++)
			pix[(yscale * curWidth) + x] = curCol; // bottom
		for (y = 3; y <= 7; y++)
			pix[curWidth * y + 1] = curCol; // left
		for (y = 3; y <= 7; y++)
			pix[(curWidth * y) + yscale] = curCol; // right
		pix[2 + curWidth + curWidth] = curCol;
		pix[yscale + curWidth + curWidth - 1] = curCol;
		pix[1 + (curWidth * (yscale - 1)) + 1] = curCol;
		pix[(curWidth * (yscale - 1)) + yscale - 1] = curCol;

		Image img = createImage(new MemoryImageSource(curWidth, curHeight, pix, 0, curWidth));
		return Toolkit.getDefaultToolkit().createCustomCursor(img, new Point(5, 5), "circle");
	}

	/**
	 * Implemented function which allows the drawing to the image
	 * @param currentOverlay 
	 *            Current overlay
	 * @param image
	 *            The image which is being manipulated
	 * @return a pointer to the overlay which is affecting the data
	 */
	@Override
	public DoubleBufferedImageData getOverlay(DoubleBufferedImageData currentOverlay, DoubleDataset image) {

		// check the size of the data, if its different make a new overlay
		boolean newdata = false;

		if (data != null) {
			// check length of the data
			if (data.getRank() == image.getRank()) {
				// check the dimensions
				for (int i = 0; i < data.getRank(); i++) {
					if (data.getShape()[i] != image.getShape()[i]) {
						newdata = true;
					}
				}
			}
		}
		
		overlay = currentOverlay;
		if ((overlay == null) || (newdata)) {

			overlay = new DoubleBufferedImageData(image.getShape()[1], image.getShape()[0]);

		}

		// if not, simple pass on the pointer to the old one.

		data = image;

		if (!dragging)
			updatePlot(); // don't update whilst we are dragging

		return overlay;

	}

	private void updatePlot() {
		updateOverlay();
		updatePlot(sx, sy, ex, ey);
	}

	private void updatePlot(int ax, int ay, int bx, int by) {
		// sanity check that image is bigger than handle sides
		if (data == null || hBoxes.getHandleside() > MINSIZEFACTOR * data.getShape()[0]
				|| hBoxes.getHandleside() > MINSIZEFACTOR * data.getShape()[1])
			return;

		// first perform the sampling
		LineSample ls = new LineSample(ax, ay, bx, by, lineStep);
		List<? extends Dataset> dsets = ls.value(data);
		DoubleDataset dls = (DoubleDataset) dsets.get(0);

		linePlot.setBatching(true);
		// clear the graph
		linePlot.deleteAllLines();

		linePlot.initializeLine(0);
		linePlot.setLineName(0, "Profile");

		linePlot.setXAxisAutoScaling(true);
		linePlot.setYAxisAutoScaling(true);

		int dist = dls.getShape()[0];
		for (int x = 0; x < dist; x++) {
			linePlot.addPointToLine(0, lineStep * x, dls.get(x));
		}

		linePlot.setBatching(false);

		linePlot.validate();
		linePlot.repaint();
	}

	private void updateOverlay() {
		if (overlay == null)
			return;

		// clear off the old dataset
		overlay.clear();

		// now draw the line
		overlay.drawAALine(sx, sy, ex, ey, lineRGBA);

		// draw arrowhead
		double dx = ex - sx;
		double dy = ey - sy;
		double hypot = Math.hypot(dx, dy);
		dx /= hypot;
		dy /= hypot;

		mx = (sx + ex) / 2;
		my = (sy + ey) / 2;
		overlay.drawArrowhead(mx, my, dx, dy, hBoxes.getHandleside(), (int) 1.5 * hBoxes.getHandleside(), lineRGBA);

		// draw end handles
		int hs = hBoxes.getHandleside() / 2;
		int[] nhboxcoords = { sx - hs, sy - hs, sx + hs, sy + hs, ex - hs, ey - hs, ex + hs, ey + hs };

		hBoxes.addHandleBoxes(nhboxcoords);
		for (HandleBox hb : hBoxes.getHandleBoxList()) {
			int[] hbc = hb.getHandleCoords();

			overlay.drawBoxOutline(hbc[0], hbc[1], hbc[2], hbc[3], hBoxes.getHandlethickness(), outLineRGBA);
		}

		overlay.flipImage();
		updateJythonTerminalNamespace();
	}

	/**
	 * Overloaded function, but not used in this case
	 * 
	 * @param e
	 */
	@Override
	public void mouseClicked(MouseEvent e) {

	}

	/**
	 * Overloaded function, but not used in this case
	 * 
	 * @param e
	 */
	@Override
	public void mouseEntered(MouseEvent e) {

	}

	/**
	 * Overloaded function, but not used in this case
	 * 
	 * @param e
	 */
	@Override
	public void mouseExited(MouseEvent e) {

	}

	/*
	 * Event mask to check for shift key being held on a mouse button event
	 */
	static final int onmask = InputEvent.SHIFT_DOWN_MASK | InputEvent.BUTTON1_DOWN_MASK;

	/**
	 * Overloaded function, Allows drawing to the image
	 * 
	 * @param e
	 */
	@Override
	public void mousePressed(MouseEvent e) {
		if (overlay == null)
			return;
		switch (e.getButton()) {
		case 1:
			if (hBoxes == null || hBoxes.getCurrent() < 0) {
				sx = e.getX();
				sy = e.getY();

				// clear off the old dataset
				overlay.clear();
				overlay.flipImage();

				dragging = true;
				owner.getDataSetImage().setCursor(new Cursor(Cursor.HAND_CURSOR));
			} else if (hBoxes.getCurrent() >= 0) {
				overlay.clear();
				overlay.drawLine(sx, sy, ex, ey, lineRGBA);
				overlay.flipImage();

				if ((e.getModifiersEx() & onmask) != onmask) {
					hStatus = HandleStatus.RESIZE;
				} else {
					hStatus = HandleStatus.REORIENT;
				}
				dragging = true;
			}
			break;
		case 2:
			break;
		case 3:
			mx = (sx + ex) / 2;
			my = (sy + ey) / 2;
			if (hBoxes.getCurrent() >= 0) {
				overlay.clear();
				overlay.drawLine(sx, sy, ex, ey, lineRGBA);
				overlay.flipImage();
				owner.getDataSetImage().setCursor(circleCursor);
				hStatus = HandleStatus.ROTATE;
				dragging = true;
			} else {
				int xlen = Math.abs(sx - ex) / 2;
				int ylen = Math.abs(sy - ey) / 2;
				if (xlen < 2)
					xlen = 1;
				else
					xlen--;
				if (ylen < 2)
					ylen = 1;
				else
					ylen--;
				if (overlay.isInBox(e.getX(), e.getY(), mx - xlen, my - ylen, mx + xlen, my + ylen)) {
					owner.getDataSetImage().setCursor(new Cursor(Cursor.MOVE_CURSOR));
					hStatus = HandleStatus.MOVE;
					dragging = true;
					cx = e.getX();
					cy = e.getY();
				}
			}
			break;
		}

	}

	/**
	 * Overloaded function, Allows drawing to the image
	 * 
	 * @param e
	 */
	@Override
	public void mouseReleased(MouseEvent e) {
		boolean update = false;

		switch (e.getButton()) {
		case 1:
			if (dragging) {
				switch (hStatus) {
				case NONE:
					ex = e.getX();
					ey = e.getY();
					update = true;
					break;
				case RESIZE:
					if (hBoxes.getCurrent() >= 0) {
						switch (hBoxes.getCurrent()) {
						case 0:
							sx = e.getX();
							sy = e.getY();
							break;
						case 1:
							ex = e.getX();
							ey = e.getY();
							break;
						default:
							break;
						}
						update = true;
					}
					break;
				case MOVE:
					break;
				case ROTATE:
					break;
				case REORIENT:
					if (hBoxes.getCurrent() >= 0) {
						double dx = 0.0, dy = 0.0;

						switch (hBoxes.getCurrent()) {
						case 0:
							dx = e.getX() - ex;
							dy = e.getY() - ey;
							break;
						case 1:
							dx = e.getX() - sx;
							dy = e.getY() - sy;
							break;
						default:
							break;
						}
						update = true;
						if (dx != 0 || dy != 0) {
							double f = Math.hypot(ex - sx, ey - sy) / Math.hypot(dx, dy); // normalisation factor
							dx *= f;
							dy *= f;
							switch (hBoxes.getCurrent()) {
							case 0:
								sx = (int) (ex + dx);
								sy = (int) (ey + dy);
								break;
							case 1:
								ex = (int) (sx + dx);
								ey = (int) (sy + dy);
								break;
							default:
								break;
							}
						}
					}
					break;
				}
			}
			break;
		case 2:
			if (dragging) {
				switch (hStatus) {
				case NONE:
					break;
				case RESIZE:
					break;
				case MOVE:
					break;
				case ROTATE:
					break;
				case REORIENT:
					break;
				}
			}
			break;
		case 3:
			if (dragging) {
				switch (hStatus) {
				case MOVE:
					double dx = e.getX() - cx;
					double dy = e.getY() - cy;

					if (sx <= sy) { // SE case
						if (sx + dx < 0) {
							dx = -sx;
						} else if (ex + dx >= overlay.getW()) {
							dx = overlay.getW() - 1 - ex;
						}
					} else { // SW case
						if (ex + dx < 0) {
							dx = -ex;
						} else if (sx + dx >= overlay.getW()) {
							dx = overlay.getW() - 1 - sx;
						}
					}

					if (sy + dy < 0) {
						dy = -sy;
					} else if (ey + dy >= overlay.getH()) {
						dy = overlay.getH() - 1 - ey;
					}
					sx += dx;
					ex += dx;
					sy += dy;
					ey += dy;

					update = true;
					break;
				case NONE:
					break;
				case RESIZE:
					break;
				case ROTATE:
					dx = e.getX() - mx;
					dy = e.getY() - my;
					if (dx != 0 || dy != 0) {
						double f = Math.hypot(ex - sx, ey - sy) * 0.5 / Math.hypot(dx, dy); // normalisation factor
						dx *= f;
						dy *= f;
						switch (hBoxes.getCurrent()) {
						case 0:
							sx = (int) (mx + dx);
							sy = (int) (my + dy);
							ex = (int) (mx - dx);
							ey = (int) (my - dy);
							break;
						case 1:
							sx = (int) (mx - dx);
							sy = (int) (my - dy);
							ex = (int) (mx + dx);
							ey = (int) (my + dy);
							break;
						default:
							break;
						}
					}
					update = true;
					break;
				case REORIENT:
					break;
				}
			}
			break;
		default:
			break;
		}

		if (update) {
			hStatus = HandleStatus.NONE;

			ssx.getModel().setValue(sx);
			sex.getModel().setValue(ex);
			ssy.getModel().setValue(sy);
			sey.getModel().setValue(ey);
			angle = 180.0 * Math.atan2(ey - sy, ex - sx) / Math.PI;
			if (angle < 0)
				angle += 360.0;
			spa.getModel().setValue(angle);

			owner.getDataSetImage().setCursor(mycursor);
			updatePlot();
			dragging = false;
		}
	}

	/**
	 * Overloaded function, Allows drawing to the image
	 * 
	 * @param e
	 */
	@Override
	public void mouseDragged(MouseEvent e) {
		if (overlay == null)
			return;

		int tsx = sx;
		int tsy = sy;
		int tex = ex;
		int tey = ey;
		double dx = 0.0, dy = 0.0;
		if (dragging) {
			// clear off the old dataset
			overlay.clear();

			switch (hStatus) {
			case NONE:
				// use temporary start and end points
				tsx = sx;
				tsy = sy;
				tex = e.getX();
				tey = e.getY();
				break;
			case MOVE:
				dx = e.getX() - cx;
				dy = e.getY() - cy;

				if (sx <= sy) { // SE case
					if (sx + dx < 0) {
						dx = -sx;
					} else if (ex + dx >= overlay.getW()) {
						dx = overlay.getW() - 1 - ex;
					}
				} else { // SW case
					if (ex + dx < 0) {
						dx = -ex;
					} else if (sx + dx >= overlay.getW()) {
						dx = overlay.getW() - 1 - sx;
					}
				}
				if (sy + dy < 0) {
					dy = -sy;
				} else if (ey + dy >= overlay.getH()) {
					dy = overlay.getH() - 1 - ey;
				}
				tsx = (int) (sx + dx);
				tsy = (int) (sy + dy);
				tex = (int) (ex + dx);
				tey = (int) (ey + dy);
				break;
			case RESIZE:
				if (hBoxes.getCurrent() >= 0) {
					switch (hBoxes.getCurrent()) {
					case 0:
						tsx = e.getX();
						tsy = e.getY();
						tex = ex;
						tey = ey;
						break;
					case 1:
						tsx = sx;
						tsy = sy;
						tex = e.getX();
						tey = e.getY();
						break;
					default:
						tsx = sx;
						tsy = sy;
						tex = ex;
						tey = ey;
						break;
					}
				}
				break;
			case ROTATE:
				dx = e.getX() - mx;
				dy = e.getY() - my;
				if (dx != 0 || dy != 0) {
					double f = Math.hypot(ex - sx, ey - sy) * 0.5 / Math.hypot(dx, dy); // normalisation factor
					dx *= f;
					dy *= f;
					switch (hBoxes.getCurrent()) {
					case 0:
						tsx = (int) (mx + dx);
						tsy = (int) (my + dy);
						tex = (int) (mx - dx);
						tey = (int) (my - dy);
						break;
					case 1:
						tsx = (int) (mx - dx);
						tsy = (int) (my - dy);
						tex = (int) (mx + dx);
						tey = (int) (my + dy);
						break;
					default:
						tsx = sx;
						tsy = sy;
						tex = ex;
						tey = ey;
						break;
					}
				}
				break;
			case REORIENT:
				if (hBoxes.getCurrent() >= 0) {

					switch (hBoxes.getCurrent()) {
					case 0:
						dx = e.getX() - ex;
						dy = e.getY() - ey;
						break;
					case 1:
						dx = e.getX() - sx;
						dy = e.getY() - sy;
						break;
					default:
						break;
					}
					if (dx != 0 || dy != 0) {
						double f = Math.hypot(ex - sx, ey - sy) / Math.hypot(dx, dy); // normalisation factor
						dx *= f;
						dy *= f;
						switch (hBoxes.getCurrent()) {
						case 0:
							tsx = (int) (ex + dx);
							tsy = (int) (ey + dy);
							break;
						case 1:
							tex = (int) (sx + dx);
							tey = (int) (sy + dy);
							break;
						default:
							break;
						}
					}
				}
				break;
			}
			// update spinners
			ssx.getModel().setValue(tsx);
			sex.getModel().setValue(tex);
			ssy.getModel().setValue(tsy);
			sey.getModel().setValue(tey);
			double tangle = 180.0 * Math.atan2(tey - tsy, tex - tsx) / Math.PI;
			if (tangle < 0)
				tangle += 360.0;
			spa.getModel().setValue(tangle);

			// now draw the line
			overlay.drawLine(tsx, tsy, tex, tey, lineRGBA);
			overlay.flipImage();
			updatePlot(tsx, tsy, tex, tey);
		}

	}

	/**
	 * Overloaded function, but not used in this case
	 * 
	 * @param e
	 */
	@Override
	public void mouseMoved(MouseEvent e) {
		if (!dragging) {
			if (hStatus == HandleStatus.NONE) {
				if (hBoxes != null) {
					hBoxes.whichHandleBox(e.getX(), e.getY());

					if (hBoxes.getCurrentBox() != null)
						owner.getDataSetImage().setCursor(new Cursor(Cursor.HAND_CURSOR));
					else
						owner.getDataSetImage().setCursor(mycursor);
				}
			}
		}
	}

	@Override
	public void releaseOverlay() {
		overlay = null;
	}

}
