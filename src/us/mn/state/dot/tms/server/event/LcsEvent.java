/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2025  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.utils.SString;

/**
 * This is a class for logging LCS events to a database.
 *
 * @author Douglas Lau
 */
public class LcsEvent extends BaseEvent {

	/** Is the specified event an LCS event? */
	static private boolean isLcsEvent(EventType et) {
		return EventType.LCS_LOCKED == et
		    || EventType.LCS_UNLOCKED == et
		    || EventType.LCS_DEPLOYED == et
		    || EventType.LCS_CLEARED == et;
	}

	/** LCS name */
	private final String lcs;

	/** Lock (JSON) */
	private final String lock;

	/** Status (JSON) */
	private final String status;

	/** Create a new LCS event */
	public LcsEvent(EventType et, String l, String lk, String st) {
		super(et);
		assert isLcsEvent(et);
		lcs = l;
		lock = lk;
		status = st;
	}

	/** Get the event config name */
	@Override
	protected String eventConfigName() {
		return "lcs_event";
	}

	/** Get the database table name */
	@Override
	public String getTable() {
		return "event.lcs_event";
	}

	/** Get a mapping of the columns */
	@Override
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("event_date", new Timestamp(event_date.getTime()));
		map.put("event_desc", event_type.id);
		map.put("lcs", lcs);
		map.put("lock", lock);
		map.put("status", status);
		return map;
	}
}
