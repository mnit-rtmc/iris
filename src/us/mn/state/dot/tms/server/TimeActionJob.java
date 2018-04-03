/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2018  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.TimeAction;
import us.mn.state.dot.tms.TimeActionHelper;
import us.mn.state.dot.tms.TMSException;

/**
 * Job to perform time actions.
 *
 * @author Douglas Lau
 */
public class TimeActionJob extends Job {

	/** Create a new time action job */
	public TimeActionJob() {
		super(0);
	}

	/** Perform time actions */
	@Override
	public void perform() throws TMSException {
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
