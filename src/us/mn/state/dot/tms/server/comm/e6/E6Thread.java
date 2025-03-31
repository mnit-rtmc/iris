/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2015-2024  Minnesota Department of Transportation
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

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;
import java.net.URI;
import us.mn.state.dot.sched.DebugLog;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.server.TagReaderImpl;
import us.mn.state.dot.tms.server.comm.CommThread;
import us.mn.state.dot.tms.server.comm.Messenger;
import us.mn.state.dot.tms.server.comm.MessengerException;
import us.mn.state.dot.tms.server.comm.OpController;
import us.mn.state.dot.tms.server.comm.OpQueue;
import us.mn.state.dot.tms.server.comm.PacketMessenger;

/**
 * A thread to communicate with TransCore E6 tag readers.
 *
 * @author Douglas Lau
 */
public class E6Thread extends CommThread<E6Property> {

	/** Tag response */
	static private final Command TAG_RESPONSE = new Command(
		CommandGroup.MODE);

	/** Thread group for all receive threads */
	static private final ThreadGroup RECV = new ThreadGroup("Recv");

	/** E6 debug log */
	static private final DebugLog E6_LOG = new DebugLog("e6");

	/** Packet timeout (ms) */
	private final int timeout;

	/** Get the packet timeout */
	public int getTimeout() {
		return timeout;
	}

	/** Thread to receive packets */
	private final Thread rx_thread;

	/** E6 poller */
	private final E6Poller poller;

	/** Transmit Packet */
	private E6Packet tx_pkt;

	/** Receive Packet */
	private E6Packet rx_pkt;

	/** Response Packet */
	private E6Packet resp_pkt;

	/** Create a new E6 thread */
	public E6Thread(E6Poller dp, OpQueue<E6Property> q, URI s, String u,
		int rt, int nrd)
	{
		super(dp, q, s, u, rt, nrd, E6_LOG);
		poller = dp;
		timeout = rt;
 		rx_thread = new Thread(RECV, "Recv: " + dp.name) {
			@Override
			public void run() {
				receivePackets();
			}
		};
		rx_thread.setDaemon(true);
	}

	/** Start the thread */
	@Override
	public void start() {
		super.start();
		rx_thread.start();
	}

	/** Destroy the comm thread */
	@Override
	public void destroy() {
		rx_thread.interrupt();
		super.destroy();
	}

	/** Create a messenger.
	 * @param s Default URI scheme.
	 * @param u The URI.
	 * @param rt Receive timeout (ms).
	 * @return The new messenger.
	 * @throws MessengerException if the messenger could not be created. */
	@Override
	protected Messenger createMessenger(URI s, String u, int rt, int nrd)
		throws MessengerException
	{
		PacketMessenger m = PacketMessenger.create(u, rt);
		DatagramSocket ds = m.getSocket();
		tx_pkt = new E6Packet(ds, false);
		rx_pkt = new E6Packet(ds, true);
		resp_pkt = new E6Packet(ds, true);
		return m;
	}

	/** Create a message for the specified operation.
	 * @param m The messenger.
	 * @param o The operation.
	 * @return New comm message. */
	@Override
	protected E6Message createCommMessage(Messenger m,
		OpController<E6Property> o) throws IOException
	{
		return new E6Message(m, o, E6_LOG, this);
	}

	/** Receive packets */
	private void receivePackets() {
		try {
			while (receivePacket());
		}
		catch (IOException e) {
			logException(e);
		}
		finally {
			destroy();
		}
	}

	/** Receive one packet */
	private boolean receivePacket() throws IOException {
		if (rx_thread.isInterrupted())
			return false;
		E6Packet rx = rx_pkt;
		E6Packet tx = tx_pkt;
		E6Packet rp = resp_pkt;
		if (rx == null || tx == null || rp == null) {
			try {
				TimeSteward.sleep(200);
				return true;
			}
			catch (InterruptedException e) {
				return false;
			}
		}
		try {
			doReceivePacket(rx, tx, rp);
		}
		catch (SocketTimeoutException e) {
			// we expect lots of timeouts
		}
		return true;
	}

	/** Receive one packet */
	private void doReceivePacket(E6Packet rx, E6Packet tx, E6Packet rp)
		throws IOException
	{
		rx.receive();
		Command cmd = rx.parseCommand();
		if (cmd.acknowledge)
			return;
		else
			sendAck(rx, tx, cmd);
		rx.advanceMsn();
		if (rp.checkResponse(rx))
			return;
		if (cmd.equals(TAG_RESPONSE)) {
			Response rsp = rx.parseResponse();
			if (rsp == Response.COMMAND_COMPLETE)
				logTagTransaction(rx);
		}
	}

	/** Send an ack packet */
	private void sendAck(E6Packet rx, E6Packet tx, Command cmd)
		throws IOException
	{
		Command c = new Command(cmd.group, false, true);
		byte[] data = new byte[3];
		data[0] = (byte) (Response.ACK.bits() >> 8);
		data[1] = (byte) (Response.ACK.bits() >> 0);
		data[2] = rx.parseMsn();
		tx.send(c, data);
	}

	/** Log a real-time tag transaction */
	private void logTagTransaction(E6Packet rx) throws IOException {
		byte[] data = rx.parseData();
		if (data.length > 2) {
			TagTransaction tt = new TagTransaction(data, 2,
				data.length - 2);
			TagReaderImpl tr = poller.getReader();
			if (tr != null)
				tt.logRead(tr);
			if (E6_LOG.isOpen())
				E6_LOG.log(readerId() + ": " + tt.toString());
		}
	}

	/** Get the reader ID */
	private String readerId() {
		TagReaderImpl tr = poller.getReader();
		return (tr != null) ? tr.getName() : "reader unknown";
	}

	/** Send a store packet */
	public void sendStore(E6Property p) throws IOException {
		E6Packet rp = resp_pkt;
		E6Packet tx = tx_pkt;
		if (rp != null && tx != null) {
			byte[] data = p.storeData();
			PendingCommand pc = sendPacket(tx, p.command(), data);
			byte[] resp = rp.waitData(getTimeout(), pc);
			p.parseStore(resp);
		}
	}

	/** Send a packet */
	private PendingCommand sendPacket(E6Packet tx, Command cmd, byte[] data)
		throws IOException
	{
		tx.send(cmd, data);
		return new PendingCommand(cmd, getSubCmd(cmd, data));
	}

	/** Get the sub-command */
	private int getSubCmd(Command cmd, byte[] data) {
		// FIXME: get sub-command from property?
		switch (cmd.group) {
		case RF_TRANSCEIVER:
			return data[0] & 0xFF;
		default:
			return ((data[0] << 8) & 0xFF) | (data[1] & 0xFF);
		}
	}

	/** Send a query packet */
	public void sendQuery(E6Property p) throws IOException {
		E6Packet rp = resp_pkt;
		E6Packet tx = tx_pkt;
		if (rp != null && tx != null) {
			byte[] data = p.queryData();
			PendingCommand pc = sendPacket(tx, p.command(), data);
			byte[] resp = rp.waitData(getTimeout(), pc);
			p.parseQuery(resp);
		}
	}
}
