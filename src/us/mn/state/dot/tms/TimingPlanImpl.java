/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2009  Minnesota Department of Transportation
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
package us.mn.state.dot.tms;

import java.sql.ResultSet;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import us.mn.state.dot.sonar.Namespace;

/**
 * Timing plan for operating a traffic management device
 *
 * @author Douglas Lau
 */
public class TimingPlanImpl extends BaseObjectImpl implements TimingPlan {

	/** Number of minutes in a day */
	static protected final int MINUTES_PER_DAY = 24 * 60;

	/** Calendar instance for calculating the minute of day */
	static protected final Calendar STAMP = Calendar.getInstance();

	/** Get the current minute of the day */
	static protected int minute_of_day() {
		synchronized(STAMP) {
			STAMP.setTimeInMillis(System.currentTimeMillis());
			return STAMP.get(Calendar.HOUR_OF_DAY) * 60 +
				STAMP.get(Calendar.MINUTE);
		}
	}

	/** Get the current second of the day */
	static protected int second_of_day() {
		synchronized(STAMP) {
			STAMP.setTimeInMillis(System.currentTimeMillis());
			return STAMP.get(Calendar.HOUR_OF_DAY) * Interval.HOUR +
			       STAMP.get(Calendar.MINUTE) * Interval.MINUTE +
			       STAMP.get(Calendar.SECOND);
		}
	}

	/** Create a 4 character time stamp.
	 * @param min Minute of the day (0-1440)
	 * @return 4 character time stamp (1330 for 1:30 PM) */
	static protected String stamp_hhmm(int min) {
		StringBuilder b = new StringBuilder();
		b.append(min / 60);
		while(b.length() < 2)
			b.insert(0, '0');
		b.append(min % 60);
		while(b.length() < 4)
			b.insert(2, '0');
		return b.toString();
	}

	/** Get a stamp of the current 30 second interval */
	static public String stamp_30() {
		int i30 = second_of_day() / 30 + 1;
		StringBuilder b = new StringBuilder();
		b.append(i30 / 120);
		while(b.length() < 2)
			b.insert(0, '0');
		b.append(':');
		b.append((i30 % 120) / 2);
		while(b.length() < 5)
			b.insert(3, '0');
		b.append(':');
		b.append((i30 % 2) * 30);
		while(b.length() < 8)
			b.insert(6, '0');
		return b.toString();
	}

	/** Load all the timing plans */
	static protected void loadAll() throws TMSException {
		System.err.println("Loading timing plans...");
		namespace.registerType(SONAR_TYPE, TimingPlanImpl.class);
		store.query("SELECT name, plan_type, device, start_min, " +
			"stop_min, active, testing, target FROM iris." +
			SONAR_TYPE  + ";", new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new TimingPlanImpl(
					namespace,
					row.getString(1),	// name
					row.getInt(2),		// plan_type
					row.getString(3),	// device
					row.getInt(4),		// start_min
					row.getInt(5),		// stop_min
					row.getBoolean(6),	// active
					row.getBoolean(7),	// testing
					row.getInt(8)		// target
				));
			}
		});
	}

	/** Lookup a device (DMS or ramp meter) */
	static protected Device2 lookupDevice(Namespace ns, String d) {
		Device2 dv = (Device2)ns.lookupObject(DMS.SONAR_TYPE, d);
		if(dv != null)
			return dv;
		else
			return (Device2)ns.lookupObject(RampMeter.SONAR_TYPE,d);
	}

	/** Get a mapping of the columns */
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("plan_type", plan_type);
		map.put("device", device);
		map.put("start_min", start_min);
		map.put("stop_min", stop_min);
		map.put("active", active);
		map.put("testing", testing);
		map.put("target", target);
		return map;
	}

	/** Get the database table name */
	public String getTable() {
		return "iris." + SONAR_TYPE;
	}

	/** Get the SONAR type name */
	public String getTypeName() {
		return SONAR_TYPE;
	}

	/** Create a new timing plan */
	public TimingPlanImpl(String n) {
		super(n);
	}

	/** Create a new timing plan */
	protected TimingPlanImpl(Namespace ns, String n, int p, String d,
		int st, int sp, boolean a, boolean tst, int t)
	{
		this(n, p, lookupDevice(ns, d), st, sp, a, tst, t);
	}

	/** Create a new timing plan */
	protected TimingPlanImpl(String n, int p, Device2 d, int st, int sp,
		boolean a, boolean tst, int t)
	{
		this(n);
		plan_type = p;
		device = d;
		start_min = st;
		stop_min = sp;
		active = a;
		testing = tst;
		target = t;
	}

	/** Timing plan type */
	protected int plan_type;

	/** Get the plan type */
	public int getPlanType() {
		return plan_type;
	}

	/** Device */
	protected Device2 device;

	/** Get the device */
	public Device2 getDevice() {
		return device;
	}

	/** Timing plan start time (minute of day) */
	protected int start_min;

	/** Set the start time (minute of day) */
	public void setStartMin(int t) {
		start_min = t;
	}

	/** Set the start time (minute of day) */
	public void doSetStartMin(int t) throws TMSException {
		if(t == start_min)
			return;
		if(t < 0 || t > stop_min)
			throw new ChangeVetoException("Invalid start time");
		store.update(this, "start_min", t);
		setStartMin(t);
	}

	/** Get the start time (minute of day) */
	public int getStartMin() {
		return start_min;
	}

	/** Timing plan stop time (minute of day) */
	protected int stop_min;

	/** Set the stop time (minute of day) */
	public void setStopMin(int t) {
		stop_min = t;
	}

	/** Set the stop time (minute of day) */
	public void doSetStopMin(int t) throws TMSException {
		if(t == stop_min)
			return;
		if(t < start_min || t >= MINUTES_PER_DAY)
			throw new ChangeVetoException("Invalid stop time");
		store.update(this, "stop_min", t);
		setStopMin(t);
	}

	/** Get the stop time (minute of day) */
	public int getStopMin() {
		return stop_min;
	}

	/** Get the timing plan start stamp */
	public String getStamp() {
		return stamp_hhmm(getStartMin());
	}

	/** Active status */
	protected boolean active;

	/** Set the active status */
	public void setActive(boolean a) {
		active = a;
	}

	/** Set the active status */
	public void doSetActive(boolean a) throws TMSException {
		if(a == active)
			return;
		store.update(this, "active", a);
		setActive(a);
	}

	/** Get the active status */
	public boolean getActive() {
		return active;
	}

	/** Testing status */
	protected boolean testing;

	/** Set the testing status */
	public void setTesting(boolean t) {
		testing = t;
	}

	/** Set the testing status */
	public void doSetTesting(boolean t) throws TMSException {
		if(t == testing)
			return;
		store.update(this, "testing", t);
		setTesting(t);
	}

	/** Get the testing status */
	public boolean getTesting() {
		return testing;
	}

	/** Target value (release rate) */
	protected int target;

	/** Set the target value */
	public void setTarget(int t) {
		target = t;
	}

	/** Set the target value */
	public void doSetTarget(int t) throws TMSException {
		if(t == target)
			return;
		store.update(this, "target", t);
		setTarget(t);
	}

	/** Get the target value */
	public int getTarget() {
		return target;
	}

	/** Flag to determine if timing plan is operating */
	protected transient boolean operating;

	/** Check if the timing plan is operating */
	public boolean isOperating() {
		return operating;
	}

	/** Validate the timing plan */
	public void validate() {
		if(shouldOperate()) {
			if(!isOperating())
				start();
		} else if(isOperating())
			stop();
		TimingPlanState s = getState();
		if(s != null)
			s.validate(this);
	}

	/** Current timing plan state data */
	protected transient TimingPlanState state;

	/** Check if the timing plan should be operating */
	protected boolean shouldOperate() {
		return getActive() && isWithin();
	}

	/** Check if the current time is within the timing plan window */
	public boolean isWithin() {
		int m = minute_of_day();
		return m >= getStartMin() && m < getStopMin();
	}

	/** Start operating the timing plan */
	protected void start() {
		operating = true;
		state = null;
	}

	/** Stop operating the timing plan */
	protected void stop() {
		operating = false;
		state = null;
	}

	/** Get the current timing plan state */
	protected TimingPlanState getState() {
		if(state == null)
			state = createState();
		return state;
	}

	/** Create the timing plan state */
	protected TimingPlanState createState() {
		switch(TimingPlanType.fromOrdinal(plan_type)) {
		case TRAVEL:
			return new TimingPlanState();
		case SIMPLE:
			return new SimplePlanState();
		case STRATIFIED:
			return lookupOrCreateStratified();
		default:
			return null;
		}
	}

	/** Lookup or create a stratified plan state */
	protected TimingPlanState lookupOrCreateStratified() {
		Device2 d = device;
		if(d instanceof RampMeterImpl) {
			RampMeterImpl meter = (RampMeterImpl)d;
			Corridor c = meter.getCorridor();
			if(c != null)
				return StratifiedPlanState.lookupCorridor(c);
		}
		return null;
	}
}
