/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2011  Minnesota Department of Transportation
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

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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

	/** Number of iterations reading from input stream */
	static private final int BUF_READS = 100;

	/** Feed name */
	private final String feed;

	/** Create a new msg_feed property */
	public MsgFeedProperty(String fd) {
		feed = fd;
	}

	/** Perform a get request, parsing all feed messages */
	public void doGetRequest(InputStream input) throws IOException {
		if(input == null)
			throw new EOFException();
		InputStreamReader isr = new InputStreamReader(input,"US-ASCII");
		for(String line: parseReader(isr).split("\n")) {
			FeedMsg msg = new FeedMsg(feed, line);
			if(msg.isValid()) {
				FeedBucket.add(msg);
				MsgFeedPoller.log("VALID " + msg);
			} else
				MsgFeedPoller.log("INVALID " + msg);
		}
	}

	/** Parse the feed from a reader */
	private String parseReader(InputStreamReader reader) throws IOException{
		StringBuilder sb = new StringBuilder();
		char[] buf = new char[1024];
		/* We can't use something like BufferedReader.readLine, because
		 * it will happily keep reading data from the reader until all
		 * memory is exhausted. */
		for(int i = 0; i < BUF_READS; i++) {
			int n_chars = reader.read(buf, 0, buf.length);
			if(n_chars < 0)
				return sb.toString();
			sb.append(buf, 0, n_chars);
		}
		MsgFeedPoller.log("Feed too long; stopped at " + BUF_READS +
			"reads");
		return sb.toString();
	}
}
