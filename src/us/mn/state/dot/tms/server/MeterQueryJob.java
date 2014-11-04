/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2014  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.RampMeter;
import us.mn.state.dot.tms.RampMeterHelper;
import static us.mn.state.dot.tms.server.SampleQuery30SecJob.OFFSET_SECS;

/**
 * Job to query ramp meter status and green counts.
 *
 * @author Douglas Lau
 */
public class MeterQueryJob extends Job {

	/** Create a new ramp meter status query job */
	public MeterQueryJob() {
		super(Calendar.SECOND, 30, Calendar.SECOND, OFFSET_SECS);
	}

	/** Perform the job */
	@Override
	public void perform() {
		queryMeterStatus();
	}

	/** Poll all ramp meter status */
	private void queryMeterStatus() {
		final int dr = DeviceRequest.QUERY_STATUS.ordinal();
		Iterator<RampMeter> it = RampMeterHelper.iterator();
		while (it.hasNext())
			it.next().setDeviceRequest(dr);
	}
}
