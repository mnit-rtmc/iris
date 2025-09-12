/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2013-2025  Minnesota Department of Transportation
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
 * Gate Arm state enumeration.
 *
 * The ordinal values correspond to the records in the iris.gate_arm_state
 * look-up table.
 *
 * @author Douglas Lau
 */
public enum GateArmState {

	/** Gate Arm states */
	UNKNOWN,    /* 0: no communication */
	FAULT,      /* 1: fault in gate operation */
	OPENING,    /* 2: open in progress */
	OPEN,       /* 3: gate open, open msg on DMS */
	WARN_CLOSE, /* 4: -- obsolete -- */
	CLOSING,    /* 5: close in progress */
	CLOSED;     /* 6: gate closed */

	/** Get gate arm state from an ordinal value */
	static public GateArmState fromOrdinal(int o) {
		return (o >= 0 && o < values().length) ? values()[o] : UNKNOWN;
	}

	/** Check if operator can request OPENING */
	public boolean canRequestOpening() {
		switch (this) {
		case CLOSED:
		case FAULT:
			return true;
		default:
			return false;
		}
	}

	/** Check if operator can request WARN_CLOSE */
	public boolean canRequestWarnClose(boolean has_signs) {
		switch (this) {
		case OPEN:
			return has_signs;
		default:
			return false;
		}
	}

	/** Check if operator can request CLOSING */
	public boolean canRequestClosing(boolean has_signs) {
		switch (this) {
		case FAULT:
			return true;
		case OPEN:
			// only allow OPEN -> CLOSING if there are no signs
			return !has_signs;
		default:
			return false;
		}
	}
}
