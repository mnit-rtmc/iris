/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2015  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.TollZone;
import us.mn.state.dot.tms.TollZoneHelper;

/**
 * Job to periodically calculate toll zone pricing.
 *
 * @author Douglas Lau
 */
public class TollZoneJob extends Job {

	/** Period to recalculate toll zone pricing */
	static private final int PERIOD_MINS = 3;

	/** Seconds to offset each poll from start of interval */
	static private final int OFFSET_SECS = 12;

	/** Create a new job to calculate toll zone pricing */
	public TollZoneJob() {
		super(Calendar.SECOND, 30, Calendar.SECOND, OFFSET_SECS);
	}

	/** 3-minute period in day */
	private int period_3 = 0;

	/** Perform the job */
	@Override
	public void perform() {
		updateDensity();
		int p3 = period3Minute();
		if (p3 != period_3) {
			calculatePricing();
			period_3 = p3;
		}
	}

	/** Update density for all toll zones */
	private void updateDensity() {
		Iterator<TollZone> it = TollZoneHelper.iterator();
		while (it.hasNext()) {
			TollZone tz = it.next();
			if (tz instanceof TollZoneImpl) {
				TollZoneImpl zone = (TollZoneImpl)tz;
				zone.updateDensity();
			}
		}
	}

	/** Calculate current 3-minute period */
	private int period3Minute() {
		int min = TimeSteward.currentMinuteOfDayInt();
		return min / PERIOD_MINS;
	}

	/** Calculate pricing for all toll zones */
	private void calculatePricing() {
		Iterator<TollZone> it = TollZoneHelper.iterator();
		while (it.hasNext()) {
			TollZone tz = it.next();
			if (tz instanceof TollZoneImpl) {
				TollZoneImpl zone = (TollZoneImpl)tz;
				zone.calculatePricing();
			}
		}
	}
}
