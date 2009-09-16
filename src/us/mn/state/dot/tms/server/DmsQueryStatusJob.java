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

import java.util.Calendar;
import us.mn.state.dot.sched.Job;
import us.mn.state.dot.sonar.Checker;
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSHelper;

/**
 * Job to periodically query all DMS status.
 *
 * @author Douglas Lau
 */
public class DmsQueryStatusJob extends Job {

	/** Seconds to offset each poll from start of interval */
	static protected final int OFFSET_SECS = 2;

	/** Create a new job to query DMS status */
	public DmsQueryStatusJob() {
		super(Calendar.MINUTE, 5, Calendar.SECOND, OFFSET_SECS);
	}

	/** Perform the DMS query status job */
	public void perform() {
		final int req = DeviceRequest.QUERY_STATUS.ordinal();
		DMSHelper.find(new Checker<DMS>() {
			public boolean check(DMS dms) {
				if(DMSHelper.isPeriodicallyQueriable(dms))
					dms.setDeviceRequest(req);
				return false;
			}
		});
	}
}
