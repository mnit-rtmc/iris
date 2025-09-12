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
 * The ordinal values correspond to the records in the iris.gate_arm_interlock
 * look-up table.
 *
 * @author Douglas Lau
 */
public enum GateArmInterlock {

	/** Gate Arm interlock states */
	NONE,           /* 0: open and close allowed */
	DENY_OPEN,      /* 1: open disallowed / close allowed */
	DENY_CLOSE,     /* 2: close disallowed / open allowed */
	DENY_ALL,       /* 3: open and close disallowed */
	SYSTEM_DISABLE; /* 4: system disable */

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

	/** Check if gate arm open is denied */
	public boolean isOpenDenied() {
		switch (this) {
		case DENY_OPEN:
		case DENY_ALL:
			return true;
		default:
			// NOTE: For SYSTEM_DISABLE, open is not denied
			//       to allow manual front panel control
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

	/** Check if gate arm close is denied */
	public boolean isCloseDenied() {
		switch (this) {
		case DENY_CLOSE:
		case DENY_ALL:
			return true;
		default:
			return false;
		}
	}
}
