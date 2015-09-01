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

	/** E6 debug log */
	static private final DebugLog E6_LOG = new DebugLog("e6");

	/** Thread group for all response threads */
	static private final ThreadGroup RESP = new ThreadGroup("Resp");

	/** Thread to receive responses */
	private final Thread r_thread;

	/** Create a new E6 poller */
	public E6Poller(String n, Messenger m) {
		super(n, m);
 		r_thread = new Thread(RESP, "Resp: " + n) {
			@Override
			public void run() {
				receiveResponses();
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
		super.stopPolling();
		// FIXME: kill r_thread
	}

	/** Receive responses */
	private void receiveResponses() {
		while (true) {
			// FIXME
			break;
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
