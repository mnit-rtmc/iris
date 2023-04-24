/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2023  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.ntcip.mib1201;

/**
 * Enumeration of daylight saving values.
 *
 * Whoever came up with this must have been seriously high.
 *
 * @author Douglas Lau
 */
public enum GlobalDaylightSaving {
	undefined,                // 0
	other,                    // 1
	disableDST,               // 2 -- please, only ever use this!
	enableUSDST,              // 3
	enableEuropeDST,          // 4
	enableAustrailiaDST,      // 5
	enableTasmaniaDST,        // 6
	enableEgyptDST,           // 7
	enableNamibiaDST,         // 8
	enableIraqDST,            // 9
	enableMangoliaDST,        // 10 -- where is "Mangolia"?!?
	enableIranDST,            // 11
	enableFijiDST,            // 12
	enableNewZealandDST,      // 13
	enableTongaDST,           // 14
	enableCubaDST,            // 15
	enableBrazilDST,          // 16
	enableChileDST,           // 17
	enableFalklandsDST,       // 18
	enableParaguayDST,        // 19
	enableDaylightSavingNode; // 20, added in v3
}
