/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2021  Minnesota Department of Transportation
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
package us.mn.state.dot.tms;

/**
 * InvalidMsgException indicates that a sign is not able to display the
 * specified message.
 *
 * @author Douglas Lau
 */
public class InvalidMsgException extends TMSException {

	/** Event type for logging */
	private final EventType event_type;

	/** Get the event type for logging */
	public EventType getEventType() {
		return event_type;
	}

	/** Create a new invalid message exception */
	public InvalidMsgException(String msg, boolean pixel) {
		super("INVALID MSG: " + msg);
		event_type = (pixel)
			? EventType.DMS_PIXEL_ERROR
		        : EventType.DMS_MSG_ERROR;
	}

	/** Create a new invalid message exception */
	public InvalidMsgException(String msg) {
		this(msg, false);
	}
}
