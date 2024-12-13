/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2024  Minnesota Department of Transportation
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

/**
 * Email events in database.
 *
 * @author Douglas Lau
 */
public class EmailEvent extends BaseEvent {

	/** Is the specified event an email event? */
	static private boolean isEmailEvent(EventType et) {
		return EventType.ACTION_PLAN_SYSTEM == et ||
		       EventType.GATE_ARM_SYSTEM == et;
	}

	/** Message subject */
	private final String subject;

	/** Message text */
	private final String message;

	/** Create a new email event.
	 * @param et Event type.
	 * @param sub Subject.
	 * @param msg Message text. */
	public EmailEvent(EventType et, String sub, String msg) {
		super(et);
		assert isEmailEvent(et);
		subject = sub;
		message = msg;
	}

	/** Get the event config name */
	@Override
	protected String eventConfigName() {
		return "email_event";
	}

	/** Get the database table name */
	@Override
	public String getTable() {
		return "event.email_event";
	}

	/** Get a mapping of the columns */
	@Override
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("event_date", new Timestamp(event_date.getTime()));
		map.put("event_desc", event_type.id);
		map.put("subject", subject);
		map.put("message", message);
		return map;
	}
}
