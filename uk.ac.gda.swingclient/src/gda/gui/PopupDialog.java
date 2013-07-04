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

package gda.gui;

import gda.configuration.properties.LocalProperties;
import gda.util.MultiScreenSupport;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * A customisable dialog box that popups up for user interaction. It provides a Borderlayout for developers to customse
 * for their need. The only restriction is the south panel must be a dialog of the JOptionPane, which is used to provide
 * interaction with users. This class is now support mutil-screen display.
 * 
 * @see javax.swing.JDialog
 * @see javax.swing.JOptionPane
 */
public class PopupDialog extends JDialog {
	private JOptionPane southPane;

	private JPanel northPane;

	private JPanel westPane;

	private JPanel eastPane;

	private JPanel centrePane;

	private MultiScreenSupport mss = new MultiScreenSupport();

	private int screenIndex = 0;

	/**
	 * Creates the reusable dialog window.
	 * 
	 * @param aFrame -
	 *            the parent Component
	 * @param title -
	 *            the title string
	 * @param modal -
	 *            if true it blocks.
	 * @param northPane -
	 *            a JPanel
	 * @param westPane -
	 *            a JPanel
	 * @param centrePane -
	 *            a JPanel
	 * @param eastPane -
	 *            a JPanel
	 * @param optionPane -
	 *            must be an instance of the JOptionPane
	 */
	public PopupDialog(Frame aFrame, String title, boolean modal, JPanel northPane, JPanel westPane, JPanel centrePane,
			JPanel eastPane, final JOptionPane optionPane) {
		super(aFrame, title, modal);
		this.southPane = optionPane;
		this.northPane = northPane;
		this.westPane = westPane;
		this.eastPane = eastPane;
		this.centrePane = centrePane;
		if (LocalProperties.get("gda.screen.primary") != null) {
			this.screenIndex = LocalProperties.getInt("gda.screen.primary", 0);
		}

		JDialog.setDefaultLookAndFeelDecorated(true);

		// Make a JPanel the content pane.
		JPanel contentPane = new JPanel(new BorderLayout());
		if (northPane != null) {
			contentPane.add(northPane, BorderLayout.NORTH);
		}
		if (westPane != null) {
			contentPane.add(westPane, BorderLayout.WEST);
		}
		if (centrePane != null) {
			contentPane.add(centrePane, BorderLayout.CENTER);
		}
		if (eastPane != null) {
			contentPane.add(eastPane, BorderLayout.EAST);
		}
		if (optionPane != null) {
			contentPane.add(optionPane, BorderLayout.SOUTH);
		}

		contentPane.setOpaque(true);
		contentPane.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createRaisedBevelBorder(), BorderFactory
				.createEmptyBorder(3, 3, 3, 3)));
		setContentPane(contentPane);
		pack();
		if (aFrame != null) {
			setLocation(aFrame.getX() + (aFrame.getWidth() / 2 - contentPane.getWidth() / 2), aFrame.getY()
					+ (aFrame.getHeight() / 2 - contentPane.getHeight() / 2));
		} else {
			setLocation(mss.getScreenXoffset(screenIndex)
					+ (mss.getScreenWidth(screenIndex) / 2 - contentPane.getWidth() / 2), mss
					.getScreenYoffset(screenIndex)
					+ (mss.getScreenHeight(screenIndex) / 2 - contentPane.getHeight() / 2));
		}
		// Handle window closing correctly.
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent we) {
				/*
				 * Instead of directly closing the window, we're going to change the JOptionPane's value property.
				 */
				if (optionPane != null) {
					optionPane.setValue(new Integer(JOptionPane.CLOSED_OPTION));
				}
			}
		});
	}

	/**
	 * A convenient constructor
	 * 
	 * @param aFrame
	 * @param title
	 * @param modal
	 * @param summary
	 * @param graph
	 * @param optionPane
	 */
	public PopupDialog(Frame aFrame, String title, boolean modal, JPanel summary, JPanel graph,
			final JOptionPane optionPane) {
		this(aFrame, title, modal, summary, null, graph, null, optionPane);
	}

	/**
	 * @param aFrame
	 * @param title
	 * @param modal
	 * @param optionPane
	 */
	public PopupDialog(Frame aFrame, String title, boolean modal, final JOptionPane optionPane) {
		this(aFrame, title, modal, null, null, null, null, optionPane);
	}

	@Override
	public void addPropertyChangeListener(PropertyChangeListener l) {
		southPane.addPropertyChangeListener(l);
	}

	@Override
	public void removePropertyChangeListener(PropertyChangeListener l) {
		southPane.removePropertyChangeListener(l);
	}

	/** This method clears the dialog and hides it. */
	public void clearAndHide() {
		setVisible(false);
	}

	/**
	 * @return centre pane
	 */
	public JPanel getCentrePane() {
		return centrePane;
	}

	/**
	 * @param centrePane
	 */
	public void setCentrePane(JPanel centrePane) {
		this.centrePane = centrePane;
	}

	/**
	 * @return east pane
	 */
	public JPanel getEastPane() {
		return eastPane;
	}

	/**
	 * @param eastPane
	 */
	public void setEastPane(JPanel eastPane) {
		this.eastPane = eastPane;
	}

	/**
	 * @return north pane
	 */
	public JPanel getNorthPane() {
		return northPane;
	}

	/**
	 * @param northPane
	 */
	public void setNorthPane(JPanel northPane) {
		this.northPane = northPane;
	}

	/**
	 * @return MultiScreenSupport
	 */
	public MultiScreenSupport getScreen() {
		return mss;
	}

	/**
	 * @param screen
	 */
	public void setScreen(MultiScreenSupport screen) {
		this.mss = screen;
	}

	/**
	 * @return south pane
	 */
	public JOptionPane getSouthPane() {
		return southPane;
	}

	/**
	 * @param southPane
	 */
	public void setSouthPane(JOptionPane southPane) {
		this.southPane = southPane;
	}

	/**
	 * @return west pane
	 */
	public JPanel getWestPane() {
		return westPane;
	}

	/**
	 * @param westPane
	 */
	public void setWestPane(JPanel westPane) {
		this.westPane = westPane;
	}
}