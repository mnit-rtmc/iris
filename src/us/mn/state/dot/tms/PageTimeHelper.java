/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2022  Minnesota Department of Transportation
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

import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.units.Interval;
import static us.mn.state.dot.tms.units.Interval.Units.DECISECONDS;
import us.mn.state.dot.tms.utils.MultiString;

/**
 * DMS page time helper.
 *
 * @see us.mn.state.dot.tms.utils.MultiString
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class PageTimeHelper {

	/** Don't allow instantiation */
	private PageTimeHelper() { }

	/** Get minimum page-on interval */
	static Interval minPageOnInterval() {
		return new Interval(
			SystemAttrEnum.DMS_PAGE_ON_MIN_SECS.getFloat());
	}

	/** Get maximum page-on interval */
	static Interval maxPageOnInterval() {
		return new Interval(
			SystemAttrEnum.DMS_PAGE_ON_MAX_SECS.getFloat());
	}

	/** Get default page-on interval */
	static public Interval defaultPageOnInterval() {
		return new Interval(2.8f);
	}

	/** Get default page-on time (ds) */
	static public int defaultPageOnTimeDs() {
		return defaultPageOnInterval().round(DECISECONDS);
	}

	/** Get default page-off interval */
	static public Interval defaultPageOffInterval() {
		return new Interval(0.0f);
	}

	/** Get default page-off time (ds) */
	static public int defaultPageOffTimeDs() {
		return defaultPageOffInterval().round(DECISECONDS);
	}

	/** Validate a page-on interval.
	 * @param po Page-on interval.
	 * @param singlepg True for single page messages.
	 * @return Validated interval. */
	static public Interval validateOnInterval(Interval po,
		boolean singlepg)
	{
		return validateValue(po, singlepg, minPageOnInterval(),
			maxPageOnInterval());
	}

	/** Return a validated spinner value. A value of zero is valid
	 * for single page messages only. */
	static public Interval validateValue(Interval val, boolean singlepg,
		Interval min, Interval max)
	{
		int ds = val.round(DECISECONDS);
		int min_ds = min.round(DECISECONDS);
		int max_ds = max.round(DECISECONDS);
		int tenths = validateValue(ds, singlepg, min_ds,max_ds);
		return new Interval(tenths, DECISECONDS);
	}

	/** Validate a page time. A value of zero is valid for single
	 * page messages only.
	 * @param value Page time in tenths.
	 * @param min Minimum page time in tenths.
	 * @param max Maximum page time in tenths.
	 * @return The validated page time in tenths. */
	static private int validateValue(int value, boolean singlepg,
		int min, int max)
	{
		if(singlepg) {
			if(value == 0)
				return 0;
			if(value < min)
				return 0;
			if(value > max)
				return max;
		} else {
			if(value < min)
				return min;
			if(value > max)
				return max;
		}
		return value;
	}

	/** Get the page-on intervals for the specified MULTI string.
	 * @param ms MULTI string.
	 * @return Array of page-on intervals (for each page). */
	static public Interval[] pageOnIntervals(String ms) {
		MultiString m = new MultiString(ms);
		return m.pageOnIntervals(defaultPageOnInterval());
	}

	/** Get the page-off intervals for the specified MULTI string.
	 * @param ms MULTI string.
	 * @return Array of page-off intervals (for each page). */
	static public Interval[] pageOffIntervals(String ms) {
		MultiString m = new MultiString(ms);
		return m.pageOffIntervals(defaultPageOffInterval());
	}
}
