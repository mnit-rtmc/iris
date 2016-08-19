/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2016  Minnesota Department of Transportation
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

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.utils.URIUtils;

/**
 * A Messenger is a class which can poll a field controller and get the
 * response.  Subclasses are DatagramMessenger, StreamMessenger, etc.
 *
 * @author Douglas Lau
 */
abstract public class Messenger implements Closeable {

	/** Exception thrown for invalid URI scheme */
	static protected final MessengerException INVALID_URI_SCHEME =
		new MessengerException("INVALID URI SCHEME");

	/** Create a messenger.
	 * @param scheme Default URI scheme.
	 * @param uri URI of remote host.
	 * @param rt Receive timeout.
	 * @throws MessengerException if the messenger could not be created. */
	static public Messenger create(URI scheme, String uri, int rt)
		throws MessengerException
	{
		try {
			URI u = createURI(scheme, uri);
			if ("udp".equals(u.getScheme()))
				return DatagramMessenger.create(u, rt);
			else if ("tcp".equals(u.getScheme()))
				return StreamMessenger.create(u, rt, rt);
			else if ("http".equals(u.getScheme()))
				return HttpFileMessenger.create(u, rt);
			else if ("modem".equals(u.getScheme()))
				return ModemMessenger.create(u, rt);
			else
				throw INVALID_URI_SCHEME;
		}
		catch (IOException e) {
			throw new MessengerException(e);
		}
	}

	/** Create the URI with a default URI scheme */
	static protected URI createURI(URI scheme, String uri)
		throws MessengerException
	{
		return scheme.resolve(createURI(uri));
	}

	/** Create the URI */
	static protected URI createURI(String uri) throws MessengerException {
		try {
			return URIUtils.create(uri);
		}
		catch (URISyntaxException e) {
			throw new MessengerException(e);
		}
	}

	/** Create an inet socket address */
	static protected InetSocketAddress createSocketAddress(URI u)
		throws MessengerException
	{
		String host = u.getHost();
		int p = u.getPort();
		try {
			return new InetSocketAddress(host, p);
		}
		catch (IllegalArgumentException e) {
			throw new MessengerException(e);
		}
	}

	/** Close the messenger */
	@Override
	abstract public void close() throws IOException;

	/** Get the input stream.
	 * @param path Relative path name.  Only needed for protocols which
	 *             require it, such as HTTP.
	 * @return An input stream for reading from the messenger. */
	abstract public InputStream getInputStream(String path)
		throws IOException;

	/** Get an input stream for the specified controller.
	 * @param path Relative path name.  Only needed for protocols which
	 *             require it, such as HTTP.
	 * @param c Controller to read from.
	 * @return An input stream for reading from the messenger. */
	public InputStream getInputStream(String path, ControllerImpl c)
		throws IOException
	{
		return getInputStream(path);
	}

	/** Get the output stream */
	public final OutputStream getOutputStream() throws IOException {
		return getOutputStream(null);
	}

	/** Get an output stream for the specified controller */
	abstract public OutputStream getOutputStream(ControllerImpl c)
		throws IOException;

	/** Drain any bytes from the input stream */
	abstract public void drain() throws IOException;
}
