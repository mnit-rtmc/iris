/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2016  Minnesota Department of Transportation
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

/**
 * DMS type enumeration. Note: these values and ordering are taken from
 * NTCIP 1203.
 *
 * @author Michael Darter
 * @author Douglas Lau
 */
public enum DMSType {

	/** Unknown sign type */
	UNKNOWN("???"),

	/** Other sign type */
	OTHER("Other"),

	/** Blank out sign */
	BOS("BOS (blank-out sign)"),

	/** CMS (Changeable message sign) */
	CMS("CMS (changeable message sign)"),

	/** VMS Character-matrix */
	VMS_CHAR("VMS Character-matrix"),

	/** VMS Line-matrix */
	VMS_LINE("VMS Line-matrix"),

	/** VMS Full-matrix */
	VMS_FULL("VMS Full-matrix");

	/** Description string */
	public final String description;

	/** Create a new DMS type */
	private DMSType(String d) {
		description = d;
	}

	/** Get a DMS type from an ordinal value */
	static public DMSType fromOrdinal(int o) {
		if (o >= 0 && o < values().length)
			return values()[o];
		else
			return UNKNOWN;
	}

	/** Check if a DMS type is fixed-height VMS */
	static public boolean isFixedHeight(DMSType t) {
		return t == VMS_CHAR || t == VMS_LINE;
	}
}
