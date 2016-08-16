/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2016  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import us.mn.state.dot.tms.server.ControllerImpl;

/**
 * A StreamMessenger is a class which can poll a field controller and get the
 * response using a TCP socket connection.
 *
 * @author Douglas Lau
 */
public class StreamMessenger extends Messenger {

	/** Address to connect */
	private final SocketAddress address;

	/** Receive timeout (ms) */
	private final int recv_timeout;

	/** Connect timeout (ms) */
	private final int conn_timeout;

	/** TCP socket */
	private final Socket socket;

	/** Input stream */
	private final InputStream input;

	/** Output stream */
	private final OutputStream output;

	/** Create a new stream messenger.
	 * NOTE: must call setConnected to switch from conn_timeout to
	 *       recv_timeout. */
	public StreamMessenger(SocketAddress a, int rt, int ct)
		throws IOException
	{
		address = a;
		recv_timeout = rt;
		conn_timeout = ct;
		socket = new Socket();
		socket.setSoTimeout(conn_timeout);
		socket.connect(address, conn_timeout);
		input = socket.getInputStream();
		output = socket.getOutputStream();
	}

	/** Create a new stream messenger */
	public StreamMessenger(SocketAddress a, int rt) throws IOException {
		this(a, rt, rt);
	}

	/** Get the input stream.
	 * @param path Relative path name.
	 * @return An input stream for reading from the messenger. */
	@Override
	public InputStream getInputStream(String path) {
		return input;
	}

	/** Get the output stream */
	@Override
	public OutputStream getOutputStream(ControllerImpl c) {
		return output;
	}

	/** Drain any bytes from the input stream */
	@Override
	public void drain() throws IOException {
		int a = input.available();
		if (a > 0)
			input.skip(a);
	}

	/** Close the stream messenger */
	@Override
	public void close() throws IOException {
		socket.close();
	}

	/** Set the messenger to a connected state */
	public void setConnected() throws SocketException {
		socket.setSoTimeout(recv_timeout);
	}
}
