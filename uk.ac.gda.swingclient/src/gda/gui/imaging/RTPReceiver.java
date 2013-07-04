package gda.gui.imaging;

/*
 * @(#)AVReceive2.java 1.3 01/03/13 Copyright (c) 1999-2001 Sun Microsystems, Inc. All Rights Reserved. Sun grants you
 * ("Licensee") a non-exclusive, royalty free, license to use, modify and redistribute this software in source and
 * binary code form, provided that i) this copyright notice and license appear on all copies of the software; and ii)
 * Licensee does not utilize the software in a manner which is disparaging to Sun. This software is provided "AS IS,"
 * without a warranty of any kind. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING ANY
 * IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN
 * AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR
 * DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR ANY LOST REVENUE,
 * PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND
 * REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE SOFTWARE, EVEN IF SUN HAS BEEN
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGES. This software is not designed or intended for use in on-line control of
 * aircraft, air traffic, aircraft navigation or aircraft communications; or in the design, construction, operation or
 * maintenance of any nuclear facility. Licensee represents and warrants that it will not use or redistribute the
 * Software for such purposes.
 */

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.net.InetAddress;

import javax.media.ControllerEvent;
import javax.media.ControllerListener;
import javax.media.Player;
import javax.media.RealizeCompleteEvent;
import javax.media.control.BufferControl;
import javax.media.protocol.DataSource;
import javax.media.rtp.Participant;
import javax.media.rtp.RTPControl;
import javax.media.rtp.RTPManager;
import javax.media.rtp.ReceiveStream;
import javax.media.rtp.ReceiveStreamListener;
import javax.media.rtp.SessionAddress;
import javax.media.rtp.SessionListener;
import javax.media.rtp.event.ByeEvent;
import javax.media.rtp.event.NewParticipantEvent;
import javax.media.rtp.event.NewReceiveStreamEvent;
import javax.media.rtp.event.ReceiveStreamEvent;
import javax.media.rtp.event.RemotePayloadChangeEvent;
import javax.media.rtp.event.SessionEvent;
import javax.media.rtp.event.StreamMappedEvent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * JPanel which displays data from a RTP/JPEG source
 * <p>
 * Requirements to run this: jmf.jar and the GDA jmf.properties file placed into the same folder as that jar
 * <p>
 * If running on Linux, you may need to install libXp.i386 rpm on that system
 * <p>
 * Also check that the firewall is not preventing the UDP socket connection.
 */
public class RTPReceiver extends JPanel implements ReceiveStreamListener, SessionListener, ControllerListener {

	String address = "";
	int port;
	Object dataSync = new Object();
	Component vc;
	RTPManager mgr = null;
	Player thisPlayer;

	/**
	 * Constructor. The address and port must be for a UDP socket
	 * 
	 * @param address
	 * @param port
	 */
	public RTPReceiver(String address, int port) {
		this.address = address;
		this.port = port;
		setLayout(new BorderLayout());
	}

	/**
	 * Connect to the multicast address. Data will be displayed in a separate thread when it starts to arrive
	 * 
	 * @return boolean - if the connection was successful
	 */
	public boolean start() {

		try {
			InetAddress ipAddr;
			SessionAddress localAddr = new SessionAddress();
			SessionAddress destAddr;

			System.err.println("  - Open RTP session for: addr: " + this.address + " port: " + this.port);

			mgr = RTPManager.newInstance();
			mgr.addSessionListener(this);
			mgr.addReceiveStreamListener(this);

			ipAddr = InetAddress.getByName(this.address);

			if (ipAddr.isMulticastAddress()) {
				// local and remote address pairs are identical:
				localAddr = new SessionAddress(ipAddr, this.port, 1);
				destAddr = new SessionAddress(ipAddr, this.port, 1);
			} else {
				localAddr = new SessionAddress(InetAddress.getLocalHost(), this.port);
				destAddr = new SessionAddress(ipAddr, this.port);
			}

			mgr.initialize(localAddr);

			// You can try out some other buffer size to see
			// if you can get better smoothness.
			BufferControl bc = (BufferControl) mgr.getControl("javax.media.control.BufferControl");
			if (bc != null) {
				bc.setBufferLength(350);
			}
			mgr.addTarget(destAddr);

		} catch (Exception e) {
			System.err.println("Cannot create the RTP Session: " + e.getMessage());
			return false;
		}
		return true;
	}

	/**
	 * Close the players and the session managers.
	 */
	public void close() {
		thisPlayer.close();

		mgr.removeTargets("Closing session from AVReceive2");
		mgr.dispose();
		mgr = null;
	}

	@Override
	public void setVisible(boolean isVisible) {
		if (!isVisible) {
			this.close();
		}
		super.setVisible(isVisible);
	}

	@Override
	public Dimension getPreferredSize() {
		int w = 0, h = 0;
		if (vc != null) {
			Dimension size = vc.getPreferredSize();
			w = size.width;
			h = size.height;
		}
		if (w < 160) {
			w = 160;
		}
		return new Dimension(w, h);
	}

	/**
	 * SessionListener. {@inheritDoc}
	 * 
	 * @see javax.media.rtp.SessionListener#update(javax.media.rtp.event.SessionEvent)
	 */
	@Override
	public synchronized void update(SessionEvent evt) {
		if (evt instanceof NewParticipantEvent) {
			Participant p = ((NewParticipantEvent) evt).getParticipant();
			System.err.println("  - A new participant had just joined: " + p.getCNAME());
		}
	}

	/**
	 * ReceiveStreamListener {@inheritDoc}
	 * 
	 * @see javax.media.rtp.ReceiveStreamListener#update(javax.media.rtp.event.ReceiveStreamEvent)
	 */
	@Override
	public synchronized void update(ReceiveStreamEvent evt) {
		Participant participant = evt.getParticipant(); // could be null.
		ReceiveStream stream = evt.getReceiveStream(); // could be null.

		if (evt instanceof RemotePayloadChangeEvent) {

			System.err.println("  - Received an RTP PayloadChangeEvent.");
			System.err.println("Sorry, cannot handle payload change.");
			System.exit(0);

		}

		else if (evt instanceof NewReceiveStreamEvent) {

			try {
				stream = ((NewReceiveStreamEvent) evt).getReceiveStream();
				DataSource ds = stream.getDataSource();

				// Find out the formats.
				RTPControl ctl = (RTPControl) ds.getControl("javax.media.rtp.RTPControl");
				if (ctl != null) {
					System.err.println("  - Recevied new RTP stream: " + ctl.getFormat());
				} else
					System.err.println("  - Recevied new RTP stream");

				if (participant == null)
					System.err.println("      The sender of this stream had yet to be identified.");
				else {
					System.err.println("      The stream comes from: " + participant.getCNAME());
				}

				// create a player by passing datasource to the Media Manager
				Player p = javax.media.Manager.createPlayer(ds);
				if (p == null)
					return;

				p.addControllerListener(this);
				p.realize();
			} catch (Exception e) {
				System.err.println("NewReceiveStreamEvent exception " + e.getMessage());
				return;
			}

		}

		else if (evt instanceof StreamMappedEvent) {

			if (stream != null && stream.getDataSource() != null) {
				DataSource ds = stream.getDataSource();
				// Find out the formats.
				RTPControl ctl = (RTPControl) ds.getControl("javax.media.rtp.RTPControl");
				System.err.println("  - The previously unidentified stream ");
				if (ctl != null)
					System.err.println("      " + ctl.getFormat());
				System.err.println("      had now been identified as sent by: " + participant.getCNAME());
			}
		} else if (evt instanceof ByeEvent) {
			System.err.println("  - Got \"bye\" from: " + participant.getCNAME());
		}
	}

	/**
	 * ControllerListener for the Players. {@inheritDoc}
	 * 
	 * @see javax.media.ControllerListener#controllerUpdate(javax.media.ControllerEvent)
	 */
	@Override
	public synchronized void controllerUpdate(ControllerEvent ce) {

		Player p = (Player) ce.getSourceController();

		// Get this when the internal players are realized.
		if (ce instanceof RealizeCompleteEvent) {
			if (p != null) {
				setupUI(p);
				p.start();
			}
		}

	}

	private void setupUI(Player p) {

		thisPlayer = p;

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				if ((vc = thisPlayer.getVisualComponent()) != null) {
					add("Center", vc);
				}
			}
		});
	}

	/**
	 * Test harness
	 * 
	 * @param argv
	 */
	public static void main(String argv[]) {

		if (argv.length != 2) {
			prUsage();
		}
		String address = argv[0];
		int port = Integer.parseInt(argv[1]);

		JFrame thisFrame = new JFrame();
		thisFrame.setSize(1024, 768);
		thisFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		RTPReceiver avReceive = new RTPReceiver(address, port);

		thisFrame.add(avReceive);

		thisFrame.setVisible(true);

		if (!avReceive.start()) {
			System.err.println("Failed to initialize the sessions.");
			System.exit(-1);
		}
	}

	static void prUsage() {
		System.err.println("Usage: AVReceive2 <session> <session> ...");
		System.err.println("     <session>: <address>/<port>/<ttl>");
		System.exit(0);
	}

}
