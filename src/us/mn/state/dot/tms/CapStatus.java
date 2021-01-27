/*
 * IRIS -- Intelligent Roadway Information System
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
 * Common Alerting Protocol (CAP) status enum.
 *
 * The ordinal values correspond to the records in the cap.status look-up table.
 *
 * @author Douglas Lau
 */
public enum CapStatus {
	UNKNOWN,
	ACTUAL,
	EXERCISE,
	SYSTEM,
	TEST,
	DRAFT;

	/** Values array */
	static private final CapStatus[] VALUES = values();

	/** Get a CapStatus from an ordinal value */
	static public CapStatus fromOrdinal(int o) {
		return (o >= 0 && o < VALUES.length) ? VALUES[o] : null;
	}

	/** Get the CapStatus from the value provided */
	static public CapStatus fromValue(String v) {
		for (CapStatus e: VALUES) {
			if (e.name().equalsIgnoreCase(v))
				return e;
		}
		return UNKNOWN;
	}
}
