/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2026  Minnesota Department of Transportation
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
import us.mn.state.dot.sched.Scheduler;
import static us.mn.state.dot.tms.server.MainServer.TIMER;

/**
 * Job to calculate station data.
 *
 * @author Douglas Lau
 */
public class StationDataJob extends Job {

	/** Seconds to offset from start of interval.
	 *
	 * This must be *after* binned detector data has been collected, to
	 * enable station data calculation. */
	static private final int OFFSET_SECS = 26;

	/** FLUSH Scheduler for writing XML (I/O to disk) */
	private final Scheduler flush;

	/** Station manager */
	private final StationManager station_manager;

	/** Job to be performed after data has been processed */
	private final FlushXmlJob flush_job;

	/** Create a new station data job */
	public StationDataJob(Scheduler f) {
		super(Calendar.SECOND, 30, Calendar.SECOND, OFFSET_SECS);
		flush = f;
		station_manager = new StationManager();
		flush_job = new FlushXmlJob(station_manager);
	}

	/** Perform the station data job */
	@Override
	public void perform() {
		try {
			// FIXME: read pollinator data
			station_manager.calculateData();
			// Perform flush job after station data calculated
			flush.addJob(flush_job);
			BaseObjectImpl.corridors.findBottlenecks();
		}
		finally {
			TIMER.addJob(new MeteringJob());
		}
	}
}
