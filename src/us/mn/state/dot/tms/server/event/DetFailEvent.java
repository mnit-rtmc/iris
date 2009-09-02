/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2009  Minnesota Department of Transportation
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
 * This is a class for logging detector fail events to a database.
 *
 * @author Douglas Lau
 */
public class DetFailEvent extends BaseEvent {

	/** Device ID (if device specific) */
	protected final String device_id;

	/** Create a new detector fail event */
	public DetFailEvent(EventType e, String d) {
		super(e);
		assert e == EventType.DET_CHATTER ||
		       e == EventType.DET_LOCKED_ON ||
		       e == EventType.DET_NO_HITS;
		device_id = d;
	}

	/** Get the database table name */
	public String getTable() {
		return "event.detector_event";
	}

	/** Get a mapping of the columns */
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("event_desc_id", event_type.id);
		map.put("event_date", event_date);
		map.put("device_id", device_id);
		return map;
	}
}
