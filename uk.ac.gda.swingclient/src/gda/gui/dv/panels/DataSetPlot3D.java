/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council
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

package gda.gui.dv.panels;

import gda.gui.dv.ImageData;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.ListIterator;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;

import de.jreality.geometry.IndexedLineSetFactory;
import de.jreality.scene.Appearance;
import de.jreality.scene.Camera;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.CommonAttributes;
import de.jreality.ui.viewerapp.ViewerApp;
import de.jreality.util.CameraUtility;
import de.jreality.util.SceneGraphUtility;
import de.jreality.util.Secure;
import de.jreality.util.SystemProperties;

enum ScaleType {
	LINEAR, LOG2, LOG10
}

enum DisplayType {
	GRID, LINE
}

enum PlotingMode {
	MULTI1DPLOT, SINGLE2D
}

/**
 * Panel that contains all the functionality to do 3D Plotting inside the GDA.
 */

public class DataSetPlot3D extends JPanel implements ActionListener, ChangeListener {

	private IMainPlotVisualiser visualiser = null;
	private DoubleDataset currentData = null;
	private JLabel lblScaling = null;
	private JLabel lblGraphRendering = null;
	private JLabel lblWindow = null;
	private JPanel controlPanel = null;
	private JCheckBox chkShowBBox = null;
	private JCheckBox chkShowCoordGrid = null;
	private JCheckBox chkShowCoordTicks = null;
	private JCheckBox chkShowCoordLabels = null;
	private JComboBox cmbGraphRendering = null;
	private JComboBox cmbScaling = null;
	private DataWindowPanel pnlWindow = null;
	private SceneGraphComponent root = null;
	private SceneGraphComponent graph = null;
	private SceneGraphComponent bbox = null;
	private SceneGraphComponent coordTicks = null;
	private SceneGraphComponent coordAxes = null;
	private SceneGraphComponent coordXLabels = null;
	private SceneGraphComponent coordYLabels = null;
	private SceneGraphComponent coordZLabels = null;
	private SceneGraphComponent coordGrid = null;
	private ViewerApp viewerApp = null;
	@SuppressWarnings("unused")
	private DataSet3DToolMenu tools = null;
	private static final double MAXX = 15.0;
	private static final double MAXY = 15.0;
	private static final double MAXZ = 15.0;
	private int MAXSOFTDIMENSION = 256;
	private int MAXSOFTDIMENSIONSQR = MAXSOFTDIMENSION * MAXSOFTDIMENSION;

	private double globalZmin = Double.MAX_VALUE;
	private double globalZmax = Double.MIN_VALUE;
	private double globalRealZmin = Double.MAX_VALUE;
	private double globalRealZmax = Double.MIN_VALUE;

	private int globalXdim = 0;
	private int globalYdim = 0;
	private int dataWindowX = 0;
	private int dataWindowY = 0;

	private boolean subSample = false;
	private boolean useWindow = false;
	private boolean ignoreComboBoxEvents = false;
	private boolean hasJOGL = false;

	private ImageData colourTable = null;
	private ScaleType currentScaling = ScaleType.LINEAR;
	private DisplayType currentDisplay = DisplayType.GRID;
	private PlotingMode currentMode = PlotingMode.SINGLE2D;
	private final LinkedList<DoubleDataset> currentDataSets = new LinkedList<DoubleDataset>();
	private IDataSet3DCorePlot current3DPlotter = null;

	private static final Logger logger = LoggerFactory.getLogger(DataSetPlot3D.class);

	/**
	 * Define the handness of the coordinate system
	 */
	public static final double HANDNESS = 1.0; // -1.0 right hand system 1.0 left hand system

	/**
	 * @param in
	 *            input z value
	 * @param currentScaling
	 *            current scaling type
	 * @return scaled value
	 */
	public static double zScaler(double in, ScaleType currentScaling) {
		double out = in;
		switch (currentScaling) {
		case LINEAR:
			break;
		case LOG2:
			if (in > 0.0) {
				out = Math.log10((in + 1)) / Math.log10(2.0);
			} else {
				out = 0.0f;
			}
			break;
		case LOG10:
			if (in > 0.0) {
				out = Math.log10((in + 1));
			} else {
				out = in;
			}
			break;
		}
		return out;
	}

	/**
	 * Compute the inverse operation of the axis scaling
	 * 
	 * @param in
	 *            scaled value
	 * @param currentScaling
	 *            current scaling mode
	 * @return inverse scaled value
	 */

	public static double inverseScaler(double in, ScaleType currentScaling) {
		double out = in;
		switch (currentScaling) {
		case LINEAR:
			break;
		case LOG2:
			out = Math.pow(2.0, in) - 1.0;
			break;
		case LOG10:
			out = Math.pow(10.0, in) - 1.0;
			break;
		}
		return out;
	}

	private SceneGraphComponent buildBoundingBox(double xAxis, double yAxis, double zMin, double zCoord) {
		SceneGraphComponent sgc = SceneGraphUtility.createFullSceneGraphComponent("BBox");
		double[][] vertices = new double[][] { { 0, zMin, 0 }, { xAxis, zMin, 0 }, { xAxis, zCoord, 0 },
				{ 0, zCoord, 0 }, { 0, zMin, yAxis * HANDNESS }, { xAxis, zMin, yAxis * HANDNESS },
				{ xAxis, zCoord, yAxis * HANDNESS }, { 0, zCoord, yAxis * HANDNESS } };
		int[][] edgeIndices = new int[][] { { 0, 1 }, { 1, 2 }, { 2, 3 }, { 3, 0 }, { 4, 5 }, { 5, 6 }, { 6, 7 },
				{ 7, 4 }, { 0, 4 }, { 1, 5 }, { 2, 6 }, { 3, 7 } };

		IndexedLineSetFactory indexLineFactory = new IndexedLineSetFactory();
		indexLineFactory.setVertexCount(vertices.length);
		indexLineFactory.setEdgeCount(edgeIndices.length);
		indexLineFactory.setVertexCoordinates(vertices);
		indexLineFactory.setEdgeIndices(edgeIndices);
		indexLineFactory.update();
		sgc.setGeometry(indexLineFactory.getIndexedLineSet());

		Appearance ap = new Appearance();
		ap.setAttribute(CommonAttributes.VERTEX_DRAW, false);
		ap.setAttribute(CommonAttributes.EDGE_DRAW, true);
		ap.setAttribute(CommonAttributes.TUBES_DRAW, false);
		ap.setAttribute(CommonAttributes.FACE_DRAW, false);
		ap.setAttribute(CommonAttributes.TUBE_RADIUS, 2.0);
		ap.setAttribute(CommonAttributes.DIFFUSE_COLOR, java.awt.Color.WHITE);
		sgc.setAppearance(ap);

		return sgc;

	}

	private void buildCoordAxes(double xAxis, double yAxis, double zAxis, double zMin) {
		if (coordAxes == null) {
			coordAxes = SceneGraphUtility.createFullSceneGraphComponent("AxisSystem");
		}

		double[][] coords = new double[5][3];

		int lineFace[][] = new int[][] { { 0, 1 }, { 2, 3 }, { 0, 4 } };
		coords[0][0] = 0.0;
		coords[0][1] = 0.0;
		coords[0][2] = 0.0;
		coords[1][0] = xAxis;
		coords[1][1] = 0.0;
		coords[1][2] = 0.0;

		coords[2][0] = 0.0;
		coords[2][1] = zMin;
		coords[2][2] = 0.0;

		coords[3][0] = 0.0;
		coords[3][1] = zAxis;
		coords[3][2] = 0.0;

		coords[4][0] = 0.0;
		coords[4][1] = 0.0;
		coords[4][2] = yAxis * HANDNESS;

		IndexedLineSetFactory indexLineFactory = new IndexedLineSetFactory();
		indexLineFactory.setVertexCount(5);
		indexLineFactory.setVertexCoordinates(coords);
		indexLineFactory.setEdgeCount(3);
		indexLineFactory.setEdgeIndices(lineFace);
		indexLineFactory.setEdgeColors(new java.awt.Color[] { java.awt.Color.RED, java.awt.Color.green,
				java.awt.Color.blue });
		indexLineFactory.setEdgeLabels(new String[] { "X-Axis", "Z-Axis", "Y-Axis" });
		indexLineFactory.update();
		Appearance ap = new Appearance();
		ap.setAttribute(CommonAttributes.VERTEX_DRAW, false);
		ap.setAttribute(CommonAttributes.EDGE_DRAW, true);
		ap.setAttribute(CommonAttributes.TUBES_DRAW, false);
		ap.setAttribute(CommonAttributes.FACE_DRAW, false);
		ap.setAttribute(CommonAttributes.TUBE_RADIUS, 2.0);
		ap.setAttribute("lineShader.textShader.alignment", SwingConstants.RIGHT);
		ap.setAttribute("lineShader.textShader.scale", 0.008);
		coordAxes.setGeometry(indexLineFactory.getIndexedLineSet());
		coordAxes.setAppearance(ap);
	}

	private void fillComboBox(PlotingMode mode) {
		ignoreComboBoxEvents = true;
		cmbGraphRendering.removeAllItems();
		if (mode == PlotingMode.SINGLE2D) {
			cmbGraphRendering.addItem("Fill Shaded");
			cmbGraphRendering.addItem("Wireframe");
			cmbGraphRendering.addItem("Line graph");
			cmbGraphRendering.addItem("Dots");
			cmbGraphRendering.addItem("Fill + wireframe");
		} else {
			cmbGraphRendering.addItem("Line graph");
			cmbGraphRendering.addItem("Dots");
		}
		ignoreComboBoxEvents = false;
	}

	private void buildControlPanel() {
		if (controlPanel != null) {
			GridBagLayout layout = new GridBagLayout();
			GridBagConstraints gridConst = new GridBagConstraints();

			// MigLayout layout = new MigLayout();
			controlPanel.setLayout(layout);
			chkShowBBox = new JCheckBox("Show bounding box");
			chkShowBBox.addActionListener(this);
			chkShowCoordGrid = new JCheckBox("Show coordinate grid");
			chkShowCoordGrid.addActionListener(this);
			chkShowCoordTicks = new JCheckBox("Show coordinate ticks");
			chkShowCoordTicks.addActionListener(this);
			chkShowCoordLabels = new JCheckBox("Show coordinate labels");
			chkShowCoordLabels.addActionListener(this);
			lblGraphRendering = new JLabel("Display as:");
			cmbGraphRendering = new JComboBox();
			fillComboBox(currentMode);
			cmbGraphRendering.addActionListener(this);
			lblScaling = new JLabel("Z-Axis scale:");
			cmbScaling = new JComboBox();
			cmbScaling.addItem("Linear");
			cmbScaling.addItem("Log2");
			cmbScaling.addItem("Log10");
			cmbScaling.addActionListener(this);
			pnlWindow = new DataWindowPanel();
			pnlWindow.setEnabled(false);
			lblWindow = new JLabel("Data Window");
			gridConst.fill = GridBagConstraints.NONE;
			gridConst.weightx = 0.0;
			gridConst.weighty = 0.0;
			gridConst.anchor = GridBagConstraints.WEST;
			gridConst.gridx = 0;
			gridConst.gridy = 0;
			layout.setConstraints(chkShowBBox, gridConst);
			controlPanel.add(chkShowBBox);
			gridConst.gridx = 0;
			gridConst.gridy = 1;
			layout.setConstraints(chkShowCoordGrid, gridConst);
			controlPanel.add(chkShowCoordGrid);
			gridConst.gridx = 1;
			gridConst.gridy = 0;
			layout.setConstraints(chkShowCoordTicks, gridConst);
			controlPanel.add(chkShowCoordTicks);
			gridConst.gridx = 1;
			gridConst.gridy = 1;
			layout.setConstraints(chkShowCoordLabels, gridConst);
			controlPanel.add(chkShowCoordLabels);
			gridConst.gridx = 2;
			gridConst.gridy = 0;
			layout.setConstraints(lblGraphRendering, gridConst);
			controlPanel.add(lblGraphRendering);
			gridConst.gridx = 3;
			gridConst.gridy = 0;
			layout.setConstraints(cmbGraphRendering, gridConst);
			controlPanel.add(cmbGraphRendering);
			gridConst.gridx = 2;
			gridConst.gridy = 1;
			layout.setConstraints(lblScaling, gridConst);
			controlPanel.add(lblScaling);
			gridConst.gridx = 3;
			gridConst.gridy = 1;
			layout.setConstraints(cmbScaling, gridConst);
			controlPanel.add(cmbScaling);
			gridConst.gridx = 0;
			gridConst.gridy = 2;
			layout.setConstraints(lblWindow, gridConst);
			controlPanel.add(lblWindow);
			gridConst.gridx = 1;
			gridConst.gridy = 2;
			gridConst.gridwidth = 2;
			layout.setConstraints(pnlWindow, gridConst);
			controlPanel.add(pnlWindow);
			pnlWindow.addChangeListener(this);
		}
	}

	/**
	 * Constructor of a DataSetPlot3D panel
	 * 
	 * @param overlayMaster
	 */

	@SuppressWarnings("unused")
	public DataSetPlot3D(MainPlot overlayMaster) {
		root = SceneGraphUtility.createFullSceneGraphComponent("world");
		graph = SceneGraphUtility.createFullSceneGraphComponent("graph");
		coordAxes = SceneGraphUtility.createFullSceneGraphComponent("axis");

		root.addChild(coordAxes);
		root.addChild(graph);
		viewerApp = new ViewerApp(root);
		tools = new DataSet3DToolMenu(viewerApp);
		controlPanel = new JPanel();
		BoxLayout layout = new BoxLayout(this, BoxLayout.Y_AXIS);
		setLayout(layout);
		java.awt.Component comp = viewerApp.getContent();
		comp.setPreferredSize(new java.awt.Dimension(512, 480));
		comp.setMaximumSize(new java.awt.Dimension(512, 480));
		this.add(comp);
		controlPanel.setPreferredSize(new java.awt.Dimension(512, 150));
		buildControlPanel();
		this.add(controlPanel);
		// check if JOGL is available
		hasJOGL = true;
		String viewer = Secure.getProperty(SystemProperties.VIEWER, SystemProperties.VIEWER_DEFAULT_JOGL);

		try {
			Class.forName(viewer).newInstance();
		} catch (Exception e) { //
			logger.info("No JOGL using software render");
			hasJOGL = false;
		} catch (NoClassDefFoundError ndfe) {
			logger.info("No JOGL using software render");
			hasJOGL = false;
		} catch (UnsatisfiedLinkError le) {
			logger.info("No JOGL using software render");
			hasJOGL = false;
		}
		// if there is JOGL we can increase the limits
		if (hasJOGL) {
			// increase the dimension size by factor 3 for the hardware
			// at the moment should in theory far higher but because of memory
			// restrictions and performance issues in jReality

			MAXSOFTDIMENSION = MAXSOFTDIMENSION * 3;
			MAXSOFTDIMENSIONSQR = MAXSOFTDIMENSION * MAXSOFTDIMENSION;
		}
	}

	/**
	 * 
	 */
	public void dispose() {
		// TODO Auto-generated method stub
		cleanUp();

	}

	private double[] computeScalingAndCoords() {
		double[] returnValues = new double[6];
		int longestAxis = Math.max(globalXdim, globalYdim);
		double zCoord = globalZmax;
		double zScale = 1.0;

		if (zCoord > MAXZ) {
			zScale = MAXZ / zCoord;
			zCoord = MAXZ;
		}

		double xCoord = MAXX / ((double) longestAxis / (double) globalXdim);
		double yCoord = MAXY / ((double) longestAxis / (double) globalYdim);

		if (currentMode == PlotingMode.MULTI1DPLOT) {
			yCoord = Math.min(globalYdim, MAXY);
		}
		double xScale = xCoord / Math.max((globalXdim - 1), 1.0);
		double yScale = yCoord / Math.max((globalYdim - 1), 1.0);
		if (currentMode == PlotingMode.MULTI1DPLOT) {
			yScale = yCoord / globalYdim;
		}
		returnValues[0] = xCoord;
		returnValues[1] = yCoord;
		returnValues[2] = zCoord;
		returnValues[3] = xScale;
		returnValues[4] = yScale;
		returnValues[5] = zScale;
		return returnValues;
	}

	private void removeOldSceneNodes() {
		if (bbox != null) {
			root.removeChild(bbox);
			bbox = null;
		}
		if (coordTicks != null) {
			root.removeChild(coordTicks);
			coordTicks = null;
		}
		if (coordGrid != null) {
			root.removeChild(coordGrid);
			coordGrid = null;
		}
		if (coordXLabels != null) {
			root.removeChild(coordXLabels);
			coordXLabels = null;
		}
		if (coordYLabels != null) {
			root.removeChild(coordYLabels);
			coordYLabels = null;
		}
		if (coordZLabels != null) {
			root.removeChild(coordZLabels);
			coordZLabels = null;
		}

		// since we removed all the previous
		// scene nodes now might be a good time
		// to call garbage collector to make sure

		System.gc();
	}

	private void determineZRange() {
		if (currentMode == PlotingMode.SINGLE2D) {
			if (currentData != null) {
				globalZmax = zScaler(currentData.max().doubleValue(), currentScaling);
				globalZmin = zScaler(currentData.min().doubleValue(), currentScaling);
				globalRealZmax = currentData.max().doubleValue();
				globalRealZmin = currentData.min().doubleValue();
			}
		} else {
			globalZmax = 0.0;
			globalZmin = 0.0;
			double globalRealZmin = Double.MAX_VALUE;
			double globalRealZmax = Double.MIN_VALUE;
			if (currentDataSets != null && currentDataSets.size() > 0) {
				ListIterator<DoubleDataset> iter = currentDataSets.listIterator(0);
				while (iter.hasNext()) {
					DoubleDataset newData = iter.next();
					globalZmin = Math.min(globalZmin, zScaler(newData.min().doubleValue(), currentScaling));
					globalZmax = Math.max(globalZmax, zScaler(newData.max().doubleValue(), currentScaling));
					globalRealZmin = Math.min(globalRealZmin, newData.min().doubleValue());
					globalRealZmax = Math.max(globalRealZmax, newData.max().doubleValue());
				}
			}
		}
	}

	private void add1DPlot() {
		boolean success = false;
		currentMode = PlotingMode.MULTI1DPLOT;
		chkShowCoordGrid.setEnabled(false);
		while (!success) {
			// make sure to remove the old graph, ticks, grid,
			// labels before generating them again
			try {
				removeOldSceneNodes();
				globalYdim = currentDataSets.size();
				ListIterator<DoubleDataset> iter = currentDataSets.listIterator(0);
				globalXdim = 0;
				globalZmax = 0.0;
				globalZmin = 0.0;
				double realZMin = Double.MAX_VALUE;
				double realZMax = Double.MIN_VALUE;
				while (iter.hasNext()) {
					DoubleDataset newData = iter.next();
					globalXdim = Math.max(globalXdim, newData.getShape()[0]);

					globalZmin = Math.min(globalZmin, zScaler(newData.min().doubleValue(), currentScaling));
					globalZmax = Math.max(globalZmax, zScaler(newData.max().doubleValue(), currentScaling));
					realZMin = Math.min(realZMin, newData.min().doubleValue());
					realZMax = Math.max(realZMax, newData.max().doubleValue());
					globalRealZmax = Math.max(globalRealZmax, realZMax);
					globalRealZmin = Math.min(globalRealZmin, realZMin);
					if (globalXdim * globalYdim > MAXSOFTDIMENSIONSQR) {
						int maximumDim = MAXSOFTDIMENSIONSQR / globalYdim;
						current3DPlotter.setDataDimensions(maximumDim, globalYdim);

						if (subSample) {
							logger.warn("The data set is too big to be displayed all at once! Using subsampling");
						} else {
							logger.warn("The data set is too big to be displayed all at once! Using a default window");
							pnlWindow.setEnabled(true);
							pnlWindow.setDataSetSize(globalXdim, globalYdim);
							pnlWindow.setWindowSize(maximumDim, globalYdim);
							globalXdim = maximumDim;
						}
					}
				}
				buildSceneNodes(realZMin, realZMax);
				success = true;
			} catch (final java.lang.OutOfMemoryError error) {
				MAXSOFTDIMENSIONSQR = MAXSOFTDIMENSIONSQR >> 1;
				logger.warn("Not enough memory to execute the plot will autodownscale by 2 and try again");
				success = false;
			}
		}
	}

	private void buildSceneNodes(double zRealMin, double zRealMax) {
		double[] values = computeScalingAndCoords();
		double xCoord = values[0];
		double yCoord = values[1];
		double zCoord = values[2];
		double xScale = values[3];
		double yScale = values[4];
		double zScale = values[5];

		buildCoordAxes(xCoord, yCoord, zCoord, (globalZmin < 0.0 ? globalZmin : 0.0));

		if (chkShowCoordTicks.isSelected()) {
			coordTicks = current3DPlotter.buildCoordAxesTicks(xCoord, yCoord, zCoord, (globalZmin < 0.0 ? globalZmin
					: 0.0), globalZmax, globalXdim, globalYdim);

			root.addChild(coordTicks);
		}
		if (chkShowCoordGrid.isSelected()) {
			coordGrid = current3DPlotter.buildCoordGrid(xCoord, yCoord, globalXdim, globalYdim);
			root.addChild(coordGrid);
		}
		if (chkShowBBox.isSelected()) {
			bbox = buildBoundingBox(xCoord, yCoord, globalZmin, zCoord);
			root.addChild(bbox);
		}
		if (chkShowCoordLabels.isSelected()) {
			coordXLabels = current3DPlotter.buildXCoordLabeling(xCoord, globalXdim);
			coordYLabels = current3DPlotter.buildYCoordLabeling(yCoord, globalYdim);
			coordZLabels = current3DPlotter.buildZCoordLabeling(zRealMin, zRealMax);
			root.addChild(coordXLabels);
			root.addChild(coordYLabels);
			root.addChild(coordZLabels);
		}
		graph = current3DPlotter.buildGraph(currentDataSets, graph, xScale, yScale, zScale, currentDisplay);
	}

	private void add2DPlot(DoubleDataset doubleDataset) {
		currentData = doubleDataset;
		current3DPlotter = new DataSet3DPlot2D(useWindow, hasJOGL, dataWindowX, dataWindowY);
		current3DPlotter.setScaling(currentScaling);
		currentMode = PlotingMode.SINGLE2D;
		boolean success = false;
		chkShowCoordGrid.setEnabled(true);
		MAXSOFTDIMENSIONSQR = MAXSOFTDIMENSION * MAXSOFTDIMENSION;
		current3DPlotter.setDataDimensions(MAXSOFTDIMENSION, MAXSOFTDIMENSION);
		while (!success) {
			// make sure to remove the old scene nodes like ticks, grid,
			// labels before generating them again
			try {
				removeOldSceneNodes();
				globalXdim = doubleDataset.getShape()[1];
				globalYdim = doubleDataset.getShape()[0];
				if (globalXdim * globalYdim > MAXSOFTDIMENSIONSQR) {
					if (subSample) {
						logger.warn("The data set is too big to be displayed all at once! Using subsampling");
					} else {
						logger.warn("The data set is too big to be displayed all at once! Using a default window");
					}

					current3DPlotter.setDataDimensions(MAXSOFTDIMENSION, MAXSOFTDIMENSION);
				}
				pnlWindow.setDataSetSize(globalXdim, globalYdim);
				pnlWindow.setWindowSize(globalXdim, globalYdim);
				if (useWindow) {
					pnlWindow.setEnabled(false);
					if (globalXdim > MAXSOFTDIMENSION) {
						pnlWindow.setEnabled(true);
						globalXdim = MAXSOFTDIMENSION;
						pnlWindow.setWindowSize(MAXSOFTDIMENSION, MAXSOFTDIMENSION);
					}
					if (globalYdim > MAXSOFTDIMENSION) {
						pnlWindow.setEnabled(true);
						globalYdim = MAXSOFTDIMENSION;
						pnlWindow.setWindowSize(MAXSOFTDIMENSION, MAXSOFTDIMENSION);
					}
				}

				globalZmax = zScaler(currentData.max().doubleValue(), currentScaling);
				globalZmin = zScaler(currentData.min().doubleValue(), currentScaling);
				globalRealZmin = currentData.min().doubleValue();
				globalRealZmax = currentData.max().doubleValue();
				buildSceneNodes(currentData.min().doubleValue(), currentData.max().doubleValue());

				success = true;
			} catch (final java.lang.OutOfMemoryError error) {
				logger.warn("Not enough memory to execute the plot will autoscale by 2 and try again");
				MAXSOFTDIMENSION = MAXSOFTDIMENSION >> 1;
				MAXSOFTDIMENSIONSQR = MAXSOFTDIMENSION * MAXSOFTDIMENSION;
				success = false;
				System.gc();
			}
		}
	}

	/**
	 * @param doubleDataset
	 *            DataSet that should be plotted
	 * @param subsample
	 *            should sub sampling be used (true) otherwise use a window on the data
	 */

	public void setPlot(DoubleDataset doubleDataset, boolean subsample) {
		globalZmin = Double.MAX_VALUE;
		globalZmax = Double.MIN_VALUE;
		globalRealZmin = Double.MAX_VALUE;
		globalRealZmax = Double.MIN_VALUE;
		currentDataSets.clear();
		currentDataSets.add(doubleDataset);
		subSample = subsample;
		useWindow = !subsample;
		if (doubleDataset.getRank() > 1) {
			fillComboBox(PlotingMode.SINGLE2D);
			add2DPlot(doubleDataset);
		} else {
			fillComboBox(PlotingMode.MULTI1DPLOT);
			current3DPlotter = new DataSet3DPlot1Ds(useWindow);
			current3DPlotter.setScaling(currentScaling);
			add1DPlot();
		}
		// now set the camera
		Camera sceneCamera = CameraUtility.getCamera(viewerApp.getCurrentViewer());
		sceneCamera.setFieldOfView(60);
		sceneCamera.setNear(1.0);
		sceneCamera.setFar(100);
		System.gc();
		CameraUtility.encompass(viewerApp.getCurrentViewer());
		System.gc();
	}

	/**
	 * Add more 1D plots to the graph
	 * 
	 * @param datasets
	 *            n number of 1D datasets that should be plotted
	 */

	public void addPlot(DoubleDataset... datasets) {
		if (currentMode == PlotingMode.SINGLE2D) {
			logger.info("Plot3D is currently in 2D mode but 1D plot has been added switching to Multi 1D");
			globalZmin = Double.MAX_VALUE;
			globalZmax = Double.MIN_VALUE;
			globalRealZmin = Double.MAX_VALUE;
			globalRealZmax = Double.MIN_VALUE;
			currentDataSets.clear();
			for (DoubleDataset dataset : datasets) {
				currentDataSets.add(dataset);
			}

			current3DPlotter = new DataSet3DPlot1Ds(useWindow);
			current3DPlotter.setScaling(currentScaling);
			fillComboBox(PlotingMode.MULTI1DPLOT);
			add1DPlot();

			// now set the camera
			Camera sceneCamera = CameraUtility.getCamera(viewerApp.getCurrentViewer());
			sceneCamera.setFieldOfView(60);
			sceneCamera.setNear(1.0);
			sceneCamera.setFar(100);
			System.gc();
			CameraUtility.encompass(viewerApp.getCurrentViewer());
		} else {
			for (DoubleDataset dataset : datasets) {
				currentDataSets.add(dataset);
			}
			add1DPlot();
		}
	}

	/**
	 * Gets the visualiser which is to be used for colour-casting the graph
	 * 
	 * @return the visualiser which is in use.
	 */
	public IMainPlotVisualiser getVisualiser() {
		return visualiser;
	}

	/**
	 * Sets the visualiser to use.
	 * 
	 * @param visualiser
	 */

	public void setVisualiser(IMainPlotVisualiser visualiser) {
		this.visualiser = visualiser;
	}

	/**
	 * This function applies the colour cast which is specified by the IMainPlotVisualiser which can be accessed by set
	 * and get Visualiser
	 */
	public void applyColorCast() {

		// this should now all be replaced with the new functionality
		if (currentDataSets.size() > 0 && graph != null) {
			colourTable = null;
			System.gc();
			colourTable = visualiser.cast(currentData);
			current3DPlotter.handleColourCast(colourTable, graph, globalXdim, globalYdim);
			pnlWindow.setOverviewImage(colourTable);
			// it would also be useful to suggest a GC at this point
		}
		System.gc();

	}

	@Override
	public void actionPerformed(java.awt.event.ActionEvent event) {
		// compute some prequisits which are used in
		// most of the cases

		determineZRange();
		double[] values = computeScalingAndCoords();
		double xCoord = values[0];
		double yCoord = values[1];
		double zCoord = values[2];

		if (event.getSource().equals(chkShowBBox)) {
			if (chkShowBBox.isSelected()) {

				bbox = buildBoundingBox(xCoord, yCoord, globalZmin, zCoord);
				root.addChild(bbox);
			} else {
				root.removeChild(bbox);
				bbox = null;
			}
		} else if (event.getSource().equals(chkShowCoordTicks)) {
			if (chkShowCoordTicks.isSelected()) {
				coordTicks = current3DPlotter.buildCoordAxesTicks(xCoord, yCoord, zCoord,
						(globalZmin < 0.0 ? globalZmin : 0.0), globalZmax, globalXdim, globalYdim);
				root.addChild(coordTicks);
			} else {
				root.removeChild(coordTicks);
				coordTicks = null;
			}
		} else if (event.getSource().equals(chkShowCoordGrid)) {
			if (chkShowCoordGrid.isSelected()) {
				coordGrid = current3DPlotter.buildCoordGrid(xCoord, yCoord, globalXdim, globalYdim);
				root.addChild(coordGrid);
			} else {
				root.removeChild(coordGrid);
				coordGrid = null;
			}
		} else if (event.getSource().equals(chkShowCoordLabels)) {
			if (chkShowCoordLabels.isSelected()) {
				coordXLabels = current3DPlotter.buildXCoordLabeling(xCoord, globalXdim);
				coordYLabels = current3DPlotter.buildYCoordLabeling(yCoord, globalYdim);
				coordZLabels = current3DPlotter.buildZCoordLabeling(globalRealZmin, globalRealZmax);
				root.addChild(coordXLabels);
				root.addChild(coordYLabels);
				root.addChild(coordZLabels);
			} else {
				root.removeChild(coordXLabels);
				root.removeChild(coordYLabels);
				root.removeChild(coordZLabels);
				coordXLabels = null;
				coordYLabels = null;
				coordZLabels = null;
			}
		} else if (event.getSource().equals(cmbGraphRendering) && !ignoreComboBoxEvents) {
			graph = current3DPlotter.setDisplayMode(root, graph, currentDataSets, cmbGraphRendering.getSelectedIndex());
			currentDisplay = current3DPlotter.getCurrentDisplay();
		} else if (event.getSource().equals(cmbScaling)) {
			switch (cmbScaling.getSelectedIndex()) {
			case 0:
				currentScaling = ScaleType.LINEAR;
				break;
			case 1:
				currentScaling = ScaleType.LOG2;
				break;
			case 2:
				currentScaling = ScaleType.LOG10;
				break;
			}
			current3DPlotter.setScaling(currentScaling);

			if (bbox != null) {
				root.removeChild(bbox);
				bbox = null;
			}
			if (coordTicks != null) {
				root.removeChild(coordTicks);
				coordTicks = null;
			}
			if (coordZLabels != null) {
				root.removeChild(coordZLabels);
				coordZLabels = null;
			}
			// since we removed all the nodes lets
			// see if we can clear some memory before
			// adding the new nodes

			System.gc();

			determineZRange();

			values = computeScalingAndCoords();
			double xScale = values[3];
			double yScale = values[4];
			zCoord = values[2];
			double zScale = values[5];

			buildCoordAxes(xCoord, yCoord, zCoord, (globalZmin < 0.0 ? globalZmin : 0.0));

			if (chkShowCoordTicks.isSelected()) {
				coordTicks = current3DPlotter.buildCoordAxesTicks(xCoord, yCoord, zCoord,
						(globalZmin < 0.0 ? globalZmin : 0.0), globalZmax, globalXdim, globalYdim);

				root.addChild(coordTicks);
			}
			if (chkShowCoordLabels.isSelected()) {
				coordZLabels = current3DPlotter.buildZCoordLabeling(globalRealZmin, globalRealZmax);
				root.addChild(coordZLabels);
			}
			if (chkShowBBox.isSelected()) {
				bbox = buildBoundingBox(xCoord, yCoord, globalZmin, zCoord);
				root.addChild(bbox);
			}
			graph = current3DPlotter.buildGraph(currentDataSets, graph, xScale, yScale, zScale, currentDisplay);
		}
		System.gc();
	}

	private void movedWindow() {
		current3DPlotter.setNewWindowPos(dataWindowX, dataWindowY);
		if (coordXLabels != null) {
			root.removeChild(coordXLabels);
			coordXLabels = null;
		}
		if (coordYLabels != null) {
			root.removeChild(coordYLabels);
			coordYLabels = null;
		}
		determineZRange();
		double[] values = computeScalingAndCoords();
		double xCoord = values[0];
		double yCoord = values[1];
		double xScale = values[3];
		double yScale = values[4];
		double zScale = values[5];

		if (chkShowCoordLabels.isSelected()) {
			coordXLabels = current3DPlotter.buildXCoordLabeling(xCoord, globalXdim);
			coordYLabels = current3DPlotter.buildYCoordLabeling(yCoord, globalYdim);
			root.addChild(coordXLabels);
			root.addChild(coordYLabels);
		}

		graph = current3DPlotter.buildGraph(currentDataSets, graph, xScale, yScale, zScale, currentDisplay);
	}

	/**
	 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
	 */

	@Override
	public void stateChanged(ChangeEvent evt) {
		dataWindowX = pnlWindow.getWindowXPos();
		dataWindowY = pnlWindow.getWindowYPos();
		movedWindow();
	}

	/**
	 * CleanUp the data when it is inactive, that hopefully will reduce the memory footprint
	 */
	public void cleanUp() {
		chkShowBBox.setSelected(false);
		chkShowCoordGrid.setSelected(false);
		chkShowCoordLabels.setSelected(false);
		chkShowCoordTicks.setSelected(false);
		if (bbox != null) {
			root.removeChild(bbox);
		}
		if (coordGrid != null) {
			root.removeChild(coordGrid);
		}
		if (coordXLabels != null) {
			root.removeChild(coordXLabels);
		}
		if (coordYLabels != null) {
			root.removeChild(coordYLabels);
		}
		if (coordZLabels != null) {
			root.removeChild(coordZLabels);
		}
		if (coordTicks != null) {
			root.removeChild(coordTicks);
		}

		bbox = null;
		coordTicks = null;
		coordXLabels = null;
		coordYLabels = null;
		coordZLabels = null;
		coordGrid = null;
		currentData = null;
		current3DPlotter = null;
		currentDataSets.clear();
		System.gc();
	}
}
