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

import java.util.TreeSet;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumnModel;
import us.mn.state.dot.sonar.Name;
import us.mn.state.dot.sonar.Namespace;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.DayPlan;
import us.mn.state.dot.tms.Holiday;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;

/**
 * Table model for holidays assigned to day plans.
 *
 * @author Douglas Lau
 */
public class DayPlanHolidayModel extends ProxyTableModel<Holiday> {

	/** Count of columns in table model */
	static protected final int COLUMN_COUNT = 2;

	/** Name column number */
	static protected final int COL_NAME = 0;

	/** Assigned column number */
	static protected final int COL_ASSIGNED = 1;

	/** SONAR namespace */
	protected final Namespace namespace;

	/** Logged-in user */
	protected final User user;

	/** Currently selected day plan */
	protected DayPlan day_plan;

	/** Create a new day plan holiday table model */
	public DayPlanHolidayModel(TypeCache<Holiday> c, Namespace ns, User u) {
		super(c);
		namespace = ns;
		user = u;
		initialize();
	}

	/** Set the holidays for a day plan */
	public void setDayPlan(DayPlan dp) {
		day_plan = dp;
		fireTableDataChanged();
	}

	/** Update the holidays for the specified day plan */
	public void updateHolidays(DayPlan dp) {
		if(dp == day_plan)
			fireTableDataChanged();
	}

	/** Get the count of columns in the table */
	public int getColumnCount() {
		return COLUMN_COUNT;
	}

	/** Get the count of rows in the table */
	public int getRowCount() {
		return super.getRowCount() - 1;
	}

	/** Get the value at the specified cell */
	public Object getValueAt(int row, int column) {
		Holiday hol = getProxy(row);
		if(hol == null)
			return "";
		if(column == COL_NAME)
			return hol.getName();
		DayPlan dp = day_plan;	// Avoid NPE
		if(dp != null) {
			for(Holiday h: dp.getHolidays())
				if(h == hol)
					return true;
		}
		return false;
	}

	/** Get the class of the specified column */
	public Class getColumnClass(int column) {
		if(column == COL_ASSIGNED)
			return Boolean.class;
		else
			return String.class;
	}

	/** Check if the specified cell is editable */
	public boolean isCellEditable(int row, int column) {
		return (column == COL_ASSIGNED) && (day_plan != null) &&
		       canUpdate();
	}

	/** Set the value at the specified cell */
	public void setValueAt(Object value, int row, int column) {
		DayPlan dp = day_plan;	// Avoid NPE
		if((column != COL_ASSIGNED) || (dp == null))
			return;
		Holiday hol = getProxy(row);
		if(hol != null && value instanceof Boolean) {
			Holiday[] holidays = dp.getHolidays();
			if((Boolean)value)
				holidays = addHoliday(holidays, hol);
			else
				holidays = removeHoliday(holidays, hol);
			dp.setHolidays(holidays);
		}
	}

	/** Add a holiday to an array */
	protected Holiday[] addHoliday(Holiday[] holidays, Holiday hol) {
		TreeSet<Holiday> h_set = createProxySet();
		for(Holiday h: holidays)
			h_set.add(h);
		h_set.add(hol);
		return h_set.toArray(new Holiday[0]);
	}

	/** Remove a holiday from an array */
	protected Holiday[] removeHoliday(Holiday[] holidays, Holiday hol) {
		TreeSet<Holiday> h_set = createProxySet();
		for(Holiday h: holidays)
			h_set.add(h);
		h_set.remove(hol);
		return h_set.toArray(new Holiday[0]);
	}

	/** Create the table column model */
	public TableColumnModel createColumnModel() {
		TableColumnModel m = new DefaultTableColumnModel();
		m.addColumn(createColumn(COL_NAME, 120, "Holiday"));
		m.addColumn(createColumn(COL_ASSIGNED, 80, "Assigned"));
		return m;
	}

	/** Check if the user can set day plan holidays */
	public boolean canUpdate() {
		return namespace.canUpdate(user, new Name(DayPlan.SONAR_TYPE,
		       "holidays"));
	}
}
