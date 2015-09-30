/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2015  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.e6;

import java.io.EOFException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;
import us.mn.state.dot.sched.DebugLog;
import us.mn.state.dot.tms.server.comm.ChecksumException;
import us.mn.state.dot.tms.server.comm.ParsingException;
import us.mn.state.dot.tms.utils.HexString;

/**
 * E6 packet.
 *
 * @author Douglas Lau
 */
public class E6Packet {

	/** E6 pkt debug log */
	static private final DebugLog E6_PKT_LOG = new DebugLog("e6_pkt");

	/** Timeout exception */
	static private final IOException TIMEOUT =
		new SocketTimeoutException("TIMEOUT");

	/** Exception thrown when stream is closed */
	static private final EOFException CLOSED = new EOFException("CLOSED");

	/** Log a packet */
	private void log(String x) {
		if (E6_PKT_LOG.isOpen()) {
			E6_PKT_LOG.log(x + datagram.getSocketAddress() + ' ' +
				toString());
		}
	}

	/** Datagram socket */
	private final DatagramSocket socket;

	/** Flag for rx packets */
	private final boolean rx;

	/** Packet buffer */
	private final byte[] pkt = new byte[128];

	/** Datagram for UDP send/recv */
	private final DatagramPacket datagram =
		new DatagramPacket(pkt, pkt.length);

	/** Number of bytes in packet */
	private int n_bytes = 0;

	/** Message sequence number */
	private byte msn = 0;

	/** Command sequence number (SYSTEM_INFO CommandGroup only) */
	private byte csn = 0;

	/** Create a new E6 packet */
	public E6Packet(DatagramSocket s, boolean r) {
		socket = s;
		rx = r;
	}

	/** Send the packet */
	public synchronized void send(Command cmd, byte[] data)
		throws IOException
	{
		if (!cmd.acknowledge) {
			updateMsn();
			if (cmd.group == CommandGroup.SYSTEM_INFO)
				csn++;
		}
		format(cmd, data);
		datagram.setLength(n_bytes);
		socket.send(datagram);
		log("tx");
	}

	/** Format command packet */
	private void format(Command cmd, byte[] data) {
		n_bytes = data.length + 7;
		pkt[0] = (byte) ((n_bytes >> 8) & 0xFF);
		pkt[1] = (byte) ((n_bytes >> 0) & 0xFF);
		pkt[2] = msn;
		int b = cmd.bits();
		pkt[3] = (byte) ((b >> 8) & 0xFF);
		pkt[4] = (byte) ((b >> 0) & 0xFF);
		if (cmd.group == CommandGroup.SYSTEM_INFO)
			pkt[5] = (byte) (csn & 0xFF);
		else
			pkt[5] = 0;
		System.arraycopy(data, 0, pkt, 6, data.length);
		int xsum = 0;
		for (int i = 0; i < n_bytes - 1; i++)
			xsum += pkt[i];
		pkt[n_bytes - 1] = (byte) (xsum & 0xFF);
	}

	/** Pending command */
	private PendingCommand pending;

	/** Check for a response to a pending command */
	public synchronized boolean checkResponse(E6Packet p)
		throws IOException
	{
		if (pending != null) {
			Response rsp = p.parseResponse();
			if (rsp == Response.COMMAND_COMPLETE) {
				Command cmd = p.parseCommand();
				if (cmd.equals(pending.cmd) &&
				    p.parseSubCmd(cmd) == pending.sub_cmd)
				{
					copy(p);
					return true;
				}
			}
		}
		return false;
	}

	/** Copy from another packet */
	private void copy(E6Packet p) {
		pending = null;
		System.arraycopy(p.pkt, 0, pkt, 0, pkt.length);
		n_bytes = p.n_bytes;
		msn = p.msn;
		csn = p.csn;
		notify();
	}

	/** Wait for a response to a pending command */
	public synchronized byte[] waitData(int timeout, PendingCommand pc)
		throws IOException
	{
		try {
			pending = pc;
			try {
				wait(timeout);
			}
			catch (InterruptedException e) {
				// doesn't matter
			}
			if (pending != null)
				throw TIMEOUT;
			return parseData();
		}
		finally {
			pending = null;
		}
	}

	/** Parse the packet data */
	public byte[] parseData() throws IOException {
		if (n_bytes >= 7) {
			byte[] data = new byte[n_bytes - 7];
			System.arraycopy(pkt, 6, data, 0, data.length);
			return data;
		}
		throw new ParsingException("BAD LEN: " + n_bytes);
	}

	/** Update the message sequence number (MSN) */
	private void updateMsn() {
		msn = (byte) (parseMsn() + 1);
	}

	/** Advance the message sequence number (MSN) */
	public void advanceMsn() {
		if (parseMsn() != msn)
			E6_PKT_LOG.log("rx ** msn seq ERR **");
		updateMsn();
	}

	/** Receive one packet */
	public void receive() throws IOException {
		datagram.setLength(128);
		socket.receive(datagram);
		n_bytes = datagram.getLength();
		if (n_bytes < 0)
			throw CLOSED;
		int n_len = (pkt[0] << 8) | pkt[1];
		if (n_bytes < 7 || n_len != n_bytes)
			throw new ParsingException("BAD LEN: " + n_len);
		int xsum = 0;
		for (int i = 0; i < n_bytes - 1; i++)
			xsum += pkt[i];
		xsum &= 0xFF;
		if (xsum != (pkt[n_bytes - 1] & 0xFF))
			throw new ChecksumException(pkt);
		log("rx");
	}

	/** Parse the command */
	public Command parseCommand() throws IOException {
		int c = parseCmd();
		Command cmd = Command.create(c);
		if (cmd != null)
			return cmd;
		else
			throw new ParsingException("BAD CMD: " + c);
	}

	/** Parse the command */
	private int parseCmd() {
		return ((pkt[3] & 0xFF) << 8) | (pkt[4] & 0xFF);
	}

	/** Parse the message sequence number */
	public byte parseMsn() {
		return pkt[2];
	}

	/** Parse the response/status field */
	public Response parseResponse() throws IOException {
		if (n_bytes >= 9) {
			int r = parseResp();
			Response rsp = Response.fromBits(r);
			if (rsp != null)
				return rsp;
			else
				throw new ParsingException("BAD RESP: " + r);
		} else
			throw new ParsingException("BAD LEN: " + n_bytes);
	}

	/** Parse the response / status field */
	private int parseResp() {
		if (n_bytes >= 9)
			return ((pkt[6] & 0xFF) << 8) | (pkt[7] & 0xFF);
		else
			return 0;
	}

	/** Parse the sub-command field */
	private int parseSubCmd(Command cmd) {
		switch (cmd.group) {
		case RF_TRANSCEIVER:
			if (n_bytes >= 9)
				return pkt[8] & 0xFF;
			break;
		default:
			if (n_bytes >= 9)
				return ((pkt[8] & 0xFF) << 8) | (pkt[9] & 0xFF);
			break;
		}
		return 0;
	}

	/** Get a string representation */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(HexString.format(pkt, n_bytes, ':'));
		Command cmd = Command.create(parseCmd());
		if (cmd != null) {
			sb.append(' ');
			sb.append(cmd);
		}
		if (rx) {
			Response rsp = Response.fromBits(parseResp());
			if (rsp != null) {
				sb.append(' ');
				sb.append(rsp);
			}
		}
		return sb.toString();
	}
}
