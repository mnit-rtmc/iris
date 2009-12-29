/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2004-2009  Minnesota Department of Transportation
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
import java.util.HashSet;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.table.TableCellEditor;
import us.mn.state.dot.sonar.Checker;
import us.mn.state.dot.sonar.Name;
import us.mn.state.dot.tms.Device;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.RampMeter;
import us.mn.state.dot.tms.TimingPlan;
import us.mn.state.dot.tms.TimingPlanType;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;

/**
 * Table model for timing plans 
 *
 * @author Douglas Lau
 */
public class TimingPlanModel extends ProxyTableModel<TimingPlan> {

	/** Time parser formats */
	static protected final DateFormat[] TIME_FORMATS = {
		new SimpleDateFormat("h:mm a"),
		new SimpleDateFormat("H:mm"),
		new SimpleDateFormat("h a"),
		new SimpleDateFormat("H")
	};

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

	/** Parse a time string and return the minute-of-day */
	static protected int parseMinute(String t) {
		Calendar c = Calendar.getInstance();
		c.setTime(parseTime(t));
		return c.get(Calendar.HOUR_OF_DAY) * 60 +
			c.get(Calendar.MINUTE);
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
		new ProxyColumn<TimingPlan>("Name") {
			public Object getValueAt(TimingPlan tp) {
				return tp.getName();
			}
		},
		new ProxyColumn<TimingPlan>("Plan Type", 140) {
			public Object getValueAt(TimingPlan tp) {
				return TimingPlanType.fromOrdinal(
					tp.getPlanType());
			}
			public boolean isEditable(TimingPlan tp) {
				return tp == null && canAdd();
			}
			public void setValueAt(TimingPlan tp, Object value) {
				if(value instanceof String)
					createPlan((String)value);
			}
			protected TableCellEditor createCellEditor() {
				JComboBox combo = new JComboBox(
					TimingPlanType.getDescriptions());
				return new DefaultCellEditor(combo);
			}
		},
		new ProxyColumn<TimingPlan>("Device") {
			public Object getValueAt(TimingPlan tp) {
				Device d = tp.getDevice();
				if(d != null)
					return d.getName();
				else
					return null;
			}
		},
		new ProxyColumn<TimingPlan>("Start Time") {
			public Object getValueAt(TimingPlan tp) {
				return timeString(tp.getStartMin());
			}
			public boolean isEditable(TimingPlan tp) {
				return canUpdate(tp);
			}
			public void setValueAt(TimingPlan tp, Object value) {
				tp.setStartMin(parseMinute(value.toString()));
			}
		},
		new ProxyColumn<TimingPlan>("Stop Time") {
			public Object getValueAt(TimingPlan tp) {
				return timeString(tp.getStopMin());
			}
			public boolean isEditable(TimingPlan tp) {
				return canUpdate(tp);
			}
			public void setValueAt(TimingPlan tp, Object value) {
				tp.setStopMin(parseMinute(value.toString()));
			}
		},
		new ProxyColumn<TimingPlan>("Active", 0, Boolean.class) {
			public Object getValueAt(TimingPlan tp) {
				return tp.getActive();
			}
			public boolean isEditable(TimingPlan tp) {
				return canUpdate(tp);
			}
			public void setValueAt(TimingPlan tp, Object value) {
				if(value instanceof Boolean)
					tp.setActive((Boolean)value);
			}
		},
		new ProxyColumn<TimingPlan>("Testing", 0, Boolean.class) {
			public Object getValueAt(TimingPlan tp) {
				return tp.getTesting();
			}
			public boolean isEditable(TimingPlan tp) {
				return canUpdate(tp);
			}
			public void setValueAt(TimingPlan tp, Object value) {
				if(value instanceof Boolean)
					tp.setTesting((Boolean)value);
			}
		},
		new ProxyColumn<TimingPlan>("Target", 0, Integer.class) {
			public Object getValueAt(TimingPlan tp) {
				return tp.getTarget();
			}
			public boolean isEditable(TimingPlan tp) {
				return canUpdate(tp);
			}
			public void setValueAt(TimingPlan tp, Object value) {
				if(value instanceof Integer)
					tp.setTarget((Integer)value);
			}
		}
	    };
	}

	/** Device in question */
	protected final Device device;

	/** Create a new timing plan table model */
	public TimingPlanModel(Session s, Device d) {
		super(s, s.getSonarState().getTimingPlans());
		device = d;
	}

	/** Add a new proxy to the table model */
	protected int doProxyAdded(TimingPlan proxy) {
		if(device == null || device == proxy.getDevice())
			return super.doProxyAdded(proxy);
		else
			return -1;
	}

	/** Create a new timing plan */
	protected void createPlan(String pdesc) {
		if(pdesc.equals(TimingPlanType.SIMPLE.description)) {
			if(device instanceof RampMeter)
				create(TimingPlanType.SIMPLE);
		} else if(pdesc.equals(TimingPlanType.STRATIFIED.description)) {
			if(device instanceof RampMeter)
				create(TimingPlanType.STRATIFIED);
		}
	}

	/** 
	 * Create a new timing plan.
	 * @param ptype Timing plan type.
	 */
	protected void create(TimingPlanType ptype) {
		String name = createUniqueName();
		if(name != null) {
			HashMap<String, Object> attrs =
				new HashMap<String, Object>();
			attrs.put("plan_type", ptype.ordinal());
			attrs.put("device", device);
			cache.createObject(name, attrs);
		}
	}

	/** 
	 * Create a TimingPlan name, which is in this form: 
	 *    device.name + "_" + uniqueid
	 *    where uniqueid is a sequential integer.
	 * @return A unique string for a new TimingPlan name
	 */
	protected String createUniqueName() {
		HashSet<String> names = createTimingPlanNameSet();
		int uid_max = names.size() + 2;
		for(int uid = 1; uid <= uid_max; uid++) {
			String n = device.getName() + "_" + uid;
			if(!names.contains(n))
				return n;
		}
		assert false;
		return null;
	}

	/** Create a HashSet containing all TimingPlan names for the device.
	 * @return A HashSet with entries as TimingPlan names */
	protected HashSet<String> createTimingPlanNameSet() {
		final HashSet<String> names = new HashSet<String>();
		cache.findObject(new Checker<TimingPlan>() {
			public boolean check(TimingPlan tp) {
				if(tp.getDevice() == device)
					names.add(tp.getName());
				return false;
			}
		});
		return names;
	}

	/** Check if the user can add a proxy */
	public boolean canAdd() {
		return namespace.canAdd(user, new Name(TimingPlan.SONAR_TYPE,
			"oname"));
	}
}
