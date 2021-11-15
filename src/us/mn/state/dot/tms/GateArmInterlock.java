/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2013-2021  Minnesota Department of Transportation
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
	NONE,           /* open and close allowed */
	DENY_OPEN,      /* open disallowed / close allowed */
	DENY_CLOSE,     /* close disallowed / open allowed */
	DENY_ALL,       /* open and close disallowed */
	SYSTEM_DISABLE; /* system disable */

	/** Get gate arm interlock from an ordinal value */
	static public GateArmInterlock fromOrdinal(int o) {
		return (o >= 0 && o < values().length) ? values()[o] : null;
	}

	/** Check if gate arm open is allowed */
	public boolean isOpenAllowed() {
		switch (this) {
		case NONE:
		case DENY_CLOSE:
			return true;
		default:
			return false;
		}
	}

	/** Check if gate arm close is allowed */
	public boolean isCloseAllowed() {
		switch (this) {
		case NONE:
		case DENY_OPEN:
			return true;
		default:
			return false;
		}
	}
}
