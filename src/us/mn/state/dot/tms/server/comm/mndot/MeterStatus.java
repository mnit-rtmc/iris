/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2005-2014  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.mndot;

/**
 * Meter status enumeration
 *
 * @author Douglas Lau
 */
public class MeterStatus {

	/** Flash metering status from 170 */
	static public final int FLASH = 0;

	/** Manual metering status from 170 */
	static public final int MANUAL = 1;

	/** Central (remote) metering status from 170 */
	static public final int CENTRAL = 2;

	/** Time-of-day metering status from 170 */
	static public final int TOD = 3;

	/** Check if the status code is valid */
	static public boolean isValid(int s) {
		return s >= FLASH && s <= TOD;
	}

	/** Check if the status is metering */
	static public boolean isMetering(int s) {
		return s == MANUAL || s == CENTRAL || s == TOD;
	}

	/** Check if the status is manual mode */
	static public boolean isManual(int s) {
		return s == MANUAL;
	}

	/** Disallow instantiation */
	private MeterStatus() { }
}
