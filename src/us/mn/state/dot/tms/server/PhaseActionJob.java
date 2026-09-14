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
import us.mn.state.dot.tms.PhaseAction;
import us.mn.state.dot.tms.PhaseActionHelper;

/**
 * Job to perform phase actions.
 *
 * @author Douglas Lau
 */
public class PhaseActionJob extends Job {

	/** Create a new phase action job */
	public PhaseActionJob() {
		super(Calendar.SECOND, 5);
	}

	/** Perform job */
	@Override
	public void perform() {
		Calendar cal = TimeSteward.getCalendarInstance();
		int min = PhaseActionHelper.getMinuteOfDay(cal.getTime());
		Iterator<PhaseAction> it = PhaseActionHelper.iterator();
		while (it.hasNext()) {
			PhaseAction pa = it.next();
			if (pa instanceof PhaseActionImpl) {
				PhaseActionImpl pai = (PhaseActionImpl) pa;
				pai.checkPerform(cal, min);
			}
		}
	}
}
