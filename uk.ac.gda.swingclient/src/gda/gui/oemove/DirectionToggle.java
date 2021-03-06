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

package gda.gui.oemove;

import gda.configuration.properties.LocalProperties;
import gda.gui.oemove.control.DOFMode;
import gda.oe.MoveableException;
import gda.oe.OE;
import gda.oe.dofs.DOF;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.GrayFilter;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

import org.jscience.physics.quantities.Quantity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class acts a container for two DirectionButtons. Each button responsible moving continuously in either a
 * positive or negative direction. This class is also handling mouse events generated by either of it's two buttons.
 * 
 * @see DirectionButton
 */
public class DirectionToggle extends JPanel {
	private static final Logger logger = LoggerFactory.getLogger(DirectionToggle.class);

	private ImageIcon plusImage = new ImageIcon(getClass().getResource("OEImages/plus.gif"));

	private ImageIcon minusImage = new ImageIcon(getClass().getResource("OEImages/minus.gif"));

	private ImageIcon selectedPlusImage = new ImageIcon(getClass().getResource("OEImages/s_plus.gif"));

	private ImageIcon selectedMinusImage = new ImageIcon(getClass().getResource("OEImages/s_minus.gif"));

	private DirectionButton positiveButton;

	private DirectionButton negativeButton;

	private OE oe;

	private boolean selected = false;

	private DOFControlPanel dofControlPanel;

	private String dofSelected;

	private int direction;

	/**
	 */
	public DirectionToggle() {
		positiveButton = new DirectionButton(DOF.POSITIVE);
		positiveButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				doMousePressed((DirectionButton) e.getSource());
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				doMouseReleased((DirectionButton) e.getSource());
			}
		});
		positiveButton.setIcon(plusImage);
		positiveButton.setDisabledIcon(new ImageIcon(GrayFilter.createDisabledImage(plusImage.getImage())));

		negativeButton = new DirectionButton(DOF.NEGATIVE);
		negativeButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				doMousePressed((DirectionButton) e.getSource());
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				doMouseReleased((DirectionButton) e.getSource());
			}
		});
		negativeButton.setIcon(minusImage);
		negativeButton.setDisabledIcon(new ImageIcon(GrayFilter.createDisabledImage(minusImage.getImage())));

		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(2, 1));
		panel.setBorder(new BevelBorder(BevelBorder.RAISED));
		panel.add(positiveButton);
		panel.add(negativeButton);
		setLayout(new FlowLayout());
		add(panel);

		setVisible(true);
		setEnabled(false);
	}

	private void generateMouseReleased(DirectionButton directionButton) {
		// This is not ideal! However it was the only way I could get the
		// DirectionButton to de-select when operation is interrupted
		// by a dialog i.e. at the very least its background remain in the
		// pressed state. SHK.
		directionButton.dispatchEvent(new MouseEvent(directionButton, MouseEvent.MOUSE_RELEASED, 0, 0, 0, 0, 0, false));
	}

	/**
	 * Set the button to selected and start the continuous drive.
	 * 
	 * @param directionButton
	 */
	public void doMousePressed(DirectionButton directionButton) {
		if (selected) {
			direction = directionButton.getDirection();

			if (direction == DOF.POSITIVE) {
				directionButton.setIcon(selectedPlusImage);
			} else {
				directionButton.setIcon(selectedMinusImage);
			}
			directionButton.setSelected(true);
			directionButton.revalidate();
			directionButton.repaint();
			if (!startContinuousDrive()) {
				generateMouseReleased(directionButton);
			}
		}
	}

	/**
	 * This method returns the button to its default look and stops the move continuously operation.
	 * 
	 * @param directionButton
	 */
	public void doMouseReleased(DirectionButton directionButton) {
		if (selected) {
			direction = directionButton.getDirection();

			if (direction == DOF.POSITIVE) {
				directionButton.setIcon(plusImage);
			} else {
				directionButton.setIcon(minusImage);
			}
			directionButton.setSelected(true);
			directionButton.revalidate();
			directionButton.repaint();
			stopContinuousDrive();
		}
	}

	/**
	 * This enables the DirectionButtons when the mode of movement selected from mode ComboBox is move Continuously.
	 * 
	 * @param selected
	 */
	@Override
	public void setEnabled(boolean selected) {
		this.selected = selected;
		positiveButton.setEnabled(selected);
		negativeButton.setEnabled(selected);
	}

	/**
	 * @param oe
	 *            The oe to set.
	 */
	public void setOe(OE oe) {
		this.oe = oe;
	}

	/**
	 * @param dofSelected
	 *            the name of the selected DOF.
	 * @param dofControlPanel
	 *            The dofControlPanel to set.
	 */
	public void setDOFControlPanel(String dofSelected, DOFControlPanel dofControlPanel) {
		this.dofSelected = dofSelected;
		this.dofControlPanel = dofControlPanel;
	}

	private boolean startContinuousDrive() {
		boolean started = true;
//		int protectionLevel;
		int driveMode = dofControlPanel.getMode();

		try {
			// protectionLevel = oe.getProtectionLevel(dofSelected);

			// if ((protectionLevel == 0) || PasswordDialog.getInstance().isPasswordVerified(protectionLevel - 1)) {

			oe.setSpeedLevel(dofSelected, dofControlPanel.getSpeedLevel());

			switch (driveMode) {
			case DOFMode.CONTINUOUS:
				oe.moveContinuously(dofSelected, direction);
				break;
			case DOFMode.RELATIVE:
				Quantity q = dofControlPanel.getInputQuantity();
				if (q != null) {
					Quantity inputQuantity = q.times(direction);
					try {
						oe.moveBy(dofSelected, inputQuantity);
					} catch (MoveableException mex) {
						// Often the +/- buttons are clicked repeatedly
						// whilst
						// watching some feedback indicator. Quite
						// osften whilst
						// doing this users press again either before
						// the move
						// has quite finished or the moevable is
						// unlocked. This
						// gives a bit of leeway by retrying before
						// producing an
						// error.
						int relativeRetry = LocalProperties.getInt("gda.gui.oemove.relativeRetry", 0);
						if (relativeRetry != 0) {
							Thread.sleep(relativeRetry);
							oe.moveBy(dofSelected, inputQuantity);
						} else
							throw mex;
					}
				}
				break;
			}
			// } else {
			// PasswordDialog.getInstance().verifyPassword(protectionLevel,
			// "Password successfully entered.\n" + "Use the button again to drive.");
			// started = false;
			// }
		} catch (MoveableException mex) {
			logger.error(mex.getMoveableStatus().getMessage());
			JOptionPane.showMessageDialog(getTopLevelAncestor(), mex.getMoveableStatus().getMessage(), "Error Message",
					JOptionPane.ERROR_MESSAGE);
			started = false;
		} catch (Exception mex) {
			logger.error(mex.getMessage());
			JOptionPane.showMessageDialog(getTopLevelAncestor(), mex.getMessage(), "Error Message",
					JOptionPane.ERROR_MESSAGE);
			started = false;
		}
		return started;
	}

	private void stopContinuousDrive() {
		int driveMode = dofControlPanel.getMode();

		if (driveMode == DOFMode.CONTINUOUS) {
			try {
				oe.stop(dofSelected);
			} catch (MoveableException mex) {
				logger.error(mex.getMoveableStatus().getMessage());
				JOptionPane.showMessageDialog(getTopLevelAncestor(), mex.getMoveableStatus().getMessage(),
						"Error Message", JOptionPane.ERROR_MESSAGE);
			}
		}
	}
}