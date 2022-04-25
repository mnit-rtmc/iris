/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2022  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.ntcip.mib1204;

/**
 * Cloud situation as defined by essCloudSituation in NTCIP 1204
 *
 * @author Douglas Lau
 */
public enum CloudSituation {
	undefined,    // 0
	overcast,     // 1 (100% cloud cover)
	cloudy,       // 2 (62.5% - 99% cover)
	partlyCloudy, // 3 (37.5% - 62.5% cover)
	mostlyClear,  // 4 (1% - 37.5% cover)
	clear;        // 5 (0% cover)
}
