/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2010  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * A HttpFileMessenger is a class which reads a file from a URL using http.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class HttpFileMessenger extends Messenger {

	/** URL to read */
	protected final URL url;

	/** Receive timeout (ms) */
	protected int timeout = 2000;

	/** URL connection */
	protected URLConnection connection = null;

	/** Create a new HTTP file messenger.
	 * @param url The URL of the file to read. */
	public HttpFileMessenger(URL url) {
		this.url = url;
		input = null;
		output = null;
	}

	/** Set the receive timeout */
	public void setTimeout(int t) throws IOException {
		timeout = t;
		URLConnection c = connection;
		if(c != null) {
			c.setConnectTimeout(timeout);
			c.setReadTimeout(timeout);
		}
	}

	/** Open the messenger */
	public void open() throws IOException {
		URLConnection c = url.openConnection();
		c.setConnectTimeout(timeout);
		c.setReadTimeout(timeout);
		input = c.getInputStream();
		connection = c;
	}

	/** Close the messenger */
	public void close() {
		InputStream in = input;
		if(in != null) {
			try {
				in.close();
			}
			catch(IOException e) {
				// Ignore
			}
		}
		input = null;
		connection = null;
	}
}
