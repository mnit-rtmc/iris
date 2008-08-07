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
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import us.mn.state.dot.sonar.SonarObject;
import us.mn.state.dot.sonar.client.ProxyListener;
import us.mn.state.dot.sonar.client.TypeCache;

/**
 * Table model for IRIS proxies
 *
 * @author Douglas Lau
 */
abstract public class ProxyTableModel<T extends SonarObject>
	extends AbstractTableModel implements ProxyListener<T>
{
	/** Create a table column */
	static protected TableColumn createColumn(int column, int width,
		String header)
	{
		TableColumn c = new TableColumn(column, width);
		c.setHeaderValue(header);
		return c;
	}

	/** Proxy type cache */
	protected final TypeCache<T> cache;

	/** Administrator flag */
	protected final boolean admin;

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

	/** Create a new proxy table model */
	public ProxyTableModel(TypeCache<T> c, boolean a) {
		cache = c;
		admin = a;
	}

	/** Initialize the proxy table model. This cannot be done in the
	 * constructor because subclasses may not be fully constructed. */
	public void initialize() {
		cache.addProxyListener(this);
	}

	/** Dispose of the proxy table model */
	public void dispose() {
		cache.removeProxyListener(this);
	}

	/** Add a new proxy to the table model */
	protected int doProxyAdded(T proxy) {
		synchronized(proxies) {
			proxies.add(proxy);
			return getRow(proxy);
		}
	}

	/** Add a new proxy to the table model */
	public void proxyAdded(T proxy) {
		final int row = doProxyAdded(proxy);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				fireTableRowsInserted(row, row);
			}
		});
	}

	/** Remove a proxy from the table model */
	protected int doProxyRemoved(T proxy) {
		synchronized(proxies) {
			int row = getRow(proxy);
			proxies.remove(proxy);
			return row;
		}
	}

	/** Remove a proxy from the table model */
	public void proxyRemoved(T proxy) {
		final int row = doProxyRemoved(proxy);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				fireTableRowsDeleted(row, row);
			}
		});
	}

	/** Change a proxy in the table model */
	public void proxyChanged(T proxy, String attrib) {
		int pre_row, post_row;
		synchronized(proxies) {
			pre_row = preChangeRow(proxy);
			if(pre_row < 0)
				return;
			post_row = doProxyAdded(proxy);
		}
		final int r0 = Math.min(pre_row, post_row);
		final int r1 = Math.max(pre_row, post_row);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				fireTableRowsUpdated(r0, r1);
			}
		});
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

	/** Get the count of rows in the table */
	public int getRowCount() {
		synchronized(proxies) {
			if(admin)
				return proxies.size() + 1;
			else
				return proxies.size();
		}
	}

	/** Check if the specified row is the last row in the table */
	public boolean isLastRow(int row) {
		synchronized(proxies) {
			return row == proxies.size();
		}
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
