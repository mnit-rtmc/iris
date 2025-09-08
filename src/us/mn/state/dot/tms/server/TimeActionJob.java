/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2025  Minnesota Department of Transportation
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
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.sched.Scheduler;
import us.mn.state.dot.tms.TimeAction;
import us.mn.state.dot.tms.TimeActionHelper;
import us.mn.state.dot.tms.TMSException;

/**
 * Job to perform time actions.
 *
 * @author Douglas Lau
 */
public class TimeActionJob extends Job {

	/** Seconds to offset each poll from start of interval */
	static private final int OFFSET_SECS = 14;

	/** TIMER Scheduler */
	private final Scheduler timer;

	/** Create a new time action job */
	public TimeActionJob(Scheduler t) {
		super(Calendar.SECOND, 30, Calendar.SECOND, OFFSET_SECS);
		timer = t;
	}

	/** Perform job */
	@Override
	public void perform() throws TMSException {
		performTimeActions();
		timer.addJob(new DeviceActionJob());
	}

	/** Perform all time actions */
	private void performTimeActions() {
		Calendar cal = TimeSteward.getCalendarInstance();
		int min = TimeSteward.currentMinuteOfDayInt();
		Iterator<TimeAction> it = TimeActionHelper.iterator();
		while (it.hasNext()) {
			TimeAction ta = it.next();
			if (ta instanceof TimeActionImpl) {
				TimeActionImpl tai = (TimeActionImpl) ta;
				tai.perform(cal, min);
			}
		}
	}
}
