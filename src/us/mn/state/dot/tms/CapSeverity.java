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
 * Common Alerting Protocol (CAP) severity field value enum.
 *
 * Used for IPAWS alert processing for generating messages for posting to DMS.
 * Values are taken from the OASIS CAP Standard v1.2.  Values are ordered from
 * least (Unknown/Minor) to most (Extreme) emphatic.
 * The ordinal values correspond to the records in the cap.severity look-up
 * table.
 *
 * @author Gordon Parikh
 * @author Douglas Lau
 */
public enum CapSeverity {
	UNKNOWN,
	MINOR,
	MODERATE,
	SEVERE,
	EXTREME;

	/** Values array */
	static private final CapSeverity[] VALUES = values();

	/** Get a CapSeverity from an ordinal value */
	static public CapSeverity fromOrdinal(int o) {
		return (o >= 0 && o < VALUES.length) ? VALUES[o] : null;
	}

	/** Get the CapSeverity from the value provided */
	static public CapSeverity fromValue(String v) {
		for (CapSeverity e: VALUES) {
			if (e.name().equalsIgnoreCase(v))
				return e;
		}
		return UNKNOWN;
	}
}
