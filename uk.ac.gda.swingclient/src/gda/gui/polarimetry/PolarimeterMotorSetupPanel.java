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

package gda.gui.polarimetry;

import gda.device.Motor;
import gda.device.MotorException;
import gda.device.MotorStatus;
import gda.factory.Finder;
import gda.gui.AcquisitionPanel;
import gda.observable.IObserver;
import gda.oe.MoveableException;
import gda.oe.OE;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A panel for setting up a Polarimeter motor.
 */
public class PolarimeterMotorSetupPanel extends AcquisitionPanel implements IObserver {
	private static final Logger logger = LoggerFactory.getLogger(PolarimeterMotorSetupPanel.class);

	/**
	 * Inner class Homer is responsible for homing the encoded rotation motors.
	 */
	private class Homer extends JPanel {
		private Motor analyserTiltMotor = null;
		private Motor analyserRotationMotor = null;
		private Motor analyserDetectorMotor = null;
		private Motor retarderTiltMotor = null;
		private Motor retarderRotationMotor = null;
		private JCheckBox chkRetarderTilt;
		private JCheckBox chkRetarderRotation;
		private JCheckBox chkAnalyserTilt;
		private JCheckBox chkAnalyserRotation;
		private JCheckBox chkAnalyserDetector;
		private JButton homeAnalyserTiltButton;
		private JButton homeAnalyserRotationButton;
		private JButton homeAnalyserDetectorButton;
		private JButton homeRetarderTiltButton;
		private JButton homeRetarderRotationButton;

		private Homer(Motor retarderTiltMotor, Motor retarderRotationMotor, Motor analyserTiltMotor,
				Motor analyserRotationMotor, Motor analyserDetectorMotor) {
			super(new BorderLayout());
			this.analyserTiltMotor = analyserTiltMotor;
			this.analyserRotationMotor = analyserRotationMotor;
			this.analyserDetectorMotor = analyserDetectorMotor;
			this.retarderTiltMotor = retarderTiltMotor;
			this.retarderRotationMotor = retarderRotationMotor;

			JPanel leftColumn = new JPanel(new BorderLayout());
			JPanel leftLabels = new JPanel(new GridLayout(6, 0));
			JPanel leftFields = new JPanel(new GridLayout(6, 0));
			JPanel leftButtons = new JPanel(new GridLayout(6, 0));

			leftLabels.add(new JLabel("Retarder Tilt"));
			leftLabels.add(new JLabel("Retarder Rotation"));
			leftLabels.add(new JLabel("Analyser Tilt"));
			leftLabels.add(new JLabel("Analyser Rotation"));
			leftLabels.add(new JLabel("Analyser Detector"));
			chkRetarderTilt = new JCheckBox("Homed");
			chkRetarderTilt.setHorizontalTextPosition(SwingConstants.RIGHT);
			chkRetarderTilt.setEnabled(false);
			chkRetarderRotation = new JCheckBox("Homed");
			chkRetarderRotation.setHorizontalTextPosition(SwingConstants.RIGHT);
			chkRetarderRotation.setEnabled(false);
			chkAnalyserTilt = new JCheckBox("Homed");
			chkAnalyserTilt.setHorizontalTextPosition(SwingConstants.RIGHT);
			chkAnalyserTilt.setEnabled(false);
			chkAnalyserRotation = new JCheckBox("Homed");
			chkAnalyserRotation.setHorizontalTextPosition(SwingConstants.RIGHT);
			chkAnalyserRotation.setEnabled(false);
			chkAnalyserDetector = new JCheckBox("Homed");
			chkAnalyserDetector.setHorizontalTextPosition(SwingConstants.RIGHT);
			chkAnalyserDetector.setEnabled(false);

			JButton updateButton = new JButton("Update");
			updateButton.setToolTipText("Press to update motor home status");
			updateButton.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					updateHomeStatii();
				}
			});
			leftFields.add(chkRetarderTilt);
			leftFields.add(chkRetarderRotation);
			leftFields.add(chkAnalyserTilt);
			leftFields.add(chkAnalyserRotation);
			leftFields.add(chkAnalyserDetector);
			leftFields.add(updateButton);

			homeRetarderTiltButton = new JButton("Home");
			homeRetarderTiltButton.setToolTipText("Home Retarder Tilt Motor");
			homeRetarderTiltButton.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					homeAndZeroMotor(retarderTiltMotorName, retarderTiltDofName, retarder);
				}
			});
			homeRetarderRotationButton = new JButton("Home");
			homeRetarderRotationButton.setToolTipText("Home Retarder Rotation Motor");
			homeRetarderRotationButton.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					homeAndZeroMotor(retarderRotationMotorName, retarderRotationDofName, retarder);
				}
			});
			homeAnalyserTiltButton = new JButton("Home");
			homeAnalyserTiltButton.setToolTipText("Home Analyser Tilt Motor");
			homeAnalyserTiltButton.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					homeAndZeroMotor(analyserTiltMotorName, analyserTiltDofName, analyser);
				}
			});
			homeAnalyserRotationButton = new JButton("Home");
			homeAnalyserRotationButton.setToolTipText("Home Analyser Rotation Motor");
			homeAnalyserRotationButton.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					homeAndZeroMotor(analyserRotationMotorName, analyserRotationDofName, analyser);
				}
			});
			homeAnalyserDetectorButton = new JButton("Home");
			homeAnalyserDetectorButton.setToolTipText("Home Analyser Detector Motor");
			homeAnalyserDetectorButton.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					homeAndZeroMotor(analyserDetectorMotorName, analyserDetectorDofName, analyser);
				}
			});

			leftButtons.add(homeRetarderTiltButton);
			leftButtons.add(homeRetarderRotationButton);
			leftButtons.add(homeAnalyserTiltButton);
			leftButtons.add(homeAnalyserRotationButton);
			leftButtons.add(homeAnalyserDetectorButton);

			leftColumn.add(leftLabels, BorderLayout.WEST);
			leftColumn.add(leftFields, BorderLayout.CENTER);
			leftColumn.add(leftButtons, BorderLayout.EAST);
			add(leftColumn, BorderLayout.NORTH);
			setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Home rotation motors",
					TitledBorder.LEFT, TitledBorder.TOP, null, Color.black));

			updateHomeStatii();
			checkMotorsHomed();
		}

		private void homeAndZeroMotor(String motorToHomeName, String DOFname, OE OEname) {

			Finder finder = Finder.getInstance();
			Motor motor = (Motor) finder.find(motorToHomeName);
			try {
				motor.home();
			} catch (MotorException e2) {
				logger.debug("Exception in homeAndZeroMotor - {}", e2.getMessage());
			}
			updateHomeStatii();

			try {
				OEname.refresh(DOFname);
			} catch (MoveableException e1) {
			}

		}

		/*
		 * Checks that all motors have been homed - i.e. know where they are
		 */
		private void checkMotorsHomed() {
			boolean homed1 = true;
			boolean homed2 = true;
			boolean homed3 = true;
			boolean homed4 = true;
			boolean homed5 = true;
			boolean homed6 = true;
			try {
				if (retarderTiltMotor.isHomeable())
					homed1 = retarderTiltMotor.isHomed();
			} catch (MotorException e) {
				showErrorDialog("Error testing for retarder tilt homed condition\n", "Motor error warning");
			}
			try {
				if (retarderRotationMotor.isHomeable())
					homed2 = retarderTiltMotor.isHomed();
			} catch (MotorException e) {
				showErrorDialog("Error testing for retarder rotation motor homed condition\n", "Motor error warning");
			}
			try {
				if (analyserTiltMotor.isHomeable())
					homed3 = analyserTiltMotor.isHomed();
			} catch (MotorException e) {
				showErrorDialog("Error testing for analyser tilt motor homed condition\n", "Motor error warning");
			}
			try {
				if (analyserRotationMotor.isHomeable())
					homed4 = analyserRotationMotor.isHomed();
			} catch (MotorException e) {
				showErrorDialog("Error testing for analyser rotation motor homed condition\n", "Motor error warning");
			}
			try {
				if (analyserDetectorMotor.isHomeable())
					homed6 = analyserDetectorMotor.isHomed();
			} catch (MotorException e) {
				showErrorDialog("Error testing for analyser detector motor homed condition\n", "Motor error warning");
			}

			if (!homed1 || !homed2 || !homed3 || !homed4 || !homed5 || !homed6) {

				showErrorDialog("One or more polarimeter motors have not been homed \n \n"
						+ " You must home the motors via the Motor Setup panel before using this program.",
						"Motor error warning");
			}
		}

		/**
	       * 
	       */
		private void updateHomeStatii() {
			try {
				chkRetarderTilt.setSelected(retarderTiltMotor.isHomed());
			} catch (MotorException e1) {
				showErrorDialog("Error getting retarder tilt motor home state\n", "Motor error warning");
			}
			try {
				chkRetarderRotation.setSelected(retarderRotationMotor.isHomed());
			} catch (MotorException e2) {
				showErrorDialog("Error getting retarder rotation motor home state\n", "Motor error warning");
			}
			try {
				chkAnalyserTilt.setSelected(analyserTiltMotor.isHomed());
			} catch (MotorException e1) {
				showErrorDialog("Error getting analsyer tilt motor home state\n", "Motor error warning");
			}
			try {
				chkAnalyserRotation.setSelected(analyserRotationMotor.isHomed());
			} catch (MotorException e2) {
				showErrorDialog("Error getting analyser rotation motor home state\n", "Motor error warning");
			}
			try {
				chkAnalyserDetector.setSelected(analyserDetectorMotor.isHomed());
			} catch (MotorException e2) {
				showErrorDialog("Error getting analyser detector motor home state\n", "Motor error warning");
			}
			// homeAnalyserTiltButton.setEnabled(chkAnalyserTilt.isSelected());
			// homeAnalyserRotationButton.setEnabled(chkAnalyserRotation.isSelected());
		}

	} // End of inner class Homer

	/**
	 * Inner class Indexer is responsible for sending the unencoded linear motors to the precision limit and setting
	 * position to zero.
	 */
	private class Indexer extends JPanel {
		private JCheckBox chkFPTrans;
		private JCheckBox chkRPTrans;
		private JCheckBox chkAnalyserTrans;
		private JButton indexFPButton;
		private JButton indexRPButton;
		private JButton indexAnalyserTransButton;

		private Indexer() {
			super(new BorderLayout());

			JPanel leftColumn = new JPanel(new BorderLayout());
			JPanel leftLabels = new JPanel(new GridLayout(3, 0));
			JPanel leftFields = new JPanel(new GridLayout(3, 0));
			JPanel leftButtons = new JPanel(new GridLayout(3, 0));

			leftLabels.add(new JLabel("Front Pinhole"));
			leftLabels.add(new JLabel("Rear Pinhole"));
			leftLabels.add(new JLabel("Analyser Translation"));
			chkFPTrans = new JCheckBox("Zeroed");
			chkFPTrans.setHorizontalTextPosition(SwingConstants.RIGHT);
			chkFPTrans.setSelected(false);
			chkFPTrans.setEnabled(false);
			chkRPTrans = new JCheckBox("Zeroed");
			chkRPTrans.setHorizontalTextPosition(SwingConstants.RIGHT);
			chkRPTrans.setSelected(false);
			chkRPTrans.setEnabled(false);
			chkAnalyserTrans = new JCheckBox("Zeroed");
			chkAnalyserTrans.setHorizontalTextPosition(SwingConstants.RIGHT);
			chkAnalyserTrans.setSelected(false);
			chkAnalyserTrans.setEnabled(false);

			leftFields.add(chkFPTrans);
			leftFields.add(chkRPTrans);
			leftFields.add(chkAnalyserTrans);

			indexFPButton = new JButton("Zero");
			indexFPButton.setToolTipText("Zero front pinhole motor");
			indexFPButton.setEnabled(true); // Temporary fix til I get it working
			indexFPButton.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					indexAndZeroMotor(frontPinholeMotorName);
				}
			});

			indexRPButton = new JButton("Zero");
			indexRPButton.setToolTipText("Zero rear pinhole motor");
			indexRPButton.setEnabled(true); // Temporary fix til I get it working
			indexRPButton.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					indexAndZeroMotor(rearPinholeMotorName);
				}
			});

			indexAnalyserTransButton = new JButton("Zero");
			indexAnalyserTransButton.setToolTipText("Zero Analyser Translation Motor");
			indexAnalyserTransButton.setEnabled(true); // Temporary fix til I get it working
			indexAnalyserTransButton.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					indexAndZeroMotor(analyserTranslationMotorName);
				}
			});

			leftButtons.add(indexFPButton);
			leftButtons.add(indexRPButton);
			leftButtons.add(indexAnalyserTransButton);

			leftColumn.add(leftLabels, BorderLayout.WEST);
			leftColumn.add(leftFields, BorderLayout.CENTER);
			leftColumn.add(leftButtons, BorderLayout.EAST);
			add(leftColumn, BorderLayout.NORTH);
			setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Zero linear motors",
					TitledBorder.LEFT, TitledBorder.TOP, null, Color.black));
		}

		private void indexAndZeroMotor(String motorToHomeName) {

			Finder finder = Finder.getInstance();
			Motor motor = (Motor) finder.find(motorToHomeName);
			int direction = 0; // Always move to lower limit
			MotorStatus ms;
			try {
				motor.stop();
				motor.moveContinuously(direction);
				do {
					Thread.sleep(500);
					ms = motor.getStatus();
				} while (ms.value() == MotorStatus._BUSY);

			} catch (MotorException e2) {
				logger.debug("Exception in homeAndZeroMotor - {}", e2.getMessage());
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			try {
				motor.setPosition(0.0);
				if (motorToHomeName.equals("AnalyserTranslationMotor")) {
					chkAnalyserTrans.setEnabled(true);
					chkAnalyserTrans.setSelected(true);
					chkAnalyserTrans.setEnabled(false);
				} else if (motorToHomeName.equals("FrontPinholeMotor")) {
					chkFPTrans.setEnabled(false);
					chkFPTrans.setSelected(true);
					chkFPTrans.setEnabled(false);
				} else if (motorToHomeName.equals("RearPinholeMotor")) {
					chkRPTrans.setEnabled(true);
					chkRPTrans.setSelected(true);
					chkRPTrans.setEnabled(false);
				}
			} catch (MotorException e2) {
				logger.debug("Exception in homeAndZeroMotor - {}", e2.getMessage());
			}

		}

	} // End of inner class Indexer

	private JFrame frame = new JFrame();

	private OE analyser;
	private OE retarder;

	private String analyserName;
	private String retarderName;
	private String retarderTiltDofName;
	private String retarderRotationDofName;
	private String analyserTiltDofName;
	private String analyserRotationDofName;
	private String analyserTranslationDofName;
	private String analyserDetectorDofName;
	private String analyserTiltMotorName;
	private String analyserRotationMotorName;
	private String analyserTranslationMotorName;
	private String analyserDetectorMotorName;
	private String retarderTiltMotorName;
	private String retarderRotationMotorName;
	private String frontPinholeMotorName;
	private String rearPinholeMotorName;

	private JPanel frontPinholePanel;
	private JPanel rearPinholePanel;
	private JRadioButton[] frontPinholeButtons;
	private JRadioButton[] rearPinholeButtons;

	/**
	    * 
	    */
	public PolarimeterMotorSetupPanel() {
		setLayout(new GridLayout());

		setBorder(BorderFactory.createCompoundBorder(BorderFactory.createRaisedBevelBorder(), BorderFactory
				.createEmptyBorder(3, 3, 3, 3)));
	}

	@Override
	public void configure() {
		Finder finder = Finder.getInstance();
		analyser = (OE) finder.find(analyserName);
		retarder = (OE) finder.find(retarderName);

		JPanel westPanel = new JPanel(new BorderLayout());

		westPanel.add(new Homer((Motor) finder.find(retarderTiltMotorName), (Motor) finder
				.find(retarderRotationMotorName), (Motor) finder.find(analyserTiltMotorName), (Motor) finder
				.find(analyserRotationMotorName), (Motor) finder.find(analyserDetectorMotorName)), BorderLayout.NORTH);

		JPanel eastPanel = new JPanel(new BorderLayout());

		eastPanel.add(new Indexer(),BorderLayout.NORTH);

		add(westPanel);// , GridLayout);
		add(eastPanel);// , BorderLayout.EAST);
		add(createPinholePanels());// , BorderLayout.WEST);

	}

	/**
	 * @return Returns the retarderTiltMotorName.
	 */
	public String getRetarderTiltMotorName() {
		return retarderTiltMotorName;
	}

	/**
	 * @param tiltMotorName
	 *            The tiltMotorName to set.
	 */
	public void setRetarderTiltMotorName(String tiltMotorName) {
		this.retarderTiltMotorName = tiltMotorName;
	}

	/**
	 * @return Returns the analyserRotationMotorName.
	 */
	public String getRetarderRotationMotorName() {
		return retarderRotationMotorName;
	}

	/**
	 * @param rotationMotorName
	 *            The rotationMotorName to set.
	 */
	public void setRetarderRotationMotorName(String rotationMotorName) {
		this.retarderRotationMotorName = rotationMotorName;
	}

	/**
	 * @return Returns the analyserTiltMotorName.
	 */
	public String getAnalyserTiltMotorName() {
		return analyserTiltMotorName;
	}

	/**
	 * @param tiltMotorName
	 *            The tiltMotorName to set.
	 */
	public void setAnalyserTiltMotorName(String tiltMotorName) {
		this.analyserTiltMotorName = tiltMotorName;

	}

	/**
	 * @return Returns the analyserRotationMotorName.
	 */
	public String getAnalyserRotationMotorName() {
		return analyserRotationMotorName;
	}

	/**
	 * @param rotationMotorName
	 *            The rotationMotorName to set.
	 */
	public void setAnalyserRotationMotorName(String rotationMotorName) {
		this.analyserRotationMotorName = rotationMotorName;
	}

	/**
	 * @return Returns the analyserTranslationMotorName.
	 */
	public String getAnalyserTranslationMotorName() {
		return analyserTranslationMotorName;
	}

	/**
	 * @param translationMotorName
	 *            The translationnMotorName to set.
	 */
	public void setAnalyserTranslationMotorName(String translationMotorName) {
		this.analyserTranslationMotorName = translationMotorName;
	}

	/**
	 * @return Returns the frontPinholeMotorName.
	 */
	public String getFrontPinholeMotorName() {
		return frontPinholeMotorName;
	}

	/**
	 * @param fronPinholeMotorName
	 *            The translationnMotorName to set.
	 */
	public void setFrontPinholeMotorName(String fronPinholeMotorName) {
		this.frontPinholeMotorName = fronPinholeMotorName;
	}

	/**
	 * @return Returns the rearPinholeMotorName.
	 */
	public String getRearPinholeMotorName() {
		return rearPinholeMotorName;
	}

	/**
	 * @param rearPinholeMotorName
	 *            The translationnMotorName to set.
	 */
	public void setRearPinholeMotorName(String rearPinholeMotorName) {
		this.rearPinholeMotorName = rearPinholeMotorName;
	}

	/**
	 * @return Returns the analyserDetectorMotorName.
	 */
	public String getAnalyserDetectorMotorName() {
		return analyserDetectorMotorName;
	}

	/**
	 * @param detectorMotorName
	 *            The detectorMotorName to set.
	 */
	public void setAnalyserDetectorMotorName(String detectorMotorName) {
		this.analyserDetectorMotorName = detectorMotorName;
	}

	/**
	 * @return Returns the retarderTiltDofName.
	 */
	public String getRetarderTiltDofName() {
		return retarderTiltDofName;
	}

	/**
	 * @param tiltDofName
	 *            The retarderTiltDofName to set.
	 */
	public void setRetarderTiltDofName(String tiltDofName) {
		this.retarderTiltDofName = tiltDofName;
	}

	/**
	 * @return Returns the RetarderRotationDofName.
	 */
	public String getRetarderRotationDofName() {
		return retarderRotationDofName;
	}

	/**
	 * @param retarderRotationDofName
	 *            The mirrorDofName to set.
	 */
	public void setRetarderRotationDofName(String retarderRotationDofName) {
		this.retarderRotationDofName = retarderRotationDofName;
	}

	/**
	 * @return Returns the AnalyserTiltDofName.
	 */
	public String getAnalyserTiltDofName() {
		return analyserTiltDofName;
	}

	/**
	 * @param analyserTiltDofName
	 *            The analyserTiltDofName to set.
	 */
	public void setAnalyserTiltDofName(String analyserTiltDofName) {
		this.analyserTiltDofName = analyserTiltDofName;
	}

	/**
	 * @return Returns the AnalyserRotationDofName.
	 */
	public String getAnalyserRotationDofName() {
		return analyserRotationDofName;
	}

	/**
	 * @param analyserRotationDofName
	 *            The mirrorDofName to set.
	 */
	public void setAnalyserRotationDofName(String analyserRotationDofName) {
		this.analyserRotationDofName = analyserRotationDofName;
	}

	/**
	 * @return Returns the AnalyserTranslationDofName.
	 */
	public String getAnalyserTranslationDofName() {
		return analyserTranslationDofName;
	}

	/**
	 * @param analyserTranslationDofName
	 *            The analyserTranslationDofName to set.
	 */
	public void setAnalyserTranslationDofName(String analyserTranslationDofName) {
		this.analyserTranslationDofName = analyserTranslationDofName;
	}

	/**
	 * @return Returns the analyserDetectorDofName.
	 */
	public String getAnalyserDetectorDofName() {
		return analyserDetectorDofName;
	}

	/**
	 * @param analyserDetectorDofName
	 *            The analyserDetectorDofName to set.
	 */
	public void setAnalyserDetectorDofName(String analyserDetectorDofName) {
		this.analyserDetectorDofName = analyserDetectorDofName;
	}

	/**
	 * @return Returns the analyserName.
	 */
	public String getAnalyserName() {
		return analyserName;
	}

	/**
	 * @param analyserName
	 *            The analyserName to set.
	 */
	public void setAnalyserName(String analyserName) {
		this.analyserName = analyserName;
	}

	/**
	 * @return Returns the retarderName.
	 */
	public String getRetarderName() {
		return retarderName;
	}

	/**
	 * @param retarderName
	 *            The analyserName to set.
	 */
	public void setRetarderName(String retarderName) {
		this.retarderName = retarderName;
	}

	@Override
	public void update(Object theObserved, Object changeCode) {
		// TODO Auto-generated method stub

	}

	private void showErrorDialog(String errorString, String errorTitle) {
		JOptionPane.showOptionDialog(frame, errorString, errorTitle, JOptionPane.OK_OPTION, JOptionPane.ERROR_MESSAGE,
				null, new String[] { "OK" }, "OK");
	}

	private JPanel createPinholePanels() {

		int numPinholeButtons = 10;
		String[] labels = new String[10];

		labels[0] = "PH0.05";
		labels[1] = "PH0.1";
		labels[2] = "PH0.2";
		labels[3] = "PH0.5";
		labels[4] = "PH3";
		labels[5] = "DET3";
		labels[6] = "DET0.05";
		labels[7] = "DET0.1";
		labels[8] = "DET0.2";
		labels[9] = "DET0.5";

		frontPinholePanel = new JPanel(new GridLayout(30, 1));
		frontPinholePanel.setBorder(BorderFactory.createEtchedBorder());
		JLabel frontPinholePanelLabel = new JLabel("Front Pinholes");
		frontPinholePanel.add(frontPinholePanelLabel);
		ButtonGroup frontPinholeButtonsGroup = new ButtonGroup();
		frontPinholeButtons = new JRadioButton[numPinholeButtons];
		for (int i = 0; i < numPinholeButtons; i++) {
			frontPinholeButtons[i] = new JRadioButton(labels[i]);
			frontPinholeButtons[i].addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent ev) {
					setFrontPinhole();
				}
			});
			frontPinholeButtons[i].setEnabled(false); // TODO temporary fix to disable buttons for now
			frontPinholeButtonsGroup.add(frontPinholeButtons[i]);
			frontPinholePanel.add(frontPinholeButtons[i]);

		}
		// TODO read in selected one from positioner class
		frontPinholeButtons[0].setSelected(true); // TODO read in selected one

		rearPinholePanel = new JPanel(new GridLayout(30, 1));
		rearPinholePanel.setBorder(BorderFactory.createEtchedBorder());
		JLabel rearPinholePanelLabel = new JLabel("Rear Pinholes");
		rearPinholePanel.add(rearPinholePanelLabel);
		ButtonGroup rearPinholeButtonsGroup = new ButtonGroup();
		rearPinholeButtons = new JRadioButton[numPinholeButtons];
		for (int i = 0; i < numPinholeButtons; i++) {
			rearPinholeButtons[i] = new JRadioButton(labels[i]);
			rearPinholeButtons[i].addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent ev) {
					setRearPinhole();
				}
			});
			rearPinholeButtons[i].setEnabled(false); // TODO temporary fix to disable buttons for now
			rearPinholeButtonsGroup.add(rearPinholeButtons[i]);
			rearPinholePanel.add(rearPinholeButtons[i]);

		}
		// TODO read in selected one from positioner class
		rearPinholeButtons[1].setSelected(true); // TODO read in selected one

		// Create panel to return
		JPanel jPanel = new JPanel();
		jPanel.setLayout(new GridLayout(1, 2));
		Border b = BorderFactory.createEtchedBorder();
		jPanel.setBorder(BorderFactory.createTitledBorder(b, "Set Pinhole Positions", TitledBorder.LEFT,
				TitledBorder.CENTER));
		jPanel.add(frontPinholePanel);
		jPanel.add(rearPinholePanel);
		return jPanel;
	}

	private void setFrontPinhole() {

	}

	private void setRearPinhole() {

	}

}
