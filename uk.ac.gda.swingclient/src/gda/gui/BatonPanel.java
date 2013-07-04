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

import gda.configuration.properties.LocalProperties;
import gda.factory.Findable;
import gda.jython.InterfaceProvider;
import gda.jython.JythonServerFacade;
import gda.jython.UserMessage;
import gda.jython.authenticator.UserAuthentication;
import gda.jython.batoncontrol.BatonChanged;
import gda.jython.batoncontrol.BatonLeaseRenewRequest;
import gda.jython.batoncontrol.BatonRequested;
import gda.jython.batoncontrol.ClientDetails;
import gda.observable.IObserver;
import gda.util.ObjectServer;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.util.ThreadManager;

/**
 *
 */
public class BatonPanel extends gda.gui.AcquisitionPanel implements IObserver, ListSelectionListener, MouseListener {

	private static final Logger logger = LoggerFactory.getLogger(BatonPanel.class);

	static private BatonPanel batonPanel_IL;
	private JScrollPane logScrollPane;
	private JPanel pnlSudoButtons;
	private JButton btnSend;
	private JCheckBox chkAutoAccept;
	private JPanel pnlTickBox;
	private JPanel pnlControlButton;
	private JButton btnrequestBaton;
	private JPanel pnlSUMessage;
	private JLabel lblSUMessage;
	private JPanel jPanel2;
	private JTextField txtMessage;
	private JTextPane logPanel;
	private JPanel pnlMessages;
	static private JLabel lblBaton;
	static private JLabel lblUser;
	static private JButton btnGive;
	static private JButton btnClaim;
	static private JButton btnRelease;
	static private JTable userClients;
	static private JPanel buttonsPanel;
	static private JPanel otherClientPanel;
	private String originalUsername;
	private JPanel sudoPanel;
	private JButton btnSudo;
	private JButton btnRevertUser;
	private BatonPanelDialog popup;
	private volatile boolean batonRequestMade = false;

	protected boolean firstUse = true;

	private boolean closeDownOnBatonRenewTimeout = false;
	private double closeDownOnBatonRenewTimeoutTimeMinutes=5;
	private double closeDownOnBatonRenewUserPromptTimeMinutes=1;

	/**
	 * If set to {@code true}, this panel will monitor the receipt of {@link BatonLeaseRenewRequest}s. If a request is
	 * not received for
	 * {@link #setCloseDownOnBatonRenewTimeoutTimeMinutes(double) closeDownOnBatonRenewTimeoutTimeMinutes} minutes, a
	 * dialog will be opened saying the client will shut down. The user can cancel the shutdown if they respond within
	 * {@link #setCloseDownOnBatonRenewUserPromptTimeMinutes(double) closeDownOnBatonRenewUserPromptTimeMinutes}
	 * minutes.
	 */
	public void setCloseDownOnBatonRenewTimeout(boolean closeDownOnBatonRenewTimeout) {
		this.closeDownOnBatonRenewTimeout = closeDownOnBatonRenewTimeout;
	}

	/**
	 * Sets the number of minutes to wait for receipt of a {@link BatonLeaseRenewRequest}. If a request is not received
	 * for this amount of time, a dialog will be opened saying that the client will shut down.
	 */
	public void setCloseDownOnBatonRenewTimeoutTimeMinutes(double closeDownOnBatonRenewTimeoutTimeMinutes) {
		this.closeDownOnBatonRenewTimeoutTimeMinutes = closeDownOnBatonRenewTimeoutTimeMinutes;
	}

	/**
	 * Sets the number of minutes during which the user is able to cancel automatic shutdown of the client.
	 */
	public void setCloseDownOnBatonRenewUserPromptTimeMinutes(double closeDownOnBatonRenewUserPromptTimeMinutes) {
		this.closeDownOnBatonRenewUserPromptTimeMinutes = closeDownOnBatonRenewUserPromptTimeMinutes;
	}

	/**
	 * Auto-generated main method to display this JPanel inside a new JFrame.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		frame.pack();
		frame.setSize(792, 419);
		frame.setPreferredSize(new java.awt.Dimension(792, 419));
		frame.setVisible(true);
		{
			batonPanel_IL = new BatonPanel();
			frame.getContentPane().add(batonPanel_IL, BorderLayout.CENTER);
			BorderLayout batonPanel_ILLayout = new BorderLayout();
			batonPanel_IL.setLayout(batonPanel_ILLayout);
		}
	}

	/**
	 * Constructor
	 */
	public BatonPanel() {
		super();

		if (LocalProperties.isBatonManagementEnabled()) {
			initGUI();
		} else {
			JPanel jPanel1 = new JPanel();
			jPanel1.setLayout(new FlowLayout());
			jPanel1.setPreferredSize(new java.awt.Dimension(500, 200));
			lblUser = new JLabel();
			lblUser.setBounds(15, 36, 422, 14);
			lblUser.setHorizontalAlignment(SwingConstants.CENTER);
			lblUser.setHorizontalTextPosition(SwingConstants.CENTER);
			lblUser.setText("Please close this panel: it is for baton control and this is not currently in use.");
			this.add(jPanel1);
			jPanel1.add(lblUser);
		}
		setLabel("Baton Panel");
	}

	private volatile boolean keepMonitoringServer = true;
	
	/**
	 * Time when the last baton renew request was received.
	 */
	private volatile long batonRenewRequestReceived;
	
	/**
	 * Time when the user last cancelled an automatic shutdown.
	 */
	private volatile long userCancelledShutdown = 0;

	void startMonitoringServer() {
		Thread batonMonitor = ThreadManager.getThread(new Runnable() {
			@Override
			public void run() {
				
				final long timeoutInMilliseconds = (long) (closeDownOnBatonRenewTimeoutTimeMinutes * 60 * 1000);
				final long closedownTimeoutInMilliseconds = (long) (closeDownOnBatonRenewUserPromptTimeMinutes * 60 * 1000);
				
				// Pretend the last baton renew request was received now.
				batonRenewRequestReceived = System.currentTimeMillis();
				
				try {
					while (keepMonitoringServer) {
						
						Thread.sleep(1000);
						
						final long timeSinceRenewRequest = System.currentTimeMillis() - batonRenewRequestReceived;
						final long timeSinceUserCancelledShutdown = System.currentTimeMillis() - userCancelledShutdown;
						final long timeSinceLastEvent = Math.min(timeSinceRenewRequest, timeSinceUserCancelledShutdown);
						
						if (timeSinceLastEvent >= timeoutInMilliseconds) {
							
							// Use invokeAndWait so that the monitoring doesn't continue until the dialog closes
							SwingUtilities.invokeAndWait(new Runnable() {
								@Override
								public void run() {
									SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

										@Override
										protected Void doInBackground() throws Exception {
											Thread.sleep(closedownTimeoutInMilliseconds);
											return null;
										}

										@Override
										protected void done() {
											if (isCancelled()) {
												userCancelledShutdown = System.currentTimeMillis();
											} else {
												logger.info("Client closing down due to lack of baton renew requests from the server which is assumed to be dead");
												AcquisitionFrame.instance.exit();
												keepMonitoringServer=false;
											}
										}

									};
									SwingWorkerProgressDialog dlg = new SwingWorkerProgressDialog(worker, null, "GDA Shutdown",
											"GDA Client has lost connection with the server. It will shut down unless you press cancel...", true, null, null);
									dlg.execute();
								}

							});
						}
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}, "BatonRequestMonitor");
		batonMonitor.start();
	}

	@Override
	public void configure() {
		if (LocalProperties.isBatonManagementEnabled()) {
			if (closeDownOnBatonRenewTimeout)
				startMonitoringServer();
			
			JythonServerFacade.getInstance().addIObserver(this);
			lblUser.setText("You are logged in as " + UserAuthentication.getUsername() + " in client #"
					+ JythonServerFacade.getInstance().getClientID());
			this.originalUsername = UserAuthentication.getUsername();
			updateDetails();
			StyledDocument log = getLog();
			if (log != null) {
				logPanel.setStyledDocument(log);
			}
			logPanel.getCaret().setDot(getLogPanel().getText().length());
		}
	}

	/**
	 * change the appearance of the give button depending if there is a selection or not {@inheritDoc}
	 * 
	 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
	 */
	@Override
	public void valueChanged(ListSelectionEvent e) {
		boolean enable = false;
		ListSelectionModel lsm = (ListSelectionModel) e.getSource();
		if (!lsm.isSelectionEmpty() && btnRelease.isEnabled() && userClients.getModel().getRowCount() > 1) {
			int selectedRow = lsm.getMinSelectionIndex();
			Integer selectedUser = (Integer) userClients.getValueAt(selectedRow, 0);
			enable = selectedUser != JythonServerFacade.getInstance().getClientID();
		}
		final boolean fenable = enable;
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				btnGive.setEnabled(fenable);
			}
		});

	}

	private void initGUI() {
		try {
			{
				BorderLayout thisLayout = new BorderLayout();
				this.setLayout(thisLayout);
				this.setPreferredSize(new java.awt.Dimension(902, 431));
			}
			{
				JPanel jPanel1 = new JPanel();
				this.add(jPanel1, BorderLayout.WEST);
				this.add(getPnlMessages(), BorderLayout.CENTER);
				jPanel1.setLayout(null);
				jPanel1.setPreferredSize(new java.awt.Dimension(480, 419));
				{
					sudoPanel = new JPanel();
					BorderLayout sudoPanelLayout = new BorderLayout();
					sudoPanel.setLayout(sudoPanelLayout);
					jPanel1.add(sudoPanel);
					sudoPanel.add(getPnlSudoButtons(), BorderLayout.CENTER);
					sudoPanel.add(getPnlSUMessage(), BorderLayout.NORTH);
					sudoPanel.setBorder(BorderFactory.createTitledBorder("Switch User"));
					sudoPanel.setBounds(12, 340, 456, 80);
				}
				{
					otherClientPanel = new JPanel();
					jPanel1.add(otherClientPanel);
					otherClientPanel.setBorder(BorderFactory.createTitledBorder("Clients on this beamline"));
					otherClientPanel.setBounds(12, 15, 456, 215);
					otherClientPanel.setLayout(null);
					{
						lblUser = new JLabel();
						otherClientPanel.add(lblUser);
						lblUser.setText("You are logged in as: abc123");
						lblUser.setBounds(15, 36, 422, 14);
						lblUser.setHorizontalAlignment(SwingConstants.CENTER);
						lblUser.setHorizontalTextPosition(SwingConstants.CENTER);
					}
					{
						lblBaton = new JLabel();
						otherClientPanel.add(lblBaton);
						lblBaton.setText("You hold the baton and have control of the beamline");
						lblBaton.setBounds(15, 66, 422, 14);
						lblBaton.setHorizontalTextPosition(SwingConstants.CENTER);
						lblBaton.setInheritsPopupMenu(false);
						lblBaton.setHorizontalAlignment(SwingConstants.CENTER);
					}
					{
						JScrollPane jScrollPane1 = new JScrollPane();
						otherClientPanel.add(jScrollPane1);
						jScrollPane1.setBounds(73, 96, 306, 97);
						{
							userClients = new JTable();
							jScrollPane1.setViewportView(userClients);
							userClients.setModel(this.new BatonTableModel());
							userClients.setAutoCreateRowSorter(true);
							userClients.setPreferredSize(new java.awt.Dimension(213, 97));
							userClients.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
							userClients.setColumnSelectionAllowed(false);
							userClients.getSelectionModel().addListSelectionListener(this);

							DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
							renderer.setHorizontalAlignment(SwingConstants.CENTER);
							userClients.getColumnModel().getColumn(0).setCellRenderer(renderer);
							userClients.getColumnModel().getColumn(1).setCellRenderer(renderer);
							userClients.getColumnModel().getColumn(2).setCellRenderer(renderer);

							JTableHeader th = userClients.getTableHeader();
							th.addMouseListener(this);

						}
					}
				}
				{
					buttonsPanel = new JPanel();
					BorderLayout buttonsPanelLayout = new BorderLayout();
					buttonsPanel.setLayout(buttonsPanelLayout);
					jPanel1.add(buttonsPanel);
					buttonsPanel.setBorder(BorderFactory.createTitledBorder("Baton control"));
					buttonsPanel.setBounds(12, 242, 456, 92);
					buttonsPanel.add(getPnlControlButton(), BorderLayout.CENTER);
					buttonsPanel.add(getPnlTickBox(), BorderLayout.SOUTH);
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	private void updateDetails() {
		// this should be run in the swing thread

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				boolean amHolder = JythonServerFacade.getInstance().amIBatonHolder();

				userClients.clearSelection();
				userClients.setRowSorter(null);
				ClientDetails[] facades = JythonServerFacade.getInstance().getOtherClientInformation();

				changeTitleBarString(getPnlSUMessage().isVisible());

				((BatonTableModel) userClients.getModel()).setData(facades);
				userClients.addNotify();
				userClients.setAutoCreateRowSorter(true);

				boolean batonHeld = JythonServerFacade.getInstance().isBatonHeld();

				if (amHolder) {
					lblBaton.setText("You hold the baton and have control of the beamline");
					btnRelease.setEnabled(true);
					btnClaim.setEnabled(false);
					btnGive.setEnabled(false);
					btnrequestBaton.setEnabled(false);
				} else if (batonHeld) {
					lblBaton.setText("The baton is held by another client");
					btnRelease.setEnabled(false);
					btnGive.setEnabled(false);
					// compare auth levels
					if (JythonServerFacade.getInstance().getBatonHolder().getAuthorisationLevel() > JythonServerFacade
							.getInstance().getMyDetails().getAuthorisationLevel()) {
						btnClaim.setEnabled(false);
						btnrequestBaton.setEnabled(true);
					} else {
						btnClaim.setEnabled(true);
						btnrequestBaton.setEnabled(false);
					}
				} else {
					lblBaton.setText("The baton is not held by anyone");
					btnRelease.setEnabled(false);
					btnGive.setEnabled(false);
					btnClaim.setEnabled(true);
					btnrequestBaton.setEnabled(false);
					chkAutoAccept.setEnabled(false);
				}
				// Always show message log as it can be used as an experiment log
				getPnlMessages().setVisible(true);
			}
		});
	}

	private class BatonTableModel extends AbstractTableModel {

		ClientDetails[] facades = new ClientDetails[0];

		/**
		 * Constructor.
		 */
		public BatonTableModel() {
			super();
		}

		@Override
		public boolean isCellEditable(int row, int col) {
			return false;
		}

		@Override
		public String getColumnName(int i) {
			switch (i) {
			case 0:
				return "Client #";
			case 1:
				return "Username";
			case 2:
				return "Host name";
			case 3:
				return "Holds baton";
			default:
				return null;
			}
		}

		@Override
		public int getColumnCount() {
			return 4;
		}

		@Override
		public int getRowCount() {
			return facades.length;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {

			// if out of bounds
			if (rowIndex >= facades.length) {
				return null;
			}

			switch (columnIndex) {
			case 0:
				return facades[rowIndex].getIndex();
			case 1:
				return facades[rowIndex].getUserID();
			case 2:
				return facades[rowIndex].getHostname();
			case 3:
				if (facades[rowIndex].isHasBaton()) {
					return "*";
				}
				return "";
			default:
				return null;
			}
		}

		public void setData(ClientDetails[] newFacades) {
			ClientDetails me = JythonServerFacade.getInstance().getMyDetails();

			newFacades = (ClientDetails[]) ArrayUtils.add(newFacades, me);
			facades = newFacades;
			addNotify();
		}

	}

	@Override
	public void update(Object theObserved, Object changeCode) {

		if (isVisible()) {

			if (changeCode instanceof BatonChanged) {
				// make this call here as Swing thread will not be run if panel hidden!
				updateDetails();
			} else if (changeCode instanceof BatonLeaseRenewRequest) {
				InterfaceProvider.getBatonStateProvider().amIBatonHolder();
				batonRenewRequestReceived = System.currentTimeMillis();
			} else if (changeCode instanceof UserMessage) {
				updateMessagePanel((UserMessage) changeCode);
				gda.gui.AcquisitionFrame.showFrame(getName());
			} else if (changeCode instanceof BatonRequested) {
				if (JythonServerFacade.getInstance().amIBatonHolder() && !batonRequestMade) {
					batonRequestMade = true;
					final BatonRequested request = (BatonRequested) changeCode;
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							BatonRequestPopup batonRequestPopup = new BatonRequestPopup((Frame) BatonPanel.this
									.getTopLevelAncestor(), request.getRequester().getUserID(), 120, getChkAutoAccept()
									.isSelected());
							boolean willRelease = batonRequestPopup.isOk();
							batonRequestPopup = null;

							if (willRelease) {
								InterfaceProvider.getBatonStateProvider()
										.assignBaton(request.getRequester().getIndex());
							} else {
								InterfaceProvider.getBatonStateProvider().sendMessage("Baton request denied.");
							}

							batonRequestMade = false;
						}
					});
				}
			}
		}
	}

	private void updateMessagePanel(final UserMessage message) {
		SwingUtilities.invokeLater(new Runnable() {

			private void addMessageHeader() throws BadLocationException {
				StyledDocument doc = (StyledDocument) logPanel.getDocument();

				Date date = new Date();
				Format formatter = new SimpleDateFormat("dd MMM HH:mm:ss");
				String newMessage = "\n" + formatter.format(date) + "\t";

				newMessage += "#" + message.getSourceClientNumber();
				newMessage += " " + message.getSourceUsername() + "";

				doc.insertString(doc.getLength(), newMessage, doc.getStyle("bold"));
			}

			private void addMessageBody() throws BadLocationException {
				StyledDocument doc = (StyledDocument) logPanel.getDocument();
				String newMessage = "\n    ";
				newMessage += message.getMessage();
				doc.insertString(doc.getLength(), newMessage, doc.getStyle("regular"));
			}

			@Override
			public void run() {
				try {
					addMessageHeader();
					addMessageBody();
					// scroll down to display new message
					getLogPanel().getCaret().setDot(getLogPanel().getText().length());
					saveLog(logPanel.getStyledDocument());
				} catch (BadLocationException e) {
					//
				}
			}
		});
	}

	private void btnClaimActionPerformed() {
		if (!JythonServerFacade.getInstance().requestBaton()) {
			JOptionPane.showMessageDialog(this, "A request has been sent to the current baton holder", "Baton Request",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private void btnReleaseActionPerformed() {
		JythonServerFacade.getInstance().returnBaton();
	}

	private void btnSudoActionPerformed() {

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				getSUDialog().setVisible(true);
				getSUDialog().setLocationRelativeTo(btnSudo);
				if (getSUDialog().getAuthenticated()) {
					btnRevertUser.setEnabled(true);
					btnSudo.setEnabled(false);
					changeTitleBarString(true);

					getLblSUMessage().setText("Running as User: " + getSUDialog().getUserName());

					getPnlSUMessage().setVisible(true);

					JOptionPane.showMessageDialog(getTopLevelAncestor(), "Client now operating as user "
							+ getSUDialog().getUserName());

					updateDetails();
				} else {
					JOptionPane.showMessageDialog(getTopLevelAncestor(), "User not authenticated", "Switch User",
							JOptionPane.WARNING_MESSAGE);
				}
			}
		});
	}

	private BatonPanelDialog getSUDialog() {
		if (popup == null) {
			popup = new BatonPanelDialog((JFrame) BatonPanel.this.getTopLevelAncestor());
			popup.pack();
		}
		return popup;
	}

	private void btnRevertUserActionPerformed() {
		lblUser.setText("You are logged in as: " + originalUsername);
		JythonServerFacade.getInstance().revertToOriginalUser();
		btnRevertUser.setEnabled(false);
		btnSudo.setEnabled(true);

		changeTitleBarString(false);

		getPnlSUMessage().setVisible(false);

	}

	private void btnGiveActionPerformed() {

		int selectedRow = userClients.getSelectedRow();

		// nothing selected
		if (selectedRow == -1) {
			JOptionPane.showMessageDialog(this, "Please select a client from the table", "Selected required",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		Integer index = (Integer) userClients.getModel().getValueAt(selectedRow, 0);

		JythonServerFacade.getInstance().assignBaton(index);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				btnGive.setEnabled(false);
			}
		});
	}

	private JPanel getPnlMessages() {
		if (pnlMessages == null) {
			pnlMessages = new JPanel();
			BorderLayout pnlMessagesLayout = new BorderLayout();
			pnlMessages.setLayout(pnlMessagesLayout);
			pnlMessages.setBorder(BorderFactory.createTitledBorder("Messages"));
			pnlMessages.setPreferredSize(new java.awt.Dimension(17, 431));
			// pnlMessages.setPreferredSize(new java.awt.Dimension(333, 389));
			pnlMessages.add(getMessageSendPnl(), BorderLayout.NORTH);
			pnlMessages.add(getLogScrollPane(), BorderLayout.CENTER);
			pnlMessages.setVisible(false);
		}
		return pnlMessages;
	}

	private JScrollPane getLogScrollPane() {
		if (logScrollPane == null) {
			logScrollPane = new JScrollPane();
			logScrollPane.setViewportView(getLogPanel());
			logScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
			logScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		}
		return logScrollPane;
	}

	private String getLogPath() {
		ClientDetails details = JythonServerFacade.getInstance().getMyDetails();
		File f = new File(LocalProperties.getVarDir() + File.separator + "message_" + details.getVisitID() + ".log");
		return f.getAbsolutePath();
	}

	private JTextPane getLogPanel() {
		if (logPanel == null) {
			logPanel = new JTextPane();
			logPanel.setEditable(false);
			StyledDocument doc = logPanel.getStyledDocument();

			Style def = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);

			Style regular = doc.addStyle("regular", def);
			StyleConstants.setFontFamily(def, "SansSerif");

			Style s = doc.addStyle("italic", regular);
			StyleConstants.setItalic(s, true);

			s = doc.addStyle("bold", regular);
			StyleConstants.setBold(s, true);
		}
		return logPanel;
	}

	private StyledDocument getLog() {
		ObjectInputStream ois = null;
		StyledDocument doc = null;
		String s = getLogPath();
		File f = new File(s);
		try {
			if (f.exists()) {
				FileInputStream fis = new FileInputStream(s);
				ois = new ObjectInputStream(fis);
				doc = (StyledDocument) ois.readObject();
			}
		} catch (Exception e) {
			logger.error("unable to read message log from " + f, e);
		} finally {
			if (ois != null) {
				try {
					ois.close();
				} catch (IOException e) {
					// do nothing
				}
				ois = null;
			}
		}
		return doc;
	}

	private void saveLog(StyledDocument doc) {
		ObjectOutputStream oos = null;
		String s = getLogPath();
		File f = new File(s);
		try {
			File fparent = new File(f.getParent());
			if (!fparent.exists()) {
				fparent.mkdirs();
			}
			FileOutputStream fos = new FileOutputStream(getLogPath());
			oos = new ObjectOutputStream(fos);
			oos.writeObject(doc);// doc is the styled document set for this textpane
			oos.flush();
		} catch (Exception e) {
			logger.error("unable to save message log to " + f, e);
		} finally {
			if (oos != null) {
				try {
					oos.close();
				} catch (IOException e) {
					// do nothing
				}
				oos = null;
			}
		}
	}

	private JTextField getTxtMessage() {
		if (txtMessage == null) {
			txtMessage = new JTextField();
			txtMessage.setText("Type your messages here");
			// txtMessage.setPreferredSize(new java.awt.Dimension(258, 22));
			txtMessage.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent evt) {
					if (firstUse) {
						getTxtMessage().setText("");
						firstUse = false;
					}
				}
			});
			txtMessage.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent evt) {
					if (!firstUse && !getTxtMessage().getText().trim().equals("")) {
						InterfaceProvider.getBatonStateProvider().sendMessage(getTxtMessage().getText());
						getTxtMessage().setText("");
					}
				}
			});
		}
		return txtMessage;
	}

	private JPanel getMessageSendPnl() {
		if (jPanel2 == null) {
			jPanel2 = new JPanel();
			BorderLayout jPanel2Layout = new BorderLayout();
			jPanel2.setLayout(jPanel2Layout);
			jPanel2.add(getTxtMessage(), BorderLayout.CENTER);
			jPanel2.add(getBtnSend(), BorderLayout.EAST);
		}
		return jPanel2;
	}

	private JButton getBtnSend() {
		if (btnSend == null) {
			btnSend = new JButton();
			btnSend.setText("Send");
			btnSend.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent evt) {
					if (!firstUse && !getTxtMessage().getText().trim().equals("")) {
						InterfaceProvider.getBatonStateProvider().sendMessage(getTxtMessage().getText().trim());
						getTxtMessage().setText("");
					}
				}
			});
		}
		return btnSend;
	}

	private JPanel getPnlSudoButtons() {
		if (pnlSudoButtons == null) {
			pnlSudoButtons = new JPanel();
			{
				btnSudo = new JButton();
				pnlSudoButtons.add(btnSudo);
				btnSudo.setText("Switch...");
				btnSudo
						.setToolTipText("Change the user name and permission level that this client runs as. The VisitID will not be changed.");
				btnSudo.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent evt) {
						btnSudoActionPerformed();
					}
				});
			}
			{
				btnRevertUser = new JButton();
				pnlSudoButtons.add(btnRevertUser);
				btnRevertUser.setText("Revert");
				btnRevertUser.setToolTipText("Revert to original user");
				btnRevertUser.setEnabled(false);
				btnRevertUser.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent evt) {
						btnRevertUserActionPerformed();
					}
				});
			}
		}
		return pnlSudoButtons;
	}

	private JLabel getLblSUMessage() {
		if (lblSUMessage == null) {
			lblSUMessage = new JLabel();
			lblSUMessage.setText("Running as Super User:");
			lblSUMessage.setFont(new java.awt.Font("Dialog", 1, 12));
			lblSUMessage.setPreferredSize(new java.awt.Dimension(250, 13));
			lblSUMessage.setMinimumSize(new java.awt.Dimension(250, 13));
			lblSUMessage.setBackground(new java.awt.Color(30, 144, 255));
		}
		return lblSUMessage;
	}

	private JPanel getPnlSUMessage() {
		if (pnlSUMessage == null) {
			pnlSUMessage = new JPanel();
			pnlSUMessage.add(getLblSUMessage());
			pnlSUMessage.setVisible(false);
			pnlSUMessage.setBackground(new java.awt.Color(30, 144, 255));
		}
		return pnlSUMessage;
	}

	private void changeTitleBarString(final boolean usingSuperUser) {

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				Boolean amBatonHolder = JythonServerFacade.getInstance().amIBatonHolder();
				String title = AcquisitionGUI.getTitlePrefix();
				title += usingSuperUser ? " - running as ALTERNATE USER " + getSUDialog().getUserName() : "";
				title += amBatonHolder ? " - you hold the BATON" : " - you DO NOT hold the BATON";
				Container top = getTopLevelAncestor();
				if (top != null) {
					if (top instanceof JFrame) {
						((JFrame) top).setTitle(title);
					}
				}
			}
		});
	}

	private JButton getBtnrequestBaton() {
		if (btnrequestBaton == null) {
			btnrequestBaton = new JButton();
			btnrequestBaton.setText("Request");
			btnrequestBaton.setToolTipText("Request the baton - the current holder will be shown a dialog box");
			btnrequestBaton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent evt) {
					JythonServerFacade.getInstance().requestBaton();
				}
			});
		}
		return btnrequestBaton;
	}

	private JPanel getPnlControlButton() {
		if (pnlControlButton == null) {
			pnlControlButton = new JPanel();
			{
				btnClaim = new JButton();
				pnlControlButton.add(btnClaim);
				btnClaim.setText(" Take ");
				btnClaim.setToolTipText("Take the baton off the current holder");
				btnClaim.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent evt) {
						btnClaimActionPerformed();
					}
				});
			}
			{
				btnRelease = new JButton();
				pnlControlButton.add(btnRelease);
				btnRelease.setText("Release");
				btnRelease.setToolTipText("Release the baton for any client to pick up");
				btnRelease.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent evt) {
						btnReleaseActionPerformed();
					}
				});
			}
			{
				btnGive = new JButton();
				pnlControlButton.add(getBtnrequestBaton());
				pnlControlButton.add(btnGive);
				buttonsPanel.add(getPnlControlButton(), BorderLayout.CENTER);
				buttonsPanel.add(getPnlTickBox(), BorderLayout.SOUTH);
				btnGive.setText("Pass to other client...");
				btnGive.setToolTipText("Pass the baton to the selected client");
				btnGive.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent evt) {
						btnGiveActionPerformed();
					}
				});
			}
		}
		return pnlControlButton;
	}

	private JPanel getPnlTickBox() {
		if (pnlTickBox == null) {
			pnlTickBox = new JPanel();
			pnlTickBox.add(getChkAutoAccept());
		}
		return pnlTickBox;
	}

	private JCheckBox getChkAutoAccept() {
		if (chkAutoAccept == null) {
			chkAutoAccept = new JCheckBox();
			chkAutoAccept.setText("Automatically accept baton release requests after 2 mins");
			chkAutoAccept.setSelected(true);
		}
		return chkAutoAccept;
	}
}

/*
 * Dialog to show at startup when the user is not on the current visit and is not a member of staff
 */
class JBatonDialog extends JDialog implements IObserver {
	private static final Logger logger = LoggerFactory.getLogger(JBatonDialog.class);

	public static void displayBatonPanel(Frame aFrame, ObjectServer objectServer) {
		List<String> names = objectServer.getFindableNames();
		if (names != null) {
			for (String name : names) {
				try {
					Findable findable = objectServer.getFindable(name);
					if (findable instanceof BatonPanel) {
						JBatonDialog dialog = new JBatonDialog(aFrame,
								"You are not premitted to view the current visit until you have the baton", true,
								(BatonPanel) findable);
						dialog.setVisible(true);
						break;
					}
				} catch (Exception ex) {
					logger.error("AcquisitionFrame: configure() name " + name, ex);
				}
			}
		}
	}

	JBatonDialog(Frame aFrame, String title, boolean modal, BatonPanel panel) {
		super(aFrame, title, modal);
		setContentPane(panel);
		pack();
		InterfaceProvider.getBatonStateProvider().addBatonChangedObserver(this);
		addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				setVisible(false);
			}

		});
	}

	@Override
	public void update(Object theObserved, Object changeCode) {
		if (changeCode instanceof BatonChanged && InterfaceProvider.getBatonStateProvider().amIBatonHolder()) {
			InterfaceProvider.getBatonStateProvider().deleteBatonChangedObserver(this);
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					setVisible(false);
				}

			});
		}
	}
}