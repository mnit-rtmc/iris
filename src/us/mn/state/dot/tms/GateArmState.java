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
 * Gate Arm state enumeration.  These states are used for gate arm arrays as
 * well as individual gate arms.  The WARN_CLOSE state only applies to gate
 * arm arrays.
 *
 * @author Douglas Lau
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
	TIMEOUT;	/* comm. timeout	no change allowed */

	/** Get gate arm state from an ordinal value */
	static public GateArmState fromOrdinal(int o) {
		for(GateArmState gas: GateArmState.values()) {
			if(gas.ordinal() == o)
				return gas;
		}
		return UNKNOWN;
	}
}
