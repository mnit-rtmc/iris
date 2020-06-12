/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2020  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.SystemAttrEnum;

/**
 * This is a class for logging detector auto fail events to a database.
 *
 * @author Douglas Lau
 */
public class DetAutoFailEvent extends BaseEvent {

	/** Database table name */
	static private final String TABLE = "event.detector_event";

	/** Get detector event purge threshold (days) */
	static private int getPurgeDays() {
		return SystemAttrEnum.DETECTOR_EVENT_PURGE_DAYS.getInt();
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

	/** Is the specified event a detector auto fail event? */
	static private boolean isAutoFailEvent(EventType et) {
		return EventType.DET_CHATTER == et
		    || EventType.DET_LOCKED_ON == et
		    || EventType.DET_NO_HITS == et
		    || EventType.DET_NO_CHANGE == et
		    || EventType.DET_OCC_SPIKE == et;
	}

	/** Device ID (if device specific) */
	private final String device_id;

	/** Create a new detector auto fail event */
	public DetAutoFailEvent(EventType et, String d) {
		super(et);
		assert isAutoFailEvent(et);
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
