/*
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
import java.util.logging.Logger;

/**
 * The HttpDataSource gets it's data via the HTTP protocol
 *
 * @author Timothy Johnson
 */
public class HttpDataSource extends AbstractDataSource {

	protected final URL url;

	/** Constructor for the HttpDataSource. */
	public HttpDataSource(VideoRequest vr, URL url) {
		this(vr, null, null, url);
	}

	/** Constructor for the HttpDataSource. */
	public HttpDataSource(VideoRequest vr, Logger l, ThreadMonitor m,
		URL url)
	{
		super(vr, l, m);
		this.url = url;
	}

	/** Start the stream. */
	public void run() {
		HttpURLConnection conn = null;
		if(url != null){
			try{
				conn = createConnection(url);
				final MJPEGReader stream = new MJPEGReader(conn.getInputStream());
				logger.fine("Starting: " + this);
				byte[] img;
				while(!done && this.isAlive()){
					if(stream==null) {
						break;
					}
					img = stream.getImage();
					if(img != null && img.length > 0){
						notifySinks(img);
					}else{
						//FIXME: Continue trying to get images even if null or empty.
						//Pehaps a counter can keep track of contiguous failures and
						//then break.

						//break;
					}
				}
			}catch(IOException ioe){
				logger.info(ioe.getMessage());
			}catch(InstantiationException ie){
				logger.info(ie.getMessage());
			}finally{
				logger.fine("Stopping: " + this);
				try{
					conn.disconnect();
				}catch(Exception e2){
				}
				removeSinks();
			}
		}else{
			logger.fine("No encoder defined for this source.");
		}
	}

	public static HttpURLConnection createConnection(URL url)
			throws IOException {
		HttpURLConnection c = (HttpURLConnection)url.openConnection();
		HttpURLConnection.setFollowRedirects(true);
		c.setConnectTimeout(VideoThread.TIMEOUT_DIRECT);
		c.setReadTimeout(VideoThread.TIMEOUT_DIRECT);
		return c;
	}
}
