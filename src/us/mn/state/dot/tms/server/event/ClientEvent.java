/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2012-2019  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.utils.SString;

/**
 * This is a class for logging client access events to a database.
 *
 * @author Douglas Lau
 */
public class ClientEvent extends BaseEvent {

	/** Database table name */
	static private final String TABLE = "event.client_event";

	/** Get client event purge threshold (days) */
	static private int getPurgeDays() {
		return SystemAttrEnum.CLIENT_EVENT_PURGE_DAYS.getInt();
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

	/** Is the specified event a client event? */
	static private boolean isClientEvent(EventType et) {
		return EventType.CLIENT_CONNECT == et
		    || EventType.CLIENT_AUTHENTICATE == et
		    || EventType.CLIENT_FAIL_AUTHENTICATION == et
		    || EventType.CLIENT_DISCONNECT == et
		    || EventType.CLIENT_CHANGE_PASSWORD == et
		    || EventType.CLIENT_FAIL_PASSWORD == et
		    || EventType.CLIENT_FAIL_DOMAIN == et;
	}

	/** Host:port of client connection */
	private final String host_port;

	/** User name */
	private final String iris_user;

	/** Create a new client event */
	public ClientEvent(EventType et, String hp, String iu) {
		super(et);
		assert isClientEvent(et);
		host_port = SString.truncate(hp, 64);
		iris_user = SString.truncate(iu, 15);
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
		map.put("host_port", host_port);
		map.put("iris_user", iris_user);
		return map;
	}
}
