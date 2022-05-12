/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2017  Minnesota Department of Transportation
 * Copyright (C) 2022  Iteris Inc.
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
import static java.net.HttpURLConnection.HTTP_UNAUTHORIZED;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import us.mn.state.dot.tms.server.ControllerImpl;

/**
 * A HttpsFileMessenger is a class which reads a file from a URL using http.
 *
 * @author Michael Darter
 */
public class HttpsFileMessenger extends BasicMessenger {

	/** Create an HTTP file messenger.
	 * @param u URI of remote host.
	 * @param rt Receive timeout (ms).
	 * @param nrd No-response disconnect (sec). */
	static protected HttpsFileMessenger create(URI u, int rt, int nrd)
		throws IOException
	{
		assert "https".equals(u.getScheme());
		return new HttpsFileMessenger(u.toURL(), rt, nrd);
	}

	/** Return the specified value in colon separated string.
	 * @param cstr Colon separated string e.g. 'username:password'
	 * @param idx Zero based index of field to return.
	 * @return The specified field or null on error */
	static private String getField(String cstr, int idx) {
		cstr = safe(cstr);
		if (cstr.contains(":")) {
			String[] fields = cstr.split(":");
			if (fields.length >= idx + 1) {
				return fields[idx];
			}
		}
		return null;
	}

	/* Get an HTTPS connection to a CBW controller.
	 * @param iic True to ignore an invalid cert
	 * @param user User name
	 * @param pass Password
	 * @param to Timeout in ms
	 * @return New connection or null */
	static private HttpsURLConnection getConnection(boolean iic,
		String user, String pass, URL url, int to) throws
		KeyManagementException, NoSuchAlgorithmException, 
		IOException
	{
		SSLContext ctx = SSLContext.getInstance("TLS");
		if (iic) {
			ctx.init(null, new TrustManager[] { 
				new InvalidCertTrustManager() }, null);
		}
		SSLContext.setDefault(ctx);

		String authplain = user + ":" + pass;
		byte[] asa = authplain.getBytes();
		byte[] encoded = Base64.getEncoder().encode(asa);
		String authenc = new String(encoded);

		HttpsURLConnection con = 
			(HttpsURLConnection)url.openConnection();
		con.setRequestProperty("Authorization", "Basic " + authenc);
		if (iic)
			con.setHostnameVerifier(new HostVerifier());
		con.setUseCaches(false);
		con.setConnectTimeout(to);
		con.setReadTimeout(to);
		return con;
	}

	/** Return a non-null string */
	static private String safe(String str) {
		return str == null ? "" : str;
	}

	/** URL to read */
	private final URL url;

	/** Receive timeout (ms) */
	private final int timeout_ms;

	/** Get the URL with path appended */
	private URL getUrl(String path) throws MalformedURLException {
		if (path != null && path.length() > 0)
			return new URL(url, path);
		else
			return url;
	}

	/** Constructor
	 * @param url The URL of the file to read.
	 * @param rt Read timeout (ms).
	 * @param nrd No-response disconnect (sec) */
	private HttpsFileMessenger(URL url, int rt, int nrd) {
		super(nrd);
		this.url = url;
		timeout_ms = rt;
	}

	/** Close the messenger */
	@Override
	protected void close2() {
		// nothing to do
	}

	/** Get the input stream */
	@Override
	public InputStream getRawInputStream(String p) throws IOException {
		return createInputStream(p, null);
	}

	/** Get an input stream for the specified controller */
	@Override
	public InputStream getRawInputStream(String p, ControllerImpl c)
		throws IOException
	{
		return createInputStream(p, c.getPassword());
	}

	/** Create an HTTP input stream
	 * @param path
	 * @param upass Username and password in the form "user::password" */
	private InputStream createInputStream(String path, String upass)
		throws IOException
	{
		try {
			String un = safe(getField(upass, 0));
			String pw = safe(getField(upass, 1));
			URL jurl = getUrl(path);
			// ignore controller cert issues
			HttpsURLConnection con = getConnection(true, un, pw, 
				jurl, timeout_ms);
			if (con.getResponseCode() == HTTP_UNAUTHORIZED) {
				throw new ControllerException("UNAUTHORIZED: "+
					HTTP_UNAUTHORIZED);
			}
			return con.getInputStream();
		} catch(KeyManagementException ex) {
			throw new IOException(ex.toString());
		} catch(NoSuchAlgorithmException ex) {
			throw new IOException(ex.toString());
		}
	}

	/** Get the output stream */
	@Override
	public OutputStream getRawOutputStream(ControllerImpl c) {
		// HTTP messengers don't have output streams
		return null;
	}

	/** Drain any bytes from the input stream */
	@Override
	public void drain() {
		// not needed
	}
}
