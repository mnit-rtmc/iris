/*
 * Copyright (C) 2002-2010  Minnesota Department of Transportation
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */
package us.mn.state.dot.tms.client.camera.stream;

import java.util.LinkedList;

/**
 * An abstract implementation of a DataSource.
 *
 * @author Timothy Johnson
 * @author Douglas Lau
 */
abstract public class AbstractDataSource extends Thread implements DataSource {

	/** Flag to indicate the stream is done */
	protected boolean done = false;

	/** Halt the stream */
	public void halt() {
		done = true;
	}

	/** List of DataSinks for this stream */
	private LinkedList<DataSink> sinks = new LinkedList<DataSink>();

	/** Notify listeners that an image was created */
	protected synchronized void notifySinks(byte[] data) {
		for(DataSink sink: sinks)
			sink.flush(data);
	}

	/** Add a DataSink to this Image Factory. */
	public synchronized void connectSink(DataSink sink) {
		if(sink != null)
			sinks.add(sink);
	}

	/** Remove a DataSink from this DataSource */
	public synchronized void disconnectSink(DataSink sink) {
		sinks.remove(sink);
		if(sinks.size() == 0)
			halt();
	}

	/** Remove all connected data sinks */
	protected synchronized void removeSinks() {
	 	sinks.clear();
		halt();
	}
}
