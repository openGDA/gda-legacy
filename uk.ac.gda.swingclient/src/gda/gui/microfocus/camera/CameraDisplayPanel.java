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

package gda.gui.microfocus.camera;

import gda.configuration.properties.LocalProperties;
import gda.factory.Finder;
import gda.gui.AcquisitionPanel;
import gda.gui.imaging.RTPCameraImageROISelector;
import gda.gui.oemove.control.DefaultDOFPositionDisplay;
import gda.images.camera.RTPStreamReceiver;
import gda.images.camera.VideoReceiver;
import gda.jython.JythonServerFacade;
import gda.observable.IObserver;
import gda.oe.OE;
import gda.util.PropertyUtils;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class CameraDisplayPanel extends AcquisitionPanel implements IObserver {

	private static final Logger logger = LoggerFactory.getLogger(CameraDisplayPanel.class);

	private JythonServerFacade jsf ;
	private RTPCameraImageROISelector display;
	private static Dimension ImageSize = new Dimension(1024, 768);
	private int port;
	private String address;
	private JPanel zoomfocusControlPanel;
	private JPanel calibratePanel;
	private JTabbedPane tpane;
	private JTextField mmXDisField;
	private Point[] pixelData;
	private JTextField pixelXField;
	private JTextField pixelYField;
	private JTextField pixelXDisField;
	private JTextField pixelYDisField;
	private JTextField mmYDisField;
	private JTextField mmPerPixelXField;
	private JTextField mmPerPixelYField;
	private JTextField xoffsetField;
	private JTextField yoffsetField;
	private JTextField mmXField;
	private JTextField mmYField;
	private PixelToMMConverter xConverter ;
	private PixelToMMConverter yConverter;
	private String xConverterName;
	private OE cameraControl;
	private String relativeMove = ".r";
	private String snapDirectory = PropertyUtils.getExistingDirFromLocalProperties(LocalProperties.GDA_DATAWRITER_DIR);
	
	@SuppressWarnings("rawtypes")
	private VideoReceiver videoReceiver;
	
	@SuppressWarnings("rawtypes")
	public void setVideoReceiver(VideoReceiver videoReceiver) {
		this.videoReceiver = videoReceiver;
	}
	
	/**
	 * @return xConverterName
	 */
	public String getXConverterName() {
		return xConverterName;
	}

	/**
	 * @param converterName
	 */
	public void setXConverterName(String converterName) {
		xConverterName = converterName;
	}

	/**
	 * @return yConverterName
	 */
	public String getYConverterName() {
		return yConverterName;
	}

	/**
	 * @param converterName
	 */
	public void setYConverterName(String converterName) {
		yConverterName = converterName;
	}

	private String yConverterName;
	private String cameraOEName;
	/**
	 * @return cameraOEName
	 */
	public String getCameraOEName() {
		return cameraOEName;
	}

	/**
	 * @param cameraOEName
	 */
	public void setCameraOEName(String cameraOEName) {
		this.cameraOEName = cameraOEName;
	}

	/**
	 * @return zoomMotorName
	 */
	public String getZoomMotorName() {
		return zoomMotorName;
	}

	/**
	 * @param zoomMotorName
	 */
	public void setZoomMotorName(String zoomMotorName) {
		this.zoomMotorName = zoomMotorName;
	}

	/**
	 * @return focusMotorName
	 */
	public String getFocusMotorName() {
		return focusMotorName;
	}

	/**
	 * @param focusMotorName
	 */
	public void setFocusMotorName(String focusMotorName) {
		this.focusMotorName = focusMotorName;
	}

	private String zoomMotorName;
	private String focusMotorName;
	private JComboBox zoomMoveMode;
	private JTextField zoomField;
	private AbstractButton zoom;
	private JComboBox focusMoveMode;
	private JTextField focusField;
	private JButton focus;

	private JButton snapButton;

	private String videoReceiverName;

	public String getVideoReceiverName() {
		return videoReceiverName;
	}

	public void setVideoReceiverName(String videoReceiverName) {
		this.videoReceiverName = videoReceiverName;
	}

	/**
	 * 
	 */
	public CameraDisplayPanel() {
		super();
		// System.out.println("constructor");

	}

	/**
	 * @see gda.gui.AcquisitionPanel#configure()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void configure() {

		display = new RTPCameraImageROISelector();
		display.setPreferredSize(ImageSize);
		display.setMinimumSize(ImageSize);
		display.setMaximumSize(ImageSize);
		this.setLayout(new BorderLayout());
		this.add(display, BorderLayout.CENTER);
		cameraControl = (OE) Finder.getInstance().find(cameraOEName);
		zoomfocusControlPanel = getZoomFocusControlPanel();
		calibratePanel = getCalibratePanel();
		tpane = new JTabbedPane();
		tpane.add("Adjust Zoom/Focus", zoomfocusControlPanel);
		tpane.add("Calibrate X/Y", calibratePanel);
		JPanel p = new JPanel();
		p.add(tpane);
		// p.setPreferredSize(preferredSize)
		// p.add(this.getScanPointsPanel());
		this.add(p, BorderLayout.SOUTH);
		
		videoReceiver = (VideoReceiver<RTPStreamReceiver>)Finder.getInstance().find(videoReceiverName);
		if(videoReceiver == null)
		{
			RTPStreamReceiver r = new RTPStreamReceiver();
			r.setHost(address);
			r.setPort(port);
			videoReceiver = r;	
		}
		videoReceiver.addImageListener(display);
		
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				CameraDisplayPanel.this.add(display);
				Dimension d = display.getImageSize();
				display.setDisplayCrosshairAtImageCentre(true);
				display.setCrossHairColour(Color.gray);
				display.setCrosshairLength(25);
				display.setCentre(new Point(d.width/2, d.height/2));
				display.addIObserver(CameraDisplayPanel.this);
				//display.start();
			

			}

		});
		xConverter = (PixelToMMConverter)Finder.getInstance().findLocal(xConverterName);
		yConverter = (PixelToMMConverter)Finder.getInstance().findLocal(yConverterName);
		jsf = JythonServerFacade.getInstance();
	
	}

	private JPanel getZoomFocusControlPanel() {
		JLabel zoomLabel = new JLabel("Zoom");
		 zoomField = new DoubleTextField(0,5);
		DefaultDOFPositionDisplay zoomCurrentField = new DefaultDOFPositionDisplay(cameraControl, getZoomMotorName(), 5, false);
		zoomMoveMode = new JComboBox();
		zoomMoveMode.addItem("To");
		zoomMoveMode.addItem("By");
		zoom = new JButton("Move");
		zoom.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				zoom.setEnabled(false);
				moveZoom();
			}
		});
		JLabel focusLabel = new JLabel("Focus");
		focusField = new DoubleTextField(0,5);
		DefaultDOFPositionDisplay focusCurrentField = new DefaultDOFPositionDisplay(cameraControl, getFocusMotorName(), 5, false);
		focusMoveMode = new JComboBox();
		focusMoveMode.addItem("To");
		focusMoveMode.addItem("By");
		focus = new JButton("Move");
		focus.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				zoom.setEnabled(false);
				moveFocus();
			}
		});
		JButton startButton = new JButton("Start");
		JButton stopButton = new JButton("Stop");
		startButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				display.start();
				//display.setDisplayCrosshairAtImageCentre(true);
			}

		});
		stopButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				display.stop();

			}

		});
		
		snapButton = new JButton("SnapShot");
		snapButton.addActionListener(new ActionListener(){

			

			@Override
			public void actionPerformed(ActionEvent arg0) {
				 JFileChooser chooser = new JFileChooser(snapDirectory);
				    FileNameExtensionFilter filter = new FileNameExtensionFilter(
				        "JPG & GIF Images", "jpg", "gif");
				    chooser.setFileFilter(filter);
				    int returnVal = chooser.showSaveDialog(CameraDisplayPanel.this);
				    if(returnVal == JFileChooser.APPROVE_OPTION) {
				       logger.info("You chose to save this file as: " +
				            chooser.getSelectedFile().getName());
				       display.saveImage(chooser.getSelectedFile(), filter.getExtensions()[0]);
				    }
				 
				
			}
			
		});
		JPanel cPanel = new JPanel();
		cPanel.setLayout(new GridBagLayout());
		GridBagConstraints gc = new GridBagConstraints();
		// gc.gridx = GridBagConstraints.RELATIVE;
		// gc.gridy = GridBagConstraints.RELATIVE;
		gc.fill = GridBagConstraints.BOTH;
		// gc.weightx =1.0;
		// gc.weighty =1.0;
		gc.anchor = GridBagConstraints.EAST;
		gc.gridheight = 1;
		gc.gridwidth = 1;
		cPanel.add(zoomLabel, gc);
		cPanel.add(zoomField, gc);
		cPanel.add(zoomCurrentField, gc);
		cPanel.add(zoomMoveMode, gc);
		gc.gridwidth = GridBagConstraints.REMAINDER;
		cPanel.add(zoom, gc);
		gc.gridwidth = 1;
		cPanel.add(focusLabel, gc);
		cPanel.add(focusField, gc);
		cPanel.add(focusCurrentField, gc);
		cPanel.add(focusMoveMode, gc);
		gc.gridwidth = GridBagConstraints.REMAINDER;
		cPanel.add(focus, gc);
		gc.gridwidth = 2;
		cPanel.add(startButton, gc);
		cPanel.add(stopButton,gc);
		gc.gridwidth = GridBagConstraints.REMAINDER;
		cPanel.add(snapButton,gc);
		return cPanel;
	}

	protected void moveFocus() {
		try {
			String command = "";
			
			if (focusMoveMode.getSelectedItem().equals("To"))
				command = getFocusMotorName() + ".move" + focusMoveMode.getSelectedItem() + "(" + focusField.getText()
						+ ")";
			else
				command = getFocusMotorName() + relativeMove + "(" + focusField.getText() + ")";
			System.out.println("the move command is  " + command);
			
			jsf.runCommand(command);
		} catch (Exception e) {
			logger.error("Error moving X:" , e);
		}
		finally{
			focus.setEnabled(true);
		}
		
	}

	protected void moveZoom() {
		try {
			String command = "";

			if (zoomMoveMode.getSelectedItem().equals("To"))
				command = getZoomMotorName() + ".move" + zoomMoveMode.getSelectedItem() + "(" + zoomField.getText()
						+ ")";
			else
				command = getZoomMotorName() + relativeMove + "(" + zoomField.getText() + ")";
			System.out.println("the move command is  " + command);
			jsf.runCommand(command);
		} catch (Exception e) {
			logger.error("Error moving X:" , e);
		}
		finally{
			zoom.setEnabled(true);
		}
	}

	private JPanel getCalibratePanel() {
		JLabel mmX = new JLabel("x mm");
		JLabel mmY = new JLabel("y mm");
		mmXField = new DoubleTextField(0,5);
		mmYField = new DoubleTextField(0,5);
		JLabel pixelX = new JLabel("x pixel");
		JLabel pixelY = new JLabel("y pixel");
		pixelXField = new JTextField(5);
		pixelXField.setEditable(false);
		pixelYField = new JTextField(5);
		pixelYField.setEditable(false);
		JLabel pixelXDistance = new JLabel("x Distance pixels");
		pixelXDisField = new JTextField(5);
		pixelXDisField.setEditable(false);
		JLabel pixelYDistance = new JLabel("y Distance pixels");
		pixelYDisField = new JTextField(5);
		pixelYDisField.setEditable(false);
		JLabel mmXLabel = new JLabel("X Distance mm");
		mmXDisField =new DoubleTextField(0,5);
		JLabel mmYLabel = new JLabel("Y Distance mm");
		mmYDisField = new DoubleTextField(0,5);
		JButton calibrateX = new JButton("Calibrate X");
		calibrateX.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (mmXDisField.getText().length() != 0 && mmXField.getText().length() != 0) {
					mmPerPixelXField
							.setText(String.valueOf(calculateMMperPixel(Double
									.parseDouble(pixelXDisField.getText()),
									Double.parseDouble(mmXDisField.getText()))));
					xoffsetField.setText(String.valueOf(calculateOffset(Double
							.parseDouble(pixelXField.getText()), Double
							.parseDouble(mmXField.getText()))));
				}

			}

		});
		JButton calibrateY = new JButton("Calibrate Y");
		calibrateY.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(mmYDisField.getText().length() != 0 && mmYField.getText().length() != 0)
				{
				mmPerPixelYField.setText(String.valueOf(calculateMMperPixel(
						Double.parseDouble(pixelYDisField.getText()), Double
								.parseDouble(mmYDisField.getText()))));
				yoffsetField.setText(String.valueOf(calculateOffset(Double
						.parseDouble(pixelYField.getText()), Double
						.parseDouble(mmYField.getText()))));
				}

			}

		});
		JLabel mmPerPixelXLabel = new JLabel("X mm per Pixel");
		mmPerPixelXField = new JTextField(5);
		mmPerPixelXField.setEditable(false);
		JLabel mmPerPixelYLabel = new JLabel("Y mm per Pixel");
		mmPerPixelYField = new JTextField(5);
		mmPerPixelYField.setEditable(false);
		JLabel xoffsetLabel = new JLabel("X Offset");
		xoffsetField = new JTextField(5);
		xoffsetField.setEditable(false);
		JLabel yoffsetLabel = new JLabel("Y Offset");
		yoffsetField = new JTextField(5);
		yoffsetField.setEditable(false);
		JButton saveButton = new JButton("Save");
		saveButton.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				writeToFile();
			}
			
		});
		JButton reloadButton = new JButton("Reload");
		reloadButton.setEnabled(false);
		reloadButton.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				loadFromFile();
			}
			
		});
		
		
		JPanel calibPanel = new JPanel();
		calibPanel.setLayout(new GridBagLayout());
		GridBagConstraints gc = new GridBagConstraints();
		// gc.gridx = GridBagConstraints.RELATIVE;
		// gc.gridy = GridBagConstraints.RELATIVE;
		gc.fill = GridBagConstraints.BOTH;
		gc.gridheight = 1;
		gc.gridwidth = 1;
		calibPanel.add(mmX, gc);
		calibPanel.add(mmXField, gc);
		gc.gridwidth = 1;
		calibPanel.add(pixelX, gc);
		gc.gridwidth = GridBagConstraints.REMAINDER;
		calibPanel.add(pixelXField, gc);
		gc.gridwidth = 1;
		calibPanel.add(mmXLabel, gc);
		calibPanel.add(mmXDisField, gc);
		calibPanel.add(pixelXDistance, gc);
		gc.gridwidth = GridBagConstraints.REMAINDER;
		calibPanel.add(pixelXDisField, gc);
		gc.gridwidth = 1;
		calibPanel.add(mmPerPixelXLabel, gc);
		calibPanel.add(mmPerPixelXField, gc);
		gc.gridwidth = 1;
		calibPanel.add(xoffsetLabel, gc);
		calibPanel.add(xoffsetField, gc);
		gc.gridwidth = GridBagConstraints.REMAINDER;
		calibPanel.add(calibrateX, gc);
		gc.gridwidth = 1;
		calibPanel.add(mmY, gc);
		calibPanel.add(mmYField, gc);
		gc.gridwidth = 1;
		calibPanel.add(pixelY, gc);
		gc.gridwidth = GridBagConstraints.REMAINDER;
		calibPanel.add(pixelYField, gc);
		gc.gridwidth = 1;
		calibPanel.add(mmYLabel, gc);
		calibPanel.add(mmYDisField, gc);
		calibPanel.add(pixelYDistance, gc);
		gc.gridwidth = GridBagConstraints.REMAINDER;
		calibPanel.add(pixelYDisField, gc);
		gc.gridwidth = 1;
		calibPanel.add(mmPerPixelYLabel, gc);
		calibPanel.add(mmPerPixelYField, gc);
		gc.gridwidth = 1;
		calibPanel.add(yoffsetLabel, gc);
		calibPanel.add(yoffsetField, gc);
		gc.gridwidth = GridBagConstraints.REMAINDER;
		calibPanel.add(calibrateY, gc);
		gc.gridwidth = 2;
		calibPanel.add(saveButton, gc);
		gc.gridwidth = GridBagConstraints.REMAINDER;
		calibPanel.add(reloadButton, gc);
		
		return calibPanel;
	}

	protected void writeToFile() {
		xConverter.setMmOffset(Double.parseDouble(mmXField.getText()));
		xConverter.setMmPerPixel(Double.parseDouble(mmPerPixelXField.getText()));
		xConverter.setPixelReference(Integer.parseInt(pixelXField.getText()));
		yConverter.setMmOffset(Double.parseDouble(mmYField.getText()));
		yConverter.setMmPerPixel(Double.parseDouble(mmPerPixelYField.getText()));
		yConverter.setPixelReference(Integer.parseInt(pixelYField.getText()));
		xConverter.save();
		yConverter.save();
		
	}
	private void loadFromFile(){
		xConverter.reload();
		yConverter.reload();
	}

	/**
	 * @return int
	 */
	public int getPort() {
		return port;
	}

	/**
	 * @param port
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * @return String
	 */
	public String getAddress() {
		return address;
	}

	/**
	 * @param address
	 */
	public void setAddress(String address) {
		this.address = address;
	}

	/**
	 * @see gda.observable.IObserver#update(java.lang.Object, java.lang.Object)
	 */
	@Override
	public void update(Object theObserved, Object changeCode) {
		if (changeCode instanceof Point[]) {
			pixelData = (Point[]) changeCode;
			if (pixelData != null) {
				if (pixelData[0].y == pixelData[1].y) {
					// line selected is a horizontal line
					pixelXField.setText(String.valueOf(pixelData[0].x));
					pixelXDisField.setText(String.valueOf(Math
							.abs((pixelData[1].x - pixelData[0].x))));
					if (mmXDisField.getText().length() != 0 && mmXField.getText().length() != 0) {
						logger.info("mmx distance is  "+mmXDisField.getText() );
					mmPerPixelXField
							.setText(String.valueOf(calculateMMperPixel(Math
									.abs(pixelData[1].x - pixelData[0].x),
									Double.parseDouble(mmXDisField.getText()))));
					xoffsetField.setText(String.valueOf(calculateOffset(
							pixelData[0].x, Double.parseDouble(mmXField
									.getText()))));
					}
				}

				else if (pixelData[0].x == pixelData[1].x) {
					// line drawn is avertical line
					pixelYField.setText(String.valueOf(pixelData[0].y));
					pixelYDisField.setText(String.valueOf(Math
							.abs((pixelData[1].y - pixelData[0].y))));
					if (mmYDisField.getText().length() != 0 && mmYField.getText().length() != 0) {
					mmPerPixelYField
							.setText(String.valueOf(calculateMMperPixel(Math
									.abs(pixelData[1].y - pixelData[0].y),
									Double.parseDouble(mmYDisField.getText()))));
					yoffsetField.setText(String.valueOf(calculateOffset(
							pixelData[0].y, Double.parseDouble(mmYField
									.getText()))));
					}
				}
				{
					for(int i = 0; i< pixelData.length;i++)
					{
						logger.info("The x conversion is " + xConverter.getConversion(pixelData[i].x));
						logger.info("The y conversion is " + yConverter.getConversion(pixelData[i].y));
					
					}
					xConverter.notifyListeners(pixelData[0].x, pixelData[1].x);
					yConverter.notifyListeners(pixelData[0].y, pixelData[1].y);
				}

				logger.info("point 1  = (" + pixelData[0].x + ","
						+ pixelData[0].y + ")");
				logger.info("point 2 (" + pixelData[1].x + "," + pixelData[1].y
						+ ")");

				logger.info("x pixel distance ="
						+ Math.abs((pixelData[1].x - pixelData[0].x)));
			}
		}
	}

	private double calculateOffset(@SuppressWarnings("unused") double xpixel, double xreal) {
		return xreal ;

	}

	private double calculateMMperPixel(double pixeldistance, double realdistance) {
		return realdistance / pixeldistance;
	}
	
	
	/**
	 * @param args
	 */
	public static void main(String args[]) {
		JFrame f = new JFrame("Camera");
		CameraDisplayPanel cp = new CameraDisplayPanel();
		cp.setPort(22224);
		cp.setAddress("224.120.120.120");
		cp.configure();
		f.add(cp);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setSize(1200, 700);
		f.setVisible(true);
	}
	
	class DoubleTextField extends JTextField {
		public DoubleTextField(int defval, int size) {
			super(String.valueOf(defval), size);
		}

		@Override
		protected Document createDefaultModel() {
			return new DoubleTextDocument();
		}

		@Override
		public boolean isValid() {
			try {
				if(isShowing())
					Double.parseDouble(this.getText());
				return true;
			} catch (NumberFormatException e) {
				return false;
			}
		}

		public double getValue() {
			try {
				return Double.parseDouble(getText());
			} catch (NumberFormatException e) {
				return 0;
			}
		}
	}
		class DoubleTextDocument extends PlainDocument {
			@Override
			public void insertString(int offs, String str, AttributeSet a)
			throws BadLocationException {
				if (str == null)
					return;
				String oldString = getText(0, getLength());
				String newString = oldString.substring(0, offs) + str
				+ oldString.substring(offs);
				try {
					Double.parseDouble(newString + "0");
					super.insertString(offs, str, a);
				} catch (NumberFormatException e) {
				}
			}
		}


}
