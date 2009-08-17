/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009  Minnesota Department of Transportation
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

import java.lang.NullPointerException;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.utils.SString;

/**
 *  A DMS page time.
 *  @see MultiString, DmsPgTimeSpinner, SignMessageComposer
 *  @author Michael Darter
 */
public class DmsPgTime {

	/** On-time minimum and maximum values, inclusive */
	public static final DmsPgTime MIN_ONTIME = new DmsPgTime(5);
	public static final DmsPgTime MAX_ONTIME = new DmsPgTime(100);
	/** on-time default, used only if system default is bogus. */
	private static final DmsPgTime DEF_ONTIME = new DmsPgTime(25);

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

	/** Return the page time in tenths */
	public String toString() {
		return SString.intToString(m_tenths);
	}

	/** Is the value zero? */
	public Boolean isZero() {
		return toTenths() == 0;
	}

	/** Get default page on-time for sigle and multi-page messages. */
	public static DmsPgTime getDefaultOn(boolean singlepg) {
		if(singlepg)
			return new DmsPgTime(0);
		else
			return new DmsPgTime(secsToTenths(
				SystemAttrEnum.DMS_PAGE_ON_SECS.getFloat()));
	}

	/** Get default page off-time */
	public static DmsPgTime getDefaultOff() {
		return new DmsPgTime(secsToTenths(
			SystemAttrEnum.DMS_PAGE_OFF_SECS.getFloat()));
	}

	/** Convert from 10ths of a second to seconds */
	public static float tenthsToSecs(int tenths) {
		return (float)tenths / 10f;
	}

	/** Convert from seconds to 10ths. */
	public static int secsToTenths(float secs) {
		return (int)(secs * 10f);
	}

	/** Convert from seconds to 10ths. */
	public static int MsToTenths(int ms) {
		return (int)(ms / 100);
	}

	/** Validate an on-time as a function of if the message is single
	 *  or multi-page. */
	public static DmsPgTime validateOnTime(DmsPgTime t, 
		boolean singlepg)
	{
		if(t == null)
			throw new NullPointerException();
		int tenths = (int)validateValue(t.toTenths(), singlepg, 
			MIN_ONTIME.toTenths(), MAX_ONTIME.toTenths());
		return new DmsPgTime(tenths);
	}

	/** Return a validated spinner value. A value of zero is valid
	 *  for single page messages only. */
	public DmsPgTime validateValue(boolean singlepg, 
		DmsPgTime min, DmsPgTime max)
	{
		int tenths = validateValue(toTenths(), 
			singlepg, min.toTenths(), max.toTenths());
		return new DmsPgTime(tenths);
	}

	/** Validate a page time. A value of zero is valid for single 
	 *  page messages only. 
	 *  @param value Page time in tenths.
	 *  @param min Minimum page time in tenths.
	 *  @param max Maximum page time in tenths.
	 *  @return The validated page time in tenths. */
	public static int validateValue(int value, boolean singlepg, 
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
