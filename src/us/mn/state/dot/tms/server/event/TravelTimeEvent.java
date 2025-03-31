/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2016-2024  Minnesota Department of Transportation
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
 * This is a class for logging travel time events to a database.
 *
 * @author Douglas Lau
 */
public class TravelTimeEvent extends BaseEvent {

	/** Device ID */
	private final String device_id;

	/** Station ID */
	private final String station_id;

	/** Create a new travel time event */
	public TravelTimeEvent(EventType e, String d, String sid) {
		super(e);
		assert EventType.TT_LINK_TOO_LONG       == e ||
		       EventType.TT_NO_DESTINATION_DATA == e ||
		       EventType.TT_NO_ORIGIN_DATA      == e ||
		       EventType.TT_NO_ROUTE            == e;
		device_id = d;
		station_id = sid;
	}

	/** Get the event config name */
	@Override
	protected String eventConfigName() {
		return "travel_time_event";
	}

	/** Get the database table name */
	@Override
	public String getTable() {
		return "event.travel_time_event";
	}

	/** Get a mapping of the columns */
	@Override
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("event_desc_id", event_type.id);
		map.put("event_date", new Timestamp(event_date.getTime()));
		map.put("device_id", device_id);
		map.put("station_id", station_id);
		return map;
	}
}
