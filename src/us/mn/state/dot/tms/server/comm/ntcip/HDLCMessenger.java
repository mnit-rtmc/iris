/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2016  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.ntcip;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.Messenger;

/**
 * HDLC Messenger
 *
 * @author Douglas Lau
 */
public class HDLCMessenger extends Messenger {

	/** Wrapped messenger */
	protected final Messenger wrapped;

	/** Create a new HDLC messenger */
	public HDLCMessenger(Messenger m) {
		wrapped = m;
	}

	/** Set the messenger timeout */
	@Override
	public void setTimeout(int t) throws IOException {
		wrapped.setTimeout(t);
	}

	/** Get the receive timeout */
	@Override
	public int getTimeout() {
		return wrapped.getTimeout();
	}

	/** Open the messenger */
	@Override
	public void open() throws IOException {
		wrapped.open();
		output = new HDLC.FrameOutputStream(wrapped.getOutputStream());
		input = new HDLC.FrameInputStream(wrapped.getInputStream(""));
	}

	/** Close the messenger */
	@Override
	public void close() {
		wrapped.close();
		output = null;
		input = null;
	}

	/** Get an input stream for the specified controller */
	@Override
	public InputStream getInputStream(String path, ControllerImpl c)
		throws IOException
	{
		InputStream _input = input;	// Avoid races
		if(_input != null) {
			int drop = c.getDrop();
			return new HDLC.AddressedInputStream(_input, drop);
		} else
			throw new EOFException("MESSENGER CLOSED");
	}

	/** Get an output stream for the specified controller */
	@Override
	public OutputStream getOutputStream(ControllerImpl c)
		throws IOException
	{
		OutputStream _output = output;	// Avoid races
		if (_output != null) {
			int drop = c.getDrop();
			return new HDLC.AddressedOutputStream(_output, drop);
		} else
			throw new EOFException("MESSENGER CLOSED");
	}
}
