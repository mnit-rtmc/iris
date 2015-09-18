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

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.SocketTimeoutException;
import us.mn.state.dot.sched.DebugLog;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.server.TagReaderImpl;
import us.mn.state.dot.tms.server.comm.MessagePoller;
import us.mn.state.dot.tms.server.comm.PacketMessenger;
import us.mn.state.dot.tms.server.comm.TagReaderPoller;

/**
 * A Poller to communicate with TransCore E6 tag readers.
 *
 * @author Douglas Lau
 */
public class E6Poller extends MessagePoller implements TagReaderPoller {

	/** Timeout exception */
	static private final IOException TIMEOUT =
		new SocketTimeoutException("TIMEOUT");

	/** Local port */
	static public final int LOCAL_PORT = 58001;

	/** Thread group for all receive threads */
	static private final ThreadGroup RECV = new ThreadGroup("Recv");

	/** E6 debug log */
	static public final DebugLog E6_LOG = new DebugLog("e6");

	/** Log a packet */
	private void log(String x, E6Packet pkt) {
		if (E6_LOG.isOpen())
			E6_LOG.log(name + ' ' + x + ' ' + pkt.toString());
	}

	/** Packet messenger */
	private final PacketMessenger pkt_mess;

	/** Poller name */
	private final String name;

	/** Thread to receive packets */
	private final Thread rx_thread;

	/** Transmit Packet */
	private final E6Packet tx_pkt;

	/** Receive Packet */
	private final E6Packet rx_pkt;

	/** Response Packet */
	private final E6Packet resp_pkt;

	/** Waiting flag */
	private boolean waiting = false;

	/** Create a new E6 poller */
	public E6Poller(String n, PacketMessenger m) {
		super(n, m);
		name = n;
		pkt_mess = m;
		tx_pkt = new E6Packet(m, false);
		rx_pkt = new E6Packet(m, true);
		resp_pkt = new E6Packet(m, true);
 		rx_thread = new Thread(RECV, "Recv: " + n) {
			@Override
			public void run() {
				receivePackets();
			}
		};
		rx_thread.setDaemon(true);
	}

	/** Start polling */
	@Override
	protected void startPolling() {
		super.startPolling();
		rx_thread.start();
	}

	/** Stop polling */
	@Override
	protected void stopPolling() {
		rx_thread.interrupt();
		super.stopPolling();
	}

	/** Receive packets */
	private void receivePackets() {
		try {
			while (!rx_thread.isInterrupted()) {
				receivePacket();
			}
		}
		catch (IOException e) {
			setStatus(exceptionMessage(e));
		}
		finally {
			stopPolling();
		}
	}

	/** Receive one packet */
	private void receivePacket() throws IOException {
		try {
			doReceivePacket();
		}
		catch (SocketTimeoutException e) {
			// we expect lots of timeouts
		}
	}

	/** Receive one packet */
	private void doReceivePacket() throws IOException {
		rx_pkt.receive();
		log("rx", rx_pkt);
		Command cmd = rx_pkt.parseCommand();
		if (cmd.acknowledge)
			return;
		else
			sendAck(cmd);
		if (rx_pkt.parseMsn() != rx_pkt.getMsn()) {
			if (E6_LOG.isOpen())
				E6_LOG.log("rx *** msn seq ERROR ***");
		}
		rx_pkt.updateMsn();
		if (cmd.unsolicited) {
			// FIXME: log tag read
			return;
		} else if (waiting) {
			Response rsp = rx_pkt.parseResponse();
			if (rsp == Response.COMMAND_COMPLETE) {
				waiting = false;
				resp_pkt.copy(rx_pkt);
			}
		}
	}

	/** Send an ack packet */
	private void sendAck(Command cmd) throws IOException {
		Command c = new Command(cmd.group, false, true);
		byte[] data = new byte[3];
		data[0] = (byte) (Response.ACK.bits() >> 8);
		data[1] = (byte) (Response.ACK.bits() >> 0);
		data[2] = rx_pkt.parseMsn();
		synchronized (tx_pkt) {
			tx_pkt.format(c, data);
			log("tx", tx_pkt);
			tx_pkt.send();
		}
	}

	/** Wait for a response packet */
	public void waitResponse(E6Property p) throws IOException {
		synchronized (resp_pkt) {
			try {
				waiting = true;
				try {
					resp_pkt.wait(pkt_mess.getTimeout());
				}
				catch (InterruptedException e) {
					// doesn't matter
				}
				if (waiting)
					throw TIMEOUT;
				p.parse(resp_pkt.parseData());
			}
			finally {
				waiting = false;
			}
		}
	}

	/** Send a query packet */
	public void sendQuery(E6Property p) throws IOException {
		synchronized (tx_pkt) {
			tx_pkt.updateMsn();
			tx_pkt.format(p.command(), p.queryData());
			log("tx", tx_pkt);
			tx_pkt.send();
		}
	}

	/** Check if a drop address is valid */
	@Override
	public boolean isAddressValid(int drop) {
		return true;
	}

	/** Send a device request message to the tag reader */
	@Override
	public void sendRequest(TagReaderImpl tr, DeviceRequest r) {
		switch (r) {
		case SEND_SETTINGS:
			addOperation(new OpSendSettings(tr, this));
			break;
		default:
			// Ignore other requests
			break;
		}
	}

	/** Get the protocol debug log */
	@Override
	protected DebugLog protocolLog() {
		return E6_LOG;
	}
}
