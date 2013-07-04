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

package gda.gui.imaging;

import gda.images.camera.RTPStreamReceiver;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.junit.Ignore;

/**
 * Tests the {@link RTPCameraClient} class.
 */
@Ignore("Not a JUnit test class")
public class RTPCameraClientTest extends JFrame {
	
	/**
	 * Test harness for the {@link RTPCameraClient} class.
	 * 
	 * @param args command-line arguments
	 */
	public static void main(String[] args) throws Exception {
		RTPCameraClientTest test = new RTPCameraClientTest();
		
		RTPStreamReceiver receiver = new RTPStreamReceiver();
		receiver.setHost("224.120.120.120");
		receiver.setPort(22224);
		receiver.setDesiredFrameRate(30);
		receiver.setName("receiver");
		receiver.addImageListener(test.imagePanel);
		receiver.configure();
		
		test.setVisible(true);
	}
	
	private RTPCameraClient imagePanel;
	
	private JCheckBox checkboxDisplayCrossHair;
	
	private JCheckBox checkboxDisplayBeamSize;
	
	private JCheckBox checkboxDisplayBeamScale;
	
	private JCheckBox checkboxFlipHorizontally;
	
	private JButton startButton;
	
	private JButton stopButton;
	
	private void setStartStopButtonState(boolean running) {
		startButton.setEnabled(!running);
		stopButton.setEnabled(running);
	}
	
	/**
	 * Create a frame for testing the {@link RTPCameraClient}.
	 */
	public RTPCameraClientTest() {
		imagePanel = new RTPCameraClient();
		final Dimension imagePanelSize = new Dimension(1024, 768);
		imagePanel.setPreferredSize(imagePanelSize);
		imagePanel.setMinimumSize(imagePanelSize);
		imagePanel.setMaximumSize(imagePanelSize);
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		add(imagePanel, BorderLayout.CENTER);
		
		// imagePanel.getImageModifier().setDisplayCrossHair(true);
		// imagePanel.getImageModifier().setDisplayBeamSize(true);
		imagePanel.getImageModifier().setXScale(0.9);
		imagePanel.getImageModifier().setYScale(1.1);
		imagePanel.getImageModifier().setDisplayScale(true);
		imagePanel.getImageModifier().setCentre(new Point(100, 100));
		imagePanel.getImageModifier().setFocalSpotSize(new Point(50, 50), new Point(200, 200));
		imagePanel.start();
		
		checkboxDisplayCrossHair = new JCheckBox("Crosshair", false);
		checkboxDisplayBeamSize = new JCheckBox("Beam size", false);
		checkboxDisplayBeamScale = new JCheckBox("Beam scale", true);
		checkboxFlipHorizontally = new JCheckBox("Flip horizontally", false);
		startButton = new JButton("Start");
		stopButton = new JButton("Stop");
		setStartStopButtonState(true);
		
		JPanel controlPanel = new JPanel();
		controlPanel.add(checkboxDisplayCrossHair);
		controlPanel.add(checkboxDisplayBeamSize);
		controlPanel.add(checkboxDisplayBeamScale);
		controlPanel.add(checkboxFlipHorizontally);
		controlPanel.add(startButton);
		controlPanel.add(stopButton);
		add(controlPanel, BorderLayout.SOUTH);
		
		pack();
		
		checkboxDisplayCrossHair.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				imagePanel.getImageModifier().setDisplayCrossHair(checkboxDisplayCrossHair.isSelected());
			}
		});
		
		checkboxDisplayBeamSize.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				imagePanel.getImageModifier().setDisplayFocalSpotSize(checkboxDisplayBeamSize.isSelected());
			}
		});
		
		checkboxDisplayBeamScale.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				imagePanel.getImageModifier().setDisplayScale(checkboxDisplayBeamScale.isSelected());
			}
		});
		
		checkboxFlipHorizontally.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				imagePanel.setFlipImageHorizontally(checkboxFlipHorizontally.isSelected());
			}
		});
		
		startButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				imagePanel.start();
				new Thread(imagePanel).start();
				setStartStopButtonState(true);
			}
		});
		
		stopButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				imagePanel.stop();
				setStartStopButtonState(false);
			}
		});
		
		new Thread(imagePanel).start();
	}
}
