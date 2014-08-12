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
import gda.gui.util.HandleSector;
import gda.gui.util.HandleSectors;
import gda.jython.JythonServerFacade;
import gda.plots.SimplePlot;

import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import uk.ac.diamond.scisoft.analysis.coords.SectorCoords;
import uk.ac.diamond.scisoft.analysis.dataset.Dataset;
import uk.ac.diamond.scisoft.analysis.dataset.DatasetUtils;
import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
import uk.ac.diamond.scisoft.analysis.dataset.Maths;
import uk.ac.diamond.scisoft.analysis.dataset.function.Centroid;
import uk.ac.diamond.scisoft.analysis.dataset.function.Integrate2D;
import uk.ac.diamond.scisoft.analysis.dataset.function.MapToPolar;

/**
 * This panel is designed to provide the most simple integration methods for the Image visualisation toolkit
 */
public class SectorIntegratorPanel extends VisPanel implements ActionListener, IMainPlotManipulator {

	DoubleBufferedImageData overlay = null;
	DoubleDataset data = null;

	int cx = 250;
	int cy = 250;
	int markerradius = 10;
	int curx = 0; // current position for moving sector or defining sector
	int cury = 0;

	double sr = 50.0;
	double sp = 60.0;
	double er = 100.0;
	double ep = 120.0;

	boolean dragging = false;
	Cursor mycursor = getCursor();
	private int[] sectorRGBA = { 0, 0, 255, 100 };
	private int[] outSectorRGBA = { 0, 0, 255, 180 };

	private final static int MINSIZEFACTOR = 2; // minimum size factor for determining when to perform profile calculation

	int boxthickness = 4;

	/**
	 * possible handle states
	 */
	private enum HandleStatus {
		/**
		 * Specifies that the handle does nothing
		 */
		NONE,
		/**
		 * Specifies that the handle is for resizing
		 */
		RESIZE
	}

	HandleStatus hStatus = HandleStatus.NONE;
	HandleSectors hSectors = null;

	SimplePlot radPlot = null;
	SimplePlot phiPlot = null;

	/**
	 * possible centring states
	 */
	private enum CentringStatus {
		/**
		 * Specifies the centring has been done
		 */
		DONE, /**
		 * Specifies the centring is being done manually
		 */
		MANUAL, /**
		 * Specifies the centring is being done automatically using the centroid method
		 */
		CENTROID, /**
		 * Specifies the centring is being done automatically
		 */
		AUTOMATIC, /**
		 * Specifies an experimental
		 */
		EXPERIMENTAL
	}

	CentringStatus cStatus = CentringStatus.MANUAL;
	private JRadioButton radioManual = null;
	JButton resetButton = null;
	JButton centroidButton = null;

	boolean markerSelected = false;

	private JSpinner sn = null; // number of rings
	private JSpinner sd = null; // delta radius
	private JSpinner scx = null; // centre x
	private JSpinner scy = null; // centre y
	private JSpinner ssr = null; // start radius
	private JSpinner ser = null; // end radius
	private JSpinner ssp = null; // start phi
	private JSpinner sep = null; // end phi

	private ChangeListener spinnerChangeListener = new ChangeListener() {
		@Override
		public void stateChanged(ChangeEvent e) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					if (!dragging) { // don't update when dragging
						cx = (Integer) scx.getModel().getValue();
						cy = (Integer) scy.getModel().getValue();

						sr = (Double) ssr.getModel().getValue();
						er = (Double) ser.getModel().getValue();
						sp = (Double) ssp.getModel().getValue();
						ep = (Double) sep.getModel().getValue();
						updatePlot();
						owner.getDataSetImage().repaint();
					}
				}
			});
		}
	};

	// Nasty hack that places the GUI information into the JythonTerminal instance
	HashMap<String, Double> roi = null;
	String roiJythonName = "plot_client_s_int_roi";

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
				roi.put("centre x", new Double(cx));
				roi.put("centre y", new Double(cy));
				roi.put("start radius", new Double(sr));
				roi.put("start angle", new Double(sp));
				roi.put("end radius", new Double(er));
				roi.put("end angle", new Double(ep));
				jsf.placeInJythonNamespace(roiJythonName, roi);
			}
		}
	}

	/**
	 * possible symmetry states
	 */
	private enum SymmetryStatus {
		/**
		 * Symmetry is none
		 */
		NONE,
		/**
		 * Symmetry in the y reflection status
		 */
		YREFLECT,
		/**
		 * Symmetry of inversion
		 */
		INVERSION,
		/**
		 * Symmetry status is full.
		 */
		FULL
	}

	SymmetryStatus symmetry = SymmetryStatus.NONE;
	private JRadioButton radioNone = null;

	boolean clippingCompensation = false; // allow compensation for clipped sector 
	
	/**
	 * Constructor for the class, this needs to take the MainPlot which is associated as an argument This function
	 * mainly lays out the GUI
	 * 
	 * @param main
	 *            the MainPlot that this manipulator is linked to
	 */
	public SectorIntegratorPanel(MainPlot main) {
		super(main);

		setName("S-Integrator");

		GridBagLayout gbl2 = new GridBagLayout();
		gbl2.rowHeights = new int[] {7, 7, 7, 7, 7};

		JPanel pl1 = new JPanel();
		pl1.setBorder(new TitledBorder(new EtchedBorder(), "Centring"));
		pl1.setLayout(gbl2);

		ButtonGroup bg = new ButtonGroup();
		radioManual = new JRadioButton("Manual", true);
		radioManual.setToolTipText("Set centre using mouse");
		radioManual.setActionCommand("CentreMan");
		radioManual.addActionListener(this);
//		JRadioButton r1 = new JRadioButton("Reset");
//		r1.setToolTipText("Reset centre back to default of image centre");
//		r1.setActionCommand("CentreRes");
//		r1.addActionListener(this);
//		JRadioButton r2 = new JRadioButton("Automatic");
//		r2.setToolTipText("Adjust centre by minimizing peak widths");
//		r2.setActionCommand("CentreAut");
//		r2.addActionListener(this);
//		JRadioButton r3 = new JRadioButton("Centroid");
//		r3.setToolTipText("Set centre to image's centroid");
//		r3.setActionCommand("CentreCen");
//		r3.addActionListener(this);
//		JRadioButton r4 = new JRadioButton("Experimental");
//		r4.setToolTipText("Use experimentally determined value");
//		r4.setActionCommand("CentreExp");
//		r4.addActionListener(this);
		JRadioButton r5 = new JRadioButton("Lock");
		r5.setToolTipText("Lock centre");
		r5.setActionCommand("CentreDone");
		r5.addActionListener(this);

		resetButton = new JButton("Reset");
		resetButton.setToolTipText("Reset centre back to default of image centre");
		resetButton.setActionCommand("CentreRes");
		resetButton.addActionListener(this);
		centroidButton = new JButton("Centroid");
		centroidButton.setToolTipText("Set centre to image's centroid");
		centroidButton.setActionCommand("CentreCen");
		centroidButton.addActionListener(this);
		
		FlowLayout pl2f = new FlowLayout();
		pl2f.setAlignment(FlowLayout.LEFT);
		
		JPanel pl12 = new JPanel();
		pl12.setLayout(pl2f);
		pl12.add(new JLabel("Centre x: ", SwingConstants.RIGHT));
		SpinnerModel smx = new SpinnerNumberModel(cx, -1000, 5000, 1);
		scx = new JSpinner(smx);
		scx.addChangeListener(spinnerChangeListener);
		pl12.add(scx);

		JPanel pl13 = new JPanel();
		pl13.setLayout(pl2f);
		pl13.add(new JLabel("Centre y: ", SwingConstants.RIGHT));
		SpinnerModel smy = new SpinnerNumberModel(cy, -1000, 5000, 1);
		scy = new JSpinner(smy);
		scy.addChangeListener(spinnerChangeListener);
		pl13.add(scy);

		bg.add(radioManual);
		pl1.add(radioManual, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
//		bg.add(r1);
//		pl1.add(r1);
//		bg.add(r2);
//		pl1.add(r2);
//		bg.add(r3);
//		pl1.add(r3);
//		bg.add(r4);
//		pl1.add(r4);
		bg.add(r5);
		pl1.add(r5, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		pl1.add(resetButton,    new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		pl1.add(centroidButton, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		pl1.add(pl12, new GridBagConstraints(0, 3, 2, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		pl1.add(pl13, new GridBagConstraints(0, 4, 2, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

		JPanel pl2 = new JPanel();
		pl2.setBorder(new TitledBorder(new EtchedBorder(), "Guide"));
		pl2.setLayout(new BoxLayout(pl2, BoxLayout.Y_AXIS));
		JPanel pl21 = new JPanel();
		pl2f.setAlignment(FlowLayout.RIGHT);
		pl21.setLayout(pl2f);
		pl21.add(new JLabel("Number of rings: ", SwingConstants.RIGHT));
		SpinnerModel sma = new SpinnerNumberModel(3, 0, 10, 1);
		sn = new JSpinner(sma);
		sn.addChangeListener(spinnerChangeListener);
		pl21.add(sn);
		JPanel pl22 = new JPanel();
		pl22.setLayout(pl2f);
		pl22.add(new JLabel("Size difference of rings: ", SwingConstants.RIGHT));
		SpinnerModel smb = new SpinnerNumberModel(40, 20, 200, 10);
		sd = new JSpinner(smb);
		sd.addChangeListener(spinnerChangeListener);
		pl22.add(sd);

		pl2.add(pl21);
		pl2.add(pl22);

		JPanel pl3 = new JPanel();
		pl3.setBorder(new TitledBorder(new EtchedBorder(), "Symmetry"));
		pl3.setLayout(new BoxLayout(pl3, BoxLayout.Y_AXIS));
		ButtonGroup bg2 = new ButtonGroup();
		radioNone = new JRadioButton("None", true);
		radioNone.setToolTipText("Do not reflect RoI");
		radioNone.setActionCommand("DoNotReflect");
		radioNone.addActionListener(this);
		JRadioButton r22 = new JRadioButton("Y (up/down)");
		r22.setToolTipText("Reflect RoI on X axis");
		r22.setActionCommand("DoReflectY");
		r22.addActionListener(this);
		JRadioButton r23 = new JRadioButton("Inversion");
		r23.setToolTipText("Reflect RoI through centre");
		r23.setActionCommand("DoReflectI");
		r23.addActionListener(this);
		JRadioButton r24 = new JRadioButton("Circular");
		r24.setToolTipText("Full circular symmetry");
		r24.setActionCommand("DoFullCircle");
		r24.addActionListener(this);

		bg2.add(radioNone);
		pl3.add(radioNone);
		bg2.add(r22);
		pl3.add(r22);
		bg2.add(r23);
		pl3.add(r23);
		bg2.add(r24);
		pl3.add(r24);

		radPlot = new SimplePlot();
		radPlot.setTitle("Radial Integration");
		radPlot.setXAxisLabel("Polar angle");
		radPlot.setYAxisLabel("Intensity");

		phiPlot = new SimplePlot();
		phiPlot.setTitle("Azimuthal Integration");
		phiPlot.setXAxisLabel("Radius");
		phiPlot.setYAxisLabel("Intensity");

		JPanel pl41 = new JPanel();
		pl41.setLayout(pl2f);
		pl41.add(new JLabel("Start rad: ", SwingConstants.RIGHT));
		SpinnerModel smsr = new SpinnerNumberModel(sr, 0., 5000, 1);
		ssr = new JSpinner(smsr);
		ssr.addChangeListener(spinnerChangeListener);
		pl41.add(ssr);

		JPanel pl42 = new JPanel();
		pl42.setLayout(pl2f);
		pl42.add(new JLabel("End rad: ", SwingConstants.RIGHT));
		SpinnerModel smer = new SpinnerNumberModel(er, 0, 5000, 1);
		ser = new JSpinner(smer);
		ser.addChangeListener(spinnerChangeListener);
		pl42.add(ser);

		JPanel pl43 = new JPanel();
		pl43.setLayout(pl2f);
		pl43.add(new JLabel("Start azi: ", SwingConstants.RIGHT));
		SpinnerModel smsp = new SpinnerNumberModel(sp, 0., 360., 1.);
		ssp = new JSpinner(smsp);
		ssp.addChangeListener(spinnerChangeListener);
		pl43.add(ssp);
		
		JPanel pl44 = new JPanel();
		pl44.setLayout(pl2f);
		pl44.add(new JLabel("End azi: ", SwingConstants.RIGHT));
		SpinnerModel smep = new SpinnerNumberModel(ep, 0., 360., 1.);
		sep = new JSpinner(smep);
		sep.addChangeListener(spinnerChangeListener);
		pl44.add(sep);

		JCheckBox clippingBox = new JCheckBox("Clipping comp");
		clippingBox.setToolTipText("Compensate for clipping of integration region");
		clippingBox.setActionCommand("ClipComp");
		clippingBox.addActionListener(this);

		JPanel pl4 = new JPanel();
		pl4.setLayout(new GridBagLayout());
		pl4.add(pl41, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		pl4.add(pl42, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		pl4.add(pl43, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		pl4.add(pl44, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		pl4.add(clippingBox, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

		GridBagLayout thisLayout = new GridBagLayout();
		this.setLayout(thisLayout);
		thisLayout.rowWeights = new double[] {0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1};
		thisLayout.rowHeights = new int[] {7, 7, 7, 7, 7, 7, 7};
		thisLayout.columnWeights = new double[] {0.1, 0.9};
		thisLayout.columnWidths = new int[] {7, 7};

		this.add(pl1, new GridBagConstraints(0, 0, 1, 3, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		this.add(pl2, new GridBagConstraints(0, 3, 1, 2, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		this.add(pl3, new GridBagConstraints(0, 5, 1, 2, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		this.add(radPlot, new GridBagConstraints(1, 0, 1, 3, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		this.add(phiPlot, new GridBagConstraints(1, 3, 1, 3, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));	
		this.add(pl4, new GridBagConstraints(1, 6, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));	
		
		hSectors = new HandleSectors();
	}

	private void setDefaultCentre() {
		int[] dims = data.getShape();
		cx = dims[1] / 2;
		cy = dims[0] / 2;
		er = ((cx > cy) ? cy : cx) * (2. / 3);
		sr = er * 0.5;
		sp = 60.0;
		ep = 120.0;
	}

	/** 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent ae) {
		String command = ae.getActionCommand();
		boolean update = false;

		if (command.equals("CentreMan")) {
			cStatus = CentringStatus.MANUAL;
			scx.setEnabled(true);
			scy.setEnabled(true);
			resetButton.setEnabled(true);
			centroidButton.setEnabled(true);
		} else if (command.equals("CentreCen")) {
			cStatus = CentringStatus.CENTROID;
			update = true;
		} else if (command.equals("CentreRes")) {
			setDefaultCentre();
			radioManual.doClick();
			update = true;
		} else if (command.equals("CentreAut")) {
			cStatus = CentringStatus.AUTOMATIC;
			update = true;
		} else if (command.equals("CentreExp")) {
			cStatus = CentringStatus.EXPERIMENTAL;
			update = true;
		} else if (command.equals("CentreDone")) {
			cStatus = CentringStatus.DONE;
			scx.setEnabled(false);
			scy.setEnabled(false);
			resetButton.setEnabled(false);
			centroidButton.setEnabled(false);
		} else if (command.equals("DoNotReflect")) {
			symmetry = SymmetryStatus.NONE;
			update = true;
		} else if (command.equals("DoReflectY")) {
			// check if reflection in y makes sense given selected sector
			if ((sp <= 180.0 && ep <= 180.0) || (sp > 180.0 && ep > 180.0)) {
				symmetry = SymmetryStatus.YREFLECT;
				update = true;
			} else {
				radioNone.doClick();
			}
		} else if (command.equals("DoReflectI")) {
			// check if inversion makes sense given selected sector
			if (ep - sp <= 180.0) {
				symmetry = SymmetryStatus.INVERSION;
				update = true;
			} else {
				radioNone.doClick();
			}
		} else if (command.equals("DoFullCircle")) {
			symmetry = SymmetryStatus.FULL;
			update = true;
		} else if (command.equals("ClipComp")) {
			clippingCompensation = !clippingCompensation;
			update = true;
		}
		if (update) {
			updateOverlay();
			updatePlot();
			owner.getDataSetImage().repaint();
		}
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
		if (newdata) setDefaultCentre();

		if (!dragging) updatePlot(); // don't update whilst we are dragging

		return overlay;
	}

	private void updatePlot() {
		updateOverlay();
		updatePlot(cx, cy, sr, sp, er, ep);
	}

	/**
	 * Internal function which updates the plots on the panel, to give the user feedback on the integrations
	 * 
	 * @param x
	 * @param y
	 * @param ar
	 * @param ap
	 * @param br
	 * @param bp
	 */
	private void updatePlot(int x, int y, double ar, double ap, double br, double bp) {
		// sanity check that image is bigger than handle sides
		if (data == null || hSectors.getHandleside() > MINSIZEFACTOR*data.getShape()[0] || hSectors.getHandleside() > MINSIZEFACTOR*data.getShape()[1])
			return;
		
		// first perform the integration
		MapToPolar pmap = new MapToPolar(x, y, ar, ap, br, bp);
		Integrate2D int2d = new Integrate2D();
		Dataset pdata = pmap.value(data).get(0);
		List<? extends Dataset> dsets = int2d.value(pdata);
		Dataset intp = dsets.get(0);
		Dataset intr = dsets.get(1);

		if (clippingCompensation) {
			// normalise plot for case when sector is clipped to size of image
			DoubleDataset ndata = new DoubleDataset(data.getShape());
			ndata.fill(1.);
			dsets = pmap.value(ndata);
			Dataset npdata = dsets.get(0);
			Dataset unpdata = dsets.get(1);
			dsets = int2d.value(npdata);
			Dataset nintp = dsets.get(0);
			Dataset nintr = dsets.get(1);
			dsets = int2d.value(unpdata);
			// calculate fraction in each element that was not clipped
			nintp = Maths.divide(nintp, dsets.get(0));
			nintr = Maths.divide(nintr, dsets.get(1));

			intp = Maths.dividez(intp, nintp);
			intr = Maths.dividez(intr, nintr);
		}

		switch (symmetry) {
		case YREFLECT:
			// add in y reflected integral
			MapToPolar pmapy = new MapToPolar(x, y, ar, 360.0 - bp, br, 360.0 - ap);
			Dataset pdatay = pmapy.value(data).get(0);
			List<? extends Dataset> dsetsy = int2d.value(pdatay);
			intp = Maths.add(intp, dsetsy.get(0));
			intr = Maths.add(intr, dsetsy.get(1));
			break;
		case INVERSION:
			// add in inverted integral
			MapToPolar pmapi = null;
			if (ap > 180.0) {
				pmapi = new MapToPolar(x, y, ar, ap - 180.0, br, bp - 180.0);
				Dataset pdatai = pmapi.value(data).get(0);
				List<? extends Dataset> dsetsi = int2d.value(pdatai);
				intp = Maths.add(intp, dsetsi.get(0));
				intr = Maths.add(intr, dsetsi.get(1));
			} else {
				if (bp < 180.0) {
					pmapi = new MapToPolar(x, y, ar, ap + 180.0, br, bp + 180.0);
					Dataset pdatai = pmapi.value(data).get(0);
					List<? extends Dataset> dsetsi = int2d.value(pdatai);
					intp = Maths.add(intp, dsetsi.get(0));
					intr = Maths.add(intr, dsetsi.get(1));
				} else {
					// add in two parts over branch cut
					pmapi = new MapToPolar(x, y, ar, ap + 180.0, br, 360.0);
					Dataset pdatai = pmapi.value(data).get(0);
					List<? extends Dataset> dsetsi1 = int2d.value(pdatai);
					Dataset intp1 = Maths.add(intp, dsetsi1.get(0));
					Dataset intr1 = Maths.add(intr, dsetsi1.get(1));
					pmapi = new MapToPolar(x, y, ar, 0.0, br, bp - 180.0);
					pdatai = pmapi.value(data).get(0);
					dsetsi1 = int2d.value(pdatai);
					intp1 = Maths.add(intp1, dsetsi1.get(0));
					Dataset intr2 = dsetsi1.get(1);
					// check for overlaps and gaps before joining angular bits together
					int r2len = intr2.getSize();
					int rdiff = intr.getSize() - intr1.getSize();
					if (rdiff > r2len || r2len < 1) {
						break;
					} else if (rdiff < r2len) {
						Dataset td = intr2.getSlice(new int[] {r2len - rdiff}, new int[] {r2len}, null);
						intr1 = DatasetUtils.append(intr1, td, 0);
					} else {
						intr1 = DatasetUtils.append(intr1, intr2, 0);
					}
					intp = Maths.add(intp, intp1);
					intr = Maths.add(intr, intr1);
				}
			}
			break;
		case NONE:
			break;
		case FULL:
			MapToPolar pmapf = new MapToPolar(x, y, ar, 0.0, br, 360.0);
			Dataset pdataf = pmapf.value(data).get(0);
			List<? extends Dataset> dsetsf = int2d.value(pdataf);
			intp = dsetsf.get(0);
			intr = dsetsf.get(1);
			break;
		}

		// then set up all the graphs

		radPlot.setBatching(true);
		// clear the graph
		radPlot.deleteAllLines();

		radPlot.initializeLine(0);
		radPlot.setLineName(0, "Data");

		radPlot.setXAxisAutoScaling(true);
		radPlot.setYAxisAutoScaling(true);

		int pdist = intr.getShape()[0];
		double dp = (bp - ap) / pdist;
		for (int p = 0; p < pdist; p++) {
			radPlot.addPointToLine(0, ap + p * dp, intr.getDouble(p));
		}

		radPlot.setBatching(false);

		phiPlot.setBatching(true);
		// clear the graph
		phiPlot.deleteAllLines();

		phiPlot.initializeLine(0);
		phiPlot.setLineName(0, "Data");

		phiPlot.setXAxisAutoScaling(true);
		phiPlot.setYAxisAutoScaling(true);

		int rdist = intp.getShape()[0];
		for (int r = 0; r < rdist; r++) {
			phiPlot.addPointToLine(0, ar + r, intp.getDouble(r));
		}

		phiPlot.setBatching(false);

	}

	private void updateOverlay() {
		// first adjust centre position
		switch (cStatus) {
		case CENTROID:
			// calculate centroid
			Centroid cen = new Centroid();
			List<Double> cd = cen.value(data);
			cy = (int) Math.floor(cd.get(0));
			cx = (int) Math.floor(cd.get(1));
			break;
		case EXPERIMENTAL:
			// grab position from metadata
			break;
		case AUTOMATIC:
			// iterate about current position to find place where peak widths are minimised
			break;
		case DONE:
			break;
		case MANUAL:
			break;
		}

		DoubleBufferedImageData lOverlay = overlay; // as drawing is a little slow then keep local reference
		if (lOverlay == null)
			return;

		// clear off the old dataset
		lOverlay.clear();

		// now fill and draw outline the sector
		if (symmetry != SymmetryStatus.FULL) {
			lOverlay.drawSector(cx, cy, sr, sp, er, ep, sectorRGBA);
			lOverlay.drawSectorOutline(cx, cy, sr, sp, er, ep, boxthickness, outSectorRGBA);
		}
		switch (symmetry) {
		case YREFLECT:
			lOverlay.drawSector(cx, cy, sr, 360.0 - ep, er, 360.0 - sp, sectorRGBA);
			lOverlay.drawSectorOutline(cx, cy, sr, 360.0 - ep, er, 360.0 - sp, boxthickness, outSectorRGBA);
			break;
		case INVERSION:
			lOverlay.drawSector(cx, cy, sr, sp + 180.0, er, ep + 180.0, sectorRGBA);
			lOverlay.drawSectorOutline(cx, cy, sr, sp + 180.0, er, ep + 180.0, boxthickness, outSectorRGBA);
			break;
		case FULL:
			lOverlay.drawSector(cx, cy, sr, 0.0, er, 360.0, sectorRGBA);
			lOverlay.drawSectorOutline(cx, cy, sr, 0.0, er, 360.0, boxthickness, outSectorRGBA);
			break;
		case NONE:
			break;
		}

		// draw corner and side handles
		int dr = hSectors.getHandleside();
		double hr = (sr + er - dr) / 2.0;
		double dp = (180.0 * dr) / (Math.PI * er);
		double hp = (sp + ep - dp) / 2.0;
		double[] nhsectorcoords = { sr, ep - dp, sr + dr, ep, sr, sp, sr + dr, sp + dp, er - dr, ep - dp, er, ep,
				er - dr, sp, er, sp + dp, sr, hp, sr + dr, hp + dp, hr, ep - dp, hr + dr, ep, hr, sp, hr + dr, sp + dp,
				er - dr, hp, er, hp + dp };
		if (symmetry == SymmetryStatus.FULL) {
			hp = (360.0 - dp) / 2.0;
			nhsectorcoords = new double[] { sr, 360.0 - dp, sr + dr, 360.0, sr, 0.0, sr + dr, dp, er - dr, 360.0 - dp,
					er, 360.0, er - dr, 0.0, er, dp, sr, hp, sr + dr, hp + dp, hr, 360.0 - dp, hr + dr, 360.0, hr, 0.0,
					hr + dr, dp, er - dr, hp, er, hp + dp };
		}

		hSectors.addHandleSectors(nhsectorcoords);
		for (HandleSector hs : hSectors.getHandleSectorList()) {
			double[] hsc = hs.getHandleSectorCoords();

			lOverlay.drawSectorOutline(cx, cy, hsc[0], hsc[1], hsc[2], hsc[3], hSectors.getHandlethickness(), outSectorRGBA);
		}

		lOverlay.drawCentreMarker(cx, cy, markerradius, outSectorRGBA);

		int maxs = (Integer) sn.getModel().getValue();
		int rad = (Integer) sd.getModel().getValue();
		for (int ns = 0; ns < maxs; ns++) {
			lOverlay.drawCircle(cx, cy, rad * (ns + 1), outSectorRGBA);
		}

		lOverlay.flipImage();
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
			if (markerSelected) {
				overlay.clear();
				// now draw the marker
				overlay.drawCentreMarker(cx, cy, markerradius, outSectorRGBA);
				int maxs = (Integer) sn.getModel().getValue();
				int rad = (Integer) sd.getModel().getValue();
				for (int ns = 0; ns < maxs; ns++) {
					overlay.drawCircle(cx, cy, rad * (ns + 1), outSectorRGBA);
				}
				overlay.flipImage();
				dragging = true;
				curx = e.getX();
				cury = e.getY();
			} else {
				if (hSectors == null || hSectors.getCurrent() < 0) {
					curx = e.getX();
					cury = e.getY();

					// clear off the old dataset
					overlay.clear();
					overlay.flipImage();

					dragging = true;
					owner.getDataSetImage().setCursor(new Cursor(Cursor.HAND_CURSOR));
				} else if (hSectors.getCurrent() >= 0) {
					overlay.clear();
					overlay.drawSector(cx, cy, sr, sp, er, ep, sectorRGBA);
					overlay.flipImage();

					hStatus = HandleStatus.RESIZE;
					dragging = true;
				}
			}
			break;
		case 2:
			break;
		case 3:
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
		if (overlay == null)
			return;

		boolean update = false;

		switch (e.getButton()) {
		case 1:
			if (dragging) {
				if (markerSelected) {
					int dx = e.getX() - curx;
					int dy = e.getY() - cury;
					if (cx + dx < 0) {
						cx = 0;
					} else if (cx + dx >= overlay.getW()) {
						cx = overlay.getW() - 1;
					} else {
						cx += dx;
					}
					if (cy + dy < 0) {
						cy = -1;
					} else if (cy + dy >= overlay.getH()) {
						cy = overlay.getH() - 1;
					} else {
						cy += dy;
					}

					// update spinners
					scx.getModel().setValue(cx);
					scy.getModel().setValue(cy);

					update = true;
				} else {
					UpdateStatus us = interpretMouseDragging(e);
					update = us.update;
					if (update) {
						sr = us.tsr;
						sp = us.tsp;
						er = us.ter;
						ep = us.tep;

						// update spinners
						ssr.getModel().setValue(sr);
						ser.getModel().setValue(er);
						ssp.getModel().setValue(sp);
						sep.getModel().setValue(ep);
					}
				}
			}
			break;
		case 2:
			break;
		case 3:
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

			if (markerSelected) {
				int dx = e.getX() - curx;
				int dy = e.getY() - cury;

				if (cx + dx < 0) {
					dx = -cx;
				} else if (cx + dx >= overlay.getW()) {
					dx = overlay.getW() - 1 - cx;
				}
				if (cy + 1 + dy < 0) {
					dy = -cy;
				} else if (cy + dy >= overlay.getH()) {
					dy = overlay.getH() - 1 - cy;
				}

				// update spinners
				scx.getModel().setValue(cx+dx);
				scy.getModel().setValue(cy+dy);

				// now draw the marker
				overlay.drawCentreMarker(cx + dx, cy + dy, markerradius, outSectorRGBA);
				int maxs = (Integer) sn.getModel().getValue();
				int rad = (Integer) sd.getModel().getValue();
				for (int ns = 0; ns < maxs; ns++) {
					overlay.drawCircle(cx + dx, cy + dy, rad * (ns + 1), outSectorRGBA);
				}
				updatePlot(cx + dx, cy + dy, sr, sp, er, ep);
			} else {
				UpdateStatus us = interpretMouseDragging(e);

				if (us.update) {
					// update spinners
					ssr.getModel().setValue(us.tsr);
					ser.getModel().setValue(us.ter);
					ssp.getModel().setValue(us.tsp);
					sep.getModel().setValue(us.tep);

					// now draw the sector
					overlay.drawSector(cx, cy, us.tsr, us.tsp, us.ter, us.tep, sectorRGBA);
					updatePlot(cx, cy, us.tsr, us.tsp, us.ter, us.tep);
				}
			}
			overlay.flipImage();
		}

	}

	/**
	 * UpdateStatus class for sector coordinates
	 */
	private class UpdateStatus {
		boolean update;
		double tsr, tsp, ter, tep; // temporary sector coordinates

		UpdateStatus(double ar, double ap, double br, double bp) {
			tsr = ar;
			tsp = ap;
			ter = br;
			tep = bp;
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
		int tsx, tsy;
		int tex, tey;
		UpdateStatus uStat = new UpdateStatus(sr, sp, er, ep);

		SectorCoords ssc = null;
		double[] ps = null;

		if (symmetry == SymmetryStatus.FULL) {
			uStat.tsp = 0.0;
			uStat.tep = 360.0;
		}

		switch (hStatus) {
		case NONE:
			// use temporary start and end points
			tsx = curx;
			tsy = cury;
			tex = e.getX();
			tey = e.getY();

			// now draw the sector
			SectorCoords esc = null;
			double[] pe = null;
			ssc = new SectorCoords(tsx - cx, tsy - cy, true);
			esc = new SectorCoords(tex - cx, tey - cy, true);
			ps = ssc.getPolar();
			pe = esc.getPolar();

			// impose symmetry induced limitations
			switch (symmetry) {
			case FULL:
				break;
			case INVERSION:
				// prevent overlaps when resizing inverted selection
				if (pe[1] - ps[1] > 180.0) {
					pe[1] = ps[1] + 180.0;
				} else if (ps[1] - pe[1] > 180.0) {
					pe[1] = ps[1] - 180.0;
				}
				break;
			case NONE:
				break;
			case YREFLECT:
				// prevents overlaps
				if ((ps[1] <= 180.0 && pe[1] > 180.0) || (ps[1] > 180.0 && pe[1] < 180.0)) {
					pe[1] = 180.0;
				}
				break;
			}
			// check and correct bounding box
			if (ps[0] > pe[0]) {
				double tr = ps[0];
				ps[0] = pe[0];
				pe[0] = tr;
			}
			if (ps[1] > pe[1]) {
				double tp = ps[1];
				ps[1] = pe[1];
				pe[1] = tp;
			}
			uStat.tsr = ps[0];
			uStat.tsp = ps[1];
			uStat.ter = pe[0];
			uStat.tep = pe[1];
			uStat.update = true;
			break;
		case RESIZE:
			if (hSectors.getCurrent() >= 0) {
				tsx = e.getX() - cx;
				tsy = e.getY() - cy;
				ssc = new SectorCoords(tsx, tsy, true);
				ps = ssc.getPolar();

				switch (hSectors.getCurrent()) {
				case 0: // NW
					uStat.tsr = ps[0];
					uStat.tep = ps[1];
					break;
				case 1: // NE
					uStat.tsr = ps[0];
					uStat.tsp = ps[1];
					break;
				case 2: // SW
					uStat.ter = ps[0];
					uStat.tep = ps[1];
					break;
				case 3: // SE
					uStat.tsp = ps[1];
					uStat.ter = ps[0];
					break;
				case 4: // N
					uStat.tsr = (ps[1] >= sp && ps[1] <= ep) ? ps[0] : 0;
					break;
				case 5: // W
					uStat.tep = ps[1];
					break;
				case 6: // E
					uStat.tsp = ps[1];
					break;
				case 7: // S
					uStat.ter = (ps[1] >= sp && ps[1] <= ep) ? ps[0] : 0;
					break;
				default:
					break;
				}

				// impose symmetry induced limitations
				switch (symmetry) {
				case FULL:
					if (uStat.tsp != 0.0 || uStat.tep != 360.0) {
						// toggle selection back
						symmetry = SymmetryStatus.NONE;
						radioNone.doClick();
					}
					break;
				case INVERSION:
					// prevent overlaps when resizing inverted selection
					if (uStat.tep - uStat.tsp > 180.0) {
						if (uStat.tep != ep) {
							uStat.tep = uStat.tsp + 180.0;
						}
						if (uStat.tsp != sp) {
							uStat.tsp = uStat.tep - 180.0;
						}
					}
					break;
				case NONE:
					break;
				case YREFLECT:
					// prevents overlaps
					if ((sp <= 180.0 && uStat.tsp > 180.0) || (sp > 180.0 && uStat.tsp < 180.0)) {
						uStat.tsp = 180.0;
					}

					if ((ep <= 180.0 && uStat.tep > 180.0) || (ep > 180.0 && uStat.tep < 180.0)) {
						uStat.tep = 180.0;
					}
					break;
				}
				// check and correct bounding box
				if (uStat.tsr > uStat.ter) {
					double tr = uStat.tsr;
					uStat.tsr = uStat.ter;
					uStat.ter = tr;
				}
				if (uStat.tsp > uStat.tep) {
					double tp = uStat.tsp;
					uStat.tsp = uStat.tep;
					uStat.tep = tp;
				}
				uStat.update = true;
			}
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
			if (cStatus == CentringStatus.MANUAL) {
				double xd = e.getX() - cx;
				double yd = e.getY() - cy;
				if (xd * xd + yd * yd <= markerradius * markerradius) {
					owner.getDataSetImage().setCursor(new Cursor(Cursor.MOVE_CURSOR));
					markerSelected = true;
				} else {
					owner.getDataSetImage().setCursor(mycursor);
					markerSelected = false;
				}
			}
			if (hStatus == HandleStatus.NONE) {
				if (hSectors != null) {
					hSectors.whichHandleSector(e.getX() - cx, e.getY() - cy);
					if (hSectors.getCurrentSector() != null) {
						owner.getDataSetImage().setCursor(hSectors.getCurrentSector().getMyCursor());
						markerSelected = false;
					} else if (!markerSelected) {
						owner.getDataSetImage().setCursor(mycursor);
					}
				}
			}
		}
	}

	@Override
	public void releaseOverlay() {
		overlay = null;
	}
}
