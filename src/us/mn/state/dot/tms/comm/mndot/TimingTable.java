/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2009  Minnesota Department of Transportation
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

	/** AM midpoint time (BCD; minute of day) */
	int AM_MID_TIME = 730;

	/** PM midpoint time (BCD; minute of day) */
	int PM_MID_TIME = 1630;
}
