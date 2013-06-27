/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2013  Minnesota Department of Transportation
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
 * Gate Arm interlock enumeration.
 *
 * @author Douglas Lau
 */
public enum GateArmInterlock {

	/** Gate Arm interlock states */
	NONE,			/* no interlock active */
	DENY_OPEN,		/* open disallowed */
	DENY_CLOSE,		/* close disallowed */
	DENY_ALL,		/* open and close disallowed */
	SYSTEM_DISABLE;		/* system disable */

	/** Get gate arm interlock from an ordinal value */
	static public GateArmInterlock fromOrdinal(int o) {
		for(GateArmInterlock gai: GateArmInterlock.values()) {
			if(gai.ordinal() == o)
				return gai;
		}
		return null;
	}
}
