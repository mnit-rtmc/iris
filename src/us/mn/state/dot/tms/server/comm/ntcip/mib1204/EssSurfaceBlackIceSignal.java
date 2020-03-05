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
 * Surface black ice signal as defined by essSurfaceStatus in NTCIP 1204.
 *
 * @author Douglas Lau
 */
public enum EssSurfaceBlackIceSignal {
	undefined,     // 0
	other,         // 1
	noIce,         // 2
	blackIce,      // 3
	detectorError; // 4

	/** Is there an interesting value? */
	public boolean isValue() {
		switch (this) {
			case noIce:
			case blackIce:
			case detectorError:
				return true;
			default:
				return false;
		}
	}
}
