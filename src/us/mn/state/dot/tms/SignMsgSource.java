/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2015-2016  Minnesota Department of Transportation
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
 * Sign message source enumeration.  The ordinal values correspond to the
 * records in the iris.sign_msg_source look-up table.
 *
 * @author Douglas Lau
 */
public enum SignMsgSource {
	operator,	// 0  IRIS operator
	schedule,	// 1  scheduled DMS action
	tolling,	// 2  DMS action with [tz...] tag
	gate_arm,	// 3  gate arm system
	lcs,		// 4  lane-use control signal
	external;	// 5  external system

	/** Get a sign message source from an ordinal value */
	static public SignMsgSource fromOrdinal(int o) {
		if (o >= 0 && o < values().length)
			return values()[o];
		else
			return null;
	}

	/** Check if a sign message source is scheduled */
	static public boolean isScheduled(int o) {
		SignMsgSource src = fromOrdinal(o);
		switch (src) {
		case schedule:
		case tolling:
			return true;
		default:
			return false;
		}
	}
}
