/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2007  Minnesota Department of Transportation
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
import java.sql.SQLException;
import java.util.HashMap;

/**
 * DMSParameters contains a mapping of parameter names to values.
 *
 * @author Douglas Lau
 */
public class DMSParameters implements ResultFactory {

	/** Connection to SQL database */
	protected final SQLConnection store;

	/** Mapping of DMS parameters to values */
	protected final HashMap<String, Integer> params =
		new HashMap<String, Integer>();

	/** Create a new DMS parameters object */
	public DMSParameters(SQLConnection s) throws TMSException {
		store = s;
		store.query("SELECT * FROM dms_parameters;", this);
	}

	/** Create a DMS parameter from the current row of a result set */
	public synchronized void create(ResultSet row) throws SQLException {
		String name = row.getString(1);
		int value = row.getInt(2);
		params.put(name, value);
	}

	/** Insert a new DMS parameter */
	protected void insertParameter(String name, int value)
		throws TMSException
	{
		store.update("INSERT INTO dms_parameters " +
			"(name, value) VALUES " + "('" + name +
			"', '" + value + "');");
	}

	/** Update a DMS parameter */
	protected void updateParameter(String name, int value)
		throws TMSException
	{
		store.update("UPDATE dms_parameters SET value = '" +
			value + "' WHERE name = '" + name + "';");
	}

	/** Set a new value of a DMS parameter */
	protected synchronized void setParameter(String name, int value)
		throws TMSException
	{
		Integer val = params.get(name);
		if(val == null)
			insertParameter(name, value);
		else if(value != val)
			updateParameter(name, value);
		params.put(name, value);
	}

	/** Get the current value of a DMS parameter */
	protected synchronized int getParameter(String name) {
		Integer val = params.get(name);
		if(val == null)
			return 0;
		else
			return val;
	}

	/** Set one of the ring radius values */
	public void setRingRadius(int ring, int radius) throws TMSException {
		setParameter("ring_radius_" + ring, radius);
	}

	/** Get one of the ring radius values */
	public int getRingRadius(int ring) {
		return getParameter("ring_radius_" + ring);
	}

	/** Set the global sign page on time (tenths of a second) */
	public void setPageOnTime(int time) throws TMSException {
		setParameter("page_on_time", time);
	}

	/** Get the global sign page on time (tenths of a second) */
	public int getPageOnTime() {
		return getParameter("page_on_time");
	}

	/** Set the global sign page off time (tenths of a second) */
	public void setPageOffTime(int time) throws TMSException {
		setParameter("page_off_time", time);
	}

	/** Get the global sign page off time (tenths of a second) */
	public int getPageOffTime() {
		return getParameter("page_off_time");
	}

	/** Set the global ramp meter green time (tenths of a second) */
	public void setMeterGreenTime(int time) throws TMSException {
		setParameter("meter_green_time", time);
	}

	/** Get the global ramp meter green time (tenths of a second) */
	public int getMeterGreenTime() {
		return getParameter("meter_green_time");
	}

	/** Set the global ramp meter yellow time (tenths of a second) */
	public void setMeterYellowTime(int time) throws TMSException {
		setParameter("meter_yellow_time", time);
	}

	/** Get the global ramp meter yellow time (tenths of a second) */
	public int getMeterYellowTime() {
		return getParameter("meter_yellow_time");
	}

	/** Set the global ramp meter minimum red time (tenths of a second) */
	public void setMeterMinRedTime(int time) throws TMSException {
		setParameter("meter_min_red_time", time);
	}

	/** Get the global ramp meter minimum red time (tenths of a second) */
	public int getMeterMinRedTime() {
		return getParameter("meter_min_red_time");
	}
}
