/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2014-2026  Minnesota Department of Transportation
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
public class MaxPressureMeterEvent extends BaseEvent {

	/** Ramp meter ID */
	private final String ramp_meter;

	/** Metering phase */
	private final int phase;

	/** Queue state */
	private final int q_state;

	/** Queue length */
	private final float q_len;

	/** Queue length from cumulative counts */
	private final float q_len_from_cc;

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
	private final float upstream_density_detected;
	private final float upstream_density_estimated;
	private final float downstream_density_detected;
	private final float downstream_density_estimated;

	/** Weights used in max-pressure calculation **/
	private final float upstream_weight;
	private final float downstream_weight;
	private final float ramp_weight;

	/** Pressure calculations */
	private final float pressure_ud;
	private final float pressure_rd;

	/** Used to compute rate in max-pressure calculation */
	private final float receiving_flow;
	private final float sending_flow_upstream;
	private final float sending_flow_ramp;

	/** Create a new meter event */
	public MaxPressureMeterEvent(String mid, int p, int qs, float ql,
		float q_len_from_cc, float da, int ws, int lc, int mn, int rr,
		int mx, String dn, float upstream_density_detected,
		float upstream_density_estimated,
		float downstream_density_detected,
		float downstream_density_estimated,
		float upstream_weight, float downstream_weight,
		float ramp_weight, float pressure_ud, float pressure_rd,
		float receiving_flow, float sending_flow_upstream,
		float sending_flow_ramp)
	{
		super(EventType.MAX_PRESSURE_EVENT);
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
		this.q_len_from_cc = q_len_from_cc;
		this.upstream_density_detected = upstream_density_detected;
		this.upstream_density_estimated = upstream_density_estimated;
		this.downstream_density_detected = downstream_density_detected;
		this.downstream_density_estimated = upstream_density_estimated;
		this.upstream_weight = upstream_weight;
		this.downstream_weight = downstream_weight;
		this.ramp_weight = ramp_weight;
		this.pressure_ud = pressure_ud;
		this.pressure_rd = pressure_rd;
		this.receiving_flow = receiving_flow;
		this.sending_flow_upstream = sending_flow_upstream;
		this.sending_flow_ramp = sending_flow_ramp;
	}

	/** Get the event config name */
	@Override
	protected String eventConfigName() {
		return "max_pressure_event";
	}

	/** Get the database table name */
	@Override
	public String getTable() {
		return "event.max_pressure_event";
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
		map.put("q_len_from_cc", q_len_from_cc);
		map.put("dem_adj", dem_adj);
		map.put("wait_secs", wait_secs);
		map.put("limit_ctrl", limit_ctrl);
		map.put("min_rate", min_rate);
		map.put("rel_rate", rel_rate);
		map.put("max_rate", max_rate);
		if (d_node != null)
			map.put("d_node", d_node);
		map.put("us_density_detected", upstream_density_detected);
		map.put("us_density_detected", upstream_density_estimated);
		map.put("ds_density_detected", downstream_density_detected);
		map.put("ds_density_detected", downstream_density_estimated);
		map.put("us_weight", upstream_weight);
		map.put("ds_weight", downstream_weight);
		map.put("rmp_weight", ramp_weight);
		map.put("pressure_ud", pressure_ud);
		map.put("pressure_rd", pressure_rd);
		map.put("receiving_flow", receiving_flow);
		map.put("sending_flow_us", sending_flow_upstream);
		map.put("sending_flow_rmp", sending_flow_ramp);
		return map;
	}
}
