/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2003  Minnesota Department of Transportation
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
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package us.mn.state.dot.tms;

import java.rmi.RemoteException;
import java.util.Calendar;

/**
 * MeteringHoliday
 *
 * @author Douglas Lau
 */
public interface MeteringHoliday extends TMSObject {

	/** Get the holiday name */
	public String getName() throws RemoteException;

	/** Constant for holidays not determined by month */
	public int ANY_MONTH = -1;

	/** Set the month */
	public void setMonth(int m) throws TMSException, RemoteException;

	/** Get the month */
	public int getMonth() throws RemoteException;

	/** Constant for holidays not determined by day-of-month */
	public int ANY_DAY = 0;

	/** Set the day-of-month */
	public void setDay(int d) throws TMSException, RemoteException;

	/** Get the day-of-month */
	public int getDay() throws RemoteException;

	/** Constant for holidays not determined by week-of-month */
	public int ANY_WEEK = 0;

	/** Set the week-of-month */
	public void setWeek(int w) throws TMSException, RemoteException;

	/** Get the week-of-month */
	public int getWeek() throws RemoteException;

	/** Constant for holidays not determined by day-of-week */
	public int ANY_WEEKDAY = 0;

	/** Set the day-of-week */
	public void setWeekday(int d) throws TMSException, RemoteException;

	/** Get the day-of-week */
	public int getWeekday() throws RemoteException;

	/** Set the shift from the actual holiday */
	public void setShift(int s) throws TMSException, RemoteException;

	/** Get the shift from the actual holiday */
	public int getShift() throws RemoteException;

	/** Constant for holidays not determined by period */
	public int ANY_PERIOD = -1;

	/** Set the period */
	public void setPeriod(int p) throws TMSException, RemoteException;

	/** Set the period */
	public int getPeriod() throws RemoteException;

	/** Check if the holiday matches the given time */
	public boolean matches(Calendar stamp) throws RemoteException;
}
