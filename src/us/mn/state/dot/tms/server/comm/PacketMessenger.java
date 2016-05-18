/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2015-2016  Minnesota Department of Transportation
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
import java.net.DatagramSocket;
import java.net.SocketAddress;

/**
 * A PacketMessenger is a class which can poll a field controller and get the
 * response using a UDP socket connection.
 *
 * @author Douglas Lau
 */
public class PacketMessenger extends Messenger {

	/** Remote address to connect */
	private final SocketAddress remote;

	/** Receive timeout (ms) */
	private final int timeout;

	/** UDP socket */
	private final DatagramSocket socket;

	/** Get the UDP socket */
	public DatagramSocket getSocket() {
		return socket;
	}

	/** Create a new datagram packet messenger.
	 * @param ra Remote socket address.
	 * @param rt Read timeout (ms). */
	public PacketMessenger(SocketAddress ra, int rt) throws IOException {
		remote = ra;
		timeout = rt;
		socket = new DatagramSocket();
		socket.setSoTimeout(timeout);
		socket.connect(remote);
	}

	/** Open the messenger */
	@Override
	public void open() {
		// need to open in constructor due to async receive
	}

	/** Close the datagram packet messenger */
	@Override
	public void close() {
		socket.disconnect();
		socket.close();
	}
}
