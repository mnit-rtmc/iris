/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2020  SRF Consulting Group, Inc.
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
 * Common Alerting Protocol (CAP) certainty field value enum.
 *
 * Used for IPAWS alert processing for generating messages for posting to DMS.
 * Values are taken from the OASIS CAP Standard v1.2.  Values are ordered from
 * least (Unknown/Unlikely) to most (Observed) emphatic.
 * The ordinal values correspond to the records in the cap.certainty look-up
 * table.
 *
 * @author Gordon Parikh
 * @author Douglas Lau
 */
public enum CapCertainty {
	UNKNOWN,
	UNLIKELY,
	POSSIBLE,
	LIKELY,
	OBSERVED;

	/** Values array */
	static private final CapCertainty[] VALUES = values();

	/** Get a CapCertainty from an ordinal value */
	static public CapCertainty fromOrdinal(int o) {
		return (o >= 0 && o < VALUES.length) ? VALUES[o] : null;
	}

	/** Get the CapCertainty from the value provided */
	static public CapCertainty fromValue(String v) {
		for (CapCertainty e: VALUES) {
			if (e.name().equalsIgnoreCase(v))
				return e;
		}
		return UNKNOWN;
	}
}
