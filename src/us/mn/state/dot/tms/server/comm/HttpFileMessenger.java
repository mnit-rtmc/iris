/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2015  Minnesota Department of Transportation
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

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.utils.Base64;

/**
 * A HttpFileMessenger is a class which reads a file from a URL using http.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class HttpFileMessenger extends Messenger {

	/** URL to read */
	private final URL url;

	/** Relative path */
	private String path;

	/** Get the URL */
	private URL getUrl() throws MalformedURLException {
		if (path != null && path.length() > 0)
			return new URL(url, path);
		else
			return url;
	}

	/** Receive timeout (ms) */
	private int timeout = 2000;

	/** URL connection */
	private URLConnection connection = null;

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

	/** Get the receive timeout */
	@Override
	public int getTimeout() {
		return timeout;
	}

	/** Open the messenger */
	public void open() throws IOException {
		open(null);
	}

	/** Open the messenger */
	private void open(String upass) throws IOException {
		URLConnection c = getUrl().openConnection();
		if (upass != null) {
			String auth = "Basic " + new String(Base64.encode(
				upass.getBytes()));
			c.setRequestProperty("Authorization", auth);
		}
		c.setConnectTimeout(timeout);
		c.setReadTimeout(timeout);
		input = c.getInputStream();
		connection = c;
	}

	/** Close the messenger */
	public void close() {
		InputStream in = input;
		if (in != null) {
			try {
				in.close();
			}
			catch (IOException e) {
				// Ignore
			}
		}
		input = null;
		connection = null;
	}

	/** Get the input stream */
	@Override
	public InputStream getInputStream(String p) throws IOException {
		path = p;
		// make a new HTTP connection each time called
		close();
		open();
		return input;
	}

	/** Get an input stream for the specified controller */
	@Override
	public InputStream getInputStream(String p, ControllerImpl c)
		throws IOException
	{
		path = p;
		// make a new HTTP connection each time called
		close();
		open(c.getPassword());
		return input;
	}

	/** Get an output stream for the specified controller */
	@Override
	public OutputStream getOutputStream(ControllerImpl c) {
		// HTTP messengers don't have output streams
		return null;
	}
}
