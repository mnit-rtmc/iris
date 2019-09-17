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
 * Pavement surface status as defined by essSurfaceStatus in NTCIP 1204.
 *
 * @author Michael Darter
 * @author Douglas Lau
 */
public enum EssSurfaceStatus {
	undefined,            // 0
	other,                // 1
	error,                // 2
	dry,                  // 3
	traceMoisture,        // 4
	wet,                  // 5
	chemicallyWet,        // 6
	iceWarning,           // 7
	iceWatch,             // 8
	snowWarning,          // 9
	snowWatch,            // 10
	absorption,           // 11
	dew,                  // 12
	frost,                // 13
	absorptionAtDewpoint; // 14
}
