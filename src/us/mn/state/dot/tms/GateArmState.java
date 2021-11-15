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
 * Gate Arm state enumeration.  These states are used for gate arm arrays as
 * well as individual gate arms.  The WARN_CLOSE state only applies to gate
 * arm arrays.
 *
 * @author Douglas Lau
 */
public enum GateArmState {

	/** Gate Arm states */
	UNKNOWN,    /* no communication */
	FAULT,      /* fault in gate operation */
	OPENING,    /* open in progress */
	OPEN,       /* gate open, open msg on DMS */
	WARN_CLOSE, /* gate open, closed msg on DMS */
	CLOSING,    /* close in progress */
	CLOSED,     /* gate closed */
	TIMEOUT;    /* comm. timeout */

	/** Get gate arm state from an ordinal value */
	static public GateArmState fromOrdinal(int o) {
		return (o >= 0 && o < values().length) ? values()[o] : UNKNOWN;
	}

	/** Check if operator can request OPENING */
	public boolean canRequestOpening() {
		switch (this) {
		case WARN_CLOSE:
		case CLOSED:
			return true;
		default:
			return false;
		}
	}

	/** Check if operator can request WARN_CLOSE */
	public boolean canRequestWarnClose() {
		switch (this) {
		case OPEN:
			return true;
		default:
			return false;
		}
	}

	/** Check if operator can request CLOSING */
	public boolean canRequestClosing() {
		switch (this) {
		case FAULT:
		case WARN_CLOSE:
			return true;
		default:
			return false;
		}
	}
}
