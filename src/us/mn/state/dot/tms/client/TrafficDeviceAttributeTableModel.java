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
package us.mn.state.dot.tms.client;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeSet;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.sonar.SonarObject;
import us.mn.state.dot.tms.SignGroup;
import us.mn.state.dot.tms.TrafficDeviceAttribute;
import us.mn.state.dot.tms.TrafficDeviceAttributeImpl;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;

/**
 * Table model for traffic device attributes.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class TrafficDeviceAttributeTableModel extends ProxyTableModel<TrafficDeviceAttribute> {

	/** Count of columns in table model */
	static protected final int COLUMN_COUNT = 2;

	/** name column number */
	static protected final int COL_NAME = 0;

	/** value column number */
	static protected final int COL_VALUE = 1;

	/** the device id associated with this model, e.g. "V23" */
	protected String m_id;

	/** Cell renderer for this table */
	static protected final DefaultTableCellRenderer RENDERER =
		new DefaultTableCellRenderer();
	static {
		RENDERER.setHorizontalAlignment(SwingConstants.CENTER);
	}

	/** 
	 *  Create a new table model.
	 *  @param id The device id, e.g. "V1"
	 *  @param tc TypeCache for the table items being displayed/edited.
	 *  @param admin True if admin else false.
	 */
	public TrafficDeviceAttributeTableModel(String id,TypeCache<TrafficDeviceAttribute> tc, boolean admin) {
		super(tc, admin);
		assert id!=null : "id is null";
		m_id = (id==null ? "" : id);
		initialize();	// in superclass, sets this up listener (ProxyListener)
		//System.err.println("TrafficDeviceAttributeTableModel.TrafficDeviceAttributeTableModel() called. id="+m_id);
	}

	/** return the class for each column */ /*
	public Class getColumnClass(int columnIndex) {
		return getValueAt(0, columnIndex).getClass();
	} */

	/** attribute name for a new proxy, via last row in table */
	protected String m_new_aname="";

	/** set attribute name for a new proxy */
	protected void setNewAttribName(String aname) {
		m_new_aname = (aname == null ? "" : aname);
		m_new_aname = m_new_aname.trim();
	}

	/** attribute value for a new proxy, via last row in table */
	protected String m_new_avalue="";

	/** set attribute value for a new proxy */
	protected void setNewAttribValue(String avalue) {
		m_new_avalue = (avalue == null ? "" : avalue);
		m_new_avalue = m_new_avalue.trim();
	}

	/** Create a new table column */
	static protected TableColumn createColumn(int column, int width,
		String header)
	{
		assert header != null;
		TableColumn c = new TableColumn(column, width);
		c.setHeaderValue(header);
		if(column == COL_NAME || column == COL_VALUE)
			c.setCellRenderer(RENDERER);
		else {
			assert false;
			System.err.println("WARNING: bogus column");
			c.setCellRenderer(RENDERER);
		}
		return c;
	}

	/** Get the count of columns in the table */
	public int getColumnCount() {
		return COLUMN_COUNT;
	}

	/** Create an empty set of proxies */
	protected TreeSet<TrafficDeviceAttribute> createProxySet() {
		return new TreeSet<TrafficDeviceAttribute>(
			new TrafficDeviceAttributeComparator());
	}

	/** Add a new proxy to the table model, via interface ProxyListener */
	public int doProxyAdded(TrafficDeviceAttribute proxy) {
		//System.err.println("TrafficDeviceAttributeTableModel.doProxyAdded() called. proxy="+proxy.toString());
		if(proxyInModel(proxy))
			return super.doProxyAdded(proxy);
		return -1;
	}

	/** Does the specified proxy fit into this model? */
	public boolean proxyInModel(TrafficDeviceAttribute proxy) {
		//System.err.println("TrafficDeviceAttributeTableModel.proxyInModel() called. proxy="+proxy.toString()+", ret="+m_id.equals(proxy.getId()));
		if( proxy==null )
			return false;
		return m_id.equals(proxy.getId());
	}

	/** Get the value of a column */
	protected Object getValue(TrafficDeviceAttribute t, int col) {
		//System.err.println("TrafficDeviceAttributeTableModel.getValue() called. t="+t.toString()+", column="+col);
		if(col == COL_NAME)
			return t.getAttributeName();
		else if(col == COL_VALUE)
			return t.getAttributeValue();
		else {
			String err="bogus column value in getValue";
			System.err.println(err);
			assert false : err;
		}
		return null;
	}

	/** Get the value at the specified cell */
	public Object getValueAt(int row, int col) {
		//System.err.println("TrafficDeviceAttributeTableModel.getValueAt() called: row="+row+", col="+col);
		TrafficDeviceAttribute t = getProxy(row);
		if(t != null)
			return getValue(t, col);
		// return current values
		if(col == COL_NAME)
			return m_new_aname;
		else if(col == COL_VALUE)
			return m_new_avalue;
		else {
			String err="Bogus column value in getValueAt().";
			System.err.println(err);
			assert false : err;
		}
		return null;
	}

	/** Create the table column model */
	static public TableColumnModel createColumnModel() {
		TableColumnModel m = new DefaultTableColumnModel();
		m.addColumn(createColumn(COL_NAME, 36, "Name"));
		m.addColumn(createColumn(COL_VALUE, 200, "Value"));
		return m;
	}

	/** Check if the specified cell is editable */
	public boolean isCellEditable(int row, int col) {
		// only last row name is editable if an admin
		if(col == COL_NAME)
			return isLastRow(row) && admin;
		else if(col == COL_VALUE)
			return admin;
		else {
			String err="Bogus column value in isCellEditable().";
			System.err.println(err);
			assert false : err;
		}
		return admin;
	}

	/** Set the value at the specified cell */
	public void setValueAt(Object value, int row, int col) {
		//System.err.println("TrafficDeviceAttributeTableModel.setValueAt() called. value="+value+", row="+row+", col="+col);
		TrafficDeviceAttribute t = getProxy(row);
		if(t == null)
			addRow(value, col);
		else
			setValue(t, col, value);
	}

	/** Set the value of the specified sign text column */
	protected void setValue(TrafficDeviceAttribute t, int col, Object value) {
		//System.err.println("TrafficDeviceAttributeTableModel.setValue() called: t="+t.toString()+", col="+col+", value="+value);
		if(col == COL_NAME)
			t.setAttributeName(value.toString());
		else if(col == COL_VALUE)
			t.setAttributeValue(value.toString());
		else {
			String err = "bogus column value in setValue";
			System.err.println(err);
			assert false : err;
		}
	}
	
	/** Add a row to the table */
	protected void addRow(Object value, int col) {
		assert value != null : "value null in addRow()";
		value = (value == null ? "" : value);
		//System.err.println("TrafficDeviceAttributeTableModel.addRow() called: value="+value.toString()+", col="+col);

		if(col == COL_NAME) {
			setNewAttribName(value.toString());
			createTrafficDeviceAttribute();
		}
		else if(col == COL_VALUE) {
			setNewAttribValue(value.toString());
			createTrafficDeviceAttribute();
		} else {
			String err = "bogus column value in addRow";
			System.err.println(err);
			assert false : err;
		}
	}

	/** Get a set of attribute names */
	protected HashSet<String> getNames() {
		HashSet<String> names = new HashSet<String>();
		synchronized(proxies) {
			for(TrafficDeviceAttribute t: proxies)
				names.add(t.getName());
		}
		return names;
	}

	/** Create a new TrafficDeviceAttribute using the current field values */
	protected void createTrafficDeviceAttribute() {
		//System.err.println("TrafficDeviceAttributeTableModel.createTrafficDeviceAttribute() called");
		// only create new attribute if all items specified
		if( m_new_aname.length() > 0 && m_new_avalue.length() > 0 ) {
			createTrafficDeviceAttribute(m_id, 
				m_new_aname, m_new_avalue);
			setNewAttribName("");
			setNewAttribValue("");
		}
	}

	/** Create a new TrafficDeviceAttribute using the specified args */
	protected void createTrafficDeviceAttribute(String id,String aname,String avalue) {
		//System.err.println("TrafficDeviceAttributeTableModel.createTrafficDeviceAttribute(,,) called");
		if(id==null || id.length()<=0 || aname==null || 
			aname.length()<=0 || avalue==null) {
			assert false : "bogus arg in createTrafficDeviceAttribute";
			return;
		}

		String name = createName(id,aname);
		if(name == null || name.length()<=0)
			return;
		//System.err.println("TrafficDeviceAttributeTableModel.createTrafficDeviceAttribute(,,): name="+name+", id="+id+", aname="+aname+", avalue="+avalue);
		HashMap<String, Object> attrs = new HashMap<String, Object>();
		attrs.put("id", id);
		attrs.put("aname", aname);
		attrs.put("avalue", avalue);
		cache.createObject(name, attrs);
	}

	/** Create a new name using an id and aname */
	public static String createName(String id, String aname) {
		if (id==null || id.length()<=0 || aname==null || aname.length()<=0) {
			assert false : "bogus args in createName()";
			return null;
		}
		return id + "_" + aname;
	}

	/** toString */
	public String toString() {
		HashSet<String> hs=this.getNames();
		if( hs == null )
			return "";
		String ret="HashSet: size="+hs.size()+":";
		Iterator it = hs.iterator();
		while(it.hasNext()) {
			String val = (String)it.next();
			ret+=val;
			if(it.hasNext())
				ret+=", ";
		}
		ret+=".";
		return ret;		
	}
}

