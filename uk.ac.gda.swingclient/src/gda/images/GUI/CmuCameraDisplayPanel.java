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

package gda.images.GUI;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.FileConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.gui.AcquisitionPanel;
import gda.gui.imaging.RTPCameraClient;
import gda.images.camera.ImageListener;
import gda.images.camera.RTPStreamReceiver;
import gda.images.camera.VideoReceiver;
import uk.ac.diamond.daq.persistence.jythonshelf.LocalParameters;

/**
 *
 */
public class CmuCameraDisplayPanel extends AcquisitionPanel implements ImageListener<Image> {

	private static final Logger logger = LoggerFactory.getLogger(CmuCameraDisplayPanel.class);
	
	private RTPCameraClient samplePanel ;
	private static Dimension ImageSize = new Dimension(800, 600);
	private FileConfiguration config;
	private String xString = "beamcentreX";
	private String yString = "beamCentreY";
	private int x = 100;
	private int y =100;
	/**
	 * 
	 */
	public CmuCameraDisplayPanel() {
		super();
		
	}
	
	private VideoReceiver<Image> videoReceiver;
	
	public void setVideoReceiver(VideoReceiver<Image> videoReceiver) {
		this.videoReceiver = videoReceiver;
	}

	@Override
	public void configure()
	{
		//logger.info("Cmu Camera Panel config called");
		samplePanel= new RTPCameraClient();
		samplePanel.setPreferredSize(ImageSize);
		samplePanel.setMinimumSize(ImageSize);
		samplePanel.setMaximumSize(ImageSize);
		samplePanel.getImageModifier().setDisplayCrossHair(true);
		
		samplePanel.addMouseListener(new MouseListener(){

			@Override
			public void mouseClicked(MouseEvent mevt) {
				//logger.info("mouse clicked at " + mevt.getLocationOnScreen());
				//samplePanel.setCentre(mevt.getLocationOnScreen());
				if(mevt.isControlDown())
				{
					Point p = mevt.getPoint();
					x = p.x;
					y = p.y;
					samplePanel.getImageModifier().setCentre(p);
					save();
				}
				//samplePanel.revalidate();
				
			}

			@Override
			public void mouseEntered(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mouseExited(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mousePressed(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mouseReleased(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
		});
		try {
			config = LocalParameters.getXMLConfiguration();
		} catch (ConfigurationException e) {
			logger.error("unable to configure beam centre values " , e);
			
		} catch (IOException e) {
			logger.error("unable to read beam centre values " , e);
		}
		this.load();
		//samplePanel.setDisplayCrossHair(true);
		//samplePanel.setDisplayBeamSize(true);
		
		//
		samplePanel.getImageModifier().setCentre(new Point(x, y));
		//samplePanel.setBeamSize(new Point(50, 50), new Point(200, 200));
		
		this.setLayout(new BorderLayout());
		this.add(samplePanel, BorderLayout.CENTER);
		this.add(getButtonPanel(), BorderLayout.SOUTH);
		
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				CmuCameraDisplayPanel.this.add(samplePanel);
				//samplePanel.setXScale(0.9);
				//samplePanel.setYScale(1.1);
				//samplePanel.setDisplayScale(true);
				//samplePanel.start();
			}
		});
		
		videoReceiver.addImageListener(this);
	}

	private Component getButtonPanel(){ 
	JPanel buttonPanel = new JPanel();
		JButton startButton = new JButton("Start");
		JButton stopButton = new JButton("Stop");
		startButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				samplePanel.start();

			}

		});
		stopButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				samplePanel.stop();

			}

		});
		buttonPanel.add(startButton);
		buttonPanel.add(stopButton);
		return buttonPanel;
	}
	/**
	 * 
	 */
	public void load() {
		if(config != null)
		{
		this.x =Integer.parseInt((String) config.getProperty(xString));
		this.y =Integer.parseInt((String) config.getProperty(yString));
		}
	}
	/**
	 *  Save the parameters to a file
	 */
	public void save() {
		try {
			
			config.setProperty(xString, x);
			config.setProperty(yString, y);
			config.save();
			
		} catch (ConfigurationException e) {
			logger.error("unable to save beam centre values " , e);
		}
		
	}
	
	@Override
	public void processImage(Image image) {
		if (samplePanel != null) {
			samplePanel.processImage(image);
		}
	}

	/**
	 * @param args
	 */
	public static void main(String args[]) throws Exception {
		JFrame frame  = new JFrame();
		frame.setSize(1024,768);
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		CmuCameraDisplayPanel cmu =  new CmuCameraDisplayPanel();
		
		RTPStreamReceiver receiver = new RTPStreamReceiver();
		receiver.setHost("224.120.120.120");
		receiver.setPort(22224);
		receiver.addImageListener(cmu);
		receiver.configure();
		
		cmu.setVideoReceiver(receiver);
		
		cmu.configure();
		frame.add(cmu);
		frame.setVisible(true);
	}

}
