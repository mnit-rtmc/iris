/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.event;

import java.util.Date;
import us.mn.state.dot.tms.BaseObjectImpl;

/**
 * This is the base class for logging events to a database.
 *
 * @author Douglas Lau
 */
abstract public class BaseEvent extends BaseObjectImpl {

	/** Event type */
	protected final EventType event_type;

	/** Event date */
	protected final Date event_date;

	/** Create a new base event */
	protected BaseEvent(EventType e) {
		super(null);
		event_type = e;
		event_date = new Date();
	}
}
