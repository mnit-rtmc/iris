/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2010  Minnesota Department of Transportation
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
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import us.mn.state.dot.sched.AbstractJob;
import us.mn.state.dot.sonar.SonarObject;
import us.mn.state.dot.sonar.client.ProxyListener;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.toast.SonarObjectForm;
import us.mn.state.dot.tms.utils.NumericAlphaComparator;

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

	/** User session */
	protected final Session session;

	/** Proxy type cache */
	protected final TypeCache<T> cache;

	/** Proxy columns */
	protected final ProxyColumn<T>[] columns;

	/** Create an empty set of proxies */
	protected TreeSet<T> createProxySet() {
		return new TreeSet<T>(new NumericAlphaComparator<T>());
	}

	/** Set of all proxies */
	protected final TreeSet<T> proxies = createProxySet();

	/** Create a new proxy table model */
	public ProxyTableModel(Session s, TypeCache<T> c) {
		session = s;
		cache = c;
		columns = createColumns();
	}

	/** Initialize the proxy table model. This cannot be done in the
	 * constructor because subclasses may not be fully constructed. */
	public void initialize() {
		cache.addProxyListener(this); // add all children to table model
	}

	/** Dispose of the proxy table model */
	public void dispose() {
		cache.removeProxyListener(this);
	}

	/** Create the columns in the model via method, which is called
	 * prior to subclass init blocks and constructors. */
	abstract protected ProxyColumn<T>[] createColumns();

	/** Get the count of columns in the table */
	public int getColumnCount() {
		return columns.length;
	}

	/** Get the proxy column at the given column index */
	public ProxyColumn getProxyColumn(int col) {
		if(col >= 0 && col < columns.length)
			return columns[col];
		else
			return null;
	}

	/** Get the class of the specified column */
	public Class getColumnClass(int col) {
		ProxyColumn pc = getProxyColumn(col);
		if(pc != null)
			return pc.getColumnClass();
		else
			return null;
	}

	/** Get the value at the specified cell */
	public Object getValueAt(int row, int col) {
		T proxy = getProxy(row);
		if(proxy != null) {
			ProxyColumn pc = getProxyColumn(col);
			if(pc != null)
				return pc.getValueAt(proxy);
		}
		return null;
	}

	/** Check if the specified cell is editable */
	public boolean isCellEditable(int row, int col) {
		ProxyColumn pc = getProxyColumn(col);
		return pc != null && pc.isEditable(getProxy(row));
	}

	/** Set the value at the specified cell */
	public void setValueAt(Object value, int row, int col) {
		ProxyColumn pc = getProxyColumn(col);
		if(pc != null)
			pc.setValueAt(getProxy(row), value);
	}

	/** Create the table column model */
	public TableColumnModel createColumnModel() {
		TableColumnModel m = new DefaultTableColumnModel();
		for(int i = 0; i < columns.length; ++i)
			columns[i].addColumn(m, i);
		return m;
	}

	/** Add a new proxy to the table model */
	protected int doProxyAdded(T proxy) {
		synchronized(proxies) {
			if(proxies.add(proxy))
				return getRow(proxy);
			else
				return -1;
		}
	}

	/** Add a new proxy to the table model */
	protected final void proxyAddedSlow(T proxy) {
		final int row = doProxyAdded(proxy);
		if(row >= 0) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					fireTableRowsInserted(row, row);
				}
			});
		}
	}

	/** Add a new proxy to the table model */
	public final void proxyAdded(final T proxy) {
		// Don't hog the SONAR TaskProcessor thread
		new AbstractJob() {
			public void perform() {
				proxyAddedSlow(proxy);
			}
		}.addToScheduler();
	}

	/** Enumeration of all proxies is complete */
	public void enumerationComplete() {
		// Nothing to do
	}

	/** Remove a proxy from the table model */
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

	/** Remove a proxy from the table model */
	protected final void proxyRemovedSlow(T proxy) {
		final int row = doProxyRemoved(proxy);
		if(row >= 0) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					fireTableRowsDeleted(row, row);
				}
			});
		}
	}

	/** Remove a proxy from the table model */
	public final void proxyRemoved(final T proxy) {
		// Don't hog the SONAR TaskProcessor thread
		new AbstractJob() {
			public void perform() {
				proxyRemovedSlow(proxy);
			}
		}.addToScheduler();
	}

	/** Change a proxy in the table model */
	protected void proxyChangedSlow(T proxy, String attrib) {
		int pre_row, post_row;
		synchronized(proxies) {
			pre_row = doProxyRemoved(proxy);
			post_row = doProxyAdded(proxy);
		}
		if(pre_row >= 0 && post_row < 0) {
			final int r = pre_row;
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					fireTableRowsDeleted(r, r);
				}
			});
		}
		if(pre_row < 0 && post_row >= 0) {
			final int r = post_row;
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					fireTableRowsInserted(r, r);
				}
			});
		}
		if(pre_row >= 0 && post_row >= 0) {
			final int r0 = Math.min(pre_row, post_row);
			final int r1 = Math.max(pre_row, post_row);
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					fireTableRowsUpdated(r0, r1);
				}
			});
		}
	}

	/** Change a proxy in the table model */
	public final void proxyChanged(final T proxy, final String attrib) {
		// Don't hog the SONAR TaskProcessor thread
		new AbstractJob() {
			public void perform() {
				proxyChangedSlow(proxy, attrib);
			}
		}.addToScheduler();
	}

	/** Get the count of rows in the table */
	public int getRowCount() {
		synchronized(proxies) {
			return proxies.size() + 1;
		}
	}

	/** Get the proxy at the specified row */
	public T getProxy(int row) {
		if(row < 0)
			return null;
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

	/** Show the properties form for a proxy */
	public void showPropertiesForm(T proxy) {
		SonarObjectForm<T> prop = createPropertiesForm(proxy);
		if(prop != null)
			session.getDesktop().show(prop);
	}

	/** Create a properties form for one proxy */
	protected SonarObjectForm<T> createPropertiesForm(T proxy) {
		return null;
	}

	/** Determine if a properties form is available */
	public boolean hasProperties() {
		return false;
	}

	/** Determine if delete button is available */
	public boolean hasDelete() {
		return true;
	}

	/** Check if the user can add a proxy */
	public boolean canAdd(String n) {
		String tname = getSonarType();
		if(tname != null)
			return session.canAdd(tname, n);
		else
			return false;
	}

	/** Check if the user can add a proxy */
	public boolean canAdd() {
		return canAdd("oname");
	}

	/** Get the SONAR type name.  Subclasses must override this to allow
	 * canAdd permission checking to work correctly. */
	protected String getSonarType() {
		return null;
	}

	/** Check if the user can update a proxy */
	public boolean canUpdate(T proxy) {
		return session.canUpdate(proxy);
	}

	/** Check if the user can update a proxy */
	public boolean canUpdate(T proxy, String aname) {
		return session.canUpdate(proxy, aname);
	}

	/** Check if the user can remove a proxy */
	public boolean canRemove(T proxy) {
		return session.canRemove(proxy);
	}
}
