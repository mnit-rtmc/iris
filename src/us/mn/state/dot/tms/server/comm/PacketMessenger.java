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

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.net.URI;
import us.mn.state.dot.tms.server.ControllerImpl;

/**
 * A PacketMessenger is a class which can poll a field controller and get the
 * response using a UDP socket connection.
 *
 * @author Douglas Lau
 */
public class PacketMessenger extends Messenger {

	/** Default URI for UDP sockets */
	static private final URI UDP = URI.create("udp:/");

	/** Create a packet messenger.
	 * @param uri URI of remote host.
	 * @param rt Receive timeout (ms). */
	static public PacketMessenger create(String uri, int rt)
		throws MessengerException
	{
		URI u = createURI(UDP, uri);
		if ("udp".equals(u.getScheme()))
			return createPacketMessenger(u, rt);
		else
			throw INVALID_URI_SCHEME;
	}

	/** Create a packet datagram messenger */
	static private PacketMessenger createPacketMessenger(URI u, int rt)
		throws MessengerException
	{
		try {
			return new PacketMessenger(createSocketAddress(u), rt);
		}
		catch (IOException e) {
			throw new MessengerException(e);
		}
	}

	/** Exception for input / output streams */
	static private final ProtocolException PKT_STREAM =
		new ProtocolException("NO STREAM");

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

	/** Get the input stream.
	 * @param path Relative path name.
	 * @return An input stream for reading from the messenger. */
	@Override
	public InputStream getInputStream(String path) throws IOException {
		throw PKT_STREAM;
	}

	/** Get the output stream */
	@Override
	public OutputStream getOutputStream(ControllerImpl c)
		throws IOException
	{
		throw PKT_STREAM;
	}

	/** Close the datagram packet messenger */
	@Override
	public void close() {
		socket.disconnect();
		socket.close();
	}

	/** Drain any bytes from the input stream */
	@Override
	public void drain() {
		// nothing to do
	}
}
