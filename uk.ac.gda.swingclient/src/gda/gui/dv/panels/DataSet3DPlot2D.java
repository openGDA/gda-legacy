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

import org.eclipse.dawnsci.analysis.dataset.impl.DoubleDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jreality.geometry.IndexedLineSetFactory;
import de.jreality.geometry.PointSetFactory;
import de.jreality.geometry.QuadMeshFactory;
import de.jreality.scene.Appearance;
import de.jreality.scene.Geometry;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.DefaultGeometryShader;
import de.jreality.shader.DefaultPolygonShader;
import de.jreality.shader.GlslPolygonShader;
import de.jreality.shader.ShaderUtility;
import de.jreality.util.SceneGraphUtility;

/**
 * This class is responsible to surface plot a single two dimensional DataSet
 */
public class DataSet3DPlot2D implements IDataSet3DCorePlot {

	private ScaleType currentScaling = ScaleType.LINEAR;
	private DisplayType currentDisplay = DisplayType.GRID;
	private boolean useWindow;
	private boolean subSample;
	private boolean useJOGL;
	private int dataWindowY;
	private int dataWindowX;
	private int renderingMode = 0;
	private QuadMeshFactory quadFactory = null;
	private Appearance graphAppearance = null;
	private ImageData colourTable = null;
	private static int MAXSOFTDIMENSION = 256;
	private static int MAXSOFTDIMENSIONSQR = MAXSOFTDIMENSION * MAXSOFTDIMENSION;
	private static final Logger logger = LoggerFactory.getLogger(DataSet3DPlot2D.class);

	/**
	 * Constructor for 3D Plotting a 2D dataset
	 * 
	 * @param useWindow
	 *            should a window on the data to be used if dataset is too big
	 * @param haveJOGL
	 *            is JOGL (hardware) available
	 * @param windowX
	 *            window x position
	 * @param windowY
	 *            window y position
	 */

	public DataSet3DPlot2D(boolean useWindow, boolean haveJOGL, int windowX, int windowY) {
		this.useWindow = useWindow;
		this.subSample = !useWindow;
		this.dataWindowX = windowX;
		this.dataWindowY = windowY;
		this.useJOGL = haveJOGL;
	}

	private float[] determineSamplingRate(int xSize, int ySize) {
		float[] returnValues = new float[3];
		int displayYSize = ySize;
		int displayXSize = xSize;
		float samplingRate = 1.0f;

		if (xSize * ySize > MAXSOFTDIMENSIONSQR && subSample) {
			if (xSize > ySize && xSize > MAXSOFTDIMENSION)
				samplingRate = (float) xSize / (float) MAXSOFTDIMENSION;
			else
				samplingRate = (float) ySize / (float) MAXSOFTDIMENSION;

			displayXSize = (int) (xSize / samplingRate);
			displayYSize = (int) (ySize / samplingRate);
		}
		returnValues[0] = samplingRate;
		returnValues[1] = displayXSize;
		returnValues[2] = displayYSize;
		return returnValues;
	}

	private int determineNumLabels(int dim) {
		int numLabels = dim;
		if (numLabels > MAXLABELS)
			numLabels = dim / (int) Math.ceil(dim / MAXLABELS);
		return numLabels;
	}

	private int determineNumTicks(int dim) {
		int numTicks = dim;
		if (numTicks > MAXTICKS)
			numTicks = dim / (int) Math.ceil(dim / MAXTICKS);
		return numTicks;
	}

	private double[] computeScalingAndCoords(DoubleDataset currentData) {
		double[] returnValues = new double[6];
		double globalZmax = DataSetPlot3D.zScaler(currentData.max().doubleValue(), currentScaling);
		int globalXdim = currentData.getShape()[1];
		int globalYdim = currentData.getShape()[0];

		if (useWindow) {
			globalXdim = Math.min(globalXdim, MAXSOFTDIMENSION);
			globalYdim = Math.min(globalYdim, MAXSOFTDIMENSION);
		}

		int longestAxis = Math.max(globalXdim, globalYdim);
		double zCoord = globalZmax;
		double zScale = 1.0;

		if (zCoord > MAXZ) {
			zScale = MAXZ / zCoord;
			zCoord = MAXZ;
		}

		double xCoord = MAXX / ((double) (longestAxis) / (double) (globalXdim));
		double yCoord = MAXY / ((double) (longestAxis) / (double) (globalYdim));
		double xScale = xCoord / Math.max((globalXdim - 1), 1.0);
		double yScale = yCoord / Math.max((globalYdim - 1), 1.0);
		returnValues[0] = xCoord;
		returnValues[1] = yCoord;
		returnValues[2] = zCoord;
		returnValues[3] = xScale;
		returnValues[4] = yScale;
		returnValues[5] = zScale;
		return returnValues;
	}

	private SceneGraphComponent buildGraphWithNewDisplay(DisplayType oldOne, DisplayType newOne,
			SceneGraphComponent graph, LinkedList<DoubleDataset> datasets) {
		DoubleDataset currentData = datasets.get(0);
		if (oldOne != newOne) {
			System.gc();

			if (currentDisplay == DisplayType.LINE)
				currentDisplay = DisplayType.GRID;
			else
				currentDisplay = DisplayType.LINE;

			double[] values = computeScalingAndCoords(currentData);
			double xScale = values[3];
			double yScale = values[4];
			double zScale = values[5];
			graph = buildGraph(datasets, graph, xScale, yScale, zScale, currentDisplay);
		}
		return graph;
	}

	@Override
	public SceneGraphComponent buildCoordAxesTicks(double xAxis, double yAxis, double zAxis, double zMin, double zMax,
			int xDim, int yDim) {
		SceneGraphComponent sgc = SceneGraphUtility.createFullSceneGraphComponent("AxisTicks");

		int numXticks = determineNumTicks(xDim);
		int numYticks = determineNumTicks(yDim);

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
			coords[i * 2 + numXticks * 2][2] = DataSetPlot3D.HANDNESS * yStepSize * i;
			coords[i * 2 + 1 + numXticks * 2][0] = 0.0;
			coords[i * 2 + 1 + numXticks * 2][1] = 0.0;
			coords[i * 2 + 1 + numXticks * 2][2] = DataSetPlot3D.HANDNESS * yStepSize * i;
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
	public SceneGraphComponent buildCoordGrid(double xAxis, double yAxis, int xDim, int yDim) {
		SceneGraphComponent sgc = SceneGraphUtility.createFullSceneGraphComponent("AxisGrid");
		int numXticks = determineNumTicks(xDim);
		int numYticks = determineNumTicks(yDim);
		double xStepSize = xAxis / numXticks;
		double yStepSize = yAxis / numYticks;
		double[][][] coords = new double[numYticks + 1][numXticks + 1][3];
		for (int y = 0; y <= numYticks; y++)
			for (int x = 0; x <= numXticks; x++) {
				coords[y][x][0] = xStepSize * x;
				coords[y][x][1] = 0.0f;
				coords[y][x][2] = DataSetPlot3D.HANDNESS * yStepSize * y;
			}
		QuadMeshFactory factory = new QuadMeshFactory();
		factory.setVLineCount(numYticks + 1); // important: the v-direction is the left-most index
		factory.setULineCount(numXticks + 1); // and the u-direction the next-left-most index
		factory.setClosedInUDirection(false);
		factory.setClosedInVDirection(false);
		factory.setVertexCoordinates(coords);
		factory.setGenerateEdgesFromFaces(true);
		factory.setEdgeFromQuadMesh(true); // generate "long" edges: one for each u-, v- parameter curve
		factory.update();
		Appearance ap = new Appearance();
		ap.setAttribute(CommonAttributes.VERTEX_DRAW, false);
		ap.setAttribute(CommonAttributes.EDGE_DRAW, true);
		ap.setAttribute(CommonAttributes.TUBES_DRAW, false);
		ap.setAttribute(CommonAttributes.FACE_DRAW, false);
		ap.setAttribute(CommonAttributes.TUBE_RADIUS, 2.0);
		ap.setAttribute(CommonAttributes.DIFFUSE_COLOR, java.awt.Color.BLACK);
		sgc.setGeometry(factory.getIndexedFaceSet());
		sgc.setAppearance(ap);
		return sgc;
	}

	private Geometry buildPolygonGraph(final double[][] coords, final double[][] colours, int xSize, int ySize) {
		if (quadFactory == null) {
			quadFactory = new QuadMeshFactory();
		}
		quadFactory.setVLineCount(ySize); // important: the v-direction is the left-most index
		quadFactory.setULineCount(xSize); // and the u-direction the next-left-most index
		quadFactory.setClosedInUDirection(false);
		quadFactory.setClosedInVDirection(false);
		quadFactory.setVertexCoordinates(coords);
		quadFactory.setVertexColors(colours);
		quadFactory.setGenerateFaceNormals(true);
		quadFactory.setGenerateTextureCoordinates(false);
		quadFactory.setGenerateEdgesFromFaces(true);
		quadFactory.setEdgeFromQuadMesh(true); // generate "long" edges: one for each u-, v- parameter curve
		quadFactory.update();
		return quadFactory.getIndexedFaceSet();
	}

	private Geometry buildLineGraph(final double[][] coords, final double[][] colours, int xSize, int ySize) {
		IndexedLineSetFactory factory = new IndexedLineSetFactory();
		factory.setVertexCount(xSize * ySize);
		factory.setEdgeCount((xSize - 1) * (ySize - 1));
		factory.setVertexCoordinates(coords);
		factory.setVertexColors(colours);
		int edgeFaces[][] = new int[(xSize - 1) * (ySize - 1)][2];
		for (int y = 0; y < ySize - 1; y++)
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
		if (datasets.size() > 0) {
			DoubleDataset newData = datasets.get(0);
			int xSize = newData.getShape()[1];
			int ySize = newData.getShape()[0];
			float samplingRate = 1.0f;
			int displayXSize = xSize;
			int displayYSize = ySize;
			if (subSample) {
				float[] sampling = determineSamplingRate(xSize, ySize);
				displayXSize = (int) sampling[1];
				displayYSize = (int) sampling[2];
				samplingRate = sampling[0];
				if (samplingRate != 1.0f)
					logger.warn("Sampling rate " + samplingRate);
			} else {
				displayXSize = Math.min(displayXSize, MAXSOFTDIMENSION);
				displayYSize = Math.min(displayYSize, MAXSOFTDIMENSION);
			}
			double zMax = DataSetPlot3D.zScaler(newData.max().doubleValue(), currentScaling);
			double zMin = DataSetPlot3D.zScaler(newData.min().doubleValue(), currentScaling);
			double[][] coords = new double[displayYSize * displayXSize][3];
			double[][] colours = new double[displayYSize * displayXSize][3];
			for (int y = 0; y < displayYSize; y++)
				for (int x = 0; x < displayXSize; x++) {
					int dataXPos = x;
					int dataYPos = y;
					if (useWindow) {
						dataXPos += dataWindowX;
						dataYPos += dataWindowY;
					}
					double dataEntry = DataSetPlot3D.zScaler(newData.get((int) (dataXPos * samplingRate)
							+ (int) (dataYPos * samplingRate) * xSize), currentScaling);
					coords[x + y * displayXSize][0] = xScale * (x * samplingRate);
					coords[x + y * displayXSize][1] = dataEntry * zScale;
					coords[x + y * displayXSize][2] = DataSetPlot3D.HANDNESS * yScale * (y * samplingRate);
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
			if (display == DisplayType.GRID)
				graph.setGeometry(buildPolygonGraph(coords, colours, displayXSize, displayYSize));
			else
				graph.setGeometry(buildLineGraph(coords, colours, displayXSize, displayYSize));

			graphAppearance = graph.getAppearance();
			DefaultGeometryShader dgs = ShaderUtility.createDefaultGeometryShader(graphAppearance, true);

			if (useJOGL && (renderingMode == 0 || renderingMode == 4)) {

				// I do not need to do anything with this variable
				// I just need this to activate this specific shader
				// and underlying functionality using VertexArrays in
				// jReality

				@SuppressWarnings("unused")
				GlslPolygonShader tsps = (GlslPolygonShader) dgs.createPolygonShader("glsl");
			}

			switch (renderingMode) {
			case 0:
				dgs.setShowFaces(true);
				dgs.setShowLines(false);
				dgs.setShowPoints(false);
				break;
			case 1:
			case 2:
				graphAppearance.setAttribute(CommonAttributes.LINE_SHADER + "." + CommonAttributes.TUBES_DRAW, false);

				graphAppearance.setAttribute(CommonAttributes.LINE_SHADER + "."
						+ CommonAttributes.VERTEX_COLORS_ENABLED, true);
				graphAppearance.setAttribute(CommonAttributes.VERTEX_COLORS_ENABLED, true);
				dgs.setShowFaces(false);
				dgs.setShowLines(true);
				dgs.setShowPoints(false);
				break;
			case 3:
				dgs.setShowFaces(false);
				dgs.setShowLines(false);
				dgs.setShowPoints(true);
				graphAppearance
						.setAttribute(CommonAttributes.POINT_SHADER + "." + CommonAttributes.SPHERES_DRAW, false);
				graphAppearance.setAttribute(CommonAttributes.POINT_SHADER + "." + CommonAttributes.POINT_SIZE, 2.0);

				graphAppearance.setAttribute(CommonAttributes.POINT_SHADER + "."
						+ CommonAttributes.VERTEX_COLORS_ENABLED, true);
				graphAppearance.setAttribute(CommonAttributes.VERTEX_COLORS_ENABLED, true);
				break;
			case 4:
				dgs.setShowFaces(true);
				dgs.setShowLines(true);
				dgs.setShowPoints(false);
				graphAppearance.setAttribute(CommonAttributes.LINE_SHADER + "." + CommonAttributes.TUBES_DRAW, false);
				graphAppearance.setAttribute(CommonAttributes.LINE_SHADER + "." + CommonAttributes.DIFFUSE_COLOR,
						java.awt.Color.BLACK);
				break;
			}
			graphAppearance.setAttribute(CommonAttributes.LIGHTING_ENABLED, false);
		}
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
		int numLabels = determineNumLabels(yDim);
		double yStepSize = yAxis / numLabels;
		double[][] coords = new double[numLabels][3];
		String[] labels = new String[numLabels];
		for (int label = 0; label < numLabels; label++) {
			coords[label][0] = -0.25;
			coords[label][1] = 0.0;
			coords[label][2] = DataSetPlot3D.HANDNESS * yStepSize * (label + 1);
			int labelNr = (int) (yDim * ((float) (label + 1) / (float) numLabels));
			// if there is a window add the window start
			// position to get the right label

			if (useWindow)
				labelNr += dataWindowY;

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
		// double labelStep =(Math.abs(DataSetPlot3D.zScaler(zMin,currentScaling)+
		// DataSetPlot3D.zScaler(zMax,currentScaling)))/numLabels;
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
	public SceneGraphComponent setDisplayMode(SceneGraphComponent rootNode, SceneGraphComponent graphNode,
			final LinkedList<DoubleDataset> datasets, int newDisplayMode) {
		if (datasets.size() > 0) {
			renderingMode = newDisplayMode;
			switch (newDisplayMode) {
			case 0: {
				graphNode = buildGraphWithNewDisplay(currentDisplay, DisplayType.GRID, graphNode, datasets);
				DefaultGeometryShader dgs = ShaderUtility.createDefaultGeometryShader(graphAppearance, true);

				if (useJOGL) {
					@SuppressWarnings("unused")
					GlslPolygonShader tsps = (GlslPolygonShader) dgs.createPolygonShader("glsl");
				} else {
					DefaultPolygonShader dps = (DefaultPolygonShader) dgs.createPolygonShader("default");
					dps.setSpecularCoefficient(0.0);
					dps.setSmoothShading(true);
				}
				dgs.setShowFaces(true);
				dgs.setShowLines(false);
				dgs.setShowPoints(false);
			}
				break;
			case 1: {
				graphNode = buildGraphWithNewDisplay(currentDisplay, DisplayType.GRID, graphNode, datasets);
				DefaultGeometryShader dgs = ShaderUtility.createDefaultGeometryShader(graphAppearance, true);
				dgs.setShowFaces(false);
				dgs.setShowLines(true);
				dgs.setShowPoints(false);
				graphAppearance.setAttribute(CommonAttributes.LINE_SHADER + "." + CommonAttributes.TUBES_DRAW, false);

				graphAppearance.setAttribute(CommonAttributes.LINE_SHADER + "."
						+ CommonAttributes.VERTEX_COLORS_ENABLED, true);
				graphAppearance.setAttribute(CommonAttributes.VERTEX_COLORS_ENABLED, true);

			}
				break;
			case 2: {
				graphNode = buildGraphWithNewDisplay(currentDisplay, DisplayType.LINE, graphNode, datasets);

			}
				break;
			case 3: {
				graphNode = buildGraphWithNewDisplay(currentDisplay, DisplayType.GRID, graphNode, datasets);
				DefaultGeometryShader dgs = ShaderUtility.createDefaultGeometryShader(graphAppearance, true);
				dgs.setShowFaces(false);
				dgs.setShowLines(false);
				dgs.setShowPoints(true);
				graphAppearance
						.setAttribute(CommonAttributes.POINT_SHADER + "." + CommonAttributes.SPHERES_DRAW, false);
				graphAppearance.setAttribute(CommonAttributes.POINT_SHADER + "." + CommonAttributes.POINT_SIZE, 2.0);

				graphAppearance.setAttribute(CommonAttributes.POINT_SHADER + "."
						+ CommonAttributes.VERTEX_COLORS_ENABLED, true);
				graphAppearance.setAttribute(CommonAttributes.VERTEX_COLORS_ENABLED, true);
			}
				break;
			case 4: {
				graphNode = buildGraphWithNewDisplay(currentDisplay, DisplayType.GRID, graphNode, datasets);
				DefaultGeometryShader dgs = ShaderUtility.createDefaultGeometryShader(graphAppearance, true);
				dgs.setShowFaces(true);
				dgs.setShowLines(true);
				dgs.setShowPoints(false);
				if (useJOGL) {
					@SuppressWarnings("unused")
					GlslPolygonShader tsps = (GlslPolygonShader) dgs.createPolygonShader("glsl");
				} else {
					DefaultPolygonShader dps = (DefaultPolygonShader) dgs.createPolygonShader("default");
					dps.setSpecularCoefficient(0.0);
					dps.setSmoothShading(true);
				}
				graphAppearance.setAttribute(CommonAttributes.LINE_SHADER + "." + CommonAttributes.TUBES_DRAW, false);
				graphAppearance.setAttribute(CommonAttributes.LINE_SHADER + "." + CommonAttributes.DIFFUSE_COLOR,
						java.awt.Color.BLACK);

			}
				break;
			}
		}
		return graphNode;
	}

	@Override
	public void setScaling(ScaleType newScaling) {
		currentScaling = newScaling;
	}

	@Override
	public void handleColourCast(ImageData colourTable, SceneGraphComponent graph, int xDim, int yDim) {

		this.colourTable = colourTable;
		if (graph.getGeometry() instanceof de.jreality.scene.IndexedFaceSet) {
			IndexedFaceSet geom = (IndexedFaceSet) graph.getGeometry();
			float[] sampling = determineSamplingRate(xDim, yDim);
			int displayXsize = (int) sampling[1];
			int displayYsize = (int) sampling[2];
			float samplingRate = sampling[0];
			double[][] colours = new double[displayYsize * displayXsize][3];
			for (int y = 0; y < displayYsize; y++)
				for (int x = 0; x < displayXsize; x++) {
					int dataXPos = x;
					int dataYPos = y;
					if (useWindow) {
						dataXPos += dataWindowX;
						dataYPos += dataWindowY;
					}
					int packedRGBcolour = colourTable.get((int) (dataXPos * samplingRate),
							(int) (dataYPos * samplingRate));
					int red = (packedRGBcolour >> 16) & 0xff;
					int green = (packedRGBcolour >> 8) & 0xff;
					int blue = (packedRGBcolour) & 0xff;
					colours[x + y * displayXsize][0] = red / 255.0;
					colours[x + y * displayXsize][1] = green / 255.0;
					colours[x + y * displayXsize][2] = blue / 255.0;
				}
			geom.setVertexAttributes(de.jreality.scene.data.Attribute.COLORS,
					new de.jreality.scene.data.DoubleArrayArray.Array(colours));
		}
	}

	@Override
	public void setNewWindowPos(int windowX, int windowY) {
		dataWindowX = windowX;
		dataWindowY = windowY;
	}

	@Override
	public void setDataDimensions(int maxXsize, int maxYsize) {
		MAXSOFTDIMENSION = maxXsize;
		MAXSOFTDIMENSIONSQR = maxXsize * maxYsize;
	}

	@Override
	public DisplayType getCurrentDisplay() {
		return currentDisplay;
	}

}
