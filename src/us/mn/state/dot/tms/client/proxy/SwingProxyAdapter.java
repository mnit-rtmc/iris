/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2014  Minnesota Department of Transportation
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

import java.util.Collection;
import java.util.Comparator;
import java.util.TreeSet;
import us.mn.state.dot.sonar.SonarObject;
import us.mn.state.dot.sonar.client.ProxyListener;
import static us.mn.state.dot.tms.client.widget.SwingRunner.runSwing;

/**
 * An adapter to use ProxyListener for Swing models.
 *
 * @author Douglas Lau
 */
abstract public class SwingProxyAdapter<T extends SonarObject>
	implements ProxyListener<T>
{
	/** Set of proxies used until the enumeration is complete */
	private final TreeSet<T> proxies = new TreeSet<T>(comparator());

	/** Flag to pass along notifications */
	private boolean notify = false;

	/** Add a proxy.
	 * @see ProxyListener. */
	@Override
	public final void proxyAdded(final T proxy) {
		if (notify) {
			runSwing(new Runnable() {
				public void run() {
					proxyAddedSwing(proxy);
				}
			});
		} else
			proxies.add(proxy);
	}

	/** Enumeration of proxies is complete.
	 * @see ProxyListener. */
	@Override
	public final void enumerationComplete() {
		notify = true;
		runSwing(new Runnable() {
			public void run() {
				enumerationCompleteSwing(proxies);
				proxies.clear();
			}
		});
	}

	/** Remove a proxy.
	 * @see ProxyListener. */
	@Override
	public final void proxyRemoved(final T proxy) {
		if (notify) {
			runSwing(new Runnable() {
				public void run() {
					proxyRemovedSwing(proxy);
				}
			});
		}
	}

	/** A proxy has been changed.
	 * @see ProxyListener. */
	@Override
	public final void proxyChanged(final T proxy, final String attr) {
		if (notify) {
			runSwing(new Runnable() {
				public void run() {
					proxyChangedSwing(proxy, attr);
				}
			});
		}
	}

	/** Dispose of the adapter */
	public final void dispose() {
		proxies.clear();
	}

	/** Get a proxy comparator */
	abstract protected Comparator<T> comparator();

	/** Add a proxy */
	abstract protected void proxyAddedSwing(T proxy);

	/** Enumeration of proxies is complete */
	abstract protected void enumerationCompleteSwing(Collection<T> proxies);

	/** Remove a proxy */
	abstract protected void proxyRemovedSwing(T proxy);

	/** A proxy has been changed */
	abstract protected void proxyChangedSwing(T proxy, String attr);
}
