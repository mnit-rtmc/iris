/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2012-2024  Minnesota Department of Transportation
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

import us.mn.state.dot.sched.Job;
import us.mn.state.dot.sched.Scheduler;
import us.mn.state.dot.tms.EventType;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.server.event.ClientEvent;

/**
 * Client access logging (to database)
 *
 * @author Douglas Lau
 */
public class AccessLogger {

	/** FLUSH Scheduler for I/O jobs */
	private final Scheduler flush;

	/** Create a new access logger */
	public AccessLogger(Scheduler f) {
		flush = f;
	}

	/** Log a connect event */
	public void connect(String hostport) {
		log_event(EventType.CLIENT_CONNECT, hostport, null);
	}

	/** Log an authenticate event */
	public void authenticate(String hostport, String user) {
		log_event(EventType.CLIENT_AUTHENTICATE, hostport, user);
	}

	/** Callback for a client fail domain login event */
	public void failDomain(String hostport, String user) {
		log_event(EventType.CLIENT_FAIL_DOMAIN, hostport, user);
	}

	/** Log a fail authentication event */
	public void failAuthentication(String hostport, String user) {
		log_event(EventType.CLIENT_FAIL_AUTHENTICATION, hostport, user);
	}

	/** Log a disconnect event */
	public void disconnect(String hostport, String user) {
		log_event(EventType.CLIENT_DISCONNECT, hostport, user);
	}

	/** Log a change password event */
	public void changePassword(String hostport, String user) {
		log_event(EventType.CLIENT_CHANGE_PASSWORD, hostport, user);
	}

	/** Log an update password event */
	public void updatePassword(String hostport, String user) {
		log_event(EventType.CLIENT_UPDATE_PASSWORD, hostport, user);
	}

	/** Log a fail password event */
	public void failPassword(String hostport, String user) {
		log_event(EventType.CLIENT_FAIL_PASSWORD, hostport, user);
	}

	/** Log an event */
	private void log_event(EventType event, String hostport, String user) {
		final ClientEvent ev = new ClientEvent(event, hostport, user);
		flush.addJob(new Job() {
			public void perform() throws TMSException {
				ev.doStore();
			}
		});
	}
}
