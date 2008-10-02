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
import us.mn.state.dot.tms.SystemAttribute;
import us.mn.state.dot.tms.SystemAttributeImpl;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;

/**
 * Table model for system attributes.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class SystemAttributeTableModel extends ProxyTableModel<SystemAttribute> 
{

	/** Count of columns in table model */
	static protected final int COLUMN_COUNT = 2;

	/** name column number */
	static protected final int COL_NAME = 0;

	/** value column number */
	static protected final int COL_VALUE = 1;

	/** Cell renderer for this table */
	static protected final DefaultTableCellRenderer RENDERER =
		new DefaultTableCellRenderer();
	static {
		RENDERER.setHorizontalAlignment(SwingConstants.CENTER);
	}

	/** 
	 *  Create a new table model.
	 *  @param admin True if admin else false.
	 *  @param tc TypeCache for the table items being displayed/edited.
	 */
	public SystemAttributeTableModel(boolean arg_admin, 
		TypeCache<SystemAttribute> arg_tc) 
	{
		super(arg_tc, arg_admin);
		initialize();	// in superclass, sets this as listener (ProxyListener)
	}

	/** return the class for each column */ /*
	public Class getColumnClass(int columnIndex) {
		return getValueAt(0, columnIndex).getClass();
	} */

	/** attribute name for a new proxy, via last row in table */
	protected String m_new_name="";

	/** set attribute name for a new proxy */
	protected void setNewAttribName(String arg_name) {
		m_new_name = (arg_name == null ? "" : arg_name).trim();
	}

	/** attribute value for a new proxy, via last row in table */
	protected String m_new_value="";

	/** set attribute value for a new proxy */
	protected void setNewAttribValue(String arg_value) {
		m_new_value = (arg_value == null ? "" : arg_value).trim();
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
	protected TreeSet<SystemAttribute> createProxySet() {
		return new TreeSet<SystemAttribute>(
			new SystemAttributeComparator());
	}

	/** Add a new proxy to the table model, via interface ProxyListener */
	public int doProxyAdded(SystemAttribute proxy) {
		if(proxyInModel(proxy))
			return super.doProxyAdded(proxy);
		return -1;
	}

	/** Does the specified proxy fit into this model? */
	public boolean proxyInModel(SystemAttribute proxy) {
		if( proxy==null )
			return false;
		return true; //FIXME m_name.equals(proxy.getName());
	}

	/** Get the value of a column */
	protected Object getValue(SystemAttribute t, int col) {
		if(col == COL_NAME)
			return t.getName();
		else if(col == COL_VALUE)
			return t.getValue();
		else {
			String err="bogus column value in getValue";
			System.err.println(err);
			assert false : err;
		}
		return null;
	}

	/** Get the value at the specified cell */
	public Object getValueAt(int row, int col) {
		SystemAttribute t = getProxy(row);
		if(t != null)
			return getValue(t, col);
		// return current values
		if(col == COL_NAME)
			return m_new_name;
		else if(col == COL_VALUE)
			return m_new_value;
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
		SystemAttribute t = getProxy(row);
		if(t == null)
			addRow(value, col);
		else
			setValue(t, col, value);
	}

	/** Set the value of the specified sign text column */
	protected void setValue(SystemAttribute t, int col, Object value) {
		if(col == COL_NAME)
			; // FIXME t.setName(value.toString());
		else if(col == COL_VALUE)
			t.setValue(value.toString());
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

		if(col == COL_NAME) {
			setNewAttribName(value.toString());
			createSystemAttribute();
		}
		else if(col == COL_VALUE) {
			setNewAttribValue(value.toString());
			createSystemAttribute();
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
			for(SystemAttribute t: proxies)
				names.add(t.getName());
		}
		return names;
	}

	/** Create a new SystemAttribute using the current field values */
	protected void createSystemAttribute() {
		// only create new attribute if all items specified
		if( m_new_name.length() > 0 && m_new_value.length() > 0 ) {
			createSystemAttribute(m_new_name, m_new_value);
			setNewAttribName("");
			setNewAttribValue("");
		}
	}

	/** Create a new SystemAttribute using the specified args */
	protected void createSystemAttribute(String aname,String avalue) {
		if(aname==null || aname.length()<=0 || avalue==null) {
			assert false : "bogus arg in createSystemAttribute";
			return;
		}
		HashMap<String, Object> attrs = new HashMap<String, Object>();
		attrs.put("value", avalue);
		cache.createObject(aname, attrs);
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

