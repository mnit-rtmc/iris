/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2016  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.msgfeed;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import us.mn.state.dot.tms.utils.LineReader;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.FeedBucket;
import us.mn.state.dot.tms.server.FeedMsg;
import us.mn.state.dot.tms.server.comm.ControllerProperty;

/**
 * Container for message feed property.
 *
 * @author Michael Darter
 * @author Douglas Lau
 */
public class MsgFeedProperty extends ControllerProperty {

	/** Maximum number of chars in response for line reader */
	static private final int MAX_RESP = 1024;

	/** Feed name */
	private final String feed;

	/** Create a new msg_feed property */
	public MsgFeedProperty(String fd) {
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
			MsgFeedPoller.slog("parsing " + line);
			FeedMsg msg = new FeedMsg(feed, line);
			if (msg.isValid()) {
				FeedBucket.add(msg);
				MsgFeedPoller.slog("VALID " + msg);
			} else
				MsgFeedPoller.slog("INVALID " + msg);
			line = lr.readLine();
		}
	}

	/** Get a string representation of the property */
	@Override
	public String toString() {
		return "feed " + feed;
	}
}
