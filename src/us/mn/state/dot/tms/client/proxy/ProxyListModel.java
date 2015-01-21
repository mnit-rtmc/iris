/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2015  Minnesota Department of Transportation
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import javax.swing.AbstractListModel;
import us.mn.state.dot.sonar.SonarObject;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.utils.NumericAlphaComparator;

/**
 * A swing ListModel kept in sync with a SONAR TypeCache.
 *
 * @author Douglas Lau
 */
public class ProxyListModel<T extends SonarObject>
	extends AbstractListModel
{
	/** Proxy type cache */
	private final TypeCache<T> cache;

	/** Proxy list */
	private final ArrayList<T> list;

	/** Proxy comparator */
	private final Comparator<T> comp = comparator();

	/** Get a proxy comparator */
	protected Comparator<T> comparator() {
		return new NumericAlphaComparator<T>();
	}

	/** Proxy listener for SONAR updates */
	private final SwingProxyAdapter<T> listener = new SwingProxyAdapter<T>()
	{
		protected Comparator<T> comparator() {
			return ProxyListModel.this.comp;
		}
		protected void proxyAddedSwing(T proxy) {
			int i = doProxyAdded(proxy);
			if (i >= 0)
				fireIntervalAdded(this, i, i);
		}
		protected void enumerationCompleteSwing(Collection<T> proxies) {
			for (T proxy: proxies) {
				if (check(proxy))
					list.add(proxy);
			}
			int sz = list.size() - 1;
			if (sz >= 0)
				fireIntervalAdded(this, 0, sz);
		}
		protected void proxyRemovedSwing(T proxy) {
			int i = doProxyRemoved(proxy);
			if (i >= 0)
				fireIntervalRemoved(this, i, i);
		}
		protected void proxyChangedSwing(T proxy, String attr) {
			ProxyListModel.this.proxyChangedSwing(proxy);
		}
	};

	/** Create a new proxy list model */
	public ProxyListModel(TypeCache<T> c) {
		cache = c;
		list = new ArrayList<T>();
	}

	/** Initialize the proxy list model. This cannot be done in the
	 * constructor because subclasses may not be fully constructed. */
	public void initialize() {
		cache.addProxyListener(listener);
	}

	/** Dispose of the proxy model */
	public void dispose() {
		cache.removeProxyListener(listener);
		listener.dispose();
	}

	/** Check if a proxy is included in the list */
	protected boolean check(T proxy) {
		return true;
	}

	/** Add a new proxy to the model */
	private int doProxyAdded(T proxy) {
		if (check(proxy)) {
			int n_size = list.size();
			for (int i = 0; i < n_size; ++i) {
				int c = comp.compare(proxy, list.get(i));
				if (c == 0)
					return -1;
				if (c < 0) {
					list.add(i, proxy);
					return i;
				}
			}
			list.add(proxy);
			return n_size;
		} else
			return -1;
	}

	/** Remove a proxy from the model */
	protected int doProxyRemoved(T proxy) {
		int i = getIndex(proxy);
		if (i >= 0)
			list.remove(i);
		return i;
	}

	/** Change a proxy in the list model */
	private void proxyChangedSwing(T proxy) {
		int pre = doProxyRemoved(proxy);
		int post = doProxyAdded(proxy);
		if (pre >= 0 && post >= 0) {
			int r0 = Math.min(pre, post);
			int r1 = Math.max(pre, post);
			fireContentsChanged(this, r0, r1);
		} else if (pre >= 0 && post < 0)
			fireIntervalRemoved(this, pre, pre);
		else if (pre < 0 && post >= 0)
			fireIntervalAdded(this, post, post);
	}

	/** Get the size (for ListModel) */
	@Override
	public int getSize() {
		return list.size();
	}

	/** Get the element at the specified index (for ListModel) */
	@Override
	public Object getElementAt(int index) {
		return list.get(index);
	}

	/** Get the proxy at the specified index */
	public T getProxy(int i) {
		return list.get(i);
	}

	/** Get the index of the given proxy */
	public int getIndex(T proxy) {
		for (int i = 0; i < list.size(); ++i) {
			if (proxy == list.get(i))
				return i;
		}
		return -1;
	}
}
