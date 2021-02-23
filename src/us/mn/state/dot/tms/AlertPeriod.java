/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2021  Minnesota Department of Transportation
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
 * Alert Periods
 *
 * The ordinal values correspond to the records in the iris.alert_period look-up
 * table.
 *
 * @author Douglas Lau
 */
public enum AlertPeriod {
	BEFORE,    // 0  Period before alert start time
	DURING,    // 1  Period during start to end time interval
	AFTER;     // 2  Period after end time

	/** Values array */
	static public final AlertPeriod[] VALUES = values();

	/** Get a AlertPeriod from an ordinal value */
	static public AlertPeriod fromOrdinal(int o) {
		return (o >= 0 && o < VALUES.length) ? VALUES[o] : null;
	}
}
