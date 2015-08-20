/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2013-2015  Minnesota Department of Transportation
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

import us.mn.state.dot.tms.units.Interval;
import static us.mn.state.dot.tms.units.Interval.Units.DECISECONDS;

/**
 * Counter for DMS page-on and page-off times.
 *
 * @author Douglas Lau
 */
public class PageTimeCounter extends MultiAdapter {

	/** Array of page-on times */
	private final Interval[] page_on;

	/** Array of page-off times */
	private final Interval[] page_off;

	/** Create a new page time counter.
	 * @param np Number of pages. */
	public PageTimeCounter(int np) {
		page_on = new Interval[np];
		page_off = new Interval[np];
	}

	/** Add a page */
	@Override
	public void addPage() {
		super.addPage();
		assert ms_page >= 0;
		assert ms_page < page_on.length;
		assert ms_page < page_off.length;
		page_on[ms_page] = pageOnInterval();
		page_off[ms_page] = pageOffInterval();
	}

	/** Set the page times.
	 * @param pt_on Page on time (deciseconds; null means default)
	 * @param pt_off Page off time (deciseconds; null means default) */
	@Override
	public void setPageTimes(Integer pt_on, Integer pt_off) {
		super.setPageTimes(pt_on, pt_off);
		assert ms_page >= 0;
		assert ms_page < page_on.length;
		assert ms_page < page_off.length;
		page_on[ms_page] = pageOnInterval();
		page_off[ms_page] = pageOffInterval();
	}

	/** Get the current page-on interval.
	 * @return Page-on interval for current page. */
	private Interval pageOnInterval() {
		Integer pt = ms_pt_on;
		return (pt != null) ? new Interval(pt, DECISECONDS) : null;
	}

	/** Get the current page-off interval.
	 * @return Page-off interval for current page. */
	private Interval pageOffInterval() {
		Integer pt = ms_pt_off;
		return (pt != null) ? new Interval(pt, DECISECONDS) : null;
	}

	/** Get an array of page-on intervals.
	 * @param dflt Default page-on interval.
	 * @return Array of page-on intervals. */
	public Interval[] pageOnIntervals(Interval dflt) {
		Interval[] ret = new Interval[page_on.length];
		for (int i = 0; i < ret.length; i++) {
			Interval p = page_on[i];
			ret[i] = (p != null) ? p : dflt;
		}
		return ret;
	}

	/** Get an array of page-off intervals.
	 * @param dflt Default page-off interval.
	 * @return Array of page-off intervals. */
	public Interval[] pageOffIntervals(Interval dflt) {
		Interval[] ret = new Interval[page_off.length];
		for (int i = 0; i < ret.length; i++) {
			Interval p = page_off[i];
			ret[i] = (p != null) ? p : dflt;
		}
		return ret;
	}
}
