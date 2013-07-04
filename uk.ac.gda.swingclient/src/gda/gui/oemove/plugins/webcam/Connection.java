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

package gda.gui.oemove.plugins.webcam;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.CharBuffer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Connection Class
 */
public class Connection implements Runnable {
	private static final Logger logger = LoggerFactory.getLogger(Connection.class);

	private Thread runner;

	PropertyChangeSupport p = new PropertyChangeSupport(this);

	String boundary;

	BufferedInputStream stream;

	final static int max_frame = 1024 * 1024;

	byte[] block = new byte[max_frame];

	int offset = 0;

	int extra = 0;

	boolean done = false;

	/**
	 * @return size
	 */
	public int getSize() {
		logger.debug("buffer " + (offset - extra));
		return offset - extra;
	}

	interface State {
		// really no need for a state class - fix this
		// also is a CharSequence the best way to do this?
		/**
		 * @param line
		 * @return state
		 */
		State transition(CharSequence line);
	}

	class Reading implements State {
		// ready only after the first boundary
		boolean ready;

		Matcher bound = null;

		Reading(boolean ready) {
			// stream.mark here causes problems
			this.ready = ready;
		}

		@Override
		public Connection.State transition(CharSequence line) {
			if (bound == null) {
				bound = Pattern.compile(boundary).matcher(line);
			}
			if (bound.reset(line).lookingAt()) {
				if (ready) {
					// the buffer also contains --boundary at the end,
					// delete
					// line.length() bytes
					extra = line.length();
					try {
						stream.reset();
						stream.read(block, 0, offset - extra);
						// copy once here, then again later - fix this...
						stream.skip(extra);
						p.firePropertyChange("image", null, block);
					} catch (IOException e) {
						logger.debug("failed to rewind buffered stream" + e);
						// try again... then quit - have a counter
					}
				}
				return headers;
			}
			return this;
		}
	}

	class Headers implements State {
		Pattern cp = Pattern.compile("Content");

		Pattern bp = Pattern.compile("\\s*");

		Matcher blank = bp.matcher("");

		Matcher content = cp.matcher("");

		@Override
		public Connection.State transition(CharSequence line) {
			if (content.reset(line).lookingAt()) {
				logger.debug(line.toString());
				// content type should be image/jpeg
			} else if (blank.reset(line).matches()) {
				// end of headers
				stream.mark(max_frame);
				offset = 0;
				return reading;
			}
			return this;
		}
	}

	State state = new Reading(false);

	State reading = new Reading(true);

	State headers = new Headers();

	/**
	 * @param urlstring
	 */
	public Connection(String urlstring) {
		try {
			URL url = new URL(urlstring);
			URLConnection connection = url.openConnection();
			String type = connection.getHeaderField("Content-Type");
			String key = "boundary=";
			// note - may have quotes - fix this
			boundary = type.substring(type.indexOf(key) + key.length());
			stream = new BufferedInputStream(connection.getInputStream(), max_frame);
			runner = uk.ac.gda.util.ThreadManager.getThread(this);
			runner.setName(getClass().getName());
		} catch (Exception e) {
			logger.error("Exception" + e);
			e.printStackTrace();
		}
	}

	/**
	 * 
	 */
	public void go() {
		runner.start();
	}

	// only short lines can be headers or boundary
	final static int max_header = 64;

	// should read into a bytebuffer from the stream, take asCharBuffer for
	// strings...

	@Override
	public void run() {
		CharBuffer chars = CharBuffer.allocate(max_header);
		char last = ' ';
		try {
			while (true) {
				int ch = stream.read();
				if (ch == -1) {
					break;
				}
				offset++;
				if (chars.position() < chars.limit()) {
					chars.put((char) ch);
				}
				if (last == '\r' && ch == '\n') {
					chars.flip();
					state = state.transition(chars);
					logger.debug("state is " + state);
					chars.clear();
					if (done == true) {
						break;
					}
				}
				last = (char) ch;
			}
		} catch (Exception e) {
			logger.error("Exception in run" + e);
		}
	}

	/**
	 * @param name
	 * @param listener
	 */
	public void addPropertyChangeListener(String name, PropertyChangeListener listener) {
		p.addPropertyChangeListener(name, listener);
	}

}
