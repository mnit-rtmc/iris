/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.schedule;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.TreeSet;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumnModel;
import us.mn.state.dot.sonar.Name;
import us.mn.state.dot.sonar.Namespace;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.ActionPlan;
import us.mn.state.dot.tms.TimeAction;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;

/**
 * Table model for time actions assigned to action plans
 *
 * @author Douglas Lau
 */
public class TimeActionModel extends ProxyTableModel<TimeAction> {

	/** Time parser formats */
	static protected final DateFormat[] TIME_FORMATS = {
		new SimpleDateFormat("h:mm a"),
		new SimpleDateFormat("H:mm"),
		new SimpleDateFormat("h a"),
		new SimpleDateFormat("H")
	};

	/** Count of columns in table model */
	static protected final int COLUMN_COUNT = 2;

	/** Time column number */
	static protected final int COL_TIME = 0;

	/** Deploy column number */
	static protected final int COL_DEPLOY = 1;

	/** Create the table column model */
	static public TableColumnModel createColumnModel() {
		TableColumnModel m = new DefaultTableColumnModel();
		m.addColumn(createColumn(COL_TIME, 120, "Time"));
		m.addColumn(createColumn(COL_DEPLOY, 80, "Deploy"));
		return m;
	}

	/** Parse a time string and return the minute-of-day */
	static protected Integer parseMinute(String t) {
		Calendar c = Calendar.getInstance();
		Date d = parseTime(t);
		if(d != null) {
			c.setTime(d);
			return c.get(Calendar.HOUR_OF_DAY) * 60 +
				c.get(Calendar.MINUTE);
		} else
			return null;
	}

	/** Parse a time string */
	static protected Date parseTime(String t) {
		for(int i = 0; i < TIME_FORMATS.length; i++) {
			try {
				return TIME_FORMATS[i].parse(t);
			}
			catch(ParseException e) {
				// Ignore
			}
		}
		return null;
	}

	/** Convert minute-of-day to time string */
	static protected String timeString(int minute) {
		StringBuilder min = new StringBuilder();
		min.append(minute % 60);
		while(min.length() < 2)
			min.insert(0, '0');
		return (minute / 60) + ":" + min;
	}

	/** SONAR namespace */
	protected final Namespace namespace;

	/** Logged-in user */
	protected final User user;

	/** Currently selected action plan */
	protected final ActionPlan action_plan;

	/** Create a new time action table model */
	public TimeActionModel(TypeCache<TimeAction> c, ActionPlan ap,
		Namespace ns, User u)
	{
		super(c);
		action_plan = ap;
		namespace = ns;
		user = u;
		initialize();
	}

	/** Add a new proxy to the table model */
	protected int doProxyAdded(TimeAction ta) {
		if(ta.getActionPlan() == action_plan)
			return super.doProxyAdded(ta);
		else
			return -1;
	}

	/** Get the count of columns in the table */
	public int getColumnCount() {
		return COLUMN_COUNT;
	}

	/** Get the count of rows in the table */
	public int getRowCount() {
		synchronized(proxies) {
			return proxies.size() + 1;
		}
	}

	/** Get the value at the specified cell */
	public Object getValueAt(int row, int column) {
		TimeAction ta = getProxy(row);
		if(ta == null)
			return null;
		switch(column) {
		case COL_TIME:
			return timeString(ta.getMinute());
		case COL_DEPLOY:
			return ta.getDeploy();
		default:
			return null;
		}
	}

	/** Get the class of the specified column */
	public Class getColumnClass(int column) {
		if(column == COL_DEPLOY)
			return Boolean.class;
		else
			return String.class;
	}

	/** Check if the specified cell is editable */
	public boolean isCellEditable(int row, int column) {
		TimeAction ta = getProxy(row);
		if(ta != null)
			return column == COL_DEPLOY && canUpdate();
		else
			return column == COL_TIME && canAdd();
	}

	/** Set the value at the specified cell */
	public void setValueAt(Object value, int row, int column) {
		TimeAction ta = getProxy(row);
		if(ta == null) {
			if(row == COL_TIME) {
				Integer m = parseMinute(value.toString());
				if(m != null)
					create(m);
			}
		} else {
			if(row == COL_DEPLOY) {
				if(value instanceof Boolean)
					ta.setDeploy((Boolean)value);
			}
		}
	}

	/** Create a new time action */
	protected void create(int m) {
		String name = createUniqueName();
		if(name != null) {
			HashMap<String, Object> attrs =
				new HashMap<String, Object>();
			attrs.put("action_plan", action_plan);
			attrs.put("minute", m);
			cache.createObject(name, attrs);
		}
	}

	/** Create a unique time action name */
	protected String createUniqueName() {
		for(int uid = 1; uid <= 999; uid++) {
			String n = action_plan.getName() + "_" + uid;
			if(cache.lookupObject(n) == null)
				return n;
		}
		assert false;
		return null;
	}

	/** Check if the user can add */
	public boolean canAdd() {
		return namespace.canAdd(user, new Name(TimeAction.SONAR_TYPE,
			"oname"));
	}

	/** Check if the user can update */
	public boolean canUpdate() {
		return namespace.canUpdate(user, new Name(TimeAction.SONAR_TYPE,
			"oname", "aname"));
	}

	/** Check if the user can remove the action at the specified row */
	public boolean canRemove(int row) {
		TimeAction ta = getProxy(row);
		if(ta != null)
			return canRemove(ta);
		else
			return false;
	}

	/** Check if the user can remove a time action */
	public boolean canRemove(TimeAction ta) {
		return namespace.canRemove(user, new Name(TimeAction.SONAR_TYPE,
			ta.getName()));
	}
}
