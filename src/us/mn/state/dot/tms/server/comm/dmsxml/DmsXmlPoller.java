/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2016  Minnesota Department of Transportation
 * Copyright (C) 2008-2010  AHMCT, University of California
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
package us.mn.state.dot.tms.server.comm.dmsxml;

import java.io.IOException;
import us.mn.state.dot.sched.DebugLog;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.InvalidMessageException;
import us.mn.state.dot.tms.SignMessage;
import us.mn.state.dot.tms.SignMessageHelper;
import us.mn.state.dot.tms.server.DMSImpl;
import us.mn.state.dot.tms.server.comm.DevicePoller;
import us.mn.state.dot.tms.server.comm.DMSPoller;

/**
 * This class provides a DMS Poller that communicates with
 * a standalone server via XML that provides DMS functionality.
 * It was developed for the Caltrans IRIS implementation. It
 * maybe use useful for any application that needs to interface
 * IRIS DMS functionality with an external application.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class DmsXmlPoller extends DevicePoller implements DMSPoller {

	/** Debug log */
	static protected final DebugLog LOG = new DebugLog("dmsxml");

	/** Create a new dmsxml poller */
	public DmsXmlPoller(String n) {
		super(n, TCP, LOG);
	}

	/** Create a comm thread */
	@Override
	protected DmsXmlThread createCommThread(String uri, int timeout)
		throws IOException
	{
		return new DmsXmlThread(this, queue, scheme, uri, timeout);
	}

	/** Send a new message to the sign. Called by DMSImpl.
	 *  @param dms May be null.
	 *  @param m Sign message to send, may be null.
	 *  @param o User sending message, may be null.
	 *  @throws InvalidMessageException
	 *  @see DMSImpl, DMS */
	@SuppressWarnings("unchecked")
	public void sendMessage(DMSImpl dms, SignMessage m, User o)
		throws InvalidMessageException
	{
		LOG.log("DmsXmlPoller.sendMessage(" + dms + ", " +
			m + ", " + o+ ") called.");
		if (dms == null || m == null)
			return;
		if (SignMessageHelper.isBlank(m))
			addOp(new OpBlank(dms, m, o));
		else
			addOp(new OpMessage(dms, m, o));
	}

	/** Send a device request message to the sign, no user specified */
	public void sendRequest(DMSImpl dms, DeviceRequest r) {
		// user assumed to be IRIS
		User u = null;
		sendRequest(dms, r, u);
	}

	/** Send a device request message to the sign from a specific user */
	public void sendRequest(DMSImpl dms, DeviceRequest r, User u) {
		initialPoll(dms, r, u);
		_sendRequest(dms, r, u, false);
	}

	/** Perform an initial get-configuration and a get-status
	 *  operation for periodically queriable DMS. */
	private void initialPoll(DMSImpl d, DeviceRequest r, User u) {
		if(d == null)
			return;
		// SEND_SETTINGS is sent on startup
		if(r == DeviceRequest.SEND_SETTINGS)
			if(!d.getConfigure()) {
				_sendRequest(d, DeviceRequest.
					QUERY_CONFIGURATION, u, false);
				_sendRequest(d, DeviceRequest.
					QUERY_MESSAGE, u, true);
			}
	}

	/** Send a device request message to the sign from a specific user */
	@SuppressWarnings("unchecked")
	private void _sendRequest(DMSImpl dms, DeviceRequest r, User u,
		boolean startup)
	{
		if (dms == null)
			return;
		// handle requests
		if (r == DeviceRequest.QUERY_CONFIGURATION)
			addOp(new OpQueryConfig(dms, u));
		else if (r == DeviceRequest.QUERY_MESSAGE)
			addOp(new OpQueryMsg(dms, u, r, startup));
		else if (r == DeviceRequest.RESET_DEVICE)
			addOp(new OpReset(dms, u));
		else if (r == DeviceRequest.QUERY_STATUS) {
			// ignore
		} else if(r == DeviceRequest.SEND_SETTINGS) {
			// ignore
		} else if(r == DeviceRequest.RESET_MODEM) {
			// ignore
		} else if(r == DeviceRequest.QUERY_PIXEL_FAILURES) {
			// ignore
		} else {
			// ignore other requests
			LOG.log("DmsXmlPoller.sendRequest(" +
				dms.getName() +	"): ignored request r=" + r);
		}
	}
}
