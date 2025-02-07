/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2024-2025  Minnesota Department of Transportation
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
 * This is a class for logging ramp meter lock events to a database.
 *
 * @author Douglas Lau
 */
public class MeterLockEvent extends BaseEvent {

	/** Ramp meter name */
	private final String ramp_meter;

	/** Meter lock */
	private final String lock;

	/** Create a new meter lock event */
	public MeterLockEvent(String rm, String lk) {
		super(EventType.METER_LOCK_EVENT);
		ramp_meter = rm;
		lock = lk;
	}

	/** Get the event config name */
	@Override
	protected String eventConfigName() {
		return "meter_lock_event";
	}

	/** Get the database table name */
	@Override
	public String getTable() {
		return "event.meter_lock_event";
	}

	/** Get a mapping of the columns */
	@Override
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("event_date", new Timestamp(event_date.getTime()));
		map.put("event_desc", event_type.id);
		map.put("ramp_meter", ramp_meter);
		map.put("lock", lock);
		return map;
	}
}
