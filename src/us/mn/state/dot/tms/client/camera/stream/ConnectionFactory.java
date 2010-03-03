/*
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
package us.mn.state.dot.tms.client.camera.stream;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;


/** The ConnectionFactory is a convenience class for setting 
 * up URLConnections with the appropriate parameters including
 * connection timeout.
 * 
 * @author Timothy A. Johnson
 */
abstract public class ConnectionFactory {

	public static HttpURLConnection createConnection(URL url)
			throws IOException {
		HttpURLConnection c = (HttpURLConnection)url.openConnection();
		HttpURLConnection.setFollowRedirects(true);
		c.setConnectTimeout(VideoThread.TIMEOUT_DIRECT);
		c.setReadTimeout(VideoThread.TIMEOUT_DIRECT);
		return c;
	}
}
