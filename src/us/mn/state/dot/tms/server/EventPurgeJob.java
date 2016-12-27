/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2014-2016  Minnesota Department of Transportation
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

import java.util.Calendar;
import us.mn.state.dot.sched.Job;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.server.event.CommEvent;
import us.mn.state.dot.tms.server.event.MeterEvent;
import us.mn.state.dot.tms.server.event.TravelTimeEvent;

/**
 * Job to periodically purge database event records.
 *
 * @author Douglas Lau
 */
public class EventPurgeJob extends Job {

	/** Create a new job to purge database events */
	public EventPurgeJob() {
		super(Calendar.DATE, 1, Calendar.HOUR, 2);
	}

	/** Perform the event purge job */
	public void perform() throws TMSException {
		CommEvent.purgeRecords();
		MeterEvent.purgeRecords();
		TravelTimeEvent.purgeRecords();
		IncidentImpl.cache.purge();
	}
}
