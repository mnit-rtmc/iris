/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2012-2013  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.utils.SString;

/**
 * This is a class for logging client access events to a database.
 *
 * @author Douglas Lau
 */
public class ClientEvent extends BaseEvent {

	/** Host:port of client connection */
	private final String host_port;

	/** User name */
	private final String iris_user;

	/** Create a new client event */
	public ClientEvent(EventType e, String hp, String iu) {
		super(e);
		assert e == EventType.CLIENT_CONNECT ||
		       e == EventType.CLIENT_AUTHENTICATE ||
		       e == EventType.CLIENT_FAIL_AUTHENTICATION ||
		       e == EventType.CLIENT_DISCONNECT;
		host_port = SString.truncate(hp, 64);
		iris_user = SString.truncate(iu, 15);
	}

	/** Get the database table name */
	public String getTable() {
		return "event.client_event";
	}

	/** Get a mapping of the columns */
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("event_desc_id", event_type.id);
		map.put("event_date", event_date);
		map.put("host_port", host_port);
		map.put("iris_user", iris_user);
		return map;
	}
}
