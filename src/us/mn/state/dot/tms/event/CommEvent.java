/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.event;

import java.util.HashMap;
import java.util.Map;

/**
 * This is a class for logging communication events to a database.
 *
 * @author Douglas Lau
 */
public class CommEvent extends BaseEvent {

	/** Comm link affected by this event */
	protected final String comm_link;

	/** Drop address (if drop specific) */
	protected final int drop_id;

	/** Device ID (if device specific) */
	protected final String device_id;

	/** Create a new comm event */
	public CommEvent(EventType e, String l, int d, String dev) {
		super(e);
		comm_link = l;
		drop_id = d;
		device_id = dev;
	}

	/** Get the database table name */
	public String getTable() {
		return "events.comm_event";
	}

	/** Get a mapping of the columns */
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("event_type", event_type.id);
		map.put("event_date", event_date);
		map.put("comm_link", comm_link);
		map.put("drop_id", drop_id);
		map.put("device_id", device_id);
		return map;
	}
}
