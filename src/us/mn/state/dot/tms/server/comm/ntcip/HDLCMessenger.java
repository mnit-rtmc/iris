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

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.Messenger;
import us.mn.state.dot.tms.server.comm.ProtocolException;

/**
 * HDLC Messenger
 *
 * @author Douglas Lau
 */
public class HDLCMessenger extends Messenger {

	/** Exception for null controller */
	static private final ProtocolException NULL_CONTROLLER =
		new ProtocolException("NULL CONTROLLER");

	/** Wrapped messenger */
	private final Messenger wrapped;

	/** Input stream */
	private final HDLC.FrameInputStream input;

	/** Output stream */
	private final HDLC.FrameOutputStream output;

	/** Create a new HDLC messenger */
	public HDLCMessenger(Messenger m) throws IOException {
		wrapped = m;
		input = new HDLC.FrameInputStream(wrapped.getInputStream(""));
		output = new HDLC.FrameOutputStream(wrapped.getOutputStream());
	}

	/** Close the messenger */
	@Override
	public void close() throws IOException {
		wrapped.close();
	}

	/** Get the input stream.
	 * @param path Relative path name.
	 * @return An input stream for reading from the messenger. */
	public InputStream getInputStream(String path) throws IOException {
		throw NULL_CONTROLLER;
	}

	/** Get an input stream for the specified controller */
	@Override
	public InputStream getInputStream(String path, ControllerImpl c)
		throws IOException
	{
		if (c != null) {
			int drop = c.getDrop();
			return new HDLC.AddressedInputStream(input, drop);
		} else
			throw NULL_CONTROLLER;
	}

	/** Get an output stream for the specified controller */
	@Override
	public OutputStream getOutputStream(ControllerImpl c)
		throws IOException
	{
		if (c != null) {
			int drop = c.getDrop();
			return new HDLC.AddressedOutputStream(output, drop);
		} else
			throw NULL_CONTROLLER;
	}

	/** Drain any bytes from the input stream */
	@Override
	public void drain() throws IOException {
		int a = input.available();
		if (a > 0)
			input.skip(a);
	}
}
