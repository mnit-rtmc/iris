/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2013-2016  Minnesota Department of Transportation
 * Copyright (C) 2015-2021  SRF Consulting Group
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
 * @author John L. Stanley - SRF Consulting
 */
public enum GateArmState {

	/** Gate Arm states */
	UNKNOWN,	/* initial unknown	no change allowed */
	FAULT,		/* open / close fault	user: CLOSING */
	OPENING,	/* open in progress	system: OPEN or FAULT */
	OPEN,		/* gate open		user: WARN_CLOSE */
	WARN_CLOSE,	/* gate open, DMS warn	user: CLOSING or OPENING */
	CLOSING,	/* close in progress	system: CLOSED or FAULT */
	CLOSED,		/* gate closed		user: OPENING */
	TIMEOUT, 	/* comm. timeout	no change allowed */

	// Additional Nebraska gate-arm states
	BEACON_ON,      /* gate-arm & beacon lights on, waiting
	                   for  gate-controlled pre-close delay */
	STILL_CLOSED,   /* Never moved from closed */
	OPENING_FAIL,   /* Failed moving from closed to open */
	STILL_OPEN,     /* Never moved from open */
	CLOSING_FAIL,   /* Failed moving from open to closed */
	NOT_CONFIGURED, /* Requested gate-arm is not configured */
	ARM_LIGHT_ERROR,/* Gate arm lights are not working correctly */
	SIGN_ERROR;     /* Gate sign is not working correctly */

	/** Static array of GateArmState values */
	private static final GateArmState[] VALUES = values();

	/** Get gate arm state from an ordinal value */
	static public GateArmState fromOrdinal(int o) {
		return ((o >= 0) && (o < VALUES.length)) ? VALUES[o] : UNKNOWN;
	}
	
	/** Test for operation fault */
	public boolean isFault() {
		switch (this) {
			case FAULT:
			case STILL_CLOSED:
			case OPENING_FAIL:
			case STILL_OPEN:
			case CLOSING_FAIL:
			case NOT_CONFIGURED:
			case ARM_LIGHT_ERROR:
			case SIGN_ERROR:
				return true;
		}
		return false;
	}

	public boolean isMoving() {
		switch (this) {
			case OPENING:
			case BEACON_ON:
			case CLOSING:
				return true;
		}
		return false;
	}

	public boolean isDone() {
		switch (this) {
			case OPEN:
			case CLOSED:
				return true;
		}
		return false;
	}
}
