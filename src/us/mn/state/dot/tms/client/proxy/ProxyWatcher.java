/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2010-2013  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.proxy;

import us.mn.state.dot.sonar.SonarObject;
import us.mn.state.dot.sonar.client.ProxyListener;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.client.Session;

/**
 * A class for watching attributes of a sonar object.
 *
 * @author Douglas Lau
 */
public class ProxyWatcher<T extends SonarObject> {

	/** SONAR namespace */
	private final Session session;

	/** Proxy cache */
	private final TypeCache<T> cache;

	/** Proxy view */
	private final ProxyView<T> view;

	/** Flag to watch proxy */
	private final boolean watch;

	/** Proxy listener */
	private final ProxyListener<T> listener = new ProxyListener<T>() {
		public void proxyAdded(T p) { }
		public void enumerationComplete() { }
		public void proxyRemoved(T p) {
			if(proxy == p) {
				proxy = null;
				view.clear();
			}
		}
		public void proxyChanged(T p, final String a) {
			if(proxy == p)
				view.update(p, a);
		}
	};

	/** Proxy being watched */
	private T proxy;

	/** Set a new proxy to watch */
	public void setProxy(T p) {
		T op = proxy;
		if(watch) {
			if(op != null)
				cache.ignoreObject(op);
			if(p != null)
				cache.watchObject(p);
		}
		if(p != null)
			view.update(p, null);
		else
			view.clear();
		proxy = p;
	}

	/** Create the proxy watcher */
	public ProxyWatcher(Session s, ProxyView<T> pv, TypeCache<T> c,
		boolean w)
	{
		session = s;
		view = pv;
		cache = c;
		watch = w;
	}

	/** Initialize the watcher */
	public void initialize() {
		if(proxy != null)
			throw new IllegalStateException("Must init first");
		cache.addProxyListener(listener);
	}

	/** Dispose of the watcher */
	public void dispose() {
		cache.removeProxyListener(listener);
		T p = proxy;
		if(watch && p != null)
			cache.ignoreObject(p);
	}

	/** Check if the user can update an attribute */
	public boolean canUpdate(T p, String aname) {
		return session.canUpdate(p, aname);
	}
}
