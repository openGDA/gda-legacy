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

import java.awt.Color;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.image.MemoryImageSource;
import java.util.HashMap;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.eclipse.dawnsci.analysis.dataset.coords.RotatedCoords;
import org.eclipse.dawnsci.analysis.dataset.impl.function.MapToRotatedCartesian;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DoubleDataset;
import org.eclipse.january.dataset.Maths;

import gda.gui.dv.DoubleBufferedImageData;
import gda.gui.dv.panels.IMainPlotManipulator;
import gda.gui.dv.panels.MainPlot;
import gda.gui.dv.panels.VisPanel;
import gda.gui.util.HandleBox;
import gda.gui.util.HandleBoxes;
import gda.jython.JythonServerFacade;
import gda.plots.SimplePlot;
import uk.ac.diamond.scisoft.analysis.dataset.function.Integrate2D;

/**
 * This panel is designed to provide the most simple integration methods for the Image visualisation toolkit
 */
public class BasicIntegratorPanel extends VisPanel implements IMainPlotManipulator, ActionListener {

	DoubleBufferedImageData overlay = null;
	DoubleDataset data = null;

	int startx = 0;
	int starty = 0;
	int height = 0;
	int width = 0;
	double angle = 0.0;
	int curx = 0; // current position for moving box
	int cury = 0;

	boolean dragging = false;
	Cursor mycursor = getCursor();
	Cursor circleCursor;
	private int[] boxRGBA = { 0, 255, 0, 100 };
	private int[] outBoxRGBA = { 0, 255, 0, 180 };

	private final static int MINSIZEFACTOR = 2; // minimum size factor for determining when to perform profile
	// calculation

	int boxthickness = 4;

	/**
	 * possible handle states
	 */
	private enum HandleStatus {
		/**
		 * Specifies the the handle does nothing
		 */
		NONE,
		/**
		 * Specifies that the handle is for moving
		 */
		MOVE,
		/**
		 * Specifies that the handle is for resizing
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

	SimplePlot xPlot = null;
	SimplePlot yPlot = null;

	private JSpinner spx = null; // start x
	private JSpinner spy = null; // start y
	private JSpinner spw = null; // width
	private JSpinner sph = null; // height
	private JSpinner spa = null; // angle
	private ChangeListener spinnerChangeListener = new ChangeListener() {
		@Override
		public void stateChanged(ChangeEvent e) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					if (!dragging) { // don't update when dragging
						startx = (Integer) spx.getModel().getValue();
						starty = (Integer) spy.getModel().getValue();
						height = (Integer) sph.getModel().getValue();
						width = (Integer) spw.getModel().getValue();
						angle = (Double) spa.getModel().getValue();
						updatePlot();
						owner.getDataSetImage().repaint();
					}
				}
			});
		}
	};

	// Nasty hack that places the GUI information into the JythonTerminal instance
	HashMap<String, Double> roi = null;
	String roiJythonName = "plot_client_b_int_roi";

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
				roi.put("start x", new Double(startx));
				roi.put("start y", new Double(starty));
				roi.put("height", new Double(height));
				roi.put("width", new Double(width));
				roi.put("angle", new Double(angle));
				jsf.placeInJythonNamespace(roiJythonName, roi);
			}
		}
	}

	boolean clippingCompensation = false;

	/**
	 * Constructor for the class, this needs to take the MainPlot which is associated as an argument This function
	 * mainly lays out the GUI
	 *
	 * @param main
	 *            the MainPlot that this manipulator is linked to
	 */
	public BasicIntegratorPanel(MainPlot main) {
		super(main);

		setName("Integrator");

		xPlot = new SimplePlot();
		xPlot.setTitle("Major Axis Integration");
		xPlot.setXAxisLabel("Length along minor axis");
		xPlot.setYAxisLabel("Intensity");

		yPlot = new SimplePlot();
		yPlot.setTitle("Minor Axis Integration");
		yPlot.setXAxisLabel("Length along major axis");
		yPlot.setYAxisLabel("Intensity");

		FlowLayout flowlo = new FlowLayout();
		flowlo.setAlignment(FlowLayout.LEFT);

		JPanel pl41 = new JPanel();
		pl41.setLayout(flowlo);
		pl41.add(new JLabel("Start x: ", SwingConstants.RIGHT));
		SpinnerModel smx = new SpinnerNumberModel(startx, -5000, 5000, 1);
		spx = new JSpinner(smx);
		spx.addChangeListener(spinnerChangeListener);
		pl41.add(spx);

		JPanel pl42 = new JPanel();
		pl42.setLayout(flowlo);
		pl42.add(new JLabel("Width: ", SwingConstants.RIGHT));
		SpinnerModel smw = new SpinnerNumberModel(width, -5000, 5000, 1);
		spw = new JSpinner(smw);
		spw.addChangeListener(spinnerChangeListener);
		pl42.add(spw);

		JPanel pl43 = new JPanel();
		pl43.setLayout(flowlo);
		pl43.add(new JLabel("Start y: ", SwingConstants.RIGHT));
		SpinnerModel smy = new SpinnerNumberModel(starty, 0, 5000, 1);
		spy = new JSpinner(smy);
		spy.addChangeListener(spinnerChangeListener);
		pl43.add(spy);

		JPanel pl44 = new JPanel();
		pl44.setLayout(flowlo);
		pl44.add(new JLabel("Height: ", SwingConstants.RIGHT));
		SpinnerModel smh = new SpinnerNumberModel(height, 0, 5000, 1);
		sph = new JSpinner(smh);
		sph.addChangeListener(spinnerChangeListener);
		pl44.add(sph);

		JPanel pl45 = new JPanel();
		pl45.setLayout(flowlo);
		pl45.add(new JLabel("Angle: ", SwingConstants.RIGHT));
		SpinnerModel sma = new SpinnerNumberModel(angle, 0., 360., 1.);
		spa = new JSpinner(sma);
		spa.addChangeListener(spinnerChangeListener);
		pl45.add(spa);

		JCheckBox clippingBox = new JCheckBox("Clipping comp");
		clippingBox.setToolTipText("Compensate for clipping of integration region");
		clippingBox.setActionCommand("ClipComp");
		clippingBox.addActionListener(this);

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
		pl4.add(clippingBox, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

		GridBagLayout thisLayout = new GridBagLayout();
		this.setLayout(thisLayout);
		thisLayout.rowWeights = new double[] { 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0 };
		thisLayout.rowHeights = new int[] { 7, 7, 7, 7, 7, 7, 7 };
		// thisLayout.columnWeights = new double[] {0.1, 0.9};
		thisLayout.columnWidths = new int[] { 7 };

		this.add(xPlot, new GridBagConstraints(0, 0, 3, 3, 0.9, 0.0, GridBagConstraints.CENTER,
				GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		this.add(yPlot, new GridBagConstraints(0, 3, 3, 3, 0.9, 0.0, GridBagConstraints.CENTER,
				GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		this.add(pl4, new GridBagConstraints(1, 6, 1, 1, 0.1, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));

		hBoxes = new HandleBoxes();
		circleCursor = createCircleCursor();
	}

	/**
	 * @param ae
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent ae) {
		String command = ae.getActionCommand();
		boolean update = false;

		if (command.equals("ClipComp")) {
			clippingCompensation = !clippingCompensation;
			update = true;
		}
		if (update) {
			updateOverlay();
			updatePlot();
			owner.getDataSetImage().repaint();
		}
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
	 * Implemented method which allows the main panel to see the overlay contained in this object for plotting purposes
	 * @param currentOverlay
	 *
	 * @param image
	 *            Actual image to be worked on
	 * @return The overlay
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
		updatePlot(startx, starty, height, width, angle);
	}

	/**
	 * Internal function which updates the plots on the panel, to give the user feedback on the integrations
	 *
	 * @param ax
	 * @param ay
	 * @param h
	 * @param w
	 * @param ang
	 */
	private void updatePlot(int ax, int ay, int h, int w, double ang) {
		// sanity check that image is bigger than handle sides
		if (data == null || hBoxes.getHandleside() > MINSIZEFACTOR * data.getShape()[0]
				|| hBoxes.getHandleside() > MINSIZEFACTOR * data.getShape()[1] || h == 0 || w == 0)
			return;

		Dataset intx = null;
		Dataset inty = null;

		// first perform the integration
		if (angle == 0.0) {
			Integrate2D int2d = new Integrate2D(ax, ay, ax + w, ay + h);
			List<? extends Dataset> dsets = int2d.value(data);
			intx = dsets.get(0);
			inty = dsets.get(1);
		} else {
			MapToRotatedCartesian rcmap = new MapToRotatedCartesian(ax, ay, w, h, ang);
			Integrate2D int2d = new Integrate2D();
			Dataset rcdata = rcmap.value(data).get(0);
			List<? extends Dataset> dsets = int2d.value(rcdata);
			intx = dsets.get(0);
			inty = dsets.get(1);

			if (clippingCompensation) {
				// normalise plot for case when region is clipped to size of image
				DoubleDataset ndata = DatasetFactory.zeros(DoubleDataset.class, data.getShape());
				ndata.fill(1.);
				dsets = rcmap.value(ndata);
				Dataset npdata = dsets.get(0);
				Dataset unpdata = dsets.get(1);
				dsets = int2d.value(npdata);
				Dataset nintx = dsets.get(0);
				Dataset ninty = dsets.get(1);
				dsets = int2d.value(unpdata);
				// calculate fraction in each element that was not clipped
				nintx = Maths.divide(nintx, dsets.get(0));
				ninty = Maths.divide(ninty, dsets.get(1));

				intx = Maths.dividez(intx, nintx);
				inty = Maths.dividez(inty, ninty);
			}
		}

		xPlot.setBatching(true);
		// clear the graph
		xPlot.deleteAllLines();

		xPlot.initializeLine(0);
		xPlot.setLineName(0, "Data");

		xPlot.setXAxisAutoScaling(true);
		xPlot.setYAxisAutoScaling(true);

		int ydist = intx.getShape()[0];
		for (int y = 0; y < ydist; y++) {
			xPlot.addPointToLine(0, y, intx.getDouble(y));
		}

		xPlot.setBatching(false);

		yPlot.setBatching(true);
		// clear the graph
		yPlot.deleteAllLines();

		yPlot.initializeLine(0);
		yPlot.setLineName(0, "Data");

		yPlot.setXAxisAutoScaling(true);
		yPlot.setYAxisAutoScaling(true);

		int xdist = inty.getShape()[0];
		for (int x = 0; x < xdist; x++) {
			yPlot.addPointToLine(0, x, inty.getDouble(x));
		}

		yPlot.setBatching(false);
	}

	private void updateOverlay() {
		if (overlay == null)
			return;

		// clear off the old dataset
		overlay.clear();

		// now fill and draw outline the box
		overlay.drawRotatedBox(angle, startx, starty, height, width, boxRGBA);
		overlay.drawRotatedBoxOutline(angle, startx, starty, height, width, boxthickness, outBoxRGBA);

		// draw arrowhead
		int hs = hBoxes.getHandleside();

		RotatedCoords rc = new RotatedCoords(angle);
		double[] pos = null;
		pos = rc.transformToOriginal(1., 0);
		overlay.drawArrowhead((int) (startx + 0.25 * width * pos[0]), (int) (starty + 0.25 * width * pos[1]), pos[0],
				pos[1], hs, (int) 1.5 * hs, outBoxRGBA);
		overlay.drawArrowhead((int) (startx + 0.75 * width * pos[0]), (int) (starty + 0.75 * width * pos[1]), pos[0],
				pos[1], hs, (int) 1.5 * hs, outBoxRGBA);

		// draw corner and side handles
		int hx = (width - hs) / 2;
		int hy = (height - hs) / 2;

		int[] nhboxcoords = new int[32];
		int i = 0;
		nhboxcoords[i++] = 0;
		nhboxcoords[i++] = 0;
		nhboxcoords[i++] = nhboxcoords[i - 3] + hs;
		nhboxcoords[i++] = nhboxcoords[i - 3] + hs;
		nhboxcoords[i++] = width - hs;
		nhboxcoords[i++] = 0;
		nhboxcoords[i++] = nhboxcoords[i - 3] + hs;
		nhboxcoords[i++] = nhboxcoords[i - 3] + hs;
		nhboxcoords[i++] = 0;
		nhboxcoords[i++] = height - hs;
		nhboxcoords[i++] = nhboxcoords[i - 3] + hs;
		nhboxcoords[i++] = nhboxcoords[i - 3] + hs;
		nhboxcoords[i++] = width - hs;
		nhboxcoords[i++] = height - hs;
		nhboxcoords[i++] = nhboxcoords[i - 3] + hs;
		nhboxcoords[i++] = nhboxcoords[i - 3] + hs;
		nhboxcoords[i++] = hx;
		nhboxcoords[i++] = 0;
		nhboxcoords[i++] = nhboxcoords[i - 3] + hs;
		nhboxcoords[i++] = nhboxcoords[i - 3] + hs;
		nhboxcoords[i++] = 0;
		nhboxcoords[i++] = hy;
		nhboxcoords[i++] = nhboxcoords[i - 3] + hs;
		nhboxcoords[i++] = nhboxcoords[i - 3] + hs;
		nhboxcoords[i++] = width - hs;
		nhboxcoords[i++] = hy;
		nhboxcoords[i++] = nhboxcoords[i - 3] + hs;
		nhboxcoords[i++] = nhboxcoords[i - 3] + hs;
		nhboxcoords[i++] = hx;
		nhboxcoords[i++] = height - hs;
		nhboxcoords[i++] = nhboxcoords[i - 3] + hs;
		nhboxcoords[i++] = nhboxcoords[i - 3] + hs;

		hBoxes.addHandleBoxes(nhboxcoords);
		for (HandleBox hb : hBoxes.getHandleBoxList()) {
			int[] hbc = hb.getHandleCoords();
			pos = rc.transformToOriginal(hbc[0], hbc[1]);
			overlay.drawRotatedBoxOutline(angle, (int) (startx + pos[0]), (int) (starty + pos[1]), hs, hs,
					hBoxes.getHandlethickness(), outBoxRGBA);

		}

		overlay.flipImage();
		updateJythonTerminalNamespace();
	}

	/**
	 * Overloaded function, but not used in this case
	 *
	 * @param e
	 *            Mouse event
	 */
	@Override
	public void mouseClicked(MouseEvent e) {

	}

	/**
	 * Overloaded function, but not used in this case
	 *
	 * @param e
	 *            Mouse event
	 */
	@Override
	public void mouseEntered(MouseEvent e) {

	}

	/**
	 * Overloaded function, but not used in this case
	 *
	 * @param e
	 *            Mouse event
	 */
	@Override
	public void mouseExited(MouseEvent e) {

	}

	/*
	 * Event mask to check for shift key being held on a mouse button event
	 */
	static final int onmask = InputEvent.SHIFT_DOWN_MASK | InputEvent.BUTTON1_DOWN_MASK;

	/**
	 * Function which deals with drawing to the Screen
	 *
	 * @param e
	 *            Mouse event
	 */
	@Override
	public void mousePressed(MouseEvent e) {
		if (overlay == null)
			return;

		switch (e.getButton()) {
		case 1:
			if (hBoxes == null || hBoxes.getCurrent() < 0) {
				curx = e.getX();
				cury = e.getY();
				startx = e.getX();
				starty = e.getY();
				width = startx;
				height = starty;

				// clear off the old dataset
				overlay.clear();
				overlay.flipImage();

				dragging = true;
				owner.getDataSetImage().setCursor(new Cursor(Cursor.HAND_CURSOR));
			} else if (hBoxes.getCurrent() >= 0) {
				overlay.clear();
				overlay.drawRotatedBox(angle, startx, starty, height, width, boxRGBA);
				overlay.flipImage();

				if ((e.getModifiersEx() & onmask) != onmask) {
					hStatus = HandleStatus.RESIZE;
				} else {
					hStatus = HandleStatus.REORIENT;
					owner.getDataSetImage().setCursor(circleCursor);
				}
				dragging = true;
				curx = e.getX();
				cury = e.getY();
			}
			break;
		case 2:
			break;
		case 3:
			if (hBoxes.getCurrent() >= 0) {
				overlay.clear();
				overlay.drawRotatedBox(angle, startx, starty, height, width, boxRGBA);
				overlay.flipImage();
				owner.getDataSetImage().setCursor(circleCursor);
				hStatus = HandleStatus.ROTATE;
				dragging = true;
				curx = e.getX();
				cury = e.getY();
			} else {
				RotatedCoords rc = new RotatedCoords(angle);
				double[] pos = rc.transformToRotated(e.getX() - startx, e.getY() - starty);
				if (overlay.isInBox((int) pos[0], (int) pos[1], 0, 0, width, height)) {
					owner.getDataSetImage().setCursor(new Cursor(Cursor.MOVE_CURSOR));
					hStatus = HandleStatus.MOVE;
					dragging = true;
					curx = e.getX();
					cury = e.getY();
				}
			}
			break;
		default:
			break;
		}
	}

	/**
	 * Function which deals with drawing to the Screen
	 *
	 * @param e
	 *            Mouse event
	 */
	@Override
	public void mouseReleased(MouseEvent e) {
		boolean update = false;
		UpdateStatus us = null;
		switch (e.getButton()) {
		case 1:
			if (dragging) {
				us = interpretMouseDragging(e);
				update = us.update;

				if (update) {
					startx = us.tx;
					starty = us.ty;
					height = us.th;
					width = us.tw;
					angle = us.ta;

					// update spinners
					spx.getModel().setValue(startx);
					spy.getModel().setValue(starty);
					sph.getModel().setValue(height);
					spw.getModel().setValue(width);
					spa.getModel().setValue(angle);
				}
			}
			break;
		case 2:
			break;
		case 3:
			if (dragging) {
				us = interpretMouseDragging(e);
				update = us.update;

				if (update) {
					startx = us.tx;
					starty = us.ty;
					height = us.th;
					width = us.tw;
					angle = us.ta;

					// update spinners
					spx.getModel().setValue(startx);
					spy.getModel().setValue(starty);
					sph.getModel().setValue(height);
					spw.getModel().setValue(width);
					spa.getModel().setValue(angle);
				}
			}
			break;
		default:
			break;
		}
		if (update) {
			hStatus = HandleStatus.NONE;

			owner.getDataSetImage().setCursor(mycursor);
			updatePlot();
			dragging = false;
		}
	}

	/**
	 * Function which deals with drawing to the Screen
	 *
	 * @param e
	 *            Mouse event
	 */
	@Override
	public void mouseDragged(MouseEvent e) {
		if (overlay == null)
			return;

		if (dragging) {
			// clear off the old dataset
			overlay.clear();

			UpdateStatus us = interpretMouseDragging(e);

			if (us.update) {
				// update spinners
				spx.getModel().setValue(us.tx);
				spy.getModel().setValue(us.ty);
				sph.getModel().setValue(us.th);
				spw.getModel().setValue(us.tw);
				spa.getModel().setValue(us.ta);

				// now draw the box
				overlay.drawRotatedBox(us.ta, us.tx, us.ty, us.th, us.tw, boxRGBA);

				updatePlot(us.tx, us.ty, us.th, us.tw, us.ta);
			}
			overlay.flipImage();
		}

	}

	/**
	 * UpdateStatus class for rotated coordinates
	 */
	private class UpdateStatus {
		boolean update;
		int tx, ty; // temporary rotated Cartesian coords
		int tw, th;
		double ta;

		UpdateStatus(int x, int y, int h, int w, double a) {
			tx = x;
			ty = y;
			th = h;
			tw = w;
			ta = a;
			update = false;
		}
	}

	/**
	 * Interpret the mouse dragging event
	 *
	 * @param e
	 * @return update if needed
	 */
	private final UpdateStatus interpretMouseDragging(MouseEvent e) {
		UpdateStatus uStat = new UpdateStatus(startx, starty, height, width, angle);

		RotatedCoords src = null;
		double[] ps = null;
		double[] pe = null;

		int dx;
		int dy;
		switch (hStatus) {
		case NONE:
			// work in rotated coords
			src = new RotatedCoords(angle);
			ps = src.transformToRotated(curx, cury);
			pe = src.transformToRotated(e.getX(), e.getY());
			// check and correct bounding box
			if (ps[0] > pe[0]) {
				double t = ps[0];
				ps[0] = pe[0];
				pe[0] = t;
			}
			if (ps[1] > pe[1]) {
				double t = ps[1];
				ps[1] = pe[1];
				pe[1] = t;
			}
			uStat.tw = (int) (pe[0] - ps[0]);
			if (uStat.tw == 0) {
				uStat.tw = 1;
			}
			uStat.th = (int) (pe[1] - ps[1]);
			if (uStat.th == 0) {
				uStat.th = 1;
			}
			pe = src.transformToOriginal(ps[0], ps[1]);
			uStat.tx = (int) pe[0];
			uStat.ty = (int) pe[1];
			uStat.update = true;
			break;
		case RESIZE:
			if (hBoxes.getCurrent() >= 0) {
				// work in rotated coords
				src = new RotatedCoords(angle);
				ps = new double[2];
				ps[0] = e.getX() - curx;
				ps[1] = e.getY() - cury;
				pe = src.transformToRotated(ps[0], ps[1]);

				double sx, sy, ex, ey;
				sx = 0;
				sy = 0;
				ex = width;
				ey = height;

				switch (hBoxes.getCurrent()) {
				case 0: // NW
					sx = pe[0];
					sy = pe[1];
					break;
				case 1: // NE
					sy = pe[1];
					ex = width + pe[0];
					break;
				case 2: // SW
					sx = pe[0];
					ey = height + pe[1];
					break;
				case 3: // SE
					ex = width + pe[0];
					ey = height + pe[1];
					break;
				case 4: // N
					sy = pe[1];
					break;
				case 5: // W
					sx = pe[0];
					break;
				case 6: // E
					ex = width + pe[0];
					break;
				case 7: // S
					ey = height + pe[1];
					break;
				default:
					break;
				}
				// check and correct bounding box
				if (sx > ex) {
					double tx = sx;
					sx = ex;
					ex = tx;
				}
				if (sy > ey) {
					double ty = sy;
					sy = ey;
					ey = ty;
				}
				ps = src.transformToOriginal(sx, sy);
				uStat.tx = (int) (startx + ps[0]);
				uStat.ty = (int) (starty + ps[1]);
				uStat.tw = (int) (ex - sx);
				if (uStat.tw == 0) {
					uStat.tw = 1;
				}
				uStat.th = (int) (ey - sy);
				if (uStat.th == 0) {
					uStat.th = 1;
				}

				uStat.update = true;
			}
			break;
		case MOVE:
			dx = e.getX() - curx;
			dy = e.getY() - cury;

			uStat.tx += dx;
			uStat.ty += dy;
			uStat.update = true;
			break;
		case ROTATE:
			src = new RotatedCoords(angle);
			ps = src.transformToOriginal(width / 2, height / 2);
			int mx = (int) (startx + ps[0]); // midpoint coords
			int my = (int) (starty + ps[1]);
			dx = e.getX() - mx;
			dy = e.getY() - my;
			double dangle = 180.0 * Math.atan2((curx - mx) * dy - (cury - my) * dx,
					(curx - mx) * dx + (cury - my) * dy) / Math.PI;
			src = new RotatedCoords(-dangle);
			ps = src.transformToRotated(startx - mx, starty - my); // new position of corner point
			uStat.tx = (int) (mx + ps[0]);
			uStat.ty = (int) (my + ps[1]);
			uStat.ta += dangle;
			if (uStat.ta < 0.)
				uStat.ta += 360.0;
			if (uStat.ta > 360.)
				uStat.ta -= 360.0;
			uStat.update = true;
			break;
		case REORIENT:
			dx = e.getX() - startx;
			dy = e.getY() - starty;
			dangle = 180.0 * Math.atan2((curx - startx) * dy - (cury - starty) * dx,
							(curx - startx) * dx + (cury - starty) * dy) / Math.PI;
			src = new RotatedCoords(-dangle);
			uStat.ta += dangle;
			if (uStat.ta < 0.)
				uStat.ta += 360.0;
			if (uStat.ta > 360.)
				uStat.ta -= 360.0;
			uStat.update = true;
			break;
		}

		return uStat;
	}

	/**
	 * Overloaded function, but not used in this case
	 *
	 * @param e
	 *            Mouse event
	 */
	@Override
	public void mouseMoved(MouseEvent e) {
		if (!dragging) {
			if (hStatus == HandleStatus.NONE) {
				if (hBoxes != null) {
					RotatedCoords rc = new RotatedCoords(angle);
					double[] pos = rc.transformToRotated(e.getX() - startx, e.getY() - starty);
					hBoxes.whichHandleBox((int) pos[0], (int) pos[1]);

					if (hBoxes.getCurrentBox() != null)
						owner.getDataSetImage().setCursor(hBoxes.getCurrentBox().getMyCursor());
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
