/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009  Minnesota Department of Transportation
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

import java.io.IOException;
import java.util.Calendar;
import us.mn.state.dot.sched.Job;
import us.mn.state.dot.tms.SystemAttrEnum;

/**
 * Job to write out prifiling data.
 *
 * @author Douglas Lau
 */
public class ProfilingJob extends Job {

	/** Seconds to offset each poll from start of interval */
	static protected final int OFFSET_SECS = 1;

	/** Profiler instance */
	protected final Profiler profiler = new Profiler();

	/** Create a new profiling job */
	public ProfilingJob() {
		super(Calendar.MINUTE, 1, Calendar.SECOND, OFFSET_SECS);
	}

	/** Perform the profiling job */
	public void perform() throws IOException {
		profiler.debugMemory();
		profiler.debugThreads();
		if(SystemAttrEnum.UPTIME_LOG_ENABLE.getBoolean())
			profiler.appendUptimeLog();
	}
}
