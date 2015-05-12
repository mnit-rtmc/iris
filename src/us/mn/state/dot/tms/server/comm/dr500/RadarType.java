/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2015  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.dr500;

/**
 * Enumeration of radar types from Houston Radar.
 *
 * @author Douglas Lau
 */
public enum RadarType {
	DR500,		/* 0 */
	DR1500,		/* 1 */
	DR2500,		/* 2 */
	SS300,		/* 3 */
	SS300U,		/* 4 */
	PD300,		/* 5 */
	PD310,		/* 6 */
	DC310,		/* 7 */
	SPEEDLANE,	/* 8 */
	UNKNOWN;	/* 9 */

	/** Lookup a radar type from ordinal */
	static public RadarType fromOrdinal(int o) {
		for (RadarType rt: values()) {
			if (rt.ordinal() == o)
				return rt;
		}
		return UNKNOWN;
	}

	/** Lookup a radar type from name */
	static public RadarType fromName(String n) {
		RadarType rt = UNKNOWN;
		for (RadarType r: values()) {
			// Ignore appended platform version
			if (n.startsWith(r.toString()))
				rt = r;
		}
		return rt;
	}
}
