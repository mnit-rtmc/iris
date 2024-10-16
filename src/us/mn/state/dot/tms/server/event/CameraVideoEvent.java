/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2019-2024  Minnesota Department of Transportation
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
 * This is a class for logging camera video loss events to a database.
 *
 * @author Douglas Lau
 */
public class CameraVideoEvent extends BaseEvent {

	/** Is the specified event a camera video event? */
	static private boolean isCameraVideoEvent(EventType et) {
		return EventType.CAMERA_VIDEO_LOST == et
		    || EventType.CAMERA_VIDEO_RESTORED == et;
	}

	/** Camera ID */
	private final String camera_id;

	/** Video monitor ID */
	private final String monitor_id;

	/** Create a new camera video loss event */
	public CameraVideoEvent(EventType et, String c, String m) {
		super(et);
		assert isCameraVideoEvent(et);
		camera_id = c;
		monitor_id = m;
	}

	/** Get the event config name */
	@Override
	protected String eventConfigName() {
		return "camera_video_event";
	}

	/** Get the database table name */
	@Override
	public String getTable() {
		return "event.camera_video_event";
	}

	/** Get a mapping of the columns */
	@Override
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("event_desc_id", event_type.id);
		map.put("event_date", new Timestamp(event_date.getTime()));
		map.put("camera_id", camera_id);
		map.put("monitor_id", monitor_id);
		return map;
	}
}
