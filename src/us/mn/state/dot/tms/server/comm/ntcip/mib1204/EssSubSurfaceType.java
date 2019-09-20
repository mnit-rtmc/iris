/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2019  Minnesota Department of Transportation
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
 * Sub-surface type as defined by NTCIP 1204 essSubSurfaceType.
 *
 * @author Douglas Lau
 */
public enum EssSubSurfaceType {
	undefined,         // 0
	other,             // 1
	unknown,           // 2
	concrete,          // 3
	asphalt,           // 4
	openGradedAsphalt, // 5
	gravel,            // 6
	clay,              // 7
	loam,              // 8
	sand,              // 9
	permafrost,        // 10
	variousAggregates, // 11
	air;               // 12
}
