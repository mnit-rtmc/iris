/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2012  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.WarningSign;
import us.mn.state.dot.tms.WarningSignHelper;

/**
 * Job to periodically query all warning sign status.
 *
 * @author Douglas Lau
 */
public class WarnQueryStatusJob extends Job {

	/** Seconds to offset each poll from start of interval */
	static protected final int OFFSET_SECS = 12;

	/** Create a new job to query warning sign status */
	public WarnQueryStatusJob(int i_secs) {
		super(Calendar.SECOND, i_secs, Calendar.SECOND, OFFSET_SECS);
	}

	/** Perform the warning sign query status job */
	public void perform() {
		int req = DeviceRequest.QUERY_STATUS.ordinal();
		Iterator<WarningSign> it = WarningSignHelper.iterator();
		while(it.hasNext()) {
			WarningSign sign = it.next();
			sign.setDeviceRequest(req);
		}
	}
}
