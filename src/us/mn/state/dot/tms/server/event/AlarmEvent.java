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
 * This is a class for logging alarm events to a database.
 *
 * @author Douglas Lau
 */
public class AlarmEvent extends BaseEvent {

	/** Is the specified event an alarm event? */
	static private boolean isAlarmEvent(EventType et) {
		return EventType.ALARM_TRIGGERED == et
		    || EventType.ALARM_CLEARED == et;
	}

	/** Alarm name */
	private final String alarm;

	/** Create a new alarm event */
	public AlarmEvent(EventType et, String a) {
		super(et);
		assert isAlarmEvent(et);
		alarm = a;
	}

	/** Get the event config name */
	@Override
	protected String eventConfigName() {
		return "alarm_event";
	}

	/** Get the database table name */
	@Override
	public String getTable() {
		return "event.alarm_event";
	}

	/** Get a mapping of the columns */
	@Override
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("event_desc_id", event_type.id);
		map.put("event_date", new Timestamp(event_date.getTime()));
		map.put("alarm", alarm);
		return map;
	}
}
