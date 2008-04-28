/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2008  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.comm;

import java.io.IOException;
import java.net.SocketAddress;
import java.net.Socket;

/**
 * A SocketMessenger is a class which can poll a field controller and get the
 * response using a TCP socket connection.
 *
 * @author Douglas Lau
 */
public class SocketMessenger extends Messenger {

	/** Address to connect */
	protected final SocketAddress address;

	/** TCP socket */
	protected Socket socket;

	/** Receive timeout (ms) */
	protected int timeout = 750;

	/** Set the receive timeout */
	public synchronized void setTimeout(int t) throws IOException {
		timeout = t;
		if(socket != null)
			socket.setSoTimeout(t);
	}

	/** Create a new socket messenger */
	public SocketMessenger(SocketAddress a) {
		address = a;
	}

	/** Open the socket messenger */
	public void open() throws IOException {
		socket = new Socket();
		socket.setSoTimeout(timeout);
		socket.connect(address, timeout);
		input = socket.getInputStream();
		output = socket.getOutputStream();
	}

	/** Close the socket messenger */
	public synchronized void close() {
		if(socket != null) {
			try {
				socket.close();
			}
			catch(IOException e) {
				// Ignore
			}
		}
		socket = null;
		input = null;
		output = null;
	}
}
