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

package gda.gui;

import gda.jython.JythonServerFacade;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;


class BatonRequestPopup extends JDialog implements ActionListener, Runnable {

	private JButton jButton_Yes = null;
	private JButton jButton_NO = null;
	private boolean OK = false;
	private JPanel jPanel1;
	private JPanel jPanel2;
	private Thread thread = null;
	private int max = 30;
	private boolean acceptIfTimeout;

	public BatonRequestPopup(Frame frame, String requesterName, int timeout, boolean acceptIfTimeout) {
		super(frame, true);
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		this.max = timeout;
		this.acceptIfTimeout = acceptIfTimeout;

		Container cont = getContentPane();
		BorderLayout contLayout = new BorderLayout();
		cont.setLayout(contLayout);
		{
			jPanel1 = new JPanel();
			cont.add(jPanel1, BorderLayout.NORTH);
			{
				JLabel jLabel = new JLabel("User " + requesterName + " has requested the baton. Will you pass it over?");
				jPanel1.add(jLabel);
			}
		}
		{
			jPanel2 = new JPanel();
			cont.add(jPanel2, BorderLayout.CENTER);
			{
				jButton_Yes = new JButton("Yes, I will RELEASE the baton");
				jPanel2.add(jButton_Yes);
				jButton_Yes.addActionListener(this);
			}
			{
				jButton_NO = new JButton("No, I will KEEP the baton");
				jPanel2.add(jButton_NO);
				jButton_NO.addActionListener(this);
			}
		}

		setTitle("Baton request");

		pack();
		this.setSize(443, 92);
		thread = new Thread(this);
		thread.start();
		setVisible(true);
		setLocationRelativeTo(frame);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == jButton_Yes)
			OK = true;
		if (e.getSource() == jButton_NO)
			OK = false;
		setVisible(false);
	}

	@Override
	public void run() {
		while (max > 0) {
			max--;
			if (this.acceptIfTimeout) {
				setTitle("Baton request - " + max);
			}
			
			if (!JythonServerFacade.getInstance().amIBatonHolder()){
				setVisible(false);
				OK = true;
				return;
			}
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException exc) {
			}
		}
		if (isVisible()) {
			OK = this.acceptIfTimeout;
			setVisible(false);
		}
	}

	public boolean isOk() {
		return OK;
	}

}
