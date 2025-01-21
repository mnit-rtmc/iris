/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2025  Minnesota Department of Transportation
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
 * Ramp meter fault.  The ordinal values correspond to the records in the
 * iris.meter_fault look-up table.
 *
 * @author Douglas Lau
 */
public enum RampMeterFault {
	POLICE_PANEL,      /* 0 */
	MANUAL_MODE,       /* 1 */
	NO_ENTRANCE_NODE,  /* 2 */
	NO_GREEN_DETECTOR; /* 3; FIXME: remove this? */

	/** Get a ramp meter fault from an ordinal value */
	static public RampMeterFault fromOrdinal(Integer o) {
		return (o != null && o >= 0 && o < values().length)
		      ? values()[o]
		      : null;
	}
}
