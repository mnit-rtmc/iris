/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2012  Iteris Inc.
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
package us.mn.state.dot.tms.server.comm.g4;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketTimeoutException;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.ControllerProperty;

/**
 * A property which can be sent or received from a controller.
 *
 * @author Michael Darter
 */
abstract public class G4Property extends ControllerProperty {

	/** Controller associated with property */
	private final ControllerImpl controller;

	/** Record read from field controller */
	private final G4Rec g4_rec;

	/** Maximum number of bytes in a response */
	static protected final int MAX_RESP = 512;

	/** Format a get request */
	abstract protected G4Blob formatGetRequest() throws IOException;

	/** Sensor id */
	final int sensor_id;

	/** Controller timeout */
	final int time_out;

	/** Constructor */
	G4Property(ControllerImpl c, G4Rec r) {
		controller = c;
		g4_rec = r;
		sensor_id = c.getDrop();
		time_out = c.getCommLink().getTimeout();
		G4Poller.info("G4Property.G4Property(sid=" + sensor_id +
			", timeout=" + time_out);
	}

	/** Perform a get request, which consists of sending a request to 
	 * the sensor and reading the reply. Called by G4Message.
	 * @throws IOException */
	protected void doGetRequest(OutputStream os, InputStream is) 
		throws IOException 
	{
		G4Poller.info("G4Property.sendRequest(): called");
		is.skip(is.available());
		sendRequest(os, formatGetRequest());
		getResponse(is);
	}

	/** Write a request to the sensor */
	protected void sendRequest(OutputStream os, G4Blob req)
		throws IOException
	{
		G4Poller.info("G4Property.sendRequest() called: req=" + req);
		os.write(req.toArray());
		os.flush();
		G4Poller.info("G4Property.sendRequest(): wrote to G4");
	}

	/** Read response from the sensor */
	private void getResponse(InputStream is) throws IOException {
		G4Poller.info("G4Property.getResponse() called");
		G4Blob b = read(is, time_out);
		G4Poller.info("G4Property.getResponse() read done, #bytes=" + 
			b.size());
		g4_rec.parse(b);
	}

	/** Read bytes from input stream, blocking until a timeout, a complete
	 * message is read, or an exception is thrown. The first 2 bytes read 
	 * must match the expected leader bytes or an exception is thrown.
	 * @param is Input stream to read from.
	 * @param to Timeout in ms.
	 * @return Bytes read from the field device.
	 * @throws IOException, e.g. on timeout */
	static private G4Blob read(InputStream is, int to)
		throws IOException
	{
		G4Poller.info("G4Property.read() called, timeout=" + to);
		G4Blob blob = new G4Blob();
		while(dataAvailable(is, to)) {
			int b = is.read(); // throws IOException
			if(b < 0)
				return blob;
			else if(b >= 0 && b <= 255) {
				blob.add(b);
				// first 2 bytes received must be the header
				if(blob.size() == 2 && !blob.validLeader(0))
					throw new IOException("bad header");
				if(blob.readComplete())
					return blob;
			}
		}
		G4Poller.info("G4Property.read(): timeout after reading " + 
			blob.size() + " bytes");
		throw new SocketTimeoutException("waiting for msg end");
	}

	/** Block until data is available on the specified input stream or 
	 * the specified timeout expires.
	 * @param is Input stream to read from.
	 * @param timeout Timeout in milliseconds.
	 * @throws IOException
	 * @returns True if data is available else false on timeout. */
	static private boolean dataAvailable(InputStream is, int timeout)
		throws IOException
	{
		final long st = TimeSteward.currentTimeMillis();
		while(true) {
			if(is.available() > 0)
				return true;
			if(TimeSteward.currentTimeMillis() - st >= timeout)
				return (is.available() > 0);
			G4Poller.sleepy(250);
		}
	}
}
