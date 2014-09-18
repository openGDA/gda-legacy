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

import gda.gui.dv.ImageData;
import gda.gui.dv.panels.MainPlot;
import gda.plots.SimpleDataCoordinate;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.WritableRaster;

import javax.swing.JButton;
import javax.swing.JLabel;

import org.eclipse.dawnsci.analysis.dataset.impl.DoubleDataset;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.statistics.HistogramDataset;

/**
 * A basic panel which fits in the side panel and provides colour information for the image manipulator.
 */
public class ColourSelector extends ColourSelectorBase {

	private JLabel lblchart = new JLabel();

	private JFreeChart histogram = null;

	private DrawChart chart = null;

	private GridBagConstraints c = new GridBagConstraints();

	private Double max = null;
	private Double min = null;

	private JButton reset = new JButton("UnZoom Histogram");

	private DoubleDataset dataLink = null;

	private boolean unconfigured = true;


	/**
	 * Basic constructor which initialises the GUI
	 * 
	 * @param main
	 *            The MainPlot to be associated with this panel
	 */
	public ColourSelector(MainPlot main) {
		super(main);

		this.setName("Colour Selector");

		c.gridx = 0;
		c.gridy = 5;
		c.gridwidth = 2;

		reset.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (dataLink != null) {

					max = dataLink.max().doubleValue();
					min = dataLink.min().doubleValue();

					owner.getDataSetImage().applyColorCast();
					owner.getDataSetPlot3D().applyColorCast();
					owner.getDataSetImages().applyColorCast();
					owner.getDataSetImage().repaint();
				}
			}

		});

		this.add(reset, c);

	}

	

	/**
	 * The function that performs the histogram Drawing
	 * 
	 * @param raw
	 *            the raw data
	 * @return the new data in the appropriate form
	 */
	@Override
	public ImageData cast(DoubleDataset raw) {

		dataLink = raw;

		if (max == null) {
			max = raw.max().doubleValue();
		}
		if (min == null) {
			min = raw.min().doubleValue();
		}

		ImageData result = colourCast(raw, max , min);

		// if old stuff exists then remove it.
		if (chart != null) {
			chart.removeAll();
		}

		// now plot the histogram

		HistogramDataset histData = new HistogramDataset();
		histData.addSeries("h1", raw.getData(), 100, min, max);
		histogram = ChartFactory.createHistogram("Histogram", "Value", "Counts", histData, PlotOrientation.VERTICAL,
				false, false, false);

		if (unconfigured == false) {
			this.remove(chart);
		}
		unconfigured = false;

		chart = new DrawChart(histogram);

		chart.setMouseZoomable(false);

		chart.setPreferredSize(new Dimension(300, 300));
		chart.setMinimumSize(new Dimension(200, 200));

		c.gridx = 0;
		c.gridy = 4;
		c.gridwidth = 2;

		this.add(chart, c);

		chart.addMouseListener(new MouseListener() {
			Double tmin = min;
			Double tmax = max;

			@Override
			public void mouseClicked(MouseEvent e) {

			}

			@Override
			public void mouseEntered(MouseEvent e) {

			}

			@Override
			public void mouseExited(MouseEvent e) {

			}

			@Override
			public void mousePressed(MouseEvent e) {
				SimpleDataCoordinate coordinates = convertMouseEvent(e);
				if (chart.getScreenDataArea().contains(e.getX(), e.getY())) {
					tmin = coordinates.getX();
				} else {
					if (chart.getScreenDataArea().outcode(e.getX(), e.getY()) != Rectangle2D.OUT_RIGHT) {
//						System.out.println("out of bounds");
//						System.out.printf("Mouse: %d\n", e.getX());
					} else {
						tmin = max;
					}
				}

			}

			@Override
			public void mouseReleased(MouseEvent e) {
				boolean update = false;
				SimpleDataCoordinate coordinates = convertMouseEvent(e);

				if (chart.getScreenDataArea().contains(e.getX(), e.getY())) {
					tmax = coordinates.getX();
					update = true;
				} else {
					if (chart.getScreenDataArea().outcode(e.getX(), e.getY()) != Rectangle2D.OUT_LEFT) {
//						System.out.println("out of bounds");
//						System.out.printf("Mouse: %d\n", e.getX());
					} else {
						tmax = min;
						update = true;
					}
				}
				if (update && tmin != tmax) {
					if (tmin > tmax) { // check and correct limits
						Double t = tmin;
						tmin = tmax;
						tmax = t;
					}
					min = tmin;
					max = tmax;

					owner.getDataSetImage().applyColorCast();
					owner.getDataSetPlot3D().applyColorCast();
					owner.getDataSetImage().repaint();
				}
			}

			public SimpleDataCoordinate convertMouseEvent(MouseEvent me) {
				return new SimpleDataCoordinate(

				histogram.getXYPlot().getDomainAxis().java2DToValue(me.getX(), chart.getScreenDataArea(),
						histogram.getXYPlot().getDomainAxisEdge()),

				histogram.getXYPlot().getRangeAxis().java2DToValue(me.getY(), chart.getScreenDataArea(),
						histogram.getXYPlot().getRangeAxisEdge()));

			}

		});

		lblchart.invalidate();
		lblchart.validate();

		this.invalidate();
		this.validate();

		return result;

	}

	
	private class DrawChart extends ChartPanel {

		/**
		 * Constructor, this just passes the data through
		 * 
		 * @param arg0
		 */
		public DrawChart(JFreeChart arg0) {
			super(arg0);
		}

		/**
		 * Main paint command which allows the colour bar to be drawn on the histogram
		 * 
		 * @param arg0
		 */
		@Override
		public void paintComponent(Graphics arg0) {
			super.paintComponent(arg0);

			Graphics2D g = (Graphics2D) arg0;

			// ok, so the first thing to do is to construct a dataset which is
			// the correct size which can have the cast applied to it
			int[] dims = { 15, (int) this.getScreenDataArea().getWidth() };
			DoubleDataset banner = new DoubleDataset(dims);

			double min = histogram.getXYPlot().getDomainAxis().getLowerBound();
			double max = histogram.getXYPlot().getDomainAxis().getUpperBound();

			for (int i = 0; i < dims[1]; i++) {
				double val = min + (max - min) * ((double) i / (double) dims[1]);
				for (int j = 0; j < dims[0]; j++) {
					int[] pos = { j, i };
					banner.set(val, pos);
				}
			}

			// get the new dataset
			ImageData image = colourCast(banner, max, min);

			BufferedImage bi = new BufferedImage(dims[1], dims[0], BufferedImage.TYPE_INT_RGB);

			WritableRaster raster = bi.getRaster();

			int[] pixels = ((DataBufferInt) raster.getDataBuffer()).getData();

			for (int i = 0; i < pixels.length; i++) {
				pixels[i] = image.get(i);
			}

			g.drawImage(bi, (int) this.getScreenDataArea().getX(), (int) this.getScreenDataArea().getY(), null);

		}
	}
}