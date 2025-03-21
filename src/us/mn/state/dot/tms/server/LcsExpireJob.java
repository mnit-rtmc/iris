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
import us.mn.state.dot.tms.Lcs;
import us.mn.state.dot.tms.LcsHelper;

/**
 * Job to expire LCS locks.
 *
 * @author Douglas Lau
 */
public class LcsExpireJob extends Job {

	/** Seconds to offset from start of interval. */
	static private final int OFFSET_SECS = 2;

	/** Create a new LCS expire job */
	public LcsExpireJob() {
		super(Calendar.SECOND, 30, Calendar.SECOND, OFFSET_SECS);
	}

	/** Perform the job */
	@Override
	public void perform() {
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
