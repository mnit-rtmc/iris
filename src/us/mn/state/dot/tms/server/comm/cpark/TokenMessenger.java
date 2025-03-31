/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2025  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.cpark;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import static java.net.HttpURLConnection.HTTP_UNAUTHORIZED;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.ControllerException;
import us.mn.state.dot.tms.server.comm.Messenger;
import us.mn.state.dot.tms.server.comm.ProtocolException;

/**
 * `x-access-token` Messenger
 *
 * @author Douglas Lau
 */
public class TokenMessenger extends Messenger {

	/** Wrapped messenger */
	private final Messenger wrapped;

	/** URL to read */
	private final URL url;

	/** Receive timeout (ms) */
	private final int timeout;

	/** Get the URL with path appended */
	private URL getUrl(String path) throws MalformedURLException {
		if (path != null && path.length() > 0)
			return new URL(url, path);
		else
			return url;
	}

	/** Create a new token messenger */
	public TokenMessenger(Messenger m, URL u, int t) throws IOException {
		wrapped = m;
		url = u;
		timeout = t;
	}

	/** Close the messenger */
	@Override
	public void close() throws IOException {
		wrapped.close();
	}

	/** Get the input stream.
	 * @param p Relative path name.
	 * @return An input stream for reading from the messenger. */
	public InputStream getInputStream(String p) throws IOException {
		throw new ProtocolException("NULL CONTROLLER");
	}

	/** Get an input stream for the specified controller */
	@Override
	public InputStream getInputStream(String p, ControllerImpl c)
		throws IOException
	{
		return createInputStream(p, c.getPassword());
	}

	/** Create an HTTP input stream */
	private InputStream createInputStream(String path, String token)
		throws IOException
	{
		URLConnection c = getUrl(path).openConnection();
		if (token != null)
			c.setRequestProperty("x-access-token", token);
		c.setUseCaches(false);
		c.setConnectTimeout(timeout);
		c.setReadTimeout(timeout);
		if (c instanceof HttpURLConnection) {
			HttpURLConnection hc = (HttpURLConnection) c;
			if (hc.getResponseCode() == HTTP_UNAUTHORIZED) {
				throw new ControllerException("UNAUTHORIZED: " +
					HTTP_UNAUTHORIZED);
			}
		}
		return c.getInputStream();
	}

	/** Get an output stream for the specified controller */
	@Override
	public OutputStream getOutputStream(ControllerImpl c)
		throws IOException
	{
		// HTTP messengers don't have output streams
		return null;
	}

	/** Drain any bytes from the input stream */
	@Override
	public void drain() {
		// not needed
	}
}
