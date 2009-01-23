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

	/** Load all the timing plans */
	static protected void loadAll() throws TMSException {
		System.err.println("Loading timing plans...");
		namespace.registerType(SONAR_TYPE, TimingPlanImpl.class);
		store.query("SELECT name, plan_type, device, start_min, " +
			"stop_min, active, testing, target FROM " +
			SONAR_TYPE  + ";", new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.add(new TimingPlanImpl(namespace,
					row.getString(1),	// name
					row.getInt(2),		// plan_type
					row.getString(3),	// device_io
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
		map.put("plan_type", plan_type.ordinal());
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
		return SONAR_TYPE;
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
		this(n, TimingPlanType.fromOrdinal(p), lookupDevice(ns, d),
		     st, sp, a, tst, t);
	}

	/** Create a new timing plan */
	protected TimingPlanImpl(String n, TimingPlanType p, Device2 d, int st,
		int sp, boolean a, boolean tst, int t)
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
	protected TimingPlanType plan_type;

	/** Get the plan type */
	public int getPlanType() {
		return plan_type.ordinal();
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

	/** Current timing plan state data */
	protected transient TimingPlanState state;

	/** Get the current timing plan state */
	public TimingPlanState getState() {
		if(state == null)
			state = createState();
		return state;
	}

	/** Create the timing plan state */
	protected TimingPlanState createState() {
		switch(plan_type) {
		case TRAVEL:
			return new TimingPlanState(this);
		case SIMPLE:
			return new SimplePlanState(this);
		case STRATIFIED:
			return new StratifiedPlanState(this);
		default:
			return null;
		}
	}
}
