/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2010  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.DayPlan;
import us.mn.state.dot.tms.Holiday;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;

/**
 * Table model for holidays assigned to day plans.
 *
 * @author Douglas Lau
 */
public class DayPlanHolidayModel extends ProxyTableModel<Holiday> {

	/** Create the columns in the model */
	protected ProxyColumn[] createColumns() {
	    // NOTE: half-indent to declare array
	    return new ProxyColumn[] {
		new ProxyColumn<Holiday>("Holiday", 120) {
			public Object getValueAt(Holiday h) {
				return h.getName();
			}
		},
		new ProxyColumn<Holiday>("Assigned", 80, Boolean.class) {
			public Object getValueAt(Holiday h) {
				return isAssigned(h);
			}
			public boolean isEditable(Holiday h) {
				return canUpdateDayPlanHolidays();
			}
			public void setValueAt(Holiday h, Object value) {
				if(value instanceof Boolean)
					setAssigned(h, (Boolean)value);
			}
		},
	    };
	}

	/** Check if the given holiday is assigned */
	protected boolean isAssigned(Holiday hol) {
		DayPlan dp = day_plan;	// Avoid NPE
		if(dp != null) {
			for(Holiday h: dp.getHolidays())
				if(h == hol)
					return true;
		}
		return false;
	}

	/** Assign or unassign the specified holiday */
	protected void setAssigned(Holiday h, boolean a) {
		DayPlan dp = day_plan;	// Avoid NPE
		if(dp != null) {
			Holiday[] holidays = dp.getHolidays();
			if(a)
				holidays = addHoliday(holidays, h);
			else
				holidays = removeHoliday(holidays, h);
			dp.setHolidays(holidays);
		}
	}

	/** Currently selected day plan */
	protected DayPlan day_plan;

	/** Create a new day plan holiday table model */
	public DayPlanHolidayModel(Session s) {
		super(s, s.getSonarState().getHolidays());
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

	/** Get the count of rows in the table */
	public int getRowCount() {
		return super.getRowCount() - 1;
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

	/** Check if the user can update day plan holidays */
	protected boolean canUpdateDayPlanHolidays() {
		return session.canUpdate(day_plan, "holidays");
	}
}
