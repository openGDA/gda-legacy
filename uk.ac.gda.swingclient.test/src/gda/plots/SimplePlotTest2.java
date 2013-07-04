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

package gda.plots;

import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JButton;
import javax.swing.JFrame;

/**
 * SimplePlotFrameTest Class
 */
public class SimplePlotTest2 extends JFrame implements ActionListener, WindowListener {
	private final JButton addButton;

	private final JButton clearButton, TurboModeOn, TurboModeOff, addDataButton, stopAddingDataButton;
	private static AddDataQueue addDataQueue;
	private SimplePlotFrame simplePlotFrame;
	private int axis=0;
	private int which=0;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SimplePlotFrame simplePlotFrame = new SimplePlotFrame(SimplePlot.LINECHART, true);
		simplePlotFrame.pack();
		simplePlotFrame.setVisible(true);
		SimplePlotTest2 simplePlotTest2 = new SimplePlotTest2(simplePlotFrame);
		simplePlotTest2.pack();
		simplePlotTest2.setVisible(true);
		simplePlotFrame.simplePlot.setTurboMode(true);
	}

	SimplePlotTest2(SimplePlotFrame simplePlotFrame) {
		super();
		this.simplePlotFrame = simplePlotFrame;
		setLayout(new GridBagLayout());
		add(addButton = new JButton("Add data"));
		addButton.addActionListener(this);
		add(clearButton = new JButton("Clear plot"));
		clearButton.addActionListener(this);
		add(addDataButton = new JButton("Start Adding Data"));
		addDataButton.addActionListener(this);
		add(stopAddingDataButton = new JButton("Stop Adding Data"));
		stopAddingDataButton.addActionListener(this);
		add(TurboModeOn = new JButton("TurboModeOn"));
		TurboModeOn.addActionListener(this);
		add(TurboModeOff = new JButton("TurboModeOff"));
		TurboModeOff.addActionListener(this);
		addWindowListener(this);
		SimplePlot simplePlot = simplePlotFrame.simplePlot;
		addDataQueue = new AddDataQueue(simplePlot);
		
		simplePlot.setTurboMode(true);
		simplePlot.setTitle("Test");
		simplePlot.setXAxisLabel("X-Axis");
		simplePlot.setYAxisLabel("Y-Axis");
		simplePlot.addYAxisTwo(true);
		simplePlot.setYAxisTwoLabel("Y2-Axis");
		simplePlot.setXValueTransformer(new LinearValueTransformer(2.0, -10.0));
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		SimplePlot simplePlot = simplePlotFrame.simplePlot;
		if (e.getSource() == addButton) {
			
			int numOfPoints=20;
			int iLineNumber = simplePlot.getNextAvailableLine();
			double xVals[] = new double[numOfPoints];
			double yVals[] = new double[numOfPoints];
			for (int i = 0; i < numOfPoints; i++) {
				xVals[i]=i + iLineNumber*10;
				yVals[i]= Math.sin(Math.PI * i / 180);
			}
			SimpleXYSeries xySeries = new SimpleXYSeries("Test",which,axis, xVals, yVals);
			xySeries.setType(gda.plots.Type.LINEANDPOINTS);
			xySeries.setMarker(Marker.fromCounter(iLineNumber));
			simplePlot.initializeLine(xySeries);
			which +=2;
			axis = axis==0 ? 1 : 0;
			return;
		}
		if (e.getSource() == addDataButton) {
			addDataQueue.startAddingData();
			return;
		}
		if (e.getSource() == stopAddingDataButton) {
			addDataQueue.stopAddingData();
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
		if (e.getSource() == TurboModeOff) {
			simplePlot.setTurboMode(false);
			return;
		}
		if (e.getSource() == TurboModeOn) {
			simplePlot.setTurboMode(true);
			return;
		}

	}

	@Override
	public void windowClosed(WindowEvent e) {
	}

	@Override
	public void windowActivated(WindowEvent e) {
	}

	@Override
	public void windowClosing(WindowEvent e) {
		System.exit(0);
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
	}

	@Override
	public void windowIconified(WindowEvent e) {
	}

	@Override
	public void windowOpened(WindowEvent e) {
	}

}
class AddDataQueue implements Runnable {
	SimplePlot simplePlot;
	double x=0;
	AddDataQueue(SimplePlot simplePlot){
		this.simplePlot = simplePlot;
	}
	private boolean killed = false;
	private Thread thread = null;
	private Boolean addData=false;
	private Object obj = new Object();

	public void startAddingData(){
		synchronized (obj) {
			addData = true;
			if (thread == null) {
				thread = uk.ac.gda.util.ThreadManager.getThread(this);
				thread.start();
			}			
			obj.notifyAll();
		}
	}
	public void stopAddingData(){
		synchronized (obj) {
			addData = false;
			obj.notifyAll();
		}
	}

	/**
	 * 
	 */
	public void dispose() {
		killed = true;
	}

	@Override
	public void run() {
		while (!killed) {
			try {
				boolean addDataNow=false;
				synchronized (obj) {
					addDataNow = addData;
					if(!addDataNow){
						obj.wait();
						x=0.;
					}
				}
				if (addDataNow) {
					if(simplePlot.leftSeriesCollection != null && simplePlot.leftSeriesCollection.getSeriesCount() >0){
						int lineNumber = ((SimpleXYSeries)simplePlot.leftSeriesCollection.getSeries(simplePlot.leftSeriesCollection.getSeriesCount()-1)).getLineNumber();
						simplePlot.addPointToLine(lineNumber, x + (lineNumber+1)*20., Math.sin(Math.PI * x / 180), false);
					}
					if(simplePlot.rightSeriesCollection != null && simplePlot.rightSeriesCollection.getSeriesCount() >0){
						int lineNumber = ((SimpleXYSeries)simplePlot.rightSeriesCollection.getSeries(simplePlot.rightSeriesCollection.getSeriesCount()-1)).getLineNumber();
						simplePlot.addPointToLine(lineNumber, x + (lineNumber+1)*50., Math.sin(Math.PI * x / 180), false);
					}
					Thread.sleep(500);
					x+=1.0;
				}
			} catch (Throwable th) {
				th.printStackTrace();
			}
		}
	}

}
