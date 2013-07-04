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

package gda.gui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.ExecutionException;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

/*
 * Displays a dialog box whilst a worker thread is running. Optionally displays a cancel button that will attempt to cancel the worker
 * If the worker stops either a success message is shown or , if the worker threw and exception in the doBackground method , an error msg.
 */
public class SwingWorkerProgressDialog implements java.beans.PropertyChangeListener {
	protected SwingWorker<Void, Void> worker;

	private WorkerDialog<Void, Void> dialog;

	private boolean complete = false;

	private String successMsg;
	private String errMsg;
	
	public SwingWorkerProgressDialog(SwingWorker<Void, Void> worker, Component parent, String title, String comment,
			boolean addCancel, String successMsg, String errMsg) {
		if (worker == null) {
			throw new IllegalArgumentException("SwingWorkerProgressDialog - worker = null");
		}
		this.worker = worker;
		this.dialog = new WorkerDialog<Void, Void>(worker, parent, title, comment, addCancel);

		this.successMsg = successMsg;
		this.errMsg = errMsg;
		worker.addPropertyChangeListener(this);
	}

	@Override
	public void propertyChange(java.beans.PropertyChangeEvent event) {
		if ("state".equals(event.getPropertyName()) && SwingWorker.StateValue.DONE == event.getNewValue()) {
			dialog.setVisible(false);
			dialog.dispose();
			complete = true;
			done();
		}
	}

	public void execute() {
		worker.execute();
		if (!complete)
			dialog.setVisible(true);
	}

	// function to be overridden by extenders
	protected void done() {
		try {
			worker.get();
			if( successMsg != null){
				JOptionPane.showMessageDialog(null, successMsg,
						dialog.getTitle(), JOptionPane.INFORMATION_MESSAGE);
			}
		} catch (java.util.concurrent.CancellationException e) {
			return;
		} catch (ExecutionException e){
			//do not log error as already done in SwingWorker done method
			if(errMsg != null ){
				String message = buildErrorMessage(e.getCause());
				JOptionPane.showMessageDialog(null, message, dialog.getTitle(), JOptionPane.ERROR_MESSAGE);
			}
		} catch (Exception e) {
			//do not log error as already done in SwingWorker done method
			if(errMsg != null ){
				String message = buildErrorMessage(e);
				JOptionPane.showMessageDialog(null, message, dialog.getTitle(), JOptionPane.ERROR_MESSAGE);
			}
		}
	}
	
	private String buildErrorMessage(Throwable t) {
		String message = errMsg;
		
		if (t.getMessage() != null) {
			message += " " + t.getMessage();
		}
		
		return message;
	}
}

final class WorkerDialog<T, V> extends JDialog implements ActionListener {
	private final SwingWorker<T, V> worker;

	public WorkerDialog(SwingWorker<T, V> worker, Component parent, String title, String comment, boolean addCancel) {
		super((JFrame) null, title, true);
		this.worker = worker;
		JButton cancelButton = null;
		Box labelBox = Box.createHorizontalBox();
		labelBox.add(Box.createHorizontalStrut(10));
		labelBox.add(new JLabel(comment));
		labelBox.add(Box.createHorizontalStrut(10));
		labelBox.setAlignmentX(Component.CENTER_ALIGNMENT);
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.add(Box.createVerticalStrut(10));
		panel.add(labelBox);
		panel.add(Box.createVerticalStrut(10));
		if (addCancel) {
			cancelButton = new JButton("Cancel");
			cancelButton.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					WorkerDialog.this.worker.cancel(true);
					setVisible(false);
					dispose();
				}
			});
			cancelButton.setAlignmentX(Component.CENTER_ALIGNMENT);
			panel.add(cancelButton);
			panel.add(Box.createVerticalStrut(10));
		}
		getContentPane().add(panel);
		if (cancelButton != null)
			getRootPane().setDefaultButton(cancelButton);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		pack();
		setLocationRelativeTo(parent);
		setVisible(false);// must wait until after the execute command has
		// started off the new thread.
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		worker.cancel(false);
		setVisible(false);
		dispose();
	}
}