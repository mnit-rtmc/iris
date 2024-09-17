/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2024  Minnesota Department of Transportation
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
 * An enumeration of all permission access levels.  The ordinal values
 * correspond to the records in the iris.access_level look-up table.
 *
 * @author Douglas Lau
 */
public enum AccessLevel {

	/** None (0) */
	NONE,

	/** View (1) */
	VIEW,

	/** Operate (2) */
	OPERATE,

	/** Manage (3) */
	MANAGE,

	/** Configure (4) */
	CONFIGURE;

	/** Get an access level from an ordinal value */
	static public AccessLevel fromOrdinal(int o) {
		return (o >= 0 && o < values().length) ? values()[o] : NONE;
	}

	/** Array of valid access levels */
	static public AccessLevel[] VALID_VALUES = {
		VIEW, OPERATE, MANAGE, CONFIGURE
	};
}
