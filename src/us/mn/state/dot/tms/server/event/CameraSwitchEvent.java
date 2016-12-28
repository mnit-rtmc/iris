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

/**
 * This is a class for logging camera switching events to a database.
 *
 * @author Douglas Lau
 */
public class CameraSwitchEvent extends BaseEvent {

	/** Video monitor ID */
	private final String monitor_id;

	/** Camera ID */
	private final String camera_id;

	/** Source of switch command */
	private final String source;

	/** Create a new camera switch event */
	public CameraSwitchEvent(String m, String c, String s) {
		super(EventType.CAMERA_SWITCHED);
		monitor_id = m;
		camera_id = c;
		source = s;
	}

	/** Get the database table name */
	@Override
	public String getTable() {
		return "event.camera_switch_event";
	}

	/** Get a mapping of the columns */
	@Override
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("event_desc_id", event_type.id);
		map.put("event_date", event_date);
		map.put("monitor_id", monitor_id);
		map.put("camera_id", camera_id);
		map.put("source", source);
		return map;
	}
}
