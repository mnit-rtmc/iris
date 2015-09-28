/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2015  Minnesota Department of Transportation
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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import us.mn.state.dot.tms.EventType;

/**
 * This is a class for logging tag read events to a database.
 *
 * @author Douglas Lau
 */
public class TagReadEvent extends BaseEvent {

	/** Database table name */
	static private final String TABLE = "event.tag_read_event";

	/** Tag type */
	private final int tag_type;

	/** Tag (transponder) ID */
	private final int tag_id;

	/** Tag Reader ID */
	private final String tag_reader;

	/** Toll zone ID */
	private final String toll_zone;

	/** Tollway ID */
	private final String tollway;

	/** HOV flag */
	private final boolean hov;

	/** Create a new tag read event */
	public TagReadEvent(EventType et, Date ed, int tt, int tid,
		String tr, String tz, String tw, boolean h)
	{
		super(et, ed);
		tag_type = tt;
		tag_id = tid;
		tag_reader = tr;
		toll_zone = tz;
		tollway = tw;
		hov = h;
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
		map.put("tag_type", tag_type);
		map.put("tag_id", tag_id);
		map.put("tag_reader", tag_reader);
		map.put("toll_zone", toll_zone);
		map.put("tollway", tollway);
		map.put("hov", hov);
		return map;
	}
}
