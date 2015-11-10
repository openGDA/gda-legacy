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

//import gda.gui.dv.ImageData;
import gda.observable.IObserver;

import java.awt.Component;
import java.util.Vector;

import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * This simple class is a holder for all the vispanels that are needed. It should allow simple methods for adding and
 * removing panels.
 */
public class SidePlot extends JTabbedPane implements IObserver, ChangeListener {

	IMainPlotManipulator currentManipulatorPanel = null;

	MainPlot owner = null;

	Vector<IMainPlotManipulator> manipulators = new Vector<IMainPlotManipulator>();
	Vector<IMainPlotVisualiser> visualisers = new Vector<IMainPlotVisualiser>();
	Vector<IMainPlotNewImageUpdater> imageUpdaters = new Vector<IMainPlotNewImageUpdater>();

	/**
	 * Basic constructor
	 * 
	 * @param observedPlot
	 *            The plot that the side plot is associated with
	 */
	public SidePlot(MainPlot observedPlot) {
		// register with the mainplot.
		observedPlot.addIObserver(this); //FIXME: potential race condition

		// collect the main plot
		owner = observedPlot;

		// register for change events
		addChangeListener(this); //FIXME: potential race condition
	}

	/**
	 * Adds a panel to the side panel and then attaches it to the correct lists for processing
	 * 
	 * @param panel
	 */
	public void addPanel(VisPanel panel) {
		this.add(panel);

		if (panel instanceof IMainPlotVisualiser) {
			visualisers.add((IMainPlotVisualiser) panel);
			owner.dataSetImage.setVisualiser((IMainPlotVisualiser) panel);
		}

		if (panel instanceof IMainPlotManipulator) {
			manipulators.add((IMainPlotManipulator) panel);
		}

		if (panel instanceof IMainPlotNewImageUpdater) {
			imageUpdaters.add((IMainPlotNewImageUpdater) panel);
		}
	}

	private IMainPlotManipulator getManipulator(Component panel) {
		for (int i = 0; i < manipulators.size(); i++) {
			if (manipulators.get(i) == panel) {
				return manipulators.get(i);
			}
		}
		return null;
	}

	private IMainPlotVisualiser getVisualiser(Component panel) {
		for (int i = 0; i < visualisers.size(); i++) {
			if (visualisers.get(i) == panel) {
				return visualisers.get(i);
			}
		}
		return null;
	}

	private void refreshAllPanels(MainPlot mainPlot) {

		if (mainPlot.getActiveDisplayType().equalsIgnoreCase("Image")) {

			// don't need to do this with shared overlays
			// first make sure that all the panels are updated
//			for (int i = 0; i < manipulators.size(); i++) {
//				mainPlot.dataSetImage.overlay = manipulators.get(i).getOverlay(mainPlot.dataSetImage.overlay, mainPlot.dataSetImage.pix);
//			}

			// then all the observing panels
			for (int i = 0; i < imageUpdaters.size(); i++) {
				imageUpdaters.get(i).newImageUpdate(mainPlot.dataSetImage.pix);
			}

		}

		// then make sure that the active screens overlay is active

		// look to the active tab and call the getOverlay function on it if it is a manipulator

		Component active = getSelectedComponent();

		// see if this is any of the manipulators
		currentManipulatorPanel = getManipulator(active);

		if (mainPlot.dataSetImage.pix != null) {
			if (currentManipulatorPanel != null)
				mainPlot.dataSetImage.overlay = currentManipulatorPanel.getOverlay(mainPlot.dataSetImage.overlay, mainPlot.dataSetImage.pix);
			
//			if (mainPlot.dataSetImage.overlay != null) {
//				mainPlot.dataSetImage.overlay.clear();
//				mainPlot.dataSetImage.overlay.flipImage();
//				mainPlot.dataSetImage.validate();
//				mainPlot.dataSetImage.repaint();
//			}
		}

		if (active instanceof IMainPlotNewPlotUpdater&&mainPlot.getActiveDisplayType().equalsIgnoreCase("Graph")) {

			System.out.println(mainPlot.getGraphDisplay().getYAxis().size());

			((IMainPlotNewPlotUpdater) active).newPlotUpdate(mainPlot.getGraphDisplay().getXAxis(), mainPlot.getGraphDisplay().getYAxis());

		}


	}

	/**
	 * Update method which is called by the main plot to set up everything with the side plot like the registration etc.
	 * 
	 * @param theObserved
	 * @param changeCode
	 */
	@Override
	public void update(Object theObserved, Object changeCode) {

		if (theObserved instanceof MainPlot) {

			if (changeCode instanceof String) {
				refreshAllPanels((MainPlot) theObserved);
			}

			if (changeCode instanceof MainPlot) {

				// then we need to return the overlay.
				// look to the active tab and call the getOverlay function on it if it is a manipulator

				MainPlot mainPlot = (MainPlot) theObserved;
//				if (owner.dataSetImage.overlay != null) {
//					System.out.println("boo");
//					owner.dataSetImage.overlay.clear();
//					owner.dataSetImage.overlay.flipImage();
//					owner.dataSetImage.validate();
//					owner.dataSetImage.repaint();
//				}

				if (currentManipulatorPanel != null)
					currentManipulatorPanel.releaseOverlay();

				Component active = getSelectedComponent();

				// see if this is any of the manipulators
				currentManipulatorPanel = getManipulator(active);

				if (mainPlot.dataSetImage.pix != null && currentManipulatorPanel != null) {
					mainPlot.dataSetImage.overlay = currentManipulatorPanel.getOverlay(mainPlot.dataSetImage.overlay, mainPlot.dataSetImage.pix);
//					if (mainPlot.dataSetImage.overlay != null) {
//						mainPlot.dataSetImage.overlay.clear();
//						mainPlot.dataSetImage.overlay.flipImage();
//						mainPlot.dataSetImage.validate();
//						mainPlot.dataSetImage.repaint();
//					}
				}

				if (active instanceof IMainPlotNewImageUpdater&&mainPlot.getActiveDisplayType().equalsIgnoreCase("Image")) {
					((IMainPlotNewImageUpdater) active).newImageUpdate(mainPlot.dataSetImage.pix);
				}

				if (active instanceof IMainPlotNewPlotUpdater&&mainPlot.getActiveDisplayType().equalsIgnoreCase("Graph")) {

					((IMainPlotNewPlotUpdater) active).newPlotUpdate(mainPlot.getGraphDisplay().getXAxis(), mainPlot.getGraphDisplay().getYAxis());

				}
			}
		}
	}

	/**
	 * This function picks up if the tab is changed, and then uses this information to register the tab as the
	 * manipulator of visualiser or both
	 * 
	 * @param e
	 */
	@Override
	public void stateChanged(ChangeEvent e) {

//		owner.dataSetImage.overlay = null;
		// now clear shared overlay
		if (owner.dataSetImage.overlay != null) {
			owner.dataSetImage.overlay.clear();
			owner.dataSetImage.overlay.flipImage();
		}

		if (currentManipulatorPanel != null)
			currentManipulatorPanel.releaseOverlay();

		Component active = getSelectedComponent();

		// see if this is any of the manipulators
		currentManipulatorPanel = getManipulator(active);

		if (currentManipulatorPanel != null) {
			owner.dataSetImage.setManipulator(currentManipulatorPanel);
			owner.dataSetImage.overlay = currentManipulatorPanel.getOverlay(owner.dataSetImage.overlay, owner.dataSetImage.pix);
		}

		// see if this is any of the Visualisers
		IMainPlotVisualiser visualiserPanel = getVisualiser(active);

		if (visualiserPanel != null) {
			owner.setVisualiser(visualiserPanel);
		}

		// refresh if possible
		owner.dataSetImage.validate();
		owner.dataSetImage.repaint();
	}

	/**
	 * 
	 */
	public void dispose() {
		owner.deleteIObserver(this);
		currentManipulatorPanel = null;
		removeAll();

	}
}
