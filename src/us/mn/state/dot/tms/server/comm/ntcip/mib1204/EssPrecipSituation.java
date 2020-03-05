/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2017  Iteris Inc.
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
 * Precipitation situation as defined by essPrecipSituation in NTCIP 1204.
 *
 * @author Michael Darter
 * @author Douglas Lau
 */
public enum EssPrecipSituation {
	undefined,                   // 0
	other,                       // 1
	unknown,                     // 2
	noPrecipitation,             // 3
	unidentifiedSlight,          // 4
	unidentifiedModerate,        // 5
	unidentifiedHeavy,           // 6
	snowSlight,                  // 7
	snowModerate,                // 8
	snowHeavy,                   // 9
	rainSlight,                  // 10
	rainModerate,                // 11
	rainHeavy,                   // 12
	frozenPrecipitationSlight,   // 13
	frozenPrecipitationModerate, // 14
	frozenPrecipitationHeavy;    // 15
}
