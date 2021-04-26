/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2021  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.ss125;

import java.io.IOException;

/**
 * Clear Event FIFO property.
 *
 * @author Douglas Lau
 */
public class ClearEventsProperty extends SS125Property {

	/** Message ID for clear event FIFO request */
	@Override
	protected MessageID msgId() {
		return MessageID.CLEAR_EVENT_FIFO;
	}

	/** Format a STORE request */
	@Override
	protected byte[] formatStore() throws IOException {
		byte[] body = new byte[4];
		formatBody(body, MessageType.WRITE);
		return body;
	}

	/** Get a string representation of the property */
	@Override
	public String toString() {
		return "clear_event_FIFO";
	}
}
