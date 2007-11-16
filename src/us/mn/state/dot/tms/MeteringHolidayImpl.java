/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2003-2007  Minnesota Department of Transportation
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

import java.rmi.RemoteException;
import java.util.Calendar;
import us.mn.state.dot.vault.FieldMap;

/**
 * MeteringHolidayImpl
 *
 * @author Douglas Lau
 */
public class MeteringHolidayImpl extends TMSObjectImpl
	implements MeteringHoliday, Storable
{
	/** ObjectVault table name */
	static public final String tableName = "metering_holiday";

	/** Get the database table name */
	public String getTable() {
		return tableName;
	}

	/** Create a new metering holiday */
	public MeteringHolidayImpl(String n) throws TMSException,
		RemoteException
	{
		name = n;
		month = ANY_MONTH;
		day = ANY_DAY;
		weekday = ANY_WEEKDAY;
		week = ANY_WEEK;
		period = ANY_PERIOD;
		shift = 0;
	}

	/** Create a metering holiday from an ObjectVault field map */
	protected MeteringHolidayImpl(FieldMap fields) throws RemoteException {
		super();
		name = (String)fields.get("name");
	}

	/** Get the object key */
	public String getKey() {
		return name;
	}

	/** Metering holiday name */
	protected final String name;

	/** Get the holiday name */
	public String getName() { return name; }

	/** Check if a selection of properties is valid */
	protected void checkSelection(int d, int w, int s)
		throws ChangeVetoException
	{
		if(d != ANY_DAY) {
			if(w != ANY_WEEK || s != 0) throw
				new ChangeVetoException("Invalid selection");
		}
	}

	/** Month of year */
	protected int month;

	/** Set the month */
	public void setMonth(int m) throws TMSException {
		if(m == month) return;
		if(m != ANY_MONTH) {
			if(m < Calendar.JANUARY || m > Calendar.DECEMBER) throw
				new ChangeVetoException("Invalid month:" + m);
		}
		store.update(this, "month", m);
		month = m;
	}

	/** Get the month */
	public int getMonth() { return month; }

	/** Day of month */
	protected int day;

	/** Set the day-of-month */
	public void setDay(int d) throws TMSException {
		if(d == day) return;
		if(d != ANY_DAY) {
			if(d < 1 || d > 31) throw new
				ChangeVetoException("Invalid day:" + d);
			checkSelection(d, week, shift);
		}
		store.update(this, "day", d);
		day = d;
	}

	/** Get the day-of-month */
	public int getDay() { return day; }

	/** Week of month */
	protected int week;

	/** Set the week-of-month */
	public void setWeek(int w) throws TMSException {
		if(w == week) return;
		if(w != ANY_WEEK) {
			if(w < -1 || w > 5) throw new
				ChangeVetoException("Invalid week:" + w);
			checkSelection(day, w, shift);
		}
		store.update(this, "week", w);
		week = w;
	}

	/** Get the week-of-month */
	public int getWeek() { return week; }

	/** Day of week */
	protected int weekday;

	/** Set the day-of-week */
	public void setWeekday(int d) throws TMSException {
		if(d == weekday) return;
		if(d != ANY_WEEKDAY) {
			if(d < Calendar.SUNDAY || d > Calendar.SATURDAY) throw
				new ChangeVetoException("Invalid weekday:" + d);
		}
		store.update(this, "weekday", d);
		weekday = d;
	}

	/** Get the day-of-week */
	public int getWeekday() { return weekday; }

	/** Shift (in days) from actual "holiday" (for Shopping day, etc) */
	protected int shift;

	/** Set the shift from the actual holiday */
	public void setShift(int s) throws TMSException {
		if(s == shift) return;
		if(s < -6 || s > 6)
			throw new ChangeVetoException("Invalid shift:" + s);
		if(s != 0) checkSelection(day, week, s);
		store.update(this, "shift", s);
		shift = s;
	}

	/** Get the shift from the actual holiday */
	public int getShift() { return shift; }

	/** Period (AM/PM) */
	protected int period;

	/** Set the period */
	public void setPeriod(int p) throws TMSException {
		if(p == period) return;
		if(p != ANY_PERIOD) {
			if(p != Calendar.AM && p != Calendar.PM) throw new
				ChangeVetoException("Invalid period:" + p);
		}
		store.update(this, "period", p);
		period = p;
	}

	/** Set the period */
	public int getPeriod() { return period; }

	/** Check if the holiday matches the given time */
	public boolean matches(Calendar stamp) {
		if(month != ANY_MONTH && month != stamp.get(Calendar.MONTH))
			return false;
		if(day != ANY_DAY && day != stamp.get(Calendar.DAY_OF_MONTH))
			return false;
		if(week != ANY_WEEK) {
			Calendar t = (Calendar)stamp.clone();
			t.set(Calendar.DAY_OF_WEEK, weekday);
			t.set(Calendar.DAY_OF_WEEK_IN_MONTH, week);
			t.add(Calendar.DAY_OF_MONTH, shift);
			if(!t.equals(stamp)) return false;
		} else if(weekday != ANY_WEEKDAY) {
			if(weekday != stamp.get(Calendar.DAY_OF_WEEK))
				return false;
		}
		if(period != ANY_PERIOD && period != stamp.get(Calendar.AM_PM))
			return false;
		return true;
	}
}
