/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2002-2010  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.camera.stream;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * The HttpDataSource gets it's data via the HTTP protocol
 *
 * @author Timothy Johnson
 * @author Douglas Lau
 */
public class HttpDataSource {

	/** Default timeout for direct URL Connections */
	static protected final int TIMEOUT_DIRECT = 5 * 1000;

	/** Create an HTTP connection */
	static protected HttpURLConnection createConnection(URL url)
		throws IOException
	{
		HttpURLConnection c = (HttpURLConnection)url.openConnection();
		HttpURLConnection.setFollowRedirects(true);
		c.setConnectTimeout(TIMEOUT_DIRECT);
		c.setReadTimeout(TIMEOUT_DIRECT);
		return c;
	}

	/** URL of the data source */
	protected final URL url;

	/** Create a new HTTP data source */
	public HttpDataSource(URL u) {
		url = u;
	}

	/** Create the video stream */
	public VideoStream createStream() throws IOException {
		HttpURLConnection conn = createConnection(url);
		return new MJPEGStream(conn.getInputStream());
	}
}
