/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2016  Minnesota Department of Transportation
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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import us.mn.state.dot.tms.server.ControllerImpl;

/**
 * A DatagramMessenger is a class which can poll a field controller and get the
 * response using a UDP socket connection.
 *
 * @author Douglas Lau
 */
public class DatagramMessenger extends Messenger {

	/** Create a UDP datagram messenger.
	 * @param u URI of remote host.
	 * @param rt Receive timeout (ms). */
	static protected DatagramMessenger create(URI u, int rt)
		throws MessengerException, IOException
	{
		return new DatagramMessenger(createSocketAddress(u), rt);
	}

	/** Local port to bind */
	private final Integer port;

	/** Remote address to connect */
	private final SocketAddress remote;

	/** Receive timeout (ms) */
	private final int timeout;

	/** UDP socket */
	private final DatagramSocket socket;

	/** Input stream */
	private final DatagramInputStream input;

	/** Output stream */
	private final DatagramOutputStream output;

	/** Create a new datagram messenger.
	 * @param p Local port (null for any).
	 * @param ra Remote socket address.
	 * @param rt Read timeout (ms). */
	private DatagramMessenger(Integer p, SocketAddress ra, int rt)
		throws IOException
	{
		port = p;
		remote = ra;
		timeout = rt;
		socket = createSocket();
		socket.setSoTimeout(timeout);
		socket.connect(remote);
		input = new DatagramInputStream();
		output = new DatagramOutputStream();
	}

	/** Create a new datagram messenger.
	 * @param ra Remote socket address.
	 * @param rt Read timeout (ms). */
	public DatagramMessenger(SocketAddress ra, int rt) throws IOException {
		this(null, ra, rt);
	}

	/** Create the socket */
	private DatagramSocket createSocket() throws IOException {
		return (port != null)
		      ? new DatagramSocket(port)
		      : new DatagramSocket();
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

	/** Close the datagram messenger */
	@Override
	public void close() {
		socket.disconnect();
		socket.close();
	}

	/** Output stream for sending datagrams */
	private class DatagramOutputStream extends OutputStream {

		/** Buffer for assembling packets to send */
		private final ByteBuffer buffer = ByteBuffer.allocate(1024);

		/** Packet to send */
		private final DatagramPacket packet =
			new DatagramPacket(buffer.array(), 1024);

		/** Write a byte to the buffer */
		@Override
		public void write(int b) {
			buffer.put((byte)b);
		}

		/** Flush packet to datagram */
		@Override
		public void flush() throws IOException {
			packet.setLength(buffer.position());
			buffer.clear();
			socket.send(packet);
		}
	}

	/** Input stream for receiving datagrams */
	private class DatagramInputStream extends InputStream {

		/** Buffer for storing received datagram */
		private final ByteBuffer buffer = ByteBuffer.allocate(1024);

		/** Packet to receive */
		private final DatagramPacket packet =
			new DatagramPacket(buffer.array(), 1024);

		/** Create a new datagram input stream */
		private DatagramInputStream() {
			// no data in buffer before a packet is received
			buffer.limit(0);
		}

		/** Read a byte from a received datagram */
		@Override
		public int read() throws IOException {
			try {
				return buffer.get() & 0xFF;
			}
			catch (BufferUnderflowException e) {
				receivePacket();
				try {
					return buffer.get() & 0xFF;
				}
				catch (BufferUnderflowException e2) {
					throw new SocketTimeoutException("DIS");
				}
			}
		}

		/** Recvie and buffer a datagram */
		private void receivePacket() throws IOException {
			packet.setLength(1024);
			socket.receive(packet);
			buffer.position(0);
			buffer.limit(packet.getLength());
		}

		/** Get the number of available bytes */
		@Override
		public int available() {
			return buffer.remaining();
		}

		/** Skip the given number of bytes in the input stream */
		public void skip(int b) {
			if (b >= buffer.remaining()) {
				buffer.clear();
				buffer.limit(0);
			} else
				buffer.position(buffer.position() + b);
		}
	}
}
