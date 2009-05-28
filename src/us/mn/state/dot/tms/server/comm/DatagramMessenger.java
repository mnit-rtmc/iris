/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009  Minnesota Department of Transportation
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
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

/**
 * A DatagramMessenger is a class which can poll a field controller and get the
 * response using a UDP socket connection.
 *
 * @author Douglas Lau
 */
public class DatagramMessenger extends Messenger {

	/** Address to connect */
	protected final SocketAddress address;

	/** UDP socket */
	protected DatagramSocket socket;

	/** Receive timeout (ms) */
	protected int timeout = 750;

	/** Set the receive timeout */
	public void setTimeout(int t) throws IOException {
		timeout = t;
		DatagramSocket s = socket;
		if(s != null)
			s.setSoTimeout(t);
	}

	/** Create a new datagram messenger */
	public DatagramMessenger(SocketAddress a) {
		address = a;
	}

	/** Open the datagram messenger */
	public void open() throws IOException {
		socket = new DatagramSocket();
		socket.setSoTimeout(timeout);
		socket.connect(address);
		input = new DatagramInputStream();
		output = new DatagramOutputStream();
	}

	/** Close the datagram messenger */
	public void close() {
		DatagramSocket s = socket;
		if(s != null) {
			s.disconnect();
			s.close();
			socket = null;
		}
		input = null;
		output = null;
	}

	/** Output stream for sending datagrams */
	protected class DatagramOutputStream extends OutputStream {

		/** Buffer for assembling packets to send */
		protected final ByteBuffer buffer = ByteBuffer.allocate(1024);

		/** Packet to send */
		protected final DatagramPacket packet =
			new DatagramPacket(buffer.array(), 1024);

		/** Write a byte to the buffer */
		public void write(int b) {
			buffer.put((byte)b);
		}

		/** Flush packet to datagram */
		public void flush() throws IOException {
			packet.setLength(buffer.position());
			buffer.clear();
			DatagramSocket s = socket;
			if(s != null)
				s.send(packet);
		}
	}

	/** Input stream for receiving datagrams */
	protected class DatagramInputStream extends InputStream {

		/** Buffer for storing received datagram */
		protected final ByteBuffer buffer = ByteBuffer.allocate(1024);

		/** Packet to receive */
		protected final DatagramPacket packet =
			new DatagramPacket(buffer.array(), 1024);

		/** Create a new datagram input stream */
		protected DatagramInputStream() {
			// no data in buffer before a packet is received
			buffer.limit(0);
		}

		/** Read a byte from a received datagram */
		public int read() throws IOException {
			try {
				return buffer.get() & 0xFF;
			}
			catch(BufferUnderflowException e) {
				receivePacket();
				try {
					return buffer.get() & 0xFF;
				}
				catch(BufferUnderflowException e2) {
					throw new SocketTimeoutException("DIS");
				}
			}
		}

		/** Recvie and buffer a datagram */
		protected void receivePacket() throws IOException {
			DatagramSocket s = socket;
			if(s != null) {
				packet.setLength(1024);
				s.receive(packet);
				buffer.position(0);
				buffer.limit(packet.getLength());
			}
		}

		/** Get the number of available bytes */
		public int available() {
			return buffer.remaining();
		}

		/** Skip the given number of bytes in the input stream */
		public void skip(int b) {
			if(b >= buffer.remaining()) {
				buffer.clear();
				buffer.limit(0);
			} else
				buffer.position(buffer.position() + b);
		}
	}
}
