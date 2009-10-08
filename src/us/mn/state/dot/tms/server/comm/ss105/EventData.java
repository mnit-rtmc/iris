/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2005-2009  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.ss105;

import java.io.IOException;
import us.mn.state.dot.tms.server.comm.ProtocolException;

/**
 * Event Data Request
 *
 * @author Douglas Lau
 */
public class EventData extends Request {

	/** Event data */
	protected String events;

	/** Check if the request has a checksum */
	protected boolean hasChecksum() {
		return false;
	}

	/** Format a basic "GET" request */
	protected String formatGetRequest() {
		return "XA";
	}

	/** Format a basic "SET" request */
	protected String formatSetRequest() throws IOException {
		throw new ProtocolException("Event data is read-only");
	}

	/** Set the response to the request */
	protected void setResponse(String r) {
		events = r;
	}

	/** Get the event data */
	public String getEvents() {
		return events;
	}
}
