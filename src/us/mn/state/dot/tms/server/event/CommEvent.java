/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2014  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.TMSException;

/**
 * This is a class for logging communication events to a database.
 *
 * @author Douglas Lau
 */
public class CommEvent extends BaseEvent {

	/** Database table name */
	static private final String TABLE = "event.comm_event";

	/** Get comm event purge threshold (days) */
	static public int getCommEventPurgeDays() {
		return SystemAttrEnum.COMM_EVENT_PURGE_DAYS.getInt();
	}

	/** Purge old records */
	static public void purgeRecords() throws TMSException {
		int age = getCommEventPurgeDays();
		if (store != null && age >= 0) {
			store.update("DELETE FROM " + TABLE +
				" WHERE event_date < now() - '" + age +
				" days'::interval;");
		}
	}

	/** Controller affected by this event */
	protected final String controller;

	/** Device ID (if device specific) */
	protected final String device_id;

	/** Create a new comm event */
	public CommEvent(EventType e, String c, String dev) {
		super(e);
		assert e == EventType.QUEUE_DRAINED ||
		       e == EventType.POLL_TIMEOUT_ERROR ||
		       e == EventType.PARSING_ERROR ||
		       e == EventType.CHECKSUM_ERROR ||
		       e == EventType.CONTROLLER_ERROR ||
		       e == EventType.COMM_ERROR ||
		       e == EventType.COMM_FAILED ||
		       e == EventType.COMM_RESTORED;
		controller = c;
		device_id = dev;
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
		map.put("controller", controller);
		map.put("device_id", device_id);
		return map;
	}
}
