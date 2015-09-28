/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2015  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.EventType;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.server.SQLConnection;
import us.mn.state.dot.tms.server.Storable;

/**
 * This is the base class for logging events to a database.
 *
 * @author Douglas Lau
 */
abstract public class BaseEvent implements Storable {

	/** SQL connection */
	static public SQLConnection store;

	/** Event type */
	public final EventType event_type;

	/** Event date */
	public final Date event_date;

	/** Create a new base event */
	protected BaseEvent(EventType et, Date ed) {
		event_type = et;
		event_date = ed;
	}

	/** Create a new base event */
	protected BaseEvent(EventType et) {
		this(et, new Date());
	}

	/** Get the primary key name */
	public String getKeyName() {
		return "name";
	}

	/** Get the primary key */
	public String getKey() {
		return null;
	}

	/** Store an object */
	public void doStore() throws TMSException {
		store.create(this);
	}
}
