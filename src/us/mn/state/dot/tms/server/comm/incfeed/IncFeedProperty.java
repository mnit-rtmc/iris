/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2016  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.incfeed;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import us.mn.state.dot.tms.utils.LineReader;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.ControllerProperty;

/**
 * Incident feed property.
 *
 * @author Douglas Lau
 */
public class IncFeedProperty extends ControllerProperty {

	/** Maximum number of chars in response for line reader */
	static private final int MAX_RESP = 1024;

	/** Feed name */
	private final String feed;

	/** Create a new incident feed property */
	public IncFeedProperty(String fd) {
		feed = fd;
	}

	/** Decode a QUERY response */
	@Override
	public void decodeQuery(ControllerImpl c, InputStream is)
		throws IOException
	{
		LineReader lr = new LineReader(is, MAX_RESP);
		String line = lr.readLine();
		while (line != null) {
			IncFeedPoller.log("parsing " + line);
			// FIXME: parse
			line = lr.readLine();
		}
	}

	/** Get a string representation */
	@Override
	public String toString() {
		return "inc.feed " + feed;
	}
}
