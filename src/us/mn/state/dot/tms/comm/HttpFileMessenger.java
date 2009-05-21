/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2008  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.comm;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import us.mn.state.dot.tms.utils.Log;

/**
 * A HttpFileMessenger is a class which reads a file from a URL using http.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class HttpFileMessenger extends Messenger {

	/** URL to read */
	private URL m_url;

	/**
	 *  constructor
	 *  @args url The URL of the file to read.
	 */
	public HttpFileMessenger(URL url) {
		m_url = url;
		input = null;
		output = null;
	}

	/** Set the receive timeout */
	public void setTimeout(int t) throws IOException {}

	/** Open the messenger */
	public void open() throws IOException {}

	/** Close the messenger */
	public void close() {}

	/**
	 *  read the url.
	 *  @returns byte[] containing contents of read file.
	 */
	public byte[] read() {
		InputStream in = null;
		byte[] ret = new byte[0];
		try {

			// open
			URLConnection c = m_url.openConnection();
			int pl = c.getContentLength();

			// Log.finest("HttpFileMessenger.read(), len="+pl);
			long fdate = c.getDate();
			// Log.finest("HttpFileMessenger.read(), date="+d);

			// read until eof
			in = c.getInputStream();
			ArrayList<Byte> al=new ArrayList(pl);
			while(true) {
				int b = in.read(); // throws IOException on error
				if (b<0)
					break; // eof
				al.add(new Byte((byte)b));
			}

			// create byte[]
			ret = new byte[al.size()];
			for (int i=0; i<ret.length; ++i)
				ret[i]=(byte)(al.get(i));

			Log.finest("HttpFileMessenger.read(), read " +
				al.size() + " bytes, file date=" + fdate);
		} catch (UnknownHostException e) {
			Log.fine("HttpFileMessenger.read(): "+
				"ignoring bogus url: "+e);
		} catch (IOException e) {
			Log.warning(
			    "HttpFileMessenger.read(), caught exception:" + e);
		} finally {
			try {
				if (in!= null)
					in.close();
			} catch (IOException ex) {}
		}
		return ret;
	}
}
