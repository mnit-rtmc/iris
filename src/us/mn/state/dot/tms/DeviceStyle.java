/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2012  Minnesota Department of Transportation
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

import java.util.HashMap;
import us.mn.state.dot.tms.utils.I18N;

/**
 * Device style enumeration.
 *
 * @author Douglas Lau
 */
public enum DeviceStyle {
	ACTIVE,
	ALL,
	AVAILABLE,
	AWS_CONTROLLED,	// dms
	AWS_DEPLOYED,	// dms
	CLEARED,	// incident
	CRASH,		// incident
	DEPLOYED,
	DMS,		// plan
	FAILED,
	GPS,		// r_node
	HAZARD,		// incident
	INACTIVE,
	LANE,		// plan
	LOCKED,		// meter
	MAINTENANCE,
	METER,		// plan
	METERING,	// meter
	NO_CONTROLLER,
	NO_LOC,		// r_node
	PLAYLIST,	// camera
	QUEUE_EXISTS,	// meter
	QUEUE_FULL,	// meter
	ROADWORK,	// incident
	SCHEDULED,	// dms
	STALL,		// incident
	TIME,		// plan
	TRAVEL_TIME,	// dms
	UNPUBLISHED;	// camera

	/** Get a string representation of the device style */
	public String toString() {
		return I18N.get("device.style." +
			name().toLowerCase().replace('_', '.'));
	}

	/** Hash map of all styles */
	static private final HashMap<String, DeviceStyle> ALL_STYLES =
		new HashMap<String, DeviceStyle>();

	/** Initialize hash map of all styles */
	static {
		for(DeviceStyle ds: DeviceStyle.values())
			ALL_STYLES.put(ds.toString(), ds);
	}

	/** Lookup a device style from a string description */
	static public DeviceStyle getStyle(String style) {
		return ALL_STYLES.get(style);
	}
}
