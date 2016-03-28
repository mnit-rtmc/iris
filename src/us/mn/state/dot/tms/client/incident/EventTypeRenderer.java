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
package us.mn.state.dot.tms.client.incident;

import us.mn.state.dot.tms.EventType;
import us.mn.state.dot.tms.client.widget.IListCellRenderer;

/**
 * Cell renderer used for event types.
 *
 * @author Douglas Lau
 */
public class EventTypeRenderer extends IListCellRenderer<EventType> {

	/** Convert an event type to a string */
	static protected String eventTypeToString(EventType et) {
		String v = et.toString();
		// NOTE: strip off the "INCIDENT_" prefix
		int i = v.indexOf('_');
		return (i >= 0) ? v.substring(i + 1).toLowerCase() : v;
	}

	/** Convert value to a string */
	@Override
	protected String valueToString(EventType value) {
		return eventTypeToString(value);
	}
}
