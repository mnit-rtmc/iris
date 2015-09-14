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

import java.io.IOException;
import us.mn.state.dot.sched.DebugLog;
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.server.TagReaderImpl;
import us.mn.state.dot.tms.server.comm.MessagePoller;
import us.mn.state.dot.tms.server.comm.Messenger;
import us.mn.state.dot.tms.server.comm.TagReaderPoller;

/**
 * A Poller to communicate with TransCore E6 tag readers.
 *
 * @author Douglas Lau
 */
public class E6Poller extends MessagePoller implements TagReaderPoller {

	/** Local port */
	static public final int LOCAL_PORT = 58001;

	/** Thread group for all receive threads */
	static private final ThreadGroup RECV = new ThreadGroup("Recv");

	/** Thread to receive packets */
	private final Thread rx_thread;

	/** E6 debug log */
	static public final DebugLog E6_LOG = new DebugLog("e6");

	/** Transmit Packet */
	private final E6Packet tx_pkt = new E6Packet();

	/** Receive Packet */
	private final E6Packet rx_pkt = new E6Packet();

	/** Create a new E6 poller */
	public E6Poller(String n, Messenger m) {
		super(n, m);
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
		rx_pkt.receive(messenger.getInputStream(""));
		// FIXME: if op waiting, notify it; else log tag read
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
			addOperation(new OpSendSettings(tr));
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
