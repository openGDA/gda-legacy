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

import java.util.LinkedList;
import java.util.ListIterator;

import org.eclipse.january.dataset.DoubleDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jreality.geometry.IndexedLineSetFactory;
import de.jreality.geometry.PointSetFactory;
import de.jreality.scene.Appearance;
import de.jreality.scene.Geometry;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.DefaultGeometryShader;
import de.jreality.shader.ShaderUtility;
import de.jreality.util.SceneGraphUtility;

/**
 * This class is responsible for plotting many one dimensional datasets into the same graph
 */

public class DataSet3DPlot1Ds implements IDataSet3DCorePlot {

	private boolean useWindow;
	private boolean subSample;
	private int dataWindowX = 0;
	private int dataWindowY = 0;
	private ImageData colourTable = null;
	private ScaleType currentScaling = ScaleType.LINEAR;
	private Appearance graphAppearance = null;
	private int maxDataXsize = Integer.MAX_VALUE;
	private int maxDataYsize = Integer.MAX_VALUE;

	private static final Logger logger = LoggerFactory.getLogger(DataSet3DPlot1Ds.class);

	private int determineNumTicks(int dim) {
		int numTicks = dim;
		if (numTicks > MAXTICKS)
			numTicks = dim / (int) Math.ceil(dim / MAXTICKS);
		return numTicks;
	}

	private int determineNumLabels(int dim) {
		int numLabels = dim;
		if (numLabels > MAXLABELS)
			numLabels = dim / (int) Math.ceil(dim / MAXLABELS);
		return numLabels;
	}

	private float[] determineSamplingRate(int xSize) {
		float[] returnValues = new float[2];
		int displayXSize = xSize;
		float samplingRate = 1.0f;

		if (xSize > maxDataXsize && subSample) {
			samplingRate = (float) xSize / (float) maxDataXsize;
			displayXSize = (int) (xSize / samplingRate);
		}
		returnValues[0] = samplingRate;
		returnValues[1] = displayXSize;
		return returnValues;
	}

	/**
	 * Constructor for DataSet3DPlot1Ds
	 * 
	 * @param useWindow
	 *            should window on data method be used if data size is too large
	 */
	public DataSet3DPlot1Ds(boolean useWindow) {
		this.useWindow = useWindow;
		this.subSample = !useWindow;
	}

	@Override
	public SceneGraphComponent buildCoordAxesTicks(double xAxis, double yAxis, double zAxis, double zMin, double zMax,
			int xDim, int yDim) {
		SceneGraphComponent sgc = SceneGraphUtility.createFullSceneGraphComponent("AxisTicks");

		int numXticks = determineNumTicks(xDim);
		int numYticks = yDim;

		int numZticks = (int) Math.ceil(Math.abs((zMin < -MAXZ ? -MAXZ : zMin)) + (zMax > MAXZ ? MAXZ : zMax)
				/ (MAXZ / MAXTICKS));

		double[][] coords = new double[numXticks * 2 + numYticks * 2 + numZticks * 2][3];

		int lineFaces[][] = new int[numXticks + numYticks + numZticks][2];
		java.awt.Color colours[] = new java.awt.Color[numXticks + numYticks + numZticks];

		double xStepSize = xAxis / numXticks;
		double yStepSize = yAxis / numYticks;
		double zStepSize = (zAxis + Math.abs((zMin < -MAXZ ? -MAXZ : zMin))) / numZticks;
		for (int i = 0; i < numXticks; i++) {
			lineFaces[i][0] = i * 2;
			lineFaces[i][1] = i * 2 + 1;
			coords[i * 2][0] = xStepSize * i;
			coords[i * 2][1] = 0.0;
			coords[i * 2][2] = 0.0;
			coords[i * 2 + 1][0] = xStepSize * i;
			coords[i * 2 + 1][1] = 0.0;
			coords[i * 2 + 1][2] = 0.25 * -DataSetPlot3D.HANDNESS;
			colours[i] = java.awt.Color.RED;
		}

		for (int i = 0; i < numYticks; i++) {
			lineFaces[i + numXticks][0] = i * 2 + numXticks * 2;
			lineFaces[i + numXticks][1] = i * 2 + 1 + numXticks * 2;
			coords[i * 2 + numXticks * 2][0] = -0.25;
			coords[i * 2 + numXticks * 2][1] = 0.0;
			coords[i * 2 + numXticks * 2][2] = DataSetPlot3D.HANDNESS * yStepSize * i + DataSetPlot3D.HANDNESS * 0.5
					* yStepSize;
			coords[i * 2 + 1 + numXticks * 2][0] = 0.0;
			coords[i * 2 + 1 + numXticks * 2][1] = 0.0;
			coords[i * 2 + 1 + numXticks * 2][2] = DataSetPlot3D.HANDNESS * yStepSize * i + DataSetPlot3D.HANDNESS
					* 0.5 * yStepSize;
			colours[i + numXticks] = java.awt.Color.BLUE;
		}

		for (int i = 0; i < numZticks; i++) {
			lineFaces[i + numXticks + numYticks][0] = i * 2 + numXticks * 2 + numYticks * 2;
			lineFaces[i + numXticks + numYticks][1] = i * 2 + 1 + numXticks * 2 + numYticks * 2;
			coords[i * 2 + numXticks * 2 + numYticks * 2][0] = -0.25;
			coords[i * 2 + numXticks * 2 + numYticks * 2][1] = zMin + zStepSize * i;
			coords[i * 2 + numXticks * 2 + numYticks * 2][2] = 0.0;
			coords[i * 2 + 1 + numXticks * 2 + numYticks * 2][0] = 0.0;
			coords[i * 2 + 1 + numXticks * 2 + numYticks * 2][1] = zMin + zStepSize * i;
			coords[i * 2 + 1 + numXticks * 2 + numYticks * 2][2] = 0.0;
			colours[i + numXticks + numYticks] = java.awt.Color.GREEN;
		}

		IndexedLineSetFactory indexLineFactory = new IndexedLineSetFactory();
		indexLineFactory.setVertexCount(numXticks * 2 + numYticks * 2 + numZticks * 2);
		indexLineFactory.setEdgeCount(numXticks + numYticks + numZticks);
		indexLineFactory.setVertexCoordinates(coords);
		indexLineFactory.setEdgeIndices(lineFaces);
		indexLineFactory.setEdgeColors(colours);
		indexLineFactory.update();
		sgc.setGeometry(indexLineFactory.getIndexedLineSet());

		Appearance ap = new Appearance();
		ap.setAttribute(CommonAttributes.VERTEX_DRAW, false);
		ap.setAttribute(CommonAttributes.EDGE_DRAW, true);
		ap.setAttribute(CommonAttributes.TUBES_DRAW, false);
		ap.setAttribute(CommonAttributes.FACE_DRAW, false);
		ap.setAttribute(CommonAttributes.TUBE_RADIUS, 2.0);
		sgc.setAppearance(ap);
		return sgc;
	}

	@Override
	public SceneGraphComponent buildCoordGrid(double axis, double axis2, int dim, int dim2) {
		SceneGraphComponent sgc = SceneGraphUtility.createFullSceneGraphComponent("AxisGrid");
		return sgc;
	}

	private Geometry buildLineGraph(final double[][] coords, final double[][] colours, int xSize, int ySize) {
		IndexedLineSetFactory factory = new IndexedLineSetFactory();
		factory.setVertexCount(xSize * ySize);
		factory.setEdgeCount((xSize - 1) * ySize);
		factory.setVertexCoordinates(coords);
		factory.setVertexColors(colours);
		int edgeFaces[][] = new int[(xSize - 1) * ySize][2];
		for (int y = 0; y < ySize; y++)
			for (int x = 0; x < xSize - 1; x++) {
				edgeFaces[x + y * (xSize - 1)][0] = x + y * xSize;
				edgeFaces[x + y * (xSize - 1)][1] = x + 1 + y * xSize;
			}
		factory.setEdgeIndices(edgeFaces);
		factory.update();
		return factory.getIndexedLineSet();
	}

	@Override
	public SceneGraphComponent buildGraph(final LinkedList<DoubleDataset> datasets, SceneGraphComponent graph, double xScale,
			double yScale, double zScale, DisplayType display) {
		if (graph == null) {
			graph = SceneGraphUtility.createFullSceneGraphComponent("Graph");
		}
		int xSize = 0;
		int ySize = datasets.size();
		ListIterator<DoubleDataset> iter = datasets.listIterator(0);
		double zMax = Double.MIN_VALUE;
		double zMin = Double.MAX_VALUE;
		while (iter.hasNext()) {
			DoubleDataset newData = iter.next();
			DataSetPlot3D.zScaler(newData.max().doubleValue(), currentScaling);
			xSize = Math.max(xSize, newData.getShape()[0]);
			zMax = Math.max(zMax, DataSetPlot3D.zScaler(newData.max().doubleValue(), currentScaling));
			zMin = Math.min(zMin, DataSetPlot3D.zScaler(newData.min().doubleValue(), currentScaling));
		}
		int displayXSize = xSize;
		int displayYSize = ySize;
		float samplingRate = 1.0f;
		if (subSample) {
			float[] sampling = determineSamplingRate(xSize);
			displayXSize = (int) sampling[1];
			samplingRate = sampling[0];
			if (samplingRate != 1.0f)
				logger.warn("Sampling rate " + samplingRate);
		} else {
			displayXSize = Math.min(displayXSize, maxDataXsize);
			displayYSize = Math.min(displayYSize, maxDataYsize);
		}
		double[][] coords = new double[displayYSize * displayXSize][3];
		double[][] colours = new double[displayYSize * displayXSize][3];
		for (int y = 0; y < displayYSize; y++) {
			DoubleDataset newData = datasets.get(y);
			for (int x = 0; x < displayXSize; x++) {
				int dataXPos = x;
				int dataYPos = y;
				if (useWindow) {
					dataXPos += dataWindowX;
					dataYPos += dataWindowY;
				}
				int xEntry = (int) (dataXPos * samplingRate);
				xEntry = Math.min(xEntry, newData.getShape()[0] - 1);
				double dataEntry = DataSetPlot3D.zScaler(newData.get(xEntry), currentScaling);

				coords[x + y * displayXSize][0] = xScale * (x * samplingRate);
				coords[x + y * displayXSize][1] = dataEntry * zScale;
				coords[x + y * displayXSize][2] = DataSetPlot3D.HANDNESS * yScale * y + DataSetPlot3D.HANDNESS * 0.5
						* yScale;

				// if there is a colour table use it for colouring
				// otherwise just use a greyscale ramp
				if (colourTable != null) {
					int packedRGBcolour = colourTable.get((int) (dataXPos * samplingRate),
							(int) (dataYPos * samplingRate));
					int red = (packedRGBcolour >> 16) & 0xff;
					int green = (packedRGBcolour >> 8) & 0xff;
					int blue = (packedRGBcolour) & 0xff;
					colours[x + y * displayXSize][0] = red / 255.0;
					colours[x + y * displayXSize][1] = green / 255.0;
					colours[x + y * displayXSize][2] = blue / 255.0;
				} else {
					colours[x + y * displayXSize][0] = (Math.abs(zMin) + dataEntry) / (zMax + Math.abs(zMin));
					colours[x + y * displayXSize][1] = colours[x + y * displayXSize][0];
					colours[x + y * displayXSize][2] = colours[x + y * displayXSize][0];
				}
			}
		}
		graph.setGeometry(buildLineGraph(coords, colours, displayXSize, displayYSize));
		graphAppearance = graph.getAppearance();
		DefaultGeometryShader dgs = ShaderUtility.createDefaultGeometryShader(graphAppearance, true);
		graphAppearance.setAttribute(CommonAttributes.LINE_SHADER + "." + CommonAttributes.TUBES_DRAW, false);

		graphAppearance.setAttribute(CommonAttributes.LINE_SHADER + "." + CommonAttributes.VERTEX_COLORS_ENABLED, true);
		graphAppearance.setAttribute(CommonAttributes.VERTEX_COLORS_ENABLED, true);
		dgs.setShowFaces(false);
		dgs.setShowLines(true);
		dgs.setShowPoints(false);
		graphAppearance.setAttribute(CommonAttributes.LIGHTING_ENABLED, false);
		return graph;
	}

	@Override
	public SceneGraphComponent buildXCoordLabeling(double xAxis, int xDim) {
		SceneGraphComponent sgc = SceneGraphUtility.createFullSceneGraphComponent("xLabels");
		int numLabels = determineNumLabels(xDim);
		double xStepSize = xAxis / numLabels;
		double[][] coords = new double[numLabels][3];
		String[] labels = new String[numLabels];
		for (int label = 0; label < numLabels; label++) {
			coords[label][0] = xStepSize * (label + 1);
			coords[label][1] = 0.0;
			coords[label][2] = 0.25 * -DataSetPlot3D.HANDNESS;
			int labelNr = (int) (xDim * ((float) (label + 1) / (float) numLabels));

			// if there is a window add the window start
			// position to get the right label

			if (useWindow)
				labelNr += dataWindowX;

			labels[label] = new Integer(labelNr).toString();
		}
		PointSetFactory factory = new PointSetFactory();
		factory.setVertexCount(numLabels);
		factory.setVertexCoordinates(coords);
		factory.setVertexLabels(labels);
		factory.update();
		Appearance ap = new Appearance();
		ap.setAttribute(CommonAttributes.VERTEX_DRAW, true);
		ap.setAttribute(CommonAttributes.EDGE_DRAW, false);
		ap.setAttribute(CommonAttributes.TUBES_DRAW, false);
		ap.setAttribute(CommonAttributes.FACE_DRAW, false);
		ap.setAttribute(CommonAttributes.TUBE_RADIUS, 2.0);
		ap.setAttribute(CommonAttributes.SPHERES_DRAW, false);
		ap.setAttribute(CommonAttributes.SPHERE_RESOLUTION, 2.0);
		ap.setAttribute(CommonAttributes.DIFFUSE_COLOR, java.awt.Color.RED);
		ap.setAttribute("pointShader.textShader.alignment", javax.swing.SwingConstants.CENTER);
		ap.setAttribute("pointShader.textShader.scale", 0.007);
		sgc.setAppearance(ap);
		sgc.setGeometry(factory.getPointSet());
		return sgc;
	}

	@Override
	public SceneGraphComponent buildYCoordLabeling(double yAxis, int yDim) {
		SceneGraphComponent sgc = SceneGraphUtility.createFullSceneGraphComponent("yLabels");
		int numLabels = yDim;
		double yStepSize = yAxis / numLabels;
		double[][] coords = new double[numLabels][3];
		String[] labels = new String[numLabels];
		for (int label = 0; label < numLabels; label++) {
			coords[label][0] = -0.25;
			coords[label][1] = 0.0;
			coords[label][2] = DataSetPlot3D.HANDNESS * yStepSize * label + DataSetPlot3D.HANDNESS * 0.5 * yStepSize;
			int labelNr = label + 1;
			labels[label] = new Integer(labelNr).toString();
		}
		PointSetFactory factory = new PointSetFactory();
		factory.setVertexCount(numLabels);
		factory.setVertexCoordinates(coords);
		factory.setVertexLabels(labels);
		factory.update();
		Appearance ap = new Appearance();
		ap.setAttribute(CommonAttributes.VERTEX_DRAW, true);
		ap.setAttribute(CommonAttributes.EDGE_DRAW, false);
		ap.setAttribute(CommonAttributes.TUBES_DRAW, false);
		ap.setAttribute(CommonAttributes.FACE_DRAW, false);
		ap.setAttribute(CommonAttributes.TUBE_RADIUS, 2.0);
		ap.setAttribute(CommonAttributes.SPHERES_DRAW, false);
		ap.setAttribute(CommonAttributes.SPHERE_RESOLUTION, 2.0);
		ap.setAttribute(CommonAttributes.DIFFUSE_COLOR, java.awt.Color.BLUE);
		ap.setAttribute("pointShader.textShader.alignment", javax.swing.SwingConstants.CENTER);
		ap.setAttribute("pointShader.textShader.scale", 0.007);
		sgc.setAppearance(ap);
		sgc.setGeometry(factory.getPointSet());
		return sgc;
	}

	@Override
	public SceneGraphComponent buildZCoordLabeling(double zMin, double zMax) {
		SceneGraphComponent sgc = SceneGraphUtility.createFullSceneGraphComponent("zLabels");

		double zSpan = Math.abs(((DataSetPlot3D.zScaler(zMin, currentScaling) < -MAXZ) ? -MAXZ : DataSetPlot3D.zScaler(
				zMin, currentScaling)))
				+ ((DataSetPlot3D.zScaler(zMax, currentScaling) > MAXZ) ? MAXZ : DataSetPlot3D.zScaler(zMax,
						currentScaling));
		double stepSize = zSpan / MAXLABELS;
		double maxStepSize = MAXZ / MAXLABELS;
		int numLabels = MAXLABELS;
		if (stepSize < MAXZ / MAXLABELS) {
			numLabels = (int) Math.ceil(numLabels * (stepSize / maxStepSize));
		}

		stepSize = zSpan / numLabels;
		double labelStep = Math.abs(DataSetPlot3D.zScaler(zMin, currentScaling))
				+ Math.abs(DataSetPlot3D.zScaler(zMax, currentScaling)) / numLabels;
		double[][] coords = new double[numLabels + 1][3];
		String[] labels = new String[numLabels + 1];
		for (int label = 0; label < numLabels + 1; label++) {
			double labelValue = DataSetPlot3D.zScaler(zMin, currentScaling) + labelStep * label;
			coords[label][0] = -0.125;
			coords[label][1] = zMin + stepSize * label;
			coords[label][2] = 0.0;
			labels[label] = new Float(DataSetPlot3D.inverseScaler(labelValue, currentScaling)).toString();
		}
		PointSetFactory factory = new PointSetFactory();
		factory.setVertexCount(numLabels + 1);
		factory.setVertexCoordinates(coords);
		factory.setVertexLabels(labels);
		factory.update();
		Appearance ap = new Appearance();
		ap.setAttribute(CommonAttributes.VERTEX_DRAW, true);
		ap.setAttribute(CommonAttributes.EDGE_DRAW, false);
		ap.setAttribute(CommonAttributes.TUBES_DRAW, false);
		ap.setAttribute(CommonAttributes.FACE_DRAW, false);
		ap.setAttribute(CommonAttributes.TUBE_RADIUS, 2.0);
		ap.setAttribute(CommonAttributes.SPHERES_DRAW, false);
		ap.setAttribute(CommonAttributes.SPHERE_RESOLUTION, 2.0);
		ap.setAttribute(CommonAttributes.DIFFUSE_COLOR, java.awt.Color.BLACK);
		ap.setAttribute("pointShader.textShader.alignment", javax.swing.SwingConstants.CENTER);
		ap.setAttribute("pointShader.textShader.scale", 0.007);
		sgc.setAppearance(ap);
		sgc.setGeometry(factory.getPointSet());
		return sgc;
	}

	@Override
	public void handleColourCast(ImageData colourTable, SceneGraphComponent graph, int dim, int dim2) {
		// Do nothing at the moment not sure if colour casts do make any sense???
	}

	@Override
	public SceneGraphComponent setDisplayMode(SceneGraphComponent rootNode, SceneGraphComponent graphNode,
			LinkedList<DoubleDataset> datasets, int newDisplayMode) {
		switch (newDisplayMode) {
		case 0: {
			DefaultGeometryShader dgs = ShaderUtility.createDefaultGeometryShader(graphAppearance, true);
			dgs.setShowFaces(false);
			dgs.setShowLines(true);
			dgs.setShowPoints(false);
			graphAppearance.setAttribute(CommonAttributes.LINE_SHADER + "." + CommonAttributes.TUBES_DRAW, false);

			graphAppearance.setAttribute(CommonAttributes.LINE_SHADER + "." + CommonAttributes.VERTEX_COLORS_ENABLED,
					true);
			graphAppearance.setAttribute(CommonAttributes.VERTEX_COLORS_ENABLED, true);
		}
			break;
		case 1: {
			DefaultGeometryShader dgs = ShaderUtility.createDefaultGeometryShader(graphAppearance, true);
			dgs.setShowFaces(false);
			dgs.setShowLines(false);
			dgs.setShowPoints(true);
			graphAppearance.setAttribute(CommonAttributes.POINT_SHADER + "." + CommonAttributes.SPHERES_DRAW, false);
			graphAppearance.setAttribute(CommonAttributes.POINT_SHADER + "." + CommonAttributes.POINT_SIZE, 2.0);

			graphAppearance.setAttribute(CommonAttributes.POINT_SHADER + "." + CommonAttributes.VERTEX_COLORS_ENABLED,
					true);
			graphAppearance.setAttribute(CommonAttributes.VERTEX_COLORS_ENABLED, true);
		}
			break;
		}
		return graphNode;
	}

	@Override
	public void setNewWindowPos(int windowX, int windowY) {
		dataWindowX = windowX;
		dataWindowY = windowY;
	}

	@Override
	public void setScaling(ScaleType newScaling) {
		currentScaling = newScaling;
	}

	@Override
	public void setDataDimensions(int maxXsize, int maxYsize) {
		maxDataXsize = maxXsize;
		maxDataYsize = maxYsize;
	}

	@Override
	public DisplayType getCurrentDisplay() {
		return DisplayType.LINE;
	}

}
