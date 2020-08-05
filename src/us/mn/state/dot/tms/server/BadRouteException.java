/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2020  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server;

import us.mn.state.dot.tms.EventType;

/**
 * A bad route exception indicates a problem with a travel time route
 *
 * @author Douglas Lau
 */
public class BadRouteException extends Exception {

	/** Event type for logging */
	public final EventType event_type;

	/** Station ID */
	public final String sid;

	/** Create a new bad route exception */
	public BadRouteException(EventType et, String msg, String sid) {
		super(msg);
		event_type = et;
		this.sid = sid;
	}
}
