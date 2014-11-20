/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2014  Minnesota Department of Transportation
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
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import us.mn.state.dot.sonar.SonarObject;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.utils.NumericAlphaComparator;

/**
 * Table model for IRIS proxies.  This model allows a TableRowSorter to be used
 * with the table for sorting and filtering.
 *
 * @author Douglas Lau
 */
abstract public class ProxyTableModel2<T extends SonarObject>
	extends AbstractTableModel
{
	/** User session */
	private final Session session;

	/** Proxy type cache */
	private final TypeCache<T> cache;

	/** Proxy columns */
	private final ArrayList<ProxyColumn<T>> columns;

	/** Proxy list */
	private final ArrayList<T> list;

	/** Proxy listener for SONAR updates */
	private final SwingProxyAdapter<T> listener = new SwingProxyAdapter<T>()
	{
		protected Comparator<T> comparator() {
			return new NumericAlphaComparator<T>();
		}
		protected void proxyAddedSwing(T proxy) {
			int i = doProxyAdded(proxy);
			if (i >= 0)
				fireTableRowsInserted(i, i);
		}
		protected void enumerationCompleteSwing(Collection<T> proxies) {
			for (T proxy: proxies)
				list.add(proxy);
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
			int i = getIndex(proxy);
			if (i >= 0)
				fireTableRowsUpdated(i, i);
		}
		protected boolean checkAttributeChange(String attr) {
			return ProxyTableModel2.this.checkAttributeChange(attr);
		}
	};

	/** Create a new proxy table model */
	public ProxyTableModel2(Session s, TypeCache<T> c) {
		session = s;
		cache = c;
		columns = createColumns();
		list = new ArrayList<T>();
	}

	/** Initialize the proxy table model. This cannot be done in the
	 * constructor because subclasses may not be fully constructed. */
	public void initialize() {
		cache.addProxyListener(listener);
	}

	/** Dispose of the proxy table model */
	public void dispose() {
		cache.removeProxyListener(listener);
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
		ProxyColumn pc = getProxyColumn(col);
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
			ProxyColumn pc = getProxyColumn(col);
			if (pc != null)
				return pc.getValueAt(proxy);
		}
		return null;
	}

	/** Check if the specified cell is editable */
	@Override
	public boolean isCellEditable(int row, int col) {
		ProxyColumn pc = getProxyColumn(col);
		return pc != null && pc.isEditable(getRowProxy(row));
	}

	/** Set the value at the specified cell */
	@Override
	public void setValueAt(Object value, int row, int col) {
		ProxyColumn pc = getProxyColumn(col);
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

	/** Add a new proxy to the table model */
	private int doProxyAdded(T proxy) {
		int n_size = list.size();
		for (int i = 0; i < n_size; ++i) {
			if (proxy == list.get(i))
				return -1;
		}
		list.add(proxy);
		return n_size;
	}

	/** Remove a proxy from the table model */
	private int doProxyRemoved(T proxy) {
		int i = getIndex(proxy);
		if (i >= 0)
			list.remove(i);
		return i;
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

	/** Show the properties form for a proxy */
	public void showPropertiesForm(T proxy) {
		SonarObjectForm<T> prop = createPropertiesForm(proxy);
		if (prop != null)
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
		if (tname != null)
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
