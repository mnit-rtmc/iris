/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2026  Minnesota Department of Transportation
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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Helper class for phase actions.
 *
 * @author Douglas Lau
 */
public class PhaseActionHelper extends BaseHelper {

	/** Don't allow instances to be created */
	private PhaseActionHelper() {
		assert false;
	}

	/** Clock time (no date) condition format */
	static private final DateFormat CLOCK_FORMAT =
		new SimpleDateFormat("HH:mm");

	/** Clock time (with date) condition format */
	static private final DateFormat CLOCK_DATE_FORMAT =
		new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");

	/** Lookup the phase action with the specified name */
	static public PhaseAction lookup(String name) {
		return (PhaseAction) namespace.lookupObject(
			PhaseAction.SONAR_TYPE, name);
	}

	/** Get a phase action iterator */
	static public Iterator<PhaseAction> iterator() {
		return new IteratorWrapper<PhaseAction>(namespace.iterator(
			PhaseAction.SONAR_TYPE));
	}

	/** Get HOLD_TIME seconds */
	static public Integer getHoldSecs(PhaseAction pa) {
		if (pa.getCondition() == ActCondition.HOLD_TIME.ordinal()) {
			String[] v = pa.getParams().split(":", 3);
			try {
				int hr = 0;
				int mn = 0;
				int sc = 0;
				switch (v.length) {
				case 1:
					sc = Integer.parseUnsignedInt(v[0]);
					break;
				case 2:
					mn = Integer.parseUnsignedInt(v[0]);
					sc = Integer.parseUnsignedInt(v[1]);
					break;
				case 3:
					hr = Integer.parseUnsignedInt(v[0]);
					mn = Integer.parseUnsignedInt(v[1]);
					sc = Integer.parseUnsignedInt(v[2]);
					break;
				default:
					return null;
				}
				return (hr * 3600) + (mn * 60) + sc;
			}
			catch (NumberFormatException e) { }
		}
		return null;
	}

	/** Get CLOCK_TIME minute-of-day (0-1440) */
	static public Integer getClockTime(PhaseAction pa) {
		if (pa.getCondition() == ActCondition.CLOCK_TIME.ordinal()) {
			Date d = parseClockTime(pa.getParams());
			if (null == d)
				d = parseClockDateTime(pa.getParams());
			return (d != null) ? getMinuteOfDay(d) : null;
		}
		return null;
	}

	/** Get the minute-of-day (0-1440) */
	static private int getMinuteOfDay(Date d) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(d);
		return cal.get(Calendar.HOUR_OF_DAY) * 60 +
		       cal.get(Calendar.MINUTE);
	}

	/** Get CLOCK_TIME calendar date */
	static public Calendar getClockDate(PhaseAction pa) {
		if (pa.getCondition() == ActCondition.CLOCK_TIME.ordinal()) {
			Date d = parseClockDateTime(pa.getParams());
			if (d != null) {
				Calendar cal = Calendar.getInstance();
				cal.setTime(d);
				return cal;
			}
		}
		return null;
	}

	/** Parse a clock time param */
	static private Date parseClockTime(String p) {
		try {
			return CLOCK_FORMAT.parse(p);
		}
		catch (ParseException e) {
			return null;
		}
	}

	/** Parse a clock date / time param */
	static private Date parseClockDateTime(String p) {
		try {
			return CLOCK_DATE_FORMAT.parse(p);
		}
		catch (ParseException e) {
			return null;
		}
	}
}
