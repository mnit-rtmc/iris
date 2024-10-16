/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2024  Minnesota Department of Transportation
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
 * This is a class for logging communication events to a database.
 *
 * @author Douglas Lau
 */
public class CommEvent extends BaseEvent {

	/** Is the specified event a comm event? */
	static private boolean isCommEvent(EventType et) {
		return EventType.QUEUE_DRAINED == et
		    || EventType.POLL_TIMEOUT_ERROR == et
		    || EventType.PARSING_ERROR == et
		    || EventType.CHECKSUM_ERROR == et
		    || EventType.CONTROLLER_ERROR == et
		    || EventType.COMM_ERROR == et
		    || EventType.CONNECTION_REFUSED == et
		    || EventType.COMM_FAILED == et
		    || EventType.COMM_RESTORED == et;
	}

	/** Controller affected by this event */
	private final String controller;

	/** Device ID (if device specific) */
	private final String device_id;

	/** Create a new comm event */
	public CommEvent(EventType et, String c, String dev) {
		super(et);
		assert isCommEvent(et);
		controller = c;
		device_id = dev;
	}

	/** Get the event config name */
	@Override
	protected String eventConfigName() {
		return "comm_event";
	}

	/** Get the database table name */
	@Override
	public String getTable() {
		return "event.comm_event";
	}

	/** Get a mapping of the columns */
	@Override
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("event_desc_id", event_type.id);
		map.put("event_date", new Timestamp(event_date.getTime()));
		map.put("controller", controller);
		map.put("device_id", device_id);
		return map;
	}
}
