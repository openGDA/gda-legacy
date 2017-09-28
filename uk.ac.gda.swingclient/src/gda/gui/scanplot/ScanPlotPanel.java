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

package gda.gui.scanplot;

import gda.configuration.properties.LocalProperties;
import gda.factory.Finder;
import gda.jython.IScanDataPointObserver;
import gda.jython.IScanDataPointProvider;
import gda.jython.InterfaceProvider;
import gda.plots.Type;
import gda.scan.IScanDataPoint;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A visual component to hold a GDAJythonInterpreter. It is designed to be similar to a command terminal.
 */
public class ScanPlotPanel extends JPanel implements IScanDataPointObserver, ChangeListener {

	private static final Logger logger = LoggerFactory.getLogger(ScanPlotPanel.class);

	private static final String TERMINALNAME = "JythonTerminal";

	// to make sure configure is only run once
	private boolean configured = false;

	// number of graphs crated by configure()
	int numberInitialGraphs = 0;

	// will data from new scans go to new lines in current graphs or to new
	// plots?
	boolean extraGraphs = false;

	boolean extraGraphsPossible = false;

	// Added flag to allow for the clearing of the graph on a scan by scan
	// basis.
	boolean clearGraphs = false;

	// objects used in the graphics
	Vector<ScanPlot> graphs = new Vector<ScanPlot>();

	private HashMap<String, ScanPlot> sourceToGraph = new HashMap<String, ScanPlot>();

	// GUI components
	Finder finder = Finder.getInstance();

	int plotNumber = 0;

	JTabbedPane graphPanel = new JTabbedPane();

	JPanel graphOptionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

	JButton btnClear = new JButton();

	JCheckBox chkExtraGraphs = new JCheckBox();

	boolean connectState = true;
	JButton btnToggleConnectState = new JButton();

	JCheckBox chkClearGraphs = new JCheckBox();

	JComboBox cmbLineTypeSelection = Type.getChooser();
	/**
	 * method called to connect to other objects in the system. must be called when other objects are instantiated
	 */
	public void configure() {
		if (!configured) {
			InterfaceProvider.getScanDataPointProvider().addIScanDataPointObserver(this);
			if (numberInitialGraphs == 0) {
				// remove graphics and hide the divider
			} else if (numberInitialGraphs >= 1) {
				for (int i = 0; i < numberInitialGraphs; i++) {
					ScanPlot newPlot = new ScanPlot();
					newPlot.setName(Integer.toString(i));
					plotNumber++;
					addPlot(newPlot,"");
				}
				if (extraGraphs) {
					extraGraphs = false;
					extraGraphsPossible = true;
				} else {
					graphOptionPanel.remove(chkExtraGraphs);
				}
			}
			configured = true;
		}

	}

	/**
	 * Remove this terminal from the command server observer list. This method should be called to ensure new terminals
	 * can be registered with the command server.
	 */
	public void dispose() {
		InterfaceProvider.getScanDataPointProvider().deleteIScanDataPointObserver(this);
		for(ScanPlot s : graphs){
			graphPanel.remove(s);
			graphs.remove(s);
			s.dispose();
		}
		sourceToGraph.clear();
	}

	@Override
	public String getName() {
		return TERMINALNAME;
	}

	/**
	 * 
	 */
	public ScanPlotPanel() {
		try {
			jbInit();
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		}
		graphPanel.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		graphPanel.addChangeListener(this);
	}

	/**
	 * Updates the plotting area of the panel in the Swing thread
	 * 
	 * @param dataPoint
	 */
	void updatePlot(IScanDataPoint dataPoint) {
		SwingUtilities.invokeLater(new PlotUpdater(dataPoint, this));
	}

	/**
	 * Runnable class which does the work to update the plotting area
	 */
	private final class PlotUpdater implements Runnable {

		IScanDataPoint dataPoint;
		ScanPlotPanel parent;

		/**
		 * @param dataPoint
		 * @param parent
		 */
		public PlotUpdater(IScanDataPoint dataPoint, ScanPlotPanel parent) {
			this.dataPoint = dataPoint;
			this.parent = parent;
		}

		@Override
		public void run() {
			// if this object is plotting data and was the source of the
			// command
			// which ran the scan.
			try{
				if (numberInitialGraphs > 0) {

					// check if we have seen the source before.
					if (sourceToGraph.containsKey(dataPoint.getUniqueName())) {
						ScanPlot plot = sourceToGraph.get(dataPoint.getUniqueName());
						plot.addData(dataPoint);
					}
					// a new scan
					else {
						// are we making new graphs?
						if (extraGraphs) {
							ScanPlot newPlot = new ScanPlot();
							newPlot.setName(Integer.toString(plotNumber));
							plotNumber++;
							newPlot.setTitle("Current File Name: " + dataPoint.getCurrentFilename());
							addPlot(newPlot, dataPoint.getCurrentFilename());
							newPlot.addData(dataPoint);
							sourceToGraph.put(dataPoint.getUniqueName(), newPlot);
						}
						// else send data to the currently selected tab
						else {

							ScanPlot selectedPlot = (ScanPlot) graphPanel.getSelectedComponent();

							// this is a new scan so if the flag is set, clear the
							// old graph
							if (clearGraphs) {
								selectedPlot.clearGraph();
							}

							selectedPlot.setTitle("Current File Name: " + dataPoint.getCurrentFilename());
							selectedPlot.addData(dataPoint);
							sourceToGraph.put(dataPoint.getUniqueName(), selectedPlot);
						}
					}
					parent.updateUI();
				}
			} catch (Exception e){
				logger.error(e.getMessage(),e);
			}
			
		}

	}

	/**
	 * From the IObservers interface. Used when a scan has been initiated from the terminal or jython editor. This
	 * terminal is registered as an observer, so if the graphics option has been selected for this object then the data
	 * will be shown on a graph.
	 * 
	 * @param dataSource
	 *            Object
	 * @param dataPoint
	 *            Object
	 */
	@Override
	public void update(Object dataSource, Object dataPoint) {

		// if from scans then objects are in the format String, ScanDataPoint
		if(!connectState){
			return;
		}
		if (dataPoint instanceof IScanDataPoint && dataSource instanceof IScanDataPointProvider) {
			updatePlot((IScanDataPoint) dataPoint);
		}
	}

	/**
	 * Returns whether new data sets would be placed in new graphs or as extra lines to current graphs.
	 * 
	 * @return boolean
	 */
	public boolean isExtraGraphs() {
		return extraGraphs;
	}

	/**
	 * Get the number of initial graphs displayed in the panel.
	 * 
	 * @return int
	 */
	public int getNumberInitialGraphs() {
		return numberInitialGraphs;
	}

	/**
	 * Sets whether new data sets would be placed in new graphs or as extra lines to current graphs.
	 * 
	 * @param extraGraphs
	 *            boolean
	 */
	public void setExtraGraphs(boolean extraGraphs) {
		this.extraGraphs = extraGraphs;
	}

	/**
	 * Sets the number of initial graphs created by the configure method.
	 * 
	 * @param numberInitialGraphs
	 *            int
	 */
	public void setNumberInitialGraphs(int numberInitialGraphs) {
		this.numberInitialGraphs = numberInitialGraphs;
	}

	/*
	 * Clears the highlighted graph. @param e ActionEvent
	 */
	void btnClear_actionPerformed() {

		ScanPlot selectedPlot = (ScanPlot) graphPanel.getSelectedComponent();
		selectedPlot.clearGraph();

		if (extraGraphsPossible) {

			// then if there are more than the min number of graphs, remove the current one
			if (graphs.size() > numberInitialGraphs) {
				// remove from panel
				sourceToGraph.values().remove(selectedPlot);
				graphPanel.remove(selectedPlot);
				graphs.remove(selectedPlot);
				selectedPlot.dispose();
				selectedPlot = null;
			}
		}

		this.updateUI();

	}

	/*
	 * Change whether new graphs are created when data from new scans is received. @param actionEvent
	 */
	void chkExtraGraphs_actionPerformed() {
		extraGraphs = chkExtraGraphs.isSelected();
	}

	// function which is called whenever the checkbox is changed.
	void chkClearGraphs_actionPerformed() {
		clearGraphs = chkClearGraphs.isSelected();
	}

	/**
	 * Change the type of line plotted, support LINEONLY, POINTONLY, LINEANDPOINT
	 * 
	 * @param actionEvent
	 */
	void cmbLineTypeSelection_actionPerformed(ActionEvent actionEvent) {

		Type t = (Type) ((JComboBox) actionEvent.getSource()).getSelectedItem();
		ScanPlot selectedPlot = (ScanPlot) graphPanel.getSelectedComponent();
		selectedPlot.setLineType(t);
		this.updateUI();
	}

	private void addPlot(ScanPlot newPlot, String tooltip) {

		if (graphPanel.getSelectedComponent() != null && graphPanel.getSelectedComponent()  instanceof ScanPlot)
			newPlot.copySettings((ScanPlot)graphPanel.getSelectedComponent());

		graphs.add(newPlot);
		graphPanel.addTab(newPlot.getName(), newPlot);

		
		
		int numberOfGraphs = graphPanel.getTabCount();

		int maxGraphs = LocalProperties.getInt("gda.jython.jythonterminal.maxnumberoftabs", 10);

		if (numberOfGraphs > maxGraphs) {
			ScanPlot s = graphs.firstElement();
			sourceToGraph.values().remove(s);
			graphPanel.remove(s);
			graphs.remove(s);
			s.dispose();
		}
		for( ScanPlot plot : graphs){
			try {
				plot.unselected();
			} catch (IOException e) {
				logger.error("Exception archiving existing plots", e);
			}
		}
		
		int index = graphPanel.indexOfComponent(newPlot);
		graphPanel.setSelectedIndex(index);
		graphPanel.setToolTipTextAt(index,tooltip);
	}

	private void setConnectState(boolean connectState){
		this.connectState = connectState;
		btnToggleConnectState.setText(connectState ? "Disconnect":"Connect");
		btnToggleConnectState.setForeground(connectState ? btnClear.getForeground(): Color.RED );
		btnToggleConnectState.setToolTipText(connectState ? "Reject new datapoints" : "Accept new datapoints");
		validate();
	}

	private void jbInit() {
		this.setEnabled(true);
		this.setDebugGraphicsOptions(0);

		btnClear.setText("Clear");
		btnClear.setToolTipText("Existing scans in current tabbed graph are removed, and the graph " +
				"is removed if the total number of graphs exceeds a limit.");
		btnClear.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				btnClear_actionPerformed();
			}
		});
		chkExtraGraphs.setText("Create new graph");
		chkExtraGraphs.setToolTipText("New scans are output in a new tabbed graph panel");
		chkExtraGraphs.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				chkExtraGraphs_actionPerformed();
			}
		});
		chkClearGraphs.setText("Clear old graph");
		chkClearGraphs.setToolTipText("Existing scans are removed when new scans are created");
		chkClearGraphs.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				chkClearGraphs_actionPerformed();
			}
		});

		
		// set default to line only plot
		cmbLineTypeSelection.setSelectedIndex(0);
		cmbLineTypeSelection.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				cmbLineTypeSelection_actionPerformed(actionEvent);
			}
		});

		btnToggleConnectState.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				setConnectState(!connectState);
			}
		});
		setConnectState(true);		
		setLayout(new BorderLayout());
		add(graphPanel, BorderLayout.CENTER);
		add(graphOptionPanel, BorderLayout.SOUTH);
		graphOptionPanel.add(btnClear);
		graphOptionPanel.add(chkExtraGraphs);
		graphOptionPanel.add(chkClearGraphs);
		graphOptionPanel.add(cmbLineTypeSelection);
		graphOptionPanel.add(btnToggleConnectState);
	}



	@Override
	public void stateChanged(ChangeEvent evt) {
		JTabbedPane pane = (JTabbedPane)evt.getSource();
		ScanPlot s = (ScanPlot)(pane.getSelectedComponent());
		s.selected();
		
	}
}


