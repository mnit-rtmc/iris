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
 * Wind situation as defined by essWindSituation in NTCIP 1204v1 and
 * windSensorSituation in 1204v2+.
 *
 * @author Douglas Lau
 */
public enum WindSituation {
	undefined,           // 0
	other,               // 1
	unknown,             // 2
	calm,                // 3
	lightBreeze,         // 4
	moderateBreeze,      // 5
	strongBreeze,        // 6
	gale,                // 7
	moderateGale,        // 8
	strongGale,          // 9
	stormWinds,          // 10
	hurricaneForceWinds, // 11
	gustyWinds;          // 12
}
