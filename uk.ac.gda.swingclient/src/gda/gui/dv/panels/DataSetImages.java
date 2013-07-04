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


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.BoxLayout;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;

import de.jreality.geometry.QuadMeshFactory;
import de.jreality.math.MatrixBuilder;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.tool.Tool;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.DefaultGeometryShader;
import de.jreality.shader.DefaultPolygonShader;
import de.jreality.shader.ShaderUtility;
import de.jreality.shader.Texture2D;
import de.jreality.ui.viewerapp.ViewerApp;

enum DisplayMode {
	SLIDESHOW, CARDSTACK, VOLUMESTACK, STRIP, HANDSTACK
}

/**
 * Display for a 2D stack of images
 */

public class DataSetImages extends JPanel implements ActionListener, ChangeListener, MouseListener {

	/**
	 * Maximum number of images that exit at one time
	 */
	public static final int MAXIMAGEBUFFER = 5;
	/**
	 * Gap between the images in the stack
	 */
	public static final double GAPINSTACK = 0.01;

	/**
	 * How long should a frame be displayed for SlideShow and FilmStrip
	 */
	public static final int FRAMETIME = 100;

	private final SceneGraphComponent root;
	private SceneGraphComponent[] slides = null;
	private ExecutorService execSvc = null;
	private ViewerApp viewerApp = null;
	private DoubleDataset data[];
	private int numSlides;
	private Appearance[] aps;
	private Texture2D[] textures;
	private final LinkedList<Integer> stackBuffer;
	private int stackPos[];
	private IPlayBack playback;
	private ImageDataLoader imgLoader;
	private IMainPlotVisualiser visualiser = null;
	private IMovement movement = null;
	private int bufferPos = 0;
	private final JSlider sldTimeline;
	private final JPopupMenu pmnDisplayOptions;
	private final JMenuItem mtmSlideShow;
	private final JMenuItem mtmCardStack;
	private final JMenuItem mtmVolumeStack;
	private final JMenuItem mtmFilmStrip;
	private final JMenuItem mtmHandStack;
	private DisplayMode displayMode = DisplayMode.SLIDESHOW;

	@SuppressWarnings("unused")
	DataSetImages(MainPlot overlayMaster) {
		root = new SceneGraphComponent("root");
		viewerApp = new ViewerApp(root);
		java.awt.Component comp = viewerApp.getContent();
		BoxLayout layout = new BoxLayout(this, BoxLayout.Y_AXIS);
		setLayout(layout);
		comp.setPreferredSize(new java.awt.Dimension(600, 750));
		this.add(comp);
		stackBuffer = new LinkedList<Integer>();
		sldTimeline = new JSlider();
		sldTimeline.setPreferredSize(new java.awt.Dimension(300, 50));
		sldTimeline.addChangeListener(this);
		this.add(sldTimeline);
		pmnDisplayOptions = new JPopupMenu();
		mtmSlideShow = new JMenuItem("Slideshow");
		mtmCardStack = new JMenuItem("Card stack");
		mtmVolumeStack = new JMenuItem("Volume stack");
		mtmFilmStrip = new JMenuItem("Strip");
		mtmHandStack = new JMenuItem("Hand stack");
		comp.addMouseListener(this);
		mtmSlideShow.addActionListener(this);
		mtmCardStack.addActionListener(this);
		mtmVolumeStack.addActionListener(this);
		mtmHandStack.addActionListener(this);
		mtmFilmStrip.addActionListener(this);

		pmnDisplayOptions.add(mtmSlideShow);
		pmnDisplayOptions.add(mtmCardStack);
		pmnDisplayOptions.add(mtmVolumeStack);
		pmnDisplayOptions.add(mtmFilmStrip);
		pmnDisplayOptions.add(mtmHandStack);
		execSvc = Executors.newFixedThreadPool(4);
		List<SceneGraphComponent> children = viewerApp.getSceneRoot().getChildComponents();

		// remove the automated added rotation tool

		for (SceneGraphComponent child : children) {
			List<Tool> tools = child.getTools();
			for (Tool t : tools) {
				if (t instanceof de.jreality.tools.RotateTool) {
					child.removeTool(t);
				}
			}
		}
	}

	/**
	 * Plot a series of data sets directly
	 * 
	 * @param data
	 */
	public void plotImages(DoubleDataset... data) {
		if (imgLoader != null) {
			imgLoader.stop();
		}
		if (slides != null) {
			for (SceneGraphComponent slide : slides) {
				root.removeChild(slide);
			}
		}
		slides = null;
		aps = null;
		textures = null;
		stackPos = null;
		System.gc();

		this.data = data;
		numSlides = Math.min(data.length, MAXIMAGEBUFFER);
		slides = new SceneGraphComponent[numSlides];
		aps = new Appearance[numSlides];
		textures = new Texture2D[numSlides];
		stackPos = new int[numSlides];
		stackBuffer.clear();
		for (int i = 0; i < numSlides; i++) {
			String nodeName = "Polygon " + i;
			slides[i] = new SceneGraphComponent(nodeName);
			slides[i].setGeometry(createQuad());
			aps[i] = new Appearance();
			aps[i].setAttribute(CommonAttributes.LIGHTING_ENABLED, false);
			aps[i].setAttribute(CommonAttributes.TRANSPARENCY_ENABLED, true);
			aps[i].setAttribute(CommonAttributes.ADDITIVE_BLENDING_ENABLED, false);
			aps[i].setAttribute(CommonAttributes.POLYGON_SHADER + "." + CommonAttributes.TRANSPARENCY, 0.0);
			slides[i].setAppearance(aps[i]);
			DefaultGeometryShader dgs = ShaderUtility.createDefaultGeometryShader(aps[i], true);
			dgs.setShowLines(false);
			dgs.setShowPoints(false);
			DefaultPolygonShader dps = (DefaultPolygonShader) dgs.createPolygonShader("default");
			dps.setDiffuseColor(java.awt.Color.white);
			stackBuffer.add(i);
			stackPos[i] = i;
			MatrixBuilder.euclidean().translate(0, 0, -stackPos[i] * GAPINSTACK).assignTo(slides[i]);
		}

		for (int i = numSlides - 1; i >= 0; i--) {
			root.addChild(slides[i]);
		}

		sldTimeline.setMaximum(Math.max(1, (data.length - 1) * FRAMETIME - 1));
		sldTimeline.setValue(0);
		sldTimeline.setMinimum(0);
		playback = new PlayBackSlideshow(aps, (data.length - 1) * FRAMETIME - 1);
		playback.addChangeListener(this);
		imgLoader = new ImageDataLoader();
		imgLoader.addChangeListener(this);
		execSvc.execute(imgLoader);
		for (int i = 0; i < numSlides; i++) {
			imgLoader.addJob(new DataSetJob(aps[i], textures[i], this.data[i], visualiser));
		}
		movement = new SlideShowMovement(this, stackBuffer, stackPos, aps, slides, playback, data.length);
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

	private IndexedFaceSet createQuad() {
		// generate the coordinates for the surface as a 2D array of 3-vectors
		// QuadMeshFactory is the only factory which accepts such a data structure
		// as the argument of its setVertexCoordinates() method!
		double[][][] coords = new double[2][2][3];
		coords[0][0][0] = -4.0;
		coords[0][0][1] = 4.0;
		coords[0][0][2] = 0.0;
		coords[0][1][0] = 4.0;
		coords[0][1][1] = 4.0;
		coords[0][1][2] = 0.0;
		coords[1][0][0] = -4.0;
		coords[1][0][1] = -4.0;
		coords[1][0][2] = 0.0;
		coords[1][1][0] = 4.0;
		coords[1][1][1] = -4.0;
		coords[1][1][2] = 0.0;

		// QuadMeshFactory knows how to build an IndexedFaceSet from a rectangular array
		// of vectors.
		QuadMeshFactory graphFactory = new QuadMeshFactory();

		graphFactory.setVLineCount(2); // important: the v-direction is the left-most index
		graphFactory.setULineCount(2); // and the u-direction the next-left-most index
		graphFactory.setClosedInUDirection(false);
		graphFactory.setClosedInVDirection(false);
		graphFactory.setVertexCoordinates(coords);
		graphFactory.setGenerateFaceNormals(true);
		graphFactory.setGenerateTextureCoordinates(true);
		graphFactory.update();
		coords = null;
		return graphFactory.getIndexedFaceSet();
	}

	/**
	 * Update the entire image stack of images
	 * 
	 * @param frameNr
	 *            current FrameNr
	 * @param dataSetNr
	 *            dataSetNr that needs to be loaded
	 * @param forward
	 *            is this a forward or backward move in the stack
	 */
	public void updateStack(int frameNr, int dataSetNr, boolean forward) {
		for (int i = 0; i < numSlides; i++) {
			root.removeChild(slides[i]);
		}

		imgLoader.addJob(new DataSetJob(aps[frameNr], textures[frameNr], data[dataSetNr], visualiser));
		int slideNr = 0;

		if (forward) {
			slideNr = frameNr;
		} else {
			slideNr = frameNr - 1;
			if (slideNr < 0) {
				slideNr += numSlides;
			}
		}

		for (int i = 0; i < numSlides; i++) {
			root.addChild(slides[slideNr]);
			slideNr--;
			if (slideNr < 0) {
				slideNr += numSlides;
			}
		}
	}

	private void moveForward() {
		bufferPos = movement.moveForward(bufferPos);
	}

	private void moveBackward() {
		bufferPos = movement.moveBackward(bufferPos);
	}

	@Override
	public synchronized void stateChanged(ChangeEvent evt) {
		if (evt.getSource().equals(sldTimeline) && playback != null) {
			playback.setFrameNr(sldTimeline.getValue(), true);
		} else if (evt.getSource().equals(playback)) {
			switch (playback.getDirection()) {
			case FORWARD:
				moveForward();
				break;
			case BACKWARD:
				moveBackward();
				break;
			default:
				// do nothing
				break;
			}
		} else if (evt.getSource().equals(imgLoader)) {
			if (viewerApp.getCurrentViewer().canRenderAsync()) {
				viewerApp.getCurrentViewer().renderAsync();
			}
		}

	}

	private void checkOnPopup(MouseEvent evt) {
		if (evt.isPopupTrigger()) {
			pmnDisplayOptions.show(evt.getComponent(), evt.getX(), evt.getY());
		}
	}

	private void performSwitchToStack(int frameNr) {
		sldTimeline.setMaximum(Math.max(1, data.length - 1));
		sldTimeline.setValue(frameNr);
		playback = null;
		System.gc();
		playback = new PlayBackStack(data.length - 1);
		playback.addChangeListener(this);
		if (displayMode == DisplayMode.SLIDESHOW) {
			if (frameNr >= 2) {
				moveForward();
				moveForward();
			} else if (frameNr >= 1) {
				moveForward();
			}
		}
		playback.setFrameNr(frameNr, false);
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		if (evt.getSource().equals(mtmCardStack)) {
			playback.stop();
			playback.cleanup();

			int frameNr = playback.getFrameNr();

			if (displayMode == DisplayMode.SLIDESHOW || displayMode == DisplayMode.STRIP) {
				frameNr = frameNr / FRAMETIME;
			}

			for (int i = 0; i < numSlides; i++) {
				MatrixBuilder.euclidean().rotateY(-0.25 * Math.PI).translate(-5.0, 0, -3.0 - stackPos[i] * 1.5)
				.assignTo(slides[i]);
				aps[i].setAttribute(CommonAttributes.POLYGON_SHADER + "." + CommonAttributes.TRANSPARENCY, 0.5);
			}
			movement = null;
			movement = new StackMovement(this, stackBuffer, stackPos, slides, data.length);
			performSwitchToStack(frameNr);
			displayMode = DisplayMode.CARDSTACK;
		} else if (evt.getSource().equals(mtmSlideShow)) {
			playback.stop();
			playback.cleanup();
			int frameNr = playback.getFrameNr();

			if (displayMode != DisplayMode.SLIDESHOW && displayMode != DisplayMode.STRIP) {
				frameNr *= FRAMETIME;
			}

			for (int i = 0; i < numSlides; i++) {
				MatrixBuilder.euclidean().translate(0, 0, -stackPos[i] * GAPINSTACK).assignTo(slides[i]);
				aps[i].setAttribute(CommonAttributes.POLYGON_SHADER + "." + CommonAttributes.TRANSPARENCY, 0.0);
			}
			sldTimeline.setMaximum(Math.max(1, (data.length - 1) * FRAMETIME - 1));
			sldTimeline.setValue(frameNr);
			playback = null;
			System.gc();
			playback = new PlayBackSlideshow(aps, (data.length - 1) * FRAMETIME - 1);
			movement = new SlideShowMovement(this, stackBuffer, stackPos, aps, slides, playback, data.length);
			playback.addChangeListener(this);
			playback.setFrameNr(frameNr, false);
			displayMode = DisplayMode.SLIDESHOW;
		} else if (evt.getSource().equals(mtmVolumeStack)) {
			playback.stop();
			playback.cleanup();

			int frameNr = playback.getFrameNr();

			if (displayMode == DisplayMode.SLIDESHOW || displayMode == DisplayMode.STRIP) {
				frameNr = frameNr / FRAMETIME;
			}

			for (int i = 0; i < numSlides; i++) {
				MatrixBuilder.euclidean().translate(0, 0, -stackPos[i] * 2 * GAPINSTACK).assignTo(slides[i]);
				aps[i].setAttribute(CommonAttributes.POLYGON_SHADER + "." + CommonAttributes.TRANSPARENCY,
						1.0 - 1.0 / numSlides);
			}
			movement = null;
			movement = new VolumeStackMovement(this, stackBuffer, stackPos, slides, data.length);
			performSwitchToStack(frameNr);
			displayMode = DisplayMode.VOLUMESTACK;
		} else if (evt.getSource().equals(mtmHandStack)) {
			playback.stop();
			playback.cleanup();

			int frameNr = playback.getFrameNr();
			if (displayMode == DisplayMode.SLIDESHOW || displayMode == DisplayMode.STRIP) {
				frameNr = frameNr / FRAMETIME;
			}

			for (int i = 0; i < numSlides; i++) {
				MatrixBuilder.euclidean().translate(stackPos[i] * 0.25, -5, -5.0 - stackPos[i] * 2 * GAPINSTACK)
				.rotateZ(numSlides * 0.0375 * Math.PI - stackPos[i] * 0.075 * Math.PI).translate(0, 5, 0)
				.assignTo(slides[i]);
				aps[i].setAttribute(CommonAttributes.POLYGON_SHADER + "." + CommonAttributes.TRANSPARENCY, 0.5);
			}
			movement = null;
			movement = new HandStackMovement(this, stackBuffer, stackPos, slides, data.length);
			performSwitchToStack(frameNr);
			displayMode = DisplayMode.HANDSTACK;

		} else if (evt.getSource().equals(mtmFilmStrip)) {
			playback.stop();
			playback.cleanup();
			int frameNr = playback.getFrameNr();
			if (displayMode != DisplayMode.SLIDESHOW) {
				frameNr *= FRAMETIME;
			}

			for (int i = 0; i < numSlides; i++) {
				MatrixBuilder.euclidean().translate(stackPos[i] * 8.0, 0.0, 0.0).assignTo(slides[i]);
				aps[i].setAttribute(CommonAttributes.POLYGON_SHADER + "." + CommonAttributes.TRANSPARENCY, 0.0);
			}
			sldTimeline.setMaximum(Math.max(1, (data.length - 1) * FRAMETIME));
			sldTimeline.setValue(frameNr);
			playback = null;
			playback = new PlayBackFilm(slides, 8.0, stackPos, (data.length - 1) * FRAMETIME);
			movement = new FilmStripMovement(this, stackBuffer, stackPos, slides, data.length);
			playback.addChangeListener(this);
			System.gc();
			if (displayMode == DisplayMode.SLIDESHOW) {
				if (frameNr >= 2 * FRAMETIME) {
					moveForward();
					moveForward();
				} else if (frameNr >= FRAMETIME) {
					moveForward();
				}
			}
			playback.setFrameNr(frameNr, false);
			displayMode = DisplayMode.STRIP;
		}

	}

	/**
	 * This function applies the colour cast which is specified by the IMainPlotVisualiser which can be accessed by set
	 * and get Visualiser
	 */
	public void applyColorCast() {

		// this should now all be replaced with the new functionality
		if (data != null && data.length > 0) {
			for (int i = 0; i < numSlides; i++) {
				imgLoader.addJob(new DataSetJob(aps[i], textures[i], data[(i + bufferPos) % data.length],
						visualiser));
			}
		}
		System.gc();
	}

	/**
	 * CleanUp the data when it is inactive, that hopefully will reduce the memory footprint
	 */

	public void cleanUp() {
		if (slides != null) {
			for (int i = 0; i < slides.length; i++) {
				root.removeChild(slides[i]);
				slides[i] = null;
				textures[i] = null;
			}
		}
		if (data != null) {
			data = null;
		}

		slides = null;
		textures = null;
		System.gc();
	}

	@Override
	public void mouseClicked(MouseEvent evt) {
		checkOnPopup(evt);

	}

	@Override
	public void mouseEntered(MouseEvent evt) {
		// Nothing to do

	}

	@Override
	public void mouseExited(MouseEvent evt) {
		// Nothing to do

	}

	@Override
	public void mousePressed(MouseEvent evt) {
		checkOnPopup(evt);

	}

	@Override
	public void mouseReleased(MouseEvent evt) {
		checkOnPopup(evt);
	}

	/**
	 * Disposes this panel.
	 */
	public void dispose() {
		cleanUp();

	}
}
