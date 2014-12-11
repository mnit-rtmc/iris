/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2014  Minnesota Department of Transportation
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
 * This is a class for logging beacon events to a database.
 *
 * @author Douglas Lau
 */
public class BeaconEvent extends BaseEvent {

	/** Database table name */
	static private final String TABLE = "event.beacon_event";

	/** Beacon ID */
	private final String beacon;

	/** Create a new beacon event */
	public BeaconEvent(EventType e, String bid) {
		super(e);
		assert (e == EventType.BEACON_ON_EVENT)
		    || (e == EventType.BEACON_OFF_EVENT);
		beacon = bid;
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
		map.put("event_date", event_date);
		map.put("event_desc_id", event_type.id);
		map.put("beacon", beacon);
		return map;
	}
}
