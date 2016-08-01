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

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.ModemImpl;

/**
 * A Messenger is a class which can poll a field controller and get the
 * response. Subclasses are StreamMessenger, HDLCMessenger, etc.
 *
 * @author Douglas Lau
 */
abstract public class Messenger {

	/** Exception thrown when messenger is closed */
	static private final EOFException CLOSED = new EOFException(
		"MESSENGER CLOSED");

	/** Create a messenger */
	static public Messenger create(String d_uri, String uri, int timeout)
		throws IOException
	{
		try {
			URI u = createURI(d_uri, uri);
			if ("udp".equals(u.getScheme()))
				return createDatagramMessenger(u, timeout);
			else if ("tcp".equals(u.getScheme()))
				return createStreamMessenger(u, timeout);
			else if ("http".equals(u.getScheme()))
				return createHttpFileMessenger(u, timeout);
			else if ("modem".equals(u.getScheme()))
				return createModemMessenger(u, timeout);
			else
				throw new IOException("INVALID URI SCHEME");
		}
		catch (URISyntaxException e) {
			throw new IOException("INVALID URI");
		}
	}

	/** Create the URI with a default URI scheme */
	static private URI createURI(String d_uri, String uri)
		throws URISyntaxException
	{
		return URI.create(d_uri).resolve(createURI(uri));
	}

	/** Create the URI */
	static private URI createURI(String uri) throws URISyntaxException {
		try {
			return new URI(uri);
		}
		catch (URISyntaxException e) {
			// If the URI begins with a host IP address,
			// we need to prepend a couple of slashes
			return new URI("//" + uri);
		}
	}

	/** Create a UDP datagram messenger */
	static private Messenger createDatagramMessenger(URI u, int timeout)
		throws IOException
	{
		return new DatagramMessenger(createSocketAddress(u), timeout);
	}

	/** Create a TCP stream messenger */
	static private Messenger createStreamMessenger(URI u, int timeout)
		throws IOException
	{
		return new StreamMessenger(createSocketAddress(u), timeout);
	}

	/** Create an inet socket address */
	static private InetSocketAddress createSocketAddress(URI u)
		throws IOException
	{
		String host = u.getHost();
		int p = u.getPort();
		try {
			return new InetSocketAddress(host, p);
		}
		catch (IllegalArgumentException e) {
			throw new IOException(e.getMessage());
		}
	}

	/** Create an HTTP file messenger */
	static private HttpFileMessenger createHttpFileMessenger(URI u,
		int timeout) throws IOException
	{
		return new HttpFileMessenger(u.toURL(), timeout);
	}

	/** Create a modem messenger */
	static private Messenger createModemMessenger(URI u, int timeout)
		throws IOException
	{
		ModemImpl modem = ModemMessenger.getModem();
		if (modem != null) {
			return new ModemMessenger(createSocketAddress(
				createModemURI(modem)), timeout, modem,
				u.getHost());
		} else
			throw new IOException("No modem available");
	}

	/** Create a URI for the modem */
	static private URI createModemURI(ModemImpl modem) throws IOException {
		try {
			return modem.createURI();
		}
		catch (URISyntaxException e) {
			throw new IOException("INVALID MODEM URI");
		}
	}

	/** Create a packet messenger */
	static public PacketMessenger createPkt(String uri, int timeout)
		throws IOException
	{
		try {
			URI u = createURI("udp:/", uri);
			if ("udp".equals(u.getScheme()))
				return createPacketMessenger(u, timeout);
			else
				throw new IOException("INVALID URI SCHEME");
		}
		catch (URISyntaxException e) {
			throw new IOException("INVALID URI");
		}
	}

	/** Create a packet datagram messenger */
	static private PacketMessenger createPacketMessenger(URI u, int timeout)
		throws IOException
	{
		return new PacketMessenger(createSocketAddress(u), timeout);
	}

	/** Input stream */
	protected InputStream input;

	/** Output stream */
	protected OutputStream output;

	/** Open the messenger */
	abstract public void open() throws IOException;

	/** Close the messenger */
	abstract public void close();

	/** Get the input stream.
	 * @param path Relative path name.  Only needed for protocols which
	 *             require it, such as HTTP.
	 * @return An input stream for reading from the messenger. */
	public InputStream getInputStream(String path) throws IOException {
		return input;
	}

	/** Get an input stream for the specified controller.
	 * @param path Relative path name.  Only needed for protocols which
	 *             require it, such as HTTP.
	 * @param c Controller to read from.
	 * @return An input stream for reading from the messenger. */
	public InputStream getInputStream(String path, ControllerImpl c)
		throws IOException
	{
		InputStream is = getInputStream(path);
		if (is == null)
			throw CLOSED;
		else
			return input;
	}

	/** Close the input stream */
	protected final void closeInput() {
		InputStream is = input;
		if (is != null) {
			try {
				is.close();
			}
			catch (IOException e) {
				// Ignore
			}
		}
		input = null;
	}

	/** Get the output stream */
	public OutputStream getOutputStream() {
		return output;
	}

	/** Get an output stream for the specified controller */
	public OutputStream getOutputStream(ControllerImpl c)
		throws IOException
	{
		OutputStream os = getOutputStream();
		if (os == null)
			throw CLOSED;
		else
			return os;
	}

	/** Close the output stream */
	protected final void closeOutput() {
		OutputStream os = output;
		if (os != null) {
			try {
				os.close();
			}
			catch (IOException e) {
				// Ignore
			}
		}
		output = null;
	}

	/** Drain any bytes from the input stream */
	public void drain() throws IOException {
		InputStream is = input;
		if (is != null) {
			int a = is.available();
			if (a > 0)
				is.skip(a);
		}
	}
}
