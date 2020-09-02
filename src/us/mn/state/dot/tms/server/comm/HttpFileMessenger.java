/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2020  Minnesota Department of Transportation
 * Copyright (C) 2020       SRF Consulting Group
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
import java.net.HttpURLConnection;
import static java.net.HttpURLConnection.HTTP_UNAUTHORIZED;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.utils.Base64;

/**
 * A HttpFileMessenger is a class which reads a file from a URL using http.
 *
 * @author Douglas Lau
 * @author Michael Darter
 * @author John L. Stanley - SRF Consulting
 */
public class HttpFileMessenger extends BasicMessenger {

	/** Create an HTTP file messenger.
	 * @param u URI of remote host.
	 * @param rt Receive timeout (ms). */
	static protected HttpFileMessenger create(URI u, int rt, int nrd)
		throws IOException
	{
		assert "http".equals(u.getScheme());
		return new HttpFileMessenger(u.toURL(), rt, nrd);
	}

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

	/** Create a new HTTP file messenger.
	 * @param url The URL of the file to read.
	 * @param rt Read timeout (ms). */
	private HttpFileMessenger(URL url, int rt, int nrd) {
		super(nrd);
		this.url = url;
		timeout = rt;
	}

	/** Close the messenger */
	@Override
	protected void close2() {
		// nothing to do
	}

	/** Get the input stream */
	@Override
	protected InputStream getRawInputStream(String p) throws IOException {
		return createInputStream(p, null);
	}

	/** Get an input stream for the specified controller */
	@Override
	protected InputStream getRawInputStream(String p, ControllerImpl c)
		throws IOException
	{
		return createInputStream(p, c.getPassword());
	}

	/** Create an HTTP input stream */
	private InputStream createInputStream(String path, String upass)
		throws IOException
	{
		URLConnection c = getUrl(path).openConnection();
		if (upass != null) {
			String auth = "Basic " + new String(Base64.encode(
				upass.getBytes()));
			c.setRequestProperty("Authorization", auth);
		}
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

	/** Get the output stream */
	@Override
	protected OutputStream getRawOutputStream(ControllerImpl c) {
		// HTTP messengers don't have output streams
		return null;
	}

	/** Drain any bytes from the input stream */
	@Override
	public void drain() {
		// not needed
	}
}
