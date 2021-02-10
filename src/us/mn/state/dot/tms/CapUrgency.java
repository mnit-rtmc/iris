/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2020  SRF Consulting Group, Inc.
 * Copyright (C) 2021  Minnesota Department of Transportation
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
 * Common Alerting Protocol (CAP) urgency enum.
 *
 * Used for alert processing for generating messages for posting to DMS.
 * Values are taken from the OASIS CAP Standard v1.2.  Values are ordered from
 * least (Unknown/Past) to most (Immediate) emphatic
 * The ordinal values correspond to the records in the cap.urgency look-up
 * table.
 *
 * @author Gordon Parikh
 * @author Douglas Lau
 */
public enum CapUrgency {
	UNKNOWN,
	PAST,
	FUTURE,
	EXPECTED,
	IMMEDIATE;

	/** Values array */
	static private final CapUrgency[] VALUES = values();

	/** Get a CapUrgency from an ordinal value */
	static public CapUrgency fromOrdinal(int o) {
		return (o >= 0 && o < VALUES.length) ? VALUES[o] : null;
	}

	/** Get the CapUrgency from the value provided */
	static public CapUrgency fromValue(String v) {
		for (CapUrgency e: VALUES) {
			if (e.name().equalsIgnoreCase(v))
				return e;
		}
		return UNKNOWN;
	}
}
