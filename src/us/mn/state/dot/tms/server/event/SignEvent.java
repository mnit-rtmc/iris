/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2018  Minnesota Department of Transportation
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
 * This is a class for logging sign events to a database.
 *
 * @author Douglas Lau
 */
public class SignEvent extends BaseEvent {

	/** Database table name */
	static private final String TABLE = "event.sign_event";

	/** Get sign event purge threshold (days) */
	static private int getPurgeDays() {
		return SystemAttrEnum.SIGN_EVENT_PURGE_DAYS.getInt();
	}

	/** Purge old records */
	static public void purgeRecords() throws TMSException {
		int age = getPurgeDays();
		if (store != null && age > 0) {
			store.update("DELETE FROM " + TABLE +
				" WHERE event_date < now() - '" + age +
				" days'::interval;");
		}
	}

	/** Is the specified event a sign event? */
	static private boolean isSignEvent(EventType et) {
		return EventType.DMS_DEPLOYED == et
		    || EventType.DMS_CLEARED == et
		    || EventType.LCS_DEPLOYED == et
		    || EventType.LCS_CLEARED == et;
	}

	/** Device ID (if device specific) */
	private final String device_id;

	/** Message text */
	private final String message;

	/** Message owner */
	private final String owner;

	/** Create a new sign event */
	public SignEvent(EventType et, String d, String m, String o) {
		super(et);
		assert isSignEvent(et);
		device_id = d;
		message = m;
		owner = o;
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
		map.put("message", message);
		map.put("owner", owner);
		return map;
	}
}
