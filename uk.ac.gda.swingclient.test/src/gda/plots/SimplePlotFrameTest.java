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

package gda.plots;

import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JButton;
import javax.swing.JFrame;

import org.junit.Ignore;
/**
 * SimplePlotFrameTest Class
 */
@Ignore("Not a JUnit test class")
public class SimplePlotFrameTest extends JFrame implements ActionListener, WindowListener {
	private final JButton addButton;

	private final JButton clearButton;

	private SimplePlotFrame simplePlotFrame;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SimplePlotFrame simplePlotFrame = new SimplePlotFrame();
		simplePlotFrame.pack();
		simplePlotFrame.setVisible(true);
		SimplePlotFrameTest simplePlotFrameTest = new SimplePlotFrameTest(simplePlotFrame);
		simplePlotFrameTest.pack();
		simplePlotFrameTest.setVisible(true);
	}

	SimplePlotFrameTest(SimplePlotFrame simplePlotFrame) {
		super();
		this.simplePlotFrame = simplePlotFrame;
		setLayout(new GridBagLayout());
		add(addButton = new JButton("Add data"));
		addButton.addActionListener(this);
		add(clearButton = new JButton("Clear plot"));
		clearButton.addActionListener(this);
		addWindowListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		SimplePlot simplePlot = simplePlotFrame.simplePlot;
		if (e.getSource() == addButton) {
			simplePlot.setTitle("Test");
			simplePlot.setXAxisLabel("X-Axis");
			simplePlot.setYAxisLabel("Y-Axis");
			int iPlot = 0;
			while (iPlot < 50) {
				iPlot = simplePlot.getNextAvailableLine();
				simplePlot.initializeLine(iPlot);
				simplePlot.setLineMarker(iPlot, Marker.fromCounter(iPlot));
				// simplePlot.setLineMarkerSize(iPlot, 20);
				// simplePlot.setLineMarkerColor(iPlot, Color.BLACK);
				simplePlot.setLineType(iPlot, gda.plots.Type.LINEANDPOINTS);

				simplePlot.setLineName(iPlot, "Plot " + Integer.toString(iPlot));
				for (int i = 0; i < 36.; i++) {
					double x = i;
					simplePlot.addPointToLine(iPlot, x + iPlot * 10, Math.sin(Math.PI * x / 180));
				}
			}
			return;
		}
		if (e.getSource() == clearButton) {
			simplePlot.setTitle("");
			simplePlot.setXAxisLabel("");
			simplePlot.setYAxisLabel("");
			int NumberOfLines = simplePlot.getNextAvailableLine();
			for (int i = 0; i < NumberOfLines; i++)
				simplePlot.deleteLine(i);
			return;
		}

	}

	@Override
	public void windowClosed(WindowEvent e) {
		// TODO Auto-generated method stub
	}

	@Override
	public void windowActivated(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowClosing(WindowEvent e) {
		// TODO Auto-generated method stub
		System.exit(0);

	}

	@Override
	public void windowDeactivated(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowDeiconified(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowIconified(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowOpened(WindowEvent e) {
		// TODO Auto-generated method stub

	}

}
