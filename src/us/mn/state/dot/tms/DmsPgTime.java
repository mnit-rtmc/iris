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

	/** Get default page on-time */
	public static DmsPgTime getDefaultOn() {
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

	/** Validate an on-time. */
	public static DmsPgTime validateOnTime(DmsPgTime t) {
		if(t == null)
			throw new NullPointerException();
		if(t.toTenths() > MAX_ONTIME.toTenths())
			return MAX_ONTIME;
		if(t.toTenths() < MIN_ONTIME.toTenths())
			t = getDefaultOn();
		return t;
	}
}
