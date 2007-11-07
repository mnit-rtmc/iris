/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2007  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.comm.mndot;

/**
 * TimingTable
 *
 * @author Douglas Lau
 */
public interface TimingTable {

	/** Startup green time (tenths of a second) */
	int STARTUP_GREEN = 80;

	/** Startup yellow time (tenths of a second) */
	int STARTUP_YELLOW = 50;

	/** HOV preempt time (tenths of a second) (obsolete) */
	int HOV_PREEMPT = 80;

	/** Default AM start time (minute of day) */
	int AM_START_TIME = 630;

	/** Default AM stop time (minute of day) */
	int AM_STOP_TIME = 830;

	/** Default PM start time (minute of day) */
	int PM_START_TIME = 1530;

	/** Default PM stop time (minute of day) */
	int PM_STOP_TIME = 1730;
}
