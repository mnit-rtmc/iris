/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2021-2025  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.cap;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import us.mn.state.dot.sched.DebugLog;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.tms.server.comm.CommThread;
import us.mn.state.dot.tms.server.comm.Messenger;
import us.mn.state.dot.tms.server.comm.MessengerException;
import us.mn.state.dot.tms.server.comm.OpQueue;

/**
 * IPAWS thread, used to provide a Messenger with date required by IPAWS-OPEN.
 *
 * @author Douglas Lau
 */
public class IpawsThread extends CommThread<CapProperty> {

	/** Date formatter for formatting dates in IPAWS format */
	static private final SimpleDateFormat IPAWS_FORMAT =
		new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");
	static {
		IPAWS_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
	}

	/** Most recent successful request date
	 *  (at startup, initialize to an hour ago) */
	static private Date REQ_SUCCESS = new Date(
		TimeSteward.currentTimeMillis() - 60 * 60 * 1000);

	/** Get date for IPAWS path API request */
	static private String getReqDate() {
		return IPAWS_FORMAT.format(REQ_SUCCESS);
	}

	/** Set date/time of most recent successful request */
	static public void setReqSuccess(Date dt) {
		REQ_SUCCESS = dt;
	}

	/** Create a new IPAWS thread */
	public IpawsThread(CapPoller p, OpQueue<CapProperty> q, URI s,
		String u, int rt, int nrd, DebugLog log)
	{
		super(p, q, s, u, rt, nrd, log);
	}

	/** Create a messenger */
	@Override
	protected Messenger createMessenger(URI s, String u, int rt, int nrd)
		throws MessengerException, IOException
	{
		try {
			String uri = createURI_IPAWS(u).toString();
			CapPoller.slog("URI: " + uri);
			return Messenger.create(s, uri, rt, nrd);
		}
		catch (URISyntaxException e) {
			throw new MessengerException(e);
		}
	}

	/** Add date since last successful response to URI path.
	 *
	 * This date format is used by the IPAWS system. */
	private URI createURI_IPAWS(String uri) throws URISyntaxException {
		URI u = new URI(uri);
		if (u.getPath().endsWith("/")) {
			return new URI(u.getScheme(), u.getAuthority(),
				u.getPath() + getReqDate(),
				u.getQuery(), u.getFragment());
		} else
			return u;
	}
}
