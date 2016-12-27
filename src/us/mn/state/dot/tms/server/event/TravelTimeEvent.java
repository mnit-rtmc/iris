/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2016  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.TMSException;

/**
 * This is a class for logging travel time events to a database.
 *
 * @author Douglas Lau
 */
public class TravelTimeEvent extends BaseEvent {

	/** Database table name */
	static private final String TABLE = "event.travel_time_event";

	/** Get travel time event purge threshold (days) */
	static private int getTravelTimeEventPurgeDays() {
		return 1;
	}

	/** Purge old records */
	static public void purgeRecords() throws TMSException {
		int age = getTravelTimeEventPurgeDays();
		if (store != null && age >= 0) {
			store.update("DELETE FROM " + TABLE +
				" WHERE event_date < now() - '" + age +
				" days'::interval;");
		}
	}

	/** Device ID */
	private final String device_id;

	/** Create a new travel time event */
	public TravelTimeEvent(EventType e, String d) {
		super(e);
		assert EventType.TT_LINK_TOO_LONG       == e ||
		       EventType.TT_NO_DATA             == e ||
		       EventType.TT_NO_DESTINATION_DATA == e ||
		       EventType.TT_NO_ORIGIN_DATA      == e ||
		       EventType.TT_NO_ROUTE            == e;
		device_id = d;
	}

	/** Get the database table name */
	@Override
	public String getTable() {
		return TABLE;
	}

	/** Get a mapping of the columns */
	@Override
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("event_desc_id", event_type.id);
		map.put("event_date", event_date);
		map.put("device_id", device_id);
		return map;
	}
}
