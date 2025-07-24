/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2025  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSHelper;
import us.mn.state.dot.tms.Lcs;
import us.mn.state.dot.tms.LcsHelper;

/**
 * Job to expire DMS and LCS locks.
 *
 * @author Douglas Lau
 */
public class LockExpireJob extends Job {

	/** Seconds to offset from start of interval */
	static private final int OFFSET_SECS = 0;

	/** Create a new lock expire job */
	public LockExpireJob() {
		super(Calendar.SECOND, 30, Calendar.SECOND, OFFSET_SECS);
	}

	/** Perform the job */
	@Override
	public void perform() {
		expireDmsLocks();
		expireLcsLocks();
	}

	/** Check DMS locks for expiration */
	private void expireDmsLocks() {
		Iterator<DMS> it = DMSHelper.iterator();
		while (it.hasNext()) {
			DMS dms = it.next();
			if (dms instanceof DMSImpl) {
				DMSImpl dmsi = (DMSImpl) dms;
				dmsi.checkLockExpired();
			}
		}
	}

	/** Check LCS locks for expiration */
	private void expireLcsLocks() {
		Iterator<Lcs> it = LcsHelper.iterator();
		while (it.hasNext()) {
			Lcs lcs = it.next();
			if (lcs instanceof LcsImpl) {
				LcsImpl lcsi = (LcsImpl) lcs;
				lcsi.checkLockExpired();
			}
		}
	}
}
