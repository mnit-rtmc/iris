/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2013  Minnesota Department of Transportation
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

/**
 * DMS page time helper.
 * @see MultiString, DmsPgTimeSpinner, SignMessageComposer
 *
 * @author Michael Darter
 * @author Douglas Lau
 */
public class DmsPgTime {

	/** Get minimum page-on interval */
	static public Interval minPageOnInterval() {
		return new Interval(
			SystemAttrEnum.DMS_PAGE_ON_MIN_SECS.getFloat());
	}

	/** Get maximum page-on interval */
	static public Interval maxPageOnInterval() {
		return new Interval(
			SystemAttrEnum.DMS_PAGE_ON_MAX_SECS.getFloat());
	}

	/** page time */
	private final int m_tenths;

	/** constructor */
	public DmsPgTime(int tenths) {
		m_tenths = tenths;
	}

	/** constructor */
	public DmsPgTime(float secs) {
		m_tenths = secsToTenths(secs);
	}

	/** constructor */
	public DmsPgTime(double secs) {
		m_tenths = secsToTenths((float)secs);
	}

	/** Test if the DmsPgTime is equal to another DmsPgTime */
	public boolean equals(Object o) {
		if(o instanceof DmsPgTime)
			return toTenths() == ((DmsPgTime)o).toTenths();
		return false;
	}

	/** Calculate a hash code */
	public int hashCode() {
		return new Integer(m_tenths).hashCode();
	}

	/** Return the page time in tenths */
	public int toTenths() {
		return m_tenths;
	}

	/** Return the page time in seconds */
	public float toSecs() {
		return tenthsToSecs(m_tenths);
	}

	/** Return the page time in ms */
	public int toMs() {
		return m_tenths * 100;
	}

	/** Is the value zero? */
	public Boolean isZero() {
		return toTenths() == 0;
	}

	/** Get default page on-time for sigle and multi-page messages. */
	public static DmsPgTime getDefaultOn(boolean singlepg) {
		if(singlepg)
			return new DmsPgTime(0);
		else {
			return new DmsPgTime(secsToTenths(SystemAttrEnum.
				DMS_PAGE_ON_DEFAULT_SECS.getFloat()));
		}
	}

	/** Get default page-on interval.
	 * @param singlepg True for single-page messages. */
	static public Interval defaultPageOnInterval(boolean singlepg) {
		if(singlepg)
			return new Interval(0);
		else {
			return new Interval(SystemAttrEnum.
				DMS_PAGE_ON_DEFAULT_SECS.getFloat());
		}
	}

	/** Get default page-off interval */
	static public Interval defaultPageOffInterval() {
		return new Interval(
			SystemAttrEnum.DMS_PAGE_OFF_DEFAULT_SECS.getFloat());
	}

	/** Convert from 10ths of a second to seconds */
	public static float tenthsToSecs(int tenths) {
		return (float)tenths / 10f;
	}

	/** Convert from seconds to 10ths. */
	public static int secsToTenths(float secs) {
		return Math.round(secs * 10f);
	}

	/** Convert from seconds to 10ths. */
	public static int MsToTenths(int ms) {
		return (int)(ms / 100);
	}

	/** Valicate a page-on interval.
	 * @param po Page-on interval.
	 * @param singlepg True for single page messages.
	 * @return Validated interval. */
	static public Interval validateOnInterval(Interval po,
		boolean singlepg)
	{
		int ds = po.round(DECISECONDS);
		int min_on = minPageOnInterval().round(DECISECONDS);
		int max_on = maxPageOnInterval().round(DECISECONDS);
		int tenths = validateValue(ds, singlepg, min_on, max_on);
		return new Interval(tenths, DECISECONDS);
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
	static public int validateValue(int value, boolean singlepg,
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
}
