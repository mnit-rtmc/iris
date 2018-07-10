/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2018  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.Gps;
import us.mn.state.dot.tms.GpsHelper;

/**
 * Job to periodically query all GPS devices.
 *
 * @author Douglas Lau
 */
public class GpsQueryJob extends Job {

	/** Seconds to offset each poll from start of interval */
	static private final int OFFSET_SECS = 1;

	/** Create a new job to query GPS */
	public GpsQueryJob() {
		super(Calendar.MINUTE, 5, Calendar.SECOND, OFFSET_SECS);
	}

	/** Perform the GPS query job */
	@Override
	public void perform() {
		int req = DeviceRequest.QUERY_GPS_LOCATION.ordinal();
		Iterator<Gps> it = GpsHelper.iterator();
		while (it.hasNext()) {
			Gps g = it.next();
			if (g instanceof GpsImpl) {
				GpsImpl gps = (GpsImpl) g;
				if (!gps.isLongPeriodModem())
					gps.setDeviceRequest(req);
			}
		}
	}
}
