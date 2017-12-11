/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2010-2017  Minnesota Department of Transportation
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

import static us.mn.state.dot.tms.client.widget.SwingRunner.runSwing;
import us.mn.state.dot.sonar.SonarObject;
import us.mn.state.dot.sonar.client.ProxyListener;
import us.mn.state.dot.sonar.client.TypeCache;

/**
 * A class for watching attributes of a sonar object.
 *
 * @author Douglas Lau
 */
public class ProxyWatcher<T extends SonarObject> {

	/** Proxy cache */
	private final TypeCache<T> cache;

	/** Proxy view to be updated on EDT */
	private final ProxyView<T> view;

	/** Flag to watch proxy */
	private final boolean watch;

	/** Proxy listener */
	private final ProxyListener<T> listener = new ProxyListener<T>() {
		public void proxyAdded(T p) { }
		public void enumerationComplete() {
			view.enumerationComplete();
		}
		public void proxyRemoved(T p) {
			if (proxy == p) {
				proxy = null;
				clear();
			}
		}
		public void proxyChanged(T p, final String a) {
			if (proxy == p)
				update(p, a);
		}
	};

	/** Proxy being watched */
	private T proxy;

	/** Set a new proxy to watch */
	public void setProxy(T p) {
		// Need to synchronize on type cache in case sonar thread is
		// calling proxyChanged after calling watchObject.  This happens
		// when setProxy is called more than once quickly.
		synchronized (cache) {
			T op = proxy;
			proxy = p;
			if (watch) {
				if (op != null)
					cache.ignoreObject(op);
				if (p != null)
					cache.watchObject(p);
			}
			if (p != null)
				update(p, null);
			else if (op != null)
				clear();
		}
	}

	/** Create a new proxy watcher.
	 * @param c TypeCache of proxy type.
	 * @param pv ProxyView to update.
	 * @param w true to watch/ignore selected proxy. */
	public ProxyWatcher(TypeCache<T> c, ProxyView<T> pv, boolean w) {
		cache = c;
		view = pv;
		watch = w;
	}

	/** Initialize a proxy watcher */
	public void initialize() {
		if(proxy != null)
			throw new IllegalStateException("Must init first");
		cache.addProxyListener(listener);
	}

	/** Dispose of a proxy watcher */
	public void dispose() {
		cache.removeProxyListener(listener);
		setProxy(null);
	}

	/** Update attribute on view (on EDT) */
	private void update(final T p, final String a) {
		runSwing(new Runnable() {
			public void run() {
				view.update(p, a);
			}
		});
	}

	/** Clear the view (on EDT) */
	private void clear() {
		runSwing(new Runnable() {
			public void run() {
				view.clear();
			}
		});
	}
}
