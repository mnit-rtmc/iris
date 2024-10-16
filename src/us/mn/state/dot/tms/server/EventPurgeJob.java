/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2014-2024  Minnesota Department of Transportation
 * Copyright (C) 2018  Iteris Inc.
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
import java.util.Iterator;
import us.mn.state.dot.sched.Job;
import us.mn.state.dot.tms.EventConfig;
import us.mn.state.dot.tms.EventConfigHelper;
import us.mn.state.dot.tms.TMSException;

/**
 * Job to periodically purge database event records.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class EventPurgeJob extends Job {

	/** Create a new job to purge database events */
	public EventPurgeJob() {
		super(Calendar.DATE, 1, Calendar.HOUR, 2);
	}

	/** Perform the event purge job */
	public void perform() throws TMSException {
		if (null == BaseObjectImpl.store)
			return;
		Iterator<EventConfig> it = EventConfigHelper.iterator();
		while (it.hasNext()) {
			EventConfig ec = it.next();
			if (ec instanceof EventConfigImpl)
				((EventConfigImpl) ec).purgeRecords();
		}
	}
}
