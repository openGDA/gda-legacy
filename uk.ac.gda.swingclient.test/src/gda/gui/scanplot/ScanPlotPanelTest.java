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

package gda.gui.scanplot;

import gda.jython.IScanDataPointObserver;
import gda.jython.IScanDataPointProvider;
import gda.jython.InterfaceProvider;
import gda.observable.ObservableComponent;
import gda.scan.IScanDataPoint;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import org.junit.Ignore;

/**
 * Panel used for testing.
 * The test involves simulating a scan and therefore sending ScanDataPoints to the updatePlot method
 * The scans are either nested or simple.
 * The number of points in a scan is determined by the xLoops vaiable below.
 */
@Ignore("Not a JUnit test class")
public class ScanPlotPanelTest extends JPanel implements ActionListener, IScanDataPointProvider, ScanDataPointHandler {
	ScanPlotPanel scanPlot;
	JTextArea textArea;
	final static String newline = "\n";
	JButton addDataButton, stopAddingDataButton;
	ScanDataPointHandlerTester addDataQueue;

	ScanPlotPanelTest() {
		super(new BorderLayout());
		InterfaceProvider.setScanDataPointProviderForTesting(this);

		scanPlot = new ScanPlotPanel();
		scanPlot.setNumberInitialGraphs(1);
		scanPlot.configure();
		add(scanPlot,BorderLayout.CENTER);

		JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		addDataButton = new JButton("Start Adding Data");
		addDataButton.addActionListener(this);
		stopAddingDataButton = new JButton("Stop Adding Data");
		stopAddingDataButton.addActionListener(this);
		btnPanel.add(addDataButton);
		btnPanel.add(stopAddingDataButton);
		add(btnPanel,BorderLayout.SOUTH);

		setBorder(BorderFactory.createBevelBorder(1));
		addDataQueue = new ScanDataPointHandlerTester(this);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == addDataButton) {
			addDataQueue.startAddingData("1");
			return;
		}
		if (e.getSource() == stopAddingDataButton) {
			addDataQueue.stopAddingData();
			return;
		}
	}
	
	/**
	 * Create the GUI and show it. For thread safety, this method should be
	 * invoked from the event-dispatching thread.
	 */
	private static void createAndShowGUI() {
		// Create and set up the window.
		JFrame frame = new JFrame("ScanPlotDemo");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JComponent newContentPane = new ScanPlotPanelTest();
		newContentPane.setOpaque(true); // content panes must be opaque
		frame.setContentPane(newContentPane);
		frame.pack();
		frame.setVisible(true);

	}

	/**
	 * test application
	 * @param args
	 */
	public static void main(String[] args) {
		// Schedule a job for the event-dispatching thread:
		// creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				createAndShowGUI();
			}
		});
	}

	private ObservableComponent comp = new ObservableComponent();
	private IScanDataPoint lastScanDataPoint;
	@Override
	public void addIScanDataPointObserver(IScanDataPointObserver anObserver) {
		comp.addIObserver(anObserver);
	}

	@Override
	public void deleteIScanDataPointObserver(IScanDataPointObserver anObserver) {
		comp.deleteIObserver(anObserver);
	}

	@Override
	public void update(Object dataSource, Object data) {
		if( data instanceof IScanDataPoint)
			lastScanDataPoint = (IScanDataPoint)data;
		comp.notifyIObservers(dataSource, data);
	}

	@Override
	public IScanDataPoint getLastScanDataPoint() {
		return lastScanDataPoint;
	}

	@Override
	public void handlePoint(IScanDataPoint point) {
		update(this, point);
	}	
}


