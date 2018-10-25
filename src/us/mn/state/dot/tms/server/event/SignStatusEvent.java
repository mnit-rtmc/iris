/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2018  Minnesota Department of Transportation
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

import java.util.HashMap;
import java.util.Map;
import us.mn.state.dot.tms.EventType;

/**
 * This is a class for logging sign status change events to a database.
 *
 * @author Douglas Lau
 */
public class SignStatusEvent extends BaseEvent {

	/** Device ID (if device specific) */
	private final String device_id;

	/** Message text */
	private final String message;

	/** Message owner */
	private final String owner;

	/** Create a new sign status event */
	public SignStatusEvent(EventType e, String d, String m, String o) {
		super(e);
		assert e == EventType.DMS_DEPLOYED ||
		       e == EventType.DMS_CLEARED ||
		       e == EventType.LCS_DEPLOYED ||
		       e == EventType.LCS_CLEARED;
		device_id = d;
		message = m;
		owner = o;
	}

	/** Get the database table name */
	@Override
	public String getTable() {
		return "event.sign_event";
	}

	/** Get a mapping of the columns */
	@Override
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("event_desc_id", event_type.id);
		map.put("event_date", event_date);
		map.put("device_id", device_id);
		map.put("message", message);
		map.put("owner", owner);
		return map;
	}
}
