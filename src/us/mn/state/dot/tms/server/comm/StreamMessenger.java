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

import java.io.IOException;
import java.net.SocketAddress;
import java.net.Socket;

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
	private final int timeout;

	/** TCP socket */
	private Socket socket;

	/** Create a new stream messenger */
	public StreamMessenger(SocketAddress a, int rt) {
		address = a;
		timeout = rt;
	}

	/** Open the stream messenger */
	@Override
	public void open() throws IOException {
		Socket s = new Socket();
		s.setSoTimeout(timeout);
		s.connect(address, timeout);
		input = s.getInputStream();
		output = s.getOutputStream();
		socket = s;
	}

	/** Close the stream messenger */
	@Override
	public void close() {
		closeInput();
		closeOutput();
		closeSocket();
	}

	/** Close the socket */
	private void closeSocket() {
		Socket s = socket;
		if (s != null) {
			try {
				s.close();
			}
			catch (IOException e) {
				// Ignore
			}
		}
		socket = null;
	}
}
