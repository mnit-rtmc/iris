/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2005-2008  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.dms;

import java.util.HashMap;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumnModel;
import us.mn.state.dot.sonar.Checker;
import us.mn.state.dot.sonar.SonarObject;
import us.mn.state.dot.sonar.client.ProxyListener;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.DmsSignGroup;
import us.mn.state.dot.tms.SignGroup;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;

/**
 * Table model for sign groups.
 *
 * @author Douglas Lau
 */
public class SignGroupModel extends ProxyTableModel<SignGroup> {

	/** Count of columns in table model */
	static protected final int COLUMN_COUNT = 2;

	/** Sign group name column number */
	static protected final int COL_NAME = 0;

	/** Member column number */
	static protected final int COL_MEMBER = 1;

	/** Create the table column model */
	static public TableColumnModel createColumnModel() {
		TableColumnModel m = new DefaultTableColumnModel();
		m.addColumn(createColumn(COL_NAME, 200, "Sign Group"));
		m.addColumn(createColumn(COL_MEMBER, 50, "Member"));
		return m;
	}

	/** Lookup a DMS sign group */
	protected DmsSignGroup lookupDmsSignGroup(final SignGroup group) {
		return dms_sign_groups.findObject(new Checker<DmsSignGroup>() {
			public boolean check(DmsSignGroup g) {
				return g.getSignGroup() == group &&
					g.getDms().equals(dms_id);
			}
		});
	}

	/** Test if the DMS is a member of a sign group */
	protected boolean isSignGroupMember(SignGroup group) {
		return lookupDmsSignGroup(group) != null;
	}

	/** Check if a sign group should be listed */
	protected boolean isListed(SignGroup group) {
		if(!group.getLocal())
			return true;
		return dms_id.equals(group.getName());
	}

	/** Add a new proxy to the table model */
	protected int doProxyAdded(SignGroup proxy) {
		if(isListed(proxy))
			return super.doProxyAdded(proxy);
		else
			return -1;
	}

	/** DMS identifier */
	protected final String dms_id;

	/** DMS sign group type cache */
	protected final TypeCache<DmsSignGroup> dms_sign_groups;

	/** Listener for DMS sign group proxies */
	protected final ProxyListener<DmsSignGroup> listener;

	/** 
	 * Create a new sign group table model 
	 * @param dms DMS id.
	 * @param d Sonar type cache
	 * @param a True if user has admin privileges.
	 */
	public SignGroupModel(String dms, TypeCache<DmsSignGroup> d,
		TypeCache<SignGroup> g, boolean a)
	{
		super(g, a);
		dms_id = dms;
		dms_sign_groups = d;
		initialize();
		final SignGroupModel model = this;
		listener = new ProxyListener<DmsSignGroup>() {
			public void proxyAdded(DmsSignGroup proxy) {
				model.proxyChanged(proxy.getSignGroup(),
					"member");
			}
			public void enumerationComplete() { }
			public void proxyRemoved(DmsSignGroup proxy) {
				model.proxyChanged(proxy.getSignGroup(),
					"member");
			}
			public void proxyChanged(DmsSignGroup proxy, String a) {
				// NOTE: this should never happen
				model.proxyChanged(proxy.getSignGroup(), a);
			}
		};
		dms_sign_groups.addProxyListener(listener);
	}

	/** Dispose of the proxy table model */
	public void dispose() {
		dms_sign_groups.removeProxyListener(listener);
		super.dispose();
	}

	/** Get the count of columns in the table */
	public int getColumnCount() {
		return COLUMN_COUNT;
	}

	/** Get the value at the specified cell */
	public Object getValueAt(int row, int column) {
		SignGroup g = getProxy(row);
		if(g != null)
			return getValue(g, column);
		else
			return null;
	}

	/** Get the value of a sign group column */
	protected Object getValue(SignGroup g, int column) {
		switch(column) {
		case COL_NAME:
			return g.getName();
		case COL_MEMBER:
			return isSignGroupMember(g);
		}
		return null;
	}

	/** Get the column class */
	public Class getColumnClass(int column) {
		if(column == COL_NAME)
			return String.class;
		else
			return Boolean.class;
	}

	/** Check if the specified cell is editable */
	public boolean isCellEditable(int row, int column) {
		if(column == COL_MEMBER)
			return admin;
		else
			return admin && isLastRow(row);
	}

	/** Set the value at the specified cell */
	public void setValueAt(Object value, int row, int column) {
		SignGroup g = getProxy(row);
		switch(column) {
		case COL_NAME:
			String v = value.toString().trim();
			if(v.length() > 0)
				createSignGroup(v, v.equals(dms_id));
			break;
		case COL_MEMBER:
			if(g != null) {
				Boolean b = (Boolean)value;
				if(b.booleanValue())
					createDmsSignGroup(g);
				else
					destroyDmsSignGroup(g);
			}
			break;
		}
	}

	/** Create a new sign group */
	protected void createSignGroup(String name, boolean local) {
		HashMap<String, Object> attrs = new HashMap<String, Object>();
		attrs.put("local", local);
		cache.createObject(name, attrs);
	}

	/** Create a new DMS sign group */
	protected void createDmsSignGroup(SignGroup g) {
		String name = g.getName() + "_" + dms_id;
		HashMap<String, Object> attrs = new HashMap<String, Object>();
		attrs.put("dms", dms_id);
		attrs.put("sign_group", g);
		dms_sign_groups.createObject(name, attrs);
	}

	/** Destroy a DMS sign group */
	protected void destroyDmsSignGroup(SignGroup g) {
		DmsSignGroup dsg = lookupDmsSignGroup(g);
		if(dsg != null)
			dsg.destroy();
	}
}
