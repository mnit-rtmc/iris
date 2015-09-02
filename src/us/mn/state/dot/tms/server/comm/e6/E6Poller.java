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
import us.mn.state.dot.sched.DebugLog;
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.server.TagReaderImpl;
import us.mn.state.dot.tms.server.comm.ChecksumException;
import us.mn.state.dot.tms.server.comm.MessagePoller;
import us.mn.state.dot.tms.server.comm.Messenger;
import us.mn.state.dot.tms.server.comm.ParsingException;
import us.mn.state.dot.tms.server.comm.TagReaderPoller;

/**
 * A Poller to communicate with TransCore E6 tag readers.
 *
 * @author Douglas Lau
 */
public class E6Poller extends MessagePoller implements TagReaderPoller {

	/** Local port */
	static public final int LOCAL_PORT = 58001;

	/** E6 debug log */
	static private final DebugLog E6_LOG = new DebugLog("e6");

	/** Thread group for all receive threads */
	static private final ThreadGroup RECV = new ThreadGroup("Recv");

	/** Thread to receive packets */
	private final Thread r_thread;

	/** Packet buffer */
	private final byte[] pkt = new byte[128];

	/** Create a new E6 poller */
	public E6Poller(String n, Messenger m) {
		super(n, m);
 		r_thread = new Thread(RECV, "Recv: " + n) {
			@Override
			public void run() {
				receivePackets();
			}
		};
		r_thread.setDaemon(true);
	}

	/** Start polling */
	@Override
	protected void startPolling() {
		super.startPolling();
		r_thread.start();
	}

	/** Stop polling */
	@Override
	protected void stopPolling() {
		r_thread.interrupt();
		super.stopPolling();
	}

	/** Receive packets */
	private void receivePackets() {
		try {
			while (!r_thread.isInterrupted()) {
				receivePacket();
			}
		}
		catch (IOException e) {
			setStatus(exceptionMessage(e));
		}
	}

	/** Receive one packet */
	private void receivePacket() throws IOException {
		InputStream is = messenger.getInputStream("");
		int n_bytes = is.read(pkt, 0, 2);
		if (n_bytes != 2)
			throw new ParsingException("LENGTH NOT READ");
		int n_len = (pkt[0] << 8) | pkt[1];
		if (n_len < 2 || n_len > pkt.length)
			throw new ParsingException("BAD LENGTH: " + n_len);
		n_bytes = is.read(pkt, 2, n_len);
		if (n_bytes != n_len)
			throw new ParsingException("PACKET NOT READ");
		int xsum = 0;
		for (int i = 0; i < n_bytes - 1; i++)
			xsum += pkt[i];
		xsum &= 0xFF;
		if (xsum != pkt[n_bytes - 1])
			throw new ChecksumException(pkt);
		// FIXME
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
		case QUERY_CONFIGURATION:
//			addOperation(new OpQueryConfiguration(tr));
			break;
		case QUERY_STATUS:
//			addOperation(new OpQueryStatus(tr));
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
