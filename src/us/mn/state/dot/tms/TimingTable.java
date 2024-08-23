/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2021-2024  Minnesota Department of Transportation
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
package us.mn.state.dot.tms;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

/**
 * Ramp meter timing table.
 *
 * @author Douglas Lau
 */
public class TimingTable {

	/** All device actions for the ramp meter */
	private final ArrayList<DeviceAction> dev_actions;

	/** Mapping of times (minute-of-day) to start/stop boolean */
	private final TreeMap<Integer, Boolean> events =
		new TreeMap<Integer, Boolean>();

	/** Create a timing table for a device with the given hashtags */
	public TimingTable(Hashtags tags) {
		dev_actions = DeviceActionHelper.find(tags);
		ArrayList<TimeAction> time_actions =
			TimeActionHelper.find(dev_actions);
		for (TimeAction ta : time_actions) {
			ActionPlan ap = ta.getActionPlan();
			Integer min = TimeActionHelper.getMinuteOfDay(ta);
			if (ap.getActive() && min != null)
				createEvent(ta, min);
		}
	}

	/** Create an event from a time action */
	private void createEvent(TimeAction ta, Integer min) {
		boolean start = false;
		ActionPlan ap = ta.getActionPlan();
		for (DeviceAction da: dev_actions) {
			if (da.getActionPlan() == ap)
				start |= (ta.getPhase() == da.getPhase());
		}
		events.put(min, start);
	}

	/** Lookup start time for an entry */
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

	/** Lookup stop time for an entry */
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
