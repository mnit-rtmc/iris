/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2016-2025  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import us.mn.state.dot.tms.ChangeVetoException;
import us.mn.state.dot.tms.EventType;
import us.mn.state.dot.tms.IncDescriptor;
import us.mn.state.dot.tms.IncDescriptorHelper;
import us.mn.state.dot.tms.IncDetail;
import us.mn.state.dot.tms.LaneCode;
import us.mn.state.dot.tms.TMSException;

/**
 * An incident descriptor is part of a message to deploy on a DMS, matching
 * incident attributes.
 *
 * @author Douglas Lau
 */
public class IncDescriptorImpl extends BaseObjectImpl implements IncDescriptor {

	/** Load all the incident descriptors */
	static protected void loadAll() throws TMSException {
		store.query("SELECT name, event_desc_id, detail, lane_code, " +
			"multi FROM iris." + SONAR_TYPE + ";",
			new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new IncDescriptorImpl(row));
			}
		});
	}

	/** Get a mapping of the columns */
	@Override
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("event_desc_id", event_desc_id);
		map.put("detail", detail);
		map.put("lane_code", lane_code);
		map.put("multi", multi);
		return map;
	}

	/** Create an incident descriptor */
	private IncDescriptorImpl(ResultSet row) throws SQLException {
		this(row.getString(1),      // name
		     row.getInt(2),         // event_desc_id
		     row.getString(3),      // detail
		     row.getString(4),      // lane_code
		     row.getString(5)       // multi
		);
	}

	/** Create an incident descriptor */
	private IncDescriptorImpl(String n, int et, String dtl, String lc,
		String m)
	{
		super(n);
		event_desc_id = et;
		detail = lookupIncDetail(dtl);
		lane_code = lc;
		multi = m;
	}

	/** Create a new incident descriptor */
	public IncDescriptorImpl(String n) {
		super(n);
	}

	/** Event type (id of EventType enum) */
	private int event_desc_id;

	/** Set the event type */
	@Override
	public void setEventType(int et) {
		event_desc_id = et;
	}

	/** Set the event type */
	public void doSetEventType(int et) throws TMSException {
		checkEventType(et);
		if (et != event_desc_id) {
			store.update(this, "event_desc_id", et);
			setEventType(et);
		}
	}

	/** Check for valid event types */
	private void checkEventType(int et) throws ChangeVetoException {
		switch (EventType.fromId(et)) {
		case INCIDENT_CRASH:
		case INCIDENT_STALL:
		case INCIDENT_ROADWORK:
		case INCIDENT_HAZARD:
			return;
		default:
			throw new ChangeVetoException("INVALID EVENT TYPE");
		}
	}

	/** Get the event type */
	@Override
	public int getEventType() {
		return event_desc_id;
	}

	/** Incident detail */
	private IncDetail detail;

	/** Set the incident detail */
	@Override
	public void setDetail(IncDetail dtl) {
		detail = dtl;
	}

	/** Set the incident detail */
	public void doSetDetail(IncDetail dtl) throws TMSException {
		if (dtl != detail) {
			store.update(this, "detail", dtl);
			setDetail(dtl);
		}
	}

	/** Get the incident detail */
	@Override
	public IncDetail getDetail() {
		return detail;
	}

	/** Lane code */
	private String lane_code = LaneCode.MAINLINE.lcode;

	/** Set the lane code */
	@Override
	public void setLaneCode(String lc) {
		lane_code = lc;
	}

	/** Set the lane code */
	public void doSetLaneCode(String lc) throws TMSException {
		checkLaneCode(lc);
		if (!objectEquals(lc, lane_code)) {
			store.update(this, "lane_code", lc);
			setLaneCode(lc);
		}
	}

	/** Check for valid lane code */
	private void checkLaneCode(String lc) throws ChangeVetoException {
		switch (LaneCode.fromCode(lc)) {
		case MAINLINE:
		case EXIT:
		case MERGE:
		case CD_LANE:
			return;
		default:
			throw new ChangeVetoException("INVALID LANE CODE");
		}
	}

	/** Get the lane code */
	@Override
	public String getLaneCode() {
		return lane_code;
	}

	/** MULTI string */
	private String multi = "";

	/** Set the MULTI string */
	@Override
	public void setMulti(String m) {
		multi = m;
	}

	/** Set the MULTI string */
	public void doSetMulti(String m) throws TMSException {
		if (!IncDescriptorHelper.isMultiValid(m))
			throw new ChangeVetoException("Invalid MULTI: " + m);
		if (!m.equals(multi)) {
			store.update(this, "multi", m);
			setMulti(m);
		}
	}

	/** Get the MULTI string */
	@Override
	public String getMulti() {
		return multi;
	}
}
