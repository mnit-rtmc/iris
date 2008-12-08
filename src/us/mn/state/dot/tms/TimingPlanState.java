/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008  Minnesota Department of Transportation
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

import java.util.Calendar;

/**
 * Timing plan state
 *
 * @author Douglas Lau
 */
public class TimingPlanState {

	/** Calendar instance for calculating the minute of day */
	static protected final Calendar STAMP = Calendar.getInstance();

	/** Get the current minute of the day */
	static protected int minute_of_day() {
		synchronized(STAMP) {
			STAMP.setTimeInMillis(System.currentTimeMillis());
			return STAMP.get(Calendar.HOUR_OF_DAY) * 60 +
				STAMP.get(Calendar.MINUTE);
		}
	}

	/** Get the current second of the day */
	static protected int second_of_day() {
		synchronized(STAMP) {
			STAMP.setTimeInMillis(System.currentTimeMillis());
			return STAMP.get(Calendar.HOUR_OF_DAY) * Interval.HOUR +
			       STAMP.get(Calendar.MINUTE) * Interval.MINUTE +
			       STAMP.get(Calendar.SECOND);
		}
	}

	/** Create a 4 character time stamp.
	 * @param min Minute of the day (0-1440)
	 * @return 4 character time stamp (1330 for 1:30 PM) */
	static protected String stamp_hhmm(int min) {
		StringBuilder b = new StringBuilder();
		b.append(min / 60);
		while(b.length() < 2)
			b.insert(0, '0');
		b.append(min % 60);
		while(b.length() < 4)
			b.insert(2, '0');
		return b.toString();
	}

	/** Get a stamp of the current 30 second interval */
	static protected String stamp_30() {
		int i30 = second_of_day() / 30 + 1;
		StringBuilder b = new StringBuilder();
		b.append(i30 / 120);
		while(b.length() < 2)
			b.insert(0, '0');
		b.append(':');
		b.append((i30 % 120) / 2);
		while(b.length() < 5)
			b.insert(3, '0');
		b.append(':');
		b.append((i30 % 2) * 30);
		while(b.length() < 8)
			b.insert(6, '0');
		return b.toString();
	}

	/** Timing plan */
	protected final TimingPlanImpl plan;

	/** Get the timing plan */
	public TimingPlanImpl getPlan() {
		return plan;
	}

	/** Create a new timing plan state */
	public TimingPlanState(TimingPlanImpl p) {
		plan = p;
		operating = false;
	}

	/** Flag to determine if timing plan is operating */
	protected boolean operating;

	/** Check if the timing plan is operating */
	public boolean isOperating() {
		return operating;
	}

	/** Validate the timing plan */
	public void validate() {
		if(shouldOperate()) {
			if(!isOperating())
				start();
		} else if(isOperating())
			stop();
	}

	/** Start operating the timing plan */
	protected void start() {
		operating = true;
	}

	/** Stop operating the timing plan */
	protected void stop() {
		operating = false;
	}

	/** Check if the timing plan should be operating */
	protected boolean shouldOperate() {
		return plan.getActive() && isWithin();
	}

	/** Check if the current time is within the timing plan window */
	public boolean isWithin() {
		int m = minute_of_day();
		return m >= plan.getStartMin() && m < plan.getStopMin();
	}
}
