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
 * Pavement sensor errors as defined by essPavementSensorError in NTCIP 1204.
 *
 * @author Michael Darter
 * @author Douglas Lau
 */
public enum EssPavementSensorError {
	undefined,    // 0
	other,        // 1
	none,         // 2
	noResponse,   // 3
	cutCable,     // 4
	shortCircuit, // 5
	dirtyLens;    // 6

	/** Is there an error? */
	public boolean isError() {
		switch (this) {
			case other:
			case noResponse:
			case cutCable:
			case shortCircuit:
			case dirtyLens:
				return true;
			default:
				return false;
		}
	}
}
