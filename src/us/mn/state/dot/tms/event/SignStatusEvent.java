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
 * This is a class for logging sign status change events to a database.
 *
 * @author Douglas Lau
 */
public class SignStatusEvent extends BaseEvent {

	/** Device ID (if device specific) */
	protected final String device_id;

	/** Message text */
	protected final String message;

	/** User who deployed message */
	protected final String user_id;

	/** Create a new sign status event */
	public SignStatusEvent(EventType e, String d, String m, String u) {
		super(e);
		device_id = d;
		message = m;
		user_id = u;
	}

	/** Get the database table name */
	public String getTable() {
		return "events.sign_status_event";
	}

	/** Get a mapping of the columns */
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("event_type", event_type.id);
		map.put("event_date", event_date);
		map.put("device_id", device_id);
		map.put("message", message);
		map.put("user_id", user_id);
		return map;
	}
}
