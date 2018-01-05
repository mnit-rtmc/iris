/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2018  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.cux50;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Protocol handler for Panasonic CU-x50 camera keyboards.
 *
 * @author Douglas Lau
 */
public class CUx50 implements ProtocolHandler {

	/** Mapping of host strings to state structures for all keyboards */
	private final ConcurrentHashMap<String, KeyboardState> states =
		new ConcurrentHashMap<String, KeyboardState>();

	/** Lookup the keyboard state for an inet socket address */
	private KeyboardState lookupState(InetSocketAddress sa) {
		String host = sa.getHostString();
		KeyboardState ks = states.get(host);
		if (ks != null)
			return ks;
		else {
			ks = new KeyboardState(host);
			states.put(host, ks);
			return ks;
		}
	}

	/** Lookup the keyboard state for a socket address */
	private KeyboardState lookupState(SocketAddress sa) {
		return (sa instanceof InetSocketAddress)
		      ? lookupState((InetSocketAddress) sa)
		      :	null;
	}

	/** Handle data receive for protocol */
	@Override
	public byte[] handleReceive(SocketAddress sa, byte[] rcv) {
		KeyboardState ks = lookupState(sa);
		return (ks != null) ? ks.handleReceive(rcv) : null;
	}
}
