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

import gda.factory.FactoryException;
import gda.gui.AcquisitionPanel;
import gda.gui.dv.panels.MainPlot;
import gda.gui.dv.panels.SidePlot;
import gda.gui.dv.panels.VisPanel;

import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;

import org.eclipse.january.dataset.DoubleDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DataVectorPlot Class
 */
public class DataVectorPlot extends AcquisitionPanel {

	private static final Logger logger = LoggerFactory.getLogger(DataVectorPlot.class);

	private String mainPlotPosition = "West";
	private String dimension = "1D";

	private List<String> panelList = new ArrayList<String>();

	private List<VisPanel> panels = new ArrayList<VisPanel>();

	MainPlot mainPlot = null;

	SidePlot sidePlot = null;

	JSplitPane splitPane = null;
	boolean configured = false;

	/** Number of instances created so far */
	private static AtomicInteger dataVectorPlotCount = new AtomicInteger();
	
	/**
	 * Constructor
	 */
	public DataVectorPlot() {

		// set the layout of the panel
		setLayout(new GridLayout());

		// create a constraint that tells the panel to fill with its content
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = c.gridy = 0;
		c.fill = GridBagConstraints.BOTH;

		int thisPlotNumber = dataVectorPlotCount.incrementAndGet();
		setLabel("Data Vector" + ((thisPlotNumber == 1) ? "" : " " + thisPlotNumber));
	}

	@SuppressWarnings("unchecked")
	@Override
	public void configure() throws FactoryException {
		if( configured) {
			return;
		}
		mainPlot = new MainPlot(getLabel());

		if (panelList.size() > 0) {

			sidePlot = new SidePlot(mainPlot);

			// configure the sideplot
			for (String className : panelList) {

				try {

					Constructor<VisPanel> con = (Constructor<VisPanel>) Class.forName(className).getConstructor(
							mainPlot.getClass());

					VisPanel visPanel = con.newInstance(mainPlot);

					sidePlot.addPanel(visPanel);
					panels.add(visPanel);

				} catch (ClassNotFoundException e) {
					logger.error("Class file for "+className+" visPanel not found. It will not be available.", e);
				} catch (InstantiationException e) {
					logger.error("VisPanel "+className+" cannot be instantiated. ", e);
				} catch (Throwable e) {
					logger.error("Error initialising "+className+" visPanel. ", e);
				}

			}

			splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, mainPlot, sidePlot);

			if ("North".equals(mainPlotPosition)) {
				splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, mainPlot, sidePlot);
			} else

				if ("South".equals(mainPlotPosition)) {
					splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, sidePlot, mainPlot);
				} else if ("East".equals(mainPlotPosition)) {
					splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sidePlot, mainPlot);
				} else {
					splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, mainPlot, sidePlot);
				}

			splitPane.setResizeWeight(0.5);

			this.add(splitPane);

		} else {
			this.add(mainPlot);
		}
		configured = true;
	}

	public void plotXY(DoubleDataset xData, DoubleDataset... yData) {

		GraphUpdate doGUIUpdate = new GraphUpdate();

		doGUIUpdate.init(mainPlot, true, xData, yData);

		SwingUtilities.invokeLater(doGUIUpdate);
	}


	public void plotOverXY(DoubleDataset xData, DoubleDataset... yData) {

		GraphUpdate doGUIUpdate = new GraphUpdate();

		doGUIUpdate.init(mainPlot, false, xData, yData);

		SwingUtilities.invokeLater(doGUIUpdate);
	}

	public String getMainPlotPosition() {
		return mainPlotPosition;
	}

	public void setMainPlotPosition(String mainPlotPosition) {
		this.mainPlotPosition = mainPlotPosition;
	}

	/**
	 * Getter
	 * 
	 * @return value
	 */
	public List<String> getPanelList() {
		return panelList;
	}

	/**
	 * Setter
	 * 
	 * @param panelList
	 */
	public void setPanelList(List<String> panelList) {
		this.panelList = panelList;
	}

	/**
	 * Returns the dimension of this plot.
	 * 
	 * @return the dimension, e.g. "1D"
	 */
	public String getPlotDimension() {
		return dimension;
	}

	/**
	 * Disposes this panel.
	 */
	public void dispose() {
		mainPlot.dispose();
		sidePlot.dispose();

	}

	/**
	 * Sets the dimension of this plot.
	 * 
	 * @param dimension2 the dimension
	 */
	public void setPlotDimension(String dimension2) {
		dimension = dimension2;

	}

}