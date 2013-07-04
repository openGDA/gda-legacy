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

import gda.plots.PlotTreeLegend;
import gda.plots.SimplePlot;
import gda.plots.Type;
import gda.plots.XYDataHandler;
import gda.plots.XYDataHandlerLegend;
import gda.scan.IScanDataPoint;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import org.jfree.chart.ChartPanel;
import org.jfree.data.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The class encapulates a gda.plots.SimplePlot object for use by a JythonTerminal object. The data it plot is presented
 * to the class in the addData method. Whether the data is plotted and how is controllable by the ScanPlotSettings
 * member of the ScanDataPoint
 */
public class ScanPlot extends JPanel {
	private static final Logger logger = LoggerFactory.getLogger(ScanPlot.class);

	private XYDataHandler plot;
	XYDataHandlerLegend legendPanel;
	private ScanDataPointPlotter plotter;

	String name = new String("Plot");

	BorderLayout borderLayout1 = new BorderLayout();

	/**
	 * Constructor
	 */
	public ScanPlot() {

		plot = new SimplePlot(SimplePlot.LINECHART, true, false);
		legendPanel = new PlotTreeLegend(plot);

		// plot=new SWTSimplePlot();
		// legendPanel = new SWTPlotTreeLegend(plot);

		plotter = new ScanDataPointPlotter(plot, legendPanel,"");
		try {
			plot.setTurboMode(true);
			plot.setYAxisLabel("various units");
			plot.setLegendVisible(false);
			plot.setScientificXAxis();
			plot.setScientificYAxis();
			plot.setVerticalXAxisTicks(true);
			jbInit();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
	}

	/**
	 * 
	 */
	public void dispose() {
		plot.dispose();
		plotter.dispose();
	}

	/**
	 * Adds extra data points to lines on the graph. If the appropriate lines do not exist then they are created.
	 * 
	 * @param point
	 *            ScanDataPoint
	 */
	public synchronized void addData(IScanDataPoint point) {
		plotter.addData(point);
	}

	void unselected() throws IOException {
		plot.archive(true,"dummy");
	}

	void selected() {
		plot.unArchive();
	}

	void copySettings(ScanPlot other) {
		if (other != null)
			plot.copySettings(other.plot);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * sets the title on the plot
	 * 
	 * @param title
	 */
	public void setTitle(String title) {
		plot.setTitle(title);
		repaint();
	}

	/**
	 * @param t
	 */
	public void setLineType(Type t) {
		plot.setLineType(t);
	}

	void hideLegend(boolean hide) {
		this.removeAll();
		if (plot instanceof Component && legendPanel instanceof Component) {
			this.add((Component) plot, BorderLayout.CENTER);
			JPanel top = new JPanel();
			top.setLayout(new BorderLayout());
			top.add(legendVisible, BorderLayout.EAST);
			this.add(top, BorderLayout.NORTH);
			if (!hide)
				this.add((Component) legendPanel, BorderLayout.EAST);
			((Component) plot).setPreferredSize(new Dimension(800, 800));
			if (!hide) {
				((Component) legendPanel).setPreferredSize(new Dimension(200, 800));
			}
		}
		this.validate();

	}

	JCheckBoxMenuItem legendVisible = new JCheckBoxMenuItem("Legend");

	/*
	 * Creates the UI. Created by JCreator. @throws Exception
	 */
	private void jbInit() {
		this.setRequestFocusEnabled(true);
		legendVisible.setMnemonic(KeyEvent.VK_M);
		legendVisible.setSelected(false);
		legendVisible.setToolTipText("Show the legend");
		legendVisible.addActionListener(new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent e) {
				hideLegend(!((JMenuItem) e.getSource()).isSelected());
			}
		});
		{
			if (plot instanceof ChartPanel && legendPanel instanceof Component) {
				((ChartPanel) plot).setMinimumDrawWidth(10);
				((ChartPanel) plot).setMinimumDrawHeight(150);
				((Component) plot).setPreferredSize(new Dimension(800, 500));
				((Component) legendPanel).setPreferredSize(new Dimension(100, 500));
				setLayout(new BorderLayout());
				legendVisible.setSelected(true);
			}
			hideLegend(false);
		}
	}

	/**
	 * Clears the graph of all data
	 */
	public synchronized void clearGraph() {
		plot.setZooming(false);
		plot.deleteAllLines();
		plotter.clearGraph();
		legendPanel.removeAllItems();
		repaint();
	}

	/**
	 * @param leftRangeBounds
	 */
	public void setLeftRangeBounds(Range leftRangeBounds) {
		plot.setLeftRangeBounds(leftRangeBounds);
	}

	/**
	 * @param rightRangeBounds
	 */
	public void setRightRangeBounds(Range rightRangeBounds) {
		plot.setRightRangeBounds(rightRangeBounds);
	}

	/**
	 * @param domainBounds
	 */
	public void setDomainBounds(Range domainBounds) {
		plot.setDomainBounds(domainBounds);
	}
}


