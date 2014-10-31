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
 * A meter rate controls whether a ramp meter is metering or flashing.  There
 * are 6 metering rates and 2 flashing rates.
 *
 * @author Douglas Lau
 */
public class MeterRate {

	/** Flash (non-metering) rate */
	static public final int OFF = 0;

	/** Central mode metering rate */
	static public final int CENTRAL = 1;

	/** TOD mode metering rate */
	static public final int TOD = 2;

	/** Rate 3 */
	static public final int RATE_3 = 3;

	/** Rate 4 */
	static public final int RATE_4 = 4;

	/** Rate 5 */
	static public final int RATE_5 = 5;

	/** Rate 6 */
	static public final int RATE_6 = 6;

	/** Forced flash (metering disabled) rate */
	static public final int FORCED_FLASH = 7;

	/** Check if a given rate is valid */
	static public boolean isValid(int r) {
		return r >= OFF && r <= FORCED_FLASH;
	}

	/** Check if a given rate is metering */
	static public boolean isMetering(int r) {
		return r > OFF && r < FORCED_FLASH;
	}
}
