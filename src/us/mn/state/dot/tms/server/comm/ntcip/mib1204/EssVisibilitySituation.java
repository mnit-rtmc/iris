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
 * Visibility situation as defined by essVisibilitySituation in NTCIP 1204.
 *
 * @author Douglas Lau
 */
public enum EssVisibilitySituation {
	undefined,         // 0
	other,             // 1
	unknown,           // 2
	clear,             // 3
	fogNotPatchy,      // 4
	patchyFog,         // 5
	blowingSnow,       // 6
	smoke,             // 7
	seaSpray,          // 8
	vehicleSpray,      // 9
	blowingDustOrSand, // 10
	sunGlare,          // 11
	swarmOfInsects;    // 12
}
