/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2013  Minnesota Department of Transportation
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

import java.util.Iterator;
import java.util.TreeSet;
import javax.swing.AbstractListModel;
import javax.swing.ListModel;
import us.mn.state.dot.sched.Job;
import us.mn.state.dot.sched.SwingRunner;
import us.mn.state.dot.sonar.SonarObject;
import us.mn.state.dot.sonar.client.ProxyListener;
import us.mn.state.dot.sonar.client.TypeCache;
import static us.mn.state.dot.tms.client.IrisClient.WORKER;
import us.mn.state.dot.tms.utils.NumericAlphaComparator;

/**
 * List model for IRIS proxies. This class contains a TypeCache for a single
 * SonarObject. An object of this type is added as a listener to the TypeCache,
 * so that notification is received by instances of this class when any of the
 * SonarObjects change, are deleted, or new ones are added. This class also 
 * defines a TreeSet which contains proxy objects for the TypeCache objects.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class ProxyListModel<T extends SonarObject>
	extends AbstractListModel implements ProxyListener<T>
{
	/** Proxy type cache */
	private final TypeCache<T> cache;

	/** Create an empty set of proxies */
	protected TreeSet<T> createProxySet() {
		return new TreeSet<T>(new NumericAlphaComparator<T>());
	}

	/** Set of all proxies */
	private final TreeSet<T> proxies = createProxySet();

	/** Flag to indicate enumeration of all objects has completed */
	private boolean enumerated = false;

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
		synchronized(proxies) {
			proxies.clear();
		}
	}

	/** Add a new proxy to the model */
	protected int doProxyAdded(T proxy) {
		synchronized(proxies) {
			if(proxies.add(proxy) && enumerated)
				return getRow(proxy);
			else
				return -1;
		}
	}

	/** Add a new proxy to the list model */
	protected final void proxyAddedSlow(T proxy) {
		final int row = doProxyAdded(proxy);
		if(row >= 0) {
			final ListModel model = this;
			SwingRunner.invoke(new Runnable() {
				public void run() {
					fireIntervalAdded(model, row, row);
				}
			});
		}
	}

	/** Add a new proxy to the list model */
	@Override public final void proxyAdded(final T proxy) {
		// Don't hog the SONAR TaskProcessor thread
		WORKER.addJob(new Job() {
			public void perform() {
				proxyAddedSlow(proxy);
			}
		});
	}

	/** Enumeration of all proxies is complete */
	@Override public void enumerationComplete() {
		// Don't hog the SONAR TaskProcessor thread
		WORKER.addJob(new Job() {
			public void perform() {
				enumerationCompleteSlow();
			}
		});
	}

	/** Complete the enumeration of a proxy list */
	private void enumerationCompleteSlow() {
		synchronized(proxies) {
			enumerated = true;
			final int row = proxies.size() - 1;
			if(row >= 0) {
				final ListModel model = this;
				SwingRunner.invoke(new Runnable() {
					public void run() {
						fireIntervalAdded(model, 0,row);
					}
				});
			}
		}
	}

	/** Remove a proxy from the model */
	protected int doProxyRemoved(T proxy) {
		synchronized(proxies) {
			Iterator<T> it = proxies.iterator();
			for(int row = 0; it.hasNext(); row++) {
				if(proxy == it.next()) {
					it.remove();
					return row;
				}
			}
		}
		return -1;
	}

	/** Remove a proxy from the model */
	protected final void proxyRemovedSlow(final T proxy) {
		final ListModel model = this;
		final int row = doProxyRemoved(proxy);
		if(row >= 0) {
			SwingRunner.invoke(new Runnable() {
				public void run() {
					fireIntervalRemoved(model, row, row);
				}
			});
		}
	}

	/** Remove a proxy from the model */
	@Override public final void proxyRemoved(final T proxy) {
		// Don't hog the SONAR TaskProcessor thread
		WORKER.addJob(new Job() {
			public void perform() {
				proxyRemovedSlow(proxy);
			}
		});
	}

	/** Change a proxy in the model */
	protected final void proxyChangedSlow(final T proxy,
		final String attrib)
	{
		final ListModel model = this;
		int pre_row, post_row;
		synchronized(proxies) {
			pre_row = doProxyRemoved(proxy);
			post_row = doProxyAdded(proxy);
		}
		if(pre_row >= 0 && post_row < 0) {
			final int row = pre_row;
			SwingRunner.invoke(new Runnable() {
				public void run() {
					fireIntervalRemoved(model, row, row);
				}
			});
		}
		if(pre_row < 0 && post_row >= 0) {
			final int row = post_row;
			SwingRunner.invoke(new Runnable() {
				public void run() {
					fireIntervalAdded(model, row, row);
				}
			});
		}
		if(pre_row >= 0 && post_row >= 0) {
			final int r0 = Math.min(pre_row, post_row);
			final int r1 = Math.max(pre_row, post_row);
			SwingRunner.invoke(new Runnable() {
				public void run() {
					fireContentsChanged(model, r0, r1);
				}
			});
		}
	}

	/** Change a proxy in the list model */
	@Override public final void proxyChanged(final T proxy,
		final String attrib)
	{
		// Don't hog the SONAR TaskProcessor thread
		WORKER.addJob(new Job() {
			public void perform() {
				proxyChangedSlow(proxy, attrib);
			}
		});
	}

	/** Get the size (for ListModel) */
	@Override public int getSize() {
		synchronized(proxies) {
			return proxies.size();
		}
	}

	/** Get the element at the specified index (for ListModel) */
	@Override public Object getElementAt(int index) {
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
	public int getRow(T proxy) {
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

	/** Get the first proxy lower than the given proxy */
	public T lower(T proxy) {
		synchronized(proxies) {
			return proxies.lower(proxy);
		}
	}

	/** Get the first proxy higher than the given proxy */
	public T higher(T proxy) {
		synchronized(proxies) {
			return proxies.higher(proxy);
		}
	}
}
