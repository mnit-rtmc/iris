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
import java.util.LinkedList;
import us.mn.state.dot.sched.Job;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSHelper;
import us.mn.state.dot.tms.SystemAttrEnum;

/**
 * Job to periodically query all dial-up DMS.  It fires every 2 minutes,
 * and if the period has expired, all dialup signs are put on a list.
 * Every time the job fires, one sign is taken off the list and queried.
 * This is to allow a single modem to service all signs.
 *
 * @author Douglas Lau
 */
public class DmsQueryDialupJob extends Job {

	/** Seconds to offset each poll from start of interval */
	static protected final int OFFSET_SECS = 10;

	/** List of signs to poll */
	private final LinkedList<DMSImpl> signs = new LinkedList<DMSImpl>();

	/** Previous time stamp */
	private long stamp;

	/** Create a new job to query dialup DMS */
	public DmsQueryDialupJob() {
		super(Calendar.MINUTE, 2, Calendar.SECOND, OFFSET_SECS);
		queueAllDialupSigns();
	}

	/** Perform the DMS query dialup job */
	public void perform() {
		if(isNewPeriod() && signs.isEmpty())
			queueAllDialupSigns();
		DMSImpl dms = signs.poll();
		if(dms != null)
			querySign(dms);
	}

	/** Is this a new polling period? */
	private boolean isNewPeriod() {
		return stamp != periodStamp();
	}

	/** Queue all dialup signs to be queried */
	private void queueAllDialupSigns() {
		Iterator<DMS> it = DMSHelper.iterator();
		while(it.hasNext()) {
			DMS d = it.next();
			if(d instanceof DMSImpl) {
				DMSImpl dms = (DMSImpl)d;
				if(dms.isActiveDialup())
					signs.offer(dms);
			}
		}
		stamp = periodStamp();
	}

	/** Get the time stamp for the current period */
	private long periodStamp() {
		long period = periodMillis();
		long now = TimeSteward.currentTimeMillis();
		if(period > 0)
			return now - (now % period);
		else
			return now;
	}

	/** Get the polling period in milliseconds */
	private long periodMillis() {
		return SystemAttrEnum.DIALUP_POLL_PERIOD_MINS.getInt() * 60000;
	}

	/** Query one dialup sign */
	private void querySign(DMSImpl dms) {
		dms.setDeviceRequest(DeviceRequest.QUERY_STATUS.ordinal());
		dms.setDeviceRequest(DeviceRequest.QUERY_MESSAGE.ordinal());
	}
}
