/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2021-2022  Iteris Inc.
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
package us.mn.state.dot.tms.server.comm.clearguide;

/**
 * ClearGuide [cg] tag mode values
 *
 * @author Michael Darter
 */
public enum ModeEnum {

	/** Possible modes */
	UNKNOWN(""),
	DELAY("delay"),                 // work zone delay (mins)
	TRAVELTIME("tt"),               // TT adjusted w/ speed limit TT (mins)
	TRAVELTIME_ACTUAL("tta"),       // actual TT
	TRAVELTIME_SPEED_LIMIT("ttsl"), // TT at the speed limit
	SPEED("sp"),                    // speed
	SPEED_CONDITION("sp_cond");     // speed condition

	/** Name of mode */
	private final String name;

	/** Constructor */
	private ModeEnum(String mname) {
		name = mname;
	}

	/** Reverse lookup */
	static protected ModeEnum fromValue(String mname) {
		for (ModeEnum me: values()) {
			if (me.name.equals(mname))
				return me;
		}
		return UNKNOWN;
	}
}
