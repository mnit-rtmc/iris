/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2006  Minnesota Department of Transportation
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
package us.mn.state.dot.sonar.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import javax.net.SocketFactory;

/**
 * A wrapper for a socket factory which is needed because the LDAP framework
 * has a stupid API.
 *
 * @author Douglas Lau
 */
public class LDAPSocketFactory extends SocketFactory {

	/** The socket factory being wrapped */
	static protected SocketFactory FACTORY;

	/** Get the default socket factory */
	static public SocketFactory getDefault() {
		return new LDAPSocketFactory();
	}

	/** Create a socket */
	public Socket createSocket(InetAddress host, int port)
		throws IOException
	{
		return FACTORY.createSocket(host, port);
	}

	/** Create a socket */
	public Socket createSocket(InetAddress host, int port, 
		InetAddress localAddress, int localPort) throws IOException
	{
		return FACTORY.createSocket(host, port, localAddress,
			localPort);
	}

	/** Create a socket */
	public Socket createSocket(String host, int port) throws IOException {
		return FACTORY.createSocket(host, port);
	}

	/** Create a socket */
	public Socket createSocket(String host, int port, InetAddress localHost,
		int localPort) throws IOException
	{
		return FACTORY.createSocket(host, port, localHost, localPort);
	}
}
