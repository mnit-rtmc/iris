/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2011  Minnesota Department of Transportation
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
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.table.TableCellEditor;
import us.mn.state.dot.tms.ActionPlan;
import us.mn.state.dot.tms.DayPlan;
import us.mn.state.dot.tms.PlanPhase;
import us.mn.state.dot.tms.TimeAction;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyListModel;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;
import us.mn.state.dot.tms.client.toast.WrapperComboBoxModel;

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

	/** Create the columns in the model */
	protected ProxyColumn[] createColumns() {
	    // NOTE: half-indent to declare array
	    return new ProxyColumn[] {
		new ProxyColumn<TimeAction>("Day Plan", 100) {
			public Object getValueAt(TimeAction ta) {
				if(ta == null)
					return day_plan;
				else
					return ta.getDayPlan();
			}
			public boolean isEditable(TimeAction ta) {
				return ta == null && canAdd();
			}
			public void setValueAt(TimeAction ta, Object value) {
				if(value instanceof DayPlan)
					day_plan = (DayPlan)value;
			}
			protected TableCellEditor createCellEditor() {
				JComboBox combo = new JComboBox();
				combo.setModel(new WrapperComboBoxModel(
					day_model));
				return new DefaultCellEditor(combo);
			}
		},
		new ProxyColumn<TimeAction>("Time", 80) {
			public Object getValueAt(TimeAction ta) {
				if(ta != null)
					return timeString(ta.getMinute());
				else
					return null;
			}
			public boolean isEditable(TimeAction ta) {
				return (day_plan != null) &&
				       (ta == null) && canAdd();
			}
			public void setValueAt(TimeAction ta, Object value) {
				Integer m = parseMinute(value.toString());
				if(m != null)
					create(m);
				day_plan = null;
			}
		},
		new ProxyColumn<TimeAction>("Phase", 100) {
			public Object getValueAt(TimeAction ta) {
				if(ta != null)
					return ta.getPhase();
				else
					return null;
			}
			public boolean isEditable(TimeAction ta) {
				return canUpdate(ta);
			}
			public void setValueAt(TimeAction ta, Object value) {
				if(value instanceof PlanPhase)
					ta.setPhase((PlanPhase)value);
			}
			protected TableCellEditor createCellEditor() {
				JComboBox combo = new JComboBox();
				combo.setModel(new WrapperComboBoxModel(
					phase_model));
				return new DefaultCellEditor(combo);
			}
		}
	    };
	}

	/** Get the value at the specified cell.  Note: this overrides the
	 * method from ProxyTableModel to allow null proxies to be passed to
	 * ProxyColumn.getValueAt. */
	public Object getValueAt(int row, int col) {
		TimeAction ta = getProxy(row);
		ProxyColumn pc = getProxyColumn(col);
		if(pc != null)
			return pc.getValueAt(ta);
		else
			return null;
	}

	/** Currently selected action plan */
	protected final ActionPlan action_plan;

	/** Day model */
	private final ProxyListModel<DayPlan> day_model;

	/** Plan phase model */
	private final ProxyListModel<PlanPhase> phase_model;

	/** Day plan for new time action */
	protected DayPlan day_plan;

	/** Create a new time action table model */
	public TimeActionModel(Session s, ActionPlan ap) {
		super(s, s.getSonarState().getTimeActions());
		action_plan = ap;
		day_model = s.getSonarState().getDayModel();
		phase_model = s.getSonarState().getPhaseModel();
	}

	/** Add a new proxy to the table model */
	protected int doProxyAdded(TimeAction ta) {
		if(ta.getActionPlan() == action_plan)
			return super.doProxyAdded(ta);
		else
			return -1;
	}

	/** Create a new time action */
	protected void create(int m) {
		String name = createUniqueName();
		if(name != null) {
			HashMap<String, Object> attrs =
				new HashMap<String, Object>();
			attrs.put("day_plan", day_plan);
			attrs.put("action_plan", action_plan);
			attrs.put("minute", m);
			attrs.put("phase", action_plan.getDefaultPhase());
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
		return null;
	}

	/** Get the SONAR type name */
	protected String getSonarType() {
		return TimeAction.SONAR_TYPE;
	}
}
