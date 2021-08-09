/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2021  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.natch;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import us.mn.state.dot.tms.ActionPlan;
import us.mn.state.dot.tms.MeterAction;
import us.mn.state.dot.tms.MeterActionHelper;
import us.mn.state.dot.tms.RampMeter;
import us.mn.state.dot.tms.TimeAction;
import us.mn.state.dot.tms.TimeActionHelper;

/**
 * Ramp meter timing table.
 *
 * @author Douglas Lau
 */
public class TimingTable {

	/** Ramp meter */
	private final RampMeter meter;

	/** Mapping of times (minute-of-day) to start boolean */
	private final TreeMap<Integer, Boolean> events =
		new TreeMap<Integer, Boolean>();

	/** Create a timing table for a ramp meter */
	public TimingTable(RampMeter m) {
		meter = m;
		Iterator<MeterAction> it = MeterActionHelper.iterator();
		while (it.hasNext()) {
			MeterAction ma = it.next();
			if (ma.getRampMeter() == meter)
				createEvent(ma);
		}
	}

	/** Create an event from a meter action */
	private void createEvent(MeterAction ma) {
		ActionPlan ap = ma.getActionPlan();
		if (ap.getActive()) {
			Iterator<TimeAction> it = TimeActionHelper.iterator();
			while (it.hasNext()) {
				TimeAction ta = it.next();
				if (ta.getActionPlan() == ap)
					createEvent(ma, ta);
			}
		}
	}

	/** Create an event from a time action */
	private void createEvent(MeterAction ma, TimeAction ta) {
		Integer min = TimeActionHelper.getMinuteOfDay(ta);
		if (min != null) {
			boolean start = (ma.getPhase() == ta.getPhase());
			events.put(min, start);
		}
	}

	/** Lookup start time */
	public int lookupStart(int entry) {
		int e = 0;
		for (Map.Entry<Integer, Boolean> ev: events.entrySet()) {
			boolean is_start = ev.getValue();
			if (is_start) {
				if (e == entry)
					return ev.getKey();
			} else
				e++;
		}
		return 0;
	}

	/** Lookup stop time */
	public int lookupStop(int entry) {
		int e = 0;
		for (Map.Entry<Integer, Boolean> ev: events.entrySet()) {
			boolean is_start = ev.getValue();
			if (!is_start) {
				if (e == entry)
					return ev.getKey();
				e++;
			}
		}
		return 0;
	}
}
