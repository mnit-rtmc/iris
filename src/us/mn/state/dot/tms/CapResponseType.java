/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2021  Minnesota Department of Transportation
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
 * Common Alerting Protocol (CAP) response type enum.
 *
 * The ordinal values correspond to the records in the cap.response_type look-up
 * table.
 *
 * @author Douglas Lau
 * @author Gordon Parikh
 */
public enum CapResponseType {
	SHELTER,
	EVACUATE,
	PREPARE,
	EXECUTE,
	AVOID,
	MONITOR,
	ASSESS, // NOTE: not to be used for public applications
	ALLCLEAR,
	NONE;

	/** Values array */
	static private final CapResponseType[] VALUES = values();

	/** Get a CapResponseType from an ordinal value */
	static public CapResponseType fromOrdinal(int o) {
		return (o >= 0 && o < VALUES.length) ? VALUES[o] : null;
	}

	/** Get the CapResponseType from the value provided */
	static public CapResponseType fromValue(String v) {
		for (CapResponseType e: VALUES) {
			if (e.name().equalsIgnoreCase(v))
				return e;
		}
		return NONE;
	}
}
