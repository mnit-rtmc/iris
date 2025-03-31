/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2014-2024  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.event;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import us.mn.state.dot.tms.EventType;
import us.mn.state.dot.tms.TMSException;

/**
 * This is a class for logging metering events to a database.
 *
 * @author Douglas Lau
 */
public class MeterEvent extends BaseEvent {

	/** Ramp meter ID */
	private final String ramp_meter;

	/** Metering phase */
	private final int phase;

	/** Queue state */
	private final int q_state;

	/** Queue length */
	private final float q_len;

	/** Demand adjustment */
	private final float dem_adj;

	/** Estimated wait time */
	private final int wait_secs;

	/** Limit control */
	private final int limit_ctrl;

	/** Minimum rate */
	private final int min_rate;

	/** Release rate */
	private final int rel_rate;

	/** Maximum rate */
	private final int max_rate;

	/** Downstream node ID */
	private final String d_node;

	/** Segment density */
	private final float seg_density;

	/** Create a new meter event */
	public MeterEvent(EventType e, String mid, int p, int qs, float ql,
		float da, int ws, int lc, int mn, int rr, int mx, String dn,
		float sd)
	{
		super(e);
		assert e == EventType.METER_EVENT;
		ramp_meter = mid;
		phase = p;
		q_state = qs;
		q_len = ql;
		dem_adj = da;
		wait_secs = ws;
		limit_ctrl = lc;
		min_rate = mn;
		rel_rate = rr;
		max_rate = mx;
		d_node = dn;
		seg_density = sd;
	}

	/** Get the event config name */
	@Override
	protected String eventConfigName() {
		return "meter_event";
	}

	/** Get the database table name */
	@Override
	public String getTable() {
		return "event.meter_event";
	}

	/** Get a mapping of the columns */
	@Override
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("event_date", new Timestamp(event_date.getTime()));
		map.put("event_desc", event_type.id);
		map.put("ramp_meter", ramp_meter);
		map.put("phase", phase);
		map.put("q_state", q_state);
		map.put("q_len", q_len);
		map.put("dem_adj", dem_adj);
		map.put("wait_secs", wait_secs);
		map.put("limit_ctrl", limit_ctrl);
		map.put("min_rate", min_rate);
		map.put("rel_rate", rel_rate);
		map.put("max_rate", max_rate);
		if (d_node != null)
			map.put("d_node", d_node);
		map.put("seg_density", seg_density);
		return map;
	}
}
