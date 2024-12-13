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
package us.mn.state.dot.tms.server;

import us.mn.state.dot.tms.EventType;
import us.mn.state.dot.tms.server.event.EmailEvent;

/**
 * Email event handler
 *
 * @author Douglas Lau
 */
public class EmailHandler {

	/** Send an email */
	static public void send(final EventType et, final String sub,
		final String msg)
	{
		BaseObjectImpl.logEvent(new EmailEvent(et, sub, msg));
	}
}
