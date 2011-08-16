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

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import us.mn.state.dot.tms.server.comm.ControllerProperty;

/**
 * Container for message feed property.
 *
 * @author Michael Darter
 * @author Douglas Lau
 */
public class MsgFeedProperty extends ControllerProperty {

	/** Perform a get request, parsing all feed messages */
	public void doGetRequest(InputStream input) throws IOException {
		if(input == null)
			throw new EOFException();
		InputStreamReader isr = new InputStreamReader(input,"US-ASCII");
		BufferedReader br = new BufferedReader(isr);
		while(true) {
			String line = br.readLine();
			if(line == null)
				break;
			FeedMsg msg = new FeedMsg(line);
			if(msg.isValid())
				msg.activate();
		}
	}
}
