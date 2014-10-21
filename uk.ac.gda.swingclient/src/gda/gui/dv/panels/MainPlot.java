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

package gda.gui.dv.panels;

import gda.analysis.ScanFileHolder;
import gda.analysis.io.MACLoader;
import gda.analysis.plotmanager.IPlotWindow;
import gda.analysis.plotmanager.PlotManager;
import gda.analysis.plotmanager.PlotPackage;
import gda.analysis.plotmanager.PlotPasser;
import gda.device.DeviceException;
import gda.gui.dv.panels.vispanels.GreenColourCast;
import gda.jython.JythonServerFacade;
import gda.observable.IObservable;
import gda.observable.IObserver;
import gda.util.exceptionUtils;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.JPanel;

import org.eclipse.dawnsci.analysis.api.io.ScanFileHolderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The idea of this class is to be a nice wrapper for all the plotting methods from the server. It therefore needs to
 * observe the plotmanager.
 */
public class MainPlot extends JPanel implements IObserver, IObservable, IPlotWindow {

	/**
	 * Setup the logging facilities
	 */
	private static final Logger logger = LoggerFactory.getLogger(MainPlot.class);

	// Sections of the JPanel

	DataSetPlot dataSetPlot;
	DataSetPlot3D dataSetPlot3D;
	DataSetImages dataSetImages;

	DataSetImage dataSetImage;

	private GridBagConstraints gbc = null;

	// Vector for containing all the tagging functionality.
	HashMap<Integer, Integer> tagMap = null;

	private String xAxisLabel = "";

	// private AcquisitionPanel owner = null;

	private Vector<IObserver> observers = null;

	String activeDisplayType = "None";

	/**
	 * Constructor
	 * 
	 * @param panelName
	 *            The name of the panel which is to be used by the PlotManager.
	 */
	public MainPlot(String panelName) {

		setPreferredSize(new Dimension(1000, 600));

		gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;

		setLayout(new GridLayout());

		dataSetPlot = new DataSetPlot("X Axis");
		dataSetImage = new DataSetImage(this);
		dataSetPlot3D = new DataSetPlot3D(this);
		dataSetImages = new DataSetImages(this);
		dataSetImage.setVisualiser(new GreenColourCast());
		dataSetPlot3D.setVisualiser(new GreenColourCast());
		dataSetImages.setVisualiser(new GreenColourCast());

		tagMap = new HashMap<Integer, Integer>();

		setName(panelName);

		PlotManager.getIPlotManager().addIObserver(this);

		observers = new Vector<IObserver>();

	}

	/**
	 * 
	 */
	public void dispose() {
		dataSetPlot.dispose();
		dataSetImage.dispose();
		dataSetPlot3D.dispose();
		dataSetImages.dispose();
		tagMap.clear();
		tagMap = null;
		PlotManager.getIPlotManager().deleteIObserver(this);
		observers.clear();
		observers = null;
	}

	private String keyWaiting = null;

	/**
	 * This piece of code needs to update the jpanel with the appropriate data which has been sent by the update method.
	 * This should be a plotPackage
	 * 
	 * @param dataSource
	 *            Where the update has come from
	 * @param dataPoint
	 *            The data associated with the update.
	 */
	@Override
	public void update(Object dataSource, final Object dataPoint) {
		logger.debug("update received from " + dataSource.toString() + " " + dataPoint.toString());

		// changed for new code
		// if (dataPoint instanceof PlotPackage) {
		// if (((PlotPackage) dataPoint).getPlotPanelName().equals(
		// this.getName())) {
		// ((PlotPackage) dataPoint).plot(this);
		// }
		if (dataPoint instanceof PlotPasser) {
			if (((PlotPasser) dataPoint).getPanel().equals(getName())) {

				if (keyWaiting != null) {
					keyWaiting = ((PlotPasser) dataPoint).getKey();
				} else {
					keyWaiting = ((PlotPasser) dataPoint).getKey();
					Thread t = uk.ac.gda.util.ThreadManager.getThread(new Runnable() {
						@Override
						public void run() {
							try {
								while (true) {
									String thisKey = keyWaiting;
									PlotPackage pp = (PlotPackage) PlotManager.getIPlotManager().getAttribute(thisKey);

									if (pp != null) {
										pp.plot(MainPlot.this);

										// now refresh the side plot.
										refreshSidePlot();
									} else {
										logger.warn(String.format("In Panel MainPlot key '%s' cannot be found in Plot_Manager (check you don't have two PlotManagers)", thisKey ));
									}
									if (keyWaiting == thisKey) {
										break;
									}
								}
							} catch (DeviceException e) {
								exceptionUtils.logException(logger, e);
							} finally {
								keyWaiting = null;
							}
						}
					});
					t.start();
				}
			}

		} else if (dataPoint instanceof String) {
			logger.info("update the plot for file " + dataPoint.toString());
			final Object data = dataPoint;
			Thread t = uk.ac.gda.util.ThreadManager.getThread(new Runnable() {

				@Override
				public void run() {

					try {
						plotData(data.toString());
					} catch (Exception e) {
						exceptionUtils.logException(logger, e);
					}
				}
			});
			t.start();
			// plotData(dataPoint.toString());
		}
	}

	/**
	 * provide client side plotting of data directly from data file, not through server
	 * 
	 * @param filename
	 * @throws IllegalArgumentException
	 */
	@Deprecated
	// This is a hack for I11 - not needed now following use of addFullDataSet in SimplePLot
	private void plotData(String filename) throws IllegalArgumentException {
		logger.warn("This code is I11 specific, and will not work on any other beamline!");
		logger.info("plotData is called " + filename);
		ScanFileHolder sfh = new ScanFileHolder();
		logger.info("sfh is created " + filename);

		try {
			sfh.load(new MACLoader(filename));
		} catch (ScanFileHolderException e) {
			exceptionUtils.logException(logger, e);
		}
		logger.info("MAC data is loaded " + filename);

		JythonServerFacade.getInstance().print("Data plotting, please wait ...");
		logger.warn("Data plotting, please wait...");
		PlotManager.getInstance().plot("MAC", sfh.getAxis(0), sfh.getAxis(1));
		// JythonServerFacade.getInstance().print(
		// "Display sub-sampled data only. Real data is stored in"
		// + inputFileName);
		JythonServerFacade.getInstance().print("Post processing completed");
		logger.info("Post processing completed.");
	}

	/**
	 * Sets the panel up for displaying graphs, and returns the graph object.
	 * 
	 * @param clearFirst
	 *            True if the plot is to be clear as it appears
	 * @param xName
	 *            The name to be displayed on the X Axis
	 * @return The object which the plotpackage needs to plot to.
	 */
	@Override
	public DataSetPlot setGraphDisplay(boolean clearFirst, String xName) {

		if (activeDisplayType.equals("Graph") == false) {

			activeDisplayType = "Graph";
			// need to bring the dataSetPlot into the fore
			removeAll();
			this.add(dataSetPlot, gbc);

			invalidate();
			validate();
			dataSetImages.cleanUp();
			dataSetPlot3D.cleanUp();

			tagMap.clear();
		}

		if (clearFirst) {
			cleanUp();
			dataSetPlot.setXAxisLabel(xName);
			xAxisLabel = xName;
			dataSetPlot.setXAxisMotor(xAxisLabel);
		}

		return dataSetPlot;
	}

	/**
	 * Function that simply returns the dataSetPlot object
	 * 
	 * @return The dataSetPlot object that is required.
	 */
	@Override
	public DataSetPlot getGraphDisplay() {
		return dataSetPlot;
	}

	/**
	 * Sets the panel up for displaying images(2D)
	 * 
	 * @return The object that the plotpackage needs to plot to.
	 */
	@Override
	public DataSetImage setImageDisplay() {

		if (activeDisplayType.equals("Image") == false) {

			activeDisplayType = "Image";
			removeAll();
			this.add(dataSetImage, gbc);

			invalidate();
			validate();
			dataSetImages.cleanUp();
			dataSetPlot3D.cleanUp();

			dataSetImage.setXOffset(getWidth() / 2);
			dataSetImage.setYOffset(getHeight() / 2);
			dataSetImage.zoom = 1.0;

		}

		return dataSetImage;

	}

	/**
	 * Makes sure that the IPlotWindow is set to PlotImages
	 * 
	 * @return The DataSetImages for use by the plotPackage
	 */

	@Override
	public DataSetImages setImagesDisplay() {
		if (activeDisplayType.equals("Images") == false) {
			activeDisplayType = "Images";
			removeAll();
			this.add(dataSetImages, gbc);
			invalidate();
			validate();
			dataSetPlot3D.cleanUp();
		}
		return dataSetImages;
	}

	/**
	 * Makes sure that the IPlotWindow is set to Plot3D
	 * 
	 * @return The DataSetPlot3D for use by the plotPackage
	 */

	@Override
	public DataSetPlot3D setPlot3DDisplay() {
		if (activeDisplayType.equals("Plot3D") == false) {
			activeDisplayType = "Plot3D";
			removeAll();
			this.add(dataSetPlot3D, gbc);
			dataSetPlot3D.applyColorCast();
			invalidate();
			validate();
			dataSetImages.cleanUp();
		}
		return dataSetPlot3D;
	}
	/**
	 * Function that allows the tagging to be set up for this panel
	 * 
	 * @param tag
	 *            The tag to be attached to a line number
	 * @param lineNumber
	 *            The line number on the plot to associate with the tag.
	 */
	@Override
	public void setTag(int tag, int lineNumber) {
		tagMap.put(tag, lineNumber);
	}

	/**
	 * Returns the line number associated with a particular tag
	 * 
	 * @param tag
	 *            The tag to retrieve.
	 * @return The retrieved line number.
	 */
	@Override
	public int getTag(int tag) {
		return tagMap.get(tag);
	}

	/**
	 * Small function to clean up any excess plot lines left on the graph.
	 */
	private void cleanUp() {
		dataSetPlot.deleteAllLines();
		tagMap.clear();
	}

	/**
	 * gets the visualiser that is being used by the panel for rendering of the raw data to the RGB values
	 * 
	 * @return The visualiser class being used
	 */
	public IMainPlotVisualiser getVisualiser() {
		return dataSetImage.getVisualiser();
	}

	/**
	 * Sets the visualiser to be used in converting the raw images to RGB
	 * 
	 * @param visualiser
	 */
	public void setVisualiser(IMainPlotVisualiser visualiser) {

		dataSetImage.setVisualiser(visualiser);
		// this should also call the applycolourcast function to make the change
		dataSetImage.applyColorCast();
		// to update this as it happens, we need to validate
		dataSetImage.invalidate();
		dataSetImage.validate();
		dataSetPlot3D.setVisualiser(visualiser);
		dataSetPlot3D.applyColorCast();
		dataSetImages.setVisualiser(visualiser);
		dataSetImages.applyColorCast();
		invalidate();
		validate();
	}

	@Override
	public void addIObserver(IObserver anIObserver) {
		observers.add(anIObserver);
	}

	@Override
	public void deleteIObserver(IObserver anIObserver) {
		observers.remove(anIObserver);
	}

	@Override
	public void deleteIObservers() {
		observers.clear();
	}

	/**
	 * Method to be called from the internal plotting routine, which gets the overlay which is to be applied to the
	 * image
	 */
	public void getOverlay() {
		if (dataSetImage.overlay == null && observers.size() != 0) {
			observers.get(0).update(this, this);
		}
	}

	private void refreshSidePlot() {

		// if (dataSetImage.overlay != null && observers.size() != 0) {
		if (observers.size() != 0) {
			observers.get(0).update(this, "New");
		}
	}

	/**
	 * @return dataSetImage
	 */
	public DataSetImage getDataSetImage() {
		return dataSetImage;
	}

	/**
	 * @return dataSetImages
	 */
	public DataSetImages getDataSetImages() {
		return dataSetImages;
	}

	/**
	 * @return dataSetPlot3D
	 */

	public DataSetPlot3D getDataSetPlot3D() {
		return dataSetPlot3D;
	}

	/**
	 * @param dataSetImage
	 */
	public void setDataSetImage(DataSetImage dataSetImage) {
		this.dataSetImage = dataSetImage;
	}

	/**
	 * @param dataSetImages
	 */
	public void setDataSetImages(DataSetImages dataSetImages) {
		this.dataSetImages = dataSetImages;
	}

	/**
	 * @return string containing the current display
	 */

	public String getActiveDisplayType() {
		return activeDisplayType;
	}

	/**
	 * @param activeDisplayType
	 */
	public void setActiveDisplayType(String activeDisplayType) {
		this.activeDisplayType = activeDisplayType;
	}

}