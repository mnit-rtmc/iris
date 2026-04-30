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
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.sched.Scheduler;
import us.mn.state.dot.tms.PhaseAction;
import us.mn.state.dot.tms.PhaseActionHelper;
import us.mn.state.dot.tms.TimeAction;
import us.mn.state.dot.tms.TimeActionHelper;
import us.mn.state.dot.tms.TMSException;
import static us.mn.state.dot.tms.server.MainServer.TIMER;

/**
 * Job to perform phase actions.
 *
 * @author Douglas Lau
 */
public class PhaseActionJob extends Job {

	/** Seconds to offset each poll from start of interval */
	static private final int OFFSET_SECS = 14;

	/** Create a new phase action job */
	public PhaseActionJob() {
		super(Calendar.SECOND, 30, Calendar.SECOND, OFFSET_SECS);
	}

	/** Perform job */
	@Override
	public void perform() throws TMSException {
		Calendar cal = TimeSteward.getCalendarInstance();
		int min = TimeSteward.currentMinuteOfDayInt();
		performTimeActions(cal, min);
		performPhaseActions(cal, min);
		TIMER.addJob(new DeviceActionJob());
	}

	/** Perform all time actions */
	private void performTimeActions(Calendar cal, int min) {
		Iterator<TimeAction> it = TimeActionHelper.iterator();
		while (it.hasNext()) {
			TimeAction ta = it.next();
			if (ta instanceof TimeActionImpl) {
				TimeActionImpl tai = (TimeActionImpl) ta;
				tai.perform(cal, min);
			}
		}
	}

	/** Perform all phase actions */
	private void performPhaseActions(Calendar cal, int min) {
		Iterator<PhaseAction> it = PhaseActionHelper.iterator();
		while (it.hasNext()) {
			PhaseAction pa = it.next();
			if (pa instanceof PhaseActionImpl) {
				PhaseActionImpl pai = (PhaseActionImpl) pa;
				pai.perform(cal, min);
			}
		}
	}
}
