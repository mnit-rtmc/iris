/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2018  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.cux50;

import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;

/**
 * Connection state information for a Panasonic camera keyboard.
 *
 * @author Douglas Lau
 */
public class Connection {

	/** Buffer size */
	static private final int BUF_SZ = 1 << 12;

	/** Protocol handler */
	private final ProtocolHandler handler;

	/** Transmit buffer */
	private final ByteBuffer tx_buf;

	/** Receive buffer */
	private final ByteBuffer rx_buf;

	/** Create a connection */
	public Connection(ProtocolHandler ph) {
		handler = ph;
		tx_buf = ByteBuffer.allocate(BUF_SZ);
		rx_buf = ByteBuffer.allocate(BUF_SZ);
	}

	/** Get the transmit buffer */
	public ByteBuffer getTxBuffer() {
		return tx_buf;
	}

	/** Get the receive buffer */
	public ByteBuffer getRxBuffer() {
		return rx_buf;
	}

	/** Check if the transmit buffer needs writing */
	private boolean needsWrite() {
		synchronized (tx_buf) {
			return tx_buf.position() > 0;
		}
	}

	/** Get current selection key interest ops */
	public int getInterest() {
		return needsWrite()
		     ? SelectionKey.OP_READ | SelectionKey.OP_WRITE
		     : SelectionKey.OP_READ;
	}

	/** Handle receive data from a socket address */
	public void handleReceive(SocketAddress sa) {
		byte[] rcv = getReceived();
		byte[] snd = handler.handleReceive(sa, rcv);
		putSend(snd);
	}

	/** Copy data from receive buffer */
	private byte[] getReceived() {
		synchronized (rx_buf) {
			rx_buf.flip();
			byte[] rcv = new byte[rx_buf.remaining()];
			rx_buf.get(rcv);
			rx_buf.compact();
			return rcv;
		}
	}

	/** Copy to transmit buffer */
	private void putSend(byte[] snd) {
		synchronized (tx_buf) {
			tx_buf.put(snd);
		}
	}
}
