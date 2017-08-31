/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2017  Minnesota Department of Transportation
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
import javax.swing.RowSorter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import us.mn.state.dot.sonar.SonarObject;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.widget.ITableModel;
import us.mn.state.dot.tms.utils.NumericAlphaComparator;

/**
 * Table model for IRIS proxies.  This model allows a RowSorter to be used
 * with the table for sorting and filtering.
 *
 * @author Douglas Lau
 */
abstract public class ProxyTableModel<T extends SonarObject>
	extends AbstractTableModel implements ITableModel
{
	/** User session */
	protected final Session session;

	/** Get the user session */
	public Session getSession() {
		return session;
	}

	/** Proxy descriptor */
	protected final ProxyDescriptor<T> descriptor;

	/** Visible row count */
	private final int visible_row_count;

	/** Row height */
	private final int row_height;

	/** Proxy columns */
	private final ArrayList<ProxyColumn<T>> columns;

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
			return ProxyTableModel.this.comparator();
		}
		protected void proxyAddedSwing(T proxy) {
			int i = doProxyAdded(proxy);
			if (i >= 0)
				fireTableRowsInserted(i, i);
		}
		protected void enumerationCompleteSwing(Collection<T> proxies) {
			for (T proxy: proxies) {
				if (check(proxy))
					list.add(proxy);
			}
			int sz = list.size() - 1;
			if (sz >= 0)
				fireTableRowsInserted(0, sz);
		}
		protected void proxyRemovedSwing(T proxy) {
			int i = doProxyRemoved(proxy);
			if (i >= 0)
				fireTableRowsDeleted(i, i);
		}
		protected void proxyChangedSwing(T proxy, String attr) {
			ProxyTableModel.this.proxyChangedSwing(proxy);
		}
		protected boolean checkAttributeChange(String attr) {
			return ProxyTableModel.this.checkAttributeChange(attr);
		}
	};

	/** Create a new proxy table model.
	 * @param s User session.
	 * @param d Proxy descriptor.
	 * @param vrc Visible row count.
	 * @param rh Row height. */
	public ProxyTableModel(Session s, ProxyDescriptor<T> d, int vrc, int rh)
	{
		session = s;
		descriptor = d;
		visible_row_count = vrc;
		row_height = rh;
		columns = createColumns();
		list = new ArrayList<T>();
	}

	/** Create a new proxy table model.
	 * @param s User session.
	 * @param d Proxy descriptor.
	 * @param vrc Visible row count. */
	public ProxyTableModel(Session s, ProxyDescriptor<T> d, int vrc) {
		this(s, d, vrc, 18);
	}

	/** Initialize the proxy table model. This cannot be done in the
	 * constructor because subclasses may not be fully constructed. */
	public void initialize() {
		descriptor.cache.addProxyListener(listener);
	}

	/** Dispose of the proxy table model */
	public void dispose() {
		descriptor.cache.removeProxyListener(listener);
	}

	/** Create the columns in the model via method, which is called
	 * prior to subclass init blocks and constructors. */
	abstract protected ArrayList<ProxyColumn<T>> createColumns();

	/** Get the count of columns in the table */
	@Override
	public int getColumnCount() {
		return columns.size();
	}

	/** Get the proxy column at the given column index */
	public ProxyColumn<T> getProxyColumn(int col) {
		if (col >= 0 && col < columns.size())
			return columns.get(col);
		else
			return null;
	}

	/** Get the class of the specified column */
	@Override
	public Class getColumnClass(int col) {
		ProxyColumn<T> pc = getProxyColumn(col);
		if (pc != null)
			return pc.getColumnClass();
		else
			return null;
	}

	/** Get the value at the specified cell */
	@Override
	public Object getValueAt(int row, int col) {
		T proxy = getRowProxy(row);
		if (proxy != null) {
			ProxyColumn<T> pc = getProxyColumn(col);
			if (pc != null)
				return pc.getValueAt(proxy);
		}
		return null;
	}

	/** Get tooltip text for a cell */
	@Override
	public String getToolTipText(int row, int col) {
		return null;
	}

	/** Check if the specified cell is editable */
	@Override
	public boolean isCellEditable(int row, int col) {
		ProxyColumn<T> pc = getProxyColumn(col);
		return (pc != null) && pc.isEditable(getRowProxy(row));
	}

	/** Set the value at the specified cell */
	@Override
	public void setValueAt(Object value, int row, int col) {
		ProxyColumn<T> pc = getProxyColumn(col);
		if (pc != null)
			pc.setValueAt(getRowProxy(row), value);
	}

	/** Create the table column model */
	public TableColumnModel createColumnModel() {
		TableColumnModel m = new DefaultTableColumnModel();
		for (int i = 0; i < columns.size(); ++i)
			columns.get(i).addColumn(m, i);
		return m;
	}

	/** Check if a proxy is included in the list */
	protected boolean check(T proxy) {
		return true;
	}

	/** Add a new proxy to the table model */
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

	/** Remove a proxy from the table model */
	private int doProxyRemoved(T proxy) {
		int i = getIndex(proxy);
		if (i >= 0)
			list.remove(i);
		return i;
	}

	/** Change a proxy in the table model */
	protected void proxyChangedSwing(T proxy) {
		int pre = doProxyRemoved(proxy);
		int post = doProxyAdded(proxy);
		if (pre >= 0 && post >= 0) {
			int r0 = Math.min(pre, post);
			int r1 = Math.max(pre, post);
			fireTableRowsUpdated(r0, r1);
		} else if (pre >= 0 && post < 0)
			fireTableRowsDeleted(pre, pre);
		else if (pre < 0 && post >= 0)
			fireTableRowsInserted(post, post);
	}

	/** Check if an attribute change is interesting */
	protected boolean checkAttributeChange(String attr) {
		return true;
	}

	/** Get the count of rows in the table */
	@Override
	public int getRowCount() {
		return list.size();
	}

	/** Get the proxy at the specified row */
	public T getRowProxy(int row) {
		return (row >= 0) ? list.get(row) : null;
	}

	/** Get the index of the given proxy */
	public int getIndex(T proxy) {
		for (int i = 0; i < list.size(); ++i) {
			if (proxy == list.get(i))
				return i;
		}
		return -1;
	}

	/** Get the visible row count */
	public final int getVisibleRowCount() {
		return visible_row_count;
	}

	/** Get the row height */
	public final int getRowHeight() {
		return row_height;
	}

	/** Get a table row sorter */
	public RowSorter<ProxyTableModel<T>> createSorter() {
		return null;
	}

	/** Determine if a properties form is available */
	public final boolean hasProperties() {
		return descriptor.has_properties;
	}

	/** Show the properties form for a proxy */
	public void showPropertiesForm(T proxy) {
		SonarObjectForm<T> prop = descriptor.createPropertiesForm(
			proxy);
		if (prop != null)
			session.getDesktop().show(prop);
	}

	/** Determine if create and delete buttons are available */
	public final boolean hasCreateDelete() {
		return descriptor.has_create_delete;
	}

	/** Create an object with the given name */
	public void createObject(String name) {
		String n = name.trim();
		if (n.length() > 0)
			descriptor.cache.createObject(n);
	}

	/** Determine if name text field is available */
	public final boolean hasName() {
		return descriptor.has_name;
	}

	/** Check if the user can add a proxy */
	public boolean canAdd(String n) {
		String tname = descriptor.tname;
		if (tname != null)
			return session.canAdd(tname, n);
		else
			return false;
	}

	/** Check if the user can add a proxy */
	public boolean canAdd() {
		return canAdd("oname");
	}

	/** Check if the user can write a proxy */
	public boolean canWrite(T proxy) {
		return session.canWrite(proxy);
	}

	/** Check if the user can write a proxy */
	public boolean canWrite(SonarObject proxy, String aname) {
		return session.canWrite(proxy, aname);
	}

	/** Check if the user can remove a proxy */
	public boolean canRemove(T proxy) {
		return session.canRemove(proxy);
	}
}
