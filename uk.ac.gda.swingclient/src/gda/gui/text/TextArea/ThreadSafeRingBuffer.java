/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package gda.gui.text.TextArea;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.collections.buffer.CircularFifoBuffer;

public class ThreadSafeRingBuffer<E> {

	private CircularFifoBuffer buffer;
	
	public ThreadSafeRingBuffer(int capacity) {
		this.buffer = new CircularFifoBuffer(capacity);
	}
	
	public synchronized void add(E item) {
		buffer.add(item);
	}
	
	public synchronized int size() {
		return buffer.size();
	}
	
	public synchronized List<E> getContent() {
		Iterator<?> it = buffer.iterator();
		@SuppressWarnings("unchecked")
		List<E> items = IteratorUtils.toList(it);
		return items;
	}
	
	public synchronized void clear() {
		buffer.clear();
	}
	
}
