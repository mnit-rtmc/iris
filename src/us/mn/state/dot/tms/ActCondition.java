/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2026  Minnesota Department of Transportation
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
 * Action condition enumeration.   The ordinal values correspond to the
 * records in the iris.action_condition look-up table.
 *
 * @author Douglas Lau
 */
public enum ActCondition {
	HOLD_TIME,         // 0
	CLOCK_TIME,        // 1
	TRAFFIC_THRESHOLD, // 2
	RWIS_THRESHOLD,    // 3
	TOLL_MODE,         // 4
	ALERT_PERIOD;      // 5

	/** Get an action condition from an ordinal value */
	static public ActCondition fromOrdinal(int o) {
		if (o >= 0 && o < values().length)
			return values()[o];
		else
			return HOLD_TIME;
	}

	/** Get values with null as first */
	static public ActCondition[] values_with_null() {
		return new ActCondition[] {
			null,
			HOLD_TIME,
			CLOCK_TIME,
			TRAFFIC_THRESHOLD,
			RWIS_THRESHOLD,
			TOLL_MODE,
			ALERT_PERIOD,
		};
	}
}
