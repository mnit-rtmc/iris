/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2018  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.utils;

import java.io.IOException;
import java.net.Authenticator;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

/**
 * Proxy selector for HTTP clients.
 *
 * @author Tim Johnson
 * @author Douglas Lau
 */
public class HttpProxySelector extends ProxySelector {

	/** No proxy list */
	static private final ArrayList<Proxy> NO_PROXIES =
		new ArrayList<Proxy>();
	static {
		NO_PROXIES.add(Proxy.NO_PROXY);
	}

	/** Ports to be proxied */
	static private final int[] PROXY_PORTS = {80, 8080};

	/** Check if the port of a URI should be proxied */
	static private boolean isProxyPort(int p) {
		if (p == -1)
			return true;
		for (int i: PROXY_PORTS) {
			if (p == i)
				return true;
		}
		return false;
	}

	/** Create a proxy from a URL */
	static private URI createUri(String u) {
		return URIUtil.create(URIUtil.HTTP, u);
	}

	/** Create a proxy from a URL */
	static private Proxy createProxy(String u) {
		URI uri = createUri(u);
		String h = uri.getHost();
		int p = uri.getPort();
		return createProxy(h, (p >= 0) ? p : 80);
	}

	/** Create a proxy from host and port */
	static private Proxy createProxy(String host, int port) {
		SocketAddress sa = new InetSocketAddress(host, port);
		return new Proxy(Proxy.Type.HTTP, sa);
	}

	/** Create a password authentication from URI user info */
	static private PasswordAuthentication createAuth(String user_info) {
		if (user_info != null) {
			String[] auth = user_info.split(":");
			if (auth.length == 2) {
				return new PasswordAuthentication(auth[0],
					auth[1].toCharArray());
			}
		}
		return null;
	}

	/** List of proxies */
	private final List<Proxy> proxies;

	/** List of proxy authentication data */
	private final HashMap<String, PasswordAuthentication> auths;

	/** Whitelist of CIDR addresses to skip proxy */
	private final List<CIDRAddress> whitelist;

	/** Create a new HTTP proxy selector */
	public HttpProxySelector(Properties props) {
		proxies = createProxyList(props);
		auths = createAuths(props);
		whitelist = createProxyWhitelist(props);
	}

	/** Create a Proxy list from a set of properties */
	private List<Proxy> createProxyList(Properties props) {
		ArrayList<Proxy> plist = new ArrayList<Proxy>();
		String hps = props.getProperty("http.proxy");
		if (hps != null) {
			for (String u: hps.split("[ \t,]+")) {
				Proxy px = createProxy(u);
				if (px != null)
					plist.add(px);
			}
		}
		return plist;
	}

	/** Create a mapping of proxy host names to auth data */
	private HashMap<String, PasswordAuthentication> createAuths(
		Properties props)
	{
		HashMap<String, PasswordAuthentication> a =
			new HashMap<String, PasswordAuthentication>();
		String hps = props.getProperty("http.proxy");
		if (hps != null) {
			for (String u: hps.split("[ \t,]+")) {
				URI uri = createUri(u);
				String h = uri.getHost();
				PasswordAuthentication pa = createAuth(
					uri.getUserInfo());
				if (h != null && pa != null)
					a.put(h, pa);
			}
		}
		return a;
	}

	/** Create a whitelist of CIDR addresses to skip proxy */
	private List<CIDRAddress> createProxyWhitelist(Properties props) {
		ArrayList<CIDRAddress> wl = new ArrayList<CIDRAddress>();
		String p = props.getProperty("http.proxy.whitelist");
		if (p != null) {
			for (String c: p.split("[ \t,]+")) {
				try {
					wl.add(new CIDRAddress(c));
				}
				catch (UnknownHostException e) {
					// don't let DNS failure bring us down
					e.printStackTrace();
				}
			}
		}
		return wl;
	}

	/** Handle a failed connection to a proxy server */
	@Override
	public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
		System.err.println("connectFailed: " + uri);
		ioe.printStackTrace();
	}

	/** Select available proxy servers based on a URI */
	@Override
	public List<Proxy> select(URI uri) {
		if (uri != null && shouldUseProxy(uri))
			return proxies;
		else
			return NO_PROXIES;
	}

	/** Check if a proxy server should be used for a URI */
	private boolean shouldUseProxy(URI uri) {
		String host = uri.getHost();
		try {
			InetAddress addr = InetAddress.getByName(host);
			for (CIDRAddress cidr: whitelist) {
				if (cidr.matches(addr))
					return false;
			}
			return isProxyPort(uri.getPort());
		}
		catch (UnknownHostException uhe) {
			return true;
		}
	}

	/** Check if the selector has defined proxy servers */
	public boolean hasProxies() {
		return proxies.size() > 0;
	}

	/** Proxy authenticator */
	private final Authenticator authenticator = new Authenticator() {
		protected PasswordAuthentication getPasswordAuthentication() {
			if (getRequestorType() == RequestorType.PROXY)
				return auths.get(getRequestingHost());
			else
				return null;
		}
	};

	/** Get proxy authenticator */
	public Authenticator getAuthenticator() {
		return authenticator;
	}
}
