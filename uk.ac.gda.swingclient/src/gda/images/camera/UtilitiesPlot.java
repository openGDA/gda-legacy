/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package gda.images.camera;

import gda.configuration.properties.LocalProperties;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Shows the effect of gonio rotation on actual moves for given requested
 * sample moves.
 */
public class UtilitiesPlot extends JFrame {
	
	protected static final Dimension PLOT_SIZE = new Dimension(200, 200);

	/**
	 * Launches the program.
	 * 
	 * @param args command-line arguments
	 */
	public static void main(String args[]) {
		UtilitiesPlot ut = new UtilitiesPlot();
		ut.setVisible(true);
	}
	
	private JSlider sliderH;
	private JSlider sliderV;
	private JSlider sliderB;
	private JSlider sliderOmega;
	
	private Plot[] plots;
	
	/**
	 * Creates a {@link UtilitiesPlot} window.
	 */
	public UtilitiesPlot() {
		super(UtilitiesPlot.class.getSimpleName());
		
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setLayout(new FlowLayout());
		
		sliderH = slider(-10, 10, 10, 1, 5);
		sliderV = slider(-10, 10, 10, 1, 5);
		sliderB = slider(-10, 10, 10, 1, 5);
		sliderOmega = slider(-180, 180, 0, 15, 90);
		
		ChangeListener updateListener = new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				update();
			}
		};
		
		sliderH.addChangeListener(updateListener);
		sliderV.addChangeListener(updateListener);
		sliderB.addChangeListener(updateListener);
		sliderOmega.addChangeListener(updateListener);
		
		plots = new Plot[] {
			new Plot("i02", "right", 1, 0), // Y+X
			new Plot("i03", "left",  1, 0), // Y+X
			new Plot("i24", "right", 1, 2), // Y+Z
		};
		
		add(new JLabel("h:"));
		add(sliderH);
		add(new JLabel("v:"));
		add(sliderV);
		add(new JLabel("b:"));
		add(sliderB);
		add(new JLabel("omega:"));
		add(sliderOmega);
		
		for (Plot plot : plots) {
			add(plot);
		}
		
		update();
		
		pack();
	}
	
	private static JSlider slider(int min, int max, int value, int minorTickSpacing, int majorTickSpacing) {
		JSlider slider = new JSlider(SwingConstants.VERTICAL, min, max, value);
		slider.setMinorTickSpacing(minorTickSpacing);
		slider.setMajorTickSpacing(majorTickSpacing);
		slider.setPaintLabels(true);
		slider.setPaintTicks(true);
		slider.setPaintTrack(true);
		slider.setSnapToTicks(true);
		return slider;
	}
	
	private void update() {
		final int h = sliderH.getValue();
		final int v = sliderV.getValue();
		final int b = sliderB.getValue();
		final int omega = sliderOmega.getValue();
		
		for (Plot plot : plots) {
			plot.setPosition(h, v, b, omega);
			plot.repaint();
		}
	}

	protected static void initProperties(String beamline, String horizontaldirection) {
		LocalProperties.set(LocalProperties.GDA_BEAMLINE_NAME, beamline);
		LocalProperties.set(LocalProperties.GDA_IMAGES_HORIZONTAL_DIRECTION, horizontaldirection);
	}
	
}

class Plot extends JPanel {
	
	private static final int ORIGIN_DOT_SIZE = 8;
	
	private static final int MOVE_DOT_SIZE = 4;
	
	private static final int PLOT_SCALE = 2;
	
	private static final Object LOCK = new Object();

	private String beamline;
	private String horizontaldirection;
	private int first;
	private int second;
	
	public Plot(String beamline, String horizontaldirection, int first, int second) {
		this.beamline = beamline;
		this.horizontaldirection = horizontaldirection;
		this.first = first;
		this.second = second;
		
		setMinimumSize(UtilitiesPlot.PLOT_SIZE);
		setMaximumSize(UtilitiesPlot.PLOT_SIZE);
		setPreferredSize(UtilitiesPlot.PLOT_SIZE);
	}

	private double h;
	private double v;
	private double b;
	private double omega;
	
	public void setPosition(double h, double v, double b, double omega) {
		this.h = h;
		this.v = v;
		this.b = b;
		this.omega = omega;
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		double[] move;
		synchronized (LOCK) {
			UtilitiesPlot.initProperties(beamline, horizontaldirection);
			move = Utilities.micronToXYZMove(h, v, b, omega);
//			System.out.println(beamline + ": " + Arrays.toString(new double[] {h, v, b}) + " => " + Arrays.toString(move));
		}
		
		final Point centre = new Point(getWidth()/2, getHeight()/2);
		
		final double x = move[first]  * PLOT_SCALE;
		final double y = move[second] * PLOT_SCALE;
		
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		// Clear area
		g2d.setColor(Color.LIGHT_GRAY);
		g2d.fillRect(0, 0, getWidth(), getHeight());
		
		// Grid lines
		g2d.setColor(Color.GRAY);
		for (int lineX=-100; lineX<=100; lineX += 10) {
			g2d.drawLine(
				round(centre.x + PLOT_SCALE*lineX), -getWidth(),
				round(centre.x + PLOT_SCALE*lineX),  getWidth());
		}
		for (int lineY=-100; lineY<=100; lineY += 10) {
			g2d.drawLine(
				-getWidth(), round(centre.y + PLOT_SCALE*lineY),
				 getWidth(), round(centre.y + PLOT_SCALE*lineY));
		}
		
		// Beamline text label
		g2d.setColor(Color.BLACK);
		g2d.drawString(beamline + " (" + horizontaldirection + ")", 5, 15);
		
		// Origin
		g2d.setColor(Color.CYAN);
		g2d.fillOval(
			centre.x-ORIGIN_DOT_SIZE/2,
			centre.y-ORIGIN_DOT_SIZE/2,
			ORIGIN_DOT_SIZE,
			ORIGIN_DOT_SIZE);
		
		// Actual move position
		g2d.setColor(Color.RED);
		g2d.fillOval(
			centre.x - Math.round((float) x) - MOVE_DOT_SIZE/2,
			centre.y - Math.round((float) y) - MOVE_DOT_SIZE/2,
			MOVE_DOT_SIZE,
			MOVE_DOT_SIZE);
	}
	
	private static int round(double a) {
		return (int) Math.round(a);
	}
	
}
