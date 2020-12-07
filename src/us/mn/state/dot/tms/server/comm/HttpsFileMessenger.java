/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2020  SRF Consulting Group, Inc.
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
import javax.net.ssl.HttpsURLConnection;
import static java.net.HttpURLConnection.HTTP_UNAUTHORIZED;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.utils.Base64;

/**
 * A HttpsFileMessenger is a class which reads a file from a URL using secure
 * HTTP (HTTPS).
 *
 * @author Gordon Parikh
 * @author Douglas Lau
 */
public class HttpsFileMessenger extends BasicMessenger {

	/** Create an HTTPS file messenger.
	 * @param u URI of remote host.
	 * @param rt Receive timeout (ms). */
	static protected HttpsFileMessenger create(URI u, int rt, int nrd)
		throws IOException
	{
		assert "https".equals(u.getScheme());
		return new HttpsFileMessenger(u.toURL(), rt, nrd);
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

	/** Create a new HTTPS file messenger.
	 * @param url The URL of the file to read.
	 * @param rt Read timeout (ms). */
	private HttpsFileMessenger(URL url, int rt, int nrd) {
		super(nrd);
		this.url = url;
		timeout = rt;
	}

	/** Close the messenger */
	@Override
	protected void close2() throws IOException {
		// nothing to do
	}
	
	/** Get the input stream */
	@Override
	protected InputStream getRawInputStream(String path) throws IOException {
		return createInputStream(path, null);
	}
	
	/** Get an input stream for the specified controller */
	@Override
	protected InputStream getRawInputStream(String path, ControllerImpl c)
		throws IOException
	{
		return createInputStream(path, c.getPassword());
	}
	
	/** Create an HTTPS input stream */
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
		if (c instanceof HttpsURLConnection) {
			HttpsURLConnection hc = (HttpsURLConnection) c;
			if (hc.getResponseCode() == HTTP_UNAUTHORIZED) {
				throw new ControllerException("UNAUTHORIZED: " +
					HTTP_UNAUTHORIZED);
			}
		}
		return c.getInputStream();
	}

	/** Get the output stream */
	@Override
	public OutputStream getRawOutputStream(ControllerImpl c) {
		// HTTPS messengers don't have output streams
		return null;
	}

	/** Drain any bytes from the input stream */
	@Override
	public void drain() {
		// not needed
	}
}
