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
package us.mn.state.dot.tms.client.proxy;

import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeSet;
import javax.swing.AbstractListModel;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import us.mn.state.dot.sched.AbstractJob;
import us.mn.state.dot.sonar.SonarObject;
import us.mn.state.dot.sonar.client.ProxyListener;
import us.mn.state.dot.sonar.client.TypeCache;

/**
 * List model for IRIS proxies
 *
 * @author Douglas Lau
 */
public class ProxyListModel<T extends SonarObject>
	extends AbstractListModel implements ProxyListener<T>
{
	/** Proxy type cache */
	protected final TypeCache<T> cache;

	/** Create an empty set of proxies */
	protected TreeSet<T> createProxySet() {
		return new TreeSet<T>(
			new Comparator<T>() {
				public int compare(T t0, T t1) {
					return t0.getName().compareTo(
						t1.getName());
				}
				public boolean equals(Object o) {
					return o == this;
				}
				public int hashCode() {
					return super.hashCode();
				}
			}
		);
	}

	/** Set of all proxies */
	protected final TreeSet<T> proxies = createProxySet();

	/** Create a new proxy list model */
	public ProxyListModel(TypeCache<T> c) {
		cache = c;
	}

	/** Initialize the proxy list model. This cannot be done in the
	 * constructor because subclasses may not be fully constructed. */
	public void initialize() {
		cache.addProxyListener(this);
	}

	/** Dispose of the proxy model */
	public void dispose() {
		cache.removeProxyListener(this);
	}

	/** Add a new proxy to the model */
	protected int doProxyAdded(T proxy) {
		synchronized(proxies) {
			proxies.add(proxy);
			return getRow(proxy);
		}
	}

	/** Add a new proxy to the list model */
	protected void proxyAddedSlow(T proxy) {
		final int row = doProxyAdded(proxy);
		if(row >= 0) {
			final ListModel model = this;
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					fireIntervalAdded(model, row, row);
				}
			});
		}
	}

	/** Add a new proxy to the list model */
	public void proxyAdded(final T proxy) {
		// Don't hog the SONAR TaskProcessor thread
		new AbstractJob() {
			public void perform() {
				proxyAddedSlow(proxy);
			}
		}.addToScheduler();
	}

	/** Remove a proxy from the model */
	protected int doProxyRemoved(T proxy) {
		synchronized(proxies) {
			int row = getRow(proxy);
			proxies.remove(proxy);
			return row;
		}
	}

	/** Remove a proxy from the model */
	protected void proxyRemovedSlow(final T proxy) {
		final ListModel model = this;
		final int row = doProxyRemoved(proxy);
		if(row >= 0) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					fireIntervalRemoved(model, row, row);
				}
			});
		}
	}

	/** Remove a proxy from the model */
	public void proxyRemoved(final T proxy) {
		// Don't hog the SONAR TaskProcessor thread
		new AbstractJob() {
			public void perform() {
				proxyRemovedSlow(proxy);
			}
		}.addToScheduler();
	}

	/** Change a proxy in the model */
	protected void proxyChangedSlow(final T proxy, final String attrib) {
		final ListModel model = this;
		int pre_row, post_row;
		synchronized(proxies) {
			pre_row = preChangeRow(proxy);
			post_row = postChangeRow(proxy);
		}
		if(pre_row >= 0 && post_row < 0) {
			final int row = pre_row;
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					fireIntervalRemoved(model, row, row);
				}
			});
		}
		if(pre_row < 0 && post_row >= 0) {
			final int row = post_row;
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					fireIntervalAdded(model, row, row);
				}
			});
		}
		if(pre_row >= 0 && post_row >= 0) {
			final int r0 = Math.min(pre_row, post_row);
			final int r1 = Math.max(pre_row, post_row);
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					fireContentsChanged(model, r0, r1);
				}
			});
		}
	}

	/** Change a proxy in the list model */
	public void proxyChanged(final T proxy, final String attrib) {
		// Don't hog the SONAR TaskProcessor thread
		new AbstractJob() {
			public void perform() {
				proxyChangedSlow(proxy, attrib);
			}
		}.addToScheduler();
	}

	/** Find and remove a proxy which may not be in proper sort order */
	protected int preChangeRow(T proxy) {
		Iterator<T> it = proxies.iterator();
		for(int i = 0; it.hasNext(); i++) {
			if(proxy.equals(it.next())) {
				it.remove();
				return i;
			}
		}
		return -1;
	}

	/** Handle a proxy after a change has happened */
	protected int postChangeRow(T proxy) {
		return doProxyAdded(proxy);
	}

	/** Get the size (for ListModel) */
	public int getSize() {
		synchronized(proxies) {
			return proxies.size();
		}
	}

	/** Get the element at the specified index (for ListModel) */
	public Object getElementAt(int index) {
		return getProxy(index);
	}

	/** Get the proxy at the specified row */
	public T getProxy(int row) {
		synchronized(proxies) {
			Iterator<T> it = proxies.iterator();
			for(int i = 0; it.hasNext(); i++) {
				T proxy = it.next();
				if(i == row)
					return proxy;
			}
			return null;
		}
	}

	/** Get the row for the specified proxy */
	protected int getRow(T proxy) {
		synchronized(proxies) {
			Iterator<T> it = proxies.iterator();
			for(int i = 0; it.hasNext(); i++) {
				if(proxy.equals(it.next()))
					return i;
			}
			return -1;
		}
	}

	/** Delete the specified row */
	public void deleteRow(int row) {
		T proxy = getProxy(row);
		if(proxy != null)
			proxy.destroy();
	}
}
