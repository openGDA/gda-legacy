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

import gda.util.exceptionUtils;

import java.net.ServerSocket;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.net.SimpleSocketServer;
import ch.qos.logback.classic.net.SocketNode;

/**
 * A simple {@link SocketNode} based server.
 */
public class ClientSideLogService implements Runnable {

	static Logger logger = LoggerFactory.getLogger(ClientSideLogService.class);

	int port;

	private final LoggerContext lc;
	SimpleSocketServer socketServer;

	/**
	 * @param port
	 * @param lc
	 */
	public ClientSideLogService(int port, LoggerContext lc) {
		this.port = port;
		this.lc = lc;
		socketServer = new SimpleSocketServer(lc, port);
	}

	@Override
	public void run() {
		try {
			logger.info("Listening on port {}", port);
			
			@SuppressWarnings("resource") // suppressed because once the socket has been created, we go into an infinite loop
			ServerSocket serverSocket = new ServerSocket(port);
			
			while (true) {
				logger.info("Waiting to accept a new client.");
				Socket socket = serverSocket.accept();
				logger.info("Connected to client at {}", socket.getInetAddress());
				logger.info("Starting new socket node.");
				// maybe create a different context so that remote events are
				// treated differently to local ones.
				
				// The SocketNode constructor used here blocks waiting for data
				// from the client, so this loop won't continue until some data
				// is received. See Logback issue LBCLASSIC-123
				uk.ac.gda.util.ThreadManager.getThread(new SocketNode(socketServer, socket, lc)).start();
			}
		} catch (Exception e) {
			exceptionUtils.logException(logger, "Error in run", e);
		}

	}
}
