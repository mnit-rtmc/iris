/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2013-2016  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.utils;

import java.util.ArrayList;
import us.mn.state.dot.tms.units.Interval;
import static us.mn.state.dot.tms.units.Interval.Units.DECISECONDS;
import us.mn.state.dot.tms.MultiAdapter;

/**
 * Counter for DMS page-on and page-off times.
 *
 * @author Douglas Lau
 */
public class PageTimeCounter extends MultiAdapter {

	/** Convert a page time to an interval.
	 * @param pt Page time (in deciseconds).
	 * @return Interval value. */
	static private Interval toInterval(Integer pt) {
		return (pt != null) ? new Interval(pt, DECISECONDS) : null;
	}

	/** Replace null page intervals with a default value.
	 * @param pi Page interval array.
	 * @param dflt Default interval.
	 * @return Page interval array. */
	static private Interval[] intervalsDef(Interval[] pi, Interval dflt) {
		for (int i = 0; i < pi.length; i++) {
			if (pi[i] == null)
				pi[i] = dflt;
		}
		return pi;
	}

	/** Array of page-on times */
	private final ArrayList<Interval> page_on = new ArrayList<Interval>();

	/** Array of page-off times */
	private final ArrayList<Interval> page_off = new ArrayList<Interval>();

	/** Current page-on interval */
	private Interval pg_on;

	/** Current page-off interval */
	private Interval pg_off;

	/** Create a new page time counter */
	public PageTimeCounter() {
		page_on.add(pg_on);
		page_off.add(pg_off);
	}

	/** Add a page */
	@Override
	public void addPage() {
		page_on.add(pg_on);
		page_off.add(pg_off);
	}

	/** Set the page times.
	 * @param pt_on Page on time (deciseconds; null means default)
	 * @param pt_off Page off time (deciseconds; null means default) */
	@Override
	public void setPageTimes(Integer pt_on, Integer pt_off) {
		pg_on = toInterval(pt_on);
		pg_off = toInterval(pt_off);
		page_on.set(page_on.size() - 1, pg_on);
		page_off.set(page_off.size() - 1, pg_off);
	}

	/** Get an array of page-on intervals.
	 * @param dflt Default page-on interval.
	 * @return Array of page-on intervals. */
	public Interval[] pageOnIntervals(Interval dflt) {
		return intervalsDef(page_on.toArray(new Interval[0]), dflt);
	}

	/** Get an array of page-off intervals.
	 * @param dflt Default page-off interval.
	 * @return Array of page-off intervals. */
	public Interval[] pageOffIntervals(Interval dflt) {
		return intervalsDef(page_off.toArray(new Interval[0]), dflt);
	}
}
