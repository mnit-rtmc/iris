/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2015  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.ntcip.mib1203;

/**
 * Enumeration of sign technology flags.
 *
 * @author Douglas Lau
 * @author John L. Stanley
 */
public enum DmsSignTechnology {
	OTHER,
	LED,
	FLIP_DISK,
	FIBER_OPTIC,
	SHUTTERED,
	LAMP,
	DRUM;

	/** Get the bit flag */
	public int bit() {
		return 1 << ordinal();
	}

	/** Test if the flag is set */
	public boolean isSet(int v) {
		return (v & bit()) != 0;
	}
}
