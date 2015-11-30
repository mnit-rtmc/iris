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

	/** Agency ID */
	private final Integer agency;

	/** Tag (transponder) ID */
	private final int tag_id;

	/** Tag Reader ID */
	private final String tag_reader;

	/** HOV flag */
	private final boolean hov;

	/** Create a new tag read event */
	public TagReadEvent(EventType et, Date ed, int tt, Integer ag, int tid,
		String tr, boolean h)
	{
		super(et, ed);
		tag_type = tt;
		agency = ag;
		tag_id = tid;
		tag_reader = tr;
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
		map.put("agency", agency);
		map.put("tag_id", tag_id);
		map.put("tag_reader", tag_reader);
		map.put("hov", hov);
		return map;
	}
}
