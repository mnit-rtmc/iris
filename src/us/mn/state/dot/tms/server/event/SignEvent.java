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
 * This is a class for logging sign events to a database.
 *
 * @author Douglas Lau
 */
public class SignEvent extends BaseEvent {

	/** Is the specified event a sign event? */
	static private boolean isSignEvent(EventType et) {
		return EventType.DMS_DEPLOYED == et
		    || EventType.DMS_CLEARED == et
		    || EventType.DMS_MSG_ERROR == et
		    || EventType.DMS_PIXEL_ERROR == et
		    || EventType.DMS_MSG_RESET == et
		    || EventType.LCS_DEPLOYED == et
		    || EventType.LCS_CLEARED == et;
	}

	/** Device ID (if device specific) */
	private final String device_id;

	/** Message MULTI text */
	private final String multi;

	/** Message owner */
	private final String msg_owner;

	/** Duration (minutes) */
	private final Integer duration;

	/** Create a new sign event */
	public SignEvent(EventType et, String d, String m, String o,
		Integer dur)
	{
		super(et);
		assert isSignEvent(et);
		device_id = d;
		multi = m;
		msg_owner = o;
		duration = dur;
	}

	/** Get the event config name */
	@Override
	protected String eventConfigName() {
		return "sign_event";
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
		map.put("event_date", new Timestamp(event_date.getTime()));
		map.put("device_id", device_id);
		map.put("multi", multi);
		map.put("msg_owner", msg_owner);
		map.put("duration", duration);
		return map;
	}
}
